import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import android.util.Log;
import androidx.core.content.ContextCompat;
import java.lang.reflect.Method;

public class initEvilAndroid {
    
    private static final String TAG = "initEvilAndroid";
    private static final String MODIFY_PHONE_STATE_ERROR = "Permission MODIFY_PHONE_STATE not granted. Can't change IMEI";
    private static final String SERIAL_NUMBER_SUCCESS = "Serial number changed successfully to ";
    private static final String SERIAL_NUMBER_ERROR = "Error changing serial number: ";
    private static final String WIFI_MAC_ERROR = "Failed to change WiFi MAC address: ";
    private static final String WIFI_MAC_SUCCESS = "WiFi MAC address changed successfully to ";

    private Context context;

    public initEvilAndroid(Context context) {
        this.context = context;
    }

    public void h1ddenDeviceIMEI(String d3viceIMEI) {
        if (ContextCompat.checkSelfPermission(
                context, Manifest.permission.MODIFY_PHONE_STATE
        ) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, MODIFY_PHONE_STATE_ERROR);
            return;
        }

        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            Class<?> telephonyManagerClass = Class.forName(telephonyManager.getClass().getName());
            Method setImei = telephonyManagerClass.getMethod("setImei", String.class);
            setImei.invoke(telephonyManager, d3viceIMEI);
            SystemProperties.set("ro.serialno", d3viceIMEI);
            Log.d(TAG, "IMEI changed successfully to " + d3viceIMEI);
        } catch (Exception e) {
            Log.e(TAG, "Error changing IMEI: " + e.getMessage());
        }
    }

    public void h1ddenSerialNumber(String d3viceGSM) {
        try {
            SystemProperties.set("gsm.serial", d3viceGSM);
            Log.d(TAG, SERIAL_NUMBER_SUCCESS + d3viceGSM);
        } catch (Exception e) {
            Log.e(TAG, SERIAL_NUMBER_ERROR + e.getMessage());
        }
    }

    public void h1ddenWiFiMAC(String d3viceMAC) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Log.e(TAG, "Requires Android 6.0 or higher");
            return;
        }

        if (ContextCompat.checkSelfPermission(
                context, Manifest.permission.CHANGE_WIFI_STATE
        ) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Permission CHANGE_WIFI_STATE not granted. Can't change Wi-Fi MAC address");
            return;
        }

        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager == null) {
            Log.e(TAG, "Failed to get WifiManager");
            return;
        }

        try {
            wifiManager.setWifiEnabled(false);
            SystemProperties.set("persist.wifi_mac", d3viceMAC);
            wifiManager.setWifiEnabled(true);
            Log.d(TAG, WIFI_MAC_SUCCESS + d3viceMAC);
        } catch (Exception e) {
            Log.e(TAG, WIFI_MAC_ERROR + e.getMessage());
        }
    }
}
