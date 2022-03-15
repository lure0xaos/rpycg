package gargoyle.fx;

import java.io.Closeable;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;

@SuppressWarnings({"rawtypes", "unchecked"})
final class FXRegistry implements Closeable {

    private final Collection<FXBeanDef> beanDefs = new CopyOnWriteArraySet<>();

    public FXRegistry() {
    }

    @Override
    public void close() {
        beanDefs.forEach(FXBeanDef::close);
        beanDefs.clear();
    }

    public <T> T getBean(final String name, final Class<T> type) {
        return beanDefs.stream()
                .filter(beanDef -> beanDef.isDeclared(type))
                .filter(beanDef -> beanDef.isNamed(name))
                .map(beanDef -> ((FXBeanDef<T, T>) beanDef).get())
                .collect(FXUtil.toSingleton(() -> new FXException(FXUtil.format("no bean {name}",
                        Map.of("name", name)))));
    }

    public <T> T getBean(final Class<T> type) {
        return beanDefs.stream()
                .filter(beanDef -> beanDef.isDeclared(type))
                .map(beanDef -> ((FXBeanDef<T, T>) beanDef).get())
                .collect(FXUtil.toSingleton(() -> new FXException("no bean " + type.getName())));
    }

    public <D, I extends D> void register(final String name,
                                          final Class<D> declarationClass, final Class<I> implementationClass) {
        final boolean added = beanDefs.add(new FXBeanDef<>(name, declarationClass, implementationClass, null));
        if (!added) throw new FXException(FXUtil.format("{name} already registered", Map.of("name", name)));
    }

    public <D, I extends D> void register(final String name,
                                          final Class<D> declarationClass, final Class<? extends I> implementationClass,
                                          final I existingBean) {
        final boolean added = beanDefs.add(new FXBeanDef<>(name, declarationClass, implementationClass, existingBean));
        if (!added) throw new FXException(FXUtil.format("{name} already registered", Map.of("name", name)));
    }

    @Override
    public String toString() {
        return FXUtil.format("FXRegistry{defs={array}}", Map.of("array", beanDefs));
    }
}
