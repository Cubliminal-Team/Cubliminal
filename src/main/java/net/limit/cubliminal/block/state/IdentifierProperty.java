package net.limit.cubliminal.block.state;

import net.limit.cubliminal.Cubliminal;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Used to encode a finite list of identifiers as a block state property because for some reason colons are not supported.
 */

public final class IdentifierProperty extends Property<String> {

    private static final String splitter = "__";
    private final Supplier<Collection<Identifier>> idSupplier;
    private List<String> values = List.of();

    private IdentifierProperty(String name, Supplier<Collection<Identifier>> idSupplier) {
        super(name, String.class);
        this.idSupplier = idSupplier;
    }

    public static IdentifierProperty of(String name, Supplier<Collection<Identifier>> idSupplier) {
        if (idSupplier.get().size() > 16) {
            Cubliminal.LOGGER.warn("It is recommended to not exceed the blockstate count of 16");
        }
        return new IdentifierProperty(name, idSupplier);
    }

    public static String encode(Identifier id) {
        return id.getNamespace() + splitter + id.getPath();
    }

    public static Identifier decode(String id) {
        String decoded = id.replaceFirst(splitter, ":");
        return Identifier.of(decoded);
    }

    private void sortValues() {
        this.values = idSupplier.get()
                .stream()
                .sorted(Comparator.comparing(Identifier::toString))
                .map(IdentifierProperty::encode)
                .toList();
    }

    @Override
    public List<String> getValues() {
        if (this.values.isEmpty()) {
            this.sortValues();
        }
        return this.values;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else {
            return object instanceof IdentifierProperty identifierProperty
                    && super.equals(object)
                    && this.values.equals(identifierProperty.values);
        }
    }

    @Override
    public int computeHashCode() {
        int i = super.computeHashCode();
        return 31 * i + this.values.hashCode();
    }

    @Override
    public String name(String value) {
        return value;
    }

    @Override
    public Optional<String> parse(String name) {
        return this.getValues().contains(name) ? Optional.of(name) : Optional.empty();
    }

    @Override
    public int ordinal(String value) {
        return this.getValues().indexOf(value);
    }
}