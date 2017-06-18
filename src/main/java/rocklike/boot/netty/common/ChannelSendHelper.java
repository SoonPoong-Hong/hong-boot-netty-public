package rocklike.boot.netty.common;

import java.util.List;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.ReferenceCountUtil;

/**
 * @author 홍순풍(rocklike@gmail.com)
 */
public class ChannelSendHelper {

	public static void writeAndFlushToClients(List<Client> clients, JsonMsgEntity entity){
		if(clients==null){
			return;
		}
		String str = entity.toStr();
		TextWebSocketFrame frame = new TextWebSocketFrame(str);
		clients.stream().forEach(cl->{
			Channel ch = cl.getChannel();
			if(ch.isActive()){
				ch.writeAndFlush(frame.duplicate().retain());
			}else{
				ClientCoordinator.INSTANCE.invalidateClient(cl);
			}
		});
		ReferenceCountUtil.release(frame);
	}

	public static void writeAndFlushToClient(Client client, JsonMsgEntity entity){
		String str = entity.toStr();
		TextWebSocketFrame frame = new TextWebSocketFrame(str);
		Channel ch = client.getChannel();
		if(ch.isActive()){
			ch.writeAndFlush(frame);
		}else{
			ClientCoordinator.INSTANCE.invalidateClient(client);
		}
	}


}
