package gargoyle.rpycg.fx;

import java.util.function.Supplier;

public class FXHolder<T> implements Supplier<T> {
    private final Supplier<T> supplier;
    private T data;

    public FXHolder(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    @Override
    public T get() {
        if (data == null) {
            synchronized (this) {
                if (data == null) {
                    data = supplier.get();
                }
            }
        }
        return data;
    }
}
