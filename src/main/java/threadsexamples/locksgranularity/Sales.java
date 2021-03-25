package threadsexamples.locksgranularity;

import java.time.LocalDateTime;

@SuppressWarnings("all")
public class Sales {
    private static int[] salesByDay = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
    private long totalSales = 0; // a shared variable between 2 threads
    private long maxSoldItems = 0; // a shared variable
    private final Object totalSalesLock = new Object();
    private final Object maxSoldItemsLock = new Object();

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        int startDay = Integer.parseInt(args[0]);
        int endDay = Integer.parseInt(args[1]);
        int daysPerThread = (int)Math.ceil((endDay - startDay) / 2.0);
        Sales sales = new Sales();

        Thread calculationThread1 =  new CalculationThread(sales, startDay, daysPerThread + startDay, "calculation-thread-1");
        Thread calculationThread2 =  new CalculationThread(sales, daysPerThread +startDay, endDay, "calculation-thread-2");
        Thread backupThread = new Thread(sales::createBackup, "backup-thread");

        calculationThread1.start();
        calculationThread2.start();
        backupThread.start();

        try {
            calculationThread1.join();
            calculationThread2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        backupThread.interrupt();
        try {
            backupThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long timeDiff = System.currentTimeMillis() - startTime;
        System.out.println("Total sales are: " + sales.totalSales
                + ", execution of the program took: " + timeDiff + " ms.");
        System.out.println("The maximum of sold items is: " + sales.maxSoldItems);

    }

    // guarantee for 'happens before' relationships, when we will try to read values
    public long getTotalSales() {
        synchronized (totalSalesLock) {
        return totalSales;
        }
    }

    private void createBackup() {
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // writing data into the file...
            System.out.println("Total sales the backup value is: " + getTotalSales());

            System.out.println(LocalDateTime.now() + " the backup has created");
            if (Thread.currentThread().isInterrupted()) {
                return;
            }
        }
    }

    private void calculateTotal(int startDay, int endDay) {
        int salesForPeriod = 0;
        int maxSoldItems = 0;
        Thread current = Thread.currentThread();
        for (int z = startDay; z < endDay; z++) {
            salesForPeriod += salesByDay[z];
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                System.out.println(String.format("The thread %s was interrupted", current.getName()));
                return;
            }
            maxSoldItems = Math.max(maxSoldItems, salesByDay[z]);
        }
        applyCalculationResult(salesForPeriod, maxSoldItems);

        System.out.println(String.format(
                "Thread name: %s%nThread id: %d. Partial sales are: %d, start day is %d, end day is %d",
                current.getName(), current.getId(), salesForPeriod, startDay, endDay));
    }

    public void applyCalculationResult(long partialSales, long maxItems) {
        synchronized (totalSalesLock) {  // totalSalesLock.monitor.lock
            System.out.println("A monitor of the object " + totalSalesLock + " is locked by the thread "
                    + Thread.currentThread().getName());

            totalSales += partialSales;

            System.out.println("A monitor of the object " + totalSalesLock + " is unlocked by the thread "
                    + Thread.currentThread().getName());
        }  // totalSalesLock.monitor.unlock
        synchronized (maxSoldItemsLock) {

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                // ignore
            }
            maxSoldItems = Math.max(maxSoldItems, maxItems);
        }
    }

    static class CalculationThread extends Thread{
        final Sales sales;
        final int startDay;
        final int endDay;

        public CalculationThread(Sales sales, int startDay, int endDay, String threadName) {
            super(threadName);
            this.sales = sales;
            this.startDay = startDay;
            this.endDay = endDay;
        }

        @Override
        public void run() {
            sales.calculateTotal(startDay, endDay);
        }
    }

}
