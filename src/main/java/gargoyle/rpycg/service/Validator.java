package gargoyle.rpycg.service;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.util.Callback;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class Validator {
    private final Map<Property<?>, Consumer<Set<String>>> listeners = new LinkedHashMap<>(2);
    private final SimpleBooleanProperty valid = new SimpleBooleanProperty(true);
    private final Map<Property<?>, Set<Callback<Object, String>>> validators = new LinkedHashMap<>(2);

    @SuppressWarnings("unchecked")
    public <T> void addValidator(final Property<T> property,
                                 final Callback<T, String> validator, final Consumer<Set<String>> listener) {
        if (!validators.containsKey(property)) {
            listeners.put(property, listener);
            validators.put(property, new HashSet<>(2));
            property.addListener((observable, oldValue, newValue) -> {
                final Set<String> errors = validators.get(property).stream()
                        .map(v -> v.call(property.getValue()))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());
                errors.removeIf(Objects::isNull);
                validate();
                listener.accept(errors);
            });
        }
        validators.get(property).add((Callback<Object, String>) validator);
    }

    public boolean isValid() {
        return valid.getValue();
    }

    public SimpleBooleanProperty validProperty() {
        return valid;
    }

    public Map<Property<?>, Set<String>> validate() {
        final Map<Property<?>, Set<String>> errors = validators.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().stream()
                        .map(predicate -> predicate.call(entry.getKey().getValue()))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet())));
        listeners.forEach((property, listConsumer) -> listConsumer.accept(errors.get(property)));
        errors.entrySet().removeIf(entries -> entries.getValue().isEmpty());
        valid.setValue(errors.isEmpty());
        return errors;
    }
}
