import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.Vector;

import javax.management.openmbean.KeyAlreadyExistsException;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

public class RoomUI extends JPanel implements ActionListener {
	private JLabel lblRoomKind;
	private JLabel lblPersonKind;
	private JPanel pnlTitle;
	private JTextArea taChat;
	private JList personList;

	private JButton btnWhisper;
	private static JComboBox cbChoice;
	private JTextField tfMsg;
	private JButton btnSend;
	private static Vector<String> vecCb = new Vector<String>();
	private static int selectedNum;
	private String selected;

	// 채팅방 추가
	public static JComboBox cbHost;
	private Vector<String> vecHostMenu = new Vector<String>();
	private Vector<String> hostMenu = new Vector<String>();
	private JButton btnExit;
	private JButton btnInvite;
	private Vector<String> names;
	private String s;
	private boolean whisperFlag;
	private String whisperName;

//	public static String host;

	public final static int WAITINGROOM = 1;
	public final static int CHATINGROOM = 2;
	public final static Dimension BTNSIZE = new Dimension(80, 30);
	public final static Dimension CBSIZE = new Dimension(150, 30);
	public final static Font FONT = new Font("맑은 고딕", Font.BOLD, 14);
	private ChattingRoom room;
	private int roomType;

	public RoomUI(int roomType, Vector<String> names, String s) {
		this.s = s;
		this.names = names;
		this.roomType = roomType;
		pnlTitle = new JPanel(new FlowLayout(FlowLayout.LEFT));
		if (roomType == WAITINGROOM) {

			lblRoomKind = new JLabel("대기실", JLabel.LEFT);
			pnlTitle.add(lblRoomKind);
			lblPersonKind = new JLabel("대기실 인원");
		} else {
			room = (ChattingRoom) Client.roomInfo;
			String sName = room.getRoomName();
			JLabel lblName = new JLabel(sName);
			lblName.setToolTipText(sName);
			lblName.setPreferredSize(new Dimension(50, 50));
			String ss = "  (" + room.getPersonList().size() + "/" + room.getPersonNum_MAX() + ")";
			lblRoomKind = new JLabel(ss, JLabel.LEFT);
			pnlTitle.add(lblName);
			pnlTitle.add(lblRoomKind);
			lblPersonKind = new JLabel("채팅방 인원");
		}

		init();
		setDisplay(roomType);
		addListeners();
	}

	public void init() {
//		host = names.get(0);
		taChat = new JTextArea();
		taChat.setEditable(false);
		taChat.setFont(FONT);		
		if (roomType == WAITINGROOM) {
			personList = new JList(names);
		} else {
			Vector<String> vec = new Vector<String>();
			for (int i = 0; i < names.size(); i++) {
				if (i == 0 && SearchPnl.searchFlag) {
					String ss = names.get(0) + "(방장)";
					vec.add(ss);
				} else {
					vec.add(names.get(i));
				}
			}
			personList = new JList(vec);
		}

		personList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		personList.setFont(FONT);
		btnWhisper = new JButton("귓속말");
		btnWhisper.setPreferredSize(BTNSIZE);
		btnWhisper.setFont(FONT);

		for (int i = vecCb.size() - 1; i > 0; i--) {
			String vecCb_str = vecCb.get(i);
			if (!names.contains(vecCb_str)) {
				boolean flag = vecCb.remove(vecCb_str);
				selectedNum = 0;
			}
		}

		if (!vecCb.contains("- 모두에게 -")) {
			vecCb.add("- 모두에게 -");
		}

		cbChoice = new JComboBox(vecCb);
		cbChoice.setSelectedIndex(selectedNum);
		cbChoice.setFont(FONT);
		tfMsg = new JTextField(28);
		Font font1 = new Font("맑은 고딕", Font.BOLD, 17);
		tfMsg.setFont(font1);
		tfMsg.setFocusable(true);
		btnSend = new JButton("전송");
		btnSend.setPreferredSize(BTNSIZE);
		btnSend.setFont(FONT);

		hostMenu.add("- 방장 메뉴 -");
		hostMenu.add("방 정보 변경");
		hostMenu.add("강퇴 / 위임");
		cbHost = new JComboBox(hostMenu);
		if (!Client.getName().equals(names.get(0))) {
			cbHost.setEnabled(false);
		}
		cbHost.setFont(FONT);
		btnExit = new JButton("나가기");
		btnExit.setPreferredSize(BTNSIZE);
		btnExit.setFont(FONT);
		btnInvite = new JButton("초대");
		btnInvite.setPreferredSize(BTNSIZE);
		btnInvite.setFont(FONT);
	}

	JScrollPane scrollChat;

	public void setDisplay(int roomType) {
		JLabel lbl1 = new JLabel(" ");
		lbl1.setPreferredSize(new Dimension(10, 0));
		JPanel pnlNorth = new JPanel(new FlowLayout(FlowLayout.LEFT));
		pnlNorth.add(lbl1);
		lblRoomKind.setFont(FONT);
		pnlNorth.add(pnlTitle);
		JPanel pnlsear = new SearchPnl(RoomUI.WAITINGROOM, 10, 10);// 1
		if (roomType == 2) {
			pnlsear = new SearchPnl(RoomUI.CHATINGROOM, 10, 10);// 1
			JLabel lbl2 = new JLabel(" ");
			lbl2.setPreferredSize(new Dimension(400, 0));
			pnlNorth.add(lbl2);
			pnlNorth.add(cbHost);
			cbHost.setPreferredSize(CBSIZE);
			pnlNorth.add(btnExit);
		}
		JPanel pnlCenter = new JPanel();
		JPanel pnlChat = new JPanel();
		scrollChat = new JScrollPane(taChat);
		taChat.setLineWrap(true);
		scrollChat.getVerticalScrollBar().setValue(scrollChat.getVerticalScrollBar().getMaximum());
		scrollChat.setPreferredSize(new Dimension(450, 470));
		scrollChat.setVerticalScrollBarPolicy(scrollChat.VERTICAL_SCROLLBAR_ALWAYS);
		pnlChat.add(scrollChat);
		JPanel pnlPersonList = new JPanel(new BorderLayout());
		JPanel pnlPersonListTop = new JPanel(new GridLayout(2, 1));
		pnlPersonListTop.add(lblPersonKind);
		lblPersonKind.setFont(FONT);
		JPanel pnlSearch = new JPanel();

		pnlSearch.add(pnlsear);
		pnlPersonListTop.add(pnlSearch);
		JScrollPane scrollPersonList = new JScrollPane(personList);
		scrollPersonList.setVerticalScrollBarPolicy(scrollPersonList.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPersonList.setPreferredSize(new Dimension(200, 350));
		pnlPersonList.add(pnlPersonListTop, BorderLayout.NORTH);
		pnlPersonList.add(scrollPersonList, BorderLayout.CENTER);
		JPanel pnlPersonListBottom = new JPanel();
		JPanel pnlWhisper = new JPanel();
		pnlWhisper.add(btnWhisper);
		pnlPersonListBottom.add(pnlWhisper);
		if (roomType == 2) {
			JPanel pnlInvite = new JPanel();
			btnInvite.setPreferredSize(BTNSIZE);
			pnlInvite.add(btnInvite);
			pnlPersonListBottom.add(pnlInvite);
		}
		pnlPersonList.add(pnlPersonListBottom, BorderLayout.SOUTH);
		pnlCenter.add(pnlChat);
		pnlCenter.add(pnlPersonList);
		JPanel pnlSouth = new JPanel();
		pnlSouth.add(cbChoice);
		cbChoice.setPreferredSize(CBSIZE);
		pnlSouth.add(tfMsg);
		pnlSouth.add(btnSend);

		JPanel pnl = new JPanel(new BorderLayout());
		pnl.add(pnlNorth, BorderLayout.NORTH);
		pnl.add(pnlCenter, BorderLayout.CENTER);
		pnl.add(pnlSouth, BorderLayout.SOUTH);

		setLayout(new FlowLayout());
		add(pnl);

	}

	public void addListeners() {
		btnSend.addActionListener(this);
		btnWhisper.addActionListener(this);
		cbChoice.addActionListener(this);
		btnExit.addActionListener(this);
		btnInvite.addActionListener(this);
		cbHost.addActionListener(this);
		tfMsg.addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnSend || e.getSource() == tfMsg) {// Request.ROOM_MSG_SEND
			String str = tfMsg.getText();
			Object[] obj = { str };
			SendData data = new SendData(Request.ROOM_MSG_SEND, obj);
			if (!str.equals("")) {
				if (whisperFlag || cbChoice.getSelectedIndex() != 0) {
					whisperName = (String) cbChoice.getSelectedItem();
					Object[] obj_whisper = { str, whisperName };
					data = new SendData(Request.WHISPER_MSG_CLIENT, obj_whisper);
				}
				try {
					tfMsg.setText("");
					tfMsg.requestFocus();
					tfMsg.setFocusable(true);
					Client.getWrite().writeObject(data);
					Client.getWrite().flush();
					Client.getWrite().reset();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}

		} else if (e.getSource() == btnWhisper) {// Request.WHISPER_MSG_CLIENT
			if (personList.isSelectionEmpty()) {
				JOptionPane.showMessageDialog(null, "귓속말 대화하고 싶은 사람을 고르시오!");
			} else {
				selected = (String) personList.getSelectedValue();
				String selected_name = "";
				if (selected.contains("(방장)")) {
					selected_name = selected.replace("(방장)", "");
				} else {
					selected_name = selected;
				}
				if (!vecCb.contains(selected_name)) {
					vecCb.add(selected_name);
				}
				cbChoice.setSelectedItem(selected_name);
				selectedNum = cbChoice.getSelectedIndex();
				whisperFlag = true;
			}

		} else if (e.getSource() == cbChoice) {
			if (cbChoice.getSelectedIndex() == 0) {
				whisperFlag = false;
				selectedNum = cbChoice.getSelectedIndex();
			} else {
				whisperFlag = true;
				selectedNum = cbChoice.getSelectedIndex();
			}
		} else if (e.getSource() == btnExit) {

			if (cbHost.isEnabled() && Client.roomInfo.getPersonList().size() > 1) {
				JOptionPane.showConfirmDialog(this, "먼저 방장 위임을 하시고 퇴장하세요!", "알림", JOptionPane.DEFAULT_OPTION);
			} else {
				SendData sd = new SendData(Request.CHATTINGROOM_BACKTO_WAITINGROOM);
				try {
					Client.getWrite().writeObject(sd);
					Client.getWrite().flush();
					Client.getWrite().reset();
				} catch (IOException e1) {
					e1.printStackTrace();
				}

			}
		} else if (e.getSource() == btnInvite) {

			ChattingRoom room = (ChattingRoom) Client.roomInfo;
			if (room.getPersonList().size() == room.getPersonNum_MAX()) {
				JOptionPane.showConfirmDialog(this, "정원이 가득 찬 방입니다!", "알림", JOptionPane.DEFAULT_OPTION);
				return;
			}
			// Request.INVITE_CLIENTLIST 94
			try {
				Client.getWrite().writeObject(new SendData(Request.INVITE_CLIENTLIST));
				Client.getWrite().flush();
				Client.getWrite().reset();

			} catch (IOException e1) {
				e1.printStackTrace();
			}

		} else if (e.getSource() == cbHost) {
			if (cbHost.getSelectedIndex() == 1) {
				String pw = null;
				if (room.getPw() != 0) {
					pw = String.valueOf(room.getPw());
				}
				new RoomSetting(room.getRoomName(), String.valueOf(room.getPersonNum_MAX()), pw);
			} else if (cbHost.getSelectedIndex() == 2) {
				if (Client.getPersons().size() <= 1) {
					JOptionPane.showMessageDialog(this, "강퇴/위임 할 수 없는 상황입니다");
					return;
				}
				try {
					Client.getWrite().writeObject(new SendData(Request.EXPULSION_HOST_CLICK)); // 강퇴,위임 선택));= 91
					Client.getWrite().flush();
					Client.getWrite().reset();
				} catch (IOException e1) {
					e1.printStackTrace();
				}

			}
		}

	}

	public void setPersonList(JList personList) {
		this.personList = personList;
	}

	public JTextField getTfMsg() {
		return tfMsg;
	}

	public JTextArea getTaChat() {
		return taChat;
	}

	public JList getPersonList() {
		return personList;
	}

	public JPanel getPnlTitle() {
		return pnlTitle;
	}

	public JScrollPane getScrollChat() {
		return scrollChat;
	}

	public String getSelected() {
		return selected;
	}

	public Vector<String> getVecCb() {
		return vecCb;
	}

	public JComboBox getCbChoice() {
		return cbChoice;
	}

}
