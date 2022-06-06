package br.furb.ariel.middleware.client.simulator;

import br.furb.ariel.middleware.client.config.Config;
import br.furb.ariel.middleware.client.dto.MessageDTO;
import br.furb.ariel.middleware.client.metrics.MessageReceivedMetric;
import br.furb.ariel.middleware.client.metrics.MetricsService;
import br.furb.ariel.middleware.client.metrics.MiddlewareTimeoutMetric;
import br.furb.ariel.middleware.client.utils.NamedThreadFactory;
import br.furb.ariel.middleware.client.utils.RandomUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

class Simulator {

    public final String clientId;
    public final long millisBetweenMessages;

    private final ScheduledExecutorService executor;
    private final Map<String, SentMessage> semaphoreByIds = new ConcurrentHashMap<>();
    private final Random random = new Random();
    private final BlockingQueue<MessageDTO> toSend = new ArrayBlockingQueue<>(1000);

    private WebSocket ws;
    private boolean stoped;

    Simulator(String clientId, long millisBetweenMessages) {
        this.clientId = clientId;
        this.millisBetweenMessages = millisBetweenMessages;
        this.executor = Executors.newScheduledThreadPool(3, new NamedThreadFactory("simulator-" + this.clientId + "-"));
    }

    public void stop() {
        this.stoped = true;
        System.out.println(this.clientId + " - Stopping");
        if (this.ws != null) {
            this.ws.abort();
        }
        this.executor.shutdownNow();
    }

    public void start() throws Exception {
        System.out.println(this.clientId + " - Starting");

        startListener();
        ClientSimulatorService.getInstance().registerClientId(this.clientId);

        startSender();
        startScheduler();
    }

    private void startScheduler() {
        this.executor.scheduleWithFixedDelay(() -> {
            if (Config.DEBUG) {
                System.out.println(this.clientId + " - publishing a new message");
            }

            String destination = ClientSimulatorService.getInstance().peekClient(this.clientId);
            String text = generateText();

            MessageDTO message = new MessageDTO();
            message.setId(UUID.randomUUID().toString());
            message.setRoute("chat");
            message.setTtl(new Date(System.currentTimeMillis() + 30000));
            message.setData(Map.of( //
                    "to", destination, //
                    "message", text //
            ));
            publishToSend(message);
        }, 0, this.millisBetweenMessages, TimeUnit.MILLISECONDS);
    }

    private void startListener() throws Exception {
        AtomicReference<Exception> atomicException = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        this.executor.submit(() -> {
            try {
                this.ws = HttpClient.newHttpClient() //
                        .newWebSocketBuilder() //
                        .buildAsync(new URI(Config.WEBSOCKET_URL + "?id=" + this.clientId), new Listener()) //
                        .join();
                Thread.sleep(100);
            } catch (Exception e) {
                atomicException.set(e);
            }
            latch.countDown();
        });
        latch.await();
        if (atomicException.get() != null) {
            throw atomicException.get();
        }
    }

    private void startSender() {
        this.executor.submit(() -> {
            while (!this.stoped) {
                try {
                    if (this.ws.isOutputClosed()) {
                        Simulator.this.executor.submit(Simulator.this::stop);
                        break;
                    }
                    MessageDTO message = this.toSend.poll(99999, TimeUnit.DAYS);
                    if (message != null) {
                        if (Config.DEBUG) {
                            System.out.println(this.clientId + " - Sending a new Message " + message.getId());
                        }

                        SentMessage sentMessage = new SentMessage(new Semaphore(0));
                        this.semaphoreByIds.put(message.getId(), sentMessage);

                        this.ws.sendText(new ObjectMapper().writeValueAsString(message), true);

                        sentMessage.getSemaphore().tryAcquire(2, TimeUnit.SECONDS);

                        if (this.semaphoreByIds.containsKey(message.getId())) {
                            MetricsService.getInstance().publish(new MiddlewareTimeoutMetric());
                            System.out.println(this.clientId + " - Middleware took so much time to answer");
                            Simulator.this.executor.submit(Simulator.this::stop);
                            break;
                        }
                    }
                } catch (InterruptedException ignored) {
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void publishToSend(MessageDTO messageDTO) {
        this.toSend.add(messageDTO);
    }

    private String generateText() {
        int size = RandomUtils.range(this.random, Config.MIN_MESSAGE_SIZE, Config.MAX_MESSAGE_SIZE);
        StringBuilder sb = new StringBuilder(size);
        for (int i = 0; i < size; i++) {
            char value = (char) RandomUtils.range(this.random, 'A', 'Z');
            sb.append(value);
        }
        return sb.toString();
    }

    private class Listener implements WebSocket.Listener {

        private final List<Byte> data = new ArrayList<>(4096);

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            for (int i = 0; i < data.length(); i++) {
                this.data.add((byte) data.charAt(i));
            }
            if (last) {
                onMessage(webSocket);
            }
            return WebSocket.Listener.super.onText(webSocket, data, last);
        }

        @Override
        public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
            byte[] array = data.array();
            for (byte b : array) {
                this.data.add(b);
            }
            if (last) {
                onMessage(webSocket);
            }
            return WebSocket.Listener.super.onBinary(webSocket, data, last);
        }

        private void onMessage(WebSocket ws) {
            byte[] bytes = new byte[this.data.size()];
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = this.data.get(i);
            }
            this.data.clear();

            try {
                MessageDTO dto = new ObjectMapper().readValue(bytes, MessageDTO.class);

                String answerId = dto.getAnswerId();
                if (answerId != null) {
                    Object status = dto.getData().get("status");
                    boolean ok = Objects.equals("OK", status);
                    if (ok) {
                        if (Config.DEBUG) {
                            System.out.println(Simulator.this.clientId + " - Received OK for message " + answerId);
                        }
                    } else {
                        System.out.println(Simulator.this.clientId + " - Received " + status + " for message " + answerId + ": " + dto.getData().get("message"));
                    }
                    SentMessage sentMessage = Simulator.this.semaphoreByIds.remove(answerId);
                    if (sentMessage != null) {
                        sentMessage.getSemaphore().release();
                        long ellapsed = System.nanoTime() - sentMessage.getTime();
                        MetricsService.getInstance().publish(new MessageReceivedMetric(ok, ellapsed));
                    }
                } else {
                    String id = dto.getId();
                    MessageDTO answer = MessageDTO.ok(id).build();
                    if (Config.DEBUG) {
                        System.out.println(Simulator.this.clientId + " - Confirming message " + id + " from " + dto.getData().get("from") + " with " + answer.getId());
                    }
                    Simulator.this.publishToSend(answer);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static class SentMessage {

        private final Semaphore semaphore;
        private final long time;

        public SentMessage(Semaphore semaphore) {
            this.semaphore = semaphore;
            this.time = System.nanoTime();
        }

        public Semaphore getSemaphore() {
            return semaphore;
        }

        public long getTime() {
            return time;
        }
    }
}
