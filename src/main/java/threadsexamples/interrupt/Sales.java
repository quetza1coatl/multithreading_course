package threadsexamples.interrupt;

import java.time.LocalDateTime;


public class Sales {
    private static int[] salesByDay = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

    public static void main(String[] args) {
        int startDay = Integer.parseInt(args[0]);
        int endDay = Integer.parseInt(args[1]);
        Sales sales = new Sales();

        Thread calculationThread = new Thread(() -> sales.calculateTotal(startDay, endDay), "calculation-thread");
        Thread backupThread = new Thread(sales::createBackup, "backup-thread");

        calculationThread.start();
        backupThread.start();

        try {
            calculationThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        backupThread.interrupt();
        try {
            backupThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("We have got the result of work");

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
        for (int z = startDay; z < endDay; z++) {
            salesForPeriod += salesByDay[z];
        }
        Thread current = Thread.currentThread();
        System.out.println(String.format(
                "Thread name: %s%nThread id: %d. Total sales are: %d, start day is %d, end day is %d",
                current.getName(), current.getId(), salesForPeriod, startDay, endDay));
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            System.out.println(String.format("The thread %s was interrupted", current.getName()));
            return;
        }


    }

}
