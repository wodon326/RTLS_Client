import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;

//1초마다 실시간 위치를 서버로 전송하는 쓰레드
public class RTLS extends Thread implements RTLS_Variable {
	private byte[] buf_RTLS = new byte[13];
	private ObjectOutputStream oos;
	private Client client;

	public RTLS(ObjectOutputStream oos, Client client) throws IOException {
		setName("RTLS");
		this.oos = oos;
		this.client = client;
	}

	@Override
	public void run() {
		while (true) {
			// 1초마다 CMD_RTDATA 패킷을 만들어 서버로 전송
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			byte[] data_RTLS = new byte[10];
			byte[] int_byte = new byte[4];
			data_RTLS[0] = client.getID();
			data_RTLS[1] = client.getState();
			int_byte = intToBytes(client.getX());
			System.arraycopy(int_byte, 0, data_RTLS, 2, 4);
			int_byte = intToBytes(client.getY());
			System.arraycopy(int_byte, 0, data_RTLS, 6, 4);
			buf_RTLS = makepacket(CMD_RTDATA, data_RTLS);
			try {
				oos.writeObject(buf_RTLS);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	// 패킷 만드는 함수
	public static byte[] makepacket(byte cmd, byte[] data) {
		byte[] pack = new byte[data.length + 3];
		pack[0] = STX;
		pack[1] = cmd;
		System.arraycopy(data, 0, pack, 2, data.length);
		pack[pack.length - 1] = ETX;
		return pack;
	}

	// int -> byte[] 함수
	public static byte[] intToBytes(final int i) {
		ByteBuffer bytebuffer = ByteBuffer.allocate(4);
		bytebuffer.putInt(i);
		return bytebuffer.array();
	}
}
