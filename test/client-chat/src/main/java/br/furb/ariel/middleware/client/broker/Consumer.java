package br.furb.ariel.middleware.client.broker;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Delivery;

import java.io.IOException;

public class Consumer implements DeliverCallback {

    private final Handler runnable;
    private final Channel channel;

    public Consumer(Handler runnable, Channel channel) {
        this.runnable = runnable;
        this.channel = channel;
    }

    @Override
    public void handle(String consumerTag, Delivery message) throws IOException {
        try {
            this.runnable.run(message);
            this.ack(message);
        } catch (Exception e) {
            e.printStackTrace();
            this.nack(message, false);
        }
    }

    protected void nack(Delivery delivery, boolean requeue) throws IOException {
        final long deliveryTag = delivery.getEnvelope().getDeliveryTag();
        this.channel.basicNack(deliveryTag, false, requeue);
    }

    protected void ack(Delivery delivery) throws IOException {
        final long deliveryTag = delivery.getEnvelope().getDeliveryTag();
        this.channel.basicAck(deliveryTag, false);
    }

    public interface Handler {

        void run(Delivery message);

    }
}
