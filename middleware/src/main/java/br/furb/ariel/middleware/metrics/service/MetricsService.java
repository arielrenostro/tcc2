package br.furb.ariel.middleware.metrics.service;

import br.furb.ariel.middleware.metrics.model.Metric;
import br.furb.ariel.middleware.metrics.model.NewConnectionMetric;
import br.furb.ariel.middleware.metrics.repository.MetricsRepository;
import org.jboss.logging.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

@Singleton
public class MetricsService {

    private final ExecutorService executor;

    @Inject
    MetricsRepository repository;

    @Inject
    Logger logger;

    public MetricsService() {
        this.executor = Executors.newFixedThreadPool(2, new NamedThreadFactory("metrics-"));
    }

    public void publish(Metric metric) {
        this.executor.submit(() -> this.innerPublish(metric));
    }

    private void innerPublish(Metric metric) {
        try {
            this.repository.publish(metric);
        } catch (Exception e) {
            this.logger.error(e.getMessage(), e);
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
