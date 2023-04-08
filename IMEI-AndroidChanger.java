import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;


public class ImeiManager {
    private final List<String> imeis;
    private final List<String> newImeis;

    public ImeiManager(List<String> imeis) {
        this.imeis = new ArrayList<>(imeis);
        this.newImeis = new ArrayList<>();
    }

    public void generateNewImeis() {
        Random random = new Random();
        for (int i = 0; i < imeis.size(); i++) {
            String newImei = String.valueOf(
                100000000000000L + random.nextLong() % 900000000000000L
            );
            while (!isValidImei(newImei) || newImeis.contains(newImei)) {
                newImei = String.valueOf(
                    100000000000000L + random.nextLong() % 900000000000000L
                );
            }
            newImeis.add(newImei);
            System.out.println("[*] New IMEI for slot " + (i + 1) + ": " + newImei);

            if (!writeImeiToDevice(i, newImei)) {
                System.err.println("[*] Failed to write new IMEI for slot " + (i + 1));
            }
        }
    }

    public List<String> getNewImeis() {
        return newImeis;
    }

    private boolean isValidImei(String imei) {
        if (imei.length() != 15 && imei.length() != 16) {
            return false;
        }

        Pattern pattern = Pattern.compile("^[0-9]{15,16}$");
        return pattern.matcher(imei).matches();
    }

    private boolean writeImeiToDevice(int slot, String imei) {
        String[] cmd = { 
            "/system/bin/su", "-c", "service call iphonesubinfo 1 i32 " + slot + " s16 " + imei 
        };
        try {
            Process process = Runtime.getRuntime().exec(cmd);
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
            );
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            process.waitFor();
            return process.exitValue() == 0;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

}

public class Main {

public static void main(String[] args) {
    if (android.os.Process.myUid() != 0) {
        if (runAsRoot() != 0) {
            System.err.println("[*] Failed to get root access");
            return;
        }
    }

    List<String> imeis = readImeisFromDevice();
    ImeiManager imeiManager = new ImeiManager(imeis);

    System.out.println("[*] Current IMEIs:");
    for (String imei : imeis) {
        System.out.println("[*] IMEI: " + imei);
    }

    imeiManager.generateNewImeis();
}