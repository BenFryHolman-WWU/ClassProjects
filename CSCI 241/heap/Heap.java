package heap;
/*
 * Author: Ben Fry-Holman
 * Date: 7/25/2024
 * Purpose: Heaps for A3
 */
import java.util.NoSuchElementException;

/** An instance is a min-heap of distinct values of type V with
 *  priorities of type P. Since it's a min-heap, the value
 *  with the smallest priority is at the root of the heap. */
public final class Heap<V, P extends Comparable<P>> {

    // TODO 1.1: Read and understand the class invariants given in the
    // following comment:

    /**
     * The contents of c represent a complete binary tree. We use square-bracket
     * shorthand to denote indexing into the AList (which is actually
     * accomplished using its get method. In the complete tree,
     * c[0] is the root; c[2i+1] is the left child of c[i] and c[2i+2] is the
     * right child of i.  If c[i] is not the root, then c[(i-1)/2] (using
     * integer division) is the parent of c[i].
     *
     * Class Invariants:
     *
     *   The tree is complete:
     *     1. `c[0..c.size()-1]` are non-null
     *
     *   The tree satisfies the heap property:
     *     2. if `c[i]` has a parent, then `c[i]`'s parent's priority
     *        is smaller than or equal to `c[i]`'s priority
     *
     *   In Phase 3, the following class invariant also must be maintained:
     *     3. The tree cannot contain duplicate *values*; note that duplciate
     *        *priorities* are still allowed.
     *     4. map contains one entry for each element of the heap, so
     *        map.size() == c.size()
     *     5. For each value v in the heap, its map entry contains in
     *        the index of v in c. Thus: map.get(c[i]) = i.
     */
    protected AList<Entry> c;
    protected HashTable<V, Integer> map;

    /** Constructor: an empty heap with capacity 10. */
    public Heap() {
        c = new AList<Entry>(10);
        map = new HashTable<V, Integer>();
    }

    /** An Entry contains a value and a priority. */
    class Entry {
        public V value;
        public P priority;

        /** An Entry with value v and priority p*/
        Entry(V v, P p) {
            value = v;
            priority = p;
        }

        public String toString() {
            return value.toString();
        }
    }

    /** Add v with priority p to the heap.
     *  The expected time is logarithmic and the worst-case time is linear
     *  in the size of the heap. Precondition: p is not null.
     *  In Phase 3 only:
     *  @throws IllegalArgumentException if v is already in the heap.*/
    public void add(V v, P p) throws IllegalArgumentException {
        if (map.containsKey(v)) throw new IllegalArgumentException();  
        Entry entry = new Entry(v, p);
        c.append(entry);
        map.put(v, c.size() - 1);
        bubbleUp(c.size() - 1);
    }

    /** Return the number of values in this heap.
     *  This operation takes constant time. */
    public int size() {
        return c.size();
    }

    /** Swap c[h] and c[k].
     *  precondition: h and k are >= 0 and < c.size() */
    protected void swap(int h, int k) {
        Entry temp = c.get(h);
        c.put(h, c.get(k));
        c.put(k, temp);
        map.put(c.get(h).value, h);
        map.put(c.get(k).value, k);
    }

    /** Bubble c[k] up in heap to its right place.
     *  Precondition: Priority of every c[i] >= its parent's priority
     *                except perhaps for c[k] */
    protected void bubbleUp(int k) {
        while (k > 0) {
            int parent = (k - 1) / 2;
            if (c.get(k).priority.compareTo(c.get(parent).priority) >= 0) {
                break;
            }
            swap(k, parent);
            k = parent;
        }
    }

    /** Return the value of this heap with lowest priority. Do not
     *  change the heap. This operation takes constant time.
     *  @throws NoSuchElementException if the heap is empty. */
    public V peek() throws NoSuchElementException {
        // if AList length is 0 throw new
        if (c.size() == 0) throw new NoSuchElementException("Heap is empty.");
        return c.get(0).value;
    }

    /** Remove and return the element of this heap with lowest priority.
     *  The expected time is logarithmic and the worst-case time is linear
     *  in the size of the heap.
     *  @throws NoSuchElementException if the heap is empty. */
    public V poll() throws NoSuchElementException {
        // if AList length is 0 throw new
        if (c.size() == 0) throw new NoSuchElementException("Heap is empty.");
        swap(0, c.size() - 1);
        Entry minEntry = c.pop();
        map.remove(minEntry.value);
        bubbleDown(0);
        return minEntry.value;
    }

    /** Bubble c[k] down in heap until it finds the right place.
     *  If there is a choice to bubble down to both the left and
     *  right children (because their priorities are equal), choose
     *  the right child.
     *  Precondition: Each c[i]'s priority <= its childrens' priorities
     *                except perhaps for c[k] */
    protected void bubbleDown(int k) {
        while (2 * k + 1 < c.size()) {
            int child = smallerChild(k);
            if (c.get(k).priority.compareTo(c.get(child).priority) <= 0) {
                break;
            }
            swap(k, child);
            k = child;
        }
    }

    /** Return true if the value v is in the heap, false otherwise.
     *  The average case runtime is O(1).  */
    public boolean contains(V v) {
        return map.containsKey(v);
    }

    /** Change the priority of value v to p.
     *  The expected time is logarithmic and the worst-case time is linear
     *  in the size of the heap.
     *  @throws IllegalArgumentException if v is not in the heap. */
    public void changePriority(V v, P p) throws IllegalArgumentException {
        // throw if v not in map
        if (!map.containsKey(v)) throw new IllegalArgumentException("Value not found in heap.");
        int index = map.get(v);
        P oldPriority = c.get(index).priority;
        c.get(index).priority = p;
        if (p.compareTo(oldPriority) < 0) {
            bubbleUp(index);
        } else {
            bubbleDown(index);
        }
    }

    // Recommended helper method spec:
    /* Return the index of the child of k with smaller priority.
     * if only one child exists, return that child's index
     * Precondition: at least one child exists.*/
    private int smallerChild(int k) {
        int left = 2 * k + 1;
        int right = 2 * k + 2;
        if (right >= c.size() || c.get(left).priority.compareTo(c.get(right).priority) <= 0) {
            return left;
        } else {
            return right;
        }
    }

}
