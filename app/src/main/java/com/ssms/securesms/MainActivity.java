package com.ssms.securesms;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

public class MainActivity extends AppCompatActivity {

    public final static String EXTRA_MESSAGE = "com.ssms.securesms.Action";
    private final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
    private String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_SMS
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new MainFragment()).commit();

        }


        // for Android version > 6.0
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            // Check if we have read or write permission and to send and receive sms
            int writePermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int readPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            int sendPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS);
            int readsmsPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS);

            if (writePermission != PackageManager.PERMISSION_GRANTED || readPermission != PackageManager.PERMISSION_GRANTED
                    || sendPermission != PackageManager.PERMISSION_GRANTED || readsmsPermission != PackageManager.PERMISSION_GRANTED) {

                // We don't have permission so prompt the user
                ActivityCompat.requestPermissions(
                        this,
                        PERMISSIONS,
                        REQUEST_ID_MULTIPLE_PERMISSIONS
                );
            }
        }
    }

    /*
         Buttons
    */
    public void buttonAction(View arg0) {

        Intent i = new Intent(this, PinActivity.class);

        switch(arg0.getId())
        {
            case R.id.sendButton:
                i.putExtra(EXTRA_MESSAGE,"SEND");
                break;

            case R.id.receiveButton:
                i.putExtra(EXTRA_MESSAGE,"RECEIVE");
                break;
        }

        startActivity(i);

    }


    /*
         Creazione menu
    */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        return id == R.id.MENU_1 || super.onOptionsItemSelected(item);
    }

// Callback for the result from requesting permissions
  @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
      super.onRequestPermissionsResult(requestCode, permissions, grantResults);
      if (requestCode == REQUEST_ID_MULTIPLE_PERMISSIONS) {
          if (grantResults.length == 4 &&
                  grantResults[0] != PackageManager.PERMISSION_GRANTED ||
                  grantResults[1] != PackageManager.PERMISSION_GRANTED ||
                  grantResults[2] != PackageManager.PERMISSION_GRANTED ||
                  grantResults[3] != PackageManager.PERMISSION_GRANTED){
              // exit from the application
              this.finish();
              System.exit(0);
          }
      }
  }
    /*
         Fragment creation
    */
    public static class MainFragment extends Fragment {

        public MainFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_main, container, false);
        }
    }

}
