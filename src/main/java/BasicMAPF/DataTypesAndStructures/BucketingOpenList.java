package BasicMAPF.DataTypesAndStructures;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public class BucketingOpenList<E> implements I_OpenList<E> {
    private static class Bucket<E> implements Comparable<Bucket<E>> {
        private static final int LIST_TO_MAP_THRESHOLD = 8;
        final int key;
        List<E> elementsList;
        Map<E,E> elementsMap;
        OpenListTree<E> elementsTree;
        final BucketingComparator<E> comparator;

        Bucket(int key, BucketingComparator<E> comparator) {
            this.key = key;
            this.elementsList = new ArrayList<>(LIST_TO_MAP_THRESHOLD);
            this.elementsMap = null;
            this.elementsTree = null;
            this.comparator = comparator;
        }

        void upgradeToMapIfNeeded() {
            if (elementsMap == null && elementsList.size() >= LIST_TO_MAP_THRESHOLD) {
                elementsMap = new HashMap<>(elementsList.size() * 2);
                for (E e : elementsList) {
                    elementsMap.put(e, e);
                }
                elementsList = null;  // Free the list
            }
        }

        void initializeTreeIfNeeded() {
            if (elementsTree == null) {
                // If we now have a map, use it to initialize the tree
                if (elementsMap != null) {
                    elementsTree = new OpenListTree<>(comparator, elementsMap);
                    elementsMap = null;  // Free the map
                } else {
                    // We still have a list, create tree directly from it
                    elementsTree = new OpenListTree<>(comparator);
                    elementsTree.addAll(elementsList);
                    elementsList = null;  // Free the list
                }
            }
        }

        E peek() {
            initializeTreeIfNeeded();
            return elementsTree.peek();
        }

        E poll() {
            initializeTreeIfNeeded();
            return elementsTree.poll();
        }

        boolean add(E e) {
            if (elementsTree != null) {
                return elementsTree.add(e);
            }
            if (elementsMap != null) {
                return elementsMap.putIfAbsent(e, e) == null;
            }
            if (!elementsList.contains(e)) {
                elementsList.add(e);
                upgradeToMapIfNeeded();
                return true;
            }
            return false;
        }

        boolean remove(E e) {
            if (elementsTree != null) {
                return elementsTree.remove(e);
            }
            if (elementsMap != null) {
                return elementsMap.remove(e) != null;
            }
            return elementsList.remove(e);
        }

        boolean contains(E e) {
            if (elementsTree != null) {
                return elementsTree.contains(e);
            }
            if (elementsMap != null) {
                return elementsMap.containsKey(e);
            }
            return elementsList.contains(e);
        }

        Iterator<E> iterator() {
            if (elementsTree != null) {
                return elementsTree.iterator();
            }
            if (elementsMap != null) {
                return elementsMap.values().iterator();
            }
            return elementsList.iterator();
        }

        boolean isEmpty() {
            if (elementsTree != null) {
                return elementsTree.isEmpty();
            }
            if (elementsMap != null) {
                return elementsMap.isEmpty();
            }
            return elementsList.isEmpty();
        }

        @Override
        public int compareTo(@NotNull Bucket<E> other) {
            return Integer.compare(this.key, other.key);
        }
    }

    private final PriorityQueue<Bucket<E>> bucketsQueue;
    private final Map<Integer, Bucket<E>> bucketsDict;
    private final BucketingComparator<E> comparator;
    private int size;

    public BucketingOpenList(@NotNull BucketingComparator<E> comparator) {
        this.comparator = comparator;
        this.bucketsQueue = new PriorityQueue<>();
        this.bucketsDict = new HashMap<>();
        this.size = 0;
    }

    private Bucket<E> getBucketForElement(E e, boolean computeIfAbsent) {
        int bucketKey = comparator.getBucket(e);
        Bucket<E> res = bucketsDict.get(bucketKey);
        if (res == null && computeIfAbsent) {
            Bucket<E> newBucket = new Bucket<>(bucketKey, comparator);
            res = newBucket;
            bucketsQueue.add(newBucket);
            bucketsDict.put(bucketKey, res);
        }
        return res;
    }

    private Bucket<E> getMinBucket() {
        while (!bucketsQueue.isEmpty()) {
            Bucket<E> minBucket = bucketsQueue.peek();
            if (!minBucket.isEmpty()) {
                return minBucket;
            }
            bucketsQueue.poll();
            bucketsDict.remove(minBucket.key);
        }
        return null;
    }

    @Override
    public E get(E e) {
        if (e == null) return null;
        Bucket<E> bucket = getBucketForElement(e, false);
        if (bucket == null) return null;
        return bucket.contains(e) ? e : null;
    }

    @Override
    public boolean add(E e) {
        if (e == null) return false;

        Bucket<E> bucket = getBucketForElement(e, true);
        if (bucket.add(e)) {
            size++;
            return true;
        }
        return false;
    }

    @Override
    public boolean remove(Object o) {
        if (o == null) return false;

        @SuppressWarnings("unchecked")
        E e = (E) o;
        Bucket<E> bucket = getBucketForElement(e, false);
        if (bucket != null && bucket.remove(e)) {
            size--;
            return true;
        }
        return false;
    }

    @Override
    public E poll() {
        Bucket<E> minBucket = getMinBucket();
        if (minBucket == null) return null;

        E e = minBucket.poll();
        if (e != null) {
            size--;
        }
        return e;
    }

    @Override
    public E peek() {
        Bucket<E> minBucket = getMinBucket();
        return minBucket != null ? minBucket.peek() : null;
    }

    @Override
    public E keepOne(E e1, E e2, Comparator<E> criteria) {
        int compared = criteria.compare(e1, e2);
        if (compared == 0) {
            boolean containsE1 = contains(e1);
            boolean containsE2 = contains(e2);
            if (containsE1 && !containsE2) {
                return e2;
            } else if (containsE2 && !containsE1) {
                return e1;
            } else if (!containsE1) { // and !contains(e2)
                this.add(e1);
                return e2;
            }
        }
        // if they are not equal, or it contains both
        E keepElem = compared <= 0 ? e1 : e2;
        E discardElem = keepElem == e1 ? e2 : e1;
        return this.replace(discardElem, keepElem);
    }

    private E replace(E e1, E e2) {
        boolean removed = this.remove(e1);
        this.add(e2);
        return removed ? e1 : null;
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
    public boolean contains(Object o) {
        if (o == null) return false;

        @SuppressWarnings("unchecked")
        E e = (E) o;
        Bucket<E> bucket = getBucketForElement(e, false);
        return bucket != null && bucket.contains(e);
    }

    @Override
    public @NotNull Iterator<E> iterator() {
        return new Iterator<E>() {
            private final Iterator<Bucket<E>> bucketsIterator = new ArrayList<>(bucketsQueue).iterator();
            private Iterator<E> currentBucketIterator = null;

            private boolean advance() {
                while ((currentBucketIterator == null || !currentBucketIterator.hasNext())
                        && bucketsIterator.hasNext()) {
                    Bucket<E> currentBucket = bucketsIterator.next();
                    currentBucketIterator = currentBucket.iterator();
                }
                return currentBucketIterator != null && currentBucketIterator.hasNext();
            }

            @Override
            public boolean hasNext() {
                return advance();
            }

            @Override
            public E next() {
                if (!hasNext()) throw new NoSuchElementException();
                return currentBucketIterator.next();
            }
        };
    }

    @Override
    public Object @NotNull [] toArray() {
        ArrayList<E> list = new ArrayList<>(this);
        return list.toArray();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T @NotNull [] toArray(@NotNull T @NotNull [] a) {
        ArrayList<E> list = new ArrayList<>(this);
        if (a.length < list.size()) {
            return (T[]) Arrays.copyOf(list.toArray(), list.size(), a.getClass());
        }
        System.arraycopy(list.toArray(), 0, a, 0, list.size());
        if (a.length > list.size()) {
            a[list.size()] = null;
        }
        return a;
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        for (Object o : c) {
            if (!contains(o)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends E> c) {
        boolean changed = false;
        for (E e : c) {
            changed |= add(e);
        }
        return changed;
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        boolean changed = false;
        for (Object o : c) {
            changed |= remove(o);
        }
        return changed;
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        boolean changed = false;
        ArrayList<E> toRemove = new ArrayList<>();

        for (E e : this) {
            if (!c.contains(e)) {
                toRemove.add(e);
                changed = true;
            }
        }

        toRemove.forEach(this::remove);
        return changed;
    }

    @Override
    public void clear() {
        bucketsQueue.clear();
        bucketsDict.clear();
        size = 0;
    }

    @Override
    public boolean offer(E e) {
        return add(e);
    }

    @Override
    public E remove() {
        E e = poll();
        if (e == null) {
            throw new NoSuchElementException();
        }
        return e;
    }

    @Override
    public E element() {
        E e = peek();
        if (e == null) {
            throw new NoSuchElementException();
        }
        return e;
    }
}