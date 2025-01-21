package BasicMAPF.DataTypesAndStructures;

import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * An implementation of {@link I_OpenList} that supports PriorityQueue-like operations, using an instance
 * of {@link TreeSet} (instead of {@link PriorityQueue}), while also using a {@link HashMap} to support a O(1)
 * {@link #get(E)} operation. Unlike PriorityQueue, {{@link #poll()}} and {{@link #peek()}} are O(logN) time, but
 * {{@link #keepOne(Object, Object, Comparator)}} has O(logN) time instead of O(N).
 *
 * @param <E>
 */
public class OpenListTree<E> implements I_OpenList<E> {
    private TreeSet<E> queue;
    private Map<E, E> map;
    private final Comparator<? super E> comparator;

    /*  = constructors =  */

    public OpenListTree() {
        this.queue = new TreeSet<>();
        this.map = new HashMap<>();
        this.comparator = null;
    }

    public OpenListTree(@NotNull Comparator<? super E> comparator) {
        this.queue = new TreeSet<>(comparator);
        this.map = new HashMap<>();
        this.comparator = comparator;
    }

    public OpenListTree(Collection<? extends E> c) {
        this.queue = new TreeSet<>(c);
        this.map = new HashMap<>();
        for(E elem : c){
            this.map.put(elem, elem);
        }
        this.comparator = null;
    }

    /*  = interface implementation =  */

    @Override
    public E get(E e) {
        return this.map.get(e);
    }

    private E replace(E e1, E e2) {
        boolean removed = this.remove(e1);
        this.add(e2);
        return removed ? e1 : null;
    }

    @Override
    public E keepOne(E e1, E e2, Comparator<E> criteria) {
        int compared = criteria.compare(e1, e2);
        if(compared == 0){
            boolean containsE1 = this.map.containsKey(e1);
            boolean containsE2 = this.map.containsKey(e2);
            if (containsE1 && !containsE2){
                return e2;
            }
            else if(containsE2 && !containsE1){
                return e1;
            }
            else if (!containsE1){ // and !contains(e2)
                this.add(e1);
                return e2;
            }
        }
        // if they are not equal, or it contains both
        E keepElem = compared <= 0 ? e1 : e2;
        E discardElem = keepElem == e1 ? e2 : e1;
        return this.replace(discardElem, keepElem);
    }
//    @Override
//    public E keepOne(E e1, E e2, Comparator<E> criteria) {
//        int compared = criteria.compare(e1, e2);
//        if(compared == 0){
//            boolean containsE1 = this.map.containsKey(e1);
//            boolean containsE2 = this.map.containsKey(e2);
//            if (containsE1 && !containsE2){
//                return e2;
//            }
//            else if(containsE2 && !containsE1){
//                return e1;
//            }
//            else if (!containsE1){ // and !contains(e2)
//                this.add(e1);
//                return e2;
//            }
//        }
//        // if they are not equal, or it contains both
//        E keepElem = compared <= 0 ? e1 : e2;
//        E discardElem = keepElem == e1 ? e2 : e1;
//        return this.replace(discardElem, keepElem);
//    }

    @Override
    public int size() {
        return this.queue.size();
    }

    @Override
    public boolean isEmpty() {
        return this.queue.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return this.map.containsKey(o);
    }

    /**
     * Doesn't support remove().
     * @return an iterator over this collection.
     */
    @Override
    public Iterator<E> iterator() {
        return new Iterator<E>() {
            Iterator<E> queueIter = queue.iterator();

            @Override
            public boolean hasNext() {
                return queueIter.hasNext();
            }

            @Override
            public E next() {
                return queueIter.next();
            }

        };
    }

    @Override
    public Object[] toArray() {
        return queue.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return queue.toArray(a);
    }

    /**
     * Doesn't allow duplicates. If an element already exists so that e.equals(existing), it will be removed.
     * @param e {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public boolean add(E e) {
        E removed = map.put(e, e);
        if(removed != null && !removed.equals(e)){ // existing equal element was replaced
            queue.remove(removed);
        }
        // todo this doesn't accept multiple nodes that compare to 0.
        //  possible bug when we use an equals() function that isn't consistent with the comparator
        //  means comparators have to either be consistent with equals, or enforce a total order such that
        //  "incomparable" search nodes will still have some tie-breaking
        return queue.add(e);
    }

    @Override
    public boolean remove(Object o) {
        //did the element even exist
        boolean existed = map.remove(o) != null;
        //if it didn't exist, there is no need for a somewhat expensive remove operation on #queue
        if(existed) {queue.remove(o);}
        return existed;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return queue.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        for(E e : c){
            map.put(e, e);
        }
        return queue.addAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean result = false;
        for(Object o : c){
            result |= this.remove(o);
        }
        return result;
    }

    /**
     * Supported, but avoid using this.
     * @throws ClassCastException is c is not a collection of E or an extending class.
     */
    @Override
    public boolean retainAll(Collection<?> c) {
        if(c == null){return false;}
        Map<E, E> newMap = new HashMap<>();
        for(Object o : c){
            E e = ((E)o);
            if(this.map.containsKey(e)) {newMap.put(e, e);}
        }
        this.map = newMap;
        return queue.retainAll(c);
    }

    @Override
    public void clear() {
        if (comparator != null){
            this.queue = new TreeSet<>(comparator);
        }
        else {
            this.queue = new TreeSet<>();
        }

        this.map = new HashMap<>();
    }

    @Override
    public boolean offer(E e) {
        return this.add(e);
    }


    /**
     * This is a dequeue method.
     * @return the "smallest" element as determined by natural ordering.
     * @throws NoSuchElementException {@inheritDoc}
     */
    @Override
    public E remove() {
        E e = queue.pollFirst();
        map.remove(e);
        return e;
    }

    /**
     * This is a dequeue method.
     * @return the "smallest" element as determined by natural ordering.
     */
    @Override
    public E poll() {
        E e = queue.pollFirst();
        map.remove(e);
        return e;
    }

    @Override
    public E element() {
        return queue.first();
    }

    @Override
    public E peek() {
        return queue.first();
    }

}
