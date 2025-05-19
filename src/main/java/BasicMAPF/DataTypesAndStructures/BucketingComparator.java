package BasicMAPF.DataTypesAndStructures;

import java.util.Comparator;

/**
 * A {@link Comparator} that also groups elements into high-level buckets. The buckets should be disjoint, and represented
 * by unique integer keys. The buckets should be ordered such that if the bucket of element 1 is smaller than the bucket of element 2, then element 1 is also smaller than element 2.
 * For performance reasons, the buckets should be large. If there are many small buckets (containing few elements),
 * then the performance of the bucketing might be worse than with no bucketing at all.
 * @param <T> the type of elements to compare
 */
public interface BucketingComparator<T> extends Comparator<T> {
    /**
     * Returns the bucket of the element.
     * @param t the element
     * @return the bucket of the element.
     */
    int getBucket(T t);

    /**
     * Compares the buckets of the two elements.
     * @param o1 the first element
     * @param o2 the second element
     * @return the result of comparing the buckets of the two elements.
     */
    default int bucketCompare(T o1, T o2){return Integer.compare(getBucket(o1), getBucket(o2));}
}
