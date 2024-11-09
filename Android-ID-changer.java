import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;


public class AndroidIDChanger {
    private static final Logger logger = Logger.getLogger(AndroidIDChanger.class.getName());

    private static void checkRootAccess() throws Exception {
        executeCommand("id");
    }

    private static void checkCommandSupport() throws Exception {
        executeCommand("settings put secure android_id 1234567890abcdef");
    }

    private static String getCurrentAndroidID() throws Exception {
        return executeCommandAndGetOutput("settings get secure android_id").trim();
    }

    private static boolean isValidAndroidID(String androidID) {
        return androidID.matches("[0-9a-fA-F]{16}");
    }

    private static void changeAndroidID(String newAndroidID) throws Exception {
        executeCommand("settings put secure android_id " + newAndroidID);
    }

    private static String executeCommandAndGetOutput(String command) throws Exception {
        ProcessBuilder pb = new ProcessBuilder("su", "-c", command);
        Process process = pb.start();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            process.waitFor();
            return output.toString();
        }
    }

    private static void executeCommand(String command) throws Exception {
        ProcessBuilder pb = new ProcessBuilder("su", "-c", command);
        pb.inheritIO();
        Process process = pb.start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Ошибка при выполнении команды: " + command);
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try {
            checkRootAccess();
            checkCommandSupport();
        } catch (RuntimeException e) {
            logger.log(Level.SEVERE, e.getMessage());
            return;
        } catch (Exception e) {
            logger.log(
            Level.SEVERE, "Произошла ошибка при проверке доступа: ", e);
            return;
        }

        String newAndroidID;

        if (args.length == 1) {
            newAndroidID = args[0];
        } else {
            System.out.print("Введите новый Android ID (16 шестнадцатеричных символов): ");
            newAndroidID = scanner.nextLine().trim();
        }

        while (!isValidAndroidID(newAndroidID)) {
            logger.log(Level.SEVERE, "Ошибка: Некорректный формат Android ID '" + newAndroidID + "'.");
            System.out.println("Убедитесь, что ваш Android ID состоит из 16 шестнадцатеричных символов.");
            System.out.print("Попробуйте снова: ");
            newAndroidID = scanner.nextLine().trim();
        }

        try {
            String currentAndroidID = getCurrentAndroidID();
            logger.info("Текущий Android ID: " + currentAndroidID);

            changeAndroidID(newAndroidID);
            logger.info("Android ID изменен на: " + newAndroidID);

        } catch (RuntimeException e) {
            logger.log(Level.SEVERE, e.getMessage());
            System.out.println("Проблема с выполнением команды. Убедитесь, что у вас есть необходимые права и команда поддерживается.");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Произошла ошибка: ", e);
            System.out.println("Произошла ошибка при изменении Android ID.");
        } finally {
            scanner.close(); // освобождаем ресурсы
        }
    }
}
