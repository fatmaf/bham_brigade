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
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Connects to the simulation and sends a fake mission command to every UAV that
 * is requested in the plan request.
 */
public class MoveUAVs extends Thread {

	/** simulation TCP port to connect to */
	private static int port = 5555;
	/** address of the server */
	private static String host = "localhost";
	/** Array of booleans indicating if loiter command has been sent to each UAV */
	boolean[] uavsLoiter = new boolean[4];
	Polygon estimatedHazardZone = new Polygon();
	HashMap<Long,Location3D> uavLocations= new HashMap<Long,Location3D>();

	enum fsmState {
		selectUAV, moveUAV
	};

	enum movement {
		UP, DOWN, LEFT, RIGHT, END
	};

	public MoveUAVs() {
	}

	@Override
	public void run() {
		Scanner keyboard = new Scanner(System.in);
		try {
			// connect to the server
			Socket socket = connect(host, port);

			int uavNumber = -1;
			fsmState currentState = fsmState.selectUAV;
			while (true) {
				// Continually read the LMCP messages that AMASE is sending out
				readMessages(socket.getInputStream(), socket.getOutputStream());
				if (currentState == fsmState.selectUAV) {
					if (keyboard.hasNextInt()) {
						uavNumber = keyboard.nextInt();
						System.out.print("UAV Chosen:" + uavNumber);
						currentState = fsmState.moveUAV;

					}
				}
				if (currentState == fsmState.moveUAV) {
					double movex = 1; 
					double movey = 1; 
					if (uavNumber != -1) {
						movement mv = movement.END;
						// asdf
						switch (keyboard.next()) {
						case "a":
							mv = movement.UP;
							movex = 0; 
							movey = 0.1; 
							break;
						case "s":
							mv = movement.DOWN;
							movex = 0; 
							movey = -0.1;
							break;
						case "d":
							mv = movement.LEFT;
							movex = -0.1; 
							movey = 0;
							break;
						case "f":
							mv = movement.RIGHT;
							movex = 0.1; 
							movey = 0;
						default:
							mv = movement.END;
							break;
						}
						System.out.println("Moving UAV " + mv.toString());
						
						if (mv == movement.END) {
							currentState = fsmState.selectUAV;
						}
						else {
							if(uavLocations.containsKey((long)(uavNumber)))
								{
								Location3D uavLocation = uavLocations.get((long)uavNumber);
								System.out.println("UAV Location: "+uavLocation.toString());
								uavLocation.setLatitude(uavLocation.getLatitude()+movey);
								uavLocation.setLongitude(uavLocation.getLongitude()+movex); 
								sendLoiterCommand(socket.getOutputStream(),uavNumber,uavLocation);
								
								}
								}

					}
				}

			}

		} catch (Exception ex) {
			keyboard.close();
			Logger.getLogger(MoveUAVs.class.getName()).log(Level.SEVERE, null, ex);
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
		
		if (o instanceof afrl.cmasi.AirVehicleState) {
			AirVehicleState uavState = ((AirVehicleState) o);
		
			Location3D uavLocation = uavState.getLocation();
			uavLocations.put(uavState.getID(), uavLocation);
			System.out.println(uavState.getID());
		}
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
				Logger.getLogger(MoveUAVs.class.getName()).log(Level.SEVERE, null, ex1);
			}
			return connect(host, port);
		}
		System.out.println("Connected to " + host + ":" + port);
		return socket;
	}

	public static void main(String[] args) {
		new MoveUAVs().start();
	}
}
