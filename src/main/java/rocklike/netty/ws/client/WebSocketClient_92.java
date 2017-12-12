/*
 * Copyright 2014 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package rocklike.netty.ws.client;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * 장애 받는 웹소켓
 */
public final class WebSocketClient_92 {

	public static void main(String[] args) throws Exception {
		WebSocketClient_92 main = new WebSocketClient_92();
		EventLoopGroup eventGroup = new NioEventLoopGroup();
		main.start(eventGroup);

		Thread eternal = new Thread(()-> {
			try {
				Thread.sleep(24*60*1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		eternal.setDaemon(false);
		eternal.start();
	}

	private Logger logger = LoggerFactory.getLogger(getClass());

	void start(EventLoopGroup eventGroup) throws Exception {
		logger.info("== start..");
		URI uri = new URI("ws://12.4.96.92:59768");
		String scheme = uri.getScheme();
		final String host = uri.getHost();
		final int port;
		port = uri.getPort();

		final WebSocketClientHandler handler = new WebSocketClientHandler(
				WebSocketClientHandshakerFactory.newHandshaker(uri, WebSocketVersion.V13, null, true, new DefaultHttpHeaders()));

		Bootstrap b = new Bootstrap();

		b.group(eventGroup)
		.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 4 * 1000)
		.channel(NioSocketChannel.class)
				.handler(new ChannelInitializer<SocketChannel>() {
					@Override
					protected void initChannel(SocketChannel ch) {
						ChannelPipeline p = ch.pipeline();
						p.addLast(new HttpClientCodec()
								, new HttpObjectAggregator(8192)
								, WebSocketClientCompressionHandler.INSTANCE
								, new IdleStateHandler(50, 0, 0)
								, handler);
					}
				});


		ChannelFuture connectFuture = b.connect(uri.getHost(), port);
		connectFuture.addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				if(!future.isSuccess()) {
					System.err.println("== connect 실패");
					future.cause().printStackTrace();
				}
			}
		});

	}

}
