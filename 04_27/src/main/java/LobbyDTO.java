import java.io.Serializable;
import java.util.Vector;

public class LobbyDTO implements Serializable {
	static final long serialVersionUID = 1000000000000000002L;
	private Vector<RoomInfo> roomMade;
	private Vector<String> personList;

	public LobbyDTO(Vector<RoomInfo> roomMade, Vector<String> personList) {
		super();
		this.roomMade = roomMade;
		this.personList = personList;
	}

	public Vector<RoomInfo> getRoomMade() {
		return roomMade;
	}

	public void setRoomMade(Vector<RoomInfo> roomMade) {
		this.roomMade = roomMade;
	}

	public Vector<String> getPersonList() {
		return personList;
	}

	public void setPersonList(Vector<String> personList) {
		this.personList = personList;
	}

	@Override
	public String toString() {
		return "LobbyDTO [roomMade=" + roomMade + ", personList=" + personList + "]";
	}

}
