import java.io.Serializable;
import java.util.Vector;

public class ChattingRoom extends Room implements Serializable {
	static final long serialVersionUID = 1000000000000000001L;
	private int personNum_MAX;
	private int pw;

	public ChattingRoom(Vector<String> personList, int roomID, String roomName, int personNum_MAX) {
		super(personList, roomID, roomName);
		this.personNum_MAX = personNum_MAX;
	}

	public ChattingRoom(Vector<String> personList, int roomID, String roomName, int personNum_MAX, int pw) {
		super(personList, roomID, roomName);
		this.personNum_MAX = personNum_MAX;
		this.pw = pw;
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
		return "ChattingRoom [personNum_MAX=" + personNum_MAX + ", pw=" + pw + ", personList= " + super.getPersonList()
				+ "]";
	}

}
