package gargoyle.fx

import gargoyle.fx.log.FxLog
import java.io.Closeable
import java.io.IOException
import java.util.concurrent.CopyOnWriteArraySet
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

class FxRegistry : Closeable {
    private val beans: MutableCollection<FxBeanInfo<*, *>> = CopyOnWriteArraySet()
    override fun close() {
        beans.forEach { it.close() }
        beans.clear()
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getBean(name: String, type: KClass<T>): T =
        requireNotNull(beans
            .filter { it.isDeclared(type) }
            .filter { it.isNamed(name) }
            .map { (it as FxBeanInfo<T, T>)() }
            .firstOrNull()) { "no bean $name" }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getBean(type: KClass<T>): T =
        requireNotNull(beans
            .filter { beanDef: FxBeanInfo<*, *> -> beanDef.isDeclared(type) }
            .map { beanDef: FxBeanInfo<*, *> -> (beanDef as FxBeanInfo<T, T>)() }
            .firstOrNull()) { "no bean ${type.qualifiedName}" }

    fun <D : Any, I : D> register(name: String, declarationClass: KClass<D>, implementationClass: KClass<I>) {
        if (!beans.add(FxBeanInfo(name, declarationClass, implementationClass)))
            throw FxException(("$name already registered"))
    }

    fun <D : Any, I : D> register(
        name: String,
        declarationClass: KClass<D>,
        implementationClass: KClass<I>,
        existingBean: I
    ) {
        if (!beans.add(FxBeanInfo(name, declarationClass, implementationClass, lazy { existingBean })))
            throw FxException(("$name already registered"))
    }

    override fun toString(): String =
        "FXRegistry{beans=$beans}"

    internal class FxBeanInfo<D : Any, I : D>(
        private val name: String,
        private val declaration: KClass<D>,
        private val implementation: KClass<I>,
        private var instance: Lazy<D> = lazy { FxIntUtil.newInstance(implementation) }
    ) : Comparable<FxBeanInfo<D, I>>, () -> I?, Closeable {
        private val lock = Any()
        private val instantiated
            get() = instance.isInitialized()

        override fun close() {
            synchronized(lock) {
                if (instantiated && instance.value is Closeable)
                    try {
                        (instance.value as Closeable).close()
                    } catch (e: IOException) {
                        FxLog.error(e, "$this close")
                    }
            }
        }

        override fun compareTo(other: FxBeanInfo<D, I>): Int =
            name.compareTo(other.name)

        @Suppress("UNCHECKED_CAST")
        override fun invoke(): I {
            synchronized(lock) {
                return instance.value as I
            }
        }

        override fun hashCode(): Int =
            name.hashCode()

        override fun equals(other: Any?): Boolean =
            this === other || null != other && javaClass == other.javaClass && name == (other as FxBeanInfo<*, *>).name

        override fun toString(): String =
            "FXBeanDef{name=$name, implemented=$instantiated, declarationClass=$declaration, implementationClass=$implementation}"

        fun <T : Any> isDeclared(type: KClass<T>): Boolean =
            type.isSubclassOf(declaration)

        fun isNamed(otherName: String): Boolean =
            otherName == name
    }
}
