import java.io.ObjectOutputStream;

public class User {

	private String name;
	private ObjectOutputStream oos;
	private int roomID;
	private int page;

	public User(String name, ObjectOutputStream oos) {
		this.name = name;
		this.oos = oos;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ObjectOutputStream getOos() {
		return oos;
	}

	public void setOos(ObjectOutputStream oos) {
		this.oos = oos;
	}

	public int getRoomID() {
		return roomID;
	}

	public void setRoomID(int roomID) {
		this.roomID = roomID;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	@Override
	public String toString() {
		return "User [name=" + name + ", oos=" + oos + ", roomID=" + roomID + ", page=" + page + "]";
	}

}
