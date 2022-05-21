import java.awt.Color;

import javax.swing.JLabel;

public class Client implements RTLS_Variable {
	private byte client_ID;
	private byte state = (byte) 0x00;
	private int x;
	private int y;
	private JLabel location = new JLabel(" ");

	public Client(byte ID) {
		client_ID = ID;
		location.setText(Integer.toString((int) client_ID));
		location.setSize(100, 20);
	}

	public byte getID() {
		return client_ID;
	}
	public void setID(byte ID) {
		client_ID = ID;
		location.setText(Integer.toString((int) client_ID));
	}
	public byte getState() {
		return state;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public void setLocation(int x, int y) {
		this.x = x;
		this.y = y;
		location.setLocation(x, y);
	}

	public JLabel getClient() {
		return location;
	}

	public void moveClient(int moveX,int moveY) {
		x += moveX;
		y += moveY;
		location.setLocation(x, y);
		state = state_check(x, y);
		if(state == danger) {
			location.setForeground(Color.RED);// state가 danger일때 빨간색으로 교체
		}
		else {
			location.setForeground(Color.BLACK);// 앞에 if문을 모두 지나치면 안전하므로 검은색으로 교체
		}
	}
	// 클라이언트의 상태를 체크하는 함수 (danger room : room 1,room 4, room 6)
	public static byte state_check(int x, int y) {
		if (0 <= x && x <= 100 && 0 <= y && y <= 110)// room 1
		{
			return danger;
		} else if (360 <= x && x <= 500 && 0 <= y && y <= 110)// room 4
		{
			return danger;
		} else if (160 <= x && x <= 310 && 160 <= y && y <= 270)// room 6
		{
			return danger;
		}
		return normal;
	}
}
