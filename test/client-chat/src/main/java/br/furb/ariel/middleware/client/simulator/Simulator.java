package br.furb.ariel.middleware.client.simulator;

import br.furb.ariel.middleware.client.config.Config;
import br.furb.ariel.middleware.client.dto.MessageDTO;
import br.furb.ariel.middleware.client.utils.RandomUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
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
    private final Map<String, Semaphore> semaphoreByIds = new ConcurrentHashMap<>();
    private final Random random = new Random();
    private final BlockingQueue<MessageDTO> toPublish = new ArrayBlockingQueue<>(1000);

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
            System.out.println(this.clientId + " - publishing a new message");

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
                    MessageDTO message = this.toPublish.poll(99999, TimeUnit.DAYS);
                    if (message != null) {
                        System.out.println(this.clientId + " - Sending a new Message " + message.getId());

                        Semaphore semaphore = new Semaphore(0);
                        this.semaphoreByIds.put(message.getId(), semaphore);

                        this.ws.sendText(new ObjectMapper().writeValueAsString(message), true);

                        semaphore.tryAcquire(2, TimeUnit.SECONDS);

                        if (this.semaphoreByIds.containsKey(message.getId())) {
                            System.out.println(this.clientId + " - Middleware took so much time to answer");
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
        this.toPublish.add(messageDTO);
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
                    System.out.println(Simulator.this.clientId + " - Received OK for message " + answerId);
                    Semaphore semaphore = Simulator.this.semaphoreByIds.remove(answerId);
                    if (semaphore != null) {
                        semaphore.release();
                    }
                } else {
                    String id = dto.getId();
                    MessageDTO answer = MessageDTO.ok(id).build();
                    System.out.println(Simulator.this.clientId + " - Confirming message " + id + " from " + dto.getData().get("from") + " with " + answer.getId());
                    Simulator.this.publishToSend(answer);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static class NamedThreadFactory implements ThreadFactory {

        private final AtomicInteger threadNumber = new AtomicInteger(0);
        private final ThreadGroup group;
        private final String threadPrefix;

        public NamedThreadFactory(String threadPrefix) {
            SecurityManager s = System.getSecurityManager();
            this.group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            this.threadPrefix = threadPrefix;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r, threadPrefix + threadNumber.incrementAndGet(), 0);
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }
    }
}
