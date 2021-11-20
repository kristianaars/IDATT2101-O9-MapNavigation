package no.ntnu.idi.krisvaa.idatt2101;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.StringTokenizer;

/***
 * MapGraph contains the datastructure of a roadmap. The structure is built up as a weighted graph.
 */
public class MapGraph {

    int nodeCount, edgeCount;
    ArrayList<Node> nodes;

    /**
     * Initiate using MapGraph.buildFromInputStream()
     */
    private MapGraph() {}

    public void dijkstrasAlgorithm(Node startNode, Node endNode) {
        PriorityQueue priorityQueue = new PriorityQueue(nodeCount);
        priorityQueue.createHeap(nodes.toArray(new Node[0]));

        for(int i = nodeCount; i > 1; --i) {
            Node activeNode = priorityQueue.getMin();

            if(activeNode == endNode) {
                //We found the shortest road to the end node.
                return;
            }

            int activeNodeDistance = activeNode.predecessor.distance;

            for (Edge e = activeNode.rootEdge; e!= null; e = e.next) {
                Predecessor neighborNode = e.to.predecessor;
                int edgeWeight = ((RoadEdge)e).length;

                if(neighborNode.distance > activeNodeDistance + edgeWeight) {
                    neighborNode.distance = activeNodeDistance + edgeWeight;
                    neighborNode.predecessor = activeNode;
                }
            }
        }
    }

    /**
     * Load a MapGraph by providing a list of intersection nodes and the associated road edges.
     *
     * @param intersectionNodes
     * @param roadEdges
     * @return Prebuilt MapGraph data-structure.
     * @throws IOException
     */
    public static MapGraph buildFromInputStream(InputStream intersectionNodes, InputStream roadEdges) throws IOException {
        try(
            BufferedReader intersectionNodeBuffer = new BufferedReader(new InputStreamReader(intersectionNodes, StandardCharsets.UTF_8));
            BufferedReader roadEdgeBuffer = new BufferedReader(new InputStreamReader(roadEdges, StandardCharsets.UTF_8))
        ) {
            MapGraph mapGraph = new MapGraph();

            StringTokenizer intersectionNodeBufferST = new StringTokenizer(intersectionNodeBuffer.readLine());
            mapGraph.nodeCount = Integer.parseInt(intersectionNodeBufferST.nextToken());

            StringTokenizer roadEdgeBufferST = new StringTokenizer(roadEdgeBuffer.readLine());
            mapGraph.edgeCount = Integer.parseInt(roadEdgeBufferST.nextToken());

            mapGraph.nodes = new ArrayList<>(mapGraph.nodeCount);

            //Add intersections nodes to Map Graph
            while (intersectionNodeBuffer.ready()){
                intersectionNodeBufferST = new StringTokenizer(intersectionNodeBuffer.readLine());
                int index = Integer.parseInt(intersectionNodeBufferST.nextToken());
                double latitudes = Double.parseDouble(intersectionNodeBufferST.nextToken());
                double longitudes = Double.parseDouble(intersectionNodeBufferST.nextToken());

                IntersectionNode ISNode = new IntersectionNode(index, latitudes, longitudes);
                mapGraph.nodes.add(index, ISNode);
            }

            //Add roadEdges to Map Graph
            while (roadEdgeBuffer.ready()){
                roadEdgeBufferST = new StringTokenizer(roadEdgeBuffer.readLine());
                Node fromNode = mapGraph.nodes.get(Integer.parseInt(roadEdgeBufferST.nextToken()));
                Node toNode = mapGraph.nodes.get(Integer.parseInt(roadEdgeBufferST.nextToken()));
                int elapsedTime = Integer.parseInt(roadEdgeBufferST.nextToken());
                int length = Integer.parseInt(roadEdgeBufferST.nextToken());
                int speedLimit = Integer.parseInt(roadEdgeBufferST.nextToken());

                fromNode.rootEdge = new RoadEdge(toNode, fromNode.rootEdge, elapsedTime, length, speedLimit);
            }
        }

        return null;
    }

    private void initPredecessor(Node startNode) {
        for(Node n : nodes) {
            n.predecessor = new Predecessor();
        }
        startNode.predecessor.distance = 0;
    }

}

class IntersectionNode extends Node {
    double latitudes;
    double longitudes;

    public IntersectionNode(int nodeNumber, double latitudes, double longitudes) {
        super(nodeNumber);
        this.latitudes = latitudes;
        this.longitudes = longitudes;
    }
}

class RoadEdge extends Edge {
    int elapsedTime;
    int length;
    int speedLimit;

    public RoadEdge(Node to, Edge next, int elapsedTime, int length, int speedLimit) {
        super(to, next);
        this.elapsedTime = elapsedTime;
        this.length = length;
        this.speedLimit = speedLimit;
    }
}

class Node implements Comparable {
    int nodeNumber;
    Edge rootEdge;

    Predecessor predecessor;

    public Node(int nodeNumber) {
        this.nodeNumber = nodeNumber;
    }

    @Override
    public int compareTo(Object o) {
        return Integer.compare(this.predecessor.distance, ((Node)o).predecessor.distance);
    }
}

class Edge {
    Edge next;
    Node to;

    public Edge(Node to, Edge next) {
        this.to = to;
        this.next = next;
    }
}

class Predecessor {
    public static final int INFINITY = Integer.MAX_VALUE;

    int distance = INFINITY;
    Node predecessor;
}
