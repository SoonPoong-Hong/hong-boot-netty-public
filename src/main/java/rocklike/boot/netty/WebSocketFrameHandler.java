package rocklike.boot.netty;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import rocklike.boot.netty.biz.LoginProcessHandler;
import rocklike.boot.netty.common.AttachHelper;
import rocklike.boot.netty.common.ChannelSendHelper;
import rocklike.boot.netty.common.ChatRcvMainProcessor;
import rocklike.boot.netty.common.Client;
import rocklike.boot.netty.common.EventType;
import rocklike.boot.netty.common.JsonMsgEntity;

/**
 * @author 홍순풍(rocklike@gmail.com)
 */
public class WebSocketFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketFrameHandler.class);
    private static AtomicInteger idGen = new AtomicInteger();
    private static LoginProcessHandler loginProcessHandler = new LoginProcessHandler();
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
        if (frame instanceof TextWebSocketFrame) {
            String requestStr = ((TextWebSocketFrame) frame).text();
            logger.info("== received : {}", requestStr);
            JsonMsgEntity requestEntity = JsonMsgEntity.from(requestStr);
            Client client = Client.from(ctx);
            if(client==null && !EventType.LogIn.code.equals(requestEntity.action)){
            	throw new IllegalStateException("login부터 시작해야 함.");
            }

            //== 아직 로그인을 하지 않아서 client가 만들어지지 않은 상태라서, login 과정을 거침
            // (현재는 그냥 단순증가시키는 id를 만들어서 통과시킴)
            if(client==null){
            	client = loginProcessHandler.loginProcess(ctx, requestEntity);
            }

            ChatRcvMainProcessor.INSTANCE.process(client, requestEntity);

        } else {
            String message = "unsupported frame type: " + frame.getClass().getName();
            throw new UnsupportedOperationException(message);
        }
    }



	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		Client client = AttachHelper.about(ctx).getClient();
		logger.warn("== inactive : {}", client);
	}



	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		logger.error(cause.getMessage(), cause);
		Client client = AttachHelper.about(ctx).getClient();
		JsonMsgEntity entity = new JsonMsgEntity.Builder(ctx).setAction(EventType.SendInfo).setContents("[error] " + cause.toString()).build();
		ChannelSendHelper.writeAndFlushToClient(client, entity);
	}


}
