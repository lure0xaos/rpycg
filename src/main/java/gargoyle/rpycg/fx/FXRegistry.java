package gargoyle.rpycg.fx;

import java.io.Closeable;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Set;
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
                .collect(FXIntUtil.toSingleton(() -> new FXException(MessageFormat.format("no bean {0}", name))));
    }

    public <T> T getBean(final Class<T> type) {
        return beanDefs.stream()
                .filter(beanDef -> beanDef.isDeclared(type))
                .map(beanDef -> ((FXBeanDef<T, T>) beanDef).get())
                .collect(FXIntUtil.toSingleton(() -> new FXException("no bean " + type.getName())));
    }

    public <D, I extends D> void register(final String name,
                                          final Class<D> declarationClass, final Class<I> implementationClass) {
        final boolean added = beanDefs.add(new FXBeanDef<>(name, declarationClass, implementationClass, null));
        if (!added) throw new FXException(MessageFormat.format("{0} already registered", name));
    }

    public <D, I extends D> void register(final String name,
                                          final Class<D> declarationClass, final Class<? extends I> implementationClass,
                                          final I existingBean) {
        final boolean added = beanDefs.add(new FXBeanDef<>(name, declarationClass, implementationClass, existingBean));
        if (!added) throw new FXException(MessageFormat.format("{0} already registered", name));
    }

    @Override
    public String toString() {
        return MessageFormat.format("FXRegistry'{'defs={0}'}'", beanDefs);
    }
}
