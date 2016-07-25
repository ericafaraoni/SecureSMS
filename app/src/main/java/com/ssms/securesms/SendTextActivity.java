package com.ssms.securesms;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.security.PrivateKey;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class SendTextActivity extends AppCompatActivity {

    private String nonceA, destPhone;
    private SymmetricCipher sc;
    private final byte[] IV = { 0x0a, 0x01, 0x02, 0x03, 0x04, 0x0b, 0x0c, 0x0d, 0x0a, 0x01, 0x02, 0x03, 0x04, 0x0b, 0x0c, 0x0d };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_text);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new SendTextFragment()).commit();
        }

        // recover my nonce
        Intent intent = getIntent();
        nonceA = intent.getStringExtra("nonceA");
        destPhone = intent.getStringExtra("destPhone");
    }

    //Send the third message of the session key exchange protocol
    private int sendMsg2() throws Exception
    {
        AsymmetricCipher ac = new AsymmetricCipher("RSA/ECB/PKCS1Padding");
        KeyStorage myAsymStorage;
        SecretKey sharedKey;
        PrivateKey myKey;
        String cipherText;
        smsHandler hdl = new smsHandler(this, destPhone);

        // retrieve my private key
        File path = Environment.getExternalStorageDirectory();
        String keysPath = path.getAbsolutePath() + "/SSMSkeys/";
        myAsymStorage = new KeyStorage("", keysPath, "", "private.key");
        myKey = myAsymStorage.loadPrivateKey();

        // retrieve cipherText
        cipherText = hdl.smsReceive();
        if(cipherText.equals("Error1"))
            return -1;
        if(cipherText.equals("Error2"))
            return -2;

        // decrypt first and split the message;
        // msgFields[0] contains my nonce (A's nonce)
        // msgFields[1] contains B's nonce
        // msgFields[2] contains B's phone number
        // msgFields[3] contains the session key encoded in bytes expressed with their integer representation and separated by white spaces
        String plainText = ac.decrypt(cipherText, myKey);
        String[] msgFields = plainText.split("[\\x7C]"); // 'x7C' ASCII code for vertical bar '|'
        // check for sender equality; if destPhone is not equal to the number contained in the message, it returns -3
        if(!destPhone.equals(msgFields[2]))
            return -3;
        // check for nonce equality; if nonceA is not equal to that contained in the message, it returns -4
        if(!msgFields[0].equals(nonceA))
            return -4;

        //reconstruct the shared key
        String[] bytesString = msgFields[3].split(" ");
        byte[] bytes = new byte[bytesString.length];
        for(int i=0; i<bytes.length; i++)
            bytes[i] = Byte.parseByte(bytesString[i]);
        sharedKey = new SecretKeySpec(bytes, "AES");

        // generate the symmetric cipher
        sc = new SymmetricCipher(sharedKey, "AES/CBC/PKCS5Padding", IV);

        // prepare SMS text
        // prepare text to encrypt (B's nonce)
        plainText = msgFields[1];
        // encrypt plaintext
        cipherText = sc.encrypt(plainText);
        // send message
        hdl.smsSend(cipherText);

        return 0;
    }

    /*
        Gestione button
    */

    public void buttonAction(View arg0)
    {
        TextView txtView = (TextView) findViewById(R.id.textViewSendTextActivity);
        EditText edit = (EditText) findViewById(R.id.editTextSendTextActivity);
        Intent nextActivityIntent;

        switch(arg0.getId())
        {
            case R.id.okButtonSendTextActivity:
                try
                {
                    String errorString = "";
                    int tmp = sendMsg2();

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
                                errorString = "Error on the sender!";
                                break;

                            case -4:
                                errorString = " Error, nonce is not correct!!";
                                break;
                        }
                        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_LONG).show();
                        nextActivityIntent = new Intent(this, MainActivity.class);
                        startActivity(nextActivityIntent);
                        break;
                    }
                }
                catch(Exception e)
                {
                    // if an exception occurs during the protocol execution, the app returns on the main activity
                    Toast.makeText(getApplicationContext(), "Sorry, an error occurs!", Toast.LENGTH_LONG).show();
                    nextActivityIntent = new Intent(this, MainActivity.class);
                    startActivity(nextActivityIntent);
                }
                arg0.setVisibility(View.INVISIBLE);
                txtView.setVisibility(View.INVISIBLE);
                findViewById(R.id.inviaButtonSendTextActivity).setVisibility(View.VISIBLE);
                edit.setVisibility(View.VISIBLE);
                Toast.makeText(getApplicationContext(), "Handshake successfully concluded!", Toast.LENGTH_SHORT).show();
                break;

            case R.id.inviaButtonSendTextActivity:
                try
                {
                    // retrieve the text of the message written
                    String plainText = edit.getText().toString();
                    // encrypt the plain text
                    String cipherText = sc.encrypt(plainText);
                    smsHandler hdl = new smsHandler(this,destPhone);
                    hdl.smsSend(cipherText);
                    Toast.makeText(getApplicationContext(), "SMS sent!", Toast.LENGTH_SHORT).show();
                    nextActivityIntent = new Intent(this, MainActivity.class);

                }
                catch(Exception e)
                {
                    // if an exception occurs during the protocol execution, the app returns on the main activity
                    Toast.makeText(getApplicationContext(), "Sorry, an error occurs!", Toast.LENGTH_LONG).show();
                    nextActivityIntent = new Intent(this, MainActivity.class);
                }
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
        return id == R.id.MENU_1 || super.onOptionsItemSelected(item);
    }

    /*
         Fragment Creation
    */
    public static class SendTextFragment extends Fragment {

        public SendTextFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_send,
                    container, false);
        }
    }

}
