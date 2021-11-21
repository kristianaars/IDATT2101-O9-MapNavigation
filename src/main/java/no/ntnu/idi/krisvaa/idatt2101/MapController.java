package no.ntnu.idi.krisvaa.idatt2101;

import org.jxmapviewer.viewer.GeoPosition;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;

public class MapController {

    public static void main(String[] args) throws IOException {
        MapController mc = new MapController("island_noder.txt", "island_kanter.txt");
    }

    private MainFrame mainFrame;
    private MapGraph mapGraph;

    public MapController(String intersectioNodeFile, String roadEdgeFile) throws IOException {
        mainFrame = new MainFrame(this);
        mapGraph = MapGraph.buildFromInputStream(new FileInputStream(intersectioNodeFile), new FileInputStream(roadEdgeFile));

        mainFrame.setVisible(true);
    }

    public void plotRoadOnMap(int startNodeID, int endNodeID, AlgorithmType algType, PriorityType priType) {
        System.out.println("Seraching for road between " + startNodeID + " and " + endNodeID);
        Node endNode = mapGraph.dijkstrasAlgorithm(startNodeID, endNodeID);

        HashSet<GeoPosition> points = new HashSet<>();
        while (endNode.predecessor.predecessor!=null) {
            endNode = endNode.predecessor.predecessor;
            points.add(new GeoPosition(((IntersectionNode)endNode).latitudes, ((IntersectionNode)endNode).longitudes));
        }

        mainFrame.plotPoints(points);
    }
}
