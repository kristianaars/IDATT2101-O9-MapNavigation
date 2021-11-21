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
        mainFrame = new MainFrame();
        mapGraph = MapGraph.buildFromInputStream(new FileInputStream(intersectioNodeFile), new FileInputStream(roadEdgeFile));

        mainFrame.setVisible(true);

        Node endNode = mapGraph.dijkstrasAlgorithm(5434, 5873);
        System.out.println("End: " + endNode);
        HashSet<GeoPosition> points = new HashSet<>();

        while (endNode.predecessor.predecessor!=null) {
            endNode = endNode.predecessor.predecessor;
            points.add(new GeoPosition(((IntersectionNode)endNode).latitudes, ((IntersectionNode)endNode).longitudes));
        }
        System.out.println("Start: " + endNode);
        mainFrame.plotPoints(points);

        System.out.println("Found road...");
    }
}
