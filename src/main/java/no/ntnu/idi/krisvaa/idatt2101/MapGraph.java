package no.ntnu.idi.krisvaa.idatt2101;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.StringTokenizer;

/***
 * MapGraph contains the datastructure of a roadmap. The structure is built up as a weighted graph.
 */
public class MapGraph {

    int highestSpeedLimit = 0;
    int nodeCount, edgeCount;
    //ArrayList<Node> nodes;
    Node[] nodes;
    boolean[] lastSearch;
    int lastNodeCount;
    Node[] lastSpecialSearchResult;

    int[] landmarkIDs;
    int[][][] landmarkTable;


    /**
     * Initiate using MapGraph.buildFromInputStream()
     */
    private MapGraph() {}

    public void generateLandmarks(int[] landmarkIDs) {
        this.landmarkIDs = landmarkIDs;
        int[][][] distances = new int[landmarkIDs.length][nodeCount][2];

        for(int k = 0; k < 2; k++) {
            for(int i = 0; i < landmarkIDs.length; i++) {
                dijkstrasAlgorithm(landmarkIDs[i], -1, PriorityType.Time);
                System.out.println("Calculating landmark " + landmarkIDs[i]);
                for(int j = 0; j < nodeCount; j++) {
                    Node n = nodes[j];
                    distances[i][j][k] = ((IntersectionPredecessor)n.predecessor).time;
                }
            }

            reverseGraph();
        }

        this.landmarkTable = distances;
    }

    private void reverseGraph() {
        System.out.println("Reversing map graph...");

        Edge[] edgeCopy = new Edge[edgeCount];
        int edgeCount = 0;

        for(int i = 0; i < nodeCount; i++) {
            Node n = nodes[i];
            Edge e = n.rootEdge;
            n.rootEdge = null;

            while (e != null) {
                edgeCopy[edgeCount++] = e;
                e = e.next;
            }
        }

        for(Edge e : edgeCopy) {
            Node to = e.from;
            Node from = e.to;

            e.next = from.rootEdge;
            from.rootEdge = e;
            e.to = to;
            e.from = from;
        }

        System.out.println("Reverse completed.");
    }

    public void saveLandmarks(OutputStream stream) throws IOException {
        int fileDataIndex = 0;
        byte[] fileData = new byte[(landmarkIDs.length * (1  + nodeCount) * 4 + 8) * 2];

        addIntToByteArray(landmarkIDs.length, fileDataIndex+=4, fileData);

        for(int i = 0; i < landmarkIDs.length; i++) {
            int landmarkID = landmarkIDs[i];

            addIntToByteArray(landmarkID, fileDataIndex+=4, fileData);

            for(int k = 0; k < 2; k++) {
                for(int j = 0; j < nodeCount; j++) {
                    int weight = landmarkTable[i][j][k];
                    addIntToByteArray(weight, fileDataIndex+=4, fileData);
                }
            }

        }

        stream.write(fileData);
    }

    public void loadLandmarks(InputStream stream) throws IOException {
        byte[] data = stream.readAllBytes();

        int traversCounter = 0;
        int landmarkCount = readIntFromByteArray(traversCounter+=4, data);

        landmarkIDs = new int[landmarkCount];
        landmarkTable = new int[landmarkCount][nodeCount][2];

        for(int i = 0; i < landmarkCount; i++) {

            int landmarkID = readIntFromByteArray(traversCounter+=4, data);
            landmarkIDs[i] = landmarkID;

            for(int k = 0; k < 2; k++) {

                for(int j = 0; j < nodeCount; j++) {
                    int weight = readIntFromByteArray(traversCounter+=4, data);
                    landmarkTable[i][j][k] = weight;
                }
            }
        }
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

    public Node dijkstrasAlgorithm(int startNodeID, int endNodeID, PriorityType priorityType) {
        Node startNode = nodes[startNodeID];
        Node endNode = null;

        if(endNodeID > 0) {
            endNode = nodes[endNodeID];
        }

        return dijkstrasAlgorithm(startNode, endNode, priorityType);
    }

    private Node dijkstrasAlgorithm(Node startNode, Node endNode, PriorityType priorityType) {
        initPredecessor(startNode);

        lastSearch = new boolean[nodeCount];
        lastNodeCount = 0;

        IntersectionNodeType targetType = IntersectionNodeType.None;
        int typeCount = 0;
        final int targetTypeCount = 10;
        switch (priorityType) {
            case GasStations -> { targetType = IntersectionNodeType.GasStation; lastSpecialSearchResult = new Node[targetTypeCount]; }
            case CharningStations -> {targetType = IntersectionNodeType.ChargingStation; lastSpecialSearchResult = new Node[targetTypeCount]; }
        }

        PriorityQueue priorityQueue = new PriorityQueue(nodeCount);
        priorityQueue.insert(startNode);

        while (priorityQueue.length > 0) {
            Node activeNode = priorityQueue.getMin();

            if(activeNode == endNode && targetType == IntersectionNodeType.None) {
                System.out.println("Found shortest path to endnode. While scanning " + lastNodeCount + " nodes");
                return endNode;
            } else if(targetType == IntersectionNodeType.GasStation && ((IntersectionNode)activeNode).type == IntersectionNodeType.GasStation) {
                lastSpecialSearchResult[typeCount++] = activeNode;
            } else if(targetType == IntersectionNodeType.ChargingStation && ((IntersectionNode)activeNode).type == IntersectionNodeType.ChargingStation) {
                lastSpecialSearchResult[typeCount++] = activeNode;
            }

            if(typeCount > targetTypeCount-1) {
                System.out.println("Found " + priorityType + ". While scanning " + lastNodeCount + " nodes");
                return null;
            }

            for (Edge e = activeNode.rootEdge; e!= null; e = e.next) {
                IntersectionPredecessor activeNodePred = (IntersectionPredecessor) activeNode.predecessor;
                IntersectionPredecessor neighborNode = (IntersectionPredecessor) e.to.predecessor;
                RoadEdge roadEdge = ((RoadEdge)e);

                int edgeWeight = 0;

                switch (priorityType) {
                    case Length ->  edgeWeight = roadEdge.length;
                    default -> edgeWeight = roadEdge.elapsedTime;
                }

                if(neighborNode.totalWeight > activeNodePred.totalWeight + edgeWeight) {
                    lastNodeCount++;
                    lastSearch[activeNode.nodeNumber] = true;

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

    public Node ALTAlgorithm(int startNodeID, int endNodeID, PriorityType priorityType) {
        Node startNode = nodes[startNodeID];
        Node endNode = nodes[endNodeID];

        initPredecessor(startNode);
        lastSearch = new boolean[nodeCount];
        lastNodeCount = 0;

        PriorityQueue priorityQueue = new PriorityQueue(nodeCount);
        priorityQueue.insert(startNode);

        while (priorityQueue.length > 0) {
            Node activeNode = priorityQueue.getMin();

            if(activeNode == endNode) {
                System.out.println("Found shortest path to endnode. While scanning " + lastNodeCount + " nodes");
                return endNode;
            }

            for (Edge e = activeNode.rootEdge; e!= null; e = e.next) {
                IntersectionPredecessor activeNodePred = (IntersectionPredecessor) activeNode.predecessor;
                IntersectionPredecessor neighborNode = (IntersectionPredecessor) e.to.predecessor;
                RoadEdge roadEdge = ((RoadEdge)e);

                int edgeWeight = 0;

                switch (priorityType) {
                    case Time ->  edgeWeight = roadEdge.elapsedTime;
                    case Length ->  edgeWeight = roadEdge.length;
                }

                if(neighborNode.totalWeight > activeNodePred.totalWeight + edgeWeight) {
                    lastNodeCount++;
                    lastSearch[activeNode.nodeNumber] = true;

                    neighborNode.totalWeight = activeNodePred.totalWeight + edgeWeight;
                    neighborNode.distance = activeNodePred.distance + roadEdge.length;
                    neighborNode.time = activeNodePred.time + roadEdge.elapsedTime;
                    neighborNode.predecessor = activeNode;

                    int highestDiff = 0;

                    for(int l = 0; l < landmarkIDs.length; l++) {

                        int landmarkToTarget = landmarkTable[l][endNodeID][0];
                        int landmarkToNode = landmarkTable[l][activeNode.nodeNumber][0];
                        int diff1 = landmarkToTarget - landmarkToNode;
                        if(diff1 < 0) { diff1 = 0; }

                        int targetToLandmark = landmarkTable[l][endNodeID][1];
                        int nodeToLandmark = landmarkTable[l][activeNode.nodeNumber][1];
                        int diff2 = nodeToLandmark - targetToLandmark;
                        if(diff2 < 0) { diff2 = 0; }

                        if(diff1 > highestDiff || diff2 > highestDiff) {
                            if(diff1 > diff2) { highestDiff = diff1; }
                            else { highestDiff = diff2; }
                        }
                    }

                    //Time 0.9 to lower the effect on the priority queue. This opens the algorithm to search for more nodes.
                    neighborNode.distanceToTarget = (int) (highestDiff*0.90);
                    priorityQueue.insert(e.to);
                }
            }
        }

        System.out.println("Scanned " + lastNodeCount + " nodes. Unable to find path....");
        return endNode;
    }

    /**
     * Load a MapGraph by providing a list of intersection nodes and the associated road edges.
     *
     * @param intersectionNodes
     * @param roadEdges
     * @return Prebuilt MapGraph data-structure.
     * @throws IOException
     */
    public static MapGraph buildFromInputStream(InputStream intersectionNodes, InputStream roadEdges, InputStream nodeTypes) throws IOException {
        try(
            BufferedReader intersectionNodeBuffer = new BufferedReader(new InputStreamReader(intersectionNodes, StandardCharsets.UTF_8));
            BufferedReader roadEdgeBuffer = new BufferedReader(new InputStreamReader(roadEdges, StandardCharsets.UTF_8));
            BufferedReader nodeTypeBuffer = new BufferedReader(new InputStreamReader(nodeTypes, StandardCharsets.UTF_8));
        ) {
            MapGraph mapGraph = new MapGraph();

            StringTokenizer intersectionNodeBufferST = new StringTokenizer(intersectionNodeBuffer.readLine());
            mapGraph.nodeCount = Integer.parseInt(intersectionNodeBufferST.nextToken());

            StringTokenizer roadEdgeBufferST = new StringTokenizer(roadEdgeBuffer.readLine());
            mapGraph.edgeCount = Integer.parseInt(roadEdgeBufferST.nextToken());

            StringTokenizer nodeTypeBufferST = new StringTokenizer(nodeTypeBuffer.readLine());
            int nodeTypeCount =  Integer.parseInt(nodeTypeBufferST.nextToken());

            mapGraph.nodes = new Node[mapGraph.nodeCount];

            System.out.println("Loading map-nodes...");
            //Add intersections nodes to Map Graph
            while (intersectionNodeBuffer.ready()){
                String[] lineValues = intersectionNodeBuffer.readLine().replace("  ", " ").split(" ");
                int index = Integer.parseInt(lineValues[0]);
                double latitudes = Double.parseDouble(lineValues[1]);
                double longitudes = Double.parseDouble(lineValues[2]);

                IntersectionNode ISNode = new IntersectionNode(index, latitudes, longitudes);
                mapGraph.nodes[index] = ISNode;
            }

            System.out.println("Loading map-edges...");
            //Add roadEdges to Map Graph
            while (roadEdgeBuffer.ready()){
                String[] lineValues = roadEdgeBuffer.readLine().split("\t");
                Node fromNode = mapGraph.nodes[Integer.parseInt(lineValues[0])];
                Node toNode = mapGraph.nodes[Integer.parseInt(lineValues[1])];
                int elapsedTime = Integer.parseInt(lineValues[2]);
                int length = Integer.parseInt(lineValues[3]);
                int speedLimit = Integer.parseInt(lineValues[4]);

                if(speedLimit > mapGraph.highestSpeedLimit) mapGraph.highestSpeedLimit = speedLimit;

                fromNode.rootEdge = new RoadEdge(toNode, fromNode, fromNode.rootEdge, elapsedTime, length, speedLimit);
            }

            System.out.println("Loading node types...");
            while (nodeTypeBuffer.ready()){
                String[] lineValues = nodeTypeBuffer.readLine().split("\t");
                IntersectionNode node = (IntersectionNode) mapGraph.nodes[Integer.parseInt(lineValues[0])];
                int nodeType = Integer.parseInt(lineValues[1]);
                String name = lineValues[2];

                switch (nodeType) {
                    case 2: node.type = IntersectionNodeType.GasStation; break;
                    case 4: node.type = IntersectionNodeType.ChargingStation; break;
                    default: node.type = IntersectionNodeType.Normal;
                }
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
    IntersectionNodeType type;

    public IntersectionNode(int nodeNumber, double latitudes, double longitudes) {
        super(nodeNumber);
        this.latitudes = latitudes;
        this.longitudes = longitudes;
        type = IntersectionNodeType.Normal;
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
    int time = 0;
}

class RoadEdge extends Edge {
    int elapsedTime;
    int length;
    int speedLimit;

    public RoadEdge(Node to, Node from, Edge next, int elapsedTime, int length, int speedLimit) {
        super(to,from, next);
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
        return Integer.compare(this.predecessor.totalWeight + this.predecessor.distanceToTarget, ((Node)o).predecessor.totalWeight + ((Node)o).predecessor.distanceToTarget);
    }
}

class Edge {
    Edge next;
    Node to;
    Node from;

    public Edge(Node to, Node from, Edge next) {
        this.to = to;
        this.from = from;
        this.next = next;
    }
}

class Predecessor {
    public static final int INFINITY = 1000000000;

    int totalWeight = INFINITY;
    int distanceToTarget = 0;
    Node predecessor;
    int distance = 0;
}

enum IntersectionNodeType {
    None, Normal, GasStation, ChargingStation;
}

enum AlgorithmType {
    Dijkstras { public String toString() {return "Dijkstras Algorithm";}},
    ALT { public String toString() {return "ALT Algortihm";}}
}

enum PriorityType {
    Length { public String toString() {return "Travel distance";}},
    Time { public String toString() {return "Travel time";}},
    GasStations { public String toString() {return "10 Closest Gas Stations";}},
    CharningStations { public String toString() {return "10 Closest Charging Stations";}}
}