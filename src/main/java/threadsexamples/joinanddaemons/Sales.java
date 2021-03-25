package threadsexamples.joinanddaemons;

import java.time.LocalDateTime;


public class Sales {
    private static int[] salesByDay = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

    public static void main(String[] args) {
        int startDay =  Integer.parseInt(args[0]);
        int endDay =  Integer.parseInt(args[1]);

        Sales sales = new Sales();

        /* Daemon thread*/
        Thread daemon = new Thread (sales::createBackup, "daemon-backup-thread");
        // just comment it and application will never stop.
        daemon.setDaemon(true);

        /* Thread with join*/
        Thread calculationThread = new Thread(() -> sales.calculateTotal(startDay, endDay),"calculation-thread");
        calculationThread.start();
        daemon.start();
        // do something
        try {
            calculationThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(calculationThread.getName() + " has finished calculating\n");

        /* Thread without join*/
        Thread calculationThreadWithoutJoin = new Thread(() -> sales.calculateTotal(startDay, endDay),"calculation-thread-without-join");
        calculationThreadWithoutJoin.start();
        System.out.println(String.format("U will see it before %s will finish its calculations", calculationThread.getName()));

    }

    private void calculateTotal(int startDay, int endDay){
        int salesForPeriod = 0;
        for (int z = startDay; z < endDay; z++){
            salesForPeriod += salesByDay[z];
        }
        Thread current = Thread.currentThread();
        System.out.println(String.format(
                "Thread name: %s%nThread id: %d. Total sales are: %d, start day is %d, end day is %d",
                current.getName(), current.getId(), salesForPeriod, startDay, endDay));
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void createBackup() {
        while(true){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // write backup calculation data into  the file
            LocalDateTime now = LocalDateTime.now();
            System.out.println(String.format("%s -- %tT %tD -- backup has created",Thread.currentThread().getName(), now, now));
        }
    }
}
