import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class LoginFrame extends JFrame {
	private JLabel lblChat;
	private JTextField tfNick;
	private JButton btnEnter;
	private Socket sock;
	private Client client;
	private String ip;

	public LoginFrame() {
		ip = JOptionPane.showInputDialog("접속할 IP를 입력하세요");
		if (ip == null) {
			return;
		}
		init();
		setDisplay();
		addListeners();
		showFrame();

	}

	public void init() {
		lblChat = new JLabel("로그인할 < 닉네임 > 입력하세요:");
		tfNick = new JTextField(20);
		btnEnter = new JButton("로그인");
	}

	public void setDisplay() {
		JPanel pnlNorth = new JPanel(new FlowLayout(FlowLayout.CENTER));
		pnlNorth.add(lblChat);

		JPanel pnlCenter = new JPanel();
		pnlCenter.add(tfNick);

		JPanel pnlSouth = new JPanel(new FlowLayout(FlowLayout.CENTER));
		pnlSouth.add(btnEnter);

		add(pnlNorth, BorderLayout.NORTH);
		add(pnlCenter, BorderLayout.CENTER);
		add(pnlSouth, BorderLayout.SOUTH);
	}

	public void addListeners() {
		ActionListener aListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				Object src = ae.getSource();
				if (src == btnEnter) {

					boolean flag = true;
					if (tfNick.getText().trim().length() == 0 && flag) {
						JOptionPane.showMessageDialog(LoginFrame.this, "닉네임을 입력하십시오.");
						flag = false;
						tfNick.requestFocus();
					}

					if (flag) { // 닉네임을 입력 된 상태이면...
						Go_waittingRoom();
					}
				}
			}
		};

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				int x = JOptionPane.showConfirmDialog(LoginFrame.this, "종료하시겠습니까?", "종료", JOptionPane.YES_NO_OPTION,
						JOptionPane.INFORMATION_MESSAGE);
				if (x == JOptionPane.YES_OPTION) {
					System.exit(0);
				}
			}
		});
		btnEnter.addActionListener(aListener);
	}

	public void showFrame() {
		setTitle("채팅 프로그램");
		setSize(360, 150);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setResizable(false);
		setVisible(true);
	}

	private void Go_waittingRoom() {

		try {
			sock = new Socket(ip, 10001);

			client = new Client(sock, tfNick.getText());
			Object[] obj = { (Object) tfNick.getText() };
			ObjectOutputStream oos = client.getWrite();
			oos.writeObject(new SendData(Request.CONNECT, obj));
			oos.flush();
			oos.reset();
			dispose();
		} catch (UnknownHostException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				new LoginFrame();

			}

		});

	}
}
