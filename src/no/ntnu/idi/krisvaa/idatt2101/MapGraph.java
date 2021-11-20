package no.ntnu.idi.krisvaa.idatt2101;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class MapGraph {

    int nodeCount, edgeCount;
    ArrayList<Node> nodes;

    public static MapGraph readFromInputStream(InputStream intersectionNodes, InputStream roadEdges) throws IOException {
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

class Node {
    int nodeNumber;
    Edge rootEdge;

    Predecessor predecessor;

    public Node(int nodeNumber) {
        this.nodeNumber = nodeNumber;
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
