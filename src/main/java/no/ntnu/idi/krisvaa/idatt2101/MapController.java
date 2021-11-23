package no.ntnu.idi.krisvaa.idatt2101;

import org.jxmapviewer.viewer.GeoPosition;

import java.io.*;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;

public class MapController {

    public static void main(String[] args) throws IOException {
        MapController mc = new MapController(
                "norden_noder.txt",
                "norden_kanter.txt",
                "norden_landemerker.lm",
                "norden_interessepkt.txt",
                new int[]{2151398, 2724481, 251295, 1045921, 3209493, 6579443, 5492224, 6487468}
        );
    }

    private MainFrame mainFrame;
    private MapGraph mapGraph;

    public MapController(String intersectioNodeFile, String roadEdgeFile, String landmarkFile, String pointsOfInterestFile, int[] landmarkNodes) throws IOException {
        if(!new File(intersectioNodeFile).exists()) {
            System.out.println("Unable to find intersection node file with filename " + intersectioNodeFile + "... Starting download.");
            downloadFile("http://www.iie.ntnu.no/fag/_alg/Astjerne/opg/norden/noder.txt", intersectioNodeFile);
        }

        if(!new File(roadEdgeFile).exists()) {
            System.out.println("Unable to find road edge file with filename " + roadEdgeFile + "... Starting download.");
            downloadFile("http://www.iie.ntnu.no/fag/_alg/Astjerne/opg/norden/kanter.txt", roadEdgeFile);
        }

        if(!new File(pointsOfInterestFile).exists()) {
            System.out.println("Unable to find point of interest file with filename " + roadEdgeFile + "... Starting download.");
            downloadFile("http://www.iie.ntnu.no/fag/_alg/Astjerne/opg/norden/interessepkt.txt", pointsOfInterestFile);
        }

        System.out.println("Loading map-data...");
        mapGraph = MapGraph.buildFromInputStream(new FileInputStream(intersectioNodeFile), new FileInputStream(roadEdgeFile), new FileInputStream(pointsOfInterestFile));

        System.out.println("Loading landmarks...");
        loadLandmarks(mapGraph, landmarkNodes, landmarkFile);

        System.out.println("Map-data finished loading!");

        mainFrame = new MainFrame(this);

        mainFrame.setVisible(true);

        plotNodesOnMap(landmarkNodes);
    }

    public void downloadFile(String url, String fileOutput) {
        System.out.println("Initiating download... Saving " + url + " as " + fileOutput);

        try (BufferedInputStream in = new BufferedInputStream(new URL(url).openStream());
             FileOutputStream fileOutputStream = new FileOutputStream(fileOutput)) {
            byte dataBuffer[] = new byte[1024];
            int bytesWritten = 0;
            int bytesRead;

            long lastPrint = System.currentTimeMillis();

            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                if(System.currentTimeMillis() - lastPrint > 1000) {
                    lastPrint = System.currentTimeMillis();
                    System.out.println("Downloaded " + (bytesWritten)/1000000f + " MB from " + url);
                }
                bytesWritten+=1024;
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }

            System.out.println("Download complete! " + (bytesWritten)/1000000f + " MB saved to " + fileOutput);
        } catch (IOException e) {
            // handle exception
        }

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

    public void plotLatestActivity() {
        boolean[] activity = mapGraph.lastSearch;
        HashSet<GeoPosition> set = new HashSet<GeoPosition>();

        for(int i = 0; i < activity.length; i++) {
            if(activity[i]) {
                IntersectionNode n = (IntersectionNode) mapGraph.nodes[i];
                set.add(new GeoPosition(n.latitudes, n.longitudes));
            }
        }

        mainFrame.plotPoints(set);
    }

    public void plotRoadOnMap(int startNodeID, int endNodeID, AlgorithmType algType, PriorityType priType) {
        System.out.println("Seraching for road between " + startNodeID + " and " + endNodeID + " using " + algType);

        long startTime = System.currentTimeMillis();
        Node endNode = null;
        switch (algType) {
            case ALT ->  endNode = mapGraph.ALTAlgorithm(startNodeID, endNodeID, priType);
            default -> endNode = mapGraph.dijkstrasAlgorithm(startNodeID, endNodeID, priType);
        }
        long elapsedExecutionTime = System.currentTimeMillis() - startTime;

        System.out.println("Found road from " + mapGraph.nodes[startNodeID] + " to " + endNode);

        String distance = "N/A";
        String travelDuration = "N/A";
        String endPosition = "N/A";
        String startPosition = "N/A";

        HashSet<GeoPosition> points = new HashSet<>();
        if(endNode==null) {
            for(Node n : mapGraph.lastSpecialSearchResult) {
                points.add(new GeoPosition(((IntersectionNode)n).latitudes, ((IntersectionNode)n).longitudes));
            }
        } else {
            distance = String.format("%.2f km", ((IntersectionPredecessor)endNode.predecessor).distance / 1000f);
            travelDuration = secondsToTime(((IntersectionPredecessor)endNode.predecessor).time / 100);
            endPosition = ((IntersectionNode)endNode).latitudes + ", " + ((IntersectionNode)endNode).longitudes;

            while (endNode.predecessor.predecessor!=null) {
                endNode = endNode.predecessor.predecessor;
                points.add(new GeoPosition(((IntersectionNode)endNode).latitudes, ((IntersectionNode)endNode).longitudes));
            }

            startPosition = ((IntersectionNode)endNode).latitudes + ", " + ((IntersectionNode)endNode).longitudes;
        }

        String algorithmType = algType.toString();
        String priorityType = priType.toString();
        String nodeCount = mapGraph.lastNodeCount + " nodes visited";
        String executionTime = String.format("%d ms", elapsedExecutionTime);

        mainFrame.setRouteStats(startPosition, endPosition, distance, travelDuration);
        mainFrame.setAlgorithmStats(algorithmType, nodeCount, executionTime, priorityType);
        mainFrame.plotPoints(points);
        //plotLatestActivity();
    }

    public static String secondsToTime(int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int sec = seconds%60;

        return String.format("%d:%02d:%02d", hours, minutes, sec);
    }
}
