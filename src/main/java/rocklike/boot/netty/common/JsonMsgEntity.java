package rocklike.boot.netty.common;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

/**
 *
 * action
 * headers[key,value]
 * msg
 *
 * (예)
   {
	  "msg" : "잘 지내고 있냐",
	  "action" : "SendMsg",
	  "headers" : {
	    "roomId" : "room_1234"
	  }
	}

 * @author Hong Soon Poong(rocklike@gmail.com)
 */
public class JsonMsgEntity extends BaseEntity<JsonMsgEntity> {
	public String msg;
	public String action;
	public Map<String, String> headers;

	private static final Charset UTF_8 = Charset.forName("UTF-8");

	@Override
	public ByteBuf formatToByteBuf() throws Exception {
		String jsonStr = toStr();
		ByteBuf result = ByteBufUtil.writeUtf8(PooledByteBufAllocator.DEFAULT, jsonStr);
		return result;
	}

	public String extractFromHeader(String key){
		if(headers==null){
			return null;
		}
		return headers.get(key);
	}

	public String toStr() {
		Objects.requireNonNull(action);
		Map<String,Object> obj = new HashMap<>();
		obj.put("action", action);
		obj.put("headers", headers);
		obj.put("msg", msg);
		String jsonStr = JacksonHelper.toJsonStr(obj);
		return jsonStr;
	}


	public static JsonMsgEntity from(String str) {
		JsonMsgEntity entity = JacksonHelper.toObj(str, JsonMsgEntity.class);
		return entity;
	}


	public static class Builder{
		private Client client;

		public Builder(ChannelHandlerContext ctx) {
			client = Client.from(ctx);
		}
		public Builder(Channel channel) {
			client = Client.from(channel);
		}
		public Builder(Client client) {
			this.client = client;
		}
		public Builder() {
			this.client = null;
		}

		private EventType action;
		private String contents;
		private Map<String, String> headers;

		public Builder setAction(EventType action) {
			this.action = action;
			return this;
		}
		public Builder setContents(String contents) {
			this.contents = contents;
			return this;
		}
		public Builder setRefId(String refId) {
			setHeader("refId", refId);
			return this;
		}
		public Builder setRefName(String refName) {
			setHeader("refName", refName);
			return this;
		}
		public Builder setRoomId(String roomId) {
			setHeader("roomId", roomId);
			return this;
		}

		public Builder setRefIdAndName(Client userInfo) {
			setRefId(userInfo.getId());
			setRefName(userInfo.getName());
			return this;
		}

		public Builder setHeader(String key, String value) {
			if(headers==null){
				headers = new HashMap<>();
			}
			headers.put(key, value);
			return this;
		}

		public JsonMsgEntity build(){
			JsonMsgEntity e = new JsonMsgEntity();
			e.msg = contents;
			e.action = action.code;
			if(client!=null){
				setRefId(client.getId());
				setRefName(client.getName());
			}
			e.headers = headers;
			return e;
		}
	}

	public static void main(String[] args) {
		JsonMsgEntity entity = new Builder().setAction(EventType.SendMsg).setRefId("id_12345")
				.setHeader("roomId", "room_1234").setHeader("lets", "go go").setContents("잘 지내고 있냐").build();
		String str = JacksonHelper.toJsonStrFormatted(entity);
		System.out.println(str);
		System.out.println("===============");
		for(int i=0; i> 10; i++){
			System.out.println(i);
		}
	}

}
