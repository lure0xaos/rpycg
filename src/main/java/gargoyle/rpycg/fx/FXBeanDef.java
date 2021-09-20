package gargoyle.rpycg.fx;

import java.io.Closeable;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Objects;
import java.util.function.Supplier;

final class FXBeanDef<D, I extends D> implements Comparable<FXBeanDef<D, I>>, Supplier<I>, Closeable {

    private final Class<D> declarationClass;
    private final Class<? extends I> implementationClass;
    private final String name;
    private volatile I implementation;
    private volatile boolean implemented;
    private final Object lock = new Object();

    public FXBeanDef(final String name, final Class<D> declarationClass, final Class<? extends I> implementationClass,
                     final I implementation) {
        this.name = name;
        this.declarationClass = declarationClass;
        this.implementationClass = implementationClass;
        this.implementation = implementation;
    }

    @Override
    public void close() {
        synchronized (lock) {
            if (implemented && implementation instanceof Closeable) {
                try {
                    ((Closeable) implementation).close();
                } catch (final IOException e) {
                    throw new FXException(this + " close", e);
                }
            }
        }
    }

    @Override
    public int compareTo(final FXBeanDef<D, I> o) {
        return name.compareTo(o.name);
    }

    @Override
    public I get() {
        synchronized (lock) {
            if (null == implementation) {
                this.implementation = FXIntUtil.newInstance(implementationClass);
                implemented = true;
            }
            return implementation;
        }
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj || null != obj && getClass() == obj.getClass()
                && Objects.equals(name, ((FXBeanDef<?, ?>) obj).name);
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "FXBeanDef'{'name=''{0}'', implemented={1}, declarationClass={2}, implementationClass={3}'}'",
                name, implemented, declarationClass, implementationClass);
    }

    public <T> boolean isDeclared(final Class<T> type) {
        return declarationClass.isAssignableFrom(type);
    }

    public boolean isNamed(final String otherName) {
        return Objects.equals(otherName, name);
    }
}
