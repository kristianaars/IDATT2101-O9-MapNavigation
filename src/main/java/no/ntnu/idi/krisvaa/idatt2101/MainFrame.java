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
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;

public class MainFrame extends JFrame {

    private JTextField startNodeTextField;
    private JTextField endNodeTextField;
    private JButton ALTAlgorithmButton;
    private JButton dijkstrasAlgorithmButton;
    private JComboBox<String> prioritySelector;

    private JLabel lengthLabel;
    private JLabel timeLabel;

    private MapPanel mapPanel;

    private MapController mapController;

    public MainFrame(MapController mapController) {
        super();

        this.mapController = mapController;

        startNodeTextField = new JTextField();
        startNodeTextField.setPreferredSize(new Dimension(60, 25));
        endNodeTextField = new JTextField();
        endNodeTextField.setPreferredSize(new Dimension(60, 25));
        ALTAlgorithmButton = new JButton("ALT-Algorithm");
        dijkstrasAlgorithmButton = new JButton("Dijkstras Algorithm");
        prioritySelector = new JComboBox<>(new String[]{"Travel Time", "Road Length"});

        lengthLabel = new JLabel();
        lengthLabel.setSize(120, 25);
        setLengthLabel("");
        timeLabel = new JLabel();
        timeLabel.setSize(120, 25);
        setTimeLabel("");

        ALTAlgorithmButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                initiateMapSearch();
            }
        });

        JPanel toolbar = new JPanel();
        toolbar.setLayout(new FlowLayout());
        toolbar.add(new JLabel("Start node number: "));
        toolbar.add(startNodeTextField);
        toolbar.add(new JLabel("End node number: "));
        toolbar.add(endNodeTextField);
        toolbar.add(new JLabel("Prioritize: "));
        toolbar.add(prioritySelector);
        toolbar.add(new JLabel("Find road using: "));
        toolbar.add(ALTAlgorithmButton);
        toolbar.add(dijkstrasAlgorithmButton);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new FlowLayout());
        infoPanel.add(lengthLabel);
        infoPanel.add(timeLabel);
        infoPanel.setPreferredSize(new Dimension(0, 35));

        mapPanel = new MapPanel();

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        mainPanel.add(toolbar, BorderLayout.NORTH);
        mainPanel.add(infoPanel, BorderLayout.SOUTH);
        mainPanel.add(mapPanel, BorderLayout.CENTER);
        this.setContentPane(mainPanel);

        this.setSize(1000, 600);
        this.setMinimumSize(new Dimension(1050, 600));
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    private void initiateMapSearch() {
        int startNodeID = Integer.parseInt(startNodeTextField.getText());
        int endNodeID = Integer.parseInt(endNodeTextField.getText());
        mapController.plotRoadOnMap(startNodeID, endNodeID, null, null);
    }

    public void plotPoints(Set<GeoPosition> waypoints) { mapPanel.plotPoints(waypoints); }

    public void setTimeLabel(String time) { timeLabel.setText("Time: " + time); }

    public void setLengthLabel(String length) { lengthLabel.setText("Length: " + length);}
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
