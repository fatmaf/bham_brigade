
import com.google.protobuf.InvalidProtocolBufferException;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class ListenerChannel {
	private ZContext _context;
	private ZMQ.Socket socket;

	public ListenerChannel() {
		_context = new ZContext();
		socket = _context.createSocket(SocketType.SUB);
		socket.connect("tcp://localhost:5556");
		String filter = "";
		socket.subscribe(filter.getBytes(ZMQ.CHARSET));
	}

	public static void main(String[] args) {
		final ListenerChannel listenerChannel = new ListenerChannel();
		listenerChannel.run(x -> {
			System.out.println("Received update at simulation time :" + x.getTime());
			final int rows = x.getSize(0);
			final int columns = x.getSize(1);
			double[][] heatmap = new double[rows][columns];
			int index = 0;
			for (int i = 0; i < rows; i++)
				for (int j = 0; j < columns; j++) {
					heatmap[i][j] = x.getMap(index);
					index++;
				}
			System.out.println(heatmap);
		});
	}

	public void run(HeatmapEventListener callback) {
		Thread thread = new Thread(new ListenerThread(this.socket, callback));
		thread.start();
	}

	interface HeatmapEventListener {
		void onUpdate(HeatmapOuterClass.Heatmap heatmap);
	}

	class ListenerThread implements Runnable {
		private final ZMQ.Socket socket;
		private final HeatmapEventListener callback;

		public ListenerThread(ZMQ.Socket socket, HeatmapEventListener callback) {

			this.socket = socket;
			this.callback = callback;
		}

		@Override
		public void run() {
			while (true) {
				try {
					final byte[] recv = this.socket.recv();
					HeatmapOuterClass.Heatmap heatmapOuterClass = HeatmapOuterClass.Heatmap.parseFrom(recv);
					this.callback.onUpdate(heatmapOuterClass);

				} catch (InvalidProtocolBufferException e) {
					e.printStackTrace();
				}
			}
		}
	}
}