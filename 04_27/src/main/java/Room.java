import java.io.Serializable;
import java.util.Vector;

public class Room implements Serializable {
	static final long serialVersionUID = 1000000000000000000L;
	private Vector<String> personList;
	private int roomId;
	private String roomName;

	public Room() {
		super();
	}

	public Room(Vector<String> personList, int roomId, String roomName) {
		super();
		this.personList = personList;
		this.roomId = roomId;
		this.roomName = roomName;
	}

	public Vector<String> getPersonList() {
		return personList;
	}

	public void setPersonList(Vector<String> personList) {
		this.personList = personList;
	}

	public int getRoomId() {
		return roomId;
	}

	public void setRoomId(int roomId) {
		this.roomId = roomId;
	}

	public String getRoomName() {
		return roomName;
	}

	public void setRoomName(String roomName) {
		this.roomName = roomName;
	}

	@Override
	public String toString() {
		return "Room [personList=" + personList + ", roomId=" + roomId + ", roomName=" + roomName + "]";
	}
}
