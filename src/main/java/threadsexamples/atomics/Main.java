package threadsexamples.atomics;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings("all")
public class Main {

    private AtomicInteger sharedVariable = new AtomicInteger(0);
    private volatile boolean isStopped = false;
    private volatile AtomicReference<Set<Integer>> cache = new AtomicReference<>(new HashSet<>());

    public static void main(String[] args) throws InterruptedException {
        Main main = new Main();

        Thread writerThread1 = new Thread(() -> {
            while (!main.isStopped) {
                int newValue = main.sharedVariable.incrementAndGet();

                while (true) {
                    Set<Integer> currentCache = main.cache.get();
                    Set<Integer> tempCache = new HashSet<>(currentCache);
                    tempCache.add(newValue);
                    if (main.cache.compareAndSet(currentCache, tempCache))
                        break;
                }

                System.out.println(Thread.currentThread().getName() + ":: The new value of the shared variable is: " + newValue);

                Set<Integer> tempCache = main.cache.get();
                System.out.println(Thread.currentThread().getName() + ":: The content of the cache is: " + tempCache);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // ignore
                }
            }
        });

        Thread writerThread2 = new Thread(() -> {
            while (!main.isStopped) {
                int newValue = main.sharedVariable.incrementAndGet();
                System.out.println(Thread.currentThread().getName() + ":: The new value of the shared variable is: " + newValue);

                while (true) {
                    Set<Integer> currentCache = main.cache.get();
                    Set<Integer> tempCache = new HashSet<>(currentCache);
                    tempCache.add(newValue);
                    if (main.cache.compareAndSet(currentCache, tempCache))
                        break;
                }

                Set<Integer> tempCache = main.cache.get();
                System.out.println(Thread.currentThread().getName() + ":: The content of the cache is: " + tempCache);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // ignore
                }
            }
        });

        Thread readerThread = new Thread(() -> {
            while (!main.isStopped) {
                System.out.println(Thread.currentThread().getName() + ":: Shared variable is: " + main.sharedVariable.get());

                Set<Integer> tempCache = main.cache.get();
                System.out.println(Thread.currentThread().getName() + ":: The content of the cache is: " + tempCache);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // ignore
                }
            }
        });

        writerThread1.setName("writer-1-thread");
        writerThread2.setName("writer-2-thread");
        readerThread.setName("reader-thread");
        writerThread1.start();
        writerThread2.start();
        readerThread.start();

        Thread.sleep(4000);

        main.isStopped = true;

        writerThread1.join();
        writerThread2.join();
        readerThread.join();
    }
}
