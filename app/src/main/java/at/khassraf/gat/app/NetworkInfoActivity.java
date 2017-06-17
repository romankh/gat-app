package at.khassraf.gat.app;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.telephony.CellInfo;
import android.telephony.NeighboringCellInfo;
import android.telephony.TelephonyManager;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class NetworkInfoActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_info);

        TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (manager == null) {
            Toast.makeText(getApplicationContext(), "TelephonyManager not found",
                    Toast.LENGTH_LONG).show();
            return;
        }

        ((TextView) findViewById(R.id.textViewPhoneNr)).setText(
                manager.getLine1Number() != null && !manager.getLine1Number().isEmpty() ? manager.getLine1Number() : "Unknown"
        );

        ((TextView) findViewById(R.id.textViewImsi)).setText(
                manager.getSubscriberId() != null && !manager.getSubscriberId().isEmpty() ? manager.getSubscriberId() : "Unknown"
        );

        ((TextView) findViewById(R.id.textViewImei)).setText(
                manager.getDeviceId() != null && !manager.getDeviceId().isEmpty() ? manager.getDeviceId() : "Unknown"
        );

        ((TextView) findViewById(R.id.textViewSimSerial)).setText(
                manager.getSimSerialNumber() != null && !manager.getSimSerialNumber().isEmpty() ? manager.getSimSerialNumber() : "Unknown"
        );

        String mccmnc = manager.getNetworkOperator();
        if (mccmnc != null && mccmnc.length() > 2) {
            mccmnc = mccmnc.substring(0, 3) + " / " + mccmnc.substring(3);
        } else {
            mccmnc = "Unknown";
        }
        ((TextView) findViewById(R.id.textViewMccMnc)).setText(mccmnc);

        ((TextView) findViewById(R.id.textViewNetworkType)).setText(
                getNetworkType(manager.getNetworkType())
        );

        ((TextView) findViewById(R.id.textViewNetworkOperatorName)).setText(
                manager.getNetworkOperatorName() != null && !manager.getNetworkOperatorName().isEmpty() ? manager.getNetworkOperatorName() : "Unknown"
        );

        ((TextView) findViewById(R.id.textViewNetworkCountry)).setText(
                manager.getNetworkCountryIso() != null && !manager.getNetworkCountryIso().isEmpty() ? manager.getNetworkCountryIso().toUpperCase() : "Unknown"
        );

        // SIM
        ((TextView) findViewById(R.id.textViewSimOperatorName)).setText(
                manager.getSimOperatorName() != null && !manager.getSimOperatorName().isEmpty() ? manager.getSimOperatorName() : "Unknown"
        );

        ((TextView) findViewById(R.id.textViewSimCountry)).setText(
                manager.getSimCountryIso() != null && !manager.getSimCountryIso().isEmpty() ? manager.getSimCountryIso().toUpperCase() : "Unknown"
        );

        //starts with api 17
//        if (Build.VERSION.SDK_INT >= 17) {
//
//            List<CellInfo> infos = manager.getAllCellInfo();
//            if (infos != null) {
//                for (CellInfo info : infos) {
//                    text += "info: " + info.toString() + "\r\n";
//                }
//            }
//
//            List<NeighboringCellInfo> neighboringCellInfos = manager.getNeighboringCellInfo();
//            if (neighboringCellInfos != null) {
//                for (NeighboringCellInfo info : neighboringCellInfos) {
//                    text += "info: " + info.getNetworkType()
//                            + " : " + info.getCid()
//                            + " : " + info.getLac()
//                            + " : " + info.getRssi()
//
//
//                            + "\r\n";
//                }
//            }
//        }
    }

    private String getNetworkType(int type) {
        switch (type) {
            case 0:
                return "Unknown";
            case 1:
                return "GPRS";
            case 2:
                return "EDGE";
            case 3:
                return "UMTS";
            case 4:
                return "CDMA";
            case 5:
                return "EVDO_0";
            case 6:
                return "EVDO_A";
            case 7:
                return "1xRTT";
            case 8:
                return "HSDPA";
            case 9:
                return "HSUPA";
            case 10:
                return "HSPA";
            case 11:
                return "IDEN";
            case 12:
                return "EVDO_B";
            case 13:
                return "LTE";
            case 14:
                return "EHRPD";
            case 15:
                return "HSPAP";
            default:
                return "Unknown";
        }
    }

}
