package com.ssms.securesms;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    public final static String EXTRA_MESSAGE = "com.ssms.securesms.MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        // Permission requested at run time necessary from Android 4. bla bla
        final int REQUEST_EXTERNAL_STORAGE = 1;
        String[] PERMISSIONS = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.SEND_SMS,
                Manifest.permission.READ_SMS,
        };
        // Check if we have read or write permission and to send and receive sms
        int writePermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int sendPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS);
        int readsmsPermission = ActivityCompat.checkSelfPermission(this,Manifest.permission.READ_SMS);

        if (writePermission != PackageManager.PERMISSION_GRANTED || readPermission != PackageManager.PERMISSION_GRANTED
                || sendPermission != PackageManager.PERMISSION_GRANTED || readsmsPermission != PackageManager.PERMISSION_GRANTED ) {
            // We don't have permission so prompt the user

            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS,
                    REQUEST_EXTERNAL_STORAGE
            );
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null)
        {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new MainFragment()).commit();

        }

        Button sendSMS = (Button) findViewById(R.id.sendButton);
        Button receiveSMS = (Button) findViewById(R.id.receiveButton);

    }

    /*
         Gestione button
    */
    public void buttonAction(View arg0) {

        Intent i = new Intent(this, PinActivity.class);

        switch(arg0.getId())
        {
            case R.id.sendButton:
                i.putExtra("action","SEND");
                break;

            case R.id.receiveButton:
                i.putExtra("action","RECEIVE");
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
        if (id == R.id.MENU_1) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*
         Creazione fragment
    */
    public static class MainFragment extends Fragment {

        public MainFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }

}
