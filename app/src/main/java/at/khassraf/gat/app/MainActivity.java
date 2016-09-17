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
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.CellInfo;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.List;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_manual_main, menu);
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

    public void buttonClass0Clicked(View view) {
        Intent intent = new Intent(this, SendClass0SMSActivity.class);
        startActivity(intent);
    }

    public void buttonType0Clicked(View view) {
        Intent intent = new Intent(this, SendMessagelessSMSActivity.class);
        intent.putExtra("MESSAGETYPE", MessageType.SILENT_TYPE0.getValue());
        startActivity(intent);
    }

    public void buttonType0PingClicked(View view) {
        Intent intent = new Intent(this, SendMessagelessSMSActivity.class);
        intent.putExtra("MESSAGETYPE", MessageType.SILENT_TYPE0_DELIVERY_REPORT.getValue());
        startActivity(intent);
    }

    public void buttonMWIPingClicked(View view) {
        Intent intent = new Intent(this, SendMessagelessSMSActivity.class);
        intent.putExtra("MESSAGETYPE", MessageType.MWID_DELIVERY_REPORT.getValue());
        startActivity(intent);
    }

    public void buttonMWIActivateClicked(View view) {
        Intent intent = new Intent(this, SendMessagelessSMSActivity.class);
        intent.putExtra("MESSAGETYPE", MessageType.MWIA.getValue());
        startActivity(intent);
    }

    public void buttonMWIDeactivateClicked(View view) {
        Intent intent = new Intent(this, SendMessagelessSMSActivity.class);
        intent.putExtra("MESSAGETYPE", MessageType.MWID.getValue());
        startActivity(intent);
    }

    public void buttonServerModeClicked(View view) {
        Intent intent = new Intent(this, ServerModeActivity.class);
        startActivity(intent);
    }

    public void buttonTMEvalClicked(View view) {

        TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (manager == null) {
            Toast.makeText(getApplicationContext(), "TelephonyManager not found",
                    Toast.LENGTH_LONG).show();
            return;
        }

        String text = "";

        text += "IMEI: " + manager.getDeviceId() + "\r\n";
        text += "SIM Serial Number: " + manager.getSimSerialNumber() + "\r\n";
        text += "MNC: " + manager.getNetworkCountryIso() + "\r\n";

//        List<CellInfo> cellInfos = manager.getAllCellInfo();

        // CellLocation cellLocation = manager.getCellLocation();
        // text += "CellLocation: " + cellLocation.toString();
        text += "Network Operator: " + manager.getNetworkOperator() + "\r\n";
        text += "Network Operator Name: " + manager.getNetworkOperatorName() + "\r\n";
        text += "SIM Operator: " + manager.getSimOperator() + "\r\n";
        text += "SIM Operator Name: " + manager.getSimOperatorName() + "\r\n";

        // manager.iccExchangeSimIO(int fileID, int command, int p1, int p2, int p3, String filePath)
        // Returns the response APDU for a command APDU sent through SIM_IO.



        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
    }
}
