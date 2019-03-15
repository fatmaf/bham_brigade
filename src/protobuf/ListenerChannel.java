package protobuf;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import zmq.*;

public class ListenerChannel {
    public ListenerChannel() {
        ZContext context = new ZContext();
        ZMQ.Socket subscriber = context.createSocket(SocketType.SUB);
        subscriber.connect("tcp://localhost:5563");
    }

    public static void main(String[] args) {
        new ListenerChannel();
    }
}