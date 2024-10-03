package gh2;
import deque.*;


//Note: This file will not compile until you complete the Deque61B implementations
public class GuitarString {
    /**
     * Constants. Do not change. In case you're curious, the keyword final
     * means the values cannot be changed at runtime. We'll discuss this and
     * other topics in lecture on Friday.
     */
    private static final int SR = 44100;      // Sampling Rate
    private static final double DECAY = .996; // energy decay factor

    /* Buffer for storing sound data. */

    private Deque61B<Double> buffer;
    private int cap;

    /* Create a guitar string of the given frequency.  */
    public GuitarString(double frequency) {

        cap = (int) Math.round(SR / frequency);
        buffer = new LinkedListDeque61B<>();

        for (int i = 0; i < cap; i++) {
            buffer.addLast(0.0); //start with all as 0

        }

    }


    /* Pluck the guitar string by replacing the buffer with white noise. */
    public void pluck() {

        while (!buffer.isEmpty()) {
            buffer.removeFirst();
        }

        for (int i = 0; i < cap; i++) {
            double r = Math.random() - 0.5;
            buffer.addLast(r);
        }

    }

    /* Advance the simulation one time step by performing one iteration of
     * the Karplus-Strong algorithm.
     */

    public void tic() {

        double f = buffer.removeFirst();
        double n = buffer.get(0);
        double newDouble = ((f + n) / 2) * DECAY;
        buffer.addLast(newDouble);
    }


    /* Return the double at the front of the buffer. */
    public double sample() {


        return buffer.get(0);
    }
}



