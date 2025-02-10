import java.util.concurrent.atomic.AtomicBoolean;

class Producer implements Runnable {
    private final TicketPool ticketPool;
    private final AtomicBoolean isRunning;
    private boolean isTicketPoolEmpty = false;

    public Producer(TicketPool ticketPool, AtomicBoolean isRunning) {
        this.ticketPool = ticketPool;
        this.isRunning = isRunning;
    }

    @Override
    public void run() {
        while (isRunning.get()) {
            if (ticketPool.getTotalTickets() == 0 && !isTicketPoolEmpty) {
                System.out.println(ticketPool.getName() + " ticket production completed. No more tickets to add.");
                isTicketPoolEmpty = true;
            }

            if (ticketPool.getTotalTickets() == 0 && ticketPool.getCurrentTickets() == 0) {
                System.out.println(ticketPool.getName() + " ticket pool is empty. Stopping ticket operation.");
                isRunning.set(false);
                break;
            }

            ticketPool.addTickets();

            while (ticketPool.isAtMaxCapacity()) {
                try {
                    Thread.sleep(1000);
                    System.out.println(ticketPool.getName() + " ticket pool is full. Waiting for vacancy.");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }

            try {
                Thread.sleep(1000 / ticketPool.getTicketReleaseRate());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}