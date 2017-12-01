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
import java.net.URISyntaxException;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
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
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * 장애 받는 웹소켓
 */
public final class WebSocketClient_92 {

	public static void main(String[] args) throws Exception {
		WebSocketClient_92 main = new WebSocketClient_92();
		EventLoopGroup eventGroup = new NioEventLoopGroup();
		main.start(eventGroup);
		Thread.sleep(24 * 60 * 1000L);
	}

	void start(EventLoopGroup eventGroup) throws Exception {
		URI uri = new URI("ws://12.4.96.92:59768");
		String scheme = uri.getScheme();
		final String host = uri.getHost();
		final int port;
		port = uri.getPort();

		final WebSocketClientHandler handler = new WebSocketClientHandler(
				WebSocketClientHandshakerFactory.newHandshaker(uri, WebSocketVersion.V13, null, true, new DefaultHttpHeaders()));

		Bootstrap b = new Bootstrap();

		b.group(eventGroup).channel(NioSocketChannel.class).option(ChannelOption.SO_KEEPALIVE, true)
				.handler(new ChannelInitializer<SocketChannel>() {
					@Override
					protected void initChannel(SocketChannel ch) {
						ChannelPipeline p = ch.pipeline();
						p.addLast(new HttpClientCodec(), new HttpObjectAggregator(8192), WebSocketClientCompressionHandler.INSTANCE,
								handler);
					}
				});

		Channel ch = b.connect(uri.getHost(), port).sync().channel();
		handler.handshakeFuture().sync();

	}

}
