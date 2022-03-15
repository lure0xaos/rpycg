package gargoyle.rpycg.fx;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

final class FXRegistry {

    private final Map<Class<?>, Object> singletons = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<? extends T> type) {
        return (T) singletons.computeIfAbsent(type, FXReflection::instantiate);
    }
}
