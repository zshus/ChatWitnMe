import java.io.Serializable;
import java.util.Arrays;

public class SendData implements Serializable {
	static final long serialVersionUID = 3518731767529258119L;
	private int function;
	private Object[] dataList;

	public int getFuntion() {
		return function;
	}

	public void setFuntion(int funtion) {
		this.function = funtion;
	}

	public Object[] getDataList() {
		return dataList;
	}

	public void setDataList(Object[] dataList) {
		this.dataList = dataList;
	}

	public SendData(int funtion) {
		this.function = funtion;
	}

	public SendData(int funtion, Object[] dataList) {
		this.function = funtion;
		this.dataList = dataList;
	}

	@Override
	public String toString() {
		return "SendData [funtion=" + function + ", dataList=" + Arrays.toString(dataList) + "]";
	}

}
