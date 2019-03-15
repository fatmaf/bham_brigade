// ===============================================================================
// Authors: Jacob Allex-Buckner
// Organization: University of Dayton Research Institute Applied Sensing Division
//
// Copyright (c) 2018 Government of the United State of America, as represented by
// the Secretary of the Air Force.  No copyright is claimed in the United States under
// Title 17, U.S. Code.  All Other Rights Reserved.
// ===============================================================================

// This file was auto-created by LmcpGen. Modifications will be overwritten.

import afrl.cmasi.searchai.HazardZoneDetection;
import afrl.cmasi.searchai.HazardZoneEstimateReport;
import afrl.cmasi.AirVehicleState;
import afrl.cmasi.AltitudeType;
import afrl.cmasi.CommandStatusType;
import afrl.cmasi.GimbalStareAction;
import afrl.cmasi.GoToWaypointAction;
import afrl.cmasi.Location3D;
import afrl.cmasi.LoiterAction;
import afrl.cmasi.LoiterDirection;
import afrl.cmasi.LoiterType;
import afrl.cmasi.MissionCommand;
import afrl.cmasi.NavigationAction;
import afrl.cmasi.SessionStatus;
import afrl.cmasi.SpeedType;
import afrl.cmasi.TurnType;
import afrl.cmasi.VehicleAction;
import afrl.cmasi.VehicleActionCommand;
import afrl.cmasi.Waypoint;
import afrl.cmasi.Polygon;
import afrl.cmasi.Circle;
import avtas.lmcp.LMCPFactory;
import avtas.lmcp.LMCPObject;

import java.awt.Point;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Connects to the simulation and sends a fake mission command to every UAV that
 * is requested in the plan request.
 */
public class UCTClient extends Thread {

	/** simulation TCP port to connect to */
	private static int port = 5555;
	/** address of the server */
	private static String host = "localhost";
	/** Array of booleans indicating if loiter command has been sent to each UAV */
	boolean[] uavsLoiter = new boolean[4];
	Polygon estimatedHazardZone = new Polygon();
	Socket socket;
	// <SimulationView LongExtent="0.18987491679164492" Latitude="53.46878761863578"
	// Longitude="-1.7973158725175726"/>
	// Lat: 53.4458 Lon: -1.8354 Alt: 524 m
	// Lat: 53.4442 Lon: -1.7551 Alt: 347 m
	// Lat: 53.4931 Lon: -1.8388 Alt: 298 m
	// Lat: 53.492 Lon: -1.754 Alt: 426 m

	double lat_low = 53.445192256636574;// 53.444;
	double lat_high = 53.496089786696714;// 53.492;
	double lon_high = -1.8348498429921174;// -1.8354;
	double lon_low = -1.7534335596466306;// -1.754;
	// lat = y
	// lon = x
	int numCellsSide = 20;

	double cellRadiusLat = 0;
	double cellRadiusLon = 0;
	int numActions = 9;
	int numUAVs = 0;

	enum action {
		up, down, left, right, up_right, up_left, down_right, down_left, stay
	};

	HashMap<Long, Location3D> uavLocations;
	HashMap<Long, Point> uavCells;

	public UCTClient() {
		cellRadiusLat = (lat_high - lat_low) / (double) numCellsSide;
		cellRadiusLon = (lon_high - lon_low) / (double) numCellsSide;
		System.out.println("Radius - Lat: " + cellRadiusLat);
		System.out.println("Radius - Lon: " + cellRadiusLon);
		uavLocations = new HashMap<Long, Location3D>();
		uavCells = new HashMap<Long, Point>();
	}

	String cellToString(Point cell) {
		return "(" + cell.getX() + "," + cell.getY() + ")";
	}

	String locToString(Location3D loc) {
		return "[lon:" + loc.getLongitude() + ",lat:" + loc.getLatitude() + ",alt:" + loc.getAltitude() + "]";
	}

	Point uavCell(long id, Location3D loc) {
		// max cells = numCellsSide*numCellsSide

		Point toret = locCell(loc);
		uavCells.put(id, toret);
		System.out.println("UAV " + id + " cell: " + cellToString(toret));
		return toret;

	}

	Point locCell(Location3D loc) {
		int xloc = (int) ((loc.getLongitude() - lon_low) / cellRadiusLon);
		int yloc = (int) ((loc.getLatitude() - lat_low) / cellRadiusLat);

		// si*i + sj*j
		Point toret = new Point(xloc, yloc);
		return toret;
	}

	// cell to location
	Location3D cellToLoc(Point cell) {
		Location3D location = new Location3D();
		double lat = ((double) cell.y) * cellRadiusLat + lat_low;
		double lon = ((double) cell.x) * cellRadiusLon + lon_low;
		location.setLongitude(lon);
		location.setLatitude(lat);
		System.out.println("Cell " + cellToString(cell) + " = [lon:" + lon + " " + (lon + cellRadiusLon) + ",lat:" + lat
				+ " " + (lat + cellRadiusLat) + "]");
		return location;
	}

	void addUAV(long id, Location3D loc) {
		uavLocations.put(id, loc);
		numUAVs++;
		System.out.println("Added uav=" + id + " ,total uavs = " + numUAVs);
	}

	void updateLocation(long id, Location3D loc) {
		if (uavLocations.containsKey(id)) {
			uavLocations.put(id, loc);
		} else {
			addUAV(id, loc);
		}
		Point cell = uavCell(id, loc);
		System.out.println("Location: " + locToString(loc));

		System.out.println("In Cell: " + locInCell(loc, cell));

	}

	void moveUAV(OutputStream out, long id, action act) {
		Location3D newloc = movement(uavLocations.get(id), act);
		try {
			sendLoiterCommand(out, id, newloc);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	void moveUAV(long id, action act) {
		try {
			moveUAV(socket.getOutputStream(), id, act);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	void moveUAV(long id, Point cell) {
		try {
			moveUAV(socket.getOutputStream(), id, cell);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	void moveUAV(OutputStream out, long id, Point cell) {
		if (!locInCell(uavLocations.get(id), cell)) {
			Location3D loc = cellToLoc(cell);
			try {
				sendLoiterCommand(out, id, loc);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

			}
		}
	}

	boolean locInCell(Location3D loc, Point cell) {
		Location3D testLoc = cellToLoc(cell);
		boolean isLatInLimit = loc.getLatitude() > testLoc.getLatitude()
				&& loc.getLatitude() < (testLoc.getLatitude() + cellRadiusLat);
		boolean isLonInLimit = loc.getLongitude() < testLoc.getLongitude()
				&& loc.getLongitude() > (testLoc.getLongitude() + cellRadiusLon);
		return (isLatInLimit && isLonInLimit);
	}

	Location3D movement(Location3D base, action act) {
		Location3D newLoc = base;
		double newLat = newLoc.getLatitude();
		double newLon = newLoc.getLongitude();
		double latAdd = cellRadiusLat;
		double lonAdd = cellRadiusLon;
		switch (act) {
		case up:
			lonAdd = 0;

			break;
		case down:
			lonAdd = 0;
			latAdd *= -1.0;
			break;
		case left:
			latAdd = 0;
			break;
		case right:
			latAdd = 0;
			lonAdd *= -1.0;
			break;
		case up_right:
			lonAdd *= -1.0;
			break;
		case up_left:
			break;
		case down_right:
			latAdd *= -1.0;
			lonAdd *= -1.0;
			break;
		case down_left:
			latAdd *= -1.0;
			break;

		default:
			break;

		}
		newLoc.setLatitude(newLat + latAdd);
		newLoc.setLongitude(newLon + lonAdd);
		return newLoc;
	}

	@Override
	public void run() {
		try {
			// connect to the server
			socket = connect(host, port);

			while (true) {
				// Continually read the LMCP messages that AMASE is sending out
				readMessages(socket.getInputStream(), socket.getOutputStream());
			}

		} catch (Exception ex) {
			Logger.getLogger(UCTClient.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * Sends loiter command to the AMASE Server
	 * 
	 * @param out
	 * @throws Exception
	 */
	public void sendLoiterCommand(OutputStream out, long vehicleId, Location3D location) throws Exception {
		// Setting up the mission to send to the UAV
		VehicleActionCommand o = new VehicleActionCommand();
		o.setVehicleID(vehicleId);
		o.setStatus(CommandStatusType.Pending);
		o.setCommandID(1);

		// Setting up the loiter action
		LoiterAction loiterAction = new LoiterAction();
		loiterAction.setLoiterType(LoiterType.Circular);
		loiterAction.setRadius(250);
		loiterAction.setAxis(0);
		loiterAction.setLength(0);
		loiterAction.setDirection(LoiterDirection.Clockwise);
		loiterAction.setDuration(100000);
		loiterAction.setAirspeed(15);

		// Creating a 3D location object for the stare point
		loiterAction.setLocation(location);

		// Adding the loiter action to the vehicle action list
		o.getVehicleActionList().add(loiterAction);

		// Sending the Vehicle Action Command message to AMASE to be interpreted
		out.write(avtas.lmcp.LMCPFactory.packMessage(o, true));
	}

	/**
	 * Sends loiter command to the AMASE Server
	 * 
	 * @param out
	 * @throws Exception
	 */
	public void sendEstimateReport(OutputStream out, Polygon estimatedShape) throws Exception {
		// Setting up the mission to send to the UAV
		HazardZoneEstimateReport o = new HazardZoneEstimateReport();
		o.setEstimatedZoneShape(estimatedShape);
		o.setUniqueTrackingID(1);
		o.setEstimatedGrowthRate(0);
		o.setPerceivedZoneType(afrl.cmasi.searchai.HazardType.Fire);
		o.setEstimatedZoneDirection(0);
		o.setEstimatedZoneSpeed(0);

		// Sending the Vehicle Action Command message to AMASE to be interpreted
		out.write(avtas.lmcp.LMCPFactory.packMessage(o, true));
	}

	/**
	 * Reads in messages being sent out by the AMASE Server
	 */
	public void readMessages(InputStream in, OutputStream out) throws Exception {
		// Use each of the if statements to use the incoming message
		LMCPObject o = LMCPFactory.getObject(in);
		// Check if the message is a HazardZoneDetection
		if (o instanceof afrl.cmasi.searchai.HazardZoneDetection) {
			HazardZoneDetection hazardDetected = ((HazardZoneDetection) o);
			// Get location where zone first detected
			Location3D detectedLocation = hazardDetected.getDetectedLocation();
			// Get entity that detected the zone
			int detectingEntity = (int) hazardDetected.getDetectingEnitiyID();

			// Check if hint
			if (detectingEntity == 0) {
				// Do nothing for now, hints will be added later
				return;
			}

			// Check if the UAV has already been sent the loiter command and proceed if it
			// hasn't
			if (uavsLoiter[detectingEntity - 1] == false) {
				// Send the loiter command
				sendLoiterCommand(out, detectingEntity, detectedLocation);

				// Note: Polygon points must be in clockwise or counter-clockwise order to get a
				// shape without intersections
				estimatedHazardZone.getBoundaryPoints().add(detectedLocation);

				// Send out the estimation report to draw the polygon
				sendEstimateReport(out, estimatedHazardZone);

				uavsLoiter[detectingEntity - 1] = true;
				System.out.println("UAV" + detectingEntity + " detected hazard at " + detectedLocation.getLatitude()
						+ "," + detectedLocation.getLongitude() + ". Sending loiter command.");
			}
		} else if (o instanceof afrl.cmasi.AirVehicleState) {
			AirVehicleState uavState = ((AirVehicleState) o);
			long id = uavState.getID();
			Location3D loc = uavState.getLocation();
			updateLocation(id, loc);
			// send uav1 up
			// send uav2 left
			// send uav3 up left
			switch ((int) (id)) {

			case 3:

//				moveUAV(id, action.right);
				moveUAV(id, new Point(0, 0));
				break;
			case 4:
//				moveUAV(id, action.down);
				moveUAV(id, new Point(numCellsSide, 0));
				break;
			case 2:
//				moveUAV(id, action.left);
				moveUAV(id, new Point(0, numCellsSide));
				break;
			case 1:
//				moveUAV(id, action.up);
				moveUAV(id, new Point(numCellsSide, numCellsSide));
				break;
			default:
				break;

			}
		}
//        String xmlVersion = "";
//       xmlVersion= o.toXML(xmlVersion);
//        System.out.println(xmlVersion);
	}

	 /**
     * Sends mission command to the AMASE Server
     * @param out
     * @throws Exception
     */
    public void sendMissionCommand(OutputStream out, long id, Location3D loc) throws Exception {
        VehicleActionCommand o = new VehicleActionCommand();
        o.setVehicleID(id);
        o.setStatus(CommandStatusType.Pending);
        o.setCommandID(1);


        //Setting up the loiter action
        LoiterAction loiterAction = new LoiterAction();
        loiterAction.setLoiterType(LoiterType.Circular);
        loiterAction.setRadius(250);
        loiterAction.setAxis(0);
        loiterAction.setLength(0);
        loiterAction.setDirection(LoiterDirection.Clockwise);
        loiterAction.setDuration(15000);
        loiterAction.setAirspeed(15);

        //Creating a 3D location object f
        Location3D location = loc;
        loiterAction.setLocation(location);

        //Adding the loiter action to the vehicle action list
        o.getVehicleActionList().add(loiterAction);

        //Sending the Vehicle Action Command message to AMASE to be interpreted
        out.write(avtas.lmcp.LMCPFactory.packMessage(o, true));

 
    }

	/**
	 * tries to connect to the server. If there is a problem (such as the server not
	 * running yet) it pauses, then tries again. If the server quits and restarts,
	 * this method is called by the thread in order to re-establish communication.
	 * 
	 * @param host
	 * @param port
	 * @return
	 */
	public Socket connect(String host, int port) {
		Socket socket = null;
		try {
			socket = new Socket(host, port);
		} catch (UnknownHostException ex) {
			System.err.println("Host Unknown. Quitting");
			System.exit(0);
		} catch (IOException ex) {
			System.err.println("Could not Connect to " + host + ":" + port + ".  Trying again...");
			try {
				Thread.sleep(500);
			} catch (InterruptedException ex1) {
				Logger.getLogger(UCTClient.class.getName()).log(Level.SEVERE, null, ex1);
			}
			return connect(host, port);
		}
		System.out.println("Connected to " + host + ":" + port);
		return socket;
	}

	public static void main(String[] args) {
		new UCTClient().start();
	}
}
