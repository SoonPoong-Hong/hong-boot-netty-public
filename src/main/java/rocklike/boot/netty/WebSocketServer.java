package rocklike.boot.netty;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * @author 홍순풍(rocklike@gmail.com)
 */
@Component
public class WebSocketServer {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Value("${websocket.port}")
	private int port;

	Channel ch;

	EventLoopGroup bossGroup ;
	EventLoopGroup workerGroup ;

	@PostConstruct
	public void postConstruct(){ //@Value("${websocket.port}")int port){
//		this.port = port;
		new Thread(){
			@Override
			public void run() {
				try {
					startServer();
				} catch (Exception e) {
					logger.error("== failed in start websocket", e);
				}
			}
		}.start();
	}

	@PreDestroy
	public void preDestroy(){
		shutdown();
	}

	public WebSocketServer startServer() throws Exception {
		logger.info("== WebSocketServer start");

		bossGroup = new NioEventLoopGroup(1);
		workerGroup = new NioEventLoopGroup();

			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup)
					.channel(NioServerSocketChannel.class)
					.handler(new LoggingHandler(LogLevel.INFO))
					.childHandler(new WebSocketServerInitializer());

			ch = b.bind(port).sync().channel();
		return this;
	}

	public void shutdown(){
		ch.close().addListener(f->{
			logger.warn("== server closing");
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		});
	}


	public static void main(String[] args) throws Exception {
		WebSocketServer main = new WebSocketServer();
		main.startServer();
	}
}
