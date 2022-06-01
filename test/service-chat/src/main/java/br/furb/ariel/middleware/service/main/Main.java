package br.furb.ariel.middleware.service.main;

import br.furb.ariel.middleware.service.broker.Broker;
import br.furb.ariel.middleware.service.config.Config;
import br.furb.ariel.middleware.service.dto.MessageDTO;
import br.furb.ariel.middleware.service.dto.ServiceNotificationDTO;
import br.furb.ariel.middleware.service.dto.ServiceNotificationType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Delivery;

import java.io.IOException;
import java.sql.Time;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Main {

    private final Broker broker = new Broker();
    private final ObjectMapper mapper = new ObjectMapper();

    private long lastMessageTime;

    public Main() throws IOException, InterruptedException, TimeoutException {
        this.broker.createRoutedExchange(Config.RABBITMQ_INPUT_QUEUE + ".dead", true);
        this.broker.createQueue(Config.RABBITMQ_INPUT_QUEUE + ".dead", null, null);
        this.broker.queueBind(Config.RABBITMQ_INPUT_QUEUE + ".dead", Config.RABBITMQ_INPUT_QUEUE + ".dead", "");

        this.broker.createQueue(Config.RABBITMQ_INPUT_QUEUE, Config.RABBITMQ_INPUT_QUEUE + ".dead", "");

        for (int i = 0; i < Config.RABBITMQ_CONSUMERS; i++) {
            System.out.println("Consuming queue " + Config.RABBITMQ_INPUT_QUEUE + " " + i);
            this.broker.consumeQueue(Config.RABBITMQ_INPUT_QUEUE, this::handle);
        }

        ScheduledExecutorService scheduled = Executors.newSingleThreadScheduledExecutor();
        scheduled.scheduleWithFixedDelay(() -> {
            System.out.println("Checking if need to get pending messages");
            if (this.lastMessageTime == 0 || this.lastMessageTime + (1000 * 60) < System.currentTimeMillis()) {
                System.out.println("Getting pending messages");
                ServiceNotificationDTO dto = new ServiceNotificationDTO();
                dto.setId(UUID.randomUUID().toString());
                dto.setType(ServiceNotificationType.GET_PENDING_MESSAGES);
                try {
                    sendToMiddleware(dto);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 5, 60, TimeUnit.SECONDS);
        Runtime.getRuntime().addShutdownHook(new Thread(scheduled::shutdownNow));
    }

    public static void main(String[] args) throws IOException, InterruptedException, TimeoutException {
        System.out.println("Starting Chat Service");
        new Main();
    }

    private void handle(Delivery message) {
        try {
            this.lastMessageTime = System.currentTimeMillis();

            MessageDTO messageDTO = this.mapper.readValue(message.getBody(), MessageDTO.class);
            System.out.println("Received a message from " + messageDTO.getFrom());

            answerOk(messageDTO);

            String to = (String) messageDTO.getData().get("to");
            String msg = (String) messageDTO.getData().get("message");

            sendMessage(messageDTO.getFrom(), to, msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(String from, String to, String msg) throws IOException, InterruptedException, TimeoutException {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.HOUR_OF_DAY, 24);
        Date ttl = calendar.getTime();

        ServiceNotificationDTO dto = new ServiceNotificationDTO();
        dto.setId(UUID.randomUUID().toString());
        dto.setTtl(ttl);
        dto.setType(ServiceNotificationType.SEND_MESSAGE);
        dto.setClientId(to);
        dto.setData(Map.of( //
                "from", from, //
                "message", msg //
        ));
        sendToMiddleware(dto);
    }

    private void answerOk(MessageDTO messageDTO) throws IOException, InterruptedException, TimeoutException {
        ServiceNotificationDTO ok = new ServiceNotificationDTO();
        ok.setId(UUID.randomUUID().toString());
        ok.setAnswerId(messageDTO.getId());
        ok.setType(ServiceNotificationType.CONFIRM_MESSAGE);
        sendToMiddleware(ok);
    }

    private void sendToMiddleware(ServiceNotificationDTO obj) throws IOException, InterruptedException, TimeoutException {
        byte[] bytes = this.mapper.writeValueAsBytes(obj);
        this.broker.publishExchange(Config.RABBITMQ_MIDDLEWARE_NOTIFICATION_EXCHANGE, "", Map.of("serviceId", Config.SERVICE_ID), bytes);
    }
}
