package com.ssms.securesms;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.telephony.SmsManager;

import java.util.ArrayList;

public class smsHandler {

    private Activity activity;
    private String destPhone;

    public smsHandler(Activity activity, String destPhone){

        this.activity =  activity;
        this.destPhone = destPhone;
    }

    public void smsSend(String smsText){
        SmsManager smanager = SmsManager.getDefault();
        ArrayList<String> parts = smanager.divideMessage(smsText);
        smanager.sendMultipartTextMessage(destPhone, null, parts, null, null);



    }

    // retrieve cipher text SMS: it is the last received message from the sender whose phone number is equal to 'destPhone'
    public String smsReceive() {
        String cipherText;
        Cursor cursor = activity.getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);
        // check if there messages; if no messages are found, it returns -1
        if(cursor==null || cursor.getCount()==0) {
            return "Error1";
        }
        // scroll all messages and find the last sent by 'destPhone'; if no message from destPhone is sent, it returns -2
        for(;;)
        {
            String tmpSender = cursor.getString(cursor.getColumnIndexOrThrow("address"));
            if(tmpSender.equals(destPhone))
            {
                // message found! it retrieves the cipher text and breaks the loop
                cipherText = cursor.getString(cursor.getColumnIndexOrThrow("body"));
                break;
            }
            // try the next message; if no more messages are available, it returns -2
            if(!cursor.moveToNext())
                return "Error2";
        }
        cursor.close();
        return cipherText;
    }


}
