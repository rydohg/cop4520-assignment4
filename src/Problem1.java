import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

public class Problem1 {
    /* This code only works sometimes and I've spent days trying to debug it
     * while also dealing with major stuff with my senior design project. Have mercy on me
     *
     * I tried to do this with a fine grained locking for better efficiency but this is probably where
     * my problems are. I could have done coarse grained locking easily but I thought it would not be efficient
     * enough for the efficiency requirement but this code turned out to not work so it would have been better
     * to lose out on points for efficiency
     */
    public static void main(String[] args) {
        FineLinkedList list = new FineLinkedList();
        // Create unordered list of 500,000 gifts
        ArrayList<Integer> integers = new ArrayList<>(500000);
        for (int i = 0; i < 20; i++) {
            integers.add(i);
        }
        Collections.shuffle(integers);

        // Run 2 operations for each gift, one to add and one to remove
        ExecutorService executor = Executors.newFixedThreadPool(4);
        ArrayList<Callable<Integer>> giftTasks = new ArrayList<>();
        for (int i = 0; i < integers.size() * 2; i++) {
            int giftId = integers.get(i % integers.size());
            giftTasks.add(() -> {
                // Count thank you's written
                boolean thankYou = servant(list, giftId);
                if(thankYou){
                    return 1;
                } else {
                    return 0;
                }
            });
        }

        try {
            // Add tasks to executor to dispatch threads
            List<Future<Integer>> results = new ArrayList<>();
            for (Callable<Integer> result: giftTasks) {
                results.add(executor.submit(result));
            }
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
            // Compile results after threads end
            int sum = 0;
            for (Future<Integer> result: results) {
                try {
                    sum += result.get();
                } catch (ExecutionException e) {
                    sum += 0;
                }
            }
            System.out.println("Thank you messages: " + sum);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean servant(FineLinkedList list, int giftTag){
        // Search for a node and if it is not there add it to the ordered list
        try {
            System.out.println("Searching for " + giftTag);
            if (list.search(giftTag)){
                System.out.println("Found! Removing");
                list.remove(giftTag);
                return true;
            } else {
                System.out.println("Not found. Adding");
                list.add(giftTag);
                return false;
            }
        } catch (Exception t){
            t.printStackTrace();
            return false;
        }
    }

    public static void printList(FineLinkedList list){
        Node current = list.head;
        while (current != null){
            System.out.print(current.key + " ");
            current = current.next;
        }
        System.out.println();
    }

    // Method that tests the individual methods of the FineLinkedList
    public static void testList(){
        ExecutorService executor = Executors.newFixedThreadPool(2);
        FineLinkedList list = new FineLinkedList();
        List<Callable<Void>> tasks = new ArrayList<>();
        for (int i = 0; i < 11; i++) {
            int finalI = i;
            tasks.add(() -> {
                list.add(finalI);
//                System.out.println("Removed " + (finalI));
                return null;
            });
        }
        try {
            List<Future<Void>> results = executor.invokeAll(tasks);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        executor.shutdown();

        printList(list);

        executor = Executors.newFixedThreadPool(2);
        List<Callable<Void>> removeTasks = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            int finalI = i;
            removeTasks.add(() -> {
                list.remove(finalI * 2);
                return null;
            });
        }
        try {
            List<Future<Void>> results = executor.invokeAll(removeTasks);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        executor.shutdown();
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        printList(list);

        executor = Executors.newFixedThreadPool(2);
        List<Callable<Boolean>> search = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            int finalI = i;
            search.add(() -> {
                boolean result = list.search(finalI * 2 - 1);
                System.out.println("Search for " + (finalI * 2 - 1) + " is " + result);
                return result;
            });
        }
        try {
            List<Future<Boolean>> results = executor.invokeAll(search);
            System.out.println(results.get(0));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        executor.shutdown();
    }
}
