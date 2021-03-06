package lists;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class VersionedOptimisticListV2<T> implements CustomList<T> {
    /**
     * First list entry
     */
    private final Node head;
    private AtomicInteger version;

    /**
     * Constructor
     */
    public VersionedOptimisticListV2() {
        this.head = new Node(Integer.MIN_VALUE);
        this.head.next = new Node(Integer.MAX_VALUE);
        this.version = new AtomicInteger(0);
    }

    /**
     * Add an element.
     *
     * @param item element to add
     * @return true iff element was not there already
     */
    public boolean add(T item) {
        int key = item.hashCode();
        while (true) {
            Node pred = this.head;
            Node current = pred.next;
            while (current.key <= key) {
                pred = current;
                current = current.next;
            }
            int prev_version = this.version.get();
            pred.lock();
            current.lock();
            try {
                if (validate(pred, current, prev_version)) {
                    if (current.key == key) { // present
                        return false;
                    } else {               // not present
                        Node entry = new Node(item);
                        entry.next = current;
                        pred.next = entry;
                        this.version.getAndIncrement();
                        return true;
                    }
                }
            } finally {                // always unlock
                pred.unlock();
                current.unlock();
            }
        }
    }

    /**
     * Remove an element.
     *
     * @param item element to remove
     * @return true iff element was present
     */
    public boolean remove(T item) {
        int key = item.hashCode();
        while (true) {
            Node pred = this.head;
            Node current = pred.next;
            while (current.key < key) {
                pred = current;
                current = current.next;
            }
            int prev_version = this.version.get();
            pred.lock();
            current.lock();
            try {
                if (validate(pred, current, prev_version)) {
                    if (current.key == key) { // present in list
                        pred.next = current.next;
                        this.version.getAndIncrement();
                        return true;
                    } else {               // not present in list
                        return false;
                    }
                }
            } finally {                // always unlock
                pred.unlock();
                current.unlock();
            }
        }
    }

    /**
     * Test whether element is present
     *
     * @param item element to test
     * @return true iff element is present
     */
    public boolean contains(T item) {
        int key = item.hashCode();
        while (true) {
            Node pred = this.head; // sentinel node;
            Node current = pred.next;
            while (current.key < key) {
                pred = current;
                current = current.next;
            }
            try {
                int prev_version = this.version.get();
                pred.lock();
                current.lock();
                if (validate(pred, current, prev_version)) {
                    return (current.key == key);
                }
            } finally {                // always unlock
                pred.unlock();
                current.unlock();
            }
        }
    }

    @Override
    public void printElements() {
        Node node = head.next;
        while(node.next != null) {
            if((Integer) node.item >= 25000)
                break;
            if((Integer) node.item % 2 == 0) {
                System.out.print("Yikes");
                break;
            }
            node = node.next;
        }
        System.out.println("\n###################\n");
    }

    /**
     * Check that prev and current are still in list and adjacent
     *
     * @param pred    predecessor node
     * @param current current node
     * @return whther predecessor and current have changed
     */
    private boolean validate(Node pred, Node current, int prev_version) {
        if (prev_version == this.version.get()) {
            return true;
        }
        Node entry = head;
        while (entry.key <= pred.key) {
            if (entry == pred)
                return pred.next == current;
            entry = entry.next;
        }
        return false;
    }

    /**
     * list node
     */
    private class Node {
        /**
         * actual item
         */
        T item;
        /**
         * item's hash code
         */
        int key;
        /**
         * next node in list
         */
        Node next;
        /**
         * Synchronizes node.
         */
        Lock lock;

        /**
         * Constructor for usual node
         *
         * @param item element in list
         */
        Node(T item) {
            this.item = item;
            this.key = item.hashCode();
            lock = new ReentrantLock();
        }

        /**
         * Constructor for sentinel node
         *
         * @param key should be min or max int value
         */
        Node(int key) {
            this.key = key;
            lock = new ReentrantLock();
        }

        /**
         * Lock entry
         */
        void lock() {
            lock.lock();
        }

        /**
         * Unlock entry
         */
        void unlock() {
            lock.unlock();
        }
    }
}
