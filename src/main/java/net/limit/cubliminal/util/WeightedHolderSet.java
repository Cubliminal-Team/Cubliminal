package net.limit.cubliminal.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import net.minecraft.util.math.random.Random;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A holder object containing a set of values with an assigned weight, so that a random one can be selected according to its weight.
 * @param <T> the object that is stored
 */
public class WeightedHolderSet<T> {

    private final List<Pair<Float, T>> values;
    private double totalWeight = 0;

    public WeightedHolderSet(int initialSize) {
        this.values = new ArrayList<>(initialSize);
    }

    public WeightedHolderSet(Collection<Pair<Float, T>> initValues) {
        this.values = new ArrayList<>(initValues.size());
        this.replace(initValues);
    }

    public static float validateWeight(float weight) {
        if (weight < 0) {
            throw new IllegalArgumentException("Weight must be a number greater than 0");
        }
        return weight;
    }

    public int size() {
        return this.values.size();
    }

    public List<Pair<Float, T>> getValues() {
        return this.values;
    }

    public void add(float weight, T value) {
        validateWeight(weight);
        this.values.add(Pair.of(weight, value));
        this.totalWeight += weight;
    }

    public void remove(T value) {
        this.values.removeIf(pair -> pair.getSecond().equals(value));
    }

    public void clear() {
        this.values.clear();
        this.totalWeight = 0;
    }

    public void replace(Collection<Pair<Float, T>> newValues) {
        this.values.clear();
        this.totalWeight = 0;
        for (Pair<Float, T> pair : newValues) {
            validateWeight(pair.getFirst());
            this.values.add(pair);
            this.totalWeight += pair.getFirst();
        }
    }

    public T random(Random random) {
        if (this.values.isEmpty() || this.totalWeight <= 0) {
            throw new IllegalStateException("No weighted values had been added");
        } else {
            double randomValue = random.nextFloat() * this.totalWeight;
            double cumulative = 0;
            for (Pair<Float, T> pair : this.values) {
                cumulative += pair.getFirst();
                if (cumulative > randomValue) {
                    return pair.getSecond();
                }
            }

            return this.values.getLast().getSecond();
        }
    }

    public <R> WeightedHolderSet<R> map(Function<? super T, ? extends R> mapper) {
        List<Pair<Float, R>> list = new ArrayList<>(this.values.size());
        this.values.forEach(pair -> list.add(Pair.of(pair.getFirst(), mapper.apply(pair.getSecond()))));
        return new WeightedHolderSet<>(list);
    }

    public static <T> Codec<WeightedHolderSet<T>> createCodec(Codec<T> typeCodec) {
        return Codec.pair(Codec.floatRange(0f, Float.MAX_VALUE).fieldOf("weight").codec(), typeCodec)
                .listOf()
                .xmap(WeightedHolderSet::new, set -> set.values);
    }

    public static <T> Codec<WeightedHolderSet<T>> createCodec(Codec<T> typeCodec, String fieldName) {
        return Codec.pair(Codec.floatRange(0f, Float.MAX_VALUE).fieldOf("weight").codec(), typeCodec.fieldOf(fieldName).codec())
                .listOf()
                .xmap(WeightedHolderSet::new, set -> set.values);
    }

    public static <T> Codec<WeightedHolderSet<T>> createHashCodec(Codec<T> typeCodec) {
        return Codec.unboundedMap(typeCodec, Codec.floatRange(0f, Float.MAX_VALUE))
                .xmap(map -> new WeightedHolderSet<>(map.entrySet().stream().map(entry -> Pair.of(entry.getValue(), entry.getKey())).collect(Collectors.toSet())),
                        holderSet -> holderSet.getValues().stream().collect(Collectors.toMap(Pair::getSecond, Pair::getFirst)));
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("Values: ");
        this.values.forEach(pair -> builder.append("Entry: { weight: " + pair.getFirst() + " value: " + pair.getSecond().toString() + " }; "));
        return builder.toString();
    }
}