import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TrafficFilter {

    private final int MAX_REQUESTS = 100;
    private final long WINDOW = 60_000;

    private final ConcurrentHashMap<String, ConcurrentLinkedQueue<Long>> requests =
            new ConcurrentHashMap<>();

    public boolean allow(String apiKey) {
        long now = System.currentTimeMillis();
        requests.putIfAbsent(apiKey, new ConcurrentLinkedQueue<>());
        ConcurrentLinkedQueue<Long> queue = requests.get(apiKey);

        synchronized (queue) {
            while (!queue.isEmpty() && (now - queue.peek()) > WINDOW) {
                queue.poll();
            }

            if (queue.size() < MAX_REQUESTS) {
                queue.add(now);
                return true;
            } else {
                return false;
            }
        }
    }
     public static void main(String[] args) {

    }
}
