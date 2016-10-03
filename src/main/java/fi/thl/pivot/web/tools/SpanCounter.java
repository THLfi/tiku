package fi.thl.pivot.web.tools;

import java.util.Map;

import com.google.common.collect.Maps;

public class SpanCounter {

    private Map<Integer, Integer> spanCounter = Maps.newHashMap();

    public int assign(int level, int span) {
        spanCounter.put(level, span);
        return span;
    }

    public boolean next(int level) {
        if (spanCounter.containsKey(level)) {
            int span = spanCounter.get(level);
            assign(level, span - 1);
            return span - 1 == 0;
        }
        return true;
    }

}
