import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initEvilAndroid evilAndroid = new initEvilAndroid(this);

        evilAndroid.hiddenDeviceIMEI("666666666666666");

        evilAndroid.hiddenSerialNumber("RAX666");

        evilAndroid.hiddenWiFiMAC("00:14:88:13:37:00");

        Toast.makeText(this, "Device information changed successfully", Toast.LENGTH_SHORT).show();
    }
}
