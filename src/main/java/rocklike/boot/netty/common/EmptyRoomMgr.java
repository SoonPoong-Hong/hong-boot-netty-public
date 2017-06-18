package rocklike.boot.netty.common;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author 홍순풍(rocklike@gmail.com)
 */
public class EmptyRoomMgr {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	public static final EmptyRoomMgr INSTANCE = new EmptyRoomMgr();
	private EmptyRoomMgr() {
		new Thread(){
			@Override
			public void run() {
				loop();
			}
		}.start();
	}

	private volatile List<RoomTime> list = new ArrayList<>();

	public synchronized void add(Room r){
		logger.info("== add : {} ", r);
		list.add(new RoomTime(r, LocalDateTime.now()));
	}


	private void loop() {
		while (true) {
			try {
				synchronized(this){
					int size = list.size();
					if(size==0){
						continue;
					}
					for(int i=size-1; i>=0; i--){
						RoomTime thisRoomTime = list.get(i);
						LocalDateTime fourSecondAfter = LocalDateTime.now().plusSeconds(4);
						LocalDateTime thisTime = thisRoomTime.time;
						if(thisTime.compareTo(fourSecondAfter)<0){
							list.remove(i);
							ClientCoordinator.INSTANCE.invalidateRoom(thisRoomTime.r);
						}
					}
				}
			} catch (Throwable e) {
				logger.error(e.toString(), e);
			} finally{
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private class RoomTime{
		Room r;
		LocalDateTime time;

		public RoomTime(Room r, LocalDateTime time) {
			this.r = r;
			this.time = time;
		}
	}

	public static void main(String[] args) {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime plus = now.plusSeconds(3);
		System.out.println(now.compareTo(plus));
		System.out.println(now.compareTo(plus)<0);
	}
}
