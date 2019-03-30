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
import afrl.cmasi.searchai.RecoveryPoint;
import afrl.cmasi.AirVehicleConfiguration;
import afrl.cmasi.AirVehicleState;
import afrl.cmasi.AltitudeType;
import afrl.cmasi.CommandStatusType;
import afrl.cmasi.FlightProfile;
import afrl.cmasi.KeepInZone;
import afrl.cmasi.Location3D;
import afrl.cmasi.LoiterAction;
import afrl.cmasi.LoiterDirection;
import afrl.cmasi.LoiterType;
import afrl.cmasi.MissionCommand;
import afrl.cmasi.VehicleActionCommand;
import afrl.cmasi.Waypoint;
import afrl.cmasi.Polygon;
import afrl.cmasi.Rectangle;
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
public class gridClientHeatmap extends Thread {
	private static final double locError = 10;//in meters
	public class Grid {

	
		boolean isProtoBufGrid = false;
		boolean gridInitialised = false;
		double lat_high;
		double lat_low;
		double lon_high;
		double lon_low;

		double cellIncLat;
		double cellIncLon;
		double numCellsLat;
		double numCellsLon;
		double[][] heatmap = null;

		public Grid(int numCellsLat, int numCellsLon, boolean isProtobuf) {
			this.numCellsLat = numCellsLat;
			this.numCellsLon = numCellsLon;
			this.isProtoBufGrid = isProtobuf;

		}

		public boolean pointInGrid(Point p) {
			if (p.getX() < numCellsLon && p.getY() < numCellsLat)
				return true;
			return false;
		}

		public void initialiseGrid(double max_lat, double min_lat, double max_lon, double min_lon) {
			lat_high = max_lat;// high_loc.getLatitude();
			lat_low = min_lat;// low_loc.getLatitude();
			lon_high = max_lon;// high_loc.getLongitude();
			lon_low = min_lon;// low_loc.getLongitude();

			cellIncLat = (lat_high - lat_low) / (double) numCellsLat;
			cellIncLon = (lon_high - lon_low) / (double) numCellsLon;
			gridInitialised = true;
		}

		public Point locationToGrid(Location3D loc) {
			int xloc =(int)Math.floor(((loc.getLongitude() - lon_low) / cellIncLon));
			int yloc = (int)Math.floor( ((loc.getLatitude() - lat_low) / cellIncLat));

			// si*i + sj*j
			Point toret = new Point(xloc, yloc);
			return toret;
		}

		public Location3D pointToLocation(Point cell) {
			Location3D location = new Location3D();
			double lat = ((double) cell.y) * cellIncLat + lat_low;
			double lon = ((double) cell.x) * cellIncLon + lon_low;
			location.setLongitude(lon);
			location.setLatitude(lat);
			location.setAltitude(200);
			location.setAltitudeType(AltitudeType.AGL);
//			System.out.println("Cell " + cell.toString() + " = [lon:" + lon + " " + (lon + cellRadiusLon) + ",lat:" + lat
//					+ " " + (lat + cellRadiusLat) + "]");
			return location;
		}

		public Location3D createUpperBoundLocation(Location3D location) {
			Location3D loc = new Location3D(location.getLatitude() + cellIncLat,
					location.getLongitude() + cellIncLon, location.getAltitude(), location.getAltitudeType());

			return loc;
		}

		ArrayList<Location3D> getLocTopBottom(Point cell) {
			Location3D loc1 = pointToLocation(cell);
			Location3D loc2 = createUpperBoundLocation(loc1);
			ArrayList<Location3D> arr = new ArrayList<Location3D>();
			arr.add(loc1);
			arr.add(loc2);
			return arr;
		}

		
		boolean locInGridPoint(Location3D loc, Point cell) {
			Location3D testLoc = pointToLocation(cell);
			// so if they're in some distance of each other,
			// we're in that location
			boolean isLatNeg = testLoc.getLatitude() < 0;
			boolean isLonNeg = testLoc.getLongitude() < 0;
			double latLimLow, latLimHigh, lonLimLow, lonLimHigh;
			if (isLatNeg) {
				if (cellIncLat > 0) {
					latLimHigh = testLoc.getLatitude() +cellIncLat;
					latLimLow = testLoc.getLatitude() - cellIncLat;
				} else {
					latLimHigh = testLoc.getLatitude() - cellIncLat;
					latLimLow = testLoc.getLatitude() + cellIncLat;
				}
			} else {
				if (cellIncLat > 0) {
					latLimHigh = testLoc.getLatitude() + cellIncLat;
					latLimLow = testLoc.getLatitude() - cellIncLat;
				} else {
					latLimHigh = testLoc.getLatitude() - cellIncLat;
					latLimLow = testLoc.getLatitude() + cellIncLat;
				}
			}
			if (isLonNeg) {
				if (cellIncLon > 0) {
					lonLimHigh = testLoc.getLongitude() + cellIncLon;
					lonLimLow = testLoc.getLongitude() -cellIncLon;
				} else {
					lonLimHigh = testLoc.getLongitude() - cellIncLon;
					lonLimLow = testLoc.getLongitude() +cellIncLon;
				}
			} else {
				if (cellIncLon > 0) {
					lonLimHigh = testLoc.getLongitude() + cellIncLon;
					lonLimLow = testLoc.getLongitude() - cellIncLon;
				} else {
					lonLimHigh = testLoc.getLongitude() - cellIncLon;
					lonLimLow = testLoc.getLongitude() + cellIncLon;
				}
			}

//			System.out.println("Current:"+testLoc.toString()+" target:"+cell.toString()); 
			boolean isLatInLimit = doComparision(loc.getLatitude(), latLimLow, latLimHigh);
			boolean isLonInLimit = doComparision(loc.getLongitude(), lonLimLow, lonLimHigh);
			return (isLatInLimit && isLonInLimit);
		}

		boolean doComparision(double p, double low, double high) {
			// lim2 > lim1
			return p >= low && p <= high;
		}
		// so if we have a resolution
		// and its not a heatmap
		// then you do nothing
		// cell(0,0) = cells (0,0) through (res,res)
		// cell(1,0) = cells(res,0) through (res+res,res)
		// so basically cell(x,y) = cells(x*res,y*res) through (x*res+res,y*res+res)

		ArrayList<Point> gridToheatMap(Point gridPoint, int res) {
			ArrayList<Point> pts = new ArrayList<Point>();

			int x = (int) (gridPoint.getX() * res);
			int y = (int) (gridPoint.getY() * res);
			Point p1 = new Point(x, y);
			x = x + res;
			y = y + res;
			Point p2 = new Point(x, y);
			pts.add(p1);
			pts.add(p2);
			return pts;

		}

		// heatmappoint = (x,y)
		// grid point = floor(x/res, y/res)
		Point heatMapToGrid(Point heatMapPoint, int res) {
			int x = (int) heatMapPoint.getX() / res;
			int y = (int) heatMapPoint.getY() / res;
			Point gridPoint = new Point(x, y);
			return gridPoint;
		}

	}

	public class uavInfo {
		Location3D currentLocation = null;
		double currentEnergyRate;
		double currentEnergy;
		double maxSpeed = -1;
		long id;
		String entityType;
		VehicleActionCommand currentCommand = null;
		Location3D targetLocation = null;
		Point currentCell = null;
		Point commandCell = null;

		public uavInfo(long id) {
			this.id = id;
		}
	}

	int startPointX = 1;
	int startPointY = 1;
	Socket socket;
	private static final float LoiterRadius = 250;

	private static final long LoiterDuration = 10000;
	ListenerChannel listenerChannel;
	/** simulation TCP port to connect to */
	private static int port = 5555;
	/** address of the server */
	private static String host = "localhost";
	/** Array of booleans indicating if loiter command has been sent to each UAV */
	boolean[] uavsLoiter = new boolean[4];
	boolean canMove = false;
	Polygon estimatedHazardZone = new Polygon();
	int numUAVs = 4;
	HashMap<Long, uavInfo> uavs;
	boolean allSpeedsSaved = false;
	boolean allLocationsSaved = false;
	final double R = 6372.8 * 1000; // In kilometers
	ArrayList<RecoveryPoint> fuelLocations;
	Grid grid;
	Grid heatmapGrid;
	boolean startMovement = false;
	double gridGranularityRelativeToHeatMapSide = 4;

	boolean uavsHashMapHasSpeedForAll() {
		int numSpeed = 0;
		for (Long u : uavs.keySet()) {
			if (uavs.get(u).maxSpeed != -1)
				numSpeed++;
		}
		return numSpeed == numUAVs;
	}

	boolean uavsHashMapHasLocForAll() {
		int numLoc = 0;
		for (Long u : uavs.keySet()) {
			if (uavs.get(u).currentLocation != null)
				numLoc++;
		}
		return numLoc == numUAVs;
	}

	public gridClientHeatmap() {
		uavs = new HashMap<Long, uavInfo>();
		fuelLocations = new ArrayList<RecoveryPoint>();
		heatmapGrid = null;
//		grid = new Grid(100,100,false);
		listenerChannel = new ListenerChannel();
		listenerChannel.run(x -> {
//			System.out.println("Received update at simulation time :" + x.getTime());
			if (heatmapGrid == null) {
				final int rows = x.getSize(0);
				final int columns = x.getSize(1);
				grid = new Grid((int) (columns / this.gridGranularityRelativeToHeatMapSide),
						(int) (rows / this.gridGranularityRelativeToHeatMapSide), false);
				this.startPointX = (int) grid.numCellsLon - 1;
				this.startPointY = (int) grid.numCellsLat - 1;
				heatmapGrid = new Grid(columns, rows, true);
				heatmapGrid.initialiseGrid(x.getMaxLat(), x.getMinLat(), x.getMaxLong(), x.getMinLong());
				grid.initialiseGrid(x.getMaxLat(), x.getMinLat(), x.getMaxLong(), x.getMinLong());
				heatmapGrid.heatmap = new double[rows][columns];
				int index = 0;
				for (int i = 0; i < rows; i++)
					for (int j = 0; j < columns; j++) {
						heatmapGrid.heatmap[i][j] = x.getMap(index);
						index++;
					}

//			System.out.println(heatmap);
			} else {
				int rows = (int) heatmapGrid.numCellsLon;
				int columns = (int) heatmapGrid.numCellsLat;
				// update heatmap only
				heatmapGrid.heatmap = new double[rows][columns];
				int index = 0;
				for (int i = 0; i < rows; i++)
					for (int j = 0; j < columns; j++) {
						heatmapGrid.heatmap[i][j] = x.getMap(index);
						index++;
					}

			}
		});

	}
	boolean closeEnough(Location3D loc1, Location3D loc2)
	{
		double dist = Math.abs(haversine(loc1,loc2));
		if (dist < locError)
		{
			return true; 
		}
		return false; 
	}

	public void setLimitsUsingKeepInZone(KeepInZone keepinzone) throws Exception {

		Rectangle bounds = (Rectangle) keepinzone.getBoundary();

		setLimitsUsingRect(bounds);
		System.out.println("Processed KeepInZone");

	}

	public Location3D newLocation(Location3D loc, float dx, float dy) {
		double latitude = loc.getLatitude();
		double longitude = loc.getLongitude();
		double new_latitude = latitude + (dy / (R)) * (180 / Math.PI);
		double new_longitude = longitude + (dx / (R)) * (180 / Math.PI) / Math.cos(latitude * Math.PI / 180);
		return new Location3D(new_latitude, new_longitude, loc.getAltitude(), loc.getAltitudeType());
	}

	public Location3D newLocation(Location3D loc, float dx, float dy, float alt, AltitudeType alttype) {
		double latitude = loc.getLatitude();
		double longitude = loc.getLongitude();
		double new_latitude = latitude + (dy / (R)) * (180 / Math.PI);
		double new_longitude = longitude + (dx / (R)) * (180 / Math.PI) / Math.cos(latitude * Math.PI / 180);
		return new Location3D(new_latitude, new_longitude, alt, alttype);
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
		if (grid == null) {
			grid = new Grid(100, 100, false);
		}
		grid.initialiseGrid(high_loc.getLatitude(), low_loc.getLatitude(), high_loc.getLongitude(),
				low_loc.getLongitude());
		this.startPointX = (int) grid.numCellsLon - 1;
		this.startPointY = (int) grid.numCellsLat- 1;
		System.out.println("Initial Locations");
		System.out.println(this.startPointX+":"+this.startPointY);

	}

	public double haversine(Location3D loc1, Location3D loc2) {
		double lat1 = loc1.getLatitude();
		double lat2 = loc2.getLatitude();
		double lon1 = loc1.getLatitude();
		double lon2 = loc2.getLatitude();
		return haversine(lat1, lon1, lat2, lon2);
	}

	public double haversine(double lat1, double lon1, double lat2, double lon2) {
		double dLat = Math.toRadians(lat2 - lat1);
		double dLon = Math.toRadians(lon2 - lon1);
		lat1 = Math.toRadians(lat1);
		lat2 = Math.toRadians(lat2);

		double a = Math.pow(Math.sin(dLat / 2), 2) + Math.pow(Math.sin(dLon / 2), 2) * Math.cos(lat1) * Math.cos(lat2);
		double c = 2 * Math.asin(Math.sqrt(a));
		return R * c;
	}

	public double calculateCost(Location3D startLoc, Location3D goalLoc, float maxSpeed, float uavEnergyRate) {
		double distance = haversine(startLoc, goalLoc);
		double time = distance / maxSpeed;
		double energySpent = uavEnergyRate * time;
		return energySpent;

	}

	public HashMap<Location3D, Long> allocateUAVsToLocations(ArrayList<Location3D> locs) {
		HashMap<Location3D, Long> locationAllocations = new HashMap<Location3D, Long>();
		HashMap<Long, Boolean> uavAllocated = new HashMap<Long, Boolean>();
		for (Long uav : uavs.keySet()) {
			uavAllocated.put(uav, false);
		}
		for (Location3D loc : locs) {
			long bestSuitedUAV = findClosestUAV(loc, uavAllocated);
			uavAllocated.put(bestSuitedUAV, true);
			locationAllocations.put(loc, bestSuitedUAV);
		}
		return locationAllocations;
	}

	public long findClosestUAV(Location3D loc, HashMap<Long, Boolean> uavAllocated) {
		long id = -1;
		double minDist = 100000;
		for (long uav : uavs.keySet()) {
			if (!uavAllocated.get(uav)) {
				double dist = haversine(uavs.get(uav).currentLocation, loc);
				if (minDist > dist) {
					minDist = dist;
					id = uav;
				}
			}
		}
		return id;
	}

	public void okayMovement() {
		if (!canMove) {
			if (this.allLocationsSaved && this.allSpeedsSaved) {
				System.out.println("Can Move Now");
				canMove = true;
				// print all the locations
//				System.out.println(this.uavsOnGrid.toString()); 
//				System.out.println(this.uavLocations.toString());
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
				okayMovement();
				if (canMove)
				{	goAround();
					
				}

			}

		} catch (Exception ex) {
			Logger.getLogger(gridClientHeatmap.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public void goAround() throws Exception {
		int inc = 5;
		for (long id : uavs.keySet()) {
			if (canMove(id)) {
				System.out.println(this.startPointX+":"+this.startPointY);
				sendWaypointCommand(id, new Point(this.startPointX, this.startPointY), 30);
				this.startPointX -= inc;
				if (this.startPointX <=0) {
					this.startPointY -= inc;
					this.startPointX = (int) (grid.numCellsLat - 1);
				}
				System.out.println(this.startPointX+":"+this.startPointY);
			}
		}

//		for (long id:uavs.keySet())
//		{
//			//print out the grid loc 
//			//print out the heatmap loc 
//			System.out.println(id+":Grid"+uavs.get(id).currentCell.toString()); 
//			if(heatmapGrid != null)
//			System.out.println(id+":Grid"+heatmapGrid.gridToheatMap(uavs.get(id).currentCell,(int)this.gridGranularityRelativeToHeatMapSide).toString()); 
//		}

//		System.out.println("Going Around"); 
//		System.out.println(uavsOnGrid.toString()); 

//		if (canMove(1))
//		sendWaypointCommand(1,new Point(70,50),30);
//		if (canMove(2))
//		sendWaypointCommand(2,new Point(10,(int) grid.numCellsLon-10),15); 
//		if(canMove(3))
//		sendWaypointCommand(3,new Point((int)grid.numCellsLat-10,10),15);
//		if(canMove(4))
//		sendWaypointCommand(4,new Point((int)grid.numCellsLat-10,(int) grid.numCellsLon-10),15);
//		Point p = grid.initLoc;
//		int inc = 10;
//		for (long id : this.uavsOnGrid.keySet()) {
//			if (canMove(id)) {
//				sendLoiterCommand(id, p, (float) uavs.get(id).maxSpeed);
//				if (p.getX() == grid.numCellsLat) {
//					p.x = 0;
//					p.y = p.y + inc;
//				}
//				p.x = p.x + inc;
//
//			}
//			grid.initLoc = p;
//		}
		// just cover the entire area

	}

	public void sendToWayPoint(OutputStream out, long vehicleId, Location3D location, float speed) throws Exception {
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
		uavs.get(vehicleId).currentCommand = mc;
//		vehicleCommands.put(vehicleId, mc);
		out.write(avtas.lmcp.LMCPFactory.packMessage(mc, true));
	}

	public void sendWaypointCommand(long vehicleId, Point cell, float uavAirSpeed) throws IOException, Exception {
		uavs.get(vehicleId).commandCell = cell;

//		commandLocations.put(vehicleId, cell);
		Location3D loc = grid.pointToLocation(cell);
		uavs.get(vehicleId).targetLocation = loc;
		sendToWayPoint(socket.getOutputStream(), vehicleId, loc, uavAirSpeed);
	}

	public void sendWaypointCommand(long vehicleId, Location3D loc, float uavAirSpeed) {

	}

	public void sendLoiterCommand(long vehicleId, Point cell, float uavAirSpeed) throws Exception {
		uavs.get(vehicleId).commandCell = cell;
//		commandLocations.put(vehicleId, cell);
		Location3D loc = grid.pointToLocation(cell);
		uavs.get(vehicleId).targetLocation = loc;
		sendLoiterCommand(socket.getOutputStream(), vehicleId, loc, uavAirSpeed);
	}

	public void sendLoiterCommand(OutputStream out, long vehicleId, Point cell, float uavAirSpeed) throws Exception {
		Location3D loc = grid.pointToLocation(cell);
		sendLoiterCommand(out, vehicleId, loc, uavAirSpeed);
	}

	/**
	 * Sends loiter command to the AMASE Server
	 * 
	 * @param out
	 * @throws Exception
	 */
	public void sendLoiterCommand(OutputStream out, long vehicleId, Location3D location, float uavAirSpeed)
			throws Exception {
		// Setting up the mission to send to the UAV
		VehicleActionCommand o = new VehicleActionCommand();
		o.setVehicleID(vehicleId);
		o.setStatus(CommandStatusType.Pending);
		o.setCommandID(1);

		// Setting up the loiter action
		LoiterAction loiterAction = new LoiterAction();
		loiterAction.setLoiterType(LoiterType.Circular);
		loiterAction.setRadius(LoiterRadius);
		loiterAction.setAxis(0);
		loiterAction.setLength(0);
		loiterAction.setDirection(LoiterDirection.Clockwise);
		loiterAction.setDuration(LoiterDuration);
		loiterAction.setAirspeed(uavAirSpeed);

		// Creating a 3D location object for the stare point
		loiterAction.setLocation(location);

		// Adding the loiter action to the vehicle action list
		o.getVehicleActionList().add(loiterAction);

		// Sending the Vehicle Action Command message to AMASE to be interpreted
		out.write(avtas.lmcp.LMCPFactory.packMessage(o, true));
		uavs.get(vehicleId).currentCommand = o;
//		this.vehicleCommands.put(vehicleId, o);
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

	public String locToString(Location3D loc) {
		return "[lat:" + loc.getLatitude() + "lon:" + loc.getLongitude() + "alt:" + loc.getAltitude() + "]";

	}

	public void printCollectedInfo() {
		// if(true)
		if (this.allLocationsSaved && this.allSpeedsSaved) {

			if (!canMove)
				canMove = true;
			for (long id : uavs.keySet()) {
				uavInfo uav = uavs.get(id);
				String infoString = "" + id + ":" + uav.entityType;
				infoString += "max speed=" + uav.maxSpeed;
				infoString += "energy=" + uav.currentEnergy;
				infoString += "energyRate=" + uav.currentEnergyRate;
				if (uav.currentLocation != null)
					infoString += "loc=" + locToString(uav.currentLocation);
				System.out.println(infoString);

			}
//			// Lat: 53.4989 Lon: -1.7509 Alt: 431 m
//			// Lat: 53.5022 Lon: -1.645 Alt: 309 m
//			Location3D loc1 = new Location3D(53.4989, -1.7509, 431, AltitudeType.MSL);
//			Location3D loc2 = new Location3D(53.5022, -1.645, 309, AltitudeType.MSL);
//			ArrayList<Location3D> locs = new ArrayList<Location3D>();
//			locs.add(loc1);
//			locs.add(loc2);
//			HashMap<Location3D, Long> res = this.allocateUAVsToLocations(locs);
//			for (Location3D loc : locs) {
//				System.out.println(locToString(loc) + "-" + res.get(loc));
//			}
		}
	}

	public void processHazardZone(HazardZoneDetection hazardDetected, OutputStream out) throws Exception {
//		HazardZoneDetection hazardDetected = ((HazardZoneDetection) o);
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
			sendLoiterCommand(out, detectingEntity, detectedLocation, 15);

			// Note: Polygon points must be in clockwise or counter-clockwise order to get a
			// shape without intersections
			estimatedHazardZone.getBoundaryPoints().add(detectedLocation);

			// Send out the estimation report to draw the polygon
			sendEstimateReport(out, estimatedHazardZone);

			uavsLoiter[detectingEntity - 1] = true;
			System.out.println("UAV" + detectingEntity + " detected hazard at " + detectedLocation.getLatitude() + ","
					+ detectedLocation.getLongitude() + ". Sending loiter command.");
		}

	}

	public void processAirVehicleConfig(AirVehicleConfiguration avc) {
		float energyRate;
		long id = avc.getID();
		if (!uavs.containsKey(id)) {
			uavs.put(id, new uavInfo(id));

		}
		uavInfo uav = uavs.get(id);
		float maxspeed = avc.getMaximumSpeed();
		avc.setNominalAltitudeType(AltitudeType.AGL);
		avc.setNominalAltitude(700);
		FlightProfile fp = avc.getNominalFlightProfile();
		energyRate = fp.getEnergyRate();
		uav.currentEnergyRate = energyRate;
		String entityType = avc.getEntityType();
		uav.maxSpeed = maxspeed;
		uav.entityType = entityType;
		if (!this.allSpeedsSaved) {
			if (uavsHashMapHasSpeedForAll())
				this.allSpeedsSaved = true;
		}

	}

	public void processAirVehicleState(AirVehicleState avs) {
		long id = avs.getID();
		if (!uavs.containsKey(id)) {
			uavs.put(id, new uavInfo(id));
		}
		uavInfo uav = uavs.get(id);
		Location3D loc = avs.getLocation();
		float energyRate = avs.getActualEnergyRate();
		uav.currentEnergyRate = energyRate;
		uav.currentLocation = loc;
		uav.currentEnergy = avs.getEnergyAvailable();
		if (!this.allLocationsSaved) {
			if (uavsHashMapHasLocForAll())
				this.allLocationsSaved = true;
		}
		// check if we need to updated the vehicle commands
//		System.out.println(avs.getLocation().getAltitude());
		updateVehicleCommands(id, loc);
		updateVehicleLocs(id, loc);
	}

	public void updateVehicleCommands(long id, Location3D loc) {
		if (uavs.get(id).currentCommand != null) {

//			if (grid.locInGridPoint(loc, uavs.get(id).commandCell)) {
			if(closeEnough(loc,uavs.get(id).targetLocation)) {
				uavs.get(id).currentCommand.setStatus(CommandStatusType.Executed);
			}
		}

	}

	public boolean canMove(long id) {
		boolean move = true;
		if (uavs.get(id).currentCommand != null) {
			if (uavs.get(id).currentCommand.getStatus() != CommandStatusType.Executed)
				move = false;
		}
		return move;
	}

	public void updateVehicleLocs(long id, Location3D loc) {
		Point cell = grid.locationToGrid(loc);
		uavs.get(id).currentCell = cell;
//		this.uavsOnGrid.put(id, cell);
//		this.uavLocations.put(id, loc);
		uavs.get(id).currentLocation = loc;
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
//			processHazardZone(hazardDetected, out);
		}

		if (o instanceof afrl.cmasi.AirVehicleConfiguration) {
			AirVehicleConfiguration avc = ((AirVehicleConfiguration) o);
			processAirVehicleConfig(avc);
		}
		if (o instanceof afrl.cmasi.AirVehicleState) {
			AirVehicleState avs = ((AirVehicleState) o);
			processAirVehicleState(avs);
		}
		if (o instanceof afrl.cmasi.searchai.RecoveryPoint) {
			RecoveryPoint rp = ((RecoveryPoint) o);
			fuelLocations.add(rp);

		}
		if (o instanceof afrl.cmasi.KeepInZone) {
			KeepInZone kiz = ((KeepInZone) o);
			setLimitsUsingKeepInZone(kiz);
		}

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
				Logger.getLogger(gridClientHeatmap.class.getName()).log(Level.SEVERE, null, ex1);
			}
			return connect(host, port);
		}
		System.out.println("Connected to " + host + ":" + port);
		return socket;
	}

	public static void main(String[] args) {
		new gridClientHeatmap().start();
	}
}
