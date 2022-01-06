import lists.CustomList;
import lists.OptimisticList;
import lists.VersionedOptimisticList;
import lists.VersionedOptimisticListV2;
import typesOfThreads.Access;
import typesOfThreads.Add;
import typesOfThreads.Remove;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;

public class Main {
    public static void main(String[] args) {
        System.out.println("Classic");
        CustomList<Integer> optimisticList = new OptimisticList<>();
        test_list(optimisticList, 100_000, 25_000, 25_000, 2, 2, 0);

        System.out.println("Versioned");
        CustomList<Integer> versionedOptimisticList = new VersionedOptimisticList<>();
        test_list(versionedOptimisticList, 100_000, 25_000, 25_000, 2, 2, 0);

        System.out.println("Versioned v2(with atomic integer)");
        CustomList<Integer> versionedOptimisticListV2 = new VersionedOptimisticListV2<>();
        test_list(versionedOptimisticListV2, 100_000, 25_000, 25_000, 2, 2, 0);
    }

    public static void test_list(CustomList<Integer> list, int howManyValuesToAdd, int howManyToRemove, int howManyToAccess, int nAddTh, int nRmTh, int nAccTh) {
        List<Thread> threadList = new LinkedList<>();
        IntStream.range(0, nAddTh)
                .forEach(value -> threadList.add(
                                new Thread(new Add(list, value * howManyValuesToAdd / nAddTh, howManyValuesToAdd / nAddTh))
                        )
                );

        IntStream.range(0, nRmTh).forEach(value -> threadList.add(
                new Thread(new Remove(list, value * howManyToRemove / nRmTh, howManyToRemove / nRmTh))
        ));

        IntStream.range(0, nAccTh).forEach(value -> threadList.add(
                new Thread(new Access(list, value * howManyToAccess / nAccTh, howManyToAccess / nAccTh))
        ));

        int waitToAddThreads = (int) Math.ceil((float) howManyToRemove / ((float) howManyValuesToAdd / (float) nAddTh));

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
