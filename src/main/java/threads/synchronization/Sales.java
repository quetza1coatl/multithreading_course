package threads.synchronization;

import java.time.LocalDateTime;

@SuppressWarnings("all")
public class Sales {
    private static int[] salesByDay = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
    private long totalSales = 0; // a shared variable between 2 threads

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

    }

    private void createBackup() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.out.println("The backup thread is interrupted");
                return;
            }

            // writing data into the file...

            System.out.println(LocalDateTime.now() + " the backup has created");
            if (Thread.currentThread().isInterrupted()) {
                return;
            }
        }
    }

    private void calculateTotal(int startDay, int endDay) {
        int salesForPeriod = 0;
        Thread current = Thread.currentThread();
        for (int z = startDay; z < endDay; z++) {
            salesForPeriod += salesByDay[z];
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                System.out.println(String.format("The thread %s was interrupted", current.getName()));
                return;
            }
        }
        addPartialSales(salesForPeriod);

        System.out.println(String.format(
                "Thread name: %s%nThread id: %d. Partial sales are: %d, start day is %d, end day is %d",
                current.getName(), current.getId(), salesForPeriod, startDay, endDay));
    }

    synchronized public void addPartialSales(long partialSales) {
        // this.monitor.lock
        System.out.println("A monitor of the object " + this + " is locked by the thread "
                + Thread.currentThread().getName());

        totalSales += partialSales;

        System.out.println("A monitor of the object " + this + " is unlocked by the thread "
                + Thread.currentThread().getName());
        // this.monitor.unlock
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
