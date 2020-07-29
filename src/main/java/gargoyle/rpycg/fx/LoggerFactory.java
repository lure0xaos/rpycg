package gargoyle.rpycg.fx;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class LoggerFactory {
    private static final Map<String, Logger> LOGGERS = Collections.synchronizedMap(new HashMap<>());

    private LoggerFactory() {
        throw new IllegalStateException(LoggerFactory.class.getName());
    }

    public static Logger getLogger(Class<?> aClass) {
        return getLogger(aClass.getName());
    }

    public static Logger getLogger(String name) {
        return LOGGERS.computeIfAbsent(name, JULLogger::new);
    }
}
