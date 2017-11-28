package is.rosserial_android;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;

public class ProxyNode extends AbstractNodeMain {
    static final String TAG = "RosSerialProxy";
    private BluetoothSocket socket;
    private Handler errorHandler;
    ROSSerialBluetooth service;

    public ProxyNode(BluetoothSocket socket, Handler handler) {
        this.socket = socket;
        this.errorHandler = handler;
    }

    @Override
    public GraphName getDefaultNodeName() {
        return null;
    }

    public void onStart(ConnectedNode connectedNode) {
        Log.i(TAG, "Start proxy");
        service = new ROSSerialBluetooth(this.errorHandler, connectedNode, socket);
    }
}
