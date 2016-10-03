package fi.thl.pivot.util;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class CumulativeStopWatch {

    private String currentTask;
    private long currentTaskStart;

    private Map<String, AtomicLong> tasks = new IdentityHashMap<>();

    public void start(String id) {
        currentTask = id;
        currentTaskStart = System.currentTimeMillis();
        if (!tasks.containsKey(id)) {
            tasks.put(id, new AtomicLong());
        }
    }

    public void stop() {
        tasks.get(currentTask).addAndGet(currentTaskStart - System.currentTimeMillis());
    }

    public String prettyPrint() {
        long total = 0;
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        for (Map.Entry<String, AtomicLong> e : tasks.entrySet()) {
            sb.append(String.format("%08d %s\n", e.getValue().longValue(), e.getKey()));
            total += e.getValue().longValue();
        }
        sb.append(String.format("%08d %s\n", total, "total"));
        return sb.toString();
    }
}
