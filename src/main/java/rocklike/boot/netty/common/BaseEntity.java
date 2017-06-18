package rocklike.boot.netty.common;

import io.netty.buffer.ByteBuf;

/**
 * @author 홍순풍(rocklike@gmail.com)
 */
public abstract class BaseEntity<T> {

	// === abstract
	public abstract ByteBuf formatToByteBuf() throws Exception;
}
