package rocklike.boot.netty.common;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author 홍순풍(rocklike@gmail.com)
 */
public class CommUtil {
	private static Logger logger = LoggerFactory.getLogger(CommUtil.class);

	public static void runSafely(SafeRunnable r){
		try {
			r.run();
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
		}
	}

	public static interface SafeRunnable {
		public void run() throws Exception;
	}

	public static String toString(StackTraceElement[] arr){
		if(arr==null){
			return "";
		}
		StringBuilder sb = new StringBuilder();
		Arrays.asList(arr).stream().skip(1).forEach(t->sb.append(t.toString()).append("\n"));
		return sb.toString();
	}

	public static void sleep(int seconds){
		try {
			Thread.sleep(seconds*1000);
		} catch (InterruptedException e) {
			logger.warn("[ignore]",  e);
		}
	}

//	public static void main(String[] args) {
//		go();
//	}
//
//	static void go(){
//		go2();
//	}
//
//	private static void go2() {
//		go3();
//	}
//
//	private static void go3() {
//		StackTraceElement[] st = Thread.currentThread().getStackTrace();
//		String str = toString(st);
//		System.out.println(str);
//	}
}
