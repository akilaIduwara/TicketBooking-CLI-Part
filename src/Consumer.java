import java.util.concurrent.atomic.AtomicBoolean;

class Consumer implements Runnable {
    private final TicketPool ticketPool;
    private final AtomicBoolean isRunning;

    public Consumer(TicketPool ticketPool, AtomicBoolean isRunning) {
        this.ticketPool = ticketPool;
        this.isRunning = isRunning;
    }

    @Override
    public void run() {
        while (isRunning.get()) {
            int retrievedTickets = ticketPool.retrieveTickets();

            if (retrievedTickets == 0) {
                System.out.println(ticketPool.getName() + " ticket pool is empty. Stopping ticket retrieval.");
                isRunning.set(false);
                break;
            }

            try {
                Thread.sleep(1000 / ticketPool.getCustomerRetrievalRate());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}