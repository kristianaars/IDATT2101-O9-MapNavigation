package no.ntnu.idi.krisvaa.idatt2101;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.StringTokenizer;

/***
 * MapGraph contains the datastructure of a roadmap. The structure is built up as a weighted graph.
 */
public class MapGraph {

    int nodeCount, edgeCount;
    ArrayList<Node> nodes;

    int[] landmarkIDs;
    int[][] landmarkTable;

    /**
     * Initiate using MapGraph.buildFromInputStream()
     */
    private MapGraph() {}

    public void generateLandmarks(int[] landmarkIDs) {
        this.landmarkIDs = landmarkIDs;

        int[][] distances = new int[landmarkIDs.length][nodeCount];
        for(int i = 0; i < landmarkIDs.length; i++) {
            dijkstrasAlgorithm(landmarkIDs[i], -1);
            System.out.println("Calculating landmark " + landmarkIDs[i]);
            for(int j = 0; j < nodeCount; j++) {
                Node n = nodes.get(j);
                distances[i][j] = n.predecessor.totalWeight;
            }
        }

        this.landmarkTable = distances;
    }

    public void loadLandmarks(InputStream stream) throws IOException {
        byte[] data = stream.readAllBytes();

        int traversCounter = 0;
        int landmarkCount = readIntFromByteArray(traversCounter+=4, data);

        landmarkIDs = new int[landmarkCount];
        landmarkTable = new int[landmarkCount][nodeCount];

        for(int i = 0; i < landmarkCount; i++) {
            int landmarkID = readIntFromByteArray(traversCounter+=4, data);
            landmarkIDs[i] = landmarkID;

            for(int j = 0; j < nodeCount; j++) {
                int weight = readIntFromByteArray(traversCounter+=4, data);
                landmarkTable[i][j] = weight;
            }
        }
    }

    public void saveLandmarks(OutputStream stream) throws IOException {
        int fileDataIndex = 0;
        byte[] fileData = new byte[landmarkIDs.length * (1  + nodeCount) * 4 + 8];

        addIntToByteArray(landmarkIDs.length, fileDataIndex+=4, fileData);

        for(int i = 0; i < landmarkIDs.length; i++) {
            int landmarkID = landmarkIDs[i];

            addIntToByteArray(landmarkID, fileDataIndex+=4, fileData);

            for(int j = 0; j < nodeCount; j++) {
                int weight = landmarkTable[i][j];
                addIntToByteArray(weight, fileDataIndex+=4, fileData);
            }
        }

        stream.write(fileData);
    }

    private void addIntToByteArray(int n, int index, byte[] arr) {
        arr[index++] = (byte)(n >>> 24);
        arr[index++] = (byte)(n >>> 16);
        arr[index++] = (byte)(n >>> 8);
        arr[index++] = (byte)(n);
    }

    public int readIntFromByteArray(int index, byte[] bytes) {
        int r = 0;
        for(int i = 0; i < bytes.length && i < 4; i++) {
            r <<= 8;
            r |= (int)bytes[index + i] & 0xFF;
        }
        return r;
    }

    public Node dijkstrasAlgorithm(int startNodeID, int endNodeID) {
        Node startNode = nodes.get(startNodeID);
        Node endNode = null;

        if(endNodeID > 0) {
            endNode = nodes.get(endNodeID);
        }

        initPredecessor(startNode);

        int nodeCounter = 0;

        PriorityQueue priorityQueue = new PriorityQueue(nodeCount);
        priorityQueue.insert(startNode);

        for(int i = nodeCount; i > 1; --i) {
            Node activeNode = priorityQueue.getMin();

            if(activeNode == endNode) {
                System.out.println("Found shortest path to endnode. While scanning " + nodeCounter + " nodes");
                return endNode;
            }

            for (Edge e = activeNode.rootEdge; e!= null; e = e.next) {
                IntersectionPredecessor activeNodePred = (IntersectionPredecessor) activeNode.predecessor;
                IntersectionPredecessor neighborNode = (IntersectionPredecessor) e.to.predecessor;
                RoadEdge roadEdge = ((RoadEdge)e);

                int edgeWeight = roadEdge.elapsedTime;

                if(neighborNode.totalWeight > activeNodePred.totalWeight + edgeWeight) {
                    nodeCounter++;
                    neighborNode.totalWeight = activeNodePred.totalWeight + edgeWeight;
                    neighborNode.distance = activeNodePred.distance + roadEdge.length;
                    neighborNode.time = activeNodePred.time + roadEdge.elapsedTime;
                    neighborNode.predecessor = activeNode;

                    priorityQueue.insert(e.to);
                }
            }
        }

        return endNode;
    }

    public Node ALTAlgorithm(int startNodeID, int endNodeID) {
        Node startNode = nodes.get(startNodeID);
        Node endNode = nodes.get(endNodeID);

        initPredecessor(startNode);

        return null;
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

            System.out.println("Loading map-nodes...");
            //Add intersections nodes to Map Graph
            while (intersectionNodeBuffer.ready()){
                intersectionNodeBufferST = new StringTokenizer(intersectionNodeBuffer.readLine());
                int index = Integer.parseInt(intersectionNodeBufferST.nextToken());
                double latitudes = Double.parseDouble(intersectionNodeBufferST.nextToken());
                double longitudes = Double.parseDouble(intersectionNodeBufferST.nextToken());

                IntersectionNode ISNode = new IntersectionNode(index, latitudes, longitudes);
                mapGraph.nodes.add(index, ISNode);
            }

            System.out.println("Loading map-edges...");
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

            return mapGraph;
        }

    }

    private void initPredecessor(Node startNode) {
        for(Node n : nodes) {
            n.predecessor = new IntersectionPredecessor();
        }
        startNode.predecessor.totalWeight = 0;
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

    @Override
    public String toString() {
        return "IntersectionNode{" +
                latitudes + "," + longitudes +
                ", nodeNumber=" + nodeNumber +
                "} ";
    }
}

class IntersectionPredecessor extends Predecessor {
    int distance = 0;
    int time = 0;
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
        return Integer.compare(this.predecessor.totalWeight, ((Node)o).predecessor.totalWeight);
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
    public static final int INFINITY = 800000000;

    int totalWeight = INFINITY;
    Node predecessor;
}


enum AlgorithmType {
    Dijkstras { public String toString() {return "Dijkstras Algorithm";}},
    ALT { public String toString() {return "ALT Algortihm";}}
}

enum PriorityType {
    Length { public String toString() {return "Travel distance";}},
    Time { public String toString() {return "Travel time";}}
}