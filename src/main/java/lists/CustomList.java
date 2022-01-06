package lists;

public interface CustomList<T> {
    boolean add(T item);

    boolean remove(T item);

    boolean contains(T item);

    void printElements();
}
