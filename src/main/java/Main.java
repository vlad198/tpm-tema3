import lists.CustomList;
import lists.OptimisticList;
import lists.VersionedOptimisticList;
import typesOfThreads.Add;
import typesOfThreads.Remove;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;

public class Main {
    public static void main(String[] args) {
        System.out.println("Classic");
        CustomList<Integer> optimisticList = new OptimisticList<>();
        test_list(optimisticList, 100_000, 25_000, 2, 2);

        System.out.println("Versioned");
        CustomList<Integer> versionedOptimisticList = new VersionedOptimisticList<>();
        test_list(versionedOptimisticList, 100_000, 25_000, 2, 2);
    }

    public static void test_list(CustomList<Integer> list, int howManyValuesToAdd, int howManyToRemove, int noAddTh, int noRmTh) {
        List<Thread> threadList = new LinkedList<>();
        IntStream.range(0, noAddTh)
                .forEach(value -> threadList.add(
                                new Thread(new Add(list, value * howManyValuesToAdd / noAddTh, howManyValuesToAdd / noAddTh))
                        )
                );

        IntStream.range(0, noRmTh).forEach(value -> threadList.add(
                new Thread(new Remove(list, value * howManyToRemove / noRmTh, howManyToRemove / noRmTh))
        ));

        int waitToAddThreads = (int) Math.ceil((float) howManyToRemove / ((float) howManyValuesToAdd / (float) noAddTh));

        IntStream.range(0, waitToAddThreads).forEach(i -> threadList.get(i).start());
        IntStream.range(0, waitToAddThreads).forEach(i -> {
            try {
                threadList.get(i).join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        IntStream.range(waitToAddThreads, threadList.size()).forEach(i -> threadList.get(i).start());
        IntStream.range(waitToAddThreads, threadList.size()).forEach(i -> {
            try {
                threadList.get(i).join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }
}
