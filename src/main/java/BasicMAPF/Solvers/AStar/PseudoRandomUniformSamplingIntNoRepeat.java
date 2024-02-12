package BasicMAPF.Solvers.AStar;

import java.util.Random;

/**
 * A random number generator that generates a random number between 0 and a given number, without repeating the same number.
 * To avoid generating an unnecessary large array, the array is generated in chunks of 1000 numbers.
 * The result isn't quite unbiased, but the relation between numbers will be similar to sampling uniformly without repetitions.
 */
public class PseudoRandomUniformSamplingIntNoRepeat {
    private final Random rnd;
    private final int[] numbers;
    private final int bottomStart = 0; // nicetohave: this can be negative for more range
    private final int chunkSize = 1000;
    private int doneIterations;
    private final int stepSize;
    private int nextIndex;

    public PseudoRandomUniformSamplingIntNoRepeat(Random rnd) {
        this.rnd = rnd;
        this.numbers = new int[chunkSize];
        // almost the maximum int value
        int topStart = 2000000000; // size of the range bottom to top should be divisible by chunkSize
//        if ((topStart - bottomStart) % chunkSize != 0){
//            throw new IllegalStateException("The range of numbers must be divisible by the chunk size");
//        }
        stepSize = (topStart - bottomStart) / chunkSize;
        generateNewChunk();
    }

    public int next() {
        if (nextIndex == numbers.length) {
            generateNewChunk();
        }
        return numbers[nextIndex++];
    }

    private void generateNewChunk() {
        for (int i = 0; i < chunkSize; i++) {
            // nicetohave: better to increment on even iterations and decrement on odd iterations, to make this less biased
            int next = bottomStart + (i * stepSize) + doneIterations;
            if (next == Integer.MAX_VALUE) {
                throw new IllegalStateException("Ran out of numbers");
            }
            numbers[i] =next;
        }
        shuffleArray(numbers, rnd);

        doneIterations++;
        nextIndex = 0;
    }

    /**
     * Fisher–Yates shuffle
      */
    public static void shuffleArray(int[] ar, Random rnd) {
        for (int i = ar.length - 1; i > 0; i--)
        {
            int index = rnd.nextInt(i + 1);
            // Simple swap
            int a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }
    }
}
