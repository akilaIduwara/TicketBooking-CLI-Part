import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TicketSystem {
    private Scanner scanner;
    private TicketPool normalTicketPool;
    private TicketPool vipTicketPool;
    private ExecutorService executor;
    private AtomicBoolean isNormalOperationRunning;
    private AtomicBoolean isVIPOperationRunning;

    public TicketSystem() {
        scanner = new Scanner(System.in);
        normalTicketPool = new TicketPool("Normal");
        vipTicketPool = new TicketPool("VIP");
        isNormalOperationRunning = new AtomicBoolean(false);
        isVIPOperationRunning = new AtomicBoolean(false);
    }

    public void run() {
        while (true) {
            try {
                displayMenu();
                int choice = getUserChoice();
                processChoice(choice);
            } catch (Exception e) {
                System.out.println("An error occurred: " + e.getMessage());
                scanner.nextLine();
            }
        }
    }

    private void displayMenu() {
        System.out.println("\n--- Ticket System Menu ---\n");
        System.out.println("1. Configure Normal Ticket System");
        System.out.println("2. Configure VIP Ticket System");
        System.out.println("3. Start Normal Ticket Operation");
        System.out.println("4. Start VIP Ticket Operation");
        System.out.println("5. Change Normal Ticket Details");
        System.out.println("6. Change VIP Ticket Details");
        System.out.println("7. Normal Ticket Pool Status");
        System.out.println("8. VIP Ticket Pool Status");
        System.out.println("9. Stop All Ticket Operation");
        System.out.println("10. Save Ticket System Configuration");
        System.out.println("11. Load Ticket System Configuration");
        System.out.println("12. Exit");
        System.out.print("Enter your choice: ");
    }

    private int getUserChoice() {
        while (true) {
            try {
                return scanner.nextInt();
            } catch (Exception e) {
                System.out.println("Invalid input. Please enter a number.");
                scanner.nextLine();
            }
        }
    }

    private void processChoice(int choice) {
        switch (choice) {
            case 1: normalTicketPool.configureSystem(scanner); break;
            case 2: vipTicketPool.configureSystem(scanner); break;
            case 3: startNormalOperation(); break;
            case 4: startVIPOperation(); break;
            case 5: normalTicketPool.changeDetails(scanner); break;
            case 6: vipTicketPool.changeDetails(scanner); break;
            case 7: normalTicketPool.ticketPoolStatus(); break;
            case 8: vipTicketPool.ticketPoolStatus(); break;
            case 9: stopAllTicketOperations(); break;
            case 10: saveConfiguration(); break;
            case 11: loadConfiguration(); break;
            case 12: exitProgram(); break;
            default: System.out.println("Invalid choice. Please try again.");
        }
    }

    private void startNormalOperation() {
        if (isNormalOperationRunning.get()) {
            System.out.println("Normal ticket operation is already running.");
            return;
        }

        if (!normalTicketPool.isConfigured()) {
            System.out.println("Please configure the normal ticket system first.");
            return;
        }

        isNormalOperationRunning.set(true);
        executor = Executors.newCachedThreadPool();
        System.out.println("Starting Normal Ticket Operation...");

        Producer normalProducer = new Producer(normalTicketPool, isNormalOperationRunning);
        Consumer normalConsumer = new Consumer(normalTicketPool, isNormalOperationRunning);

        executor.submit(normalProducer);
        executor.submit(normalConsumer);
    }

    private void startVIPOperation() {
        if (isVIPOperationRunning.get()) {
            System.out.println("VIP ticket operation is already running.");
            return;
        }

        if (!vipTicketPool.isConfigured()) {
            System.out.println("Please configure the VIP ticket system first.");
            return;
        }

        isVIPOperationRunning.set(true);
        executor = Executors.newCachedThreadPool();
        System.out.println("Starting VIP Ticket Operation...");

        Producer vipProducer = new Producer(vipTicketPool, isVIPOperationRunning);
        Consumer vipConsumer = new Consumer(vipTicketPool, isVIPOperationRunning);

        executor.submit(vipProducer);
        executor.submit(vipConsumer);
    }

    private void stopAllTicketOperations() {
        if (!isNormalOperationRunning.get() && !isVIPOperationRunning.get()) {
            System.out.println("No ticket operations are currently running.");
            return;
        }

        isNormalOperationRunning.set(false);
        isVIPOperationRunning.set(false);
        shutdownExecutor();
        System.out.println("All ticket operations stopped.");
    }

    private void shutdownExecutor() {
        if (executor != null) {
            try {
                executor.shutdown();
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    private void saveConfiguration() {
        System.out.print("Enter filename to save configuration (e.g., config.json): ");
        scanner.nextLine();
        String filename = scanner.nextLine();

        try (FileWriter writer = new FileWriter(filename)) {
            String config = "{\n" +
                    "\"normal\": {\n" +
                    "\"totalTickets\": " + normalTicketPool.getTotalTickets() + ",\n" +
                    "\"ticketReleaseRate\": " + normalTicketPool.getTicketReleaseRate() + ",\n" +
                    "\"customerRetrievalRate\": " + normalTicketPool.getCustomerRetrievalRate() + ",\n" +
                    "\"maxTicketCapacity\": " + normalTicketPool.getMaxTicketCapacity() + "\n" +
                    "},\n" +
                    "\"vip\": {\n" +
                    "\"totalTickets\": " + vipTicketPool.getTotalTickets() + ",\n" +
                    "\"ticketReleaseRate\": " + vipTicketPool.getTicketReleaseRate() + ",\n" +
                    "\"customerRetrievalRate\": " + vipTicketPool.getCustomerRetrievalRate() + ",\n" +
                    "\"maxTicketCapacity\": " + vipTicketPool.getMaxTicketCapacity() + "\n" +
                    "}\n" +
                    "}";

            writer.write(config);
            System.out.println("Configuration saved successfully to " + filename);
        } catch (IOException e) {
            System.out.println("Error saving configuration: " + e.getMessage());
        }
    }

    private void loadConfiguration() {
        System.out.print("Enter filename to load configuration (e.g., config.json): ");
        scanner.nextLine();
        String filename = scanner.nextLine();

        try {
            String content = new String(Files.readAllBytes(Paths.get(filename)));

            int normalTotalTickets = parseIntValue(content, "\"normal\".*?\"totalTickets\":\\s*(\\d+)");
            int normalTicketReleaseRate = parseIntValue(content, "\"normal\".*?\"ticketReleaseRate\":\\s*(\\d+)");
            int normalCustomerRetrievalRate = parseIntValue(content, "\"normal\".*?\"customerRetrievalRate\":\\s*(\\d+)");
            int normalMaxTicketCapacity = parseIntValue(content, "\"normal\".*?\"maxTicketCapacity\":\\s*(\\d+)");

            int vipTotalTickets = parseIntValue(content, "\"vip\".*?\"totalTickets\":\\s*(\\d+)");
            int vipTicketReleaseRate = parseIntValue(content, "\"vip\".*?\"ticketReleaseRate\":\\s*(\\d+)");
            int vipCustomerRetrievalRate = parseIntValue(content, "\"vip\".*?\"customerRetrievalRate\":\\s*(\\d+)");
            int vipMaxTicketCapacity = parseIntValue(content, "\"vip\".*?\"maxTicketCapacity\":\\s*(\\d+)");

            normalTicketPool.loadConfiguration(normalTotalTickets, normalTicketReleaseRate,
                    normalCustomerRetrievalRate, normalMaxTicketCapacity);
            vipTicketPool.loadConfiguration(vipTotalTickets, vipTicketReleaseRate,
                    vipCustomerRetrievalRate, vipMaxTicketCapacity);

            System.out.println("Configuration loaded successfully from " + filename);
        } catch (Exception e) {
            System.out.println("Error loading configuration: " + e.getMessage());
        }
    }

    private static int parseIntValue(String content, String regex) {
        Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(content);

        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        throw new IllegalArgumentException("Cannot find value for regex: " + regex);
    }

    private void exitProgram() {
        System.out.println("Exiting Ticket System. Goodbye!");
        if (executor != null) {
            shutdownExecutor();
        }
        scanner.close();
        System.exit(0);
    }

    public static void main(String[] args) {
        TicketSystem ticketSystem = new TicketSystem();
        ticketSystem.run();
    }
}