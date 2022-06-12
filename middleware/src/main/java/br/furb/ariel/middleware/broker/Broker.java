package br.furb.ariel.middleware.broker;

import br.furb.ariel.middleware.config.Config;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import com.rabbitmq.client.impl.DefaultCredentialsProvider;
import com.rabbitmq.client.impl.DefaultCredentialsRefreshService.DefaultCredentialsRefreshServiceBuilder;
import org.jboss.logging.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeoutException;

@Singleton
public class Broker {

    private final ArrayBlockingQueue<Channel> publishChannels = new ArrayBlockingQueue<>(1);
    private final Semaphore publishChannelSemaphore = new Semaphore(1);

    private ConnectionFactory connectionFactory;
    private Connection connection;

    @Inject
    Logger logger;

    @Inject
    Config config;

    public void createRoutedExchange(String exchangeName, boolean durable) throws IOException, InterruptedException, TimeoutException {
        Channel channel = getPublishChannel();
        try {
            channel.exchangeDeclare(exchangeName, BuiltinExchangeType.DIRECT, durable);
        } finally {
            releasePublishChannel(channel);
        }
    }

    public void createQueue(String queueName, boolean durable) throws IOException, InterruptedException, TimeoutException {
        Channel channel = getPublishChannel();
        try {
            channel.queueDeclare(queueName, durable, false, false, null);
        } finally {
            releasePublishChannel(channel);
        }
    }

    public void consumeExchange(String exchangeName, String routingKey, String queueName, Consumer.Handler handler) throws IOException, TimeoutException {
        Channel channel = getConnection().createChannel();
        channel.queueDeclare(queueName, false, true, true, null);
        channel.queueBind(queueName, exchangeName, routingKey);

        channel.basicQos(4);
        channel.basicConsume(queueName, false, new Consumer(handler, channel), (consumerTag) -> {});
    }

    public void consumeQueue(String queueName, Consumer.Handler handler) throws IOException, TimeoutException {
        Channel channel = getConnection().createChannel();

        channel.basicQos(4);
        channel.basicConsume(queueName, false, new Consumer(handler, channel), (consumerTag) -> {});
    }

    public void queueBind(String exchangeName, String routeKey, String queueName) throws IOException, InterruptedException, TimeoutException {
        Channel channel = getPublishChannel();
        try {
            channel.queueBind(queueName, exchangeName, routeKey);
        } finally {
            releasePublishChannel(channel);
        }
    }

    public void publishExchange(String exchangeName, String routingKey, Map<String, Object> headers, byte[] data) throws IOException, InterruptedException, TimeoutException {
        Channel channel = getPublishChannel();
        try {
            BasicProperties properties = MessageProperties.BASIC.builder().headers(headers).build();
            channel.basicPublish(exchangeName, routingKey, properties, data);
            channel.waitForConfirmsOrDie(this.config.getRabbitmqTimeoutPublish());
        } finally {
            releasePublishChannel(channel);
        }
    }

    public void publishQueue(String queueName, Map<String, Object> headers, byte[] data) throws IOException, InterruptedException, TimeoutException {
        Channel channel = getPublishChannel();
        try {
            BasicProperties properties = MessageProperties.BASIC.builder().headers(headers).build();
            channel.basicPublish("", queueName, properties, data);
            channel.waitForConfirmsOrDie(this.config.getRabbitmqTimeoutPublish());
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
            this.logger.error(e.getMessage(), e);
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
            this.logger.error(e.getMessage());
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
            this.connectionFactory.setHost(this.config.getRabbitmqHost());
            if (this.config.isRabbitmqSSL()) {
                try {
                    this.connectionFactory.useSslProtocol();
                } catch (NoSuchAlgorithmException | KeyManagementException e) {
                    throw new RuntimeException(e);
                }
            }
            this.connectionFactory.setPort(this.config.getRabbitmqPort());
            this.connectionFactory.setVirtualHost(this.config.getRabbitmqVhost());
            this.connectionFactory.setCredentialsProvider(new DefaultCredentialsProvider(this.config.getRabbitmqUsername(), this.config.getRabbitmqPassword()));
            this.connectionFactory.setCredentialsRefreshService(new DefaultCredentialsRefreshServiceBuilder().build());
        }
        return this.connectionFactory;
    }
}
