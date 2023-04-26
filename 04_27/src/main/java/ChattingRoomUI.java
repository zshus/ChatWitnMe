import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class ChattingRoomUI extends JFrame {

	private RoomUI chattingArea;
	private Vector<String> names;
	private String s;

	public ChattingRoomUI(Vector<String> names, String s) {
		this.names = names;
		this.s = s;
		init();
		setDisplay();
		addLisenter();
		showFrame();
	}

	public void init() {
		chattingArea = new RoomUI(RoomUI.CHATINGROOM, names, s);
	}

	public void setDisplay() {
		add(chattingArea, BorderLayout.CENTER);
	}

	private void addLisenter() {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (RoomUI.cbHost.isEnabled() && Client.roomInfo.getPersonList().size() > 1) {
					JOptionPane.showConfirmDialog(ChattingRoomUI.this, "먼저 방장 위임을 하시고 퇴장하세요!", "알림",
							JOptionPane.DEFAULT_OPTION);
				} else {
					SendData sd = new SendData(Request.CHATTINGROOM_BACKTO_WAITINGROOM);
					try {
						Client.getWrite().writeObject(sd);
						Client.getWrite().flush();
					} catch (IOException e1) {
						e1.printStackTrace();
					}

				}
			}
		});
	}

	public void showFrame() {
		setTitle("멋진 대화방 프로그램");
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
		setResizable(false);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
	}

	public JPanel getchattingArea(Vector<String> names, String msg) {
		chattingArea.getTaChat().setText(msg);
		Vector<String> vec = new Vector<String>();
		for (int i = 0; i < names.size(); i++) {
			if (i == 0 && SearchPnl.searchFlag) {
				String ss = names.get(0) + "(방장)";
				vec.add(ss);

			} else {
				vec.add(names.get(i));
			}
		}
		if (!Client.getName().equals(Client.roomHost)) {
			RoomUI.cbHost.setEnabled(false);
		} else {
			RoomUI.cbHost.setEnabled(true);
		}

		int target = chattingArea.getPersonList().getSelectedIndex();
		chattingArea.getPersonList().setListData(vec);
		chattingArea.getPnlTitle().removeAll();
		ChattingRoom room = (ChattingRoom)Client.roomInfo;
		String sName = room.getRoomName();
		JLabel lblName = new JLabel(sName);
		lblName.setToolTipText(sName);
		lblName.setPreferredSize(new Dimension(50, 50));
		String ss = "  (" + room.getPersonList().size() + "/" + room.getPersonNum_MAX() + ")";
		JLabel lblRoomKind = new JLabel(ss, JLabel.LEFT);

		chattingArea.getPnlTitle().add(lblName);
		chattingArea.getPnlTitle().add(lblRoomKind);
		chattingArea.getPersonList().setSelectedIndex(target);
		chattingArea.getScrollChat().getVerticalScrollBar()
				.setValue(chattingArea.getScrollChat().getVerticalScrollBar().getMaximum());
		if (chattingArea.getVecCb().size() != 0) {
			for (int i = chattingArea.getVecCb().size() - 1; i >= 0; i--) {
				String str = chattingArea.getVecCb().get(i);
				if (!names.contains(str) && !str.equals("- 모두에게 -")) {
					chattingArea.getVecCb().remove(str);
					chattingArea.getCbChoice().setSelectedIndex(0);
				}
			}
		}

		return chattingArea;
	}

}
