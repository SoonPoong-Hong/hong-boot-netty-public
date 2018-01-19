package rocklike.boot.netty.common;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * @author 홍순풍(rocklike@gmail.com)
 */
public class Room {
	private final String id;
	private final String name;
	private final CopyOnWriteArrayList<Client> clients = new CopyOnWriteArrayList<>();
	// private final ClientEventSupport clientEventSupport;
	private final ClientCoordinator clientCoordinator;

	public Room(String id, String name, ClientCoordinator clientCoordinator) {
		this.id = id;
		this.name = name;
		// this.clientEventSupport = clientEventSupport;
		this.clientCoordinator = clientCoordinator;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public List<Client> getClients() {
		return clients;
	}

	public Room enterToRoom(Client client) {
		Objects.requireNonNull(client);
		if (clientCoordinator.isBelongsToOnlyOneRoom()) {
			List<Room> rooms = clientCoordinator.getRooms(client.getRooms());
			rooms.forEach(room -> {
				room.exitFromRoom(client);
			});
		}
		if (client != null && !clients.contains(client)) {
			synchronized (this) {
				clients.add(client);
				client.addRoom(this.id);
			}
			JsonMsgEntity jsonMsgEntity = new JsonMsgEntity.Builder(client).setRoomId(this.id).setAction(EventType.EnterToRoom).build();
			ChannelSendHelper.writeAndFlushToClients(clients, jsonMsgEntity);
			sendClientList();
		}
		return this;
	}

	public Room exitFromRoom(Client client) {
		Objects.requireNonNull(client);
		if (client != null && this.clients.contains(client)) {
			JsonMsgEntity jsonMsgEntity = new JsonMsgEntity.Builder(client).setAction(EventType.ExitFromRoom).setRoomId(this.id).build();
			ChannelSendHelper.writeAndFlushToClients(clients, jsonMsgEntity);
		}
		synchronized (this) {
			clients.remove(client);
			client.removeRoom(this.getId());
		}
		if (this.clients.size() == 0) {
			EmptyRoomMgr.INSTANCE.add(this);
		}
		sendClientList();
		return this;
	}

	public Room logout(Client client) {
		synchronized (this) {
			if (client != null && this.clients.contains(client)) {
				clients.remove(client);
				exitFromRoom(client);
			}
		}
		return this;
	}

	public Map<String, String> toMap() {
		Map<String, String> map = new HashMap<>();
		map.put("id", id);
		map.put("name", name);
		return map;
	}

	public void sendMsg(Client c, String msg) {
		JsonMsgEntity entity = new JsonMsgEntity.Builder(c).setAction(EventType.SendMsg).setContents(msg).setRoomId(this.id).build();
		ChannelSendHelper.writeAndFlushToClients(clients, entity);
	}

	public void sendClientList() {
		List<Map<String, String>> list = clients.stream().map(c -> c.toMap()).collect(Collectors.toList());
		String jsonStr = JacksonHelper.toJsonStr(list);
		JsonMsgEntity entity = new JsonMsgEntity.Builder().setAction(EventType.UserList).setHeader("roomId", this.id).setContents(jsonStr)
				.build();
		ChannelSendHelper.writeAndFlushToClients(this.clients, entity);
	}

	public void sendInfoMsg(Client c, String msg) {
		JsonMsgEntity entity = new JsonMsgEntity.Builder(c).setAction(EventType.SendInfo).setContents(msg).build();
		ChannelSendHelper.writeAndFlushToClients(clients, entity);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Room other = (Room) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Room [id=" + id + ", name=" + name + ", clients=" + clients + "]";
	}

}
