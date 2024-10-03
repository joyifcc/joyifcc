import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ArrayDeque61B<T> implements Deque61B<T> {
    private T[] items;
    private int size;
    private int nextFirst;
    private int nextLast;

    public ArrayDeque61B() {
        items = (T[]) new Object[8];
        size = 0;
        nextFirst = 7;
        nextLast = 0;

    }

    public void resize(int cap) {
        T[] newItems = (T[]) new Object[cap];

        int index = Math.floorMod(nextFirst + 1, items.length);

        for (int i = 0; i < size; i++) {
            newItems[i] = items[index];
            index = Math.floorMod(index + 1, items.length);
        }

        nextFirst = cap - 1;
        nextLast = size;
        items = newItems;

    }


    @Override
    public void addFirst(T item) {

        if (size == items.length) {
            int cap = items.length * 2; //reach max cap
            resize(cap);
        }

        items[nextFirst] = item;
        nextFirst = Math.floorMod(nextFirst - 1, items.length); //acts as a wrap; decrement
        size++;

    }

    @Override
    public void addLast(T item) {

        if (size == items.length) {
            int cap = items.length * 2; //reach max cap
            resize(cap);
        }

        size++;
        items[nextLast] = item; //update back index
        nextLast = Math.floorMod(nextLast + 1, items.length); //increment
    }

    @Override
    public List<T> toList() {
        List<T> returnList = new ArrayList<>();

        int index = Math.floorMod(nextFirst + 1, items.length);

        for (int i = 0; i < size; i++) {
            returnList.add(items[index]);
            index = Math.floorMod(index + 1, items.length);

        }

        return returnList;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public T removeFirst() {

        final int j = 16;

        if (items.length > j && size < (items.length / 4)) { //given requirements
            int cap = items.length / 4;
            resize(cap);
        }

        if (size == 0) {
            return null;
        }

        nextFirst = Math.floorMod(nextFirst + 1, items.length); //decrement
        T removed = items[nextFirst];
        items[nextFirst] = null;
        size--;
        return removed;
    }

    @Override
    public T removeLast() {

        final int j = 16;

        if (items.length > j && size < (items.length / 4)) { //given requirements
            int cap = items.length / 4;
            resize(cap);
        }

        if (size == 0) {
            return null;
        }

        size--;
        nextLast = Math.floorMod(nextLast - 1, items.length); //decrement
        T removed = items[nextLast];
        items[nextLast] = null;

        return removed;
    }

    @Override
    public T get(int index) {

        if (index >= size() || index < 0) {
            return null;
        }

        int realindex = Math.floorMod(nextFirst + index + 1, items.length); //index wrap around arr

        return items[realindex];
    }



    @Override
    public T getRecursive(int index) {
        throw new UnsupportedOperationException("No need to implement getRecursive for proj 1b");
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }


    public Iterator<T> iterator() {
        return new ArraySetIterator();
    }

    private class ArraySetIterator implements Iterator<T> {
        private int pos;
        private int num;


        public ArraySetIterator() {
            pos = Math.floorMod(nextFirst + 1, items.length);
            num = 0;

        }

        @Override
        public boolean hasNext() {
            return num < size;
        }


        @Override
        public T next() {
            T returnItem = items[pos];
            pos = Math.floorMod(pos + 1, items.length);
            num++;
            return returnItem;

        }

    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }

        if (!(o instanceof Deque61B<?>)) {
            return false; // checks type
        }

        Deque61B<?> otherD = (Deque61B<?>) o;
        Iterator<T> thisIt = this.iterator();
        Iterator<?> otherIt = otherD.iterator();

        if (this.size() != otherD.size()) {
            return false;
        }

        while (thisIt.hasNext() && otherIt.hasNext()) {
            if (!thisIt.next().equals(otherIt.next())) {
                return false;
            }
        }
        boolean thisHasNext = thisIt.hasNext();
        boolean otherHasNext = otherIt.hasNext();

        return !thisHasNext && !otherHasNext;

    }

    @Override
    public String toString() {
        return this.toList().toString();
    }

}



