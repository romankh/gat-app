/**
 * Copyright (C) 2016 Roman Khassraf.
 *
 * This file is part of GAT-App.
 *
 *  GAT-App is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  GAT-App is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GAT-App.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package at.khassraf.gat.app;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.gsm.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;


public class SendMessagelessSMSActivity extends Activity {

    private static Context context;
    private static int intentId = 50000;
    private final static String TAG = "GAT-App";
    private final static String ACTION_SMS_DELIVERED = "SMS_DELIVERED";
    private final static String ACTION_SMS_SENT = "SMS_SENT";
    private final static int PICK_CONTACT = 1;

    private MessageType type;
    private TextView textViewRecipientNr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_messageless_sms);
        context = getApplicationContext();

        Intent intent = getIntent();
        int typeNr = intent.getExtras().getInt("MESSAGETYPE");
        type = MessageType.values()[typeNr];

        String title = "wrong title";
        switch (type) {
            case SILENT_TYPE0:
                title = getString(R.string.title_activity_send_type0);
                break;
            case SILENT_TYPE0_DELIVERY_REPORT:
                title = getString(R.string.title_activity_send_type0Ping);
                break;
            case MWID_DELIVERY_REPORT:
                title = getString(R.string.title_activity_send_MWIPing);
                break;
            case MWIA:
                title = getString(R.string.title_activity_send_MWI_Activate);
                break;
            case MWID:
                title = getString(R.string.title_activity_send_MWI_Dectivate);
                break;
            default:
                break;
        }
        this.setTitle(title);
        textViewRecipientNr = (TextView) findViewById(R.id.editTextPhone);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_send_messageless_sm, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onButtonSendClicked(View view) {

        SmsManager smsManager = SmsManager.getDefault();

        String cmd = ".gat#";
        cmd += String.valueOf(type.getValue());

        String recipientNr = textViewRecipientNr.getText().toString();
        PendingIntent pendingDeliveryIntent = null;

        if (type.hasDeliveryReport()) {
            Intent deliveryIntent = new Intent(ACTION_SMS_DELIVERED);
            deliveryIntent.putExtra("RECIPIENT", recipientNr);
            pendingDeliveryIntent = PendingIntent.getBroadcast(context, intentId++, deliveryIntent, 0);
        }

        Intent sentIntent = new Intent(ACTION_SMS_SENT);
        sentIntent.putExtra("RECIPIENT", recipientNr);
        sentIntent.putExtra("TYPE", type.getValue());
        PendingIntent pendingSentIntent = PendingIntent.getBroadcast(context, intentId++, sentIntent, 0);

        smsManager.sendTextMessage(recipientNr, null, cmd, pendingSentIntent, pendingDeliveryIntent);
        finish();
    }

    public void onButtonContactsClicked(View view) {
        try {
            Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
            startActivityForResult(intent, PICK_CONTACT);
        } catch (Exception e) {
            Log.e(TAG, "Failed to start contact pick: " + e.getMessage());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        switch (requestCode) {
            case (PICK_CONTACT):
                if (resultCode == Activity.RESULT_OK) {
                    Uri contactData = intent.getData();
                    Cursor c = managedQuery(contactData, null, null, null, null);
                    if (c.moveToFirst()) {
                        String id = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
                        String hasPhone = c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

                        if (hasPhone.equalsIgnoreCase("1")) {
                            Cursor phones = getContentResolver().query(
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id,
                                    null, null);
                            phones.moveToFirst();
                            String cNumber = phones.getString(phones.getColumnIndex("data1"));
                            textViewRecipientNr.setText(cNumber);
                        }
                    }
                }
                break;
            default:
                break;
        }
    }
}