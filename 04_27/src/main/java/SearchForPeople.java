
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

public class SearchForPeople extends JDialog {
	private JLabel title;

	private SearchPnl pnlSearch;

	private JList list;
	private JButton btnPositive;
	private JButton btnnagative;

	public static final int INVITE = 0;
	public static final int WHISPER = 1;
	public static final int MANAGE = 2;

	private String[] select = { "invite", "whisper", "manage" };
	private Properties[] propArr = new Properties[select.length];

	private Vector<String> personList;
	public static int selectnum;
	public static boolean isExit;

	public SearchForPeople(int select, Vector<String> personList) {
		this.personList = personList;
		init();
		selectnum = select;
		loadPack(selectnum, personList);
		if (selectnum == 0) {
			isExit = true;
		}
		setDisplay();
		addListeners();
		showFrame();
	}

	private void loadPack(int lang, Vector<String> personList) {
		FileReader fr = null;
		try {
			fr = new FileReader(select[lang] + ".properties");
			propArr[lang] = new Properties();
			propArr[lang].load(fr);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			MyUtils.closeAll(fr);
		}
		Properties prop = propArr[lang];
		title.setText(prop.getProperty("SearchForPeople.title"));
		btnPositive.setText(prop.getProperty("SearchForPeople.btnPositive"));
		btnnagative.setText(prop.getProperty("SearchForPeople.btnnagative"));

		for (int i = personList.size() - 1; i >= 0; i--) {
			if (personList.get(i).equals(Client.roomHost)) {
				personList.remove(i);
			}
		}

		list = new JList(personList);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));

	}

	public void init() {
		title = new JLabel();

		pnlSearch = new SearchPnl(RoomUI.CHATINGROOM, 0, 10);
		pnlSearch.getInput().setText("");

		btnPositive = new JButton();
		btnnagative = new JButton();

	}

	JScrollPane scrollPane;
	JPanel pnlList;

	public void setDisplay() {
		JPanel pnlNorth = new JPanel(new FlowLayout(FlowLayout.CENTER));
		pnlNorth.add(title);

		JPanel pnlCenter = new JPanel(new BorderLayout());

		JPanel pnlInput = new JPanel(new FlowLayout(FlowLayout.LEFT));

		pnlList = new JPanel();
		scrollPane = new JScrollPane(list);
		scrollPane.setPreferredSize(new Dimension(300, 200));
		pnlList.add(scrollPane);

		pnlCenter.add(pnlSearch, BorderLayout.NORTH);
		pnlCenter.add(pnlList, BorderLayout.CENTER);

		JPanel pnlbtn = new JPanel();
		pnlbtn.add(btnPositive);
		pnlbtn.add(btnnagative);

		add(pnlNorth, BorderLayout.NORTH);
		add(pnlCenter, BorderLayout.CENTER);
		add(pnlbtn, BorderLayout.SOUTH);
	}

	public void addListeners() {
		ActionListener aListener = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == btnnagative) {
					if (selectnum == 0) {
						isExit=false;
						dispose();
					} else {
						String temp = (String) list.getSelectedValue();

						try {
							Client.getWrite().writeObject(new SendData(Request.EXPULSION_HOST, new Object[] { temp }));
							Client.getWrite().flush();

							dispose();
						} catch (IOException e1) {
							e1.printStackTrace();
						}

					}

				} else {
					if (list.isSelectionEmpty()) {
						JOptionPane.showConfirmDialog(SearchForPeople.this, "초대할 사람을 선택해 주세요!", "알림",
								JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
					} else {
						String temp = (String) list.getSelectedValue();

						// INVITE_CLIENT
						try {
							if (selectnum == 0) {
								Client.getWrite()
										.writeObject(new SendData(Request.INVITE_CLIENT, new Object[] { temp }));
								Client.getWrite().flush();
								JOptionPane.showConfirmDialog(SearchForPeople.this, "초대메시지를 보냈습니다!", "알림",
										JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
								dispose();
							} else {
								Client.getWrite()
										.writeObject(new SendData(Request.DELEGATION_HOST, new Object[] { temp }));
								Client.getWrite().flush();
								JOptionPane.showConfirmDialog(SearchForPeople.this, "방장 위임 되었습니다!", "알림",
										JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
								dispose();
							}
							isExit = false;
						} catch (IOException e1) {

							e1.printStackTrace();
						}

					}
				}
			}
		};
		btnPositive.addActionListener(aListener);
		btnnagative.addActionListener(aListener);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				isExit = false;
				dispose();

			}
		});
	}

	public void showFrame() {
		pack();
		setResizable(false);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setAlwaysOnTop(true);
		setLocation(1200, 200);
		setVisible(true);
	}

	public void updatList(Vector<String> persons) {
		for (int i = persons.size() - 1; i >= 0; i--) {
			if (persons.get(i).equals(Client.roomHost)) {
				persons.remove(i);
			}
		}

		list.setListData(persons);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		pnlList.remove(scrollPane);
		scrollPane = new JScrollPane(list);
		scrollPane.setPreferredSize(new Dimension(300, 200));
		pnlList.add(scrollPane);
		pnlList.updateUI();
	}

}
