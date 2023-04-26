import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

public class WaittingRoom extends JFrame {
	private WaittingRoomPnl pnlRoom;

	private JButton btnMkRoom;
	private JButton btnleft;
	private JButton btnright;
	private JButton btnMove;
	private JPanel pnlCenter;
	private JTextField pageNum;
	private JLabel lblNum;
	private String currentPage;

	private SearchPnl pnlSearch;

	private RoomUI chattingArea;
	private Vector<String> names;
	private Vector<RoomInfo> rooms;
	private String msg;

	private Vector<String> wp;
	private String maxNum;
	

	public WaittingRoom(Vector<RoomInfo> rooms, Vector<String> names, String maxNum) {
		this.rooms = rooms;
		this.names = names;
		this.maxNum = maxNum;
		init();
		setDisplay();
		addListener();
		showFrame();
	}

	private void init() {
		currentPage = "1";
		msg = Client.getMsg();
		pnlRoom = new WaittingRoomPnl(rooms);
		btnMkRoom = new JButton("방 만들기");
		btnMkRoom.setFocusPainted(false);
		btnleft = new JButton("<<");
		btnright = new JButton(">>");
		btnMove = new JButton("이동");
		btnleft.setFocusable(false);
		btnright.setFocusable(false);
		btnMove.setFocusable(false);
		pageNum = new JTextField(3);
		pageNum.setText(currentPage);
		if (maxNum.equals("0")) {
			maxNum = "1";
		}
		lblNum = new JLabel("/ " + maxNum);
		chattingArea = new RoomUI(RoomUI.WAITINGROOM, names, msg);
		pnlSearch = new SearchPnl(0, 0, 0);		
	}

	JPanel pnlSouth;

	private void setDisplay() {
		JPanel pnlWest = new JPanel(new BorderLayout());

		JPanel pnlNorth = new JPanel();
		pnlNorth.add(pnlSearch);
		pnlWest.add(pnlNorth, BorderLayout.NORTH);

		pnlCenter = new JPanel();
		pnlCenter.add(pnlRoom);
		pnlWest.add(pnlCenter, BorderLayout.CENTER);

		pnlSouth = new JPanel(new FlowLayout(FlowLayout.CENTER));
		pnlSouth.add(btnMkRoom);
		pnlSouth.add(btnleft);
		pnlSouth.add(pageNum);
		pnlSouth.add(lblNum);
		pnlSouth.add(btnright);
		pnlSouth.add(btnMove);

		pnlWest.add(pnlSouth, BorderLayout.SOUTH);
		add(chattingArea, BorderLayout.EAST);
		add(pnlWest, BorderLayout.WEST);

		JLabel pnlc = new JLabel();
		pnlc.setOpaque(true);
		pnlc.setBackground(Color.GRAY);
		pnlc.setBorder(new LineBorder(Color.GRAY));
		add(pnlc, BorderLayout.CENTER);
		add(new JLabel(" "), BorderLayout.SOUTH);
	}

	private void addListener() {
		ActionListener al = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ObjectOutputStream oos = Client.getWrite();
				if (e.getSource() == btnMkRoom) {
					new RoomSetting(WaittingRoom.this, "채팅방 만들기", true);
				} else if (e.getSource() == btnleft) {
					int num = Integer.parseInt(pageNum.getText());
					if (num > 1 && num <= Integer.parseInt(maxNum)) {
						pageNum.setText(String.valueOf(num - 1));
						try {
							oos.writeObject(
									new SendData(Request.CHANGE_PAGE, new Object[] { pnlSearch.getSearchStr(), String.valueOf(num - 1) }));
							oos.flush();
							oos.reset();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
				} else if (e.getSource() == btnright) {
					int num = Integer.parseInt(pageNum.getText());
					if (num < Integer.parseInt(maxNum) && num >= 1) {
						pageNum.setText(String.valueOf(num + 1));

						try {
							oos.writeObject(
									new SendData(Request.CHANGE_PAGE, new Object[] { pnlSearch.getSearchStr(), String.valueOf(num + 1) }));
							oos.flush();
							oos.reset();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
				} else if (e.getSource() == btnMove) {
					int num = Integer.parseInt(pageNum.getText());
					if (num <= Integer.parseInt(maxNum) && num >= 1) {
						try {
							oos.writeObject(
									new SendData(Request.CHANGE_PAGE, new Object[] { pnlSearch.getSearchStr(), String.valueOf(num) }));
							oos.flush();
							oos.reset();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					} else {
						JOptionPane.showMessageDialog(WaittingRoom.this, "존재하지 않는 페이지입니다. 다시 한번 확인해 주세요.");
					}
				}
			}
		};
		btnMkRoom.addActionListener(al);
		btnleft.addActionListener(al);
		btnright.addActionListener(al);
		btnMove.addActionListener(al);

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				int x = JOptionPane.showConfirmDialog(null, "종료하시겠습니까?", "종료", JOptionPane.YES_NO_OPTION,
						JOptionPane.INFORMATION_MESSAGE);
				if (x == JOptionPane.YES_OPTION) {
					ObjectOutputStream oos = Client.getWrite();
					try {
						oos.writeObject(new SendData(Request.EXIT));
						oos.flush();
						oos.reset();
						dispose();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		});
	}

	private void showFrame() {
		setTitle("멋진 대기실");
		setVisible(true);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		pack();
		setResizable(false);
		setLocationRelativeTo(null);
	}

	public JPanel getchattingArea(Vector<String> names, String msg) {
		int target = chattingArea.getPersonList().getSelectedIndex();
		chattingArea.getTaChat().setText(msg);
		chattingArea.getPersonList().setListData(names);
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

	public JPanel getPnlRoom(Vector<RoomInfo> list, boolean move) {
		pnlCenter.remove(pnlRoom);
		lblNum = new JLabel("/ " + maxNum);		
		pnlRoom = new WaittingRoomPnl(list);		
		pnlCenter.add(pnlRoom);
		return pnlCenter;
	}

	public JPanel getPnlRoom(Vector<RoomInfo> list) {
		pnlCenter.remove(pnlRoom);
		lblNum = new JLabel("/ " + maxNum);
		pageNum.setText("1");
		pnlRoom = new WaittingRoomPnl(list);
		pnlCenter.add(pnlRoom);
		return pnlCenter;
	}

	public JPanel getPnlPage(String strNum) {
		pnlSouth.removeAll();
		maxNum = strNum;
		if (strNum.equals("0")) {
			strNum = "1";
		}
		lblNum = new JLabel("/ " + strNum);
		pnlSouth.add(btnMkRoom);
		pnlSouth.add(btnleft);
		pnlSouth.add(pageNum);
		pnlSouth.add(lblNum);
		pnlSouth.add(btnright);
		pnlSouth.add(btnMove);
		return pnlSouth;
	}

	public void getMaxPage(String page) {
		lblNum.setText(page);
	}

	public Vector<String> getWp() {
		return wp;
	}
	public SearchPnl getPnlSearch() {
		return pnlSearch;
	}
}
