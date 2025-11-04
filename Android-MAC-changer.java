import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class AndroidMacChanger {
    private static final Logger logger = Logger.getLogger(AndroidMacChanger.class.getName());
    private static final String LOG_FILE = "mac_change_log.txt";
    private static final Pattern MAC_PATTERN = Pattern.compile("([0-9a-fA-F]{2}[:-]){5}([0-9a-fA-F]{2})");

    /**
     * Получает текущий MAC-адрес указанного интерфейса.
     *
     * @param interfaceName имя сетевого интерфейса (e.g., wlan0, hci0)
     * @return текущий MAC-адрес или null при ошибке
     */
    private static String getCurrentMacAddress(String interfaceName) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new ProcessBuilder("sh", "-c", "cat /sys/class/net/" + interfaceName + "/address").start().getInputStream()))) {
            return reader.readLine().trim();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Ошибка чтения текущего MAC: ", e);
            return null;
        }
    }

    private static boolean isValidMacAddress(String mac) {
        return MAC_PATTERN.matcher(mac).matches();
    }

    private static boolean interfaceExists(String interfaceName) {
        try {
            Process process = new ProcessBuilder("ip", "link", "show", interfaceName).start();
            return process.waitFor() == 0;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Ошибка проверки интерфейса: ", e);
            return false;
        }
    }

    private static String generateRandomMac() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            if (i > 0) sb.append(":");
            sb.append(String.format("%02x", random.nextInt(256)));
        }
        return sb.toString().toUpperCase();
    }

    private static boolean isSelinuxEnforcing() {
        try {
            Process process = new ProcessBuilder("su", "-c", "getenforce").start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            return "Enforcing".equalsIgnoreCase(reader.readLine().trim());
        } catch (Exception e) {
            logger.log(Level.WARNING, "Не удалось проверить SELinux: ", e);
            return false;
        }
    }

    private static void setSelinuxPermissive() {
        try {
            executeCommand("setenforce 0");
            logChange("SELinux переключен в permissive mode (временно).");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Ошибка переключения SELinux: ", e);
        }
    }

    private static void changeMacAddress(String interfaceName, String newMac) throws Exception {
        // Попытка с ip (предпочтительно)
        try {
            executeCommand("ip link set dev " + interfaceName + " down");
            executeCommand("ip link set dev " + interfaceName + " address " + newMac);
            executeCommand("ip link set dev " + interfaceName + " up");
            return;
        } catch (Exception e) {
            logger.log(Level.WARNING, "ip link не сработал, пробуем ifconfig: ", e);
        }
        // Fallback на ifconfig (если BusyBox установлен)
        executeCommand("ifconfig " + interfaceName + " down");
        executeCommand("ifconfig " + interfaceName + " hw ether " + newMac);
        executeCommand("ifconfig " + interfaceName + " up");
    }

    private static String executeCommand(String command) throws Exception {
        ProcessBuilder pb = new ProcessBuilder("su", "-c", command);
        Process process = pb.start();
        BufferedReader outputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = outputReader.readLine()) != null) {
            output.append(line).append("\n");
        }
        while ((line = errorReader.readLine()) != null) {
            logger.log(Level.WARNING, "stderr: " + line);
        }
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Команда провалилась (" + exitCode + "): " + command + "\nOutput: " + output);
        }
        return output.toString().trim();
    }

    private static void checkRootAccess() throws Exception {
        if (!"uid=0".contains(executeCommand("id"))) {
            throw new RuntimeException("Необходимы права суперпользователя (root).");
        }
    }

    private static void logChange(String message) {
        try (FileWriter writer = new FileWriter(LOG_FILE, true)) {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            writer.write(timestamp + " - " + message + "\n");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Ошибка логирования: ", e);
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        try {
            checkRootAccess();
            String interfaceName;
            String newMac;
            if (args.length == 2) {
                interfaceName = args[0];
                newMac = args[1];
            } else {
                System.out.print("Введите имя интерфейса (e.g., wlan0): ");
                interfaceName = scanner.nextLine().trim();
                System.out.print("Введите новый MAC-адрес (или 'random' для генерации): ");
                newMac = scanner.nextLine().trim();
            }
            if ("random".equalsIgnoreCase(newMac)) {
                newMac = generateRandomMac();
                System.out.println("Сгенерированный MAC: " + newMac);
            }
            if (!interfaceExists(interfaceName)) {
                throw new RuntimeException("Интерфейс '" + interfaceName + "' не найден.");
            }
            if (!isValidMacAddress(newMac)) {
                throw new RuntimeException("Некорректный формат MAC: '" + newMac + "'.");
            }
            String currentMac = getCurrentMacAddress(interfaceName);
            if (currentMac == null) {
                throw new RuntimeException("Не удалось прочитать текущий MAC.");
            }
            logChange("Текущий MAC: " + currentMac + " (интерфейс: " + interfaceName + ")");
            if (isSelinuxEnforcing()) {
                System.out.println("SELinux в enforcing mode. Переключаем в permissive? (y/n): ");
                if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
                    setSelinuxPermissive();
                } else {
                    System.out.println("Предупреждение: Изменения могут не сработать без permissive SELinux.");
                }
            }
            changeMacAddress(interfaceName, newMac);
            logChange("MAC изменён на: " + newMac);
            System.out.println("MAC успешно изменён на: " + newMac + ". Перезагрузите для проверки (изменение временное).");
            System.out.print("Восстановить оригинальный MAC? (y/n): ");
            if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
                changeMacAddress(interfaceName, currentMac);
                logChange("Оригинальный MAC восстановлен: " + currentMac);
                System.out.println("Оригинальный MAC восстановлен.");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Критическая ошибка: ", e);
            System.err.println("Ошибка: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }
}
