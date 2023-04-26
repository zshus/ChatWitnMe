import java.io.Serializable;

public class ChattingRoomInfo implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String roomName;
	private int personNum_MAX;
	private int pw;

	public ChattingRoomInfo(String roomName, int personNum_MAX) {
		super();
		this.roomName = roomName;
		this.personNum_MAX = personNum_MAX;
	}

	public ChattingRoomInfo(String roomName, int personNum_MAX, int pw) {
		super();
		this.roomName = roomName;
		this.personNum_MAX = personNum_MAX;
		this.pw = pw;
	}

	public String getRoomName() {
		return roomName;
	}

	public void setRoomName(String roomName) {
		this.roomName = roomName;
	}

	public int getPersonNum_MAX() {
		return personNum_MAX;
	}

	public void setPersonNum_MAX(int personNum_MAX) {
		this.personNum_MAX = personNum_MAX;
	}

	public int getPw() {
		return pw;
	}

	public void setPw(int pw) {
		this.pw = pw;
	}

	@Override
	public String toString() {
		return "ChattingRoomInfo [roomName=" + roomName + ", personNum_MAX=" + personNum_MAX + ", pw=" + pw + "]";
	}

}
