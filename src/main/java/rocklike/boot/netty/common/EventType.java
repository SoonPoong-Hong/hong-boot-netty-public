package rocklike.boot.netty.common;

import java.util.Objects;

/**
 * @author 홍순풍(rocklike@gmail.com)
 */
public enum EventType{
	LoginConfirmed("LoginConfirmed", "로그인 확인됨"),
	LogIn("LogIn", "로그인"),
	LogOut("LogOut", "로그 아웃"),
	CreateRoom("CreateRoom", "방 새로 만들기"),
	EnterToRoom("EnterToRoom", "방 입장"),
	EnterToDefaultRoom("EnterToDefaultRoom", "기본방 입장"),
	ExitFromRoom("ExitFromRoom", "방 퇴장"),
	RoomList("RoomList", "방 목록"),
	SendMsg("SendMsg", "메시지 전송"),
	SendInfo("SendInfo", "정보 메시지 전송"),
	UserList("UserList", "방 사람들 목록"),
	AllUserList("AllUserList", "모든 사람들 목록"),
	HealthCheck("HealthCheck", "헬스 체크"),
	Invalid("Invalid", "잘못된 코드")
	;

	public final String code;
	public final String desc;

	private EventType(String code, String desc) {
		this.code = code;
		this.desc = desc;
	}


	public static EventType from(String c){
		if(c==null || c.equals("")){
			return Invalid;
		}
		for(EventType t : EventType.values()){
			if(c.equals(t.code)){
				return t;
			}
		}
		throw new IllegalArgumentException("EventType code가 맞지 않음 ("+c+")");
	}


	public static EventType from(JsonMsgEntity c){
		if(c==null){
			return Invalid;
		}
		String action = c.action;
		if(action==null || action.equals("")){
			return Invalid;
		}
		return from(action);
	}


	public static void check(String c){
		Objects.requireNonNull(c, "EventType의 code는 null이면 안됨");
		for(EventType t : EventType.values()){
			if(c.equals(t.code)){
				return ;
			}
		}
		throw new IllegalArgumentException("EventType code가 맞지 않음 ("+c+")");
	}

}