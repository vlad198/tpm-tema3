package typesOfThreads;

import lists.CustomList;
import lombok.AllArgsConstructor;

import java.util.stream.IntStream;

@AllArgsConstructor
public class Access implements Runnable {
    private CustomList<Integer> customList;
    private int start;
    private int howMany;

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        IntStream.range(start, start + howMany).filter(value -> value % 2 == 0).forEach(value -> customList.contains(value));
        System.out.println("Access took " + (System.currentTimeMillis() - startTime) + " milliseconds");
    }
}
