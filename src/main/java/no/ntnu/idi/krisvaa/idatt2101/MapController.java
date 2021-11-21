package no.ntnu.idi.krisvaa.idatt2101;

import org.jxmapviewer.viewer.GeoPosition;

import java.io.*;
import java.util.Arrays;
import java.util.HashSet;

public class MapController {

    public static void main(String[] args) throws IOException {
        MapController mc = new MapController("norden_noder.txt", "norden_kanter.txt");
    }

    private MainFrame mainFrame;
    private MapGraph mapGraph;

    public MapController(String intersectioNodeFile, String roadEdgeFile) throws IOException {
        System.out.println("Loading map-data...");
        mapGraph = MapGraph.buildFromInputStream(new FileInputStream(intersectioNodeFile), new FileInputStream(roadEdgeFile));

        System.out.println("Loading landmarks...");
        mapGraph.loadLandmarks(new FileInputStream("island_landemerker.bin"));

        System.out.println("Map-data finished loading!");

        mainFrame = new MainFrame(this);

        //mapGraph.generateLandmarks(new int[]{6, 55, 20030, 34});
        //mapGraph.saveLandmarks(new FileOutputStream("island_landemerker.bin"));

        mapGraph.loadLandmarks(new FileInputStream("island_landemerker.bin"));

        mainFrame.setVisible(true);

        plotAllPointsOnMap();
    }

    private void plotAllPointsOnMap() {
        HashSet<GeoPosition> points = new HashSet<>();
        for (int i = 0; i < mapGraph.nodeCount; i+=10) {
            Node n = mapGraph.nodes.get(i);
            points.add(new GeoPosition(((IntersectionNode)n).latitudes, ((IntersectionNode)n).longitudes));
        }
        mainFrame.plotPoints(points);
    }

    public void plotRoadOnMap(int startNodeID, int endNodeID, AlgorithmType algType, PriorityType priType) {
        System.out.println("Seraching for road between " + startNodeID + " and " + endNodeID);
        Node endNode = mapGraph.dijkstrasAlgorithm(startNodeID, endNodeID);
        int distance = ((IntersectionPredecessor)endNode.predecessor).distance;
        int time = ((IntersectionPredecessor)endNode.predecessor).time / 100;
        System.out.println("Found road from " + mapGraph.nodes.get(startNodeID) + " to " + endNode);

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
