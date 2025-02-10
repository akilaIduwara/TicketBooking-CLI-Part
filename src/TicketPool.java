import java.util.Scanner;

class TicketPool {
    private final String name;
    private int totalTickets;
    private int ticketReleaseRate;
    private int customerRetrievalRate;
    private int maxTicketCapacity;
    private int currentTickets;

    public TicketPool(String name) {
        this.name = name;
    }

    public synchronized void configureSystem(Scanner scanner) {
        System.out.println("Configuring " + name + " ticket system...");
        System.out.print("Enter total tickets: ");
        totalTickets = scanner.nextInt();
        System.out.print("Enter ticket release rate (tickets/sec): ");
        ticketReleaseRate = scanner.nextInt();
        System.out.print("Enter customer retrieval rate (tickets/sec): ");
        customerRetrievalRate = scanner.nextInt();
        System.out.print("Enter maximum ticket capacity: ");
        maxTicketCapacity = scanner.nextInt();
        currentTickets = 0;
        System.out.println(name + " ticket system configured successfully.");
    }

    public synchronized void changeDetails(Scanner scanner) {
        System.out.println("Changing details for " + name + " ticket system...");
        configureSystem(scanner);
    }

    public synchronized void ticketPoolStatus() {
        System.out.println(name + " Ticket Pool Status:");
        System.out.println("Total Tickets: " + totalTickets);
        System.out.println("Current Tickets: " + currentTickets);
        System.out.println("Ticket Release Rate: " + ticketReleaseRate);
        System.out.println("Customer Retrieval Rate: " + customerRetrievalRate);
        System.out.println("Maximum Ticket Capacity: " + maxTicketCapacity);
    }

    public synchronized boolean isConfigured() {
        return totalTickets > 0 && ticketReleaseRate > 0 && customerRetrievalRate > 0 && maxTicketCapacity > 0;
    }

    public synchronized void loadConfiguration(int totalTickets, int ticketReleaseRate, int customerRetrievalRate, int maxTicketCapacity) {
        this.totalTickets = totalTickets;
        this.ticketReleaseRate = ticketReleaseRate;
        this.customerRetrievalRate = customerRetrievalRate;
        this.maxTicketCapacity = maxTicketCapacity;
        currentTickets = 0;
    }

    public synchronized void addTickets() {
        if (totalTickets > 0 && currentTickets < maxTicketCapacity) {
            int ticketsToAdd = Math.min(totalTickets, ticketReleaseRate);
            currentTickets += ticketsToAdd;
            totalTickets -= ticketsToAdd;
            System.out.println(name + " tickets added: " + ticketsToAdd);
        } else {
            System.out.println(name + " ticket pool is at capacity.");
        }
    }

    public synchronized int retrieveTickets() {
        if (currentTickets > 0) {
            int ticketsToRetrieve = Math.min(currentTickets, customerRetrievalRate);
            currentTickets -= ticketsToRetrieve;
            System.out.println(name + " tickets Selling : " + ticketsToRetrieve);
            return ticketsToRetrieve;
        } else {
            System.out.println(name + " ticket pool is empty.");
            return 0;
        }
    }

    public synchronized boolean isAtMaxCapacity() {
        return currentTickets >= maxTicketCapacity;
    }

    public synchronized int getCurrentTickets() {
        return currentTickets;
    }

    public String getName() {
        return name;
    }

    public synchronized int getTotalTickets() {
        return totalTickets;
    }

    public synchronized int getTicketReleaseRate() {
        return ticketReleaseRate;
    }

    public synchronized int getCustomerRetrievalRate() {
        return customerRetrievalRate;
    }

    public synchronized int getMaxTicketCapacity() {
        return maxTicketCapacity;
    }
}