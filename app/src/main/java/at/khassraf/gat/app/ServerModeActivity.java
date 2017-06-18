/**
 * Copyright (C) 2016 Roman Khassraf.
 * <p>
 * This file is part of GAT-App.
 * <p>
 * GAT-App is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * GAT-App is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with GAT-App.  If not, see <http://www.gnu.org/licenses/>.
 */
package at.khassraf.gat.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.gsm.SmsManager;
import android.text.format.Formatter;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class ServerModeActivity extends Activity {

    private enum Status {STOPPED, RUNNING}

    private final Context context = this;
    private final static String ACTION_SMS_DELIVERED = "SMS_DELIVERED";
    private final static String ACTION_SMS_SENT = "SMS_SENT";
    private static int intentId = 7000000;

    private TextView textViewStatus;
    private TextView textViewOutput;
    private EditText editTextPort;
    private Button button;

    private Status status = Status.STOPPED;
    private int port;
    private String ipAddress;
    private ServerSocket serverSocket;
    private Thread serverThread;

    private Handler updateUIHandler;
    private Handler updateUIOutputHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_mode);

        textViewStatus = (TextView) findViewById(R.id.statusTextView);
        textViewOutput = (TextView) findViewById(R.id.outputTextView);
        editTextPort = (EditText) findViewById(R.id.portEditText);
        button = (Button) findViewById(R.id.button);

        textViewOutput.setMovementMethod(new ScrollingMovementMethod());

        textViewStatus.setText(Status.STOPPED.name());
        editTextPort.setText("8008");

        WifiManager wifiMgr = (WifiManager) getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
        int ip = wifiInfo.getIpAddress();
        ipAddress = Formatter.formatIpAddress(ip);

        updateUIHandler = new Handler();
        updateUIOutputHandler = new Handler();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_server_mode, menu);
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

    /**
     * Handles click on the Start / Stop button according to the status.
     *
     * @param view the view.
     */
    public void onButtonClick(View view) {
        if (this.status == Status.STOPPED) {
            startServer();
        } else {
            stopServer();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if (status != Status.STOPPED) {
                stopServer();
            }
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    private void showAlert(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setCancelable(false);
        builder.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void startServer() {
        if (this.editTextPort.getText().toString().equals("")) {
            showAlert("Empty port number", "You must set a valid port number!");
            return;
        }
        int port = 0;
        try {
            port = Integer.valueOf(this.editTextPort.getText().toString());
        } catch (NumberFormatException e) {
            showAlert("Invalid port number", "You must set a valid port number!");
            return;
        }
        if (port <= 0 || port > 65535) {
            showAlert("Invalid port number", "You must set a valid port number!");
            return;
        }
        this.port = port;
        this.serverThread = new Thread(new ServerThread());
        this.serverThread.start();
    }

    private void stopServer() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        serverThread.interrupt();
        status = Status.STOPPED;
        updateUIHandler.post(new updateUIThread(false));
        updateUIOutputHandler.post(new updateUIOutputThread("Server stopped."));
    }

    private class ServerThread implements Runnable {

        List<ReadThread> threads = new ArrayList<ReadThread>();

        public void run() {

            Socket socket = null;
            try {
                serverSocket = new ServerSocket(port);
            } catch (IOException e) {
                e.printStackTrace();
            }

            updateUIHandler.post(new updateUIThread(true));
            status = Status.RUNNING;
            updateUIOutputHandler.post(new updateUIOutputThread("Server started."));

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    socket = serverSocket.accept();
                    String remoteIp = socket.getRemoteSocketAddress().toString();
                    updateUIOutputHandler.post(new updateUIOutputThread("New Connection from " + remoteIp));
                    ReadThread readThread = new ReadThread(socket);
                    Thread t = new Thread(readThread);
                    threads.add(readThread);
                    t.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            for (ReadThread t : threads) {
                t.closeSocket();
            }
        }
    }

    private class ReadThread implements Runnable {

        private Socket clientSocket;
        private BufferedReader bufferedReader;
        private String remoteIp;
        private WriteThread writeThread;
        private BlockingQueue<String> msgQueue;
        private SMSServerSentReceiver serverSentReceiver;
        private SMSServerDeliveryReceiver serverDeliveryReceiver;

        public ReadThread(Socket clientSocket) {
            this.clientSocket = clientSocket;
            this.remoteIp = clientSocket.getRemoteSocketAddress().toString() + ": ";
            try {
                this.bufferedReader = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.msgQueue = new LinkedBlockingQueue<String>();
            this.serverSentReceiver = new SMSServerSentReceiver();
            this.serverDeliveryReceiver = new SMSServerDeliveryReceiver();
        }

        public void run() {
            writeThread = new WriteThread(this.clientSocket, this.msgQueue);
            Thread t = new Thread(writeThread);
            t.start();

            registerReceiver(this.serverSentReceiver, new IntentFilter(ACTION_SMS_SENT));
            registerReceiver(this.serverDeliveryReceiver, new IntentFilter(ACTION_SMS_DELIVERED));

            String read = null;
            while (!Thread.currentThread().isInterrupted() && clientSocket.isConnected()) {
                try {
                    if ((read = bufferedReader.readLine()) != null) {

                        if (read.startsWith("sms-send")) {
                            StringTokenizer tokenizer = new StringTokenizer(read, "#", false);
                            int tokenCount = tokenizer.countTokens();
                            if (tokenCount < 3) {
                                updateUIOutputHandler.post(new updateUIOutputThread(remoteIp + "Invalid command."));
                            } else {
                                // remove prefix
                                tokenizer.nextToken();

                                String typeS = tokenizer.nextToken();
                                MessageType type = null;

                                try {
                                    int typeNr = Integer.valueOf(typeS);
                                    type = MessageType.values()[typeNr];
                                } catch (NumberFormatException ex) {
                                    updateUIOutputHandler.post(new updateUIOutputThread(remoteIp + "Invalid command."));
                                }

                                String phoneNr = tokenizer.nextToken();

                                if (type != null) {
                                    SmsManager smsManager = SmsManager.getDefault();
                                    String cmd = ".gat#" + String.valueOf(type.getValue()) + "#";

                                    if (tokenCount == 4 && type.hasText()) {
                                        String text = tokenizer.nextToken();
                                        cmd += text;
                                    }

                                    PendingIntent pendingDeliveryIntent = null;

                                    if (type.hasDeliveryReport()) {
                                        Intent deliveryIntent = new Intent(ACTION_SMS_DELIVERED);
                                        deliveryIntent.putExtra("RECIPIENT", phoneNr);
                                        pendingDeliveryIntent = PendingIntent.getBroadcast(context, intentId++, deliveryIntent, 0);
                                    }

                                    Intent sentIntent = new Intent(ACTION_SMS_SENT);
                                    sentIntent.putExtra("RECIPIENT", phoneNr);
                                    sentIntent.putExtra("TYPE", type.getValue());

                                    PendingIntent pendingSentIntent = PendingIntent.getBroadcast(context, intentId++, sentIntent, 0);

                                    updateUIOutputHandler.post(new updateUIOutputThread(remoteIp + "Sending..."));
                                    smsManager.sendTextMessage(phoneNr, null, cmd, pendingSentIntent, pendingDeliveryIntent);
                                }
                            }
                        } else if (read.equals("quit")) {
                            updateUIOutputHandler.post(new updateUIOutputThread(remoteIp + "quit"));
                            closeSocket();
                            return;
                        } else {
                            updateUIOutputHandler.post(new updateUIOutputThread(remoteIp + "Invalid command."));
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            unregisterReceiver(this.serverSentReceiver);
            unregisterReceiver(this.serverDeliveryReceiver);
            closeSocket();
        }

        public void closeSocket() {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private class SMSServerSentReceiver extends BroadcastReceiver {

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
                            typeDesc = "Default SMS";
                            break;
                        case SMS_DELIVERY_REPORT:
                            typeDesc = "Default SMS with delivery report";
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
                                try {
                                    msgQueue.put("sms-send#" + number + "#OK#");
                                    updateUIOutputHandler.post(
                                            new updateUIOutputThread(
                                                    remoteIp + typeDesc + " sent to " + number
                                            )
                                    );
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                break;
                            default:
                                try {
                                    msgQueue.put("sms-send#" + number + "#NOK#");
                                    updateUIOutputHandler.post(
                                            new updateUIOutputThread(
                                                    remoteIp + typeDesc + " not sent to " + number
                                            )
                                    );
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                break;
                        }
                    }
                }
            }
        }

        private class SMSServerDeliveryReceiver extends BroadcastReceiver {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                final Bundle bundle = intent.getExtras();

                if (action.equals("SMS_DELIVERED") && bundle != null) {
                    String number = bundle.getString("RECIPIENT");
                    if (number != null) {
                        ContentValues values = new ContentValues();
                        values.put("address", number);
                        switch (getResultCode()) {
                            case Activity.RESULT_OK:
                                try {
                                    msgQueue.put("sms-delivery#" + number + "#OK#");
                                    updateUIOutputHandler.post(
                                            new updateUIOutputThread(
                                                    remoteIp + "Message delivered to " + number
                                            )
                                    );
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                break;

                            case Activity.RESULT_CANCELED:
                                try {
                                    msgQueue.put("sms-delivery#" + number + "#NOK#");
                                    updateUIOutputHandler.post(
                                            new updateUIOutputThread(
                                                    remoteIp + "Message not delivered to " + number
                                            )
                                    );
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                break;

                            default:
                                try {
                                    msgQueue.put("sms-delivery#" + number + "#NOK#");
                                    updateUIOutputHandler.post(
                                            new updateUIOutputThread(
                                                    remoteIp + "Message with unclear status to " + number
                                            )
                                    );
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                break;
                        }
                    }
                }
            }
        }
    }

    private class WriteThread implements Runnable {

        private Socket clientSocket;
        private PrintWriter printWriter;
        BlockingQueue<String> messages;

        public WriteThread(Socket clientSocket, BlockingQueue<String> messages) {

            this.clientSocket = clientSocket;
            try {
                OutputStream ostream = this.clientSocket.getOutputStream();
                this.printWriter = new PrintWriter(ostream, true);
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.messages = messages;
        }

        public void run() {
            while (!Thread.currentThread().isInterrupted() && clientSocket.isConnected()) {

                String output = null;

                try {
                    output = messages.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (output != null) {
                    printWriter.println(output);
                    printWriter.flush();
                }
            }
        }
    }

    private class updateUIThread implements Runnable {
        private boolean running;

        public updateUIThread(boolean running) {
            this.running = running;
        }

        @Override
        public void run() {
            if (running) {
                textViewStatus.setText(
                        String.format(
                                "%s (%s:%s)",
                                Status.RUNNING.toString(),
                                ipAddress, String.valueOf(port)));
                editTextPort.setEnabled(false);
                button.setText("Stop");
            } else {
                textViewStatus.setText(Status.STOPPED.toString());
                editTextPort.setEnabled(true);
                button.setText("Start");
            }
        }
    }

    private class updateUIOutputThread implements Runnable {
        private String msg;

        public updateUIOutputThread(String message) {
            this.msg = message;
        }

        @Override
        public void run() {
            textViewOutput.append("\n" + this.msg);
        }
    }
}
