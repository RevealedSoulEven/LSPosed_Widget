package souleven.lsposed.start;

import android.os.Bundle;
import android.widget.Toast;
import java.io.File;
import java.io.OutputStream;
import android.app.Activity;
import android.os.Handler;
import android.os.Looper;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (!isRooted()) {
            Toast.makeText(this, "Root is required to launch LSPosed!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Toast.makeText(this, "Requesting root...", Toast.LENGTH_SHORT).show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                final boolean success = executeBroadcast();
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
//                        if (success) {
//                            Toast.makeText(MainActivity.this, "LSPosed launch command sent", Toast.LENGTH_SHORT).show();
//                        } else {
                        if (!success) {
                            Toast.makeText(MainActivity.this, "Failed to execute root command", Toast.LENGTH_LONG).show();
                        }
                        finish();
                    }
                });
            }
        }).start();
    }

    private boolean isRooted() {
        String[] paths = {
            "/system/xbin/su",
            "/system/bin/su",
            "/debug_ramdisk/su"
        };

        for (String path : paths) {
            File file = new File(path);
            if (file.exists() && file.canExecute()) {
                return true;
            }
        }
        return false;
    }

    private boolean executeBroadcast() {
        int sdkVersion = android.os.Build.VERSION.SDK_INT;
        try {
            Process suProcess = Runtime.getRuntime().exec("su");
            OutputStream outputStream = suProcess.getOutputStream();

            String command = sdkVersion >= 29 ?
                    "am broadcast -a android.telephony.action.SECRET_CODE -d android_secret_code://5776733" :
                    "am broadcast -a android.provider.Telephony.SECRET_CODE -d android_secret_code://5776733";

            outputStream.write((command + "\n").getBytes());
            outputStream.flush();
            outputStream.close();
            int result = suProcess.waitFor();
            suProcess.destroy();
            return result == 0;
        } catch (Exception e) {
            android.util.Log.e("RootBroadcast", "error: " + e.getMessage());
            return false;
        }
    }
}
