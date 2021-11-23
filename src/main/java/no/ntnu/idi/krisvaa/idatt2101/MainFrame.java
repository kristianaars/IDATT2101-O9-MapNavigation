package no.ntnu.idi.krisvaa.idatt2101;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.google.GoogleMapsTileFactoryInfo;
import org.jxmapviewer.input.CenterMapListener;
import org.jxmapviewer.input.PanKeyListener;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCursor;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.viewer.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class MainFrame extends JFrame {

    private JTextField startNodeTextField;
    private JTextField endNodeTextField;
    private JButton ALTAlgorithmButton;
    private JButton dijkstrasAlgorithmButton;
    private JComboBox<PriorityType> prioritySelector;

    private JTextField lengthTextField;
    private JTextField timeTextField;
    private JTextField startPosition;
    private JTextField endPosition;
    private JTextField priorityType;
    private JTextField numberOfNodes;
    private JTextField algorithmType;
    private JTextField algorithmExecutionTime;

    private MapPanel mapPanel;

    private MapController mapController;

    public MainFrame(MapController mapController) {
        super();

        this.mapController = mapController;

        startNodeTextField = new JTextField();
        startNodeTextField.setPreferredSize(new Dimension(85, 25));
        endNodeTextField = new JTextField();
        endNodeTextField.setPreferredSize(new Dimension(85, 25));
        ALTAlgorithmButton = new JButton("ALT-Algorithm");
        dijkstrasAlgorithmButton = new JButton("Dijkstras Algorithm");
        prioritySelector = new JComboBox<PriorityType>(PriorityType.values());

        lengthTextField = new JTextField();
        timeTextField = new JTextField();
        startPosition = new JTextField();
        endPosition = new JTextField();
        priorityType = new JTextField();
        numberOfNodes = new JTextField();
        algorithmType = new JTextField();
        algorithmExecutionTime = new JTextField();

        lengthTextField.setMaximumSize(new Dimension(10000, 30));
        lengthTextField.setEditable(false);
        lengthTextField.setBackground(this.getBackground());

        timeTextField.setMaximumSize(new Dimension(10000, 30));
        timeTextField.setEditable(false);
        timeTextField.setBackground(this.getBackground());

        startPosition.setMaximumSize(new Dimension(10000, 30));
        startPosition.setEditable(false);
        startPosition.setBackground(this.getBackground());

        endPosition.setMaximumSize(new Dimension(10000, 30));
        endPosition.setEditable(false);
        endPosition.setBackground(this.getBackground());

        priorityType.setMaximumSize(new Dimension(10000, 30));
        algorithmType.setEditable(false);
        priorityType.setBackground(this.getBackground());

        numberOfNodes.setMaximumSize(new Dimension(10000, 30));
        numberOfNodes.setEditable(false);
        numberOfNodes.setBackground(this.getBackground());

        algorithmType.setMaximumSize(new Dimension(10000, 30));
        algorithmType.setEditable(false);
        algorithmType.setBackground(this.getBackground());

        algorithmExecutionTime.setMaximumSize(new Dimension(10000, 30));
        algorithmExecutionTime.setEditable(false);
        algorithmExecutionTime.setBackground(this.getBackground());

        dijkstrasAlgorithmButton.addActionListener(e -> {
            initiateMapSearch(AlgorithmType.Dijkstras);
        });

        ALTAlgorithmButton.addActionListener(e -> {
            initiateMapSearch(AlgorithmType.ALT);
        });

        prioritySelector.addActionListener(e -> {
            PriorityType priType = (PriorityType) prioritySelector.getSelectedItem();

            switch (priType) {
                case GasStations, CharningStations -> {ALTAlgorithmButton.setEnabled(false); endNodeTextField.setEnabled(false); endNodeTextField.setBackground(this.getBackground());}
                case Length -> {ALTAlgorithmButton.setEnabled(false); endNodeTextField.setEnabled(true); endNodeTextField.setBackground(Color.WHITE);}
                case Time -> {ALTAlgorithmButton.setEnabled(true); endNodeTextField.setEnabled(true); endNodeTextField.setBackground(Color.WHITE);}
            }

        });

        prioritySelector.setSelectedItem(PriorityType.Time);

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

        JLabel infoPanelHeader = new JLabel("Route information");
        infoPanelHeader.setFont(new Font("Serif", Font.BOLD, 18));

        JLabel startPositionLabel = new JLabel("Start-position");
        startPositionLabel.setFont(new Font("Serif", Font.BOLD, 14));
        startPositionLabel.setSize(120, 25);

        JLabel endPositionLabel = new JLabel("End-position");
        endPositionLabel.setFont(new Font("Serif", Font.BOLD, 14));
        endPositionLabel.setSize(120, 25);

        JLabel lengthLabel = new JLabel("Length");
        lengthLabel.setFont(new Font("Serif", Font.BOLD, 14));
        lengthLabel.setSize(120, 25);

        JLabel timeLabel = new JLabel("Duration");
        timeLabel.setFont(new Font("Serif", Font.BOLD, 14));
        timeLabel.setSize(120, 25);

        JLabel algorithmInfoHeader = new JLabel("Algorithm information:");
        algorithmInfoHeader.setFont(new Font("Serif", Font.BOLD, 18));

        JLabel algorithmTypeLabel = new JLabel("Algorithm-type");
        algorithmTypeLabel.setFont(new Font("Serif", Font.BOLD, 14));
        algorithmTypeLabel.setSize(120, 25);

        JLabel numberOfNodesLabel = new JLabel("Visited nodes");
        numberOfNodesLabel.setFont(new Font("Serif", Font.BOLD, 14));
        numberOfNodesLabel.setSize(120, 25);

        JLabel algorithmExecutionTimeLabel = new JLabel("Execuition time");
        algorithmExecutionTimeLabel.setFont(new Font("Serif", Font.BOLD, 14));
        algorithmExecutionTimeLabel.setSize(120, 25);

        JLabel priorityTypeLabel = new JLabel("Priority type");
        priorityTypeLabel.setFont(new Font("Serif", Font.BOLD, 14));
        priorityTypeLabel.setSize(120, 25);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.PAGE_AXIS));
        infoPanel.add(Box.createVerticalStrut(20));
        infoPanel.add(infoPanelHeader);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(startPositionLabel);
        infoPanel.add(startPosition);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(endPositionLabel);
        infoPanel.add(endPosition);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(lengthLabel);
        infoPanel.add(lengthTextField);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(timeLabel);
        infoPanel.add(timeTextField);

        infoPanel.add(Box.createVerticalStrut(20));
        infoPanel.add(algorithmInfoHeader);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(algorithmTypeLabel);
        infoPanel.add(algorithmType);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(numberOfNodesLabel);
        infoPanel.add(numberOfNodes);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(algorithmExecutionTimeLabel);
        infoPanel.add(algorithmExecutionTime);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(priorityTypeLabel);
        infoPanel.add(priorityType);

        infoPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        infoPanel.setPreferredSize(new Dimension(250, 0));

        mapPanel = new MapPanel();
        mapPanel.setBorder(new LineBorder(Color.LIGHT_GRAY));

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        mainPanel.add(toolbar, BorderLayout.PAGE_START);
        mainPanel.add(infoPanel, BorderLayout.LINE_START);
        mainPanel.add(mapPanel, BorderLayout.CENTER);
        this.setContentPane(mainPanel);


        this.setMinimumSize(new Dimension(1200, 800));
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    private void initiateMapSearch(AlgorithmType algType) {
        int startNodeID = Integer.parseInt(startNodeTextField.getText());
        int endNodeID = 0;
        if(endNodeTextField.isEnabled()) endNodeID = Integer.parseInt(endNodeTextField.getText());;
        PriorityType priorityType = (PriorityType)prioritySelector.getSelectedItem();
        mapController.plotRoadOnMap(startNodeID, endNodeID, algType, priorityType);
    }

    public void plotPoints(Set<GeoPosition> waypoints) { mapPanel.plotPoints(waypoints); }

    public void setRouteStats(String startPosition, String endPosition, String length, String duration) {
        this.startPosition.setText(startPosition);
        this.endPosition.setText(endPosition);
        this.lengthTextField.setText(length);
        this.timeTextField.setText(duration);
    }

    public void setAlgorithmStats(String algorithmType, String visitedNodes, String executionTime, String priorityType) {
        this.algorithmType.setText(algorithmType);
        this.numberOfNodes.setText(visitedNodes);
        this.algorithmExecutionTime.setText(executionTime);
        this.priorityType.setText(priorityType);
    }
}

class MapPanel extends JXMapViewer {

    private WaypointPainter<Waypoint> waypointPainter;
    private ActivityPainter activityPainter;

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

        this.setZoom(15);
        this.setAddressLocation(new GeoPosition(66.0017187, -16.5130859));
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

class ActivityPainter implements Painter<MapPanel> {

    private ArrayList<GeoPosition> points;

    public ActivityPainter(ArrayList<GeoPosition> points) {
        this.points = points;
    }

    @Override
    public void paint(Graphics2D g, MapPanel map, int width, int height) {
        g = (Graphics2D) g.create();

        // convert from viewport to world bitmap
        Rectangle rect = map.getViewportBounds();
        g.translate(-rect.x, -rect.y);

        //g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // do the drawing
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(4));

        for(GeoPosition gp : points) {
            Point2D pt = map.getTileFactory().geoToPixel(gp, map.getZoom());

            g.drawRect((int) pt.getX(), (int) pt.getY(), 1, 1);
        }

        g.dispose();
    }
}