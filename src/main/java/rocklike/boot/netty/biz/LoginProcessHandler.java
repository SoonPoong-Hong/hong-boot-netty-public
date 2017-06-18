package rocklike.boot.netty.biz;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import rocklike.boot.netty.common.AttachHelper;
import rocklike.boot.netty.common.Client;
import rocklike.boot.netty.common.JsonMsgEntity;

/**
 * @author 홍순풍(rocklike@gmail.com)
 */
public class LoginProcessHandler {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

    private static AtomicInteger idGen = new AtomicInteger();

	public Client loginProcess(ChannelHandlerContext ctx, JsonMsgEntity requestEntity){
    	String name = requestEntity.extractFromHeader("name");
    	Objects.requireNonNull(name, "name은 필수");
    	// 기냥 단순 증가하는 id를 발급하고서 통과
    	Client client = new Client(String.valueOf(idGen.incrementAndGet()), name, ctx.channel() );
    	logger.info("== login ({}) ({})", name, ((InetSocketAddress)ctx.channel().remoteAddress()).getAddress().getHostAddress());
    	AttachHelper.about(ctx).attachLoginUserInfo(client);
    	return client;
	}
}
