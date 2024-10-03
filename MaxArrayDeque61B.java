import java.util.Comparator;


public class MaxArrayDeque61B<T> extends ArrayDeque61B<T> {
    private Comparator<T> comparator;

    public MaxArrayDeque61B(Comparator<T> c) {
        this.comparator = c;
    }

    public T max() {
        return max(this.comparator);
    }

    public T max(Comparator<T> c) {
        if (isEmpty()) {
            return null;
        }

        T maxE = get(0);

        for (int i = 1; i < size(); i++) {
            T currE = get(i);

            if (c.compare(currE, maxE) > 0) {
                maxE = currE;
            }
        }
        return maxE;
    }

}

