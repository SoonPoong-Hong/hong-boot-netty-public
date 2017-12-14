/*
 * Copyright 2012 The Netty Project
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
//The MIT License
//
//Copyright (c) 2009 Carl Bystršm
//
//Permission is hereby granted, free of charge, to any person obtaining a copy
//of this software and associated documentation files (the "Software"), to deal
//in the Software without restriction, including without limitation the rights
//to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//copies of the Software, and to permit persons to whom the Software is
//furnished to do so, subject to the following conditions:
//
//The above copyright notice and this permission notice shall be included in
//all copies or substantial portions of the Software.
//
//THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
//THE SOFTWARE.

package rocklike.netty.ws.client;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoop;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;

public class WebSocketClientHandler extends SimpleChannelInboundHandler<Object> {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private LocalDateTime start;
	private LocalDateTime end;
	private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("[HH:mm:ss] ", Locale.KOREA);

	private final WebSocketClientHandshaker handshaker;
	private ChannelPromise handshakeFuture;

//	public static void main(String[] args) {
//		DateTimeFormatter pattern = DateTimeFormatter.ofPattern("HH:mm:ss");
//		String f = LocalDateTime.now().format(pattern);
//		System.out.println(f);
//	}

	public WebSocketClientHandler(WebSocketClientHandshaker handshaker) {
		this.handshaker = handshaker;
	}

	public ChannelFuture handshakeFuture() {
		return handshakeFuture;
	}

	@Override
	public void handlerAdded(ChannelHandlerContext ctx) {
		handshakeFuture = ctx.newPromise();
		handshakeFuture.addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				WebSocketFrame frame = new TextWebSocketFrame("{kind: \"ID\", ID: \"admin\"}");
				ctx.writeAndFlush(frame);
			}
		});
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		start = LocalDateTime.now();
		handshaker.handshake(ctx.channel());
	}

	private String now() {
		String str = LocalDateTime.now().format(formatter);
		return str;
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) {
		end = LocalDateTime.now();
		logger.error(String.format("=== disconnected => 시작:%s, 끝:%s , 걸린시간(분):%s \n", start.format(formatter), end.format(formatter) , ChronoUnit.SECONDS.between(start, end) / 60.0));
//		System.out.printf(now() + "=== disconnected => 시작:%s, 끝:%s , 걸린시간(분):%s \n", start.format(formatter), end.format(formatter) , ChronoUnit.SECONDS.between(start, end) / 60.0);
	}

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (!(evt instanceof IdleStateEvent)) {
            return;
        }

        IdleStateEvent e = (IdleStateEvent) evt;
        if (e.state() == IdleState.READER_IDLE) {
            System.out.println("=== read idle timeout으로 다시 connect함.");
            ctx.close();
        }
    }

	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
		final EventLoop loop = ctx.channel().eventLoop();
		// 끊어지면 1초 있다가 다시 connect
		loop.schedule(() -> {
			logger.error("=== reconnecting..");
			try {
				new WebSocketClient_alert_main().start(loop);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}, 1000, TimeUnit.MILLISECONDS);
	}

	@Override
	public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
		Channel ch = ctx.channel();
		if (!handshaker.isHandshakeComplete()) {
			handshaker.finishHandshake(ch, (FullHttpResponse) msg);
			logger.info("== connected..");
			handshakeFuture.setSuccess();
			return;
		}

		if (msg instanceof FullHttpResponse) {
			FullHttpResponse response = (FullHttpResponse) msg;
			throw new IllegalStateException("Unexpected FullHttpResponse (getStatus=" + response.status() + ", content="
					+ response.content().toString(CharsetUtil.UTF_8) + ')');
		}

		WebSocketFrame frame = (WebSocketFrame) msg;
		if (frame instanceof TextWebSocketFrame) {
			TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;
//			System.out.println(textFrame.text());
			logger.debug(textFrame.text());
//			System.out.println(textFrame.text().substring(0, 3));
		} else if (frame instanceof PongWebSocketFrame) {
			System.out.println("WebSocket Client received pong");
		} else if (frame instanceof CloseWebSocketFrame) {
			System.out.println("WebSocket Client received closing");
			ch.close();
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		if (!handshakeFuture.isDone()) {
			handshakeFuture.setFailure(cause);
		}
		ctx.close();
	}
}
