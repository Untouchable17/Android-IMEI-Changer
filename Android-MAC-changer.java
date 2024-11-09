import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AndroidMacChanger {
    private static final Logger logger = Logger.getLogger(AndroidMacChanger.class.getName());

    /**
     * Получает текущий MAC-адрес указанного интерфейса на случай ошибок и ретерна.
     *
     * @param interfaceName имя сетевого интерфейса
     * @return текущий MAC-адрес
     * @throws Exception если возникает ошибка при выполнении команды
     */
    private static String getCurrentMacAddress(String interfaceName) throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new ProcessBuilder("sh", "-c", "cat /sys/class/net/" + interfaceName + "/address").start().getInputStream()))) {
            return reader.readLine().trim();
        }
    }

    private static boolean isValidMacAddress(String mac) {
        return mac.matches("([0-9a-fA-F]{2}[:-]){5}([0-9a-fA-F]{2})");
    }

    private static boolean interfaceExists(String interfaceName) throws Exception {
        ProcessBuilder pb = new ProcessBuilder("sh", "-c", "ip link show " + interfaceName);
        Process process = pb.start();
        int exitCode = process.waitFor();
        return exitCode == 0; // Если возвращает True, значит интерфейс существует
    }

    private static void changeMacAddress(String interfaceName, String newMac) throws Exception {
        executeCommand("ifconfig " + interfaceName + " down");
        executeCommand("ifconfig " + interfaceName + " hw ether " + newMac);
        executeCommand("ifconfig " + interfaceName + " up");
    }

    private static void executeCommand(String command) throws Exception {
        ProcessBuilder pb = new ProcessBuilder("su", "-c", command);
        pb.inheritIO();
        Process process = pb.start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Command failed: " + command);
        }
    }

    private static void checkRootAccess() throws Exception {
        ProcessBuilder pb = new ProcessBuilder("su", "-c", "id");
        Process process = pb.start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Необходимы права суперпользователя для выполнения скрипта.");
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try {
            checkRootAccess();
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage());
            return;
        }

        String interfaceName;
        String newMac;

        if (args.length == 2) {
            interfaceName = args[0];
            newMac = args[1];
        } else {
            System.out.print("Введите имя интерфейса: ");
            interfaceName = scanner.nextLine().trim();

            System.out.print("Введите новый MAC-адрес: ");
            newMac = scanner.nextLine().trim();
        }

        try {
            if (!interfaceExists(interfaceName)) {
                logger.log(Level.SEVERE, "Ошибка: Интерфейс '" + interfaceName + "' не найден.");
                return;
            }

            if (!isValidMacAddress(newMac)) {
                logger.log(Level.SEVERE, "Ошибка: Некорректный формат MAC-адреса '" + newMac + "'.");
                return;
            }

            String currentMac = getCurrentMacAddress(interfaceName);
            logger.info("Текущий MAC: " + currentMac);

            changeMacAddress(interfaceName, newMac);
            logger.info("MAC адрес изменен на: " + newMac);

            // Восстановление оригинального MAC адреса после изменения (по желанию)
            System.out.print("Хотите восстановить оригинальный MAC адрес? (y/n): ");
            if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
                changeMacAddress(interfaceName, currentMac);
                logger.info("Оригинальный MAC адрес восстановлен: " + currentMac);
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Ошибка при выполнении: ", e);
        } finally {
            scanner.close();
        }
    }
}
