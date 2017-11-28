package is.rosserial_android;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.cloudspace.rosserial_android.ROSSerial;
import com.cloudspace.rosserial_java.TopicRegistrationListener;

import org.ros.node.ConnectedNode;

import java.io.IOException;

import rosserial_msgs.TopicInfo;

public class ROSSerialBluetooth {
    static final String TAG = "ROSSerialBluetooth";

    private ROSSerial rosserial;
    Thread ioThread;
    BluetoothSocket socket;

    Handler errorHandler;

    private ConnectedNode node;

    public interface onConnectionListener {
        void trigger(boolean connection);
    }

    private ROSSerialBluetooth.onConnectionListener connectionCB;

    public void setOnConnectonListener(ROSSerialBluetooth.onConnectionListener onConnectionListener) {
        this.connectionCB = onConnectionListener;
    }


    public ROSSerialBluetooth(Handler handler, ConnectedNode node, BluetoothSocket socket) throws IllegalStateException {
        errorHandler = handler;
        this.node = node;

        if (!startProxying(socket)) {
            throw new IllegalStateException("Unable to open socket.");
        }
    }

    private boolean startProxying(BluetoothSocket socket) {
        Log.i(TAG, "Opening socket!");

        if (socket != null) {
            this.socket = socket;

            try {
                rosserial = new ROSSerial(errorHandler, node, socket.getInputStream(), socket.getOutputStream());
                ioThread = new Thread(null, rosserial, "ROSSerialProxy");
                ioThread.setContextClassLoader(ROSSerialBluetooth.class.getClassLoader());
                ioThread.start();

                if (connectionCB != null) connectionCB.trigger(true);
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
                e.printStackTrace();
                return false;
            }
            return true;
        } else {
            return false;
        }
    }


    private void stopProxying() {
        try {
            if (socket != null) {
                if (rosserial != null) {
                    rosserial.shutdown();
                    rosserial = null;
                }
                socket.close();
            }
        } catch (IOException e) {
            Log.i(TAG, e.getMessage());
            e.printStackTrace();
        } finally {
            socket = null;
            if (connectionCB != null) connectionCB.trigger(false);
        }
    }

    public void shutdown() {
        stopProxying();
    }

    public TopicInfo[] getSubscriptions() {
        return rosserial.getSubscriptions();
    }

    public TopicInfo[] getPublications() {
        return rosserial.getPublications();
    }

    //Set Callback function for new subscription
    public void setOnSubscriptionCB(TopicRegistrationListener listener) {
        if (rosserial != null) rosserial.setOnNewSubcription(listener);
    }

    //Set Callback for new publication
    public void setOnPublicationCB(TopicRegistrationListener listener) {
        if (rosserial != null) rosserial.setOnNewPublication(listener);
    }


    public static void sendError(Handler errorHandler, int errorCode, String message) {
        if (errorHandler != null) {
            Message error = new Message();
            error.what = errorCode;
            Bundle payload = new Bundle();
            payload.putString("error", message);
            error.setData(payload);
            errorHandler.sendMessage(error);
        }
    }
}
