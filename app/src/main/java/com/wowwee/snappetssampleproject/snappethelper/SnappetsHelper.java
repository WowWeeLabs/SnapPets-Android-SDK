package com.wowwee.snappetssampleproject.snappethelper;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.wowwee.bluetoothrobotcontrollib.BluetoothRobotPrivate;
import com.wowwee.bluetoothrobotcontrollib.snappets.SnapPets;
import com.wowwee.bluetoothrobotcontrollib.snappets.SnapPetsFinder;
import com.wowwee.bluetoothrobotcontrollib.snappets.SnapPetsInterfaces;
import com.wowwee.snappetssampleproject.util.ConnectionManager;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;

public class SnappetsHelper {
    public static final boolean IS_ENABLE_PIN_CHECKING = false;

    public static final int REQUEST_CODE_BT_ENABLE = 12311;
    private static SnappetsHelper instance = null;
    public AuthenticateCallback authenticateCallback;
    public boolean isPinCodeChecked = false;
    private Context context;
    private ProgressDialog connectDialog;
    private SnapPetsInterfaces snappetBleHandler = null;
    private ConnectSnappetCallback connectedSnappetCallback;
    private BroadcastReceiver mSnapPetsFinderBroadcastReceiver = null;
    private BluetoothAdapter bluetoothAdapterInstance = null;

    private SnappetsHelper(Context context) {
        this.context = context;

        /**
         * BLE
         */
        // Set Context to SnapPetsFinder
        SnapPetsFinder.getInstance().setApplicationContext(context);
        SnapPetsFinder.getInstance().setScanOptionsFlagMask(SnapPetsFinder.RPFScanOptionMask_FilterByProductId);

        /**
         *
         */
        snappetBleHandler = new SnapPetsInterfaces() {

            @Override
            public void snapPetsDeviceReconnecting(SnapPets snapPetsRobot) {

            }

            @Override
            public void snapPetsDeviceReady(final SnapPets snapPetsRobot) {
//				if (connectDialog == null)
//					return;
                Log.d(SnappetsHelper.class.toString(), "snapPetsDeviceReady");
                ConnectionManager.mRobot = snapPetsRobot;
                ConnectionManager.mName = snapPetsRobot.getName();

                Thread newThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final int sleepTime = 500;
                        final int mainthreadSleepTime = 300;

                        try {
                            Thread.sleep(sleepTime);
                        } catch (InterruptedException e) {

                            e.printStackTrace();
                        }
                        ((Activity) (SnappetsHelper.this.context)).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(mainthreadSleepTime);
                                } catch (InterruptedException e) {

                                    e.printStackTrace();
                                }
                                snapPetsRobot.getBluetoothModuleSoftwareVersion();
                            }
                        });

                        try {
                            Thread.sleep(sleepTime);
                        } catch (InterruptedException e) {

                            e.printStackTrace();
                        }
                        ((Activity) (SnappetsHelper.this.context)).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(mainthreadSleepTime);
                                } catch (InterruptedException e) {

                                    e.printStackTrace();
                                }
                                snapPetsRobot.readModuleInfoUserDeviceName();
                            }
                        });

                        try {
                            Thread.sleep(sleepTime);
                        } catch (InterruptedException e) {

                            e.printStackTrace();
                        }
                        ((Activity) (SnappetsHelper.this.context)).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(mainthreadSleepTime);
                                } catch (InterruptedException e) {

                                    e.printStackTrace();
                                }
                                snapPetsRobot.readActivationStatus();
                            }
                        });

//TODO - AUTHENTICATION VIA PIN - REMOVE THIS SECTION OF CODE
                        if (SnappetsHelper.IS_ENABLE_PIN_CHECKING) {
                        } else {
                            enableSnapPetNotificatios(getConnectedSnappet());
                        }
                        try {
                            Thread.sleep(sleepTime);
                        } catch (InterruptedException e) {

                            e.printStackTrace();
                        }
                        ((Activity) (SnappetsHelper.this.context)).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(mainthreadSleepTime);
                                } catch (InterruptedException e) {

                                    e.printStackTrace();
                                }
                                snapPetsRobot.setPinCodeNotifications(true);
                            }
                        });
//=========================================
                        try {
                            Thread.sleep(sleepTime);
                        } catch (InterruptedException e) {

                            e.printStackTrace();
                        }
                        ((Activity) (SnappetsHelper.this.context)).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(sleepTime);
                                } catch (InterruptedException e) {

                                    e.printStackTrace();
                                }
                                snapPetsRobot.turnPhotoFullNotificationOn(true);
                            }
                        });

                        try {
                            Thread.sleep(sleepTime);
                        } catch (InterruptedException e) {

                            e.printStackTrace();
                        }
                        if (connectedSnappetCallback != null) {
                            ((Activity) SnappetsHelper.this.context).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    connectedSnappetCallback.connected(snapPetsRobot);

                                    if (connectDialog != null) {
                                        connectDialog.dismiss();
                                        connectDialog = null;
                                    }
                                }
                            });
                        }
                    }
                });
                newThread.start();
            }

            @Override
            public void snapPetsDeviceIRRemoteButtonReleased(SnapPets snapPetsRobot, byte actionByte) {
            }

            @Override
            public void snapPetsDeviceIRRemoteButtonPressed(SnapPets snapPetsRobot, byte actionByte) {
            }

            @Override
            public void snapPetsDeviceFailedToConnect(SnapPets snapPetsRobot, String error) {
                if (connectDialog != null) {
                    connectDialog.dismiss();
                    connectDialog = null;
                }
            }

            @Override
            public void snapPetsDeviceDisconnected(final SnapPets snapPetsRobot, boolean cleanly) {
                Log.d(SnappetsHelper.class.toString(), "snapPetsDeviceDisconnected");

                if (connectedSnappetCallback != null) {
                    ((Activity) SnappetsHelper.this.context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            connectedSnappetCallback.disconnected(snapPetsRobot);
                            if (connectDialog != null) {
                                connectDialog.dismiss();
                                connectDialog = null;
                            }
                        }
                    });
                }
            }

            @Override
            public void snapPetsDeviceDidTakenPhoto(int status) {
                Log.d(SnappetsHelper.class.toString(), "snapPetsDeviceDidTakenPhoto: " + status);
            }

            @Override
            public void snapPetsDeviceDidReceivedRawData(SnapPets snapPetsRobot, byte[] data) {
            }

            @Override
            public void snapPetsDeviceDidReceivedEEPROMByte(SnapPets snapPetsRobot, byte address, byte value) {
            }

            @Override
            public void snapPetsDeviceDidReceivedCurrentMotorAngle(SnapPets snapPetsRobot, int angle) {
            }

            @Override
            public void snapPetsDeviceDidReceiveSettings() {
            }

            @Override
            public void snapPetsDeviceDidReceiveSecurity() {
                Log.d("string", "tag");
            }

            @Override
            public void snapPetsDeviceDidReceivePhotoPartialBytes(final byte[] bytes) {
//				Log.d(SnappetsHelper.class.toString(), "snapPetsDeviceDidReceivePhotoPartialBytes");

                if (connectedSnappetCallback != null) {
                    ((Activity) SnappetsHelper.this.context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            connectedSnappetCallback.receivedImagePieceSize(bytes.length);
                        }
                    });
                }
            }

            @Override
            public void snapPetsDeviceDidReceivePhotoLength(final int length) {
                Log.d(SnappetsHelper.class.toString(), "snapPetsDeviceDidReceivePhotoLength");

                if (connectedSnappetCallback != null) {
                    ((Activity) SnappetsHelper.this.context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            connectedSnappetCallback.receiveImageTotalSize(length);
                        }
                    });
                }
            }

            @Override
            public void snapPetsDeviceDidReceivePhoto(final ByteBuffer buffer) {
                Log.d(SnappetsHelper.class.toString(), "snapPetsDeviceDidReceivePhoto");

                if (connectedSnappetCallback != null) {
                    try {
                        //read jpg header
                        InputStream stream = SnappetsHelper.this.context.getAssets().open("jpgheader.dat");
                        int size = stream.available();
                        byte[] bufferHeader = new byte[size];
                        stream.read(bufferHeader);
                        stream.close();

                        //combine with jpg header and buffer
                        ByteBuffer newBuffer = ByteBuffer.allocate(size + buffer.capacity());
                        newBuffer.put(bufferHeader);
//	                    newBuffer.put(buffer);
                        newBuffer.put(buffer.array()); // Updated by David. Using newBuffer.put(buffer)- Cannot copy the full byte array

                        final byte[] imageBuffer = newBuffer.array();
//	                    newBuffer.reset(); // Crashed, Mark not set
                        newBuffer = null;

                        ((Activity) SnappetsHelper.this.context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                connectedSnappetCallback.receivedImageBuffer(imageBuffer);
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void snapPetsDeviceDidReceiveBRModuleParameters() {
            }

            @Override
            public void snapPetsDeviceDidPhotoFullNotification() {
                Log.d(SnappetsHelper.class.toString(), "snapPetsDeviceDidPhotoFullNotification");
            }

            @Override
            public void snapPetsDeviceDidNewPhotoNotification(int id) {
                Log.d(SnappetsHelper.class.toString(), "snapPetsDeviceDidNewPhotoNotification: " + id);
            }

            @Override
            public void snapPetsDeviceDidListPhotos(final int count) {
                Log.d(SnappetsHelper.class.toString(), "Count: " + count);
                if (connectedSnappetCallback != null) {
                    ((Activity) SnappetsHelper.this.context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            connectedSnappetCallback.receivedPhotoCount(count);
                        }
                    });
                }
            }

            @Override
            public void snapPetsDeviceDidDeletedPhoto(final int id) {
                Log.d(SnappetsHelper.class.toString(), "snapPetsDeviceDidDeletedPhoto: " + id);

                if (connectedSnappetCallback != null) {
                    ((Activity) SnappetsHelper.this.context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            connectedSnappetCallback.didDeletePhoto(id);
                        }
                    });
                }
            }

            @Override
            public void snapPetsDeviceDidButtonPressedNotification(int type) {
                Log.d(SnappetsHelper.class.toString(), "snapPetsDeviceDidButtonPressedNotification: " + type);

                if (connectedSnappetCallback != null) {
                    ((Activity) SnappetsHelper.this.context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            connectedSnappetCallback.didPressedButton();
                        }
                    });
                }
            }

            @Override
            public void snapPetsDeviceDidAuthenticatePinCode(final boolean isValid) {
                if (isValid) {
                    ((Activity) SnappetsHelper.this.context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            final int mainthreadSleepTime = 500;
                            SnapPets snappet = getConnectedSnappet();
                            try {
                                Thread.sleep(mainthreadSleepTime);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            if (snappet != null)
                                snappet.turnNewPhotoNotificationOn(true);

                            try {
                                Thread.sleep(mainthreadSleepTime);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if (snappet != null)
                                snappet.turnPhotoBlobNotificationOn(true);

                            if (SnappetsHelper.IS_ENABLE_PIN_CHECKING) {
                                enableSnapPetNotificatios(getConnectedSnappet());
                            } else {
                                enableSnapPetNotificatios(getConnectedSnappet());
                            }

                            if (snappet != null)
                                snappet.turnButtonPressedNotificationOn(true);
                            try {
                                Thread.sleep(mainthreadSleepTime);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if (snappet != null)
                                snappet.turnGetPhotoNotificationOn(true);

                            try {
                                Thread.sleep(mainthreadSleepTime);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if (snappet != null)
                                snappet.turnPhotoFullNotificationOn(true);

                            if (authenticateCallback != null) {
                                ((Activity) SnappetsHelper.this.context).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        authenticateCallback.didAuthenticate(true);
                                    }
                                });
                            }
                        }
                    });
                } else {
                    if (authenticateCallback != null) {
                        ((Activity) SnappetsHelper.this.context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                authenticateCallback.didAuthenticate(false);
                            }
                        });
                    }
                }
            }

            @Override
            public void snapPetsDeviceConnected(SnapPets snapPetsRobot) {
                Log.d(SnappetsHelper.class.toString(), "snapPetsDeviceConnected");

            }

            @Override
            public void snapPetsDeviceCapSensorReleased(SnapPets snapPetsRobot, byte actionByte) {
                Log.d(SnappetsHelper.class.toString(), "snapPetsDeviceCapSensorReleased: " + actionByte);
            }

            @Override
            public void snapPetsDeviceCapSensorPressed(SnapPets snapPetsRobot, byte actionByte) {
                Log.d(SnappetsHelper.class.toString(), "snapPetsDeviceCapSensorPressed: " + actionByte);
            }

            @Override
            public void snapPets(SnapPets snapPetsRobot) {
            }

            @Override
            public void snapPetsDeviceDidReceiveProductActivationStatus(byte status) {
                // TODO Auto-generated method stub
                if (status == BluetoothRobotPrivate.kActivation_FactoryDefault) {
                    Log.i("Snappet", "kActivation_FactoryDefault");
                    HashMap<String, String> hashMap = new HashMap<String, String>();
                    hashMap.put("Color", "" + getConnectedSnappet().getColorID());
                    Thread thread = new Thread() {
                        @Override
                        public void run() {
                            try {
                                sleep(1000);
                                SnapPets robot = getConnectedSnappet();
                                robot.writeActivationStatus((int) BluetoothRobotPrivate.kActivation_ActivationSentToFlurry);
                            } catch (InterruptedException e) {
                            }
                        }
                    };
                    thread.start();
                } else if (status == BluetoothRobotPrivate.kActivation_Activate) {
                    Log.i("Snappet", "kActivation_Activate");
                    HashMap<String, String> hashMap = new HashMap<String, String>();
                    hashMap.put("Color", "" + getConnectedSnappet().getColorID());
                    Thread thread = new Thread() {
                        @Override
                        public void run() {
                            try {
                                sleep(1000);
                                SnapPets robot = getConnectedSnappet();
                                robot.setActivationStatus((int) BluetoothRobotPrivate.kActivation_ActivationSentToFlurry);
                            } catch (InterruptedException e) {
                            }
                        }
                    };
                    thread.start();
                } else if (status == BluetoothRobotPrivate.kActivation_ActivationSentToFlurry) {
                    Log.i("Snappet", "kActivation_ActivationSentToFlurry");
                }
            }

            @Override
            public void snapPetsDeviceDidReceivePhotoInByteArray(byte[] bytes) {
                // TODO Auto-generated method stub
                Log.d(SnappetsHelper.class.toString(), "snapPetsDeviceDidReceivePhotoInByteArray");

                if (connectedSnappetCallback != null) {
                    try {
                        //read jpg header
                        InputStream stream = SnappetsHelper.this.context.getAssets().open("jpgheader.dat");
                        int size = stream.available();
                        byte[] bufferHeader = new byte[size];
                        stream.read(bufferHeader);
                        stream.close();

                        final byte[] byteArray = new byte[size + bytes.length];
                        //combine with jpg header and buffer
                        System.arraycopy(bufferHeader, 0, byteArray, 0, bufferHeader.length);
                        System.arraycopy(bytes, 0, byteArray, bufferHeader.length, bytes.length);

                        ((Activity) SnappetsHelper.this.context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                connectedSnappetCallback.receivedImageBuffer(byteArray);
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
    }

    public static SnappetsHelper createInstance(Context context) {
        if (instance == null) {
            instance = new SnappetsHelper(context);
        }
        return instance;
    }

    public static SnappetsHelper getInstance() {
        return instance;
    }

    public void setContext(Context aContext) {
        this.context = aContext;
    }

    private BluetoothAdapter getBluetoothAdapter() {
        if (bluetoothAdapterInstance == null) {
            bluetoothAdapterInstance = ((BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
        }
        return bluetoothAdapterInstance;
    }

    public boolean isSupported() {
        return getBluetoothAdapter() != null;
    }

    public boolean isEnable() {
        return getBluetoothAdapter().isEnabled();
    }

    public boolean prepare(Fragment callbackFragment) {
        if (!getBluetoothAdapter().isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            callbackFragment.startActivityForResult(enableBtIntent, REQUEST_CODE_BT_ENABLE);
            return false;
        }
        return true;
    }

    public void startSearch(final FoundSnappetCallback callback) {
        if (getBluetoothAdapter() != null) {
            SnapPetsFinder.getInstance().setBluetoothAdapter(getBluetoothAdapter());
            stopSearch();
            mSnapPetsFinderBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    final String action = intent.getAction();
                    if (SnapPetsFinder.SnapPetsFinder_SnapPetsFound.equals(action)) {
                        callback.callback(SnapPetsFinder.getInstance().getDevicesFound());
                    }
                }
            };
            context.registerReceiver(mSnapPetsFinderBroadcastReceiver, SnapPetsFinder.getSnapPetsFinderIntentFilter());
            SnapPetsFinder.getInstance().scanForSnapPetsRobots();
        }
    }

    public void stopSearch() {
        if (mSnapPetsFinderBroadcastReceiver != null) {
            try {
                context.unregisterReceiver(mSnapPetsFinderBroadcastReceiver);
                mSnapPetsFinderBroadcastReceiver = null;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        SnapPetsFinder.getInstance().stopScanForSnapPetsRobots();
        SnapPetsFinder.getInstance().clearFoundSnapPetsRobotList();
    }

    public void connect(SnapPets snapPet, ConnectSnappetCallback callback) {
        if (snapPet != null) {
            connectedSnappetCallback = callback;
            SnappetsHelper.getInstance().stopSearch();
            startConnectThread(snapPet);
        }
    }

    private void startConnectThread(final SnapPets snapPet) {
        Thread newThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                ((Activity) SnappetsHelper.this.context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (snapPet != null) {
                            snapPet.setCallbackInterface(snappetBleHandler);
                            snapPet.connect(context);
                        }

//								public void onCancel(DialogInterface dialog) {
//									for (Object obj : SnapPetsFinder.getInstance().getDevicesConnected()) {
//										BluetoothRobot robot = (BluetoothRobot)obj;
//										robot.disconnect();
//									}
//								}
//							});
                    }
                });

            }

        });
        newThread.start();
    }

    public void disconnect(SnapPets snapPet, ConnectSnappetCallback callback) {
        if (snapPet != null) {
            connectedSnappetCallback = callback;
            snapPet.setCallbackInterface(snappetBleHandler);
            snapPet.disconnect();
        }
    }

    public void takePicture(final SnapPets snapPet, ConnectSnappetCallback callback) {
        if (snapPet != null) {
            connectedSnappetCallback = callback;
            snapPet.setCallbackInterface(snappetBleHandler);
            snapPet.takePhotoAndSendNow((byte) 2, (byte) '0');
        }
    }

    public void listenSnapPetButton(SnapPets snapPet, ConnectSnappetCallback callback) {
        if (snapPet != null) {
            connectedSnappetCallback = callback;
            snapPet.setCallbackInterface(snappetBleHandler);
        }
    }

    public void readPhotoCount(SnapPets snapPet, ConnectSnappetCallback callback) {
        if (snapPet != null) {
            connectedSnappetCallback = callback;
            snapPet.setCallbackInterface(snappetBleHandler);
            snapPet.readPhotoCount();
        }
    }

    public boolean needTutorial(SnapPets snapPet) {
        if (SnappetsHelper.IS_ENABLE_PIN_CHECKING) {
// AUTHENTICATION VIA PIN
            if (snapPet == null)
                return false;
            if (snapPet.getUserDeviceName() == null)
                return false;
            //TODO (David/Katy) determine to start the tutorial
            char[] arr = snapPet.getUserDeviceName().toCharArray();
            boolean isNewDevice = true;
            for (char anArr : arr)
                if (anArr != '\0') {
                    isNewDevice = false;
                }
            return isNewDevice;
        } else {
//NOT AUTHENTICATION VIA PIN
            snapPet.readIsSnapPetsAsciiPinCodeEnabled();

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return !snapPet.isSnapPetsAsciiPinCodeEnabled();
        }
    }

    public void enableSnapPetNotificatios(final SnapPets snapPetsRobot) {
        final int sleepTime = 500;
        final int mainthreadSleepTime = 300;

        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {

            e.printStackTrace();
        }
        ((Activity) (SnappetsHelper.this.context)).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(mainthreadSleepTime);
                } catch (InterruptedException e) {

                    e.printStackTrace();
                }
                if (snapPetsRobot != null)
                    snapPetsRobot.turnNewPhotoNotificationOn(true);
            }
        });

        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {

            e.printStackTrace();
        }
        ((Activity) (SnappetsHelper.this.context)).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(mainthreadSleepTime);
                } catch (InterruptedException e) {

                    e.printStackTrace();
                }
                if (snapPetsRobot != null)
                    snapPetsRobot.turnPhotoBlobNotificationOn(true);
            }
        });
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {

            e.printStackTrace();
        }
        ((Activity) (SnappetsHelper.this.context)).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(mainthreadSleepTime);
                } catch (InterruptedException e) {

                    e.printStackTrace();
                }
                if (snapPetsRobot != null)
                    snapPetsRobot.turnButtonPressedNotificationOn(true);
            }
        });
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {

            e.printStackTrace();
        }
        ((Activity) (SnappetsHelper.this.context)).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(mainthreadSleepTime);
                } catch (InterruptedException e) {

                    e.printStackTrace();
                }
                if (snapPetsRobot != null)
                    snapPetsRobot.turnGetPhotoNotificationOn(true);
            }
        });
    }

    public SnapPets getConnectedSnappet() {
        return SnapPetsFinder.getInstance().firstConnectedSnapPets();
    }

    public boolean isConnectedSnappet() {
        return SnapPetsFinder.getInstance().getDevicesConnected().size() > 0;
    }
}
