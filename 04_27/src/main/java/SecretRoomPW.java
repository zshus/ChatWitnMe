import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ObjectOutputStream;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class SecretRoomPW extends JFrame {
	private RoomInfo roomInfo;
	private ObjectOutputStream serverOos;
	private String pw;

	private JLabel lblRoomNum;
	private JLabel lblRoomName;
	private JLabel lblCurrentNum;
	private JLabel lblMaxNum;

	private JLabel lblPWInfo;
	private JLabel lblPW;

	private JButton btnInput;
	private JButton btnClose;

	public SecretRoomPW(ObjectOutputStream serverOos, RoomInfo roomInfo, String pw) {
		this.roomInfo = roomInfo;
		this.serverOos = serverOos;
		this.pw = pw;
		init();
		setDisplay();
		addListeners();
		showFrame();
	}

	private void init() {
		lblRoomNum = new JLabel(Integer.toString(roomInfo.getRoomID()));
		lblRoomName = new JLabel(roomInfo.getName());
		String currentNum = null;

		currentNum = String.valueOf(roomInfo.getCurrentNum());

		lblCurrentNum = new JLabel(currentNum);
		lblMaxNum = new JLabel(Integer.toString(roomInfo.getMaxNum()));

		lblPWInfo = new JLabel("비밀번호: ");
		lblPW = new JLabel(pw);

		btnInput = new JButton("입력");
		btnClose = new JButton("닫기");
	}

	private void setDisplay() {
		JPanel pnlNorth = new JPanel();
		pnlNorth.add(new JLabel(new ImageIcon("lock.png")));
		pnlNorth.add(lblRoomNum);
		pnlNorth.add(new JLabel(". "));
		pnlNorth.add(lblRoomName);
		pnlNorth.add(new JLabel("("));
		pnlNorth.add(lblCurrentNum);
		pnlNorth.add(new JLabel("/"));
		pnlNorth.add(lblMaxNum);
		pnlNorth.add(new JLabel(")"));

		JPanel pnlCenter = new JPanel();
		pnlCenter.add(lblPWInfo);
		pnlCenter.add(lblPW);

		JPanel pnlSouth = new JPanel();
		pnlSouth.add(btnInput);
		pnlSouth.add(btnClose);

		JPanel pnlMain = new JPanel(new GridLayout(3, 1));
		pnlMain.add(pnlNorth);
		pnlMain.add(pnlCenter);
		pnlMain.add(pnlSouth);

		add(pnlMain);
	}

	private void addListeners() {
		ActionListener alistener = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == btnInput) {
					dispose();
					new SecretRoomEnt(serverOos, roomInfo);
				} else {
					dispose();
				}
			}
		};
		btnInput.addActionListener(alistener);
		btnClose.addActionListener(alistener);
	}

	private void showFrame() {
		setTitle("비밀번호 안내");
		setSize(250, 150);
		setLocationRelativeTo(null);
		setVisible(true);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}
}
