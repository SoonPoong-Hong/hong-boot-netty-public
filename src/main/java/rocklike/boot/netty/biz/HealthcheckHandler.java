package rocklike.boot.netty.biz;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import rocklike.boot.netty.common.ChannelSendHelper;
import rocklike.boot.netty.common.Client;
import rocklike.boot.netty.common.EventType;
import rocklike.boot.netty.common.JsonMsgEntity;

/**
 * 5분동안 input이 없으면, health check의 목적으로 dummy 데이타를 보냄.
 * @author 홍순풍(rocklike@gmail.com)
 */
@Sharable
public class HealthcheckHandler extends ChannelInboundHandlerAdapter {
	private Logger logger = LoggerFactory.getLogger(getClass());

	public static final int READ_CHECK_INTERVAL = 5*60; // 5분
	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if(evt instanceof IdleStateEvent){
			IdleStateEvent e = (IdleStateEvent) evt;
			if(e.state()==IdleState.READER_IDLE){
				Client client = Client.from(ctx);
				if(client!=null){
					logger.info("== health check sending : {}, {}", client.getId(), client.getName());
					ChannelSendHelper.writeAndFlushToClient(client, new JsonMsgEntity.Builder().setAction(EventType.HealthCheck).build());
				}
			}
		}else{
			super.userEventTriggered(ctx, evt);
		}
	}


}
