package no.ntnu.idi.krisvaa.idatt2101;

/**
 * Minimum Priority Queue created for Dijakstras and ALT algorithm used in MapGraph.
 */
public class PriorityQueue {

    int length;
    Node[] nodes;

    public PriorityQueue(int size) {
        nodes = new Node[size];
        length = 0;
    }

    private void fixHeap(int i) {
        int m = left(i);

        if(m < length) {
            int h = m + 1;

            if(h < length && nodes[h].compareTo(nodes[m]) < 0) {
                m = h;
            }

            if(nodes[m].compareTo(nodes[i]) < 0) {
                swap(i, m);
                fixHeap(m);
            }
        }
    }

    public void heapsort() {
        createHeap(nodes);
    }

    public Node getMin() {
        Node min = nodes[0];
        nodes[0] = nodes[--length];
        fixHeap(0);
        return min;
    }

    public void insert(Node n) {
        int i = length++;
        nodes[i] = n;
        int f;
        while(i > 0 && nodes[i].compareTo(nodes[f=parent(i)]) < 0) {
            swap(i, f);
            i = f;
        }
    }

    public void createHeap(Node[] nodes) {
        if(this.length == -1) this.length = nodes.length;
        this.nodes = nodes;
        int i = length / 2;
        while (i-- > 0) fixHeap(i);
    }

    private void swap(int m, int n) {
        Node oldM = nodes[m];
        nodes[m] = nodes[n];
        nodes[n] = oldM;
    }

    //Navigation methods
    private int parent(int i) { return (i - 1) >> 1; }
    private int left(int i) { return (i << 1) + 1; }
    private int right(int i) { return (i + 1) << 1; }
}
