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

public class SMSSentReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        final Bundle bundle = intent.getExtras();

        if (action.equals("SMS_SENT") && bundle != null) {
            String number = bundle.getString("RECIPIENT");
            int typeNr = bundle.getInt("TYPE");

            MessageType type = MessageType.values()[typeNr];
            String typeDesc = "Unknown message type";
            switch (type) {
                case SMS:
                    typeDesc = "SMS";
                    break;
                case SMS_DELIVERY_REPORT:
                    typeDesc = "SMS with delivery report";
                    break;
                case CLASS0:
                    typeDesc = "Class 0";
                    break;
                case CLASS0_DELIVERY_REPORT:
                    typeDesc = "Class 0 with delivery report";
                    break;
                case SILENT_TYPE0:
                    typeDesc = "Type 0";
                    break;
                case SILENT_TYPE0_DELIVERY_REPORT:
                    typeDesc = "Type 0 Ping";
                    break;
                case MWIA:
                    typeDesc = "MWI Activate";
                    break;
                case MWID:
                    typeDesc = "MWI Deactivate";
                    break;
                case MWID_DELIVERY_REPORT:
                    typeDesc = "MWI Ping";
                    break;
                default:
                    break;
            }

            if (number != null) {
                ContentValues values = new ContentValues();
                values.put("address", number);
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(context, typeDesc + " sent to " + number,
                                Toast.LENGTH_LONG).show();
                        values.put("body", "GAT: " + typeDesc + " message sent to " + number);
                        context.getContentResolver().insert(Uri.parse("content://sms/sent"), values);
                        break;
                    default:
                        Toast.makeText(context, typeDesc + " not sent to " + number,
                                Toast.LENGTH_LONG).show();
                        values.put("body", "GAT: " + typeDesc + " not sent to " + number);
                        context.getContentResolver().insert(Uri.parse("content://sms/sent"), values);
                        break;
                }
            }
        }
    }
}
