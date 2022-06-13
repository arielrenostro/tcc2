package br.furb.ariel.middleware.client.simulator;

import br.furb.ariel.middleware.client.config.Config;
import br.furb.ariel.middleware.client.dto.MessageDTO;
import br.furb.ariel.middleware.client.metrics.ConnectionMetric;
import br.furb.ariel.middleware.client.metrics.DisconnectionMetric;
import br.furb.ariel.middleware.client.metrics.MessageReceivedMetric;
import br.furb.ariel.middleware.client.metrics.MetricsService;
import br.furb.ariel.middleware.client.metrics.MiddlewareTimeoutMetric;
import br.furb.ariel.middleware.client.metrics.RateController;
import br.furb.ariel.middleware.client.utils.NamedThreadFactory;
import br.furb.ariel.middleware.client.utils.RandomUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

class Simulator {

    private final String clientId;
    private final long millisBetweenMessages;

    private final ScheduledExecutorService executor;
    private final Map<String, SentMessage> semaphoreByIds = new ConcurrentHashMap<>();
    private final Random random = new Random();
    private final BlockingQueue<MessageDTO> toSend = new ArrayBlockingQueue<>(1000);
    private final RateController sendMessageRateController = new RateController("send");
    private final RateController receiveMessageRateController = new RateController("receive");

    private WebSocket ws;
    private boolean stoped;

    Simulator(String clientId, long millisBetweenMessages) {
        this.clientId = clientId;
        this.millisBetweenMessages = millisBetweenMessages;
        this.executor = Executors.newScheduledThreadPool(20, new NamedThreadFactory("simulator-" + this.clientId + "-"));
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
        startSender();
        startScheduler();
        startRate();
    }

    private void startRate() {
        this.executor.scheduleWithFixedDelay(this.sendMessageRateController::generateRate, 0, 10, TimeUnit.SECONDS);
        this.executor.scheduleWithFixedDelay(this.receiveMessageRateController::generateRate, 0, 10, TimeUnit.SECONDS);
    }

    private void startScheduler() {
        this.executor.scheduleWithFixedDelay(() -> {
            logDebug(this.clientId + " - publishing a new message");

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
        }, this.millisBetweenMessages, this.millisBetweenMessages, TimeUnit.MILLISECONDS);
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
                Thread.sleep(Config.WEBSOCKET_AFTER_CONNECTION_DELAY);
            } catch (Exception e) {
                atomicException.set(e);
            }
            latch.countDown();
        });
        latch.await();
        if (atomicException.get() != null) {
            throw atomicException.get();
        }
        MetricsService.getInstance().publish(new ConnectionMetric());
    }

    private void startSender() {
        this.executor.submit(() -> {
            while (!this.stoped) {
                try {
                    if (this.ws.isOutputClosed()) {
                        this.executor.submit(Simulator.this::stop);
                        return;
                    }

                    MessageDTO message = this.toSend.poll(99999, TimeUnit.DAYS);
                    if (message != null) {
                        send(message);
                    }
                } catch (InterruptedException ignored) {
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void send(MessageDTO message) throws JsonProcessingException, InterruptedException {
        logDebug(this.clientId + " - Sending a new Message " + message.getId());

        SentMessage sentMessage = new SentMessage(new Semaphore(0));
        this.semaphoreByIds.put(message.getId(), sentMessage);

        this.ws.sendText(new ObjectMapper().writeValueAsString(message), true);

        int i;
        for (i = 0; i < Config.WEBSOCKET_RECEIVE_MESSAGE_RETRY; i++) {
            sentMessage.getSemaphore().tryAcquire(Config.WEBSOCKET_RECEIVE_MESSAGE_TIMEOUT, TimeUnit.SECONDS);
            if (!this.semaphoreByIds.containsKey(message.getId())) {
                if (message.getAnswerId() == null) {
                    this.sendMessageRateController.newMessage();
                }
                break;
            }
            MetricsService.getInstance().publish(new MiddlewareTimeoutMetric());
            System.out.println(this.clientId + " - Middleware took so much time to answer");
        }

        if (i == Config.WEBSOCKET_RECEIVE_MESSAGE_RETRY) {
            MetricsService.getInstance().publish(new DisconnectionMetric());
            System.out.println(this.clientId + " - Middleware didn't answer after " + Config.WEBSOCKET_RECEIVE_MESSAGE_RETRY + " retries");
            this.executor.submit(Simulator.this::stop);
        }
    }

    private void receive(byte[] bytes) {
        try {
            MessageDTO dto = new ObjectMapper().readValue(bytes, MessageDTO.class);

            String answerId = dto.getAnswerId();
            if (answerId != null) {
                Object status = dto.getData().get("status");
                boolean ok = Objects.equals("OK", status);
                if (ok) {
                    logDebug(this.clientId + " - Received OK for message " + answerId);
                } else {
                    System.out.println(this.clientId + " - Received " + status + " for message " + answerId + ": " + dto.getData().get("message"));
                }

                SentMessage sentMessage = this.semaphoreByIds.remove(answerId);
                if (sentMessage == null) {
                    System.out.println(this.clientId + " - Semaphore not found for " + answerId);
                    return;
                }

                sentMessage.getSemaphore().release();
                long ellapsed = System.nanoTime() - sentMessage.getTime();
                MetricsService.getInstance().publish(new MessageReceivedMetric(ok, ellapsed));

            } else {
                String id = dto.getId();
                Object from = dto.getData().get("from");

                MessageDTO answer = MessageDTO.ok(id).build();
                logDebug(this.clientId + " - Confirming message " + id + " from " + from + " with " + answer.getId());
                publishToSend(answer);
                this.receiveMessageRateController.newMessage();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void publishToSend(MessageDTO messageDTO) {
        this.toSend.add(messageDTO);
    }

    private void publishToReceive(byte[] bytes) {
        this.executor.submit(() -> receive(bytes));
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

    private void logDebug(String msg) {
        if (Config.DEBUG) {
            System.out.println(msg);
        }
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

            Simulator.this.publishToReceive(bytes);
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
