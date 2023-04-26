
public class Request {
	public static final int CONNECT = 10;							//	서버에 접속하기
	public static final int ROOM_MSG_SEND	=	20;					//	채팅하기
	public static final int WHISPER_MSG_CLIENT	=	30;				//	귓속말하기
	public static final int CHATROOM_MAKE	=	40;					//	새방 만들기
	public static final int CHANGE_PAGE	=	50;						//	페이지 넘기기
	public static final int SEARCH_ROOM	=	60;						//	방리스트 검색하기
	public static final int SEARCH_PERSONLIST	=	61;				//	인원 검색하기
	public static final int CHATROOM_ENTRY	=	70;					//	일반 대화방에 입장하기
	public static final int PASSWORD_CHATROOM_ENTRYING	=	74;		//	비밀방 입장 요청
	public static final int PASSWORD_CHATROOM_ENTRY	=	75;			//	비밀방에 입장하기
	public static final int PASSWORD_CHATROOM_REQ	=	80;			//	방 비밀번호 요청하기
	public static final int PASSWORD_CHATROOM_REQ_ACCEPT	=	81;	//	방 비밀번호 요청 수락
	public static final int PASSWORD_CHATROOM_REQ_REFUSE	=	82;	//	방 비밀번호 요청 거절
	public static final int ROOM_SETTING_EDIT	=	90;				//	방정보 변경창- 변경
	public static final int EXPULSION_HOST_CLICK	=	91;			//	강퇴,위임 선택
	public static final int DELEGATION_HOST	=	92;					//	방장위임
	public static final int EXPULSION_HOST	=	93;					//	대화방 인원 강퇴
	public static final int INVITE_CLIENTLIST	=	94;				//	대기실 인원 리스트 요청
	public static final int INVITE_CLIENT	=	95;					//	인원 초대
	public static final int INVITATION_ACCEPT	=	96;				//	초대 수락
	public static final int INVITATION_REFUSE	=	97;				//	초대 거절
	public static final int CHATTINGROOM_BACKTO_WAITINGROOM =	98;	//	채팅방에서 대기실로 가기/  퇴장
	public static final int EXIT=	99;								//	대기실 프로그램 종료
	public static final int EXIT_FORCED	=	100;					//	강제 종료
}
