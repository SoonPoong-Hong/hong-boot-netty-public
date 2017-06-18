package rocklike.boot.netty.common;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author 홍순풍(rocklike@gmail.com)
 */
public class Client {
	private Logger logger = LoggerFactory.getLogger(getClass());

	private LocalDateTime createdTime = LocalDateTime.now();
	private final String id;
	private final String name;
	private final Channel channel;
	private List<String> rooms = new ArrayList<>(3);

	public Client(String id, String name, Channel channel) {
		super();
		this.id = id;
		this.name = name;
		this.channel = channel;
	}
	public LocalDateTime getCreatedTime() {
		return createdTime;
	}
	public String getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	public Channel getChannel() {
		return channel;
	}
	public synchronized Client addRoom(String roomId){
		boolean match = rooms.stream().anyMatch(r->r.equals(roomId));
		if(!match){
			rooms.add(roomId);
		}
		return this;
	}

	public synchronized Client removeRoom(String roomId){
		rooms.removeIf(s->s.equals(roomId));
		return this;
	}

	public synchronized Client removeAllRooms(){
		this.rooms.clear();
		return this;
	}

	public List<String> getRooms(){
		return this.rooms;
	}

	public static Client from(ChannelHandlerContext ctx){
		return AttachHelper.about(ctx).getClient();
	}

	public static Client from(Channel channel){
		return AttachHelper.about(channel).getClient();
	}

	public Client attachToChannel(Channel c){
		AttachHelper.about(c).attachLoginUserInfo(this);
		return this;
	}

	public Client validateRoom(List<String> coordinateRoom){
		int length = rooms.size();
		int length2 = coordinateRoom.size();
		if(length!=length2){
			logger.error("=== validateRoom ERROR : client room : {}, cooordiante room : {}", rooms, coordinateRoom);
		}
		return this;
	}


	private int length(String[] arr){
		return arr==null ? 0 : arr.length;
	}

	public Map<String, String> toMap(){
		Map<String,String> map = new HashMap();
		map.put("id", this.id);
		map.put("name", this.name);
		return map;
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
		Client other = (Client) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "Client [id=" + id + ", name=" + name + ", rooms=" + rooms + "]";
	}


}
