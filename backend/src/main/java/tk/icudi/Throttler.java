package tk.icudi;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class Throttler {

	private final static int maxRequestCount = 2;
	private final static int perMillisec = 1000 * 20;

	private List<Long> calles = new ArrayList<>();

	public synchronized String execute(Function<String, String> function, String parameter) {

		calles.removeIf(this::toOld);

		while (calles.size() > maxRequestCount) {
			try {
				System.out.println(" --- throttledRequest because of '" + calles.size() + "' requests in " + (System.currentTimeMillis() -  calles.get(0)) + "msec ");
				Thread.sleep(perMillisec / 2);
				calles.removeIf(this::toOld);
			} catch (InterruptedException e) {
				throw new RuntimeException("could not wait", e);
			}
		}

		calles.add(System.currentTimeMillis());
		return function.apply(parameter);
	}

	private boolean toOld(Long time) {
		return System.currentTimeMillis() > (time + perMillisec);
	}
}
