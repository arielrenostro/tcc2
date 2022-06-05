package br.furb.ariel.middleware.client.utils;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NamedThreadFactory implements ThreadFactory {

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
