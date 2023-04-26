import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

public class WaittingRoomPnl extends JPanel {
	public WaittingRoomPnl(Vector<RoomInfo> roomInfo) {
		int roomID = 0;
		String name = "";
		int currentNum = 0;
		int maxNum = 0;
		boolean pw = false;

		JPanel pnll = new JPanel(new GridLayout(5, 1, 1, 1));
		if (roomInfo != null || roomInfo.size() != 0) {
			for (RoomInfo info : roomInfo) {
				roomID = info.getRoomID();
				name = info.getName();
				currentNum = info.getCurrentNum();
				maxNum = info.getMaxNum();
				pw = info.isHasPw();
				JPanel pnl = mkPnl(roomID, name, currentNum, maxNum, pw);
				pnll.add(pnl);
			}
		}
		add(pnll);
	}

	private JPanel mkPnl(int roomID, String name, int currentNum, int maxNum, boolean pw) {
		JPanel pnl = new JPanel();
		if (pw) {
			JLabel lblimg = new JLabel(new ImageIcon("lock.png"));
			pnl.add(lblimg);
		}
		JLabel lbl1 = new JLabel(String.valueOf(roomID) + ". ");
		JLabel lbl2 = new JLabel(name);		
		lbl2.setPreferredSize(new Dimension(200, 50));
		JLabel lbl3 = new JLabel(currentNum + "/" + maxNum + "명");
		lbl3.setPreferredSize(new Dimension(50, 50));
		pnl.add(lbl1);
		pnl.add(lbl2);
		pnl.add(lbl3);
		pnl.setBorder(new LineBorder(Color.LIGHT_GRAY, 2));
		pnl.setToolTipText(name);
		pnl.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (pw) {
					try {
						Client.getWrite()
								.writeObject(new SendData(Request.PASSWORD_CHATROOM_ENTRYING, new Object[] { roomID }));
						Client.getWrite().flush();
						Client.getWrite().reset();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				} else {
					try {
						Client.getWrite().writeObject(new SendData(Request.CHATROOM_ENTRY, new Object[] { roomID }));
						Client.getWrite().flush();
						Client.getWrite().reset();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		return pnl;
	}
}
