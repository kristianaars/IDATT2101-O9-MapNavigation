package no.ntnu.idi.krisvaa.idatt2101;

import org.jxmapviewer.viewer.GeoPosition;

import java.io.*;
import java.util.Arrays;
import java.util.HashSet;

public class MapController {

    public static void main(String[] args) throws IOException {
        MapController mc = new MapController(
                "norden_noder.txt",
                "norden_kanter.txt",
                "norden_landemerker.lm",
                "interessepkt.txt",
                new int[]{2151398, 2724481, 251295, 1045921, 3209493, 6579443, 5492224}
        );

        /*MapController mc = new MapController(
                "island_noder.txt",
                "island_kanter.txt",
                "island_landemerker.lm",
                "interessepkt.txt",
                new int[]{6, 55, 20030, 34}
        );*/

    }

    private MainFrame mainFrame;
    private MapGraph mapGraph;

    public MapController(String intersectioNodeFile, String roadEdgeFile, String landmarkFile, String pointsOfInterestFile, int[] landmarkNodes) throws IOException {
        System.out.println("Loading map-data...");
        mapGraph = MapGraph.buildFromInputStream(new FileInputStream(intersectioNodeFile), new FileInputStream(roadEdgeFile));

        System.out.println("Loading landmarks...");
        loadLandmarks(mapGraph, landmarkNodes, landmarkFile);

        System.out.println("Map-data finished loading!");

        mainFrame = new MainFrame(this);

        mainFrame.setVisible(true);

        plotNodesOnMap(landmarkNodes);
    }

    private void loadLandmarks(MapGraph mapGraph, int[] landmarkNodes, String landmarkFile) throws IOException {
        File f = new File(landmarkFile);
        boolean generateNewFile = true;

        if(f.exists()) {
            System.out.println("Landmark file found. Loading landmarks...");
            mapGraph.loadLandmarks(new FileInputStream(f));

            if(Arrays.compare(mapGraph.landmarkIDs, landmarkNodes) != 0) {
                System.out.println("Mismatch in landmark nodes between file and the provided landmarks.");
            } else {
                generateNewFile = false;
            }
        }

        if(generateNewFile) {
            System.out.println("Valid landmark file not found. Generating landmarks...");
            mapGraph.generateLandmarks(landmarkNodes);
            System.out.println("Landmarks generated. Saving to file...");
            mapGraph.saveLandmarks(new FileOutputStream(f));
        }
    }

    private void plotAllPointsOnMap() {
        HashSet<GeoPosition> points = new HashSet<>();
        for (int i = 0; i < mapGraph.nodeCount; i+=10) {
            Node n = mapGraph.nodes[i];
            points.add(new GeoPosition(((IntersectionNode)n).latitudes, ((IntersectionNode)n).longitudes));
        }
        mainFrame.plotPoints(points);
    }

    public void plotNodesOnMap(int[] nodeIDs) {
        HashSet<GeoPosition> points = new HashSet<>();
        for (int id : nodeIDs) {
            Node n = mapGraph.nodes[id];
            points.add(new GeoPosition(((IntersectionNode)n).latitudes, ((IntersectionNode)n).longitudes));
        }
        mainFrame.plotPoints(points);
    }

    public void plotRoadOnMap(int startNodeID, int endNodeID, AlgorithmType algType, PriorityType priType) {
        System.out.println("Seraching for road between " + startNodeID + " and " + endNodeID + " using " + algType);

        Node endNode = null;
        switch (algType) {
            case ALT ->  endNode = mapGraph.ALTAlgorithm(startNodeID, endNodeID, priType);
            case Dijkstras -> endNode = mapGraph.dijkstrasAlgorithm(startNodeID, endNodeID, priType);
        }

        int distance = ((IntersectionPredecessor)endNode.predecessor).distance;
        int time = ((IntersectionPredecessor)endNode.predecessor).time / 100;
        System.out.println("Found road from " + mapGraph.nodes[startNodeID] + " to " + endNode);

        HashSet<GeoPosition> points = new HashSet<>();
        while (endNode.predecessor.predecessor!=null) {
            endNode = endNode.predecessor.predecessor;
            points.add(new GeoPosition(((IntersectionNode)endNode).latitudes, ((IntersectionNode)endNode).longitudes));
        }

        mainFrame.plotPoints(points);
        mainFrame.setLengthLabel(distance/1000f + " km");
        int hours = time / 3600;
        int minutes = (time % 3600) / 60;
        int seconds = time%60;

        mainFrame.setTimeLabel(String.format("%d:%02d:%02d", hours, minutes, seconds));
    }
}
