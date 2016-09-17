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

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

import com.android.internal.telephony.gsm.SmsMessage;

import java.util.StringTokenizer;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;


public class XposedSMSHandler implements IXposedHookLoadPackage {

    private static final String TAG = "GAT-App";
    private static final String PREFIX = ".gat";
    private final boolean logPDU = false;

    @Override
    public void handleLoadPackage(final LoadPackageParam lpparam)
            throws Throwable {

        if (!lpparam.packageName.equals("com.android.phone")) {
            return;
        }
        XposedBridge.log(TAG + ": Inserting hooks...");

        findAndHookMethod("com.android.internal.telephony.gsm.SmsMessage",
                lpparam.classLoader, "getSubmitPdu", String.class,
                String.class, String.class, boolean.class, byte[].class,
                int.class, int.class, int.class, getHook());
        XposedBridge.log(TAG + ": Hooks inserted.");
    }

    /**
     * A Xposed hook that modifies getSubmitPdu
     * of class com.android.internal.telephony.gsm.SmsMessage.
     *
     * @return a {@link XC_MethodHook} that can be registered in Xposed.
     */
    private XC_MethodHook getHook() {
        return new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param)
                    throws Throwable {
                XposedBridge.log(TAG + ": entering beforeHookedMethod for getSubmitPdu.");
                // no message param
                if (param.args[2] == null) {
                    return;
                }
                try {
                    String message = (String) param.args[2];
                    // Handle only messages with the cmd prefix
                    if (message.startsWith(PREFIX)) {
                        // token separator is '#'
                        StringTokenizer tokenizer = new StringTokenizer(message, "#", false);
                        int tokenCount = tokenizer.countTokens();
                        if (tokenCount < 2 || tokenCount > 3) {
                            XposedBridge.log(TAG + ": Invalid command.");
                            return;
                        }
                        // remove prefix token
                        tokenizer.nextToken();

                        String typeS = tokenizer.nextToken();
                        int typeNr = Integer.valueOf(typeS);
                        MessageType type = MessageType.values()[typeNr];

                        String text = "";
                        // Use text only if we are going to send Class 0 SMS
                        if (tokenCount == 3 && type.hasText()) {
                            text = tokenizer.nextToken();
                        }

                        SmsMessage.SubmitPdu rawPdu = SmsMessage.getSubmitPdu(null,
                                (String) param.args[1], text, false);
                        // set new Pdu
                        param.setResult(modifyPdu(rawPdu, type));
                        XposedBridge.log(TAG + ": Returning modified Pdu.");
                    }
                } catch (Exception ex) {
                    XposedBridge.log(TAG + ": Something went wrong: " + ex.getMessage());
                }
            }
        };
    }

    private SmsMessage.SubmitPdu modifyPdu(SmsMessage.SubmitPdu pdu, MessageType type) {
        /**
         * Structure of the SMS-Submit PDU
         * (see GSM 03.40, section 9.2.2.2 for more info)
         *
         * –––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
         * | MTI | RD | VPF | RP | UHDI | SRR | MR | DA    | PID | DCS | VP | UDL | UD     |
         * | 2b  | b  | 2b  | b  |  b   | b   |    |       |     |     |    |     |        |
         * |––––––––––––––––––––––––––––––––––|    |       |     |     |    |     |        |
         * |                  o               | o  | 2-12o | o   | o   | o  | o   | 0-140o |
         * –––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
         *
         * b... Bit
         * o... Octet
         *
         */

        if (logPDU) {
            XposedBridge.log(TAG + " Unmodified PDU: " + pdu.toString());
        }

        /**
         * length of destination address DA is variable, we need to calculate the offset
         * - first octet of DA is the length of the address value.
         *   The length is the number of semi-octets, without fill bits.
         *
         * - second octet of DA is the type-of-address field
         *
         * - further octets are the address value
         *
         * (see GSM 03.40, section 9.1.2.5 for more info)
         */
        int daSize = (int) pdu.encodedMessage[2];
        int offset = (daSize / 2) + (daSize % 2);

        switch (type) {
            case SMS_DELIVERY_REPORT:
                // set SRR bit = 1
                pdu.encodedMessage[0] = (byte) 0x21;
                break;
            case CLASS0:
                // TP-DCS, see GSM 03.38, Chapter 4
                pdu.encodedMessage[offset + 5] = (byte) 0xF0;
                break;
            case CLASS0_DELIVERY_REPORT:
                // set SRR bit = 1
                pdu.encodedMessage[0] = (byte) 0x21;
                // TP-DCS, see GSM 03.38, Chapter 4
                pdu.encodedMessage[offset + 5] = (byte) 0xF0;
                break;
            case SILENT_TYPE0:
                // TP-PID
                pdu.encodedMessage[(offset + 4)] = (byte) 0x40;
                break;
            case SILENT_TYPE0_DELIVERY_REPORT:
                // set SRR bit = 1
                pdu.encodedMessage[0] = (byte) 0x21;
                pdu.encodedMessage[(offset + 4)] = (byte) 0x40;
                break;
            case MWIA:
                // TP-DCS, see GSM 03.38, Chapter 4
                pdu.encodedMessage[offset + 5] = (byte) 0xC8;
                break;
            case MWID:
                // TP-DCS, see GSM 03.38, Chapter 4
                pdu.encodedMessage[offset + 5] = (byte) 0xC0;
                break;
            case MWID_DELIVERY_REPORT:
                // set SRR bit = 1
                pdu.encodedMessage[0] = (byte) 0x21;
                // TP-DCS, see GSM 03.38, Chapter 4
                pdu.encodedMessage[offset + 5] = (byte) 0xC0;
                break;
            default:
                break;
        }

        if (logPDU) {
            XposedBridge.log(TAG + " Modified PDU: " + pdu.toString());
        }
        return pdu;
    }
}