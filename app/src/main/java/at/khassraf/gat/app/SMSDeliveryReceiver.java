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
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

public class SMSDeliveryReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        final Bundle bundle = intent.getExtras();

        if (action.equals("SMS_DELIVERED") && bundle != null) {
            String recipientNumber = bundle.getString("RECIPIENT");
            if (recipientNumber != null) {
                ContentValues values = new ContentValues();
                values.put("address", recipientNumber);
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(context, "Message delivered to " + recipientNumber,
                                Toast.LENGTH_LONG).show();
                        values.put("body", "GAT: message to " + recipientNumber + " delivered");
                        context.getContentResolver().insert(Uri.parse("content://sms/inbox"), values);
                        break;

                    case Activity.RESULT_CANCELED:
                        Toast.makeText(context, "Message not delivered to " + recipientNumber,
                                Toast.LENGTH_LONG).show();
                        values.put("body", "GAT: message to " + recipientNumber + " not delivered");
                        context.getContentResolver().insert(Uri.parse("content://sms/inbox"), values);
                        break;

                    default:
                        Toast.makeText(context, "Message with unclear status to " + recipientNumber,
                                Toast.LENGTH_LONG).show();
                        values.put("body", "GAT: message status to " + recipientNumber +
                                " unclear. This shouldn't happen.");
                        context.getContentResolver().insert(Uri.parse("content://sms/inbox"), values);
                        break;
                }
            }
        }
    }
}