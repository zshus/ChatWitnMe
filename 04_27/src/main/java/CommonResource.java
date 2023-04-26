import java.util.Vector;

public class CommonResource {
	private static Vector<User> userList = new Vector<User>();
	private static Vector<Room> roomList = new Vector<Room>();
	private static Vector<Room> temp = new Vector<Room>();
	private static int count = 0;

	public CommonResource() {

	}

	public static synchronized Vector<User> getUserList() {

		return userList;

	}

	public static synchronized Vector<Room> getRoomList() {
		return roomList;

	}

	public static synchronized Vector<Room> getTemp() {
		return temp;

	}

	public static synchronized void makeRoom(ChattingRoomInfo chattingRoomInfo, User user) {

		count++;
		ChattingRoom chattingRoom;
		if (chattingRoomInfo.getPw() != 0) {
			chattingRoom = new ChattingRoom(new Vector<String>(), count, chattingRoomInfo.getRoomName(),
					chattingRoomInfo.getPersonNum_MAX(), chattingRoomInfo.getPw());
		} else {
			chattingRoom = new ChattingRoom(new Vector<String>(), count, chattingRoomInfo.getRoomName(),
					chattingRoomInfo.getPersonNum_MAX());
		}
		chattingRoom.getPersonList().add(user.getName());
		user.setRoomID(count); // roomID 저장
		roomList.add(chattingRoom);
	}

	public static synchronized void removeRoom(Room room) {
		roomList.remove(room);
	}

	public static synchronized String getMaxRoomNum() {
		String maxNum = String.valueOf((roomList.size() - 1) / 5 + 1);
		if((roomList.size()-1)%5==0) {
			maxNum = String.valueOf((roomList.size() - 1) / 5);
		}				
		return maxNum;
	}

	public static synchronized String getMaxRoomNum(int i) {
		String maxNum = String.valueOf((temp.size() - 1) / 5 + 1);
		if((temp.size()-1)%5==0) {
			maxNum = String.valueOf((temp.size() - 1) / 5);
		}		
		return maxNum;
	}

}
