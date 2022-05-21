import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectOutputStream;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class Client_Chat extends JFrame implements RTLS_Variable {
	private JTextField textField;
	private JTextArea textArea;

	public Client_Chat(int to, int ID, ObjectOutputStream oos,JTextArea TextArea) throws IOException {
		setTitle("Channel#" + to + " ID : " + ID);
		textArea = TextArea;
		setBounds(100, 100, 300, 500);
		getContentPane().setLayout(null);
		textArea.setEditable(false); // 채팅 기록이 쌓이는 곳이므로 입력 못하게 막아둔 코드
		textField = new JTextField();
		textField.setBounds(12, 430, 179, 21);
		getContentPane().add(textField);
		textField.setColumns(10);
		textArea.setBounds(12, 10, 260, 410);
		getContentPane().add(textArea);
		JButton Send_Button = new JButton("Send");
		// Send버튼을 눌렀을 때 CMD_MSG패킷을 만들어 서버로 전송
		Send_Button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// textField에서 String을 가져오고 textField을 비움
				String outputMessage = textField.getText();
				textField.setText("");
				// textField에서 가져온 String을 textArea에 추가
				textArea.append("클라이언트" + (int) ID + " : " + outputMessage + "\n");
				textArea.setCaretPosition(textArea.getText().length());
				// textField에서 가져온 String을 CMD_MSG패킷으로 만들어 서버로 전송
				byte[] msg = outputMessage.getBytes();
				byte[] data = new byte[msg.length + 3];
				data[0] = (byte) ID;
				data[1] = (byte) to;
				System.arraycopy(msg, 0, data, 2, msg.length);
				byte [] buf = makepacket(RTLS_Client.CMD_MSG, data);
				try {
					oos.writeObject(buf);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		Send_Button.setBounds(198, 429, 74, 23);
		getContentPane().add(Send_Button);
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
}
