package rocklike.boot.netty.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rocklike.boot.netty.common.CommUtil.SafeRunnable;


/**
 * @author 홍순풍(rocklike@gmail.com)
 */
public class ClientCoordinator implements IClientEventListener{
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	//== 관리해야 하는거
	// CLIENT_TO_ROOMS, ROOMS

	// singleton
	private ClientCoordinator() {
		startMonitorThread();
	}
	// key:Client, value:client가 들어간 room들
	private final ConcurrentHashMap<Client, List<Room>> CLIENT_TO_ROOMS = new ConcurrentHashMap<>();
	public static final ClientCoordinator INSTANCE = new ClientCoordinator();
	private final CopyOnWriteArrayList<Room> ROOMS = new CopyOnWriteArrayList<>();
	// 이것이 true이면, 다른 방으로 들어가면 현재의 방에서는 무조건 나오게 된다.
	private boolean belongsToOnlyOneRoom = false;

	private final ClientEventSupport clientEventSupport = new ClientEventSupport();
	public static final String DEFAULT_ROOM_ID = "";

	public ClientCoordinator login(Client c){
		ChannelSendHelper.writeAndFlushToClient(c, new JsonMsgEntity.Builder(c).setAction(EventType.LoginConfirmed).build());
		CLIENT_TO_ROOMS.put(c, new ArrayList<>(2));
		c.getChannel().closeFuture().addListener(f->logout(c));
		JsonMsgEntity entity = new JsonMsgEntity.Builder(c).setAction(EventType.LogIn).build();
		ChannelSendHelper.writeAndFlushToClients(Collections.list(CLIENT_TO_ROOMS.keys()), entity);

		sendAllClientListToOne(c);

		return this;
	}


	public ClientCoordinator logout(Client c){
		logger.warn("== logout : {}", c);
		if(c==null){
			// 로그인도 하기 전에 끊어진 경우
			return this;
		}
		if(!CLIENT_TO_ROOMS.containsKey(c)){
			logger.warn("== 이미 logout 함 : {}", c);
			return this;
		}
		List<Room> roomsOfClient = CLIENT_TO_ROOMS.get(c);
		CLIENT_TO_ROOMS.remove(c);
		c.validateRoom( roomsOfClient.stream().map(room->room.getId()).collect(Collectors.toList()));
		c.removeAllRooms();

		roomsOfClient.stream().forEach(room->{
			room.exitFromRoom(c).logout(c);
		});


//		sendAllRoomListToAll();
//		sendAllClientListToAll(c);
		JsonMsgEntity entity = new JsonMsgEntity.Builder(c).setAction(EventType.LogOut).build();
		ChannelSendHelper.writeAndFlushToClients(Collections.list(CLIENT_TO_ROOMS.keys()), entity);

		return this;
	}


	public ClientCoordinator invalidateClient(Client c){
		logger.error("== invalid client : {}\n{}" , c); //, CommUtil.toString(Thread.currentThread().getStackTrace()));
		Objects.requireNonNull(c);
		CLIENT_TO_ROOMS.remove(c);
		return this;
	}


	public ClientCoordinator sendAllClientListToOne(Client client){
		List<Map<String, String>> list = Collections.list(CLIENT_TO_ROOMS.keys()).stream().map(c-> {
			Map<String,String> map = new HashMap<>();
			map.put("id", c.getId());
			map.put("name", c.getName());
			return map;
		}).collect(Collectors.toList());
		String jsonStr = JacksonHelper.toJsonStr(list);
		JsonMsgEntity entity = new JsonMsgEntity.Builder(client).setAction(EventType.AllUserList).setContents(jsonStr).build();
		ChannelSendHelper.writeAndFlushToClient(client, entity);
		return this;
	}

	public ClientCoordinator sendAllClientListToAll(Client client){
		List<Map<String, String>> list = Collections.list(CLIENT_TO_ROOMS.keys()).stream().map(c-> {
			Map<String,String> map = new HashMap<>();
			map.put("id", c.getId());
			map.put("name", c.getName());
			return map;
		}).collect(Collectors.toList());
		String jsonStr = JacksonHelper.toJsonStr(list);
		JsonMsgEntity entity = new JsonMsgEntity.Builder(client).setAction(EventType.AllUserList).setContents(jsonStr).build();
		ChannelSendHelper.writeAndFlushToClients(Collections.list(CLIENT_TO_ROOMS.keys()), entity);
		return this;
	}


	public ClientCoordinator createRoom(Client c, String roomId){
		Objects.requireNonNull(c);
		Objects.requireNonNull(roomId);
		Optional<Room> optional = ROOMS.stream().filter(r->roomId.equals(r.getId())).findFirst();
		Room room;
		if(optional.isPresent()){
			logger.error("== 동일한 ID의 방이 이미 존재함.");
			room = optional.get();
		}else{
			room = new Room(roomId, roomId, this);
			ROOMS.add(room);
		}
		List<Room> oldRooms = CLIENT_TO_ROOMS.get(c);
		if(oldRooms==null || oldRooms.size()==0){
			List<Room> newRooms = new ArrayList<>();
			newRooms.add(room);
			CLIENT_TO_ROOMS.put(c, newRooms);
			c.addRoom(roomId);
		}else{
			oldRooms.add(room);
			c.addRoom(roomId);
		}
		room.enterToRoom(c);
		// 자기가 만든 방을 확인시켜줌
		JsonMsgEntity entity = new JsonMsgEntity.Builder(c).setHeader("roomId", roomId).setAction(EventType.CreateRoom).build();
		ChannelSendHelper.writeAndFlushToClient(c, entity);
		// 방이 만들어졌으므로 방 목록을 보냄
		sendAllRoomListToAll();
		return this;
	}


	public ClientCoordinator invalidateRoom(Room r){
		Objects.requireNonNull(r);
		ROOMS.remove(r);
		sendAllRoomListToAll();
//		Collections.list(CLIENT_TO_ROOMS.keys()).stream().forEach(client->sendAllRoomListToOne(client));
		return this;
	}

	public ClientCoordinator enterToRoom(Client c, String roomId){
		Optional<Room> opt = ROOMS.stream().filter(r->r.getId().equals(roomId)).findFirst();
		if(opt.isPresent()){
			Room room = opt.get();
			room.enterToRoom(c);
		}
		return this;
	}

	public ClientCoordinator enterToRoom(Client c, Room room){
		room.enterToRoom(c);
		return this;
	}

	public ClientCoordinator exitFromRoom(Client c, Room room){
		room.exitFromRoom(c);
		return this;
	}
	public ClientCoordinator exitFromRoom(Client c, String roomId){
		Optional<Room> opt = ROOMS.stream().filter(r->r.getId().equals(roomId)).findFirst();
		if(opt.isPresent()){
			Room room = opt.get();
			room.exitFromRoom(c);
			room.sendClientList();
		}

		return this;
	}

	public List<Room> getAllRoomList(){
		List<Room> list = ROOMS.stream().collect(Collectors.toList());
		return list;
	}

	public ClientCoordinator sendAllRoomListToOne(Client client){
		List<Map<String, String>> rooms = getAllRoomList().stream().map(r->r.toMap()).collect(Collectors.toList());
		String jsonStr = JacksonHelper.toJsonStr(rooms);
		JsonMsgEntity entity = new JsonMsgEntity.Builder().setAction(EventType.RoomList).setContents(jsonStr).build();
		ChannelSendHelper.writeAndFlushToClient(client, entity);
		return this;
	}

	public ClientCoordinator sendAllRoomListToAll(){
		List<Map<String, String>> rooms = getAllRoomList().stream().map(r->r.toMap()).collect(Collectors.toList());
		String jsonStr = JacksonHelper.toJsonStr(rooms);
		JsonMsgEntity entity = new JsonMsgEntity.Builder().setAction(EventType.RoomList).setContents(jsonStr).build();
		ChannelSendHelper.writeAndFlushToClients(Collections.list(CLIENT_TO_ROOMS.keys()), entity);
		return this;
	}

	public List<Room> getRooms(List<String> roomIds){
		if(roomIds==null){
			return Collections.emptyList();
		}
		List<Room> list = ROOMS.stream().filter(thisRoom->roomIds.contains(thisRoom.getId())).collect(Collectors.toList());
		return list;
	};

	public List<Room> getRooms(String clientId){
		return CLIENT_TO_ROOMS.get(clientId);
	}

	public Room getRoomByRoomid(String roomId){
		Optional<Room> findFirst = ROOMS.stream().filter(r->r.getId().equals(roomId)).findFirst();
		if(findFirst.isPresent()){
			return findFirst.get();
		}else{
			return null;
		}
	}

	public boolean isBelongsToOnlyOneRoom() {
		return belongsToOnlyOneRoom;
	}

	public void sendMsgInTheRoom(Room r, Client c,String msg){
		r.sendMsg(c, msg);
	}


	public ClientCoordinator setBelongsToOnlyOneRoom(boolean belongsToOnlyOneRoom) {
		this.belongsToOnlyOneRoom = belongsToOnlyOneRoom;
		return this;
	}

	public ClientCoordinator addListener(IClientEventListener l) {
		clientEventSupport.addListener(l);
		return this;
	}

	public ClientCoordinator removeListener(IClientEventListener l) {
		clientEventSupport.removeListener(l);
		return this;
	}

	@Override
	public void listen(ClientEvent e) {
		EventType eventType = e.eventType;
	}

	private void run(SafeRunnable r){
		try {
			r.run();
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
		}
	}


	private void startMonitorThread(){
		try {
			new Thread(){
				@Override
				public void run() {
					while(true){
						try {
							logger.info("== client 리스트");
							Collections.list(CLIENT_TO_ROOMS.keys()).stream().forEach(c->{
								logger.info("{}", c);
							});
						} catch (Exception e) {
							logger.error(e.getMessage(), e);
						} finally {
							CommUtil.sleep(5*60);
						}
					}
				}
			}.start();

		} catch (Exception e) {
			logger.error("[무시]", e);
		}
	}

}
