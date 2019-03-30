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
import afrl.cmasi.GimbalStareAction;
import afrl.cmasi.Location3D;
import afrl.cmasi.LoiterAction;
import afrl.cmasi.LoiterDirection;
import afrl.cmasi.LoiterType;
import afrl.cmasi.MissionCommand;
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
public class costCalculation extends Thread {

	public class uavInfo {
		Location3D currentLocation = null;
		double currentEnergyRate;
		double currentEnergy;
		double maxSpeed = -1;
		long id;
		String entityType;
		VehicleActionCommand currentCommand = null;
		Location3D targetLocation = null;

		public uavInfo(long id) {
			this.id = id;
		}
	}

	/** simulation TCP port to connect to */
	private static int port = 5555;
	/** address of the server */
	private static String host = "localhost";
	/** Array of booleans indicating if loiter command has been sent to each UAV */
	boolean[] uavsLoiter = new boolean[4];
	Polygon estimatedHazardZone = new Polygon();
	int numUAVs = 4;
	HashMap<Long, uavInfo> uavs;
	boolean allSpeedsSaved = false;
	boolean allLocationsSaved = false;
	final double R = 6372.8 * 1000; // In kilometers
	ArrayList<RecoveryPoint> fuelLocations;

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

	public costCalculation() {
		uavs = new HashMap<Long, uavInfo>();
		fuelLocations = new ArrayList<RecoveryPoint>();

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

	@Override
	public void run() {
		try {
			// connect to the server
			Socket socket = connect(host, port);

			while (true) {
				// Continually read the LMCP messages that AMASE is sending out
				readMessages(socket.getInputStream(), socket.getOutputStream());
				printCollectedInfo();
			}

		} catch (Exception ex) {
			Logger.getLogger(costCalculation.class.getName()).log(Level.SEVERE, null, ex);
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

	public String locToString(Location3D loc) {
		return "[lat:" + loc.getLatitude() + "lon:" + loc.getLongitude() + "alt:" + loc.getAltitude() + "]";

	}

	public void printCollectedInfo() {
		// if(true)
		if (this.allLocationsSaved && this.allSpeedsSaved) {
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
			// Lat: 53.4989 Lon: -1.7509 Alt: 431 m
			// Lat: 53.5022 Lon: -1.645 Alt: 309 m
			Location3D loc1 = new Location3D(53.4989, -1.7509, 431, AltitudeType.MSL);
			Location3D loc2 = new Location3D(53.5022, -1.645, 309, AltitudeType.MSL);
			ArrayList<Location3D> locs = new ArrayList<Location3D>();
			locs.add(loc1);
			locs.add(loc2);
			HashMap<Location3D, Long> res = this.allocateUAVsToLocations(locs);
			for (Location3D loc : locs) {
				System.out.println(locToString(loc) + "-" + res.get(loc));
			}
		}
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
		}
		float energyRate;
		if (o instanceof afrl.cmasi.AirVehicleConfiguration) {
			AirVehicleConfiguration avc = ((AirVehicleConfiguration) o);

			long id = avc.getID();
			if (!uavs.containsKey(id)) {
				uavs.put(id, new uavInfo(id));

			}
			uavInfo uav = uavs.get(id);
			float maxspeed = avc.getMaximumSpeed();
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
		if (o instanceof afrl.cmasi.AirVehicleState) {
			AirVehicleState avs = ((AirVehicleState) o);
			long id = avs.getID();
			if (!uavs.containsKey(id)) {
				uavs.put(id, new uavInfo(id));
			}
			uavInfo uav = uavs.get(id);
			Location3D loc = avs.getLocation();
			energyRate = avs.getActualEnergyRate();
			uav.currentEnergyRate = energyRate;
			uav.currentLocation = loc;
			uav.currentEnergy = avs.getEnergyAvailable();
			if (!this.allLocationsSaved) {
				if (uavsHashMapHasLocForAll())
					this.allLocationsSaved = true;
			}
		}
		if (o instanceof afrl.cmasi.searchai.RecoveryPoint) {
			RecoveryPoint rp = ((RecoveryPoint) o);
			fuelLocations.add(rp);

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
				Logger.getLogger(costCalculation.class.getName()).log(Level.SEVERE, null, ex1);
			}
			return connect(host, port);
		}
		System.out.println("Connected to " + host + ":" + port);
		return socket;
	}

	public static void main(String[] args) {
		new costCalculation().start();
	}
}
