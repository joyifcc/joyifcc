import java.util.Iterator;
import java.util.List;
import java.util.ArrayList; // import the ArrayList class


public class LinkedListDeque61B<T> implements Deque61B<T> {

    private class Node {
        T item;
        Node previous;
        Node next;

        Node(T i, Node p, Node n) {
            this.item = i;
            this.previous = p;
            this.next = n;


        }
    }

    private Node sentinel;
    private int size;

    public LinkedListDeque61B() {
        //creates new instance of Node class with 3 arguments all null as placeholder

        sentinel = new Node(null, null, null);

        size = 0; //start empty arr
        //circular trend of pointing back to itself
        sentinel.previous = sentinel;
        sentinel.next = sentinel;

    }



    @Override
    public void addFirst(T item) {
        // Node(item, previous, next)
        Node newN = new Node(item, sentinel, sentinel.next);
        sentinel.next.previous = newN;
        sentinel.next = newN;
        size++;

    }

    @Override
    public void addLast(T item) {
        Node newN = new Node(item, sentinel.previous, sentinel);
        sentinel.previous.next = newN;
        sentinel.previous = newN;
        size++;

    }

    @Override
    public List<T> toList() {
        List<T> returnList = new ArrayList<>();

        Node currN = sentinel.next; //curr node is the one right after sentinel

        while (currN != sentinel) { //loop until back to sentinel so stop when curr = sentinel
            returnList.add(currN.item);

            currN = currN.next;
        }

        return returnList;
    }

    @Override
    public boolean isEmpty() {
        return size == 0; }

    @Override
    public int size() {
        return size; }

    @Override
    public T removeFirst() {

        //return null if nothing is removed
        if (size == 0) {
            return null;
        }

        Node f = sentinel.next;
        sentinel.next = f.next; //skips the f node and makes the new f node to be next to sentinel
        f.next.previous = sentinel; //makes new f node point back to sentinel as prev
        size--;
        return (f.item);
    }


    @Override
    public T removeLast() {

        if (size == 0) {
            return null;
        }

        Node l = sentinel.previous;
        sentinel.previous = l.previous;
        l.previous.next = sentinel;
        size--;

        return (l.item);
    }

    @Override
    public T get(int index) {
        if (index >= size() || index < 0) {
            return null;
        }

        Node curr = sentinel.next;
        for (int i = 0; i < index; i++) {
            curr = curr.next;
        }
        return curr.item;
    }


    @Override
    public T getRecursive(int index) {

        if (index >= size() || index < 0) {
            return null;
        }

        Node curr = sentinel.next;

        return getRecursiveHelper(curr, index);
    }

    private T getRecursiveHelper(Node curr, int index) {
        if (index == 0) {
            return curr.item;
        } else {
            return getRecursiveHelper(curr.next, index - 1);
        }
    }


    public Iterator<T> iterator() {
        return new LinkedListDequeIterator();
    }

    private class LinkedListDequeIterator implements Iterator<T> {
        private Node curr = sentinel.next;


        public boolean hasNext() {
            return curr != sentinel;
        } //check if reached sentinel or not


        public T next() {
            T returnItem = curr.item;
            curr = curr.next;
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
