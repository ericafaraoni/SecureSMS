package com.ssms.securesms;

import android.content.Intent;
import android.os.Environment;
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

import java.io.File;

import javax.crypto.SecretKey;

public class ReceiveTextActivity extends AppCompatActivity {

    public final static String EXTRA_MESSAGE = "com.ssms.securesms.MESSAGE";
    private String nonceB, destPhone;
    private SecretKey sharedKey;
    private SymmetricCipher sc;
    private final byte[] IV = { 0x0a, 0x01, 0x02, 0x03, 0x04, 0x0b, 0x0c, 0x0d, 0x0a, 0x01, 0x02, 0x03, 0x04, 0x0b, 0x0c, 0x0d };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_text);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new ReceiveTextFragment()).commit();
        }

        // set onClick listener
        Button[] buttons = new Button[3];
        buttons[0] = (Button) findViewById(R.id.okButtonMsg1ReceiveTextActivity);
        buttons[1] = (Button) findViewById(R.id.okButtonMsg2ReceiveTextActivity);
        buttons[2] = (Button) findViewById(R.id.endButtonReceiveTextActivity);

        // recover my nonce, destPhone and myPhone
        Intent intent = getIntent();
        nonceB = intent.getStringExtra("nonceB");
        destPhone = intent.getStringExtra("destPhone");
        String myPhone = intent.getStringExtra("myPhone");
        // recover the shared key
        File path = Environment.getExternalStorageDirectory();
        String keysPath = path.getAbsolutePath() + "/SSMSkeys/";
        KeyStorage keyS = new KeyStorage(keysPath, myPhone + "_" + destPhone + ".key", "AES");
        try
        {
            sharedKey = keyS.loadSharedKey();
            // generate the symmetric cipher
            sc = new SymmetricCipher(sharedKey, "AES/CBC/PKCS5Padding", IV);
        }
        catch(Exception e)
        {
            // if an exception occours during the protocol execution, the app returns on the main activity
            Toast.makeText(getApplicationContext(), "Sorry, an error occurs!", Toast.LENGTH_LONG).show();
            Intent nextActivityIntent = new Intent(this, MainActivity.class);
            startActivity(nextActivityIntent);
        }
    }

    private int receiveMsg2() throws Exception
    {
        String plainText, cipherText;
        smsHandler hdl = new smsHandler(this, destPhone);

        // retrieve cipherText
        cipherText = hdl.smsReceive();
        if(cipherText.equals("Error1"))
            return -1;
        if(cipherText.equals("Error2"))
            return -2;

        // decrypt
        plainText = sc.decrypt(cipherText);
        // check for nonce equality; if they are different, it returns -3
        if(!plainText.equals(nonceB))
            return -3;
        return 0;
    }

    /*
        Gestione button
    */
    public void buttonAction(View arg0)
    {
        TextView txtView = (TextView) findViewById(R.id.textViewReceiveTextActivity);
        Intent nextActivityIntent = null;
        switch(arg0.getId())
        {
            // receive the last message of the handshake protocol
            case R.id.okButtonMsg1ReceiveTextActivity:
                try
                {
                    int tmp = receiveMsg2();
                    String errorString = "";

                    if(tmp!=0)
                    {
                        switch(tmp)
                        {
                            case -1:
                                errorString = "Error! No SMS found.";
                                break;

                            case -2:
                                errorString = "Error! No SMS from " + destPhone + " found.";
                                break;

                            case -3:
                                errorString = "Error, nonce is not correct!";
                                break;
                        }
                        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_LONG).show();
                        nextActivityIntent = new Intent(this, MainActivity.class);
                        startActivity(nextActivityIntent);
                        return;
                    }
                }
                catch(Exception e)
                {
                    // if an exception occours during the protocol execution, the app returns on the main activity
                    Toast.makeText(getApplicationContext(), "Sorry, an error occurs!", Toast.LENGTH_LONG).show();
                    nextActivityIntent = new Intent(this, MainActivity.class);
                    startActivity(nextActivityIntent);
                    return;
                }
                // the handshake is done
                Toast.makeText(getApplicationContext(), "Handshake successfully concluded!", Toast.LENGTH_SHORT).show();
                // set the text of the TextView and the visibility of the buttons
                txtView.setText("Wait for the SMS and then click on GOT IT! to read the message");
                arg0.setVisibility(View.INVISIBLE);
                findViewById(R.id.okButtonMsg2ReceiveTextActivity).setVisibility(View.VISIBLE);
                break;


            case R.id.okButtonMsg2ReceiveTextActivity:
                // Once the user press on this button, we have only to read the last message received, decrypt and show it
                String cipherText, plainText;
                smsHandler hdl = new smsHandler(this, destPhone);
                // retrieve cipherText
                cipherText = hdl.smsReceive();
                if(cipherText.equals("Error1") || cipherText.equals("Error2"))
                    return;
                // decrypt the ciphertext
                try
                {
                    plainText = sc.decrypt(cipherText);
                }
                catch(Exception e)
                {
                    // if an exception occurs during the protocol execution, the app returns on the main activity
                    Toast.makeText(getApplicationContext(), "Sorry, an error occurs!", Toast.LENGTH_LONG).show();
                    nextActivityIntent = new Intent(this, MainActivity.class);
                    startActivity(nextActivityIntent);
                    return;
                }


                // set the text of the TextView and the visibility of the buttons
                txtView.setText(plainText);
                arg0.setVisibility(View.INVISIBLE);
                findViewById(R.id.endButtonReceiveTextActivity).setVisibility(View.VISIBLE);
                break;


            case R.id.endButtonReceiveTextActivity:
                nextActivityIntent = new Intent(this, MainActivity.class);
                startActivity(nextActivityIntent);
                break;
        }
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
         Fragment Creation
    */
    public static class ReceiveTextFragment extends Fragment {

        public ReceiveTextFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_receive,
                    container, false);
            return rootView;
        }
    }
}
