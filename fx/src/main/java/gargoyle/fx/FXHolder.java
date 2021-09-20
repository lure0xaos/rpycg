package gargoyle.fx;

import java.util.function.Supplier;

public final class FXHolder<T> implements Supplier<T> {
    private final Object lock = new Object();
    private final Supplier<? extends T> supplier;
    private volatile T data;

    public FXHolder(final Supplier<? extends T> supplier) {
        this.supplier = supplier;
    }

    @Override
    public T get() {
        if (null == data) {
            synchronized (lock) {
                if (null == data) {
                    data = supplier.get();
                }
            }
        }
        return data;
    }

    public void set(final T data) {
        synchronized (lock) {
            this.data = data;
        }
    }
}
