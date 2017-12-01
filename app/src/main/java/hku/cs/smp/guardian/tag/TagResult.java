package hku.cs.smp.guardian.tag;

import java.io.Serializable;
import java.util.Map;

public class TagResult implements Serializable {
    private Map<String, Integer> result;

    public TagResult(Map<String, Integer> result) {
        this.result = result;
    }

    public Map<String, Integer> getResult() {
        return result;
    }

    public boolean isEmpty() {
        return result == null || result.isEmpty();
    }
}
