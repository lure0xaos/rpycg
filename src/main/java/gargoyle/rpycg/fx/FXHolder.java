package gargoyle.rpycg.fx;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class FXHolder<T> implements Supplier<T> {

    @NotNull
    private final Supplier<T> supplier;
    @Nullable
    private T data;

    public FXHolder(@NotNull Supplier<T> supplier) {
        this.supplier = supplier;
    }

    @Override
    @NotNull
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
