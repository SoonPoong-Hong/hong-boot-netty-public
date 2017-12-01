package rocklike.netty.ws.client;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class JustTest {

	public static void main(String[] args) throws InterruptedException {
		JustTest main = new JustTest();
		main.test();

	}

	void test() throws InterruptedException {
		LocalDateTime start = LocalDateTime.now();
		Thread.sleep(1110);
		LocalDateTime end = LocalDateTime.now();

		long between = ChronoUnit.MINUTES.between(start, end);
		System.out.println(between);
		between = ChronoUnit.MILLIS.between(start, end);
		System.out.println(between);
	}
}
