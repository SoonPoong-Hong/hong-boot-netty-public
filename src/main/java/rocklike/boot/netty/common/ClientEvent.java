package rocklike.boot.netty.common;

/**
 * @author 홍순풍(rocklike@gmail.com)
 */
public class ClientEvent {
	public final Client client;
	public final String roomId;
	public final EventType eventType;

	public ClientEvent(Client client, String roomId, EventType eventType) {
		super();
		this.client = client;
		this.roomId = roomId;
		this.eventType = eventType;
	}
}
