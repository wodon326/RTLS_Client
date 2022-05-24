import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import javax.swing.border.EmptyBorder;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;

public class RTLS_Client extends JFrame implements RTLS_Variable {
	private byte ID;
	private int FLYING_UNIT = 10;// 키보드 한번 클릭할때 움직이는 크기
	private JPanel contentPane;
	private JMenu Menu;
	private Socket socket;
	private OutputStream os;
	private ObjectOutputStream oos;
	private InputStream is = null;
	private ObjectInputStream ois;
	private JTextArea [] textArea;

	public RTLS_Client(Client client) {
		ImageIcon icon;// 배경
		icon = new ImageIcon("RTLS map.png");
		textArea = new JTextArea[4];
		this.addWindowListener(  new WindowAdapter() {
			public void windowClosing(WindowEvent e) {

				System.out.println(ID + "가 나갔습니다.");
				byte[] buf_Exit = new byte[4];
				buf_Exit[0] = STX;
				buf_Exit[1] = CMD_EXIT;
				buf_Exit[2] = ID;
				buf_Exit[3] = ETX;
				try {
					oos.writeObject(buf_Exit);
				} catch (IOException ex) {
					throw new RuntimeException(ex);
				}
				System.exit(0);
			}
		});
		setBounds(100, 100, 500, 350);
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		Menu = new JMenu("Chat Other Client");
		menuBar.add(Menu);
		contentPane = new JPanel() {
			public void paintComponent(Graphics g) {
				Dimension d = getSize();
				g.drawImage(icon.getImage(), 0, 0, d.width, d.height, null);
			}
		};

		for(int i = 1;i<=4;i++) {
			// 새로운 클라이언트와 채팅할 수 있게 Chat Other Client메뉴에 추가
			String Menu_name = "Channel#"+i;
			textArea[i-1] = new JTextArea();
			JMenuItem NewMenuItem = new JMenuItem(Menu_name);

			// 메뉴 클릭했을때 데이터베이스에서 채팅기록을 가져오고 채팅할수있는 Client_Chat을 띄움
			NewMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String[] Channel_num = e.getActionCommand().split("#");
					int to = Integer.parseInt(Channel_num[1]);
					Client_Chat chat = null;
					try {
						chat = new Client_Chat(to, (int) client.getID(),oos,textArea[to-1]);
					} catch (IOException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
					chat.setVisible(true);
				}
			});
			Menu.add(NewMenuItem);
		}
		JMenuItem My_State_and_Location = new JMenuItem("Check My State and Location");
		My_State_and_Location.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String State;
				if(client.getState()==danger)
					State = "Danger";
				else
					State = "Normal";
				String message = "Client#"+client.getID()+"\n"+"X : "+client.getX()+" Y : "+client.getY()+ " State : "+State;
				JOptionPane.showMessageDialog(null, message);
			}
		});
		menuBar.add(My_State_and_Location);

		JMenuItem SOS_Request = new JMenuItem("SOS Request");
		SOS_Request.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				byte[] buf_SOS;
				byte[] data_RTLS = new byte[10];
				byte[] int_byte = new byte[4];
				data_RTLS[0] = client.getID();
				data_RTLS[1] = client.getState();
				int_byte = intToBytes(client.getX());
				System.arraycopy(int_byte, 0, data_RTLS, 2, 4);
				int_byte = intToBytes(client.getY());
				System.arraycopy(int_byte, 0, data_RTLS, 6, 4);
				buf_SOS = makepacket(CMD_SOS, data_RTLS);
				try {
					oos.writeObject(buf_SOS);
				} catch (IOException ex) {
					throw new RuntimeException(ex);
				}
			}
		});
		menuBar.add(SOS_Request);

		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		contentPane.addKeyListener(new MyKeyListener(client));// 키보드 입력 이벤트 추가

		// 초기 위치를 room 7에 배치
		client.setLocation(394, 225);
		contentPane.add(client.getClient());
		contentPane.setFocusable(true);
		contentPane.requestFocus();
	}

	// 키보드의 입력은 인식하여 방향키대로 클라이언트를 움직이며 상태를 체크하는 클래스
	class MyKeyListener extends KeyAdapter {
		private Client client;
		public MyKeyListener(Client client) {
			this.client = client;
		}

		public void keyPressed(KeyEvent e) {
			int keyCode = e.getKeyCode();
			switch (keyCode) {
				case KeyEvent.VK_UP:
					client.moveClient(0,-1*FLYING_UNIT);
					break;
				case KeyEvent.VK_DOWN:
					client.moveClient(0,FLYING_UNIT);
					break;
				case KeyEvent.VK_LEFT:
					client.moveClient(-1*FLYING_UNIT,0);
					break;
				case KeyEvent.VK_RIGHT:
					client.moveClient(FLYING_UNIT,0);
					break;
			}
		}
	}

	public ObjectInputStream getOis() {
		return ois;
	}
	public ObjectOutputStream getOos() {
		return oos;
	}
	public JTextArea[] getTextArea() {
		return textArea;
	}
	public void setClientID(int ID) {
		setTitle("ID : " + ID);
		this.ID = (byte) ID;
	}
	public void ShowSOS(int ID,int State, int X, int Y){
		String State_str;
		if(State==danger)
			State_str = "Danger";
		else
			State_str = "Normal";
		String message = "Client#"+ID+"가 SOS 요청을 하였습니다.\n"+"X : "+X+" Y : "+Y+ " State : "+State_str;
		JOptionPane.showMessageDialog(null, message);
	}
	public void Rescue_Request(int ID,int State, int X, int Y){
		String State_str;
		if(State==danger)
			State_str = "Danger";
		else
			State_str = "Normal";
		String message = "옵저버가 Client#"+ID+"의 구조를 요청하였습니다.\n"+"X : "+X+" Y : "+Y+ " State : "+State_str;
		JOptionPane.showMessageDialog(null, message);
	}
	public void danger_alerts(int X, int Y){
		JLabel alerts = new JLabel("X");
		alerts.setBounds(X, Y, 57, 15);
		alerts.setForeground(Color.RED);

		contentPane.add(alerts);
		repaint();
		String message = "옵저버가 해당 위치를 경고하였습니다.\n"+"X : "+X+" Y : "+Y;
		JOptionPane.showMessageDialog(null, message);
	}
	public void setSocket(Socket socket) throws IOException {
		this.socket = socket;
		os = this.socket.getOutputStream();
		oos = new ObjectOutputStream(os);
		is = this.socket.getInputStream();
		ois = new ObjectInputStream(is);
	}
	public static void main(String[] args) throws UnknownHostException, IOException {
		int result = 0;
		result = JOptionPane.showConfirmDialog(null, "Real Time Location System에 접속하시겠습니까?");
		if(result!=JOptionPane.YES_OPTION) return;
		Socket socket = new Socket("localhost", 3000); // 서버에 접속

		byte client_ID = 0;

		Client client = new Client(client_ID);
		// 로그인 후 배경화면 띄우기
		RTLS_Client frame = new RTLS_Client(client);
		frame.setVisible(true);

		frame.setSocket(socket);

		// 데이터를 받는 쓰레드와 실시간 위치 전송 쓰레드 실행
		Receiver thread_receiver = new Receiver(frame,client,frame.getTextArea());
		thread_receiver.start();
		RTLS thread_rtls = new RTLS(frame.getOos(),client);
		thread_rtls.start();
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