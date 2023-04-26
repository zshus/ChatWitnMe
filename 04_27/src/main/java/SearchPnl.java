import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class SearchPnl extends JPanel {
	private JLabel lblImg;
	private JTextField input;
	private JButton btnSearch;
	private int size;
	private int type;
	private int roomType;
	public static boolean searchFlag = true;
	private String searchStr;
	public SearchPnl(int roomType, int size, int type) {
		this.roomType = roomType;
		this.size = size;
		this.type = type;
		lblImg = new JLabel(new ImageIcon("refresh_large.png"));
		if (size == 0) {
			size = 18;
		}
		input = new JTextField(size);
		btnSearch = new JButton("검색");
		btnSearch.setFocusable(false);
		this.setLayout(new FlowLayout(FlowLayout.CENTER));
		add(lblImg);
		add(input);
		add(btnSearch);
		addListeners();
		searchStr="";

	}

	private void addListeners() {
		btnSearch.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				Object[] obj = null;
				int functionNum;
				if (input.getText().equals("") || input.getText() == null) {
					JOptionPane.showConfirmDialog(SearchPnl.this, "검색 내용을 입력해 주세요!", "알림", JOptionPane.DEFAULT_OPTION);
					return;

				}

				if (type == 0) {// 방 검색
					functionNum = Request.SEARCH_ROOM;
					searchStr=input.getText();
					obj = new Object[] { input.getText() };
				} else {// 인원 검색
					functionNum = Request.SEARCH_PERSONLIST;
					int roomID = 0;
					if (roomType == RoomUI.WAITINGROOM) {
						roomID = 0;
					} else if (roomType == RoomUI.CHATINGROOM) {
						roomID = Client.roomInfo.getRoomId();
						if (SearchForPeople.isExit) {// 초대
							roomID = 0;
						} else if (SearchForPeople.selectnum == 1 || SearchForPeople.selectnum == 2) {// 방장 기능
							roomID = Client.roomInfo.getRoomId();
						}

					}
					obj = new Object[] { roomID, input.getText() };
				}

				SendData data = new SendData(functionNum, obj);
				try {

					Client.getWrite().writeObject(data);
					Client.getWrite().flush();
					Client.getWrite().reset();
					searchFlag = false;
					SearchForPeople.isExit = false;
				} catch (IOException e1) {
					e1.printStackTrace();
				}

			}
		});

		lblImg.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int functionNum;
				Object[] obj = null;
				if (type == 0) {
					functionNum = Request.SEARCH_ROOM;
					searchStr="";
					obj = new Object[] { "" };
				} else {
					functionNum = Request.SEARCH_PERSONLIST;
					int roomID = 0;
					if (roomType == RoomUI.WAITINGROOM) {
						roomID = 0;

					} else if (roomType == RoomUI.CHATINGROOM) {
						roomID = Client.roomInfo.getRoomId();
						if (SearchForPeople.isExit) {// 초대
							roomID = 0;
						} else if (SearchForPeople.selectnum == 1 || SearchForPeople.selectnum == 2) {// 방장 기능
							roomID = Client.roomInfo.getRoomId();
						}
					}
					obj = new Object[] { roomID, "" };
				}
				try {
					Client.getWrite().writeObject(new SendData(functionNum, obj));
					Client.getWrite().flush();
					Client.getWrite().reset();
					input.setText("");
					searchFlag = true;
				} catch (IOException e1) {

					e1.printStackTrace();
				}

			}
		});

	}

	public JTextField getInput() {
		return input;
	}

	public JButton getBtnSearch() {
		return btnSearch;
	}
	public String getSearchStr() {
		return searchStr;
	}

}
