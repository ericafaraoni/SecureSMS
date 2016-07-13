package com.ssms.securesms;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.view.View.OnClickListener;

public class MainActivity extends AppCompatActivity {

    public final static String EXTRA_MESSAGE = "com.ssms.securesms.MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null)
        {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new MainFragment()).commit();

        }

        Button sendSMS = (Button) findViewById(R.id.sendButton);
        Button receiveSMS = (Button) findViewById(R.id.receiveButton);
        //sendSMS.setOnClickListener(btnClick);
        //receiveSMS.setOnClickListener(btnClick);
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
            /*
                Codice di gestione della voce MENU_1
             */
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
