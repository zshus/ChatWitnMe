import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.io.ObjectOutputStream;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class SecretRoomEnt extends JFrame {
	private RoomInfo roomInfo;
	private ObjectOutputStream serverOos;

	private JLabel lblRoomNum;
	private JLabel lblRoomName;
	private JLabel lblCurrentNum;
	private JLabel lblMaxNum;
	private JTextField tfInput;
	private JButton btnEnt;
	private JCheckBox cb;

	private JLabel lblText;
	private JTextArea taMsg;
	private JButton btnReq;

	private JPanel pnlTitle;
	private JPanel pnlMsg;
	private JPanel pnlReq;
	private JPanel pnlMain;
	private JPanel pnlBottom;

	public SecretRoomEnt(ObjectOutputStream serverOos, RoomInfo roomInfo) {
		this.roomInfo = roomInfo;
		this.serverOos = serverOos;
		init();
		setDisplay();
		addListeners();
		showFrame();
	}

	public void init() {
		lblRoomNum = new JLabel(Integer.toString(roomInfo.getRoomID()));
		lblRoomName = new JLabel(roomInfo.getName());
		String currentNum = null;

		lblCurrentNum = new JLabel(String.valueOf(roomInfo.getCurrentNum()));
		lblMaxNum = new JLabel(Integer.toString(roomInfo.getMaxNum()));
		tfInput = new JTextField(20);
		btnEnt = new JButton("입장");
		cb = new JCheckBox("비밀번호를 모르는 경우");

		lblText = new JLabel("방장에게 보낼 메세지를 작성해주세요.");
		taMsg = new JTextArea();
		btnReq = new JButton("요청");
	}

	public void setDisplay() {
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
		pnlCenter.add(tfInput);

		JPanel pnlSouth = new JPanel();
		pnlSouth.add(btnEnt);

		JPanel pnlTop = new JPanel(new BorderLayout());
		pnlTop.add(pnlNorth, BorderLayout.NORTH);
		pnlTop.add(pnlCenter, BorderLayout.CENTER);
		pnlTop.add(pnlSouth, BorderLayout.SOUTH);

		JPanel pnlCb = new JPanel();
		pnlCb.add(cb);

		pnlMain = new JPanel(new BorderLayout());
		pnlMain.add(pnlTop, BorderLayout.NORTH);
		pnlMain.add(pnlCb, BorderLayout.CENTER);

		//////////////////////

		pnlTitle = new JPanel();
		pnlTitle.add(lblText);
		JPanel pnlMsg = new JPanel();
		taMsg.setPreferredSize(new Dimension(200, 50));
		JScrollPane scroll = new JScrollPane();
		scroll.add(taMsg);
		pnlMsg.add(taMsg);
		pnlReq = new JPanel();
		pnlReq.add(btnReq);

		pnlBottom = new JPanel(new BorderLayout());
		pnlBottom.add(pnlTitle, BorderLayout.NORTH);
		pnlBottom.add(pnlMsg, BorderLayout.CENTER);
		pnlBottom.add(pnlReq, BorderLayout.SOUTH);

		add(pnlMain);

	}

	public void addListeners() {
		cb.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					pnlMain.add(pnlBottom, BorderLayout.SOUTH);
					pnlMain.updateUI();
					pack();
				} else {
					pnlMain.add(new JPanel(), BorderLayout.SOUTH);
					pnlMain.updateUI();
					pack();
				}
			}
		});

		ActionListener aListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JButton src = (JButton) e.getSource();
				if (src == btnEnt) {
					if (tfInput == null || tfInput.getText().equals("")) {
						showMsg("비밀번호를 입력하세요.");
					} else {
						// 입장(비밀번호 확인)
						try {
							serverOos.reset();
							serverOos.writeObject(new SendData(Request.PASSWORD_CHATROOM_ENTRY,
									new Object[] { roomInfo.getRoomID(), tfInput.getText() }));
							dispose();
						} catch (IOException e2) {
							showMsg("잠시 후 다시 시도하세요.");
						}
					}
				}
				if (src == btnReq) {
					// 비밀번호 요청
					try {
						serverOos.reset();
						serverOos.writeObject(new SendData(Request.PASSWORD_CHATROOM_REQ,
								new Object[] { taMsg.getText(), roomInfo.getRoomID() }));
						dispose();
					} catch (IOException e2) {
						showMsg("잠시 후 다시 시도하세요.");
					}
				}
			}
		};
		btnEnt.addActionListener(aListener);
		btnReq.addActionListener(aListener);

		KeyListener kListener = new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent ke) {
				char c = ke.getKeyChar();
				// if(ke.getKeyCode() != KeyEvent.VK_BACK_SPACE) 가 올바르게 실행되지 않았음
				// https://m.blog.naver.com/iamfreeman/50125170804
				if (!Character.isDigit(c) && ke.paramString().indexOf("Backspace") == -1) {
					ke.consume();
					showMsg("숫자만 입력 가능합니다.");
				}
			}
		};

		tfInput.addKeyListener(kListener);
	}

	public void showFrame() {
		setTitle("비밀방 입장");
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}

	private void showMsg(String msg) {
		JOptionPane.showMessageDialog(this, msg);
	}
}
