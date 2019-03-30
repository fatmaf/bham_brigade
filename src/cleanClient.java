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
import afrl.cmasi.EntityState;
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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Connects to the simulation and sends a fake mission command to every UAV that
 * is requested in the plan request.
 */
public class cleanClient extends Thread {

	/** simulation TCP port to connect to */
	private static int port = 5555;
	/** address of the server */
	private static String host = "localhost";
	/** Array of booleans indicating if loiter command has been sent to each UAV */
	boolean[] uavsLoiter = new boolean[4];
	Polygon estimatedHazardZone = new Polygon();
	UnknownSearchClass usc = new UnknownSearchClass();
	FollowFireClass ffc = new FollowFireClass();
	enum STAGE {
		UnknownSearch, FollowFire
	};

	STAGE currentStage = STAGE.UnknownSearch;
	double clat = 53.3783;
	double clongt = -1.7616;

	public cleanClient() {
		
	}
	

	@Override
	public void run() {
		try {
			// connect to the server
			Socket socket = connect(host, port);
			switch (currentStage) {

			case UnknownSearch:
				usc.doUnknownSearch(socket.getOutputStream());
				break;
			case FollowFire:

				break;
			default:
				break;

			}

			while (true) {
				// Continually read the LMCP messages that AMASE is sending out
				readMessages(socket.getInputStream(), socket.getOutputStream());
			}

		} catch (Exception ex) {
			Logger.getLogger(cleanClient.class.getName()).log(Level.SEVERE, null, ex);
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

	public void processHazardZoneDetection(OutputStream out, HazardZoneDetection hazardDetected) throws Exception

	{
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
//		if (uavsLoiter[detectingEntity - 1] == false) {
//			// Send the loiter command
//			sendLoiterCommand(out, detectingEntity, detectedLocation);
//
//			// Note: Polygon points must be in clockwise or counter-clockwise order to get a
//			// shape without intersections
//			estimatedHazardZone.getBoundaryPoints().add(detectedLocation);
//
//			// Send out the estimation report to draw the polygon
//			sendEstimateReport(out, estimatedHazardZone);
//
//			uavsLoiter[detectingEntity - 1] = true;
//			System.out.println("UAV" + detectingEntity + " detected hazard at " + detectedLocation.getLatitude() + ","
//					+ detectedLocation.getLongitude() + ". Sending loiter command.");
//		}

	}
	
	void processEntityState(EntityState entityState,OutputStream out) throws Exception
	{//sagirs code 
		float energyAvail = entityState.getEnergyAvailable();
		float pitch = entityState.getPitch();
		float energyrate = entityState.getActualEnergyRate();
		long vehicle_ID = entityState.getID();
		float vx = entityState.getU();
		float vy = entityState.getV();
		float vz = entityState.getW();
		float ax = entityState.getU();
		float ay = entityState.getVdot();
		float az = entityState.getWdot();
		Location3D loc = entityState.getLocation();
		long time = entityState.getTime();

		// distance from charging point

		Location3D myLoc = loc;
		double distanceToChargingpoint = usc.computeDistance(myLoc.getLatitude(), clat, myLoc.getLongitude(), clongt);

		System.out.println(entityState.getID());

		if (energyAvail <= 25) {
			usc.sendMissionCommand = false;
			Location3D c = new Location3D(clat, clongt, 0, afrl.cmasi.AltitudeType.MSL);

			// goForCharge(out,((afrl.cmasi.EntityState) o).getID(), c);

			try {
				usc.sendKnownMission(out, c);
			} finally {
				usc.sendMissionCommand = true;
			}

	}}

	/**
	 * Reads in messages being sent out by the AMASE Server
	 */
	public void readMessages(InputStream in, OutputStream out) throws Exception {
		// Use each of the if statements to use the incoming message
		System.gc(); 
		LMCPObject o = LMCPFactory.getObject(in);
		if (o instanceof afrl.cmasi.EntityState)
		{
			processEntityState((EntityState)o,out);
		}
		 if (o instanceof afrl.cmasi.AirVehicleState)
		 {
			 ffc.processAirVehicleState(((AirVehicleState)o), out);
		 }
	       if (o instanceof afrl.cmasi.searchai.HazardZoneDetection) {
	    	   if (currentStage == STAGE.UnknownSearch)
	    	   currentStage = STAGE.FollowFire;
	    	   if (currentStage == STAGE.UnknownSearch)
	    	   {HazardZoneDetection hazardDetected = ((HazardZoneDetection) o);
	    		   processHazardZoneDetection(out, hazardDetected);
	    		   
	    	   }
	    	   else
	    	   {
	        	System.out.println("handled");
	        	HazardZoneDetection hzd = ((HazardZoneDetection)o);
	        	if(hzd.getDetectedHazardZoneType().getValue() != 0) {
	        		ffc.handleHazard(hzd);
	        	}}
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
				Logger.getLogger(cleanClient.class.getName()).log(Level.SEVERE, null, ex1);
			}
			return connect(host, port);
		}
		System.out.println("Connected to " + host + ":" + port);
		return socket;
	}

	public static void main(String[] args) {
		new cleanClient().start();
	}
}
