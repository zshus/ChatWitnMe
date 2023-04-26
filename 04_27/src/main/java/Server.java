
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class Server {
	public static void main(String[] args) {
		try {
			ServerSocket server = new ServerSocket(10001);
//			System.out.println("접속을 기다립니다.");
			CommonResource.getRoomList().add(new Room(new Vector<String>(), 0, "대기실"));
			while (true) {
				Socket sock = server.accept();
				ServerThread serverThread = new ServerThread(sock);
				serverThread.start();
			}
		} catch (Exception e) {
			System.out.println("server main : " + e);
		}
	}
}
