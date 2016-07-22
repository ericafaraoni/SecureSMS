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

    public final static String EXTRA_MESSAGE = "com.ssms.securesms.MESSAGE";
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
        action = intent.getStringExtra("action");

        // Keyboard buttons
        Button keyboard[] = new Button[12];
        keyboard[0] = (Button) findViewById(R.id.ButtonKeypad1Activity);
        keyboard[1] = (Button) findViewById(R.id.ButtonKeypad2Activity);
        keyboard[2] = (Button) findViewById(R.id.ButtonKeypad3Activity);
        keyboard[3] = (Button) findViewById(R.id.ButtonKeypad4Activity);
        keyboard[4] = (Button) findViewById(R.id.ButtonKeypad5Activity);
        keyboard[5] = (Button) findViewById(R.id.ButtonKeypad6Activity);
        keyboard[6] = (Button) findViewById(R.id.ButtonKeypad7Activity);
        keyboard[7] = (Button) findViewById(R.id.ButtonKeypad8Activity);
        keyboard[8] = (Button) findViewById(R.id.ButtonKeypad9Activity);
        keyboard[9] = (Button) findViewById(R.id.ButtonKeypadBackActivity);
        keyboard[10] = (Button) findViewById(R.id.ButtonKeypad0Activity);
        keyboard[11] = (Button) findViewById(R.id.ButtonKeypadOkActivity);

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
                    Toast.makeText(getApplicationContext(), "Il PIN deve essere di 4 cifre", Toast.LENGTH_LONG).show();
                    break;
                }
                else
                {
                    if(action.equals("SEND"))
                    {
                        nextActivityIntent.putExtra("action","SEND");
                        nextActivityIntent.putExtra("nonceA",pinText);
                    }
                    else
                    {
                        nextActivityIntent.putExtra("action","RECEIVE");
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
                    Toast.makeText(getApplicationContext(), "Premi OK per continuare", Toast.LENGTH_LONG).show();
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
        if (id == R.id.MENU_1) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*
         Creazione fragment
    */
    public static class InsertNumberFragment extends Fragment {

        public InsertNumberFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_pin, container,
                    false);
            return rootView;
        }
    }

}
