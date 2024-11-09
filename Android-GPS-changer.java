import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GPSChanger {
    private static final Logger logger = Logger.getLogger(GPSChanger.class.getName());

    private static void checkRootAccess() throws Exception {
        executeCommand("id");
    }

    private static void changeGPS(double latitude, double longitude) throws Exception {
        validateCoordinates(latitude, longitude);

        String command = String.format("am startservice -a com.google.android.gms.location.LocationManagerService " + "--es latitude %f --es longitude %f", latitude, longitude);
        executeCommand(command);

        logger.info(String.format("GPS координаты изменены на: %.6f, %.6f", latitude, longitude));
    }

    private static void validateCoordinates(double latitude, double longitude) {
        if (!isValidCoordinate(latitude, true)) {
            throw new IllegalArgumentException("Некорректная широта: " + latitude + ". Широта должна быть от -90 до 90.");
        }
        if (!isValidCoordinate(longitude, false)) {
            throw new IllegalArgumentException("Некорректная долгота: " + longitude + ". Долгота должна быть от -180 до 180.");
        }
    }

    private static boolean isValidCoordinate(double coordinate, boolean isLatitude) {
        return isLatitude ? (coordinate >= -90 && coordinate <= 90) : (coordinate >= -180 && coordinate <= 180);
    }

    private static String executeCommandAndGetOutput(String command) throws Exception {
        ProcessBuilder pb = new ProcessBuilder("su", "-c", command);
        Process process = pb.start();
        StringBuilder output = new StringBuilder();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }
        
        process.waitFor();
        return output.toString();
    }

    private static void executeCommand(String command) throws Exception {
        ProcessBuilder pb = new ProcessBuilder("su", "-c", command);
        pb.inheritIO();
        Process process = pb.start();
        
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Команда не выполнена: " + command);
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try {
            checkRootAccess();

            System.out.print("Введите широту: ");
            double latitude = Double.parseDouble(scanner.nextLine().trim());

            System.out.print("Введите долготу: ");
            double longitude = Double.parseDouble(scanner.nextLine().trim());

            changeGPS(latitude, longitude);

        } catch (NumberFormatException e) {
            logger.log(Level.SEVERE, "Ошибка: Введены некорректные координаты.", e);
            System.out.println("Пожалуйста, введите числовые значения для координат.");
        } catch (IllegalArgumentException e) {
            logger.log(Level.SEVERE, e.getMessage());
            System.out.println(e.getMessage());
        } catch (RuntimeException e) {
            logger.log(Level.SEVERE, e.getMessage());
            System.out.println("Проблема с выполнением команды. Убедитесь, что у вас есть необходимые права и команда поддерживается.");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Произошла ошибка: ", e);
            System.out.println("Произошла ошибка при изменении GPS координат.");
        } finally {
            scanner.close();
        }
    }
}
