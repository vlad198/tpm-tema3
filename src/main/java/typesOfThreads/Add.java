package typesOfThreads;

import lists.CustomList;
import lombok.AllArgsConstructor;

import java.util.stream.IntStream;

@AllArgsConstructor
public class Add implements Runnable {
    private CustomList<Integer> customList;
    private int startValue;
    private int howMany;

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        IntStream.range(startValue, startValue + howMany).forEach(value -> customList.add(value));
        System.out.println("Add took " + (System.currentTimeMillis() - startTime) + " milliseconds");
    }
}
