
// Authors: Jacob Allex-Buckner
// Organization: University of Dayton Research Institute Applied Sensing Division
//
// Copyright (c) 2018 Government of the United State of America, as represented by
// the Secretary of the Air Force.  No copyright is claimed in the United States under
// Title 17, U.S. Code.  All Other Rights Reserved.
// ===============================================================================

// This file was auto-created by LmcpGen. Modifications will be overwritten.

import java.awt.Point;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

import afrl.cmasi.AirVehicleState;
import afrl.cmasi.AltitudeType;
import afrl.cmasi.AreaSearchTask;
import afrl.cmasi.CommandStatusType;
import afrl.cmasi.KeepInZone;
import afrl.cmasi.Location3D;
import afrl.cmasi.LoiterAction;
import afrl.cmasi.LoiterDirection;
import afrl.cmasi.LoiterType;
import afrl.cmasi.MissionCommand;
import afrl.cmasi.Polygon;
import afrl.cmasi.Rectangle;
import afrl.cmasi.VehicleActionCommand;
import afrl.cmasi.WavelengthBand;
import afrl.cmasi.Waypoint;
import afrl.cmasi.searchai.HazardZone;
import afrl.cmasi.searchai.HazardZoneDetection;
import afrl.cmasi.searchai.HazardZoneEstimateReport;
import avtas.lmcp.LMCPFactory;
import avtas.lmcp.LMCPObject;

/**
 * Connects to the simulation and sends a fake mission command to every UAV that
 * is requested in the plan request.
 */
public class HDSClient extends Thread {

	enum action {
		down, down_left, down_right, left, right, stay, up, up_left, up_right
	}

	enum vehicleCommandPriority {
		hazard, search, waypoint
	}

	/** address of the server */
	private static String host = "localhost";

	/** simulation TCP port to connect to */
	private static int port = 5555;

	double cellRadiusLat = 0;

	double cellRadiusLon = 0;
	Polygon estimatedHazardZone = new Polygon();

	ArrayList<Location3D> hazardLocations;
	ArrayList<Point> hazardCells;
	ArrayList<Long> hazardDetectingEntity;
	ArrayList<Point> visitedCells;
	// Lat: 1.4882 Lon: -132.5493 Alt: 0 m
	// Lat: 1.5408 Lon: -132.4963 Alt: 0 m
	double lat_high = 1.5408;// 53.492;
	double lat_low = 1.4882;// 53.444;

	boolean limitsSetUsingKeepInZone = false;
	double lon_high = -132.5493;// -1.8354;
	double lon_low = -132.4963;// -1.754;
	int numActions = 9;

	// lat = y
	// lon = x
	int numCellsSide = 100;;

	int numHazardsDetected = 0;
	int numHazardsVisited = -1;
	int numUAVs = 0;
	public final double R_EARTHKM = 6372.8; // In kilometers
	Socket socket;
	// <SimulationView LongExtent="0.18987491679164492" Latitude="53.46878761863578"
	// Longitude="-1.7973158725175726"/>
	// Lat: 53.4458 Lon: -1.8354 Alt: 524 m
	// Lat: 53.4442 Lon: -1.7551 Alt: 347 m
	// Lat: 53.4931 Lon: -1.8388 Alt: 298 m
	// Lat: 53.492 Lon: -1.754 Alt: 426 m
	long speed = 35;

	HashMap<Long, Point> uavCells;

	HashMap<Long, Location3D> uavLocations;

	/** Array of booleans indicating if loiter command has been sent to each UAV */
	boolean[] uavsLoiter = new boolean[4];

	HashMap<Long, VehicleActionCommand> vehicleCommands;
	HashMap<Long, vehicleCommandPriority> vehicleCommandsPriority;

	public HDSClient() {
		cellRadiusLat = (lat_high - lat_low) / (double) numCellsSide;
		cellRadiusLon = (lon_high - lon_low) / (double) numCellsSide;
		System.out.println("Radius - Lat: " + cellRadiusLat);
		System.out.println("Radius - Lon: " + cellRadiusLon);
		uavLocations = new HashMap<Long, Location3D>();
		uavCells = new HashMap<Long, Point>();
		vehicleCommands = new HashMap<Long, VehicleActionCommand>();
		hazardLocations = new ArrayList<Location3D>();
		vehicleCommandsPriority = new HashMap<Long, vehicleCommandPriority>();
		hazardCells = new ArrayList<Point>();
		hazardDetectingEntity = new ArrayList<Long>();
		visitedCells = new ArrayList<Point>();

	}

	public static void main(String[] args) {
		new HDSClient().start();
	}

	void addUAV(long id, Location3D loc) {
		uavLocations.put(id, loc);
		numUAVs++;
		System.out.println("Added uav=" + id + " ,total uavs = " + numUAVs);
	}

	// cell to location
	Location3D cellToLoc(Point cell) {
		Location3D location = new Location3D();
		double lat = ((double) cell.y) * cellRadiusLat + lat_low;
		double lon = ((double) cell.x) * cellRadiusLon + lon_low;
		location.setLongitude(lon);
		location.setLatitude(lat);
//		System.out.println("Cell " + cellToString(cell) + " = [lon:" + lon + " " + (lon + cellRadiusLon) + ",lat:" + lat
//				+ " " + (lat + cellRadiusLat) + "]");
		return location;
	}

	String cellToString(Point cell) {
		return "(" + cell.getX() + "," + cell.getY() + ")";
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
				Logger.getLogger(HDSClient.class.getName()).log(Level.SEVERE, null, ex1);
			}
			return connect(host, port);
		}
		System.out.println("Connected to " + host + ":" + port);
		return socket;
	}

	public void doAreaSearch(OutputStream out, long vehicleId, Location3D location) throws Exception {
		MissionCommand mc = new MissionCommand();
		mc.setVehicleID(vehicleId);
		mc.setStatus(CommandStatusType.Pending);
		mc.setCommandID(vehicleId);

		Waypoint wp = new Waypoint();
		wp.setLatitude(location.getLatitude());
		wp.setLongitude(location.getLongitude());
		wp.setAltitude(2000);

		wp.setSpeed(speed);
		wp.setNumber(vehicleId);

		AreaSearchTask ast = new AreaSearchTask();
		Rectangle rect = new Rectangle();
		rect.setCenterPoint(location);
		rect.setWidth(100);
		rect.setHeight(100);
		ast.setTaskID(vehicleId);
		ast.setSearchArea(rect);
		ast.setDwellTime(100);
		ast.setGroundSampleDistance(20);
		ast.getDesiredWavelengthBands().add(WavelengthBand.EO);
		wp.getAssociatedTasks().add(vehicleId);
		mc.setFirstWaypoint(vehicleId);
		mc.getWaypointList().add(wp);
		vehicleCommands.put(vehicleId, mc);
		out.write(avtas.lmcp.LMCPFactory.packMessage(mc, true));
	}

	double extractDoubleFieldFromXML(String xmlString, String field) {
		double val;
		int f_start = xmlString.indexOf("<" + field + ">", 0);
		int f_end = xmlString.indexOf("</" + field + ">", f_start);
		f_start += ("<" + field + ">").length();
		String f_string = xmlString.substring(f_start, f_end);
		val = Double.parseDouble(f_string);
		return val;
	}

	public ArrayList<Location3D> getRectPoints(Rectangle bounds, float alt, AltitudeType alttype) throws Exception {
		ArrayList<Location3D> polygon = new ArrayList<Location3D>();
		Location3D centerPoint = bounds.getCenterPoint();
		float w = bounds.getWidth();
		float h = bounds.getHeight();
		float r = bounds.getRotation();
		if (r != 0) {
			throw new Exception("Need to rotate to get zone");
		}

		Location3D down_left = newLocation(centerPoint, w / -2f, h / -2f, alt, alttype);
		Location3D down_right = newLocation(centerPoint, w / -2f, h / 2f, alt, alttype);
		Location3D up_left = newLocation(centerPoint, w / 2f, h / 2f, alt, alttype);
		Location3D up_right = newLocation(centerPoint, w / -2f, h / 2f, alt, alttype);
		polygon.add(up_right);
		polygon.add(up_left);
		polygon.add(down_right);
		polygon.add(down_left);
		return polygon;

	}

	// haversine in meters
	public double haversine(double lat1, double lon1, double lat2, double lon2) {
		double R = R_EARTHKM;
		double dLat = Math.toRadians(lat2 - lat1);
		double dLon = Math.toRadians(lon2 - lon1);
		lat1 = Math.toRadians(lat1);
		lat2 = Math.toRadians(lat2);

		double a = Math.pow(Math.sin(dLat / 2), 2) + Math.pow(Math.sin(dLon / 2), 2) * Math.cos(lat1) * Math.cos(lat2);
		double c = 2 * Math.asin(Math.sqrt(a));
		return R * 1000.0 * c;
	}

	public Document loadXMLFromString(String xml) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder();

		return builder.parse(new ByteArrayInputStream(xml.getBytes()));
	}

	Point locCell(Location3D loc) {
		int xloc = (int) ((loc.getLongitude() - lon_low) / cellRadiusLon);
		int yloc = (int) ((loc.getLatitude() - lat_low) / cellRadiusLat);

		// si*i + sj*j
		Point toret = new Point(xloc, yloc);
		return toret;
	}

	boolean locInCell(Location3D loc, Point cell) {
		Location3D testLoc = cellToLoc(cell);
		boolean isLatInLimit = loc.getLatitude() > testLoc.getLatitude()
				&& loc.getLatitude() < (testLoc.getLatitude() + cellRadiusLat);
		boolean isLonInLimit = loc.getLongitude() < testLoc.getLongitude()
				&& loc.getLongitude() > (testLoc.getLongitude() + cellRadiusLon);
		return (isLatInLimit && isLonInLimit);
	}

	String locToString(Location3D loc) {
		return "[lon:" + loc.getLongitude() + ",lat:" + loc.getLatitude() + ",alt:" + loc.getAltitude() + "]";
	}

	public void makeUAVsDoStuff() throws IOException {
		boolean dolinesearch = false;
		boolean dohazard = false;
		boolean domove = !dohazard;

		for (long id : uavLocations.keySet()) {
			if (dohazard) {
				if (numHazardsDetected > 0 && (numHazardsVisited + 1) < numHazardsDetected) {
					for (long i = 0; i < numUAVs; i++) {
						// find free uav
						if (vehicleCommands.containsKey(i)) {
							if (vehicleCommandsPriority.get(id) != vehicleCommandPriority.hazard) {
								// cancel this and send another
								// i dont know how to cancel commands
								numHazardsVisited++;
								moveUAV(id, hazardLocations.get(numHazardsVisited), true, true);
								vehicleCommandsPriority.put(id, vehicleCommandPriority.hazard);

							} else {
								if (vehicleCommands.get(i).getStatus() == CommandStatusType.Cancelled
										|| vehicleCommands.get(i).getStatus() == CommandStatusType.Executed) {
									numHazardsVisited++;
									moveUAV(id, hazardLocations.get(numHazardsVisited), true, true);
									vehicleCommandsPriority.put(id, vehicleCommandPriority.hazard);
								}
							}
						} else {
							numHazardsVisited++;
							moveUAV(id, hazardLocations.get(numHazardsVisited), true, true);
							vehicleCommandsPriority.put(id, vehicleCommandPriority.hazard);
						}
					}
				} else {
					domove = true;
				}
			}

			if (domove) {
				switch ((int) (id)) {

				case 3:

//			moveUAV(id, action.right);
					moveUAV(id, new Point(0, 0), true, dolinesearch);
					break;
				case 4:
//			moveUAV(id, action.down);
					moveUAV(id, new Point(numCellsSide, 0), true, dolinesearch);
					break;
				case 2:
//			moveUAV(id, action.left);
					moveUAV(id, new Point(0, numCellsSide), true, dolinesearch);
					break;
				case 1:
//			moveUAV(id, action.up);
					moveUAV(id, new Point(numCellsSide, numCellsSide), true, dolinesearch);
					break;
				default:
					break;

				}
				vehicleCommandsPriority.put(id, vehicleCommandPriority.waypoint);
			}
		}
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

	void moveUAV(long id, action act, boolean noLoiter, boolean areaSearchTask) {
		try {
			moveUAV(socket.getOutputStream(), id, act, noLoiter, areaSearchTask);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	void moveUAV(long id, Location3D loc, boolean noLoiter, boolean areaSearchTask) throws IOException {
		moveUAV(socket.getOutputStream(), id, loc, noLoiter, areaSearchTask);
	}

	void moveUAV(long id, Point cell) {
		try {
			moveUAV(socket.getOutputStream(), id, cell, false, false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	void moveUAV(long id, Point cell, boolean noLoiter, boolean areaSearchTask) {
		try {
			moveUAV(socket.getOutputStream(), id, cell, noLoiter, areaSearchTask);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	void moveUAV(OutputStream out, long id, action act, boolean noLoiter, boolean areaSearchTask) {
		Location3D newloc = movement(uavLocations.get(id), act);
		try {
			moveUAV(out, id, newloc, noLoiter, areaSearchTask);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	void moveUAV(OutputStream out, long id, Location3D loc, boolean noLoiter, boolean areaSearchTask) {
		boolean executeAction = false;
		if (vehicleCommands.containsKey(id)) {
			if (vehicleCommands.get(id).getStatus() == CommandStatusType.Executed
					|| vehicleCommands.get(id).getStatus() == CommandStatusType.Cancelled)
				executeAction = true;
		} else
			executeAction = true;
		if (executeAction) {
			visitedCells.add(locCell(loc));
			try {
				if (loc.getAltitude() == 0)
					loc.setAltitude(600);
				if (noLoiter) {
					if (areaSearchTask) {
						System.out.println("Sending Area Search Command for UAV" + id + " to " + locToString(loc));
						doAreaSearch(out, id, loc);
					} else {
						System.out.println("Sending Waypoint Command for UAV" + id + " to " + locToString(loc));
						sendToWayPoint(out, id, loc);
					}
				} else {
					System.out.println("Sending Loiter Command for UAV" + id + " to " + locToString(loc));
					sendLoiterCommand(out, id, loc);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

			}
		}
//		 else {
//			System.out.println("Executing Action");
//		}

	}

	void moveUAV(OutputStream out, long id, Point cell, boolean noLoiter, boolean areaSearchTask) {
		// check if this vehicle is still doing something

		if (!locInCell(uavLocations.get(id), cell)) {
			Location3D loc = cellToLoc(cell);
			moveUAV(out, id, loc, noLoiter, areaSearchTask);

		}

	}

	public Location3D newLocation(Location3D loc, float dx, float dy) {
		double latitude = loc.getLatitude();
		double longitude = loc.getLongitude();
		double new_latitude = latitude + (dy / (R_EARTHKM * 1000)) * (180 / Math.PI);
		double new_longitude = longitude
				+ (dx / (R_EARTHKM * 1000)) * (180 / Math.PI) / Math.cos(latitude * Math.PI / 180);
		return new Location3D(new_latitude, new_longitude, loc.getAltitude(), loc.getAltitudeType());
	}

	public Location3D newLocation(Location3D loc, float dx, float dy, float alt, AltitudeType alttype) {
		double latitude = loc.getLatitude();
		double longitude = loc.getLongitude();
		double new_latitude = latitude + (dy / (R_EARTHKM * 1000)) * (180 / Math.PI);
		double new_longitude = longitude
				+ (dx / (R_EARTHKM * 1000)) * (180 / Math.PI) / Math.cos(latitude * Math.PI / 180);
		return new Location3D(new_latitude, new_longitude, alt, alttype);
	}

	public void processHazardZone(HazardZone hazardZone, boolean updateLimits) throws Exception {

		String boundaryType = "";
		float maxAlt = hazardZone.getMaxAltitude();
		ArrayList<Location3D> points = null;
		AltitudeType altType = hazardZone.getMaxAltitudeType();
		if (hazardZone.getBoundary() instanceof Rectangle) {
			// Rectangle
			// get the center point
			// then get the corners
			// command all vehicles there
			Rectangle rect = (Rectangle) hazardZone.getBoundary();
			points = getRectPoints(rect, maxAlt, altType);
			boundaryType = "Rect";
			// add these points to our hazardArray

		} else {
			if (hazardZone.getBoundary() instanceof Polygon) {

				Polygon polygon = (Polygon) hazardZone.getBoundary();
				points = polygon.getBoundaryPoints();
				getCircumscribedRectangle(polygon, updateLimits);
				boundaryType = "Polygon";
			} else {
				boundaryType = "Unknown";
			}

		}
		if (points != null) {

			for (int i = 0; i < points.size(); i++) {
				Location3D point = points.get(i);
				point.setAltitude(maxAlt);
				point.setAltitudeType(altType);
				hazardLocations.add(point);
				estimatedHazardZone.getBoundaryPoints().add(point);
				numHazardsDetected++;
			}
		}
		System.out.println("Processed Hazard Zone with boundary " + boundaryType);
	}

	public void addHazard(Location3D detectedLocation, long id) {
		hazardLocations.add(detectedLocation);
		Point cell = locCell(detectedLocation);
		hazardCells.add(cell);
		hazardDetectingEntity.add(id);
		numHazardsDetected++;
	}

	/**
	 * Reads in messages being sent out by the AMASE Server
	 */
	public void readMessages(InputStream in, OutputStream out) throws Exception {
		// Use each of the if statements to use the incoming message
		LMCPObject o = LMCPFactory.getObject(in);
//		System.out.println(o.toString());
		boolean hazardZone = false;
		if (o.toString().contains("Hazard"))
			if (o.toString().contains("Zone"))
				hazardZone = true;
		if (o instanceof afrl.cmasi.searchai.HazardZone) {
			processHazardZone((HazardZone) o, true);
//			}
		}
		// Check if the message is a HazardZoneDetection
		else if (o instanceof afrl.cmasi.searchai.HazardZoneDetection) {
			HazardZoneDetection hazardDetected = ((HazardZoneDetection) o);
			// Get location where zone first detected
			Location3D detectedLocation = hazardDetected.getDetectedLocation();
			// Get entity that detected the zone
			int detectingEntity = (int) hazardDetected.getDetectingEnitiyID();

			// Check if hint
			if (detectingEntity == 0) {
				// Do nothing for now, hints will be added later
				// lets do something now
				// basically get the zone
				// ask the UAV to go there
				addHazard(detectedLocation, -1);
				System.out.println("UAV" + detectingEntity + " detected hazard at " + detectedLocation.getLatitude()
						+ "," + detectedLocation.getLongitude());

			} else {

				// Check if the UAV has already been sent the loiter command and proceed if it
				// hasn't
				if (uavsLoiter[detectingEntity - 1] == false) {
					// Send the loiter command
//				sendLoiterCommand(out, detectingEntity, detectedLocation);

					// Note: Polygon points must be in clockwise or counter-clockwise order to get a
					// shape without intersections
					estimatedHazardZone.getBoundaryPoints().add(detectedLocation);

					// Send out the estimation report to draw the polygon
					sendEstimateReport(out, estimatedHazardZone);

					uavsLoiter[detectingEntity - 1] = true;
					addHazard(detectedLocation, detectingEntity);
					System.out.println("UAV" + detectingEntity + " detected hazard at " + detectedLocation.getLatitude()
							+ "," + detectedLocation.getLongitude());
//						+ ". Sending loiter command.");
				}
			}
//			System.out.println(hazardLocations.toString());

		} else if (o instanceof afrl.cmasi.AirVehicleState) {
			AirVehicleState uavState = ((AirVehicleState) o);
			long id = uavState.getID();
			Location3D loc = uavState.getLocation();
			updateLocation(id, loc);
			// send uav1 up
			// send uav2 left
			// send uav3 up left

		} else if (o instanceof afrl.cmasi.KeepInZone) {
			KeepInZone boundary = ((KeepInZone) o);
			setLimitsUsingKeepInZone(boundary);
//			System.out.println(boundary.toString());

		}
//        String xmlVersion = "";
//       xmlVersion= o.toXML(xmlVersion);
//        System.out.println(xmlVersion);
	}

	private double cellDist(Point p1, Point p2) {
		double squares = (p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y);
		return Math.sqrt(squares);
	}

	public void processHazards() {
		if (numHazardsDetected > 0) {
			// if <2 then go 20 cells away
			if (numHazardsDetected < 2) {
				Point cell = hazardCells.get(numHazardsDetected - 1);
				Long id = hazardDetectingEntity.get(numHazardsDetected - 1);
				if (uavCells.containsKey(id)) {
					// then send the other UAV to a point 20 cells away
					cell.x += 5;
					cell.y += 5;
					moveUAV(id + 1, cell, false, false);
				} else {
					long uavToUse = 1;
					double cellDist = numCellsSide * numCellsSide;
					double currDist = 0;
					// hazard detected so we have this point
					// lets send one uav to this point and another 5,5 away
					for (int i = 0; i < numUAVs; i++) {
						if (uavCells.containsKey(id)) {
							Point uavCell = uavCells.get(id);
							currDist = cellDist(cell, uavCell);
							if (currDist < cellDist) {
								cellDist = currDist;
								uavToUse = id;
							}
							// find smallest distance
//					moveUAV(i+1,cell,true,false); 
//					cell.x+=5; 
//					cell.y+=5; 

						}
					}
					moveUAV(uavToUse, cell, false, false);
				}
			} else {
				// get the rectangle
				ArrayList<Location3D> points = getCircumscribedRectanglePoints(hazardLocations);
				// these are the points we want to loiter to

//			Point cell = locCell(rect.getCenterPoint());
				// dummy
				// just send each uav to a point
				for (int i = 0; i < numUAVs; i++) {
					if (i < points.size()) {
						Point cell = locCell(points.get(i));
						moveUAV(i + 1, cell, false, false);

					}
				}
			}
		}
	}

	public void simpleMovement(long id) {
		// just send the uav to a location
		// mark that location as visited
		// then send it to another once its done loitering there for say 500ms
		// get the uav's current cell
		if (uavCells.containsKey(id)) {
			Point current_cell = uavCells.get(id);
			if (visitedCells.contains(current_cell)) {
				// lets visit another location
				Point new_cell = (Point) current_cell.clone();
				new_cell.setLocation(current_cell.getX() + 1, current_cell.getY());
				moveUAV(id, new_cell, true, false);
				// just wait there for like 500ms
				long time = System.currentTimeMillis();
				long endtime = System.currentTimeMillis();
				while (endtime - time < 50) {
					endtime = System.currentTimeMillis();
				}
			}
		}

	}

	@Override
	public void run() {
		try {
			// connect to the server
			socket = connect(host, port);

			while (true) {
				// Continually read the LMCP messages that AMASE is sending out
				readMessages(socket.getInputStream(), socket.getOutputStream());
				simpleMovement(1);
//				processHazards();
				// makeUAVsDoStuff();
			}

		} catch (Exception ex) {
			Logger.getLogger(HDSClient.class.getName()).log(Level.SEVERE, null, ex);
		}
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
		o.setCommandID(vehicleId);

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
		vehicleCommands.put(vehicleId, o);
		// Sending the Vehicle Action Command message to AMASE to be interpreted
		out.write(avtas.lmcp.LMCPFactory.packMessage(o, true));
	}

	/**
	 * Sends mission command to the AMASE Server
	 * 
	 * @param out
	 * @throws Exception
	 */
	public void sendMissionCommand(OutputStream out, long id, Location3D loc) throws Exception {
		VehicleActionCommand o = new VehicleActionCommand();
		o.setVehicleID(id);
		o.setStatus(CommandStatusType.Pending);
		o.setCommandID(1);

		// Setting up the loiter action
		LoiterAction loiterAction = new LoiterAction();
		loiterAction.setLoiterType(LoiterType.Circular);
		loiterAction.setRadius(250);
		loiterAction.setAxis(0);
		loiterAction.setLength(0);
		loiterAction.setDirection(LoiterDirection.Clockwise);
		loiterAction.setDuration(15000);
		loiterAction.setAirspeed(15);

		// Creating a 3D location object f
		Location3D location = loc;
		loiterAction.setLocation(location);

		// Adding the loiter action to the vehicle action list
		o.getVehicleActionList().add(loiterAction);

		// Sending the Vehicle Action Command message to AMASE to be interpreted
		out.write(avtas.lmcp.LMCPFactory.packMessage(o, true));

	}

	public void sendToWayPoint(OutputStream out, long vehicleId, Location3D location) throws Exception {
		MissionCommand mc = new MissionCommand();
		mc.setVehicleID(vehicleId);
		mc.setStatus(CommandStatusType.Pending);
		mc.setCommandID(vehicleId);

		Waypoint wp = new Waypoint();
		wp.setLatitude(location.getLatitude());
		wp.setLongitude(location.getLongitude());
		wp.setAltitude(2000);

		wp.setSpeed(speed);
		wp.setNumber(1);

		mc.setFirstWaypoint(1);
		mc.getWaypointList().add(wp);
		vehicleCommands.put(vehicleId, mc);
		out.write(avtas.lmcp.LMCPFactory.packMessage(mc, true));
	}

	public void setLimitsUsingKeepInZone(KeepInZone keepinzone) throws Exception {
		if (!limitsSetUsingKeepInZone) {
			Rectangle bounds = (Rectangle) keepinzone.getBoundary();

			setLimitsUsingRect(bounds);
			System.out.println("Processed KeepInZone");
		}
	}

	public void setLimitsUsingRect(Rectangle bounds) throws Exception {

		Location3D centerPoint = bounds.getCenterPoint();
		float w = bounds.getWidth();
		float h = bounds.getHeight();
		float r = bounds.getRotation();
		if (r != 0) {
			throw new Exception("Need to rotate to get zone");
		}
		Location3D low_loc = newLocation(centerPoint, w / -2f, h / -2f);
		Location3D high_loc = newLocation(centerPoint, w / 2f, h / 2f);
		setLimitsUsingHighLow(high_loc.getLatitude(), high_loc.getLongitude(), low_loc.getLatitude(),
				low_loc.getLongitude());

	}

	public void setLimitsUsingHighLow(double max_lat, double max_lon, double min_lat, double min_lon) {
		lat_high = max_lat;// high_loc.getLatitude();
		lat_low = min_lat;// low_loc.getLatitude();
		lon_high = max_lon;// high_loc.getLongitude();
		lon_low = min_lon;// low_loc.getLongitude();

		cellRadiusLat = (lat_high - lat_low) / (double) numCellsSide;
		cellRadiusLon = (lon_high - lon_low) / (double) numCellsSide;
		System.out.println("Radius - Lat: " + cellRadiusLat);
		System.out.println("Radius - Lon: " + cellRadiusLon);
		uavLocations = new HashMap<Long, Location3D>();
		uavCells = new HashMap<Long, Point>();
		vehicleCommands = new HashMap<Long, VehicleActionCommand>();

	}

	Point uavCell(long id, Location3D loc) {
		// max cells = numCellsSide*numCellsSide

		Point toret = locCell(loc);
		if (uavCells.containsKey(id))
			if (!uavCells.get(id).equals(toret))
				System.out.println("UAV " + id + " cell: " + cellToString(toret));
		uavCells.put(id, toret);

		return toret;

	}

	void updateLocation(long id, Location3D loc) {
		if (uavLocations.containsKey(id)) {
			uavLocations.put(id, loc);
			uavCell(id, loc);
		} else {
			addUAV(id, loc);
			Point cell = uavCell(id, loc);
			visitedCells.add(cell);
//			System.out.println("Location: " + locToString(loc));

//			System.out.println("In Cell: " + locInCell(loc, cell));

		}

	}

	Rectangle getCircumscribedRectangle(Polygon p, boolean setLimits) {
		ArrayList<Location3D> boundary = p.getBoundaryPoints();
		return getCircumscribedRectangle(boundary, setLimits);
	}

	ArrayList<Location3D> getCircumscribedRectanglePoints(ArrayList<Location3D> boundary) {
		double min_lat = 360;
		double max_lat = -360;
		double min_lon = 360;
		double max_lon = -360;
		float max_alt = 0;
		float min_alt = 10000;
		double lat, lon;
		float alt;
		AltitudeType alttyp = boundary.get(0).getAltitudeType();
		for (Location3D loc : boundary) {
			lat = loc.getLatitude();
			lon = loc.getLongitude();
			alt = loc.getAltitude();
			if (lat > max_lat)
				max_lat = lat;
			if (lat < min_lat)
				min_lat = lat;
			if (lon > max_lon)
				max_lon = lon;
			if (lon < min_lon)
				min_lon = lon;
			if (alt > max_alt)
				max_alt = alt;
			if (alt < min_alt)
				min_alt = alt;
		}
//		Location3D max = new Location3D();
//		max.setAltitude((float)max_alt); 
//		max.setLatitude(max_lat); 
//		max.setLongitude(max_lon);
//		max.setAltitudeType(alttyp);
//		
//		Location3D min = new Location3D();
//		min.setAltitude((float)min_alt); 
//		min.setLatitude(min_lat); 
//		min.setLongitude(min_lon);
//		min.setAltitudeType(alttyp);
		double mid_lat = min_lat + ((max_lat - min_lat) / 2.0);
		double mid_lon = min_lon + ((max_lon - min_lon) / 2.0);
		// creating midpoints

		Location3D p1 = new Location3D(max_lat, mid_lon, max_alt, alttyp);
		Location3D p2 = new Location3D(min_lat, mid_lon, max_alt, alttyp);
		Location3D p3 = new Location3D(mid_lat, min_lon, max_alt, alttyp);
		Location3D p4 = new Location3D(mid_lat, max_lon, max_alt, alttyp);
		ArrayList<Location3D> points = new ArrayList<>();
		points.add(p1);
		points.add(p2);
		points.add(p3);
		points.add(p4);
		return points;

	}

	Rectangle getCircumscribedRectangle(ArrayList<Location3D> boundary, boolean setLimits) {
		Rectangle rect = new Rectangle();
		// super quick thing, we're just going to find the max lat
		// min lat, max lon, min lon
		double min_lat = 360;
		double max_lat = -360;
		double min_lon = 360;
		double max_lon = -360;
		double max_alt = 0;
		double min_alt = 10000;
		double lat, lon, alt;
		AltitudeType alttyp = boundary.get(0).getAltitudeType();
		for (Location3D loc : boundary) {
			lat = loc.getLatitude();
			lon = loc.getLongitude();
			alt = loc.getAltitude();
			if (lat > max_lat)
				max_lat = lat;
			if (lat < min_lat)
				min_lat = lat;
			if (lon > max_lon)
				max_lon = lon;
			if (lon < min_lon)
				min_lon = lon;
			if (alt > max_alt)
				max_alt = alt;
			if (alt < min_alt)
				min_alt = alt;
		}
		if (setLimits) {
			setLimitsUsingHighLow(max_lat, max_lon, min_lat, min_lon);
		}
		Location3D max = new Location3D();
		max.setAltitude((float) max_alt);
		max.setLatitude(max_lat);
		max.setLongitude(max_lon);
		max.setAltitudeType(alttyp);

		Location3D min = new Location3D();
		min.setAltitude((float) min_alt);
		min.setLatitude(min_lat);
		min.setLongitude(min_lon);
		min.setAltitudeType(alttyp);

		Location3D centre = new Location3D();
		centre.setLatitude((max_alt + min_alt) / 2.0);
		centre.setLongitude((max_lon + min_lon) / 2.0);
		centre.setAltitude((float) max_alt);
		centre.setAltitudeType(alttyp);

		double latd = haversine(max_lat, 0, min_lat, 0);
		double lond = haversine(0, max_lon, 0, min_lon);

		rect.setCenterPoint(centre);
		rect.setWidth((float) latd);
		rect.setHeight((float) lond);

		return rect;
	}

}
