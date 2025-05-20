package BasicMAPF.DataTypesAndStructures;

import Environment.Config;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * ArrayMap is a simple implementation of a Map using an array.
 * It assumes that the keys have unique hash codes in the range [0, capacity).
 */
public class ArrayMap<K, V> implements Map<K, V> {
    private final V[] values;
    private int size = 0;

    /**
     * Constructor for ArrayMap.
     * @param capacity the capacity of the map.
     */
    public ArrayMap(int capacity) {
        values = (V[]) new Object[capacity];
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean containsKey(Object key) {
        if (Config.DEBUG >= 1){
            checkKey((K) key);
        }
        return values[hash((K) key)] != null;
    }

    @Override
    public boolean containsValue(Object value) {
        for (V val : values) {
            if (Objects.equals(val, value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public V get(Object key) {
        if (Config.DEBUG >= 1){
            checkKey((K) key);
        }
        return values[hash((K) key)];
    }

    @Override
    public V put(K key, V value) {
        if (Config.DEBUG >= 1){
            checkKey((K) key);
        }
        int hash = hash(key);
        if (values[hash] == null) {
            size++;
        }
        V oldValue = values[hash];
        values[hash] = value;
        return oldValue;
    }

    @Override
    @SuppressWarnings("unchecked")
    public V remove(Object key) {
        if (Config.DEBUG >= 1){
            checkKey((K) key);
        }
        int hash = hash((K) key);
        V oldValue = values[hash];
        if (oldValue != null) {
            size--;
        }
        values[hash] = null;
        return oldValue;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for (Map.Entry<? extends K, ? extends V> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public V putIfAbsent(K key, V value) {
        checkKey(key);
        int hash = hash(key);
        V oldValue = values[hash];
        if (oldValue == null) {
            values[hash] = value;
            size++;
        }
        return oldValue;
    }

    @Override
    public void clear() {
        Arrays.fill(values, null);
        size = 0;
    }

    @Override
    public @NotNull Set<K> keySet() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public @NotNull Collection<V> values() {
        // return a list of all the non-null values
        return  Arrays.stream(values).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public @NotNull Set<Entry<K, V>> entrySet() {
        throw new UnsupportedOperationException("Not implemented");
    }

    private int hash(K key) {
        return key.hashCode();
    }

    private void checkKey(K key) {
        int hash = hash(key);
        if (hash < 0 || hash >= values.length) {
            throw new IllegalArgumentException("Key's hash code is out of range. Expected in range [0, " + values.length + "), but got " + hash);
        }
    }
}