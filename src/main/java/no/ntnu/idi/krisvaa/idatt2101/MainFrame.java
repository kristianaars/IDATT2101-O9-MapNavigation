package no.ntnu.idi.krisvaa.idatt2101;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.google.GoogleMapsTileFactoryInfo;
import org.jxmapviewer.input.CenterMapListener;
import org.jxmapviewer.input.PanKeyListener;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCursor;
import org.jxmapviewer.viewer.*;

import javax.swing.*;
import javax.swing.event.MouseInputListener;
import java.util.HashSet;
import java.util.Set;

public class MainFrame extends JFrame {

    private MapPanel mapPanel;

    public MainFrame() {
        super();

        mapPanel = new MapPanel();

        this.getContentPane().add(mapPanel);

        this.setSize(800, 600);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    public void plotPoints(Set<GeoPosition> waypoints) {
        mapPanel.plotPoints(waypoints);
    }

}

class MapPanel extends JXMapViewer {

    private WaypointPainter<Waypoint> waypointPainter;

    public MapPanel() {
        super();

        TileFactoryInfo info = new OSMTileFactoryInfo();
        DefaultTileFactory tileFactory = new DefaultTileFactory(info);
        this.setTileFactory(tileFactory);

        MouseInputListener mia = new PanMouseInputListener(this);
        this.addMouseListener(mia);
        this.addMouseMotionListener(mia);
        tileFactory.setThreadPoolSize(8);

        this.addMouseListener(new CenterMapListener(this));

        this.addMouseWheelListener(new ZoomMouseWheelListenerCursor(this));

        this.addKeyListener(new PanKeyListener(this));

        waypointPainter = new WaypointPainter<>();
        this.setOverlayPainter(waypointPainter);

        this.setZoom(1);
    }

    public void plotPoints(Set<GeoPosition> points) {
        HashSet<Waypoint> wp = new HashSet<>();
        for(GeoPosition gp : points) {
            wp.add(new DefaultWaypoint(gp));
        }
        waypointPainter.setWaypoints(wp);
        repaint();
    }
}
