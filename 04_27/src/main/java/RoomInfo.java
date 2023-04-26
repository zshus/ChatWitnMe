import java.io.Serializable;

public class RoomInfo implements Serializable {

	private static final long serialVersionUID = 1L;
	private int roomID;
	private String name;
	private int currentNum;
	private int maxNum;
	private boolean hasPw;

	public RoomInfo(int roomID, String name, int currentNum, int maxNum) {
		super();
		this.roomID = roomID;
		this.name = name;
		this.currentNum = currentNum;
		this.maxNum = maxNum;
	}

	public RoomInfo(int roomID, String name, int currentNum, int maxNum, boolean hasPw) {
		super();
		this.roomID = roomID;
		this.name = name;
		this.currentNum = currentNum;
		this.maxNum = maxNum;
		this.hasPw = hasPw;
	}

	public int getRoomID() {
		return roomID;
	}

	public void setRoomID(int roomID) {
		this.roomID = roomID;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getCurrentNum() {
		return currentNum;
	}

	public void setCurrentNum(int currentNum) {
		this.currentNum = currentNum;
	}

	public int getMaxNum() {
		return maxNum;
	}

	public void setMaxNum(int maxNum) {
		this.maxNum = maxNum;
	}

	public boolean isHasPw() {
		return hasPw;
	}

	public void setHasPw(boolean hasPw) {
		this.hasPw = hasPw;
	}

	@Override
	public String toString() {
		return "RoomInfo [roomID=" + roomID + ", name=" + name + ", currentNum=" + currentNum + ", maxNum=" + maxNum
				+ ", pw=" + hasPw + "]";
	}

}
