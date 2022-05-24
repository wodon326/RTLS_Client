import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

// 서버에서 데이터를 받았을 때 받은 데이터를 처리하는 쓰레드
public class Receiver extends Thread implements RTLS_Variable {
	private Client client;
	private JTextArea [] textArea;
	private ObjectInputStream ois;
	private ObjectOutputStream oos;
	private RTLS_Client frame;

	public Receiver(RTLS_Client frame, Client client,JTextArea [] textArea){
		setName("Receiver");
		this.client = client;
		this.textArea = textArea;
		this.frame = frame;
		this.ois = frame.getOis();
		this.oos = frame.getOos();
	}

	@Override
	public void run() {
		byte[] buf = new byte[512];
		try {
			while (true) {
				buf = (byte[]) ois.readObject(); // 데이터 받기 (데이터를 받을 때까지 대기)

				int id;
				byte state;
				int x;
				int y;
				byte[] byte_recode = new byte[10];
				byte[] byte_int = new byte[4];
				int sender = 0;
				int receiver = 0;
				byte[] msg_byte = null;
				if (buf[0] == STX && buf[buf.length - 1] == ETX) {
					switch (buf[1]) {
						case CMD_MSG: // CMD_MSG일때 데이터를 분석한 후 채팅 gui에 추가
							sender = (int) buf[2];
							receiver = (int) buf[3];
							msg_byte = new byte[buf.length - 5];
							System.arraycopy(buf, 4, msg_byte, 0, buf.length - 5);
							String msg_string = new String(msg_byte);
							String msg = "클라이언트" + sender + " : " + msg_string;

							// 채팅 gui에 추가
							textArea[receiver-1].append(msg + "\n");
							textArea[receiver-1].setCaretPosition(textArea[receiver-1].getText().length());
							break;
						case CMD_LOGIN:
							int ClientID = (int) buf[2];
							frame.setClientID(ClientID);
							client.setID((byte)ClientID);
							break;
						case CMD_SOS:
							System.arraycopy(buf, 2, byte_recode, 0, 10);
							id = (int) byte_recode[0];
							state = byte_recode[1];
							System.arraycopy(byte_recode, 2, byte_int, 0, 4);
							x = ByteBuffer.wrap(byte_int).getInt();
							System.arraycopy(byte_recode, 6, byte_int, 0, 4);
							y = ByteBuffer.wrap(byte_int).getInt();
							frame.ShowSOS(id,state,x,y);
							break;
						case CMD_RESCUE:
							System.arraycopy(buf, 2, byte_recode, 0, 10);
							id = (int) byte_recode[0];
							state = byte_recode[1];
							System.arraycopy(byte_recode, 2, byte_int, 0, 4);
							x = ByteBuffer.wrap(byte_int).getInt();
							System.arraycopy(byte_recode, 6, byte_int, 0, 4);
							y = ByteBuffer.wrap(byte_int).getInt();
							frame.Rescue_Request(id,state,x,y);
							break;
						case CMD_LOCATION_ALERTS:
							System.arraycopy(buf, 2, byte_recode, 0, 8);
							System.arraycopy(byte_recode, 0, byte_int, 0, 4);
							x = ByteBuffer.wrap(byte_int).getInt();
							System.arraycopy(byte_recode, 4, byte_int, 0, 4);
							y = ByteBuffer.wrap(byte_int).getInt();
							frame.danger_alerts(x,y);
							break;
						default:
							break;
					}
				} else {
					continue;
				}
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
