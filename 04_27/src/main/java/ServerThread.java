import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Vector;

public class ServerThread extends Thread {
	private Socket sock;
	private ObjectOutputStream oos;
	private ObjectInputStream ois;
	private User user;

	public ServerThread(Socket sock) {
		this.sock = sock;
	}

	@Override
	public void run() {
		try {
			oos = new ObjectOutputStream(sock.getOutputStream());
			ois = new ObjectInputStream(sock.getInputStream());
			SendData received;
			while ((received = (SendData) ois.readObject()) != null) {
				// System.out.println("[server] received : " + received.getFuntion());
				int command = received.getFuntion(); // 프로토콜
				Vector<User> waitUsers; // 대기실에 있는 유저
				Vector<String> waitNameList; // 대기실에 있는 유저이름 리스트
				Vector<User> chatUsers; // 대화방에 있는 유저
				Vector<String> chatNameList; // 대화방에 있는 유저이름 리스트
				int roomNum; // 방넘버
				try {
					switch (command) {

					case Request.CONNECT: // 서버에 접속하기
						user = new User((String) received.getDataList()[0], oos);

						// 니넥님 중복 확인
						Vector<User> allUsers = CommonResource.getUserList();
						Object[] data = received.getDataList();
						String name = (String) data[0];
						if (allUsers.size() != 0) {
							boolean flag = false;
							for (User u : allUsers) {
								if (u.getName().equals(name)) {
									// 중복 메시지 보냄
									oos.writeObject(new SendData(Response.ERROR_NICKNAME));
									oos.flush();
									oos.reset();
									flag = true;
									break;
								}
							}
							if (flag) {
								break;
							}
						}

						// 중복되지 않아, 접속 성공
//						System.out.println((String) received.getDataList()[0] + "접속");
						user.setPage(1); // 보고 있는 페이지 표시
						user.setRoomID(0);
						CommonResource.getUserList().add(user); // 공용자원에 유저 추가

						for (Room temp : CommonResource.getRoomList()) { // 룸 객체에 이름 추가
							if (temp.getRoomId() == 0) {
								temp.getPersonList().add(user.getName());
							}
						}

						waitUsers = new Vector<User>();
						waitNameList = new Vector<String>();
						// 대기실 인원 구함
						for (User temp : CommonResource.getUserList()) {
							if (temp.getRoomID() == 0) {
								waitUsers.add(temp);
								waitNameList.add(temp.getName());
								waitUsers.remove(user); // 자기자신 제외
							}
						}

						for (User waitUser : waitUsers) {
//CONNECTOK = 100번							
							waitUser.getOos().writeObject(new SendData(100, new Object[] { waitNameList })); // 인원 이름 보냄
							waitUser.getOos().flush();
							waitUser.getOos().reset();
						}

						LobbyDTO lobbyDTO = new LobbyDTO(pageInfo(null, user.getPage()), waitNameList);

//CONNECTOKED = 101번
						oos.writeObject(new SendData(101, new Object[] { lobbyDTO, CommonResource.getMaxRoomNum() })); // 로비정보
																														// 보냄
						oos.flush();
						oos.reset();
						waitUsers.clear();
						waitNameList.clear();
						break;

					case Request.ROOM_MSG_SEND: // 채팅하기
						roomNum = user.getRoomID(); // 방번호를 가져옴
						chatUsers = new Vector<User>();
						for (User temp : CommonResource.getUserList()) { // 같은 방번호를 가진 유저 모음
							if (temp.getRoomID() == roomNum) {
								chatUsers.add(temp);
							}
						}
						String msg = (String) received.getDataList()[0]; // 안녕하세요. - 한번에 String으로 메시지를 구성
						for (User Chat : chatUsers) {
//ROOM_MSG_ALL_SEND = 200번         
							Chat.getOos().writeObject(new SendData(200, new Object[] { msg, user.getName() })); // 이름과
							Chat.getOos().flush();
							Chat.getOos().reset();
							// 메시지
							// 전송
						}
						break;

					case Request.WHISPER_MSG_CLIENT: // 귓속말하기
						roomNum = user.getRoomID(); // 방번호를 가져옴
						String whisper = (String) received.getDataList()[0]; // 안녕하세요. - 한번에 String으로 메시지를 구성
						for (User temp : CommonResource.getUserList()) { // 같은 방번호에 받은 이름이 같은 사람 특정
							if (temp.getRoomID() == roomNum
									&& (temp.getName().equals((String) received.getDataList()[1]))
									|| temp.getName().equals(user.getName())) {
//WHISPER_OK = 300번            				

								temp.getOos().writeObject(new SendData(300,
										new Object[] { whisper, user.getName(), (String) received.getDataList()[1] })); // 이름과
								temp.getOos().flush();
								temp.getOos().reset();
								// 전송
							}
						}
						break;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
					case Request.CHATROOM_MAKE: // 새방 만들기
						CommonResource.makeRoom((ChattingRoomInfo) received.getDataList()[0], user); // 방을 생성하고 자신의 이름까지
																										// 방에 추가, 룸ID
																										// 유저에 저장
						CommonResource.getRoomList().get(0).getPersonList().remove(user.getName()); // 대기방 이름 리스트에 자신의
																									// 이름 지움

						waitUsers = new Vector<User>();
						waitNameList = new Vector<String>();
						for (User temp : CommonResource.getUserList()) { // 대기실 인원 구함
							if (temp.getRoomID() == 0) {
								waitUsers.add(temp);
								waitNameList.add(temp.getName());
								waitUsers.remove(user); // 자기자신 제외
							}
						}

						for (int i = 0; i < waitUsers.size(); i++) {
							User waitUser = waitUsers.get(i);
							int result = 0;
							result = ((CommonResource.getRoomList().indexOf(searchID(user.getRoomID())) - 1) / 5) + 1; // 룸
																														// id로
							// 그 룸의
							// 페이지를
							// 알아냄

							if (waitUser.getPage() == result) {
								try {
//WAITINGROOM_OUT_SAME = 400번         
									// 룸 // 400번
									waitUser.getOos().writeObject(
											new SendData(Response.WAITINGROOM_OUT_SAME, new Object[] { waitNameList,
													pageInfo(null, result), CommonResource.getMaxRoomNum() }));
									waitUser.getOos().flush();
									waitUser.getOos().reset();
								} catch (IOException d) {
								}
							} else {
								try {
//WAITINGROOM_OUT_DEFE = 401번

									waitUser.getOos().writeObject(new SendData(Response.WAITINGROOM_OUT_DEFE,
											new Object[] { waitNameList, CommonResource.getMaxRoomNum() })); // 대기실
									waitUser.getOos().flush(); // 리스트
									waitUser.getOos().reset();
									// 보냄.
									// 401번
								} catch (IOException d) {
								}
							}
						}
//CHATROOM_ENTRY = 402번 
						user.getOos().writeObject(
								new SendData(Response.CHATROOM_ENTRY, new Object[] { searchID(user.getRoomID()) })); // 대화방
						user.getOos().flush(); // 입장
						user.getOos().reset();
						// 402번

						break;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

					case Request.CHANGE_PAGE: // 페이지 넘기기
//						System.out.println(received);
						if (CommonResource.getTemp().size() == 0 || CommonResource.getTemp() == null || ((String) received.getDataList()[0]).equals("")) {
							CommonResource.getTemp().removeAllElements();
							for (Room room : CommonResource.getRoomList()) {
								CommonResource.getTemp().add(room);
							}

						}else {
							CommonResource.getTemp().removeAllElements();
							CommonResource.getTemp().add(new Room());
							for(int i = 1; i < CommonResource.getRoomList().size(); i++) {
								if (CommonResource.getRoomList().get(i).getRoomName().contains(((String) received.getDataList()[0]))) {
									ChattingRoom r = (ChattingRoom) CommonResource.getRoomList().get(i);
									CommonResource.getTemp().add(r);
								}
							}
						}

						Vector<RoomInfo> pageInfoo = new Vector<>();
						int pagee = Integer.parseInt((String) received.getDataList()[1]);
						for (int i = (pagee - 1) * 5 + 1; i < CommonResource.getTemp().size() && i <= 5 * pagee; i++) {
							ChattingRoom room = (ChattingRoom) CommonResource.getTemp().get(i);
							RoomInfo roomInfo = new RoomInfo(room.getRoomId(), room.getRoomName(),
									room.getPersonList().size(), room.getPersonNum_MAX(), room.getPw() != 0);
							pageInfoo.add(roomInfo);
						}

						try {
//PAGE_INFO = 500번            			
							user.setPage(Integer.parseInt((String) received.getDataList()[1]));
							user.getOos().writeObject(new SendData(Response.PAGE_INFO,
									new Object[] { pageInfoo, CommonResource.getMaxRoomNum(1) })); // page 리스트 보냄. 500번
							user.getOos().flush();
							user.getOos().reset();
						} catch (IOException d) {
						}

						break;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

					case Request.SEARCH_ROOM: // 방리스트 검색하기
						try {
							// SEARCH_ROOM = 600번

							CommonResource.getTemp().removeAllElements();
							CommonResource.getTemp().add(new Room());

							Vector<RoomInfo> roomInfoList = new Vector<>();
							String r_name = (String) received.getDataList()[0];
							for (int i = 1; i < CommonResource.getRoomList().size(); i++) {
								if (CommonResource.getRoomList().get(i).getRoomName().contains(r_name)) {
									ChattingRoom r = (ChattingRoom) CommonResource.getRoomList().get(i);
									CommonResource.getTemp().add(r);
									RoomInfo rf = new RoomInfo(r.getRoomId(), r.getRoomName(), r.getPersonList().size(),
											r.getPersonNum_MAX(), r.getPw() != 0);
									roomInfoList.add(rf);
								}
							}

							if (((String) received.getDataList()[0]).equals("")) {// r_name
								CommonResource.getTemp().removeAllElements();

								for (Room room : CommonResource.getRoomList()) {
									CommonResource.getTemp().add(room);
								}

							}

							Vector<RoomInfo> roomInfoList_small = new Vector<>();
							for (int i = 0; i < roomInfoList.size() && i < 5; i++) {
								roomInfoList_small.add(roomInfoList.get(i));
							}

							user.getOos().writeObject(new SendData(600, // 600번
									new Object[] { roomInfoList_small, CommonResource.getMaxRoomNum(1) })); // page 리스트 보냄.
							user.getOos().flush();
							user.getOos().reset();
							user.setPage(1);
						} catch (IOException d) {
						}
						break;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

					case Request.SEARCH_PERSONLIST: // 인원 검색하기
						// SEARCH_PERSON = 610번
						Room r = searchID((int) received.getDataList()[0]);						
						Vector<String> uu = r.getPersonList();						
						Vector<String> ne = new Vector<String>();
						for (String s : uu) {
							if (s.contains((String) received.getDataList()[1])) {
								ne.add(s);
								break;
							}
						}
						Object[] obj = { ne };						
						user.getOos().writeObject(new SendData(Response.SEARCH_PERSON, obj)); // 인원
						user.getOos().flush();
						user.getOos().reset();
						break;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
					case Request.CHATROOM_ENTRY: // 일반 대화방에 입장하기

						int normalRoomID = (int) received.getDataList()[0];
						ChattingRoom normalRoom = null;
						for (Room temp : CommonResource.getRoomList()) {
							if (temp.getRoomId() == normalRoomID) {
								normalRoom = (ChattingRoom) temp;
								break;
							}
						}
						if (normalRoom.getPersonList().size() < normalRoom.getPersonNum_MAX()) {

							for (Room temp : CommonResource.getRoomList()) {
								if (temp.getRoomId() == normalRoomID) {
									temp.getPersonList().add(user.getName());
									user.setRoomID(normalRoomID);
									CommonResource.getRoomList().get(0).getPersonList().remove(user.getName());
									break;
								}
							}

							try {
								user.getOos().writeObject(new SendData(Response.CHATROOM_ENTRY,
										new Object[] { searchID(normalRoom.getRoomId()) })); // 대화방 입장 402번
								user.getOos().flush();
								user.getOos().reset();
							} catch (IOException d) {
							}

							for (User temp : CommonResource.getUserList()) { // 채팅방 인원 구함
								if (temp.getRoomID() == normalRoom.getRoomId()) {
									if (!temp.getName().equals(user.getName())) {
										temp.getOos().writeObject(new SendData(Response.CHATROOM_ENTRY_CLIENTS,
												new Object[] { searchID(normalRoomID).getPersonList() }));
										temp.getOos().flush();
										temp.getOos().reset();
									}

								}
							}

							waitUsers = new Vector<User>();
							waitNameList = new Vector<String>();
							for (User temp : CommonResource.getUserList()) { // 대기실 인원 구함
								if (temp.getRoomID() == 0) {
									waitUsers.add(temp);
									waitNameList.add(temp.getName());
									waitUsers.remove(user); // 자기자신 제외
								}
							}

							for (User waitUser : waitUsers) {
								int result = 0;
								result = CommonResource.getRoomList().indexOf(searchID(user.getRoomID())) / 5 + 1; // 룸
																													// id로
																													// 그
																													// 룸의
																													// 페이지를
																													// 알아냄

								if (waitUser.getPage() == result) {
									try {
//WAITINGROOM_OUT_SAME = 400번         
										waitUser.getOos().writeObject(
												new SendData(Response.WAITINGROOM_OUT_SAME, new Object[] { waitNameList,
														pageInfo(null, result), CommonResource.getMaxRoomNum() })); // 룸
																													// 리스트
																													// 보냄.
																													// 400번
										waitUser.getOos().flush();
										waitUser.getOos().reset();
									} catch (IOException d) {
									}
								} else {
									try {
//WAITINGROOM_OUT_DEFE = 401번
										waitUser.getOos().writeObject(new SendData(Response.WAITINGROOM_OUT_DEFE,
												new Object[] { waitNameList, CommonResource.getMaxRoomNum() })); // 대기실
																													// 리스트
																													// 보냄.
																													// 401번
										waitUser.getOos().flush();
										waitUser.getOos().reset();
									} catch (IOException d) {
									}
								}
							}
						} else {
//ERROR_ROOM_FULL = 1005번           				
							user.getOos().writeObject(new SendData(Response.ERROR_ROOM_FULL)); // 방이 가득차 못들어감. 1005번
							user.getOos().flush();
							user.getOos().reset();

						}
						break;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////						

					case Request.PASSWORD_CHATROOM_ENTRYING: // 비밀방 입장 요청 70
						int reqRoomID = (int) received.getDataList()[0];
						ChattingRoom reqRoom = null;
						for (Room temp : CommonResource.getRoomList()) {
							if (temp.getRoomId() == reqRoomID) {
								reqRoom = (ChattingRoom) temp;
							}
						}
						RoomInfo reqRoomInfo = new RoomInfo(reqRoom.getRoomId(), reqRoom.getRoomName(),
								reqRoom.getPersonList().size(), reqRoom.getPersonNum_MAX(), true);
						try {
//PASSWORDROOM_ENTRY_REQ = 740번            				
							user.getOos().writeObject(
									new SendData(Response.PASSWORDROOM_ENTRY_REQ, new Object[] { reqRoomInfo })); // 비밀방
																													// 정보를
																													// 보냄
							user.getOos().flush();
							user.getOos().reset();

						} catch (IOException d) {
						}
						break;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

					case Request.PASSWORD_CHATROOM_ENTRY: // 비밀방에 입장하기
						int secretRoomID = (int) received.getDataList()[0];
						ChattingRoom secretRoom = null;
						for (Room temp : CommonResource.getRoomList()) {
							if (temp.getRoomId() == secretRoomID) {
								secretRoom = (ChattingRoom) temp;
							}
						}
						int inputPW = Integer.parseInt((String) received.getDataList()[1]);
						if (secretRoom.getPw() == inputPW) {
							if (secretRoom.getPersonList().size() < secretRoom.getPersonNum_MAX()) {
								secretRoom.getPersonList().add(user.getName());
								user.setRoomID(secretRoomID);
								try {
//CHATROOM_ENTRY = 750번								
									user.getOos().writeObject(new SendData(Response.PASSWORDROOM_ENTRY,
											new Object[] { searchID(secretRoom.getRoomId()) })); // 비밀방을 보냄
									user.getOos().flush();
									user.getOos().reset();
								} catch (IOException d) {
								}
							} else {
//ERROR_ROOM_FULL = 1005번  	
								user.getOos().writeObject(new SendData(Response.ERROR_ROOM_FULL)); // 방이 가득차 못들어감 .
								user.getOos().flush();
								user.getOos().reset();
								break;
							}
						} else {
//PASSWORDROOM_ENTRY_CHECK_PW_FALSE = 751번		
							user.getOos().writeObject(new SendData(Response.PASSWORDROOM_ENTRY_CHECK_PW_FALSE)); // 비밀번호
																													// 틀려서
																													// 입장
																													// 실패
							user.getOos().flush();
							user.getOos().reset();
							break;
						}

						chatUsers = new Vector<User>();
						chatNameList = new Vector<String>();
						for (User temp : CommonResource.getUserList()) { // 채팅방 인원 구함
							if (temp.getRoomID() == secretRoom.getRoomId()) {
								chatUsers.add(temp);
								chatNameList.add(temp.getName());
								chatUsers.remove(user); // 자기자신 제외
							}
						}
						CommonResource.getRoomList().get(0).getPersonList().remove(user.getName()); // 대기방 이름 리스트에 자신의
																									// 이름 지움
						for (User chatUser : chatUsers) {
							try {
//CHATROOM_ENTRYCLIENTS = 700번
								Room rtemp = searchID(secretRoomID);
								Vector<String> humantemp = rtemp.getPersonList();
								Object[] object = { humantemp };
								chatUser.getOos().writeObject(new SendData(Response.CHATROOM_ENTRY_CLIENTS, object));// 누가
																														// 대화방에
																														// 입장했는지
																														// 대화방에
																														// 있는
																														// 클라이언트에게
																														// 인원리스트
																														// 보냄
																														// 700번
								chatUser.getOos().flush();
								chatUser.getOos().reset();
							} catch (IOException d) {
							}
						}

						waitUsers = new Vector<User>();
						waitNameList = new Vector<String>();
						for (User temp : CommonResource.getUserList()) { // 대기실 인원 구함
							if (temp.getRoomID() == 0) {
								waitUsers.add(temp);
								waitNameList.add(temp.getName());
								waitUsers.remove(user); // 자기자신 제외
							}
						}

						for (User waitUser : waitUsers) {
							int result = 0;
							result = CommonResource.getRoomList().indexOf(searchID(user.getRoomID())) / 5 + 1; // 룸 id로
																												// 그 룸의
																												// 페이지를
																												// 알아냄

							if (waitUser.getPage() == result) {
								try {
//WAITINGROOM_OUT_SAME = 400번         
									waitUser.getOos().writeObject(
											new SendData(Response.WAITINGROOM_OUT_SAME, new Object[] { waitNameList,
													pageInfo(null, result), CommonResource.getMaxRoomNum() })); // 룸 리스트
																												// 보냄
									waitUser.getOos().flush();
									waitUser.getOos().reset();
								} catch (IOException d) {
								}
							} else {
								try {
//WAITINGROOM_OUT_DEFE = 401번
									waitUser.getOos().writeObject(new SendData(Response.WAITINGROOM_OUT_DEFE,
											new Object[] { waitNameList, CommonResource.getMaxRoomNum() })); // 대기실 리스트
																												// 보냄.
									waitUser.getOos().flush();
									waitUser.getOos().reset();
								} catch (IOException d) {
								}
							}
						}
						break;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

					case Request.PASSWORD_CHATROOM_REQ: // 방 비밀번호 요청하기
						String reqMsg = (String) received.getDataList()[0];
						int reqRoomID2 = (int) received.getDataList()[1];
						Room reqRoom2 = searchID(reqRoomID2);
						String hostName = reqRoom2.getPersonList().get(0);
						User host = null;
						for (User temp : CommonResource.getUserList()) {
							if (temp.getName().equals(hostName)) {
								host = temp;
							}
						}
						host.getOos().writeObject(new SendData(Response.PASSWORDROOM_ENTRY_HOST,
								new Object[] { user.getName(), reqMsg })); // 프로토콜 800 (비밀번호 요청)
						host.getOos().flush();
						host.getOos().reset();
						break;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

					case Request.PASSWORD_CHATROOM_REQ_ACCEPT: // 방 비밀번호 요청 수락
						String requesterName = (String) received.getDataList()[0];
						User requester = null;
						for (User temp : CommonResource.getUserList()) {
							if (temp.getName().equals(requesterName)) {
								requester = temp;
							}
						}
						int requestedRoomID = user.getRoomID();
						ChattingRoom requestedRoom = null;
						for (Room temp : CommonResource.getRoomList()) {
							if (temp.getRoomId() == requestedRoomID) {
								requestedRoom = (ChattingRoom) temp;
							}
						}
						RoomInfo requestedRoomInfo = new RoomInfo(requestedRoom.getRoomId(),
								requestedRoom.getRoomName(), requestedRoom.getPersonList().size(),
								requestedRoom.getPersonNum_MAX(), true);
						requester.getOos().writeObject(new SendData(Response.PASSWORDROOM_REQ_ACCEPT,
								new Object[] { String.valueOf(requestedRoom.getPw()), requestedRoomInfo })); // 프로토콜 810
																												// (비밀번호
																												// 요청
																												// 수락)
						requester.getOos().flush();
						requester.getOos().reset();
						break;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

					case Request.PASSWORD_CHATROOM_REQ_REFUSE: // 방 비밀번호 요청 거절
						String refusedName = (String) received.getDataList()[0];
						User refused = null;
						for (User temp : CommonResource.getUserList()) {
							if (temp.getName().equals(refusedName)) {
								refused = temp;
							}
						}
						refused.getOos().writeObject(new SendData(Response.PASSWORDROOM_REQ_REFUSE)); // 프로토콜 820 (비밀번호
																										// 요청 거절)
						refused.getOos().flush();
						refused.getOos().reset();
						break;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

					case Request.ROOM_SETTING_EDIT: // 방정보 변경창- 변경
						ChattingRoomInfo info = (ChattingRoomInfo) received.getDataList()[0]; // 받아온 채팅방 정보
						int roomLocation = CommonResource.getRoomList().indexOf(searchID(user.getRoomID())); // 현 방위치
						Room thisRoom = searchID(user.getRoomID());// 현재 방정보

						if (info.getPersonNum_MAX() < thisRoom.getPersonList().size()) {

							user.getOos().writeObject(new SendData(Response.ERROR_CHECK_PERSON));
							user.getOos().flush();
							user.getOos().reset();

						} else {

							ChattingRoom fixRoom = new ChattingRoom(thisRoom.getPersonList(), thisRoom.getRoomId(),
									info.getRoomName(), info.getPersonNum_MAX(), info.getPw()); // 고친 방
							CommonResource.getRoomList().remove(roomLocation); // 현재 방 삭제
							CommonResource.getRoomList().add(roomLocation, fixRoom); // 고친 방 정보 넣기
							info = new ChattingRoomInfo(fixRoom.getRoomName(), fixRoom.getPersonNum_MAX(),
									fixRoom.getPw()); // 리턴할 정보
							chatUsers = new Vector<User>(); // 대화방 사람들
							for (User temp2 : CommonResource.getUserList()) { // 대화방 사람들 모아둠
								if (temp2.getRoomID() == user.getRoomID()) {
									chatUsers.add(temp2);
								}
							}
							try {
								for (User temp3 : chatUsers) {
									// CHANGE_ROOM_SETTING = 900번

									temp3.getOos().writeObject(
											new SendData(Response.CHANGE_ROOM_SETTING, new Object[] { info })); // 바뀐 방
																												// 정보보냄.
																												// 900번
									temp3.getOos().flush();
									temp3.getOos().reset();
								}
							} catch (IOException d) {
							}

							waitUsers = new Vector<User>(); // 대기실에 있는 유저
							for (User temp2 : CommonResource.getUserList()) { // 대기실 사람들 모아둠
								if (temp2.getRoomID() == 0) {
									waitUsers.add(temp2);
								}
							}
							for (User waitUser : waitUsers) {
								int result = 0;
								result = CommonResource.getRoomList().indexOf(searchID(user.getRoomID())) / 5 + 1; // 룸
																													// id로
																													// 그
																													// 룸의
																													// 페이지를
																													// 알아냄

								if (waitUser.getPage() == result) {
									try {
										// WAITINGROOM_OUT_SAME = 901번

										waitUser.getOos()
												.writeObject(new SendData(Response.CHANGE_ROOM_SETTING_WAITTINGROOM,
														new Object[] { pageInfo(null, result) })); // 룸 리스트 보냄. 901번
										waitUser.getOos().flush();
										waitUser.getOos().reset();
									} catch (IOException d) {
									}
								} else {
								}
							}

						}

						break;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

					case Request.EXPULSION_HOST_CLICK: // 강퇴,위임 선택
						// ROOM_PERSONLIST = 910번
						Vector<String> tempList = searchID(user.getRoomID()).getPersonList();
						Vector<String> tempnew = new Vector<String>();
						for (String s : tempList) {
							if (!s.equals(user.getName())) {
								tempnew.add(s);
							}
						}
						user.getOos().writeObject(new SendData(Response.ROOM_PERSONLIST, new Object[] { tempnew })); // 인원
																														// 리스트
																														// 보냄.
																														// 910번
						user.getOos().flush();
						user.getOos().reset();
						break;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

					case Request.DELEGATION_HOST: // 방장위임
						String newHost = (String) received.getDataList()[0]; // 새로 방장이 될 인물
						Room abc = searchID(user.getRoomID());
						abc.getPersonList().remove(newHost); // 제거
						abc.getPersonList().add(0, newHost); // 첫번쨰 자리에 추가

						chatUsers = new Vector<User>(); // 대화방 사람들
						for (User temp2 : CommonResource.getUserList()) { // 대화방 사람들 모아둠
							if (temp2.getRoomID() == user.getRoomID()) {
								chatUsers.add(temp2);
							}
						}
						try {
							for (User temp3 : chatUsers) {
//CHANGE_HOST = 920번        
								temp3.getOos().writeObject(new SendData(Response.CHANGE_HOST,
										new Object[] { abc.getPersonList(), newHost })); // 920번
								temp3.getOos().flush();
								temp3.getOos().reset();

							}
						} catch (IOException d) {
						}
						break;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

					case Request.EXPULSION_HOST: // 대화방 인원 강퇴
						String s = (String) received.getDataList()[0];// 캉퇴할 사람의 이름

						// 대화방에서 삭제
						for (Room room : CommonResource.getRoomList()) {
							if (room.getRoomId() == user.getRoomID()) {
								room.getPersonList().remove(s);
								break;
							}
						}

						if (!CommonResource.getRoomList().get(0).getPersonList().contains(s)) {

							// 대기실에서 추가
							for (User temp : CommonResource.getUserList()) { // 밴 유저 특정
								if (temp.getName().equals((String) received.getDataList()[0])) {// 강퇴당한 사람이면

									temp.setRoomID(0); // 룸iD를 대기실로 보내고
									CommonResource.getRoomList().get(0).getPersonList().add(temp.getName());

									Vector<RoomInfo> pageInfo = new Vector<>();

									getCurrentPageList(pageInfo, 1);
									lobbyDTO = new LobbyDTO(pageInfo, searchID(0).getPersonList()); // 로비 정보 만듦
									try {

//BAN = 930번
										temp.getOos().writeObject(new SendData(Response.BAN,
												new Object[] { lobbyDTO, CommonResource.getMaxRoomNum() })); // 930번
										temp.getOos().flush();
										temp.getOos().reset();
									} catch (IOException d) {
									}
									break;
								}
							}

							chatUsers = new Vector<User>(); // 대화방 사람들
							for (User temp2 : CommonResource.getUserList()) {
								if (temp2.getRoomID() == user.getRoomID()) {
									chatUsers.add(temp2);
								}
							}

							// 대화방 사람들 메시지 보내
							for (User temp3 : chatUsers) {
//BAN_INFO = 931번           		
								temp3.getOos()
										.writeObject(new SendData(Response.BAN_INFO,
												new Object[] { searchID(user.getRoomID()).getPersonList(),
														(String) received.getDataList()[0] })); // 931번
								temp3.getOos().flush();
								temp3.getOos().reset();
							}

							// 대기실 사람에게

							// 방 지금 어떤 페이지에 있는지 구함
							int roomPage = 0;
							for (int i = 0; i < CommonResource.getRoomList().size(); i++) {
								Room roomTemp = CommonResource.getRoomList().get(i);
								if (roomTemp.getRoomId() == user.getRoomID()) {
									roomPage = (i / 5) + 1;
									break;
								}
							}

							if (roomPage == 0) {
								roomPage = 1;
							}

							Vector<RoomInfo> pageInfooo = new Vector<RoomInfo>();
							getCurrentPageList(pageInfooo, roomPage);

							for (User temp : CommonResource.getUserList()) {
								if (temp.getPage() == roomPage && !temp.getName().equals(s) && temp.getRoomID() == 0) {
									// 프로토콜 980 (같은 페이지 사람에게) 대기실
									temp.getOos().writeObject(new SendData(Response.CHATROOM_OUT_SAME, new Object[] {
											CommonResource.getRoomList().get(0).getPersonList(), pageInfooo }));
									temp.getOos().flush();
									temp.getOos().reset();
								} else if (!temp.getName().equals(s) && temp.getRoomID() == 0) {
									// 프로토콜 981 (다른 페이지 사람에게) 대기실
									temp.getOos().writeObject(new SendData(Response.CONNECTOK,
											new Object[] { CommonResource.getRoomList().get(0).getPersonList() }));
									temp.getOos().flush();
									temp.getOos().reset();
								}
							}

						} else {
							user.getOos().writeObject(new SendData(Response.ERROR_INFO_NULL)); // 프로토콜 1003 (해당 사용자 찾을 수
							user.getOos().flush();
							user.getOos().reset();
						}

						break;
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

					case Request.INVITE_CLIENTLIST: // 대기실 인원 리스트 요청
						Vector<String> waiterList = searchID(0).getPersonList();
						user.getOos().writeObject(new SendData(Response.CLIENTLIST, new Object[] { waiterList })); // 프로토콜
																													// 940
																													// (대기실
																													// 인원
																													// 리스트
																													// 요청)
						user.getOos().flush();
						user.getOos().reset();
						break;
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

					case Request.INVITE_CLIENT: // 인원 초대

						// ERROR_NET(1004) 이건 어떤 경우에? case 전체를 try문에 넣어서 예외처리 하면 됨? Exception,
						// SocketException, NetworkException?

						String invited = (String) received.getDataList()[0];
						int roomID = user.getRoomID();
						boolean flag=false;
						for(Room room:CommonResource.getRoomList()) {
							
							if(room.getRoomId()==roomID) {
								if(room.getPersonList().contains(invited)){
									flag=true;
									break;
								}
							}	
						}
						if(flag)break;
						ChattingRoom inviteFrom = null;
						for (Room temp : CommonResource.getRoomList()) {
							if (temp.getRoomId() == roomID) {
								inviteFrom = (ChattingRoom) temp;
							}
						}
						User invitedUser = null;
						for (User temp : CommonResource.getUserList()) {
							if (temp.getName().equals(invited)) {
								invitedUser = temp;
							}
						}
						if (invitedUser == null) {
							user.getOos().writeObject(new SendData(Response.ERROR_INFO_NULL)); // 프로토콜 1003 (해당 사용자 찾을 수
							user.getOos().flush();
							user.getOos().reset();
							// 없음)
						} else {
							if (inviteFrom.getPersonList().size() < inviteFrom.getPersonNum_MAX()) {
								try {
									invitedUser.getOos().writeObject(new SendData(Response.INVITE, new Object[] {
											user.getName(), inviteFrom.getRoomName(), inviteFrom.getRoomId() })); // 프로토콜
																													// 950(인원초대)
									invitedUser.getOos().flush();
									invitedUser.getOos().reset();

								} catch (IOException e) {
								}
							} else {
								user.getOos().writeObject(new SendData(Response.ERROR_ROOM_FULL)); // 프로토콜 1005 (현재
																									// 방의정원이 차서 초대할 수
																									// 없음)
								user.getOos().flush();
								user.getOos().reset();
							}
						}
						break;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

					case Request.INVITATION_ACCEPT: // 초대 수락

						int roomToGo = (int) received.getDataList()[0];
						ChattingRoom chattingRoom = null;
						Vector<String> personList = new Vector<String>();
						Vector<ObjectOutputStream> ooss = new Vector<ObjectOutputStream>();
						for (Room temp : CommonResource.getRoomList()) {
							if (temp.getRoomId() == roomToGo) {
								for (int i = 0; i < temp.getPersonList().size(); i++) {
									personList.add(temp.getPersonList().get(i));
								}
								chattingRoom = (ChattingRoom) temp;
							}
						}
						for (User temp : CommonResource.getUserList()) {
							if (personList.contains(temp.getName())) {
								ooss.add(temp.getOos());
							}
						}
						personList.add(user.getName());
						user.setRoomID(roomToGo);
						CommonResource.getRoomList().get(0).getPersonList().remove(user.getName());
						if (chattingRoom != null) {
							if (chattingRoom.getPersonList().size() < chattingRoom.getPersonNum_MAX()) {
								for (ObjectOutputStream temp : ooss) {
									try {
										temp.writeObject(new SendData(Response.INVITE_SUCCESS,
												new Object[] { user.getName(), personList })); // 프로토콜 960 (방에 있는 사람에게
																								// 알림)
										temp.flush();
										temp.reset();
									} catch (IOException e) {
									}
								}
								try {
									chattingRoom.getPersonList().add(user.getName());
									user.getOos().writeObject(
											new SendData(Response.INVITATION, new Object[] { chattingRoom })); // 프로토콜
																												// 961(초대된방에들어감)
																												// 방에 있는
																												// 사람에게
									user.getOos().flush();
									user.getOos().reset();
								} catch (IOException e) {
								}

								int result = ((CommonResource.getRoomList().indexOf(searchID(user.getRoomID())) - 1)
										/ 5) + 1; // 룸 id로
								// INVITATION_WAITROOM 962
								Vector<User> waiterUser = new Vector<User>();
								for (String waitRoomPerson : CommonResource.getRoomList().get(0).getPersonList()) {
									for (User u : CommonResource.getUserList()) {
										if (u.getName().equals(waitRoomPerson)) {
											waiterUser.add(u);
										}
									}
								}

								for (User u : waiterUser) {
									if (u.getPage() == result) {
										try {
											// WAITINGROOM_OUT_SAME = 400번
											// 룸 // 400번
											u.getOos().writeObject(new SendData(Response.WAITINGROOM_OUT_SAME,
													new Object[] { CommonResource.getRoomList().get(0).getPersonList(),
															pageInfo(null, result), CommonResource.getMaxRoomNum() }));
											u.getOos().flush();
											u.getOos().reset();

										} catch (IOException d) {
										}
									} else {
										try {
											// WAITINGROOM_OUT_DEFE = 401번

											u.getOos()
													.writeObject(new SendData(Response.WAITINGROOM_OUT_DEFE,
															new Object[] {
																	CommonResource.getRoomList().get(0).getPersonList(),
																	CommonResource.getMaxRoomNum() })); // 대기실
											u.getOos().flush();
											u.getOos().reset(); // 리스트
											// 보냄.
											// 401번
										} catch (IOException d) {
										}
									}
								}

							} else {
								try {
									user.getOos().writeObject(new SendData(Response.ERROR_ROOM_FULL)); // 프로토콜 1005
																										// (방이가득 차서
																										// 입장불가)
									user.getOos().flush();
									user.getOos().reset();
								} catch (IOException e) {
								}
							}
						} else {
							try {
								user.getOos().writeObject(new SendData(Response.ERROR_INFO_NULL)); // 프로토콜 1003 (방이 없어짐)
								user.getOos().flush();
								user.getOos().reset();
							} catch (IOException e) {
							}
						}
						break;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

					case Request.INVITATION_REFUSE: // 초대 거절
						// Request 올 때 초대한 사람 닉네임을 보내줘야 함(Request 프로토콜 스프레드 시트 수정해야 함)
						String inviterName = (String) received.getDataList()[0];
						User inviter = null;
						for (User temp : CommonResource.getUserList()) {
							if (temp.getName().equals(inviterName)) {
								inviter = temp;
							}
						}
						if (inviter == null) {
							user.getOos().writeObject(new SendData(Response.ERROR_INFO_NULL)); // 프로토콜 1003 (초대자 정보가
							user.getOos().flush();
							user.getOos().reset(); // 사라짐) , 이거 방 정보 사라짐이랑
							// 구분할 필요 없는지?
						} else {
							try {
								inviter.getOos().writeObject(
										new SendData(Response.INVITE_FAILED, new Object[] { user.getName() })); // 프로토콜
																												// 970
																												// (초대
																												// 거절)
								inviter.getOos().flush();
								inviter.getOos().reset();
							} catch (IOException e) {
							}
						}
						break;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

					case Request.CHATTINGROOM_BACKTO_WAITINGROOM: // 채팅방에서 대기실로 가기/ 퇴장

						// 방ID
						int requst_room_ID = -1;
						for (User u : CommonResource.getUserList()) {
							if (u.getName().equals(user.getName())) {
								requst_room_ID = u.getRoomID();
								break;
							}
						}

						// 해당 방 찾기
						ChattingRoom chatRoom = null;
						for (Room room : CommonResource.getRoomList()) {
							if (room.getRoomId() == requst_room_ID) {
								chatRoom = (ChattingRoom) room;
								break;
							}
						}

						if (chatRoom != null) {
							Vector<String> person_list = chatRoom.getPersonList();
							Room waitRoom = CommonResource.getRoomList().get(0);
							waitRoom.getPersonList().add(user.getName());// 대기실 인원 추가
							chatRoom.getPersonList().remove(user.getName());

							for (User temp : CommonResource.getUserList()) {
								if (temp.getName().equals(user.getName())) {
									temp.setPage(1);
									temp.setRoomID(0);
								}
							}

							if (person_list.size() < 1) {
								// 사람 나가고 방이 사라짐
								CommonResource.getRoomList().remove(chatRoom);// 해당 방이 삭제

								// roomList
								Vector<RoomInfo> roomMade = new Vector<>();
								for (int i = 1; i < CommonResource.getRoomList().size() && i <= 5; i++) {
									ChattingRoom room = (ChattingRoom) CommonResource.getRoomList().get(i);
									RoomInfo roomInfo = new RoomInfo(room.getRoomId(), room.getRoomName(),
											room.getPersonList().size(), room.getPersonNum_MAX(), room.getPw() != 0);
									roomMade.add(roomInfo);
								}

								Vector<String> waitRoom_personList = waitRoom.getPersonList();
								LobbyDTO lobby = new LobbyDTO(roomMade, waitRoom_personList);

								// 101
								user.getOos().writeObject(new SendData(Response.CONNECTOKED,
										new Object[] { lobby, CommonResource.getMaxRoomNum() }));
								user.getOos().flush();
								user.getOos().reset();
//								int nn=CommonResource.getRoomList().size()-1;
//								user.getOos().writeObject(new SendData(Response.CONNECTOKED,new Object[] {lobby,nn}));

								// 대기실 인원에게 사람 리스트 보냄 + 방 리스트 갱신 982
								// waitUser.getOos().writeObject(new SendData(100, new Object[] { waitNameList
								// })); // 인원 이름 보냄
								for (User userAll : CommonResource.getUserList()) {
									for (String hu : waitRoom.getPersonList()) {
										if (!userAll.getName().equals(user.getName()) && userAll.getName().equals(hu)) {
											Vector<RoomInfo> pageInfo = new Vector<>();
											int page = userAll.getPage();
											getCurrentPageList(pageInfo, page);
											userAll.getOos()
													.writeObject(new SendData(Response.CHATROOM_OUT_WATROOMCLIENT,
															new Object[] { waitRoom.getPersonList(), pageInfo,
																	CommonResource.getMaxRoomNum() })); // 인원 이름 보냄
											userAll.getOos().flush();
											userAll.getOos().reset();
										}
									}
								}

							} else {
								// 방장 유지하는 경우

								// roomList
								Vector<RoomInfo> roomMade = new Vector<>();
								for (int i = 1; i < CommonResource.getRoomList().size() && i <= 5; i++) {
									ChattingRoom room = (ChattingRoom) CommonResource.getRoomList().get(i);
									RoomInfo roomInfo = new RoomInfo(room.getRoomId(), room.getRoomName(),
											room.getPersonList().size(), room.getPersonNum_MAX(), room.getPw() != 0);
									roomMade.add(roomInfo);
								}

								Vector<String> waitRoom_personList = waitRoom.getPersonList();
								LobbyDTO lobby = new LobbyDTO(roomMade, waitRoom_personList);

								// 101
//								int nn=CommonResource.getRoomList().size()-1;
//								user.getOos().writeObject(new SendData(Response.CONNECTOKED,new Object[] {lobby,nn}));		

								user.getOos().writeObject(new SendData(Response.CONNECTOKED,
										new Object[] { lobby, CommonResource.getMaxRoomNum() }));
								user.getOos().flush();
								user.getOos().reset();
								// 980 같은 페이지의 대기실 인원에게
								for (User userAll : CommonResource.getUserList()) {
									for (String hu : waitRoom.getPersonList()) {
										if ((!userAll.getName().equals(user.getName())) && userAll.getName().equals(hu)
												&& userAll.getPage() == 1) {
											Vector<RoomInfo> pageInfo = new Vector<>();
											int page = userAll.getPage();
											getCurrentPageList(pageInfo, 1);
											userAll.getOos().writeObject(new SendData(Response.CHATROOM_OUT_SAME,
													new Object[] { waitRoom.getPersonList(), pageInfo }));
											userAll.getOos().flush();
											userAll.getOos().reset();
										}
									}
								}

								// 981 100 대기실 인원에게 사람 리스트 보냄 방 유지하는 경우, 대기실에 다른페이지의 사람에게
								for (User userAll : CommonResource.getUserList()) {
									for (String hu : waitRoom.getPersonList()) {
										if ((!userAll.getName().equals(user.getName())) && userAll.getName().equals(hu)
												&& userAll.getPage() != 1) {
											userAll.getOos().writeObject(new SendData(Response.CONNECTOK,
													new Object[] { waitRoom.getPersonList() }));
											userAll.getOos().flush();
											userAll.getOos().reset();
										}
									}
								}

								// 983 CHATROOM_OUT_CHATROOMCLIENT = 983 대화방 인원에게
								for (User hu : CommonResource.getUserList()) {
									for (String chatperson : person_list) {
										if (hu.getName().equals(chatperson)) {
											hu.getOos().writeObject(new SendData(Response.CHATROOM_OUT_CHATROOMCLIENT,
													new Object[] { person_list }));
											hu.getOos().flush();
											hu.getOos().reset();
										}
									}
								}

							}
						}

						break;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

					case Request.EXIT: // 대기실 프로그램 종료
						CommonResource.getUserList().remove(user);// 자신을 삭제
						for (Room temp : CommonResource.getRoomList()) { // 룸 객체에서 자기 이름 지움
							if (temp.getRoomId() == 0) {
								temp.getPersonList().remove(user.getName());
							}
						}
						Vector<User> userlist = new Vector<User>();
						Vector<String> us = new Vector<String>();

						for (String strUser : CommonResource.getRoomList().get(0).getPersonList()) {
							us.add(strUser);
							for (User temp : CommonResource.getUserList()) {
								if (temp.getName().equals(strUser)) {
									userlist.add(temp);
								}
							}
						}

						for (User waitUser : userlist) {
							waitUser.getOos()
									.writeObject(new SendData(Response.EXIT, new Object[] { us, user.getName() }));
							waitUser.getOos().flush();
							waitUser.getOos().reset();
						}

						break;

					default:
//						System.out.println("잘못된 요청");
					}
				} catch (NullPointerException e) {
					oos.writeObject(new SendData(1003)); // 해당 정보를 찾을 수 없음
					oos.flush();
					oos.reset();
				} catch (Exception e) {
					e.printStackTrace();
					oos.writeObject(new SendData(1002)); // 기타 다른 문제로 인한 에러
					CommonResource.getUserList().remove(user); // 공용자원에 유저 삭제
					oos.flush();
					oos.reset();
				}
			}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		} catch (SocketException s) {// 강제 중료

//			System.out.println(user.getName() + " 님의 연결이 끊겼습니다.");

			if (user.getRoomID() == 0) { // 대기실에서 강제종료
				Vector<User> users = new Vector<User>();
				Vector<String> personList = new Vector<String>();
				CommonResource.getUserList().remove(user); // 자신의 기록 지움
				searchID(0).getPersonList().remove(user.getName()); // 룸 객체에서 자기 이름 지움
				for (User temp : CommonResource.getUserList()) {
					if (temp.getRoomID() == 0) {
						users.add(temp);
						personList.add(temp.getName());
					} // 대기실 인원 구함
				}
				for (User waitUser : users) {
					try {
//EXIT_FORCED_WAITINGROOM = 991번   	
						waitUser.getOos().writeObject(new SendData(991, new Object[] { personList })); // 인원 이름 보냄. 991번
						waitUser.getOos().flush();
						waitUser.getOos().reset();
					} catch (IOException d) {
					}
				}
			} else { // 대화방에서 강제종료
				int roomNum = user.getRoomID(); // 방번호를 가져옴
				Vector<ObjectOutputStream> ooss = new Vector<ObjectOutputStream>();
				CommonResource.getUserList().remove(user); // 자신의 기록 지움
				searchID(roomNum).getPersonList().remove(user.getName()); // 룸 객체에서 자기 이름 지움
				Vector<String> personList = searchID(roomNum).getPersonList();
				for (User temp : CommonResource.getUserList()) {
					if (temp.getRoomID() == roomNum) {
						ooss.add(temp.getOos());
					}
				} // 같은 방번호를 가진 유저의 oos를 모음

				Vector<User> users = new Vector<User>();
				Vector<String> waitPersonList = new Vector<String>();
				for (User temp : CommonResource.getUserList()) {
					if (temp.getRoomID() == 0) {
						users.add(temp);
						waitPersonList.add(temp.getName());
					} // 대기실 인원 구함
				}
				if (personList.size() != 0) { // 같은 방번호를 가진 유저가 존재함
					for (ObjectOutputStream oosChat : ooss) {
						try {
//EXIT_FORCED_CHATTINGROOM = 1100번   
							oosChat.writeObject(new SendData(1100, new Object[] { user.getName(), personList })); // 인원
							oosChat.flush();
							oosChat.reset();
							// 이름
							// 보냄.
							// 1100번
						} catch (IOException d) {
						}
					}
					for (User waitUser : users) {
						int result = CommonResource.getRoomList().indexOf(searchID(user.getRoomID())) / 5 + 1;
						if (waitUser.getPage() == result) { // 룸 id로 그 룸의 페이지를 알아냄
							try {
//EXIT_FORCED_WAITROOM_SAME = 1102번    		
								waitUser.getOos().writeObject(
										new SendData(1102, new Object[] { pageInfo(null, waitUser.getPage()) })); // 룸
								waitUser.getOos().flush();
								waitUser.getOos().reset();
								// 리스트
								// 보냄.
								// 1102번
							} catch (IOException d) {
							}
						}
					}
				} else { // 같은 방번호를 가진 유저가 존재하지 않음
					CommonResource.getRoomList().remove(searchID(roomNum)); // 해당 방 리스트에서 삭제
					for (User waitUser : users) {
						try {
//EXIT_FORCED_ROOM_DEL = 1101번    
							waitUser.getOos().writeObject(new SendData(1101, new Object[] {
									pageInfo(null, waitUser.getPage()), CommonResource.getMaxRoomNum() })); // 룸 리스트
							waitUser.getOos().flush();
							waitUser.getOos().reset();
							// 보냄.
							// 1101번
						} catch (IOException d) {
						}
					}
				}
			}
		} catch (IOException | ClassNotFoundException e) {
		} finally {	
			try {
				oos.close();
			} catch (Exception e) {
			}
			

		}
	}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private Vector<RoomInfo> pageInfo(String inputInfo, int page) { // 페이지 구함
		// 전체 방

		Vector<Room> roomList = new Vector<Room>();
		if (inputInfo == null) {
			roomList = CommonResource.getRoomList();
		} else {
			for (Room room : CommonResource.getRoomList()) {
				if (room.getRoomName().contains(inputInfo)) {
					roomList.add(room);
				}
			}
		}
		// 리턴용

		Vector<RoomInfo> inPage = new Vector<RoomInfo>();
		for (int i = (page - 1) * 5 + 1; i <= page * 5; i++) {
			if (i < roomList.size()) {
				ChattingRoom chattingRoom = (ChattingRoom) roomList.get(i);
				if (chattingRoom.getPw() == 0) {
					// 비밀번호가 없으면(0일 때)
					RoomInfo roomInfo = new RoomInfo(chattingRoom.getRoomId(), chattingRoom.getRoomName(),
							chattingRoom.getPersonList().size(), chattingRoom.getPersonNum_MAX(), false // 비밀방 아님
					);
					inPage.add(roomInfo);
				} else {
					// 비밀번호가 있으면(0이 아닐 때)
					RoomInfo roomInfo = new RoomInfo(chattingRoom.getRoomId(), chattingRoom.getRoomName(),
							chattingRoom.getPersonList().size(), chattingRoom.getPersonNum_MAX(), true // 비밀방
					);
					inPage.add(roomInfo);
				}
			}
		}
		return inPage;
	}

	private Room searchID(int roomID) { // roomID로 room객체 구함
		Room room = null;
		for (Room temp : CommonResource.getRoomList()) {
			if (temp.getRoomId() == roomID) {
				room = temp;
				return room;
			}
		}
		return room;
	}

	private void getCurrentPageList(Vector<RoomInfo> pageInfo, int page) {
		for (int i = (page - 1) * 5 + 1; i < CommonResource.getRoomList().size() && i <= 5 * page; i++) {
			ChattingRoom room = (ChattingRoom) CommonResource.getRoomList().get(i);
			RoomInfo roomInfo = new RoomInfo(room.getRoomId(), room.getRoomName(), room.getPersonList().size(),
					room.getPersonNum_MAX(), room.getPw() != 0);
			pageInfo.add(roomInfo);
		}
	}

}