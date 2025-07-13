package BasicMAPF.Solvers.MultiAgentAStar;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.concurrent.*;

public class CartesianProductWithTimeout {
    public static <T> List<List<T>> cartesianProductWithTimeout(List<List<T>> lists, long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<List<List<T>>> future = executor.submit(() -> Lists.cartesianProduct(lists));

        try {
            return future.get(timeout, unit);
        } finally {
            future.cancel(true); // Attempt to interrupt the thread
            executor.shutdownNow();
        }
    }
}