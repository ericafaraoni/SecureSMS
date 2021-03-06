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
import android.widget.TextView;
import android.widget.Toast;

public class PinActivity extends AppCompatActivity{

    public final static String EXTRA_MESSAGE = "com.ssms.securesms.Action";
    private String action = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new InsertNumberFragment()).commit();
        }

        // Retrieve the operation
        Intent intent = getIntent();
        action = intent.getStringExtra(EXTRA_MESSAGE);

    }

    /*
         Gestione button
    */

    public void buttonAction(View arg0)
    {
        Intent nextActivityIntent = new Intent(this, TelephoneNumberActivity.class);
        TextView pinTextView = (TextView) findViewById(R.id.textViewActivity);
        String pinText = pinTextView.getText().toString();

        switch(arg0.getId())
        {
            case R.id.ButtonKeypadBackActivity:
                if(pinText.length() > 0)
                    pinText = pinText.substring(0, pinText.length() - 1);
                break;

            case R.id.ButtonKeypadOkActivity:
                if(pinText.length() != 4)
                {
                    Toast.makeText(getApplicationContext(), "PIN must have 4 numbers.", Toast.LENGTH_LONG).show();
                    break;
                }
                else
                {
                    if(action.equals("SEND"))
                    {
                        nextActivityIntent.putExtra(EXTRA_MESSAGE,"SEND");
                        nextActivityIntent.putExtra("nonceA",pinText);
                    }
                    else
                    {
                        nextActivityIntent.putExtra(EXTRA_MESSAGE,"RECEIVE");
                        nextActivityIntent.putExtra("nonceB",pinText);
                    }
                }
                startActivity(nextActivityIntent);

                break;

            default:
                if(pinText.length() < 4)
                {
                    Button b = (Button)arg0;
                    pinText += b.getText().toString();
                }
                else
                    Toast.makeText(getApplicationContext(), "Click on OK to continue", Toast.LENGTH_LONG).show();
                break;

        }

        pinTextView.setText(pinText);
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

    /*
         Fragment Creation
    */
    public static class InsertNumberFragment extends Fragment {

        public InsertNumberFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_pin, container,
                    false);
        }
    }

}
