package BasicMAPF.DataTypesAndStructures;

import java.util.Comparator;
import java.util.Queue;

/**
 * Meant for managing the open list (OPEN) of a search algorithm.
 * Inherits contract from {@link Queue}, and extends it by supporting {@link #get(E)},
 * and {@link #keepOne(Object, Object, Comparator)}.
 * It is also in essence a {@link java.util.Set}, in as much as duplicates are not allowed. However, it doesn't implement
 * the Set interface.
 * @param <E> a class which preferably, but not necessarily, implements the {@link Comparable} interface.
 */
public interface I_OpenList<E> extends Queue<E> {
    /**
     * @param e an element to search for and retrieve. @NotNull
     * @return the element contained in the open list, which is equal to the given element. If there is no such element,
     *          returns null.
     */
    E get(E e);

    /**
     * Keeps the "smaller" element as determined by the given {@link Comparator}. Meaning, if
     * criteria.compare(e1, e2) <= 0 , e1 will be kept, otherwise e2 will be kept.
     * These elements can be equal by other criteria, including their natural ordering and {@link #equals(Object)} method.
     * @param e1         an element. May or may not already be contained in the open list. @NotNull
     * @param e2         an element. May or may not already be contained in the open list. @NotNull
     * @param criteria   a criteria for which element to keep. @NotNull
     * @return the element that wasn't kept.
     */
    E keepOne(E e1, E e2, Comparator<E> criteria);
}
