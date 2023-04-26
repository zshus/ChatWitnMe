import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class RoomSetting extends JDialog {
	private String strLbl;
	private JTextField tfRoomName;
	private JTextField tfMaximum;
	private JTextField tfRoomPw;
	private JCheckBox ckbPrivate;
	private JButton btnDone;
	private JButton btnCancel;

	public RoomSetting(WaittingRoom owner, String Title, boolean modal) {
		super(owner, Title, modal);
		strLbl = Title;
		init();
		setDisplay();
		addListeners();
		setTitle("채팅방 만들기");
		showDlg();
	}

	public RoomSetting(String roomName, String maximum, String roomPw) {
		// (방장)채팅방 설정 변경 시
		strLbl = "방 정보 변경";
		init();
		setDisplay();
		addListeners();
		tfRoomName.setText(roomName);
		tfRoomName.setForeground(Color.BLACK);
		tfMaximum.setText(maximum);
		tfMaximum.setForeground(Color.BLACK);
		btnDone.setText("변경");
		if (roomPw != null) {
			ckbPrivate.setSelected(true);
			tfRoomPw.setText(roomPw);
			tfRoomPw.setForeground(Color.BLACK);
		}
		setTitle("방 정보 변경");
		showDlg();
	}

	private void init() {
		tfRoomName = new JTextField(15);
		tfMaximum = new JTextField(15);
		tfRoomPw = new JTextField(10);
		tfRoomPw.setEnabled(false);

		tfRoomName.setText("방 제목");
		tfRoomName.setForeground(Color.GRAY);
		tfMaximum.setText("방 정원");
		tfMaximum.setForeground(Color.GRAY);
		tfRoomPw.setText("비밀번호");
		tfRoomPw.setForeground(Color.GRAY);

		ckbPrivate = new JCheckBox();

		btnDone = new JButton("생성");
		btnCancel = new JButton("취소");
	}

	private void setDisplay() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(new EmptyBorder(15, 15, 15, 15));

		JPanel pnlTitle = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JPanel pnlInputs = new JPanel(new GridLayout(3, 0));
		JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JPanel pnlPassword = new JPanel();

		pnlTitle.add(new JLabel(strLbl));

		pnlInputs.add(tfRoomName);
		pnlInputs.add(tfMaximum);
		pnlInputs.add(pnlPassword);

		pnlPassword.add(new JLabel(new ImageIcon("lock.png")));
		pnlPassword.add(ckbPrivate);
		pnlPassword.add(tfRoomPw);

		pnlButtons.add(btnDone);
		pnlButtons.add(btnCancel);

		panel.add(pnlTitle, BorderLayout.NORTH);
		panel.add(pnlInputs, BorderLayout.CENTER);
		panel.add(pnlButtons, BorderLayout.SOUTH);

		add(panel);
	}

	private void addListeners() {
		ActionListener aListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				JButton src = (JButton) ae.getSource();
				if (src == btnDone) {
					if (tfRoomName.getText().equals("방 제목")) {
						showMsg("방 제목을 입력하세요.");
					} else {
						if (tfMaximum.getText().equals("방 정원")) {
							showMsg("방 정원을 입력하세요.");
						} else {

							if (tfMaximum.getText().length() > 10 ||Integer.valueOf(tfMaximum.getText()) < 0) {
								showMsg("방 정원은 0~999999999을 넘을 수 없습니다.");
							} else if (Integer.parseInt(tfMaximum.getText()) == 0) {
								showMsg("방 정원은 0명일 수 없습니다.");
							} else {

								if (ckbPrivate.isSelected()) {	
									boolean flag=false;
									if (tfRoomPw.getText().equals("비밀번호")) {
										showMsg("비밀방은 반드시 비밀번호를 입력해야 합니다.");
										flag=true;
										return;
									}
									
									if (tfRoomPw.getText().length() > 10 ||Integer.valueOf(tfRoomPw.getText())  < 0) {
										showMsg("비밀번호는 0~999999999을 넘을 수 없습니다.");
										flag=true;
									}else if (tfRoomPw.getText().equals("0")) {
										showMsg("비밀번호를 0으로 설정할 수 없습니다.");
										tfRoomPw.setText("비밀번호");
										tfRoomPw.setForeground(Color.GRAY);
										flag=true;
									}
									if(flag) {
										return;
									}
								}

								ChattingRoomInfo chattingRoomInfo;
								if (ckbPrivate.isSelected()) {
									chattingRoomInfo = new ChattingRoomInfo(tfRoomName.getText(),
											Integer.parseInt(tfMaximum.getText()),
											Integer.parseInt(tfRoomPw.getText()));
								} else {
									chattingRoomInfo = new ChattingRoomInfo(tfRoomName.getText(),
											Integer.parseInt(tfMaximum.getText()));
								}

								Object[] obj = { chattingRoomInfo };
								try {
									if (strLbl.equals("채팅방 만들기")) {
										Client.getWrite().writeObject(new SendData(Request.CHATROOM_MAKE, obj));
										Client.getWrite().flush();
										Client.getWrite().reset();
										dispose();
									} else {
										Client.getWrite().writeObject(new SendData(Request.ROOM_SETTING_EDIT, obj));
										Client.getWrite().flush();
										Client.getWrite().reset();
										dispose();
									}

								} catch (IOException e) {
									e.printStackTrace();
								}

							}
						}
					}
				}
				if (src == btnCancel) {
					dispose();
				}
			}
		};

		btnDone.addActionListener(aListener);
		btnCancel.addActionListener(aListener);

		FocusListener fListener = new FocusAdapter() {
			private void gained(JTextField src, String str) {
				if (src.getText().equals(str)) {
					src.setText("");
					src.setForeground(Color.BLACK);
				} else {
					src.setText(src.getText());
				}
			}

			private void lost(JTextField src, String str) {
				if (src.getText().equals(str) || src.getText().length() == 0) {
					src.setText(str);
					src.setForeground(Color.GRAY);
				} else {
					src.setText(src.getText());
					src.setForeground(Color.BLACK);
				}
			}

			@Override
			public void focusGained(FocusEvent fe) {
				JTextField src = (JTextField) fe.getSource();
				if (src == tfRoomName) {
					gained(src, "방 제목");
				}
				if (src == tfMaximum) {
					gained(src, "방 정원");
				}
				if (src == tfRoomPw) {
					gained(src, "비밀번호");
				}
			}

			@Override
			public void focusLost(FocusEvent fe) {
				JTextField src = (JTextField) fe.getSource();
				if (src == tfRoomName) {
					lost(src, "방 제목");
				}
				if (src == tfMaximum) {
					try {
						int num = Integer.parseInt(src.getText());
						src.setText(Integer.toString(num));
					} catch (Exception e) {
					}
					lost(src, "방 정원");
				}
				if (src == tfRoomPw) {
					lost(src, "비밀번호");
				}
			}
		};

		tfRoomName.addFocusListener(fListener);
		tfMaximum.addFocusListener(fListener);
		tfRoomPw.addFocusListener(fListener);

		ItemListener iListener = new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent ie) {
				JCheckBox src = (JCheckBox) ie.getSource();
				if (src.isSelected()) {
					tfRoomPw.setText("비밀번호");
					tfRoomPw.setForeground(Color.GRAY);
					tfRoomPw.setEnabled(true);
				} else {
					tfRoomPw.setText("비밀번호");
					tfRoomPw.setEnabled(false);
				}
			}
		};

		ckbPrivate.addItemListener(iListener);

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

		tfRoomPw.addKeyListener(kListener);
		tfMaximum.addKeyListener(kListener);
	}

	private void showDlg() {
		pack();
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setLocation(500, 500);
		setVisible(true);
		ckbPrivate.requestFocus();
	}

	// Request
	private void error_net() {
		JOptionPane.showMessageDialog(this, "서버 연결이 원활하지 않아 요청을 수행할 수 없습니다.");
	}

	private void error_check_person() {
		JOptionPane.showMessageDialog(this, "현재 방의 인원보다 작게 설정할 수 없습니다.");
	}

	// GUI
	private void showMsg(String msg) {
		JOptionPane.showMessageDialog(this, msg);		
	}

//	public static void main(String[] args) {
////		new RoomSetting();
//		new RoomSetting("정보변경 확인용 방제", "9", null);
////		new RoomSetting("정보변경 확인용 방제", "9", "password");
//	}
}
