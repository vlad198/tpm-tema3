package lists;

/*
 * OptimisticList.java
 *
 * From "Multiprocessor Synchronization and Concurrent Data Structures",
 * by Maurice Herlihy and Nir Shavit.
 * Copyright 2006 Elsevier Inc. All rights reserved.
 */

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Optimistic List implementation.
 *
 * @author Maurice Herlihy
 * (minor refactoring by Emanuel Onica)
 */
public class VersionedOptimisticListV3<T> implements CustomList<T> {
    /**
     * First list entry
     */
    private final Node head;
    private int version;
//    private AtomicInteger atomicVersion;

    /**
     * Constructor
     */
    public VersionedOptimisticListV3() {
        this.head = new Node(Integer.MIN_VALUE);
        this.head.next = new Node(Integer.MAX_VALUE);
        this.version = 0;
//        this.atomicVersion = new AtomicInteger(0);
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
            int prev_version = this.version;
            int predVersion = pred.version.get();
            int currentVersion = current.version.get();
//          int prev_atomic_version = this.atomicVersion.get();
            pred.lock();
            current.lock();
            try {
//                if(validate(pred, current, prev_version) != validate(pred, current, prev_atomic_version)) {
//                    System.out.println("WE NEED ATOMIC!");
//                }
                if (validate(pred, current, prev_version, predVersion, currentVersion)) {
                    if (current.key == key) { // present
                        return false;
                    } else {               // not present
                        Node entry = new Node(item);
                        entry.next = current;
                        pred.next = entry;
                        this.version++;
//                      this.atomicVersion.getAndIncrement();
                        pred.version.getAndIncrement();
                        entry.version.getAndIncrement();
                        current.version.getAndIncrement();
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
            int prev_version = this.version;
            int predVersion = pred.version.get();
            int currentVersion = current.version.get();
//          int prev_atomic_version = this.atomicVersion.get();
            pred.lock();
            current.lock();
            try {
//                if(validate(pred, current, prev_version) != validate(pred, current, prev_atomic_version)) {
//                    System.out.println("WE NEED ATOMIC!");
//                }
                if (validate(pred, current, prev_version, predVersion, currentVersion)) {
                    if (current.key == key) { // present in list
                        pred.next = current.next;
                        this.version++;
//                      this.atomicVersion.getAndIncrement();
                        pred.version.getAndIncrement();
                        current.version.getAndIncrement();

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
            int prev_version = this.version;
            int predVersion = pred.version.get();
            int currentVersion = current.version.get();
//          int prev_atomic_version = this.atomicVersion.get();
            try {
                pred.lock(); // should these be in here?
                current.lock(); // keep it this way since that's how they are in the original
//                if(validate(pred, current, prev_version) != validate(pred, current, prev_atomic_version)) {
//                    System.out.println("WE NEED ATOMIC!");
//                }
                if (validate(pred, current, prev_version, predVersion, currentVersion)) {
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
    private boolean validate(Node pred, Node current, int prev_version, int predVersion, int currentVersion) {
        if (prev_version == this.version) {
            return true;
        }
        if (pred.next == current && pred.version.get() == predVersion && current.version.get() == currentVersion) {
//          System.out.println("Used");
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

        AtomicInteger version;

        /**
         * Constructor for usual node
         *
         * @param item element in list
         */
        Node(T item) {
            this.item = item;
            this.key = item.hashCode();
            lock = new ReentrantLock();
            version = new AtomicInteger(0);
        }

        /**
         * Constructor for sentinel node
         *
         * @param key should be min or max int value
         */
        Node(int key) {
            this.key = key;
            lock = new ReentrantLock();
            version = new AtomicInteger(0); // does this value matter?
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
