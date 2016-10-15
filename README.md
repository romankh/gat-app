# GAT-App
GAT-App is an Android app for sending several types of SMS:

* Class 0 SMS (aka Flash SMS)
* Type 0 SMS (aka Silent SMS)
* Message Waiting Indicator

This app was inspired by HushSMS, an commercial app that provides the capability of sending the same types of messages (and even some more).
The main reason for re-implementation was the lack of a possibility to control the app remotely, a feature needed for a software related to GSM security, that will be published on github at some point. GAT-App provides this feature by implementing a server mode, where the app listens on a specific network port for a incoming connection. Using a very simple network protocol the app allows sending (and receiving) SMS messages from any network-capable software and device.

On the long term, it is planned to add more features to GAT-App. For example, it is planned to implement the capability of displaying information about the mobile network to which the phone is connected to.

The app is known to run with Android 2.3 to Android 4.3. Later versions are not tested, and incompatibility has to be expected.
Furthermore, the app relies on the Xposed framework, which must be installed before the app can be used. However, this means the phone has to be root, as this is a prerequisite of Xposed.

## Install provided APK

* You should have already installed the Xposed Installer before proceeding.
* Download the latest apk file from releases.
* Install by tapping the downloaded file
* Afterwards you need to activate the app as a Xposed module. You should get a notification about that.
* You may have to restart the device

## Build

As a prerequisite, you will need to extract the internal telephony libraries, which are located in _/system/framework/telephony-common.odex_ on an Android phone with version >= 4.2.
This can be done using the programs smali and dex2jar, there are several tutorials on how to do that on the internet. The result should be a file named telephony-dex2jar.jar

* Clone the project from github
* Open in Android Studio
* Add the telephony-dex2jar.jar file to the app folder
* Build and Run/Debug the project using Android Studio

## Usage

Using the GUI for sending messages is pretty straight forward.

To use the app remotely, you have enter _Server Mode_ and tap _Start_. Additionally you can change the preset port if necessary.

The network protocol accepts two commands

* **quit**: ends the connection
* **sms-send#{0}#{1}#{2}**: send message as specified by the three parameters:
    * **{0}**: type of SMS-message. Mandatory.
        * **0**: Standard SMS
        * **1**: Standard SMS with delivery report
        * **2**: Class 0 SMS
        * **3**: Class 0 SMS with delivery report
        * **4**: Type 0 SMS
        * **5**: Type 0 SMS with delivery report
        * **6**: Message Waiting Indicator Activate SMS
        * **7**: Message Waiting Indicator Deactivate SMS
        * **8**: Message Waiting Indicator Deactivate SMS with delivery report
    * **{1}**: MSISDN / phone number of the recipient. Mandatory.
    * **{2}**: text to send. Optional.

## Contributions

Any contributions are welcome.
If you have ideas or suggestions, feel free to open an issue. If you found a bug, please open an issue that briefly describes the problem in an reproducible way. Pull requests are also welcome.