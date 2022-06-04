package br.furb.ariel.middleware.client.broker;

import br.furb.ariel.middleware.client.config.Config;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import com.rabbitmq.client.impl.DefaultCredentialsProvider;
import com.rabbitmq.client.impl.DefaultCredentialsRefreshService.DefaultCredentialsRefreshServiceBuilder;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeoutException;

public class Broker {

    private final ArrayBlockingQueue<Channel> publishChannels = new ArrayBlockingQueue<>(1);
    private final Semaphore publishChannelSemaphore = new Semaphore(1);

    private ConnectionFactory connectionFactory;
    private Connection connection;

    public void createBroadcastExchange(String exchangeName, boolean durable) throws IOException, InterruptedException, TimeoutException {
        Channel channel = getPublishChannel();
        try {
            channel.exchangeDeclare(exchangeName, BuiltinExchangeType.TOPIC, durable);
        } finally {
            releasePublishChannel(channel);
        }
    }

    public void createQueue(String queueName, String deadQueueName, String deadRoutingKey) throws IOException, TimeoutException, InterruptedException {
        Channel channel = getPublishChannel();

        try {
            Map<String, Object> queueArgs = new HashMap<>();
            queueArgs.put("x-queue-mode", "lazy");
            if (deadQueueName != null && !deadQueueName.isEmpty()) {
                queueArgs.put("x-dead-letter-exchange", deadQueueName);
            }
            if (deadRoutingKey != null && !deadRoutingKey.isEmpty()) {
                queueArgs.put("x-dead-letter-routing-key", deadRoutingKey);
            }

            channel.queueDeclare(queueName, true, false, false, queueArgs);
        } finally {
            releasePublishChannel(channel);
        }
    }

    public void queueBind(String queueName, String exchangeName, String routingKey) throws IOException, TimeoutException, InterruptedException {
        Channel channel = getPublishChannel();
        try {
            channel.queueBind(queueName, exchangeName, routingKey);
        } finally {
            releasePublishChannel(channel);
        }
    }

    public void consumeExchange(String exchangeName, String routingKey, String queueName, Consumer.Handler handler) throws IOException, TimeoutException {
        Channel channel = getConnection().createChannel();
        channel.queueDeclare(queueName, false, true, true, null);
        channel.queueBind(queueName, exchangeName, routingKey);

        channel.basicQos(1);
        channel.basicConsume(queueName, false, new Consumer(handler, channel), (consumerTag) -> {});
    }

    public void consumeQueue(String queueName, Consumer.Handler handler) throws IOException, TimeoutException {
        Channel channel = getConnection().createChannel();
        channel.basicQos(1);
        channel.basicConsume(queueName, false, new Consumer(handler, channel), (consumerTag) -> {});
    }

    public void publishExchange(String exchangeName, String routingKey, Map<String, Object> headers, byte[] data) throws IOException, InterruptedException, TimeoutException {
        Channel channel = getPublishChannel();
        try {
            BasicProperties properties = MessageProperties.BASIC.builder().headers(headers).build();
            channel.basicPublish(exchangeName, routingKey, properties, data);
            channel.waitForConfirmsOrDie(Config.RABBITMQ_PUBLISH_TIMEOUT);
        } finally {
            releasePublishChannel(channel);
        }
    }

    public void publishQueue(String queueName, Map<String, Object> headers, byte[] data) throws IOException, InterruptedException, TimeoutException {
        Channel channel = getPublishChannel();
        try {
            BasicProperties properties = MessageProperties.BASIC.builder().headers(headers).build();
            channel.basicPublish("", queueName, properties, data);
            channel.waitForConfirmsOrDie(Config.RABBITMQ_PUBLISH_TIMEOUT);
        } finally {
            releasePublishChannel(channel);
        }
    }

    private Channel getPublishChannel() throws IOException, TimeoutException, InterruptedException {
        if (Thread.interrupted()) {
            throw new InterruptedException();
        }
        try {
            this.publishChannelSemaphore.acquire();

            while (!this.publishChannels.isEmpty()) {
                Channel channel = this.publishChannels.poll();
                if (channel.isOpen()) {
                    return channel;
                }
                closeIgnoreException(channel);
            }

            Channel channel = getConnection().createChannel();
            channel.confirmSelect();
            return channel;

        } catch (InterruptedException e) {
            e.printStackTrace();
            this.publishChannelSemaphore.release();
            throw e;
        }
    }

    private void releasePublishChannel(Channel channel) {
        try {
            if (channel != null) {
                this.publishChannels.add(channel);
            }
        } finally {
            this.publishChannelSemaphore.release();
        }
    }

    private void closeIgnoreException(Channel channel) {
        try {
            if (channel != null) {
                channel.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Connection getConnection() throws IOException, TimeoutException {
        if (this.connection == null || !this.connection.isOpen()) {
            this.connection = getConnectionFactory().newConnection();
        }
        return this.connection;
    }

    private ConnectionFactory getConnectionFactory() {
        if (this.connectionFactory == null) {
            this.connectionFactory = new ConnectionFactory();
            this.connectionFactory.setHost(Config.RABBITMQ_HOST);
            if (Config.RABBITMQ_SSL) {
                try {
                    this.connectionFactory.useSslProtocol();
                } catch (NoSuchAlgorithmException | KeyManagementException e) {
                    throw new RuntimeException(e);
                }
            }
            this.connectionFactory.setPort(Config.RABBITMQ_PORT);
            this.connectionFactory.setVirtualHost(Config.RABBITMQ_VHOST);
            this.connectionFactory.setCredentialsProvider(new DefaultCredentialsProvider(Config.RABBITMQ_USERNAME, Config.RABBITMQ_PASSWORD));
            this.connectionFactory.setCredentialsRefreshService(new DefaultCredentialsRefreshServiceBuilder().build());
        }
        return this.connectionFactory;
    }
}
