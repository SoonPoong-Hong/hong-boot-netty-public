package rocklike.boot.netty.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author 홍순풍(rocklike@gmail.com)
 */
public class ChatRcvMainProcessor {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private ChatRcvMainProcessor () {}
	public static final ChatRcvMainProcessor INSTANCE = new ChatRcvMainProcessor();

	public void process(Client client, JsonMsgEntity entity){
		EventType eventType = EventType.from(entity);
		ClientCoordinator coordinator = ClientCoordinator.INSTANCE;
		String roomId = entity.extractFromHeader("roomId");
		switch (eventType) {
		case LogIn:
			coordinator.login(client);
			coordinator.sendAllClientListToOne(client);
			coordinator.sendAllRoomListToOne(client);
			break;
		case LogOut:
			coordinator.logout(client);
			break;
		case AllUserList:
			coordinator.sendAllClientListToOne(client);
			break;
		case CreateRoom:
			coordinator.createRoom(client, roomId);
			break;
		case EnterToRoom:
			coordinator.enterToRoom(client, roomId);
			break;
		case ExitFromRoom:
			coordinator.exitFromRoom(client, roomId);
			break;
		case SendMsg:
			Room room = coordinator.getRoomByRoomid(roomId);
			room.sendMsg(client, entity.msg);
			break;
		default:
			break;
		}
	}
}
