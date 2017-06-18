package rocklike.boot.netty.common;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author 홍순풍(rocklike@gmail.com)
 */
public class ClientEventSupport {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private List<IClientEventListener> listeners = new ArrayList<>();

	public ClientEventSupport addListener(IClientEventListener l){
		listeners.add(l);
		return this;
	}

	public ClientEventSupport removeListener(IClientEventListener l){
		listeners.remove(l);
		return this;
	}

	public ClientEventSupport removeAllListener(){
		listeners.clear();
		return this;
	}

	public ClientEventSupport fire(ClientEvent e){
		for(IClientEventListener l : listeners){
			try {
				l.listen(e);
			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);
			}
		}
		return this;
	}

}
