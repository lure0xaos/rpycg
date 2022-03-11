package gargoyle.rpycg.service

import javafx.beans.property.Property
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.value.ObservableValue

@Suppress("unused")
class Validator {
    private val listeners: MutableMap<Property<*>, (Set<String>) -> Unit> = LinkedHashMap(2)
    private val valid = SimpleBooleanProperty(true)
    private val validators: MutableMap<Property<*>, MutableSet<(Any) -> String>> = LinkedHashMap(2)

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> addValidator(property: Property<T>, validator: (T) -> String?, listener: (Set<String>) -> Unit) {
        if (!validators.containsKey(property)) {
            listeners[property] = listener
            validators[property] = mutableSetOf()
            property.addListener { _: ObservableValue<out T>, _: T, _: T ->
                val errors = validators[property]!!
                    .map { v: (Any) -> String -> v(property.value!!) }
                    .filter { obj: String? -> null != (obj) }
                validate()
                listener(errors.toSet())
            }
        }
        validators[property]!!.add(validator as (Any) -> String)
    }

    fun isValid(): Boolean = valid.value

    fun validProperty(): SimpleBooleanProperty = valid

    fun validate(): Map<Property<*>, Set<String>> {
        val errors: MutableMap<Property<*>, MutableSet<String>> =
            validators.mapValues { entry: Map.Entry<Property<*>, MutableSet<(Any) -> String>> ->
                entry.value.map { function -> function(entry.key.value) }.toMutableSet()
            }.toMutableMap()
        listeners.forEach { (property: Property<*>, listConsumer: (Set<String>) -> Unit) ->
            listConsumer(errors[property]!!)
        }
        errors.entries.removeIf { (_, value): Map.Entry<Property<*>, Set<String>> -> value.isEmpty() }
        valid.value = errors.isEmpty()
        return errors
    }
}
