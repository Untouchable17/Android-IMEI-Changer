import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class "Android-IMEI-Changer" {
    private static final Logger logger = Logger.getLogger(ImeiChanger.class.getName());
    private static final String LOG_FILE = "imei_change_log.txt";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try {
            checkRootAccess();

            List<String> imeisToChange = new ArrayList<>();
            boolean continueChanging = true;

            while (continueChanging) {
                System.out.print("Хотите ввести новый IMEI (введите '1') или сгенерировать случайный (введите '2')? ");
                String choice = scanner.nextLine().trim();

                if ("1".equals(choice)) {
                    String newImei = getInputImei(scanner);
                    if (newImei != null) {
                        imeisToChange.add(newImei);
                    }
                } else if ("2".equals(choice)) {
                    for (int i = 0; i < 2; i++) {
                        String randomImei = generateRandomImei();
                        imeisToChange.add(randomImei);
                        System.out.println("Сгенерированный IMEI: " + randomImei);
                    }
                } else {
                    System.err.println("Неверный выбор.");
                    continue; // Повторить ввод
                }

                for (String imei : imeisToChange) {
                    if (changeImei(imei)) {
                        logger.info("IMEI успешно изменен на: " + imei);
                        logChange(imei);
                        System.out.println("IMEI успешно изменен на: " + imei);
                    } else {
                        System.err.println("Не удалось изменить IMEI: " + imei);
                    }
                }

                System.out.print("Хотите изменить еще один IMEI? (да/нет): ");
                String response = scanner.nextLine().trim();
                continueChanging = response.equalsIgnoreCase("да");
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Произошла ошибка: ", e);
        } finally {
            scanner.close();
        }
    }

    private static void checkRootAccess() throws Exception {
        executeCommand("id");
    }

    private static boolean changeImei(String imei) throws Exception {
        String command = String.format("service call iphonesubinfo 1 s16 %s", imei);
        return executeCommand(command);
    }

    private static boolean executeCommand(String command) {
        try {
            ProcessBuilder pb = new ProcessBuilder("su", "-c", command);
            Process process = pb.start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Ошибка при выполнении команды: ", e);
            return false;
        }
    }

    private static boolean isValidImei(String imei) {
        return Pattern.matches("^[0-9]{15}$", imei);
    }

    private static String generateRandomImei() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 14; i++) {
            sb.append(random.nextInt(10));
        }

        sb.append(calculateLuhnDigit(sb.toString()));

        return sb.toString();
    }

    private static char calculateLuhnDigit(String imeiWithoutCheckDigit) {
        int sum = 0;
        boolean alternate = false;

        for (int i = imeiWithoutCheckDigit.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(imeiWithoutCheckDigit.substring(i, i + 1));

            if (alternate) {
                n *= 2;
                if (n > 9) n -= 9;
            }

            sum += n;
            alternate = !alternate;
        }

        int checkDigit = (10 - (sum % 10)) % 10;
        return Character.forDigit(checkDigit, 10);
    }

    private static void logChange(String imei) {
        try (FileWriter writer = new FileWriter(LOG_FILE, true)) {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            writer.write(timestamp + " - IMEI изменен на: " + imei + "\n");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Ошибка при записи в лог-файл: ", e);
        }
    }

    private static String getInputImei(Scanner scanner) {
        System.out.print("Введите новый IMEI: ");
        String newImei = scanner.nextLine().trim();
        
        if (isValidImei(newImei)) {
            return newImei;
        } else {
            System.err.println("Некорректный формат IMEI.");
            return null;
        }
    }
}
