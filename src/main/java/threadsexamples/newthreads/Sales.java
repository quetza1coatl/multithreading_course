package threadsexamples.newthreads;

public class Sales {
    private static int[] salesByDay = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

    public static void main(String[] args) {
        int startDay =  Integer.parseInt(args[0]);
        int endDay =  Integer.parseInt(args[1]);

        Sales sales = new Sales();
        Thread thread = new Thread(() -> sales.calculateTotal(startDay, endDay),"calculation-thread");
        thread.start();

    }

    private void calculateTotal(int startDay, int endDay){
        int salesForPeriod = 0;
        for (int z = startDay; z < endDay; z++){
            salesForPeriod += salesByDay[z];
        }
        System.out.println("Total sales are: " + salesForPeriod
                + ", start day is " + startDay + ", end day is " + endDay);

        Thread current = Thread.currentThread();
        System.out.println(String.format("Thread name: %s%nThread id: %d", current.getName(), current.getId()));
    }
}
