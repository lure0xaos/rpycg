package gargoyle.fx

import gargoyle.fx.log.FxLog
import java.io.Closeable
import java.io.IOException
import java.util.concurrent.CopyOnWriteArraySet
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

class FxRegistry : Closeable {
    private val beans: MutableSet<FxBeanInfo<*, *>> = CopyOnWriteArraySet()

    override fun close(): Unit =
        with(beans) {
            forEach { it.close() }
            clear()
        }

    fun <T : Any> getBean(name: String, type: KClass<T>): T =
        findBeanInfo<T, T> { it.isDeclared(type) && it.isNamed(name) }?.let { it() } ?: error("no bean $name")

    fun <T : Any> getBean(type: KClass<T>): T =
        findBeanInfo<T, T> { it.isDeclared(type) }?.let { it() } ?: error("no bean ${type.qualifiedName}")

    fun <D : Any, I : D> register(name: String, declaration: KClass<D>, implementation: KClass<I>): Unit =
        FxBeanInfo(declaration, implementation, name)
            .let { if (it !in beans) beans += it else error("$name already registered") }

    fun <D : Any, I : D> register(
        name: String,
        declaration: KClass<D>,
        implementation: KClass<I>,
        existingBean: I
    ): Unit =
        FxBeanInfo(declaration, implementation, name, lazy { existingBean })
            .let { if (it !in beans) beans += it else error("$name already registered") }

    override fun toString(): String =
        "FXRegistry{beans=$beans}"

    @Suppress("UNCHECKED_CAST")
    private fun <D : Any, I : D> findBeanInfo(predicate: (FxBeanInfo<*, *>) -> Boolean): FxBeanInfo<D, I>? =
        beans.filter(predicate).map { (it as FxBeanInfo<D, I>) }.firstOrNull()

    internal class FxBeanInfo<D : Any, I : D>(
        private val declaration: KClass<D>,
        private val implementation: KClass<I>,
        private val name: String = declaration.qualifiedName!!,
        private var instance: Lazy<I> = lazy { FxIntUtil.newInstance(implementation) }
    ) : Comparable<FxBeanInfo<D, I>>, () -> I, Closeable {
        private val lock = Any()
        private val instantiated
            get() = instance.isInitialized()

        override fun close() {
            synchronized(lock) {
                if (instantiated && instance.value is Closeable)
                    try {
                        (instance.value as Closeable).close()
                    } catch (e: IOException) {
                        FxLog.error("$this close", e)
                    }
            }
        }

        override fun compareTo(other: FxBeanInfo<D, I>): Int =
            name.compareTo(other.name)

        override fun invoke(): I =
            synchronized(lock) { instance.value }

        override fun hashCode(): Int =
            name.hashCode()

        override fun equals(other: Any?): Boolean =
            this === other || null != other && javaClass == other.javaClass && name == (other as FxBeanInfo<*, *>).name

        override fun toString(): String =
            "FXBeanDef{name=$name, instantiated=$instantiated, declaration=$declaration, implementation=$implementation}"

        fun <T : Any> isDeclared(type: KClass<T>): Boolean =
            type.isSubclassOf(declaration)

        fun isNamed(otherName: String): Boolean =
            otherName == name
    }
}
