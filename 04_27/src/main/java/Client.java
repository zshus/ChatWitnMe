import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class Client {

	public static final int NORMAL = 0;
	public static final int EXCEPTIONAL = -1;

	public static Room roomInfo;

	private static String msg;
	private WaittingRoom wr;
	private ChattingRoomUI cr;
	private SearchForPeople sf;
	private static Vector<RoomInfo> roomlist;
	private static Vector<String> persons;
	private ObjectInputStream read;
	private static ObjectOutputStream write;
	private Socket sock;
	private static String name;
	private SecretRoomEnt sr;
	public static String roomHost;

	public Client(Socket sock, String name) {
		this.sock = sock;
		this.name = name;
		connect();

	}

	private void connect() {
		try {

			write = new ObjectOutputStream(sock.getOutputStream());
			read = new ObjectInputStream(sock.getInputStream());
			ClientReceiver receive = new ClientReceiver();
			receive.start();

		} catch (Exception e) {
		}

	}

	public synchronized static ObjectOutputStream getWrite() {
		return write;
	}

	public synchronized ObjectInputStream getRead() {
		return read;
	}

	public synchronized static String getMsg() {
		return msg;
	}

	public synchronized static String getName() {
		return name;
	}

	public synchronized static Vector<RoomInfo> getRoomlist() {
		return roomlist;
	}

	public synchronized static Vector<String> getPersons() {
		return persons;
	}

	class ClientReceiver extends Thread {

		@Override
		public void run() {

			try {
				SendData data = null;
				while ((data = (SendData) read.readObject()) != null) {
					int functionNum = data.getFuntion();
//	                System.out.println("client received : "+ data.getFuntion());

					if (functionNum == Response.CONNECTOKED) { // 101 나에게 대기실 정보 보냄
						msg = "";
						Object[] list = data.getDataList();
						LobbyDTO d = (LobbyDTO) list[0];
						Vector<RoomInfo> rooms = d.getRoomMade();
						roomlist = rooms;
						Vector<String> names = d.getPersonList();
						persons = names;
						roomInfo = new Room(persons, 0, "대기실");
						if (cr != null) {
							cr.dispose();
						}

						SwingUtilities.invokeLater(new Runnable() {

							@Override
							public void run() {
								wr = new WaittingRoom(roomlist, persons, (String) list[1]);

							}

						});

					} else if (functionNum == Response.ERROR_SERVER) {// 접속실패(서버 연결실패) 1000
						JOptionPane.showConfirmDialog(null, "서버 연결이 원활하지 않아 프로그램이 종요됩니다!", "알림창",
								JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
						System.exit(0);

					} else if (functionNum == Response.ERROR_NICKNAME) {// 접속실패(닉네임 중복) 1001
						JOptionPane.showConfirmDialog(null, "중복된 니넥님은 사용할 수 없습니다!", "알림창", JOptionPane.DEFAULT_OPTION,
								JOptionPane.INFORMATION_MESSAGE);

						SwingUtilities.invokeLater(new Runnable() {

							@Override
							public void run() {
								new LoginFrame();

							}

						});

					} else if (functionNum == Response.CONNECTOK) {// 대기실 사람에게 '접속하기' 100
						Object[] list = data.getDataList();
						Vector<String> uNames = (Vector<String>) list[0];
						msg += "----------" + uNames.get(uNames.size() - 1) + "님이 들어 왔습니다!------\n";
						persons = uNames;
						roomInfo.setPersonList(persons);
						SwingUtilities.invokeLater(new Runnable() {

							@Override
							public void run() {
								wr.getchattingArea(persons, msg).updateUI();

							}

						});

					} else if (functionNum == Response.EXIT) {// 종료하기 990 EXIT 대기실 인원에게
						Vector<String> userlist = (Vector<String>) data.getDataList()[0];
						persons = userlist;
						roomInfo.setPersonList(persons);
						msg += "----------" + (String) data.getDataList()[1] + "님이 프로그램을 중료하였습니다!------\n";

						SwingUtilities.invokeLater(new Runnable() {

							@Override
							public void run() {
								wr.getchattingArea(persons, msg).updateUI();

							}

						});

					} else if (functionNum == Response.ROOM_MSG_ALL_SEND) {// 채팅하기
						Object[] list = data.getDataList();
						String mm = (String) list[0];
						String uname = (String) list[1];
						msg += uname + ": " + mm + "\n";
						if (cr != null && !wr.isVisible()) {

							SwingUtilities.invokeLater(new Runnable() {

								@Override
								public void run() {
									cr.getchattingArea(persons, msg).updateUI();

								}

							});
						} else {

							SwingUtilities.invokeLater(new Runnable() {

								@Override
								public void run() {
									wr.getchattingArea(persons, msg).updateUI();

								}

							});
						}

					} else if (functionNum == Response.WHISPER_OK) {// WHISPER_OK 300 귓속말
						Object[] list = data.getDataList();
						String mm = (String) list[0];
						String uname = (String) list[1];
						String temp = (String) list[2];
						if (temp.equals(name)) {
							msg += uname + ": (귓속말) " + mm + "\n";
						} else {
							msg += uname + ": (" + temp + "에게)" + mm + "\n";
						}
						if (cr != null && !wr.isVisible()) {
							SwingUtilities.invokeLater(new Runnable() {

								@Override
								public void run() {
									cr.getchattingArea(persons, msg).updateUI();

								}

							});

						} else {
							SwingUtilities.invokeLater(new Runnable() {

								@Override
								public void run() {
									wr.getchattingArea(persons, msg).updateUI();

								}

							});

						}

					} else if (functionNum == Response.SEARCH_ROOM) {// 600 방 검색
						Object[] list = data.getDataList();
						Vector<RoomInfo> roomInfos = (Vector<RoomInfo>) list[0];
						String str_num = (String) list[1];
						if (roomInfos == null || roomInfos.size() == 0) {
							JOptionPane.showConfirmDialog(wr, "해당 검사 내용이 없습니다!", "알림", JOptionPane.DEFAULT_OPTION,
									JOptionPane.INFORMATION_MESSAGE);
						} else {
							SwingUtilities.invokeLater(new Runnable() {

								@Override
								public void run() {
									wr.getPnlRoom(roomInfos).updateUI();
									wr.getPnlPage(str_num).updateUI();

								}

							});

						}

					} else if (functionNum == Response.SEARCH_PERSON) {// 610인원 검색
						Object[] list = data.getDataList();
						Vector<String> userlist = (Vector<String>) list[0];
						persons = userlist;
						if (userlist == null || userlist.size() == 0) {
							JOptionPane.showConfirmDialog(cr, "해당 검사 내용이 없습니다!", "알림", JOptionPane.DEFAULT_OPTION,
									JOptionPane.INFORMATION_MESSAGE);

						} else {
							if (sf != null && sf.isVisible()) {
								SwingUtilities.invokeLater(new Runnable() {

									@Override
									public void run() {
										sf.updatList(userlist);

									}

								});

							} else {
								if (cr != null && !wr.isVisible()) {
									SwingUtilities.invokeLater(new Runnable() {

										@Override
										public void run() {
											cr.getchattingArea(userlist, msg).updateUI();

										}

									});

								} else {
									SwingUtilities.invokeLater(new Runnable() {

										@Override
										public void run() {
											wr.getchattingArea(userlist, msg).updateUI();

										}

									});
								}
							}
						}

					} else if (functionNum == Response.WAITINGROOM_OUT_SAME) {// 400
						Object[] list = data.getDataList();
						String temp = "";
						for (String s : persons) {
							boolean flag = true;
							for (String str : (Vector<String>) list[0]) {
								if (s.equals(str)) {
									flag = false;
								}
							}
							if (flag) {
								temp = s;
							}
						}
						msg += "----------" + temp + "님이 대기실을 떠났습니다!------\n";
						persons = (Vector<String>) list[0];
						roomInfo.setPersonList(persons);
						Vector<RoomInfo> Roominfos = (Vector<RoomInfo>) list[1];
						String maxPage = (String) list[2];
						
						SwingUtilities.invokeLater(new Runnable() {

							@Override
							public void run() {
								wr.getPnlPage(maxPage).updateUI();
								wr.getchattingArea(persons, msg).updateUI();
								if(wr.getPnlSearch().getSearchStr().equals("")) {
								wr.getPnlRoom(Roominfos,true).updateUI();
								}
								

							}

						});

					} else if (functionNum == Response.WAITINGROOM_OUT_DEFE) {// 401 ??
						Object[] list = data.getDataList();
						String temp = "";
						for (String s : persons) {
							boolean flag = true;
							for (String str : (Vector<String>) list[0]) {
								if (s.equals(str)) {
									flag = false;
								}
							}
							if (flag) {
								temp = s;
							}
						}
						persons = (Vector<String>) list[0];
						roomInfo.setPersonList(persons);
						String maxPage = (String) list[1];
						SwingUtilities.invokeLater(new Runnable() {

							@Override
							public void run() {
								wr.getPnlPage(maxPage);
								wr.getchattingArea(persons, msg).updateUI();
							}

						});

					} else if (functionNum == Response.CHATROOM_ENTRY) {// 402 대화방 입장
						Object[] list = data.getDataList();
						Room rInfo = (Room) list[0];
						roomInfo = rInfo;
						msg = " ";
						persons = rInfo.getPersonList();
						roomHost = persons.get(0);
						SwingUtilities.invokeLater(new Runnable() {

							@Override
							public void run() {
								cr = new ChattingRoomUI(persons, msg);
								wr.dispose();
							}

						});

					} else if (functionNum == Response.CHATROOM_ENTRY_CLIENTS) { // 700
						Object[] list = data.getDataList();
						Vector<String> temp = (Vector<String>) list[0];
						String tempname = "";
						for (String str : temp) {
							boolean flag = false;
							for (String pr : persons) {
								if (!str.equals(pr)) {
									flag = true;
									break;
								}
							}
							if (flag) {
								tempname = str;
							}
						}
						msg += "----------" + tempname + "님이 들어 왔습니다!------\n";
						persons = temp;
						roomInfo.setPersonList(persons);
						SwingUtilities.invokeLater(new Runnable() {

							@Override
							public void run() {
								cr.getchattingArea(persons, msg).updateUI();
							}

						});

					} else if (functionNum == Response.CHATROOM_OUT_SAME) {// CHATROOM_OUT_SAME = 980 같은 페이지의 대기실 인원에게

						Object[] list = data.getDataList();
						String temp = "";
						for (String s : (Vector<String>) list[0]) {
							for (String str : persons) {
								if (!s.equals(str)) {
									temp = s;
								}
							}
						}
						msg += "----------" + temp + "님이 대기실을 들어왔습니다!------\n";
						persons = (Vector<String>) list[0];
						roomInfo.setPersonList(persons);
						Vector<RoomInfo> Roominfos = (Vector<RoomInfo>) list[1];
						SwingUtilities.invokeLater(new Runnable() {

							@Override
							public void run() {
								wr.getchattingArea(persons, msg).updateUI();
								if(wr.getPnlSearch().getSearchStr().equals("")) {
									wr.getPnlRoom(Roominfos,true).updateUI();
								}
//								wr.getPnlRoom(Roominfos,true).updateUI();
							}

						});

					} else if (functionNum == Response.CHATROOM_OUT_DEFF) {// CHATROOM_OUT_DEFF = 981 다른 페이지의 대기실 인원에게
																			// 100

					} else if (functionNum == Response.CHATROOM_OUT_WATROOMCLIENT) {// CHATROOM_OUT_WATROOMCLIENT = 982
																					// 방 없어지는 경우 /방 리스트 갱신+사람 리스트
						Object[] list = data.getDataList();
						String temp = "";
						for (String s : (Vector<String>) list[0]) {
							for (String str : persons) {
								if (!s.equals(str)) {
									temp = s;
								}
							}
						}
						msg += "----------" + temp + "님이 대기실을 들어왔습니다!------\n";
						persons = (Vector<String>) list[0];
						roomInfo.setPersonList(persons);
						Vector<RoomInfo> Roominfos = (Vector<RoomInfo>) list[1];
						String num = (String) list[2];
						SwingUtilities.invokeLater(new Runnable() {

							@Override
							public void run() {
								wr.getchattingArea(persons, msg).updateUI();
								wr.getPnlPage(num).updateUI();
//								wr.getPnlRoom(Roominfos,true).updateUI();
								if(wr.getPnlSearch().getSearchStr().equals("")) {
									wr.getPnlRoom(Roominfos,true).updateUI();
								}
							}
						});

					} else if (functionNum == Response.CHATROOM_OUT_CHATROOMCLIENT) {// CHATROOM_OUT_CHATROOMCLIENT =
																						// 983 대화방 인원에게
						Object[] list = data.getDataList();
						String temp = "";
						for (String s : persons) {
							boolean flag = true;
							for (String str : (Vector<String>) list[0]) {
								if (s.equals(str)) {
									flag = false;
								}
							}
							if (flag) {
								temp = s;
							}
						}
						msg += "----------" + temp + "님이 대화방을 떠났습니다!------\n";
						persons = (Vector<String>) list[0];
						roomInfo.setPersonList(persons);
						SwingUtilities.invokeLater(new Runnable() {

							@Override
							public void run() {
								cr.getchattingArea(persons, msg).updateUI();
							}

						});

					} else if (functionNum == Response.PASSWORDROOM_ENTRY_REQ) {/// Response.PASSWORDROOM_ENTRY_REQ 740
																				/// 비밀방 입장 요청
						Object[] list = data.getDataList();
						SwingUtilities.invokeLater(new Runnable() {

							@Override
							public void run() {
								sr = new SecretRoomEnt(write, (RoomInfo) list[0]);
							}

						});

					} else if (functionNum == Response.PASSWORDROOM_ENTRY) {/// Response.PASSWORDROOM_ENTRY 750 비밀방 입장
						Object[] list = data.getDataList();
						Room rInfo = (Room) list[0];
						roomInfo = rInfo;
						msg = " ";
						persons = rInfo.getPersonList();
						SwingUtilities.invokeLater(new Runnable() {

							@Override
							public void run() {
								cr = new ChattingRoomUI(persons, msg);
								sr.dispose();
								wr.dispose();
							}

						});

					} else if (functionNum == Response.PASSWORDROOM_ENTRY_CHECK_PW_FALSE) {/// Response.PASSWORDROOM_ENTRY_CHECK_PW_FALSE
																							/// 751 비밀번호 틀림
						String msgTemp = "비밀번호가 틀렸습니다. ";
						JOptionPane.showMessageDialog(wr, msgTemp);

					} else if (functionNum == Response.PASSWORDROOM_ENTRY_HOST) {/// Response.PASSWORDROOM_ENTRY_HOST
																					/// 800 비밀번호 요청
						Object[] list = data.getDataList();
						String msgTemp = (String) list[0] + "님이 보내온 요청 : " + (String) list[1];
						int s = JOptionPane.showConfirmDialog(cr, msgTemp, "알림", JOptionPane.YES_NO_OPTION);
						if (s == JOptionPane.YES_OPTION) {
							try {

								write.writeObject(new SendData(Request.PASSWORD_CHATROOM_REQ_ACCEPT,
										new Object[] { (String) list[0] }));
								write.flush();
								write.reset();
							} catch (Exception e) {
							}
						} else {
							try {
								write.writeObject(new SendData(Request.PASSWORD_CHATROOM_REQ_REFUSE,
										new Object[] { (String) list[0] }));
								write.flush();
								write.reset();
							} catch (Exception e) {
							}
						}

					} else if (functionNum == Response.PASSWORDROOM_REQ_ACCEPT) {// 810 비밀번호 요청 수락
						Object[] list = data.getDataList();
						SwingUtilities.invokeLater(new Runnable() {

							@Override
							public void run() {
								new SecretRoomPW(write, (RoomInfo) list[1], (String) list[0]);
							}

						});

					} else if (functionNum == Response.PASSWORDROOM_REQ_REFUSE) {// 820 비밀번호 요청 거절
						String msgStr = "비밀번호요청이 거절되었습니다. ";
						JOptionPane.showMessageDialog(wr, msgStr);

					} else if (functionNum == Response.CLIENTLIST) {// 940 대기실의 인원 리스트 받음
						Object[] list = data.getDataList();
						SwingUtilities.invokeLater(new Runnable() {

							@Override
							public void run() {
								sf = new SearchForPeople(SearchForPeople.INVITE, (Vector<String>) list[0]);
							}

						});

					} else if (functionNum == Response.INVITE) { // 950 인원 초대
						Object[] list = data.getDataList();
						String inviteName = (String) list[0];
						String roomname = (String) list[1];
						int roomid = (int) list[2];
						int num = JOptionPane.showConfirmDialog(wr,
								inviteName + "님이 당신을 " + roomname + "방으로 초대하였습니다.\n 수락하시겠습니까?", "알림",
								JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
						if (num == JOptionPane.YES_OPTION) {
							write.writeObject(new SendData(Request.INVITATION_ACCEPT, new Object[] { roomid }));
							write.flush();
							write.reset();
						} else {
							write.writeObject(new SendData(Request.INVITATION_REFUSE, new Object[] { inviteName }));
							write.flush();
							write.reset();
						}
					} else if (functionNum == Response.INVITATION) {// 961 Response.INVITATION 방에 들어가
						Object[] list = data.getDataList();
						ChattingRoom rInfo = (ChattingRoom) list[0];
						roomInfo = rInfo;
						msg = " ";
						persons = rInfo.getPersonList();
						SwingUtilities.invokeLater(new Runnable() {

							@Override
							public void run() {
								cr = new ChattingRoomUI(rInfo.getPersonList(), msg);
								wr.dispose();
							}

						});

					} else if (functionNum == Response.INVITE_SUCCESS) {// 960 Response.INVITE_SUCCESS 방에 있는 사람들한테 알려줌
						Object[] list = data.getDataList();
						Vector<String> temp = (Vector<String>) list[1];
						persons = temp;
						String tempname = (String) list[0];
						roomInfo.getPersonList().add(tempname);
						msg += "----------" + tempname + "님이 들어 왔습니다!------\n";
						SwingUtilities.invokeLater(new Runnable() {

							@Override
							public void run() {
								cr.getchattingArea(persons, msg).updateUI();
							}

						});

					} else if (functionNum == Response.ROOM_PERSONLIST) {// 910
						Object[] list = data.getDataList();
						SwingUtilities.invokeLater(new Runnable() {

							@Override
							public void run() {
								sf = new SearchForPeople(SearchForPeople.MANAGE, (Vector<String>) list[0]);
							}

						});

					} else if (functionNum == Response.CHANGE_ROOM_SETTING) {// 900 Response.CHANGE_ROOM_SETTING
						Object[] list = data.getDataList();
						ChattingRoomInfo rInfo = (ChattingRoomInfo) list[0];
						ChattingRoom tempRoom = (ChattingRoom) roomInfo;
						tempRoom.setRoomName(rInfo.getRoomName());
						tempRoom.setPersonNum_MAX(rInfo.getPersonNum_MAX());
						if (rInfo.getPw() != 0) {
							tempRoom.setPw(rInfo.getPw());
						} else {
							tempRoom.setPw(0);
						}

						msg += "----------방 정보가 변경하였습니다. 확인하세요!------\n";
						SwingUtilities.invokeLater(new Runnable() {

							@Override
							public void run() {
								cr.getchattingArea(persons, msg).updateUI();
							}

						});

					} else if (functionNum == Response.CHANGE_ROOM_SETTING_WAITTINGROOM) {// 901
						Object[] list = data.getDataList();
						Vector<RoomInfo> Roominfos = (Vector<RoomInfo>) list[0];
						SwingUtilities.invokeLater(new Runnable() {

							@Override
							public void run() {
								wr.getPnlRoom(Roominfos,true).updateUI();
							}

						});

					} else if (functionNum == Response.CHANGE_HOST) {// Response.CHANGE_HOST 920 방장 위임
						Object[] list = data.getDataList();
						Vector<String> temp = (Vector<String>) list[0];
						String host = (String) list[1];
						Vector<String> newTemp = new Vector<>();
						newTemp.add(host);
						for (String s : temp) {
							if (!newTemp.contains(s)) {
								newTemp.add(s);
							}
						}
						msg += "----------" + host + "님이 방장이 되었습니다!------\n";
						roomHost = host;
						persons = newTemp;
						roomInfo.setPersonList(persons);						
						SwingUtilities.invokeLater(new Runnable() {

							@Override
							public void run() {
								cr.getchattingArea(persons, msg).updateUI();
							}

						});

					} else if (functionNum == Response.BAN) {// 930 강퇴 해당 인원에게
						Object[] list = data.getDataList();
						msg = "";
						LobbyDTO d = (LobbyDTO) list[0];
						Vector<RoomInfo> rooms = d.getRoomMade();
						roomlist = rooms;
						Vector<String> names = d.getPersonList();
						persons = names;
						roomInfo.setPersonList(persons);
						if (cr != null) {
							cr.dispose();
						}

						String max = (String) list[1];
						SwingUtilities.invokeLater(new Runnable() {

							@Override
							public void run() {
								wr = new WaittingRoom(roomlist, persons, max);
							}

						});

					} else if (functionNum == Response.BAN_INFO) {// 931 대화방인원에게
						Object[] list = data.getDataList();
						Vector<String> names = (Vector<String>) list[0];
						persons.removeAllElements();
						roomInfo.getPersonList().removeAllElements();
						roomInfo.setPersonList(names);
						for (String s : names) {
							persons.add(s);
						}
						String goOutPerson = (String) list[1];// 강퇴된 사람

						msg += "----------" + goOutPerson + "님이 강퇴되었습니다!------\n";
						SwingUtilities.invokeLater(new Runnable() {

							@Override
							public void run() {
								cr.getchattingArea(names, msg).updateUI();
							}

						});

					} else if (functionNum == Response.PAGE_INFO) {// 페이지 넘기는 기능
						Object[] list = data.getDataList();
						Vector<RoomInfo> Roominfos = (Vector<RoomInfo>) list[0];
						SwingUtilities.invokeLater(new Runnable() {

							@Override
							public void run() {
								wr.getPnlRoom(Roominfos, true).updateUI();
							}

						});

					} else if (functionNum == Response.INVITE_FAILED) {// 970 초대 거절
						Object[] list = data.getDataList();
						String tempName = (String) list[0];
						JOptionPane.showConfirmDialog(cr, tempName + "님이 초대 요청을 거절하였습니다!", "알림",
								JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);

					}

					else if (functionNum == Response.ERROR_NET) { // 1002번
						JOptionPane.showConfirmDialog(null, "네트워크 문제로 연결이 되지 않습니다. 프로그램 종료됩니다", "알림창",
								JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
						System.exit(0);
					} else if (functionNum == Response.ERROR_INFO_NULL) {// 1003
						JOptionPane.showConfirmDialog(null, "해당 정보를 찾을 수 없습니다. 다시 시도해주세요", "알림창",
								JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);

					} else if (functionNum == Response.ERROR_MADE_FAILED) {// 1004
						String msgStr = "방생성에 실패했습니다. 다시한번 시도해 주세요. ";
						JOptionPane.showMessageDialog(wr, msgStr);

					} else if (functionNum == Response.ERROR_ROOM_FULL) {// 1005
						String msgStr = "이미 가득찬 방입니다. ";
						JOptionPane.showMessageDialog(wr, msgStr);

					} else if (functionNum == Response.ERROR_CHECK_PERSON) {// 1006 변경하려는 방정원이 현재 인원보다 적을 때 실패
						String msgStr = "방의 실제 인원보다 적어서 설정이 실패하였습니다! ";
						JOptionPane.showMessageDialog(cr, msgStr);

					} else if (functionNum == Response.EXIT_FORCED_WAITINGROOM) { // 991 대기실에서 강제종료
						Object[] list = data.getDataList();
						Vector<String> nameData = (Vector<String>) list[0];
						String tempName = "";
						for (String str : persons) {
							boolean flag = true;
							for (String s : nameData) {
								if (str.equals(s)) {
									flag = false;
									break;
								}
							}
							if (flag) {
								tempName = str;
							}
						}
						msg += "----------" + tempName + "님이 프로그램을 종료하였습니다!------\n";
						persons = nameData;
						roomInfo.setPersonList(persons);
						SwingUtilities.invokeLater(new Runnable() {

							@Override
							public void run() {
								wr.getchattingArea(persons, msg).updateUI();
							}

						});

					} else if (functionNum == Response.EXIT_FORCED_CHATTINGROOM) {// 1100 채팅방 안에서 강제종료
						Object[] list = data.getDataList();
						String goOutPerson = (String) list[0];
						Vector<String> names = (Vector<String>) list[1];
						persons = names;
						roomInfo.setPersonList(persons);
						msg += "----------" + goOutPerson + "님이 프로그램을 종료하였습니다!------\n";
						SwingUtilities.invokeLater(new Runnable() {

							@Override
							public void run() {
								cr.getchattingArea(persons, msg).updateUI();
							}

						});

					} else if (functionNum == Response.EXIT_FORCED_ROOM_DEL) { // 1101 채팅방 안에서 강제종료로 방이 사라지는 경우 대기실에게 ok
						Object[] list = data.getDataList();
						Vector<RoomInfo> Roominfos = (Vector<RoomInfo>) list[0];
						String num = (String) list[1];
						SwingUtilities.invokeLater(new Runnable() {

							@Override
							public void run() {
								wr.getPnlPage(num).updateUI();
//								wr.getPnlRoom(Roominfos,true).updateUI();
								if(wr.getPnlSearch().getSearchStr().equals("")) {
									wr.getPnlRoom(Roominfos,true).updateUI();
								}
							}

						});

					} else if (functionNum == Response.EXIT_FORCED_WAITROOM_SAME) { // 1102 채팅방 안에서 강제종료 해당 채팅방 페이지를 보고
																					// 있는 대기실 인원에게
						Object[] list = data.getDataList();
						Vector<RoomInfo> roomInfos = (Vector<RoomInfo>) list[0];
						SwingUtilities.invokeLater(new Runnable() {

							@Override
							public void run() {
//								wr.getPnlRoom(roomInfos,true).updateUI();
								if(wr.getPnlSearch().getSearchStr().equals("")) {
									wr.getPnlRoom(roomInfos,true).updateUI();
								}
							}

						});
					}
				}

			} catch (Exception e) {

			} finally {
				MyUtils.closeAll(read, write, sock);
			}
			System.exit(NORMAL);
		}

	}

}
