package BasicCBS.Solvers.ConstraintsAndConflicts.ConflictManagement;

import BasicCBS.Solvers.ConstraintsAndConflicts.A_Conflict;

import java.util.Collection;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.SortedSet;

/**
 * Selects a {@link A_Conflict} with the minimum time.
 */
public class MinTimeConflictSelectionStrategy implements ConflictSelectionStrategy {
    @Override
    public A_Conflict selectConflict(Collection<A_Conflict> conflicts) {
        if(conflicts == null || conflicts.isEmpty()) {return null;}

        //if a sorted Collection, assume it is sorted by time, and select the first
        if(conflicts instanceof SortedSet){
            SortedSet<A_Conflict> sortedConflicts = ((SortedSet<A_Conflict>)conflicts);
            return sortedConflicts.first();
        }
        if(conflicts instanceof PriorityQueue){
            PriorityQueue<A_Conflict> sortedConflicts = ((PriorityQueue<A_Conflict>)conflicts);
            return sortedConflicts.peek();
        }

        Iterator<A_Conflict> iter = conflicts.iterator();
        if(!iter.hasNext()){return null;} //might be empty (no conflicts)
        else{
            // find minimum
            A_Conflict minTimeConflict = iter.next();
            while(iter.hasNext()){
                A_Conflict candidate = iter.next();
                if(minTimeConflict.time > candidate.time) {minTimeConflict = candidate;}
            }
            return minTimeConflict;
        }
    }
}
