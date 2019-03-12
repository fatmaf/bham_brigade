package base_client;// ===============================================================================
// Authors: Jacob Allex-Buckner
// Organization: University of Dayton Research Institute Applied Sensing Division
//
// Copyright (c) 2018 Government of the United State of America, as represented by
// the Secretary of the Air Force.  No copyright is claimed in the United States under
// Title 17, U.S. Code.  All Other Rights Reserved.
// ===============================================================================

// This file was auto-created by LmcpGen. Modifications will be overwritten.

import base_client.cmasi.*;
import base_client.avtas.lmcp.LMCPFactory;
import base_client.avtas.lmcp.LMCPObject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Connects to the simulation and sends a fake mission command to every UAV that is requested in the plan request.
 */
public class AMASEJavaClient extends Thread {

    /** simulation TCP port to connect to */
    private static int port = 5555;
    /** address of the server */
    private static String host = "localhost";
    /** scenario clock time */
    private static long scenarioTime = 0;

    public AMASEJavaClient() {
    }

    @Override
    public void run() {
        try {
            // connect to the server
            Socket socket = connect(host, port);
            Boolean missionCommandSent = false;
            Boolean sensorCommandSent = false;
            Boolean loiterCommandSent = false;
            while(true) {
                //Continually read the LMCP messages that AMASE is sending out
                readMessages(socket.getInputStream());
                //Example of how to send a message after 15 seconds of the scenario starting
                if(scenarioTime >= 15000 && missionCommandSent == false){
                    //Send a message to change the 1st entity's waypoints
                    sendMissionCommand(socket.getOutputStream());
                    missionCommandSent = true;
                }
                if(scenarioTime >= 40000 && sensorCommandSent == false) {
                    sendSensorCommand(socket.getOutputStream());
                    sensorCommandSent = true;
                }
                if(scenarioTime >= 100000 && loiterCommandSent == false) {
                    sendLoiterCommand(socket.getOutputStream());
                    loiterCommandSent = true;
                }
            }

        } catch (Exception ex) {
            Logger.getLogger(AMASEJavaClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Sends mission command to the AMASE Server
     * @param out
     * @throws Exception 
     */
    public void sendMissionCommand(OutputStream out) throws Exception {
        //Setting up the mission to send to the UAV
         MissionCommand o = new MissionCommand();
         o.setFirstWaypoint(1);
         //Setting the UAV to recieve the mission
         o.setVehicleID(1);
         o.setStatus(CommandStatusType.Pending);
         //Setting a unique mission command ID
         o.setCommandID(1);
         //Creating the list of waypoints to be sent with the mission command
         ArrayList<Waypoint> waypoints = new ArrayList<Waypoint>();
         //Creating the first waypoint
         //Note: all the following attributes must be set to avoid issues
         Waypoint waypoint1 = new Waypoint();
         //Setting 3D coordinates
         waypoint1.setLatitude(1.505);
         waypoint1.setLongitude(-132.539);
         waypoint1.setAltitude(100);
         waypoint1.setAltitudeType(AltitudeType.MSL);
         //Setting unique ID for the waypoint
         waypoint1.setNumber(1);
         waypoint1.setNextWaypoint(2);
         //Setting speed to reach the waypoint
         waypoint1.setSpeed(30);
         waypoint1.setSpeedType(SpeedType.Airspeed);
         //Setting the climb rate to reach new altitude (if applicable)
         waypoint1.setClimbRate(0);
         waypoint1.setTurnType(TurnType.TurnShort);
         //Setting backup waypoints if new waypoint can't be reached
         waypoint1.setContingencyWaypointA(0);
         waypoint1.setContingencyWaypointB(0);
         
         //Setting up the second waypoint to be sent in the mission command
         Waypoint waypoint2 = new Waypoint();
         waypoint2.setLatitude(1.52);
         waypoint2.setLongitude(-132.51);
         waypoint2.setAltitude(100);
         waypoint2.setAltitudeType(AltitudeType.MSL);
         waypoint2.setNumber(2);
         waypoint2.setNextWaypoint(1);
         waypoint2.setSpeed(30);
         waypoint2.setSpeedType(SpeedType.Airspeed);
         waypoint2.setClimbRate(0);
         waypoint2.setTurnType(TurnType.TurnShort);
         waypoint2.setContingencyWaypointA(0);
         waypoint2.setContingencyWaypointB(0);
         
         //Adding the waypoints to the waypoint list
         waypoints.add(waypoint1);
         waypoints.add(waypoint2);
         
         //Setting the waypoint list in the mission command
         o.getWaypointList().addAll(waypoints);
         
         //Sending the Mission Command message to AMASE to be interpreted
         out.write(LMCPFactory.packMessage(o, true));
    }
    
    /**
     * Sends gimbal stare command to the AMASE Server
     * @param out
     * @throws Exception 
     */
    public void sendSensorCommand (OutputStream out) throws Exception {
        //Setting up the mission to send to the UAV
         VehicleActionCommand o = new VehicleActionCommand();
         o.setVehicleID(1);
         o.setStatus(CommandStatusType.Pending);
         o.setCommandID(1);
         
         //Setting up the vehical action command list
         ArrayList<VehicleAction> vehicleActionList = new ArrayList<VehicleAction>();
         
         //Setting up the gimbal stare vehicle action
         GimbalStareAction gimbalStareAction = new GimbalStareAction();
         gimbalStareAction.setPayloadID(1);
         gimbalStareAction.setDuration(1000000);
         
         //Creating a 3D location object for the stare point
         Location3D location = new Location3D(1.52, -132.51, 0, AltitudeType.MSL);
         gimbalStareAction.setStarepoint(location);
         
         //Adding the gimbal stare action to the vehicle action list
         vehicleActionList.add(gimbalStareAction);
         o.getVehicleActionList().addAll(vehicleActionList);
         
         //Sending the Vehicle Action Command message to AMASE to be interpreted
         out.write(LMCPFactory.packMessage(o, true));
    }
    
    /**
     * Sends loiter command to the AMASE Server
     * @param out
     * @throws Exception 
     */
    public void sendLoiterCommand (OutputStream out) throws Exception {
        //Setting up the mission to send to the UAV
         VehicleActionCommand o = new VehicleActionCommand();
         o.setVehicleID(1);
         o.setStatus(CommandStatusType.Pending);
         o.setCommandID(1);
         
         //Setting up the loiter action
         LoiterAction loiterAction = new LoiterAction();
         loiterAction.setLoiterType(LoiterType.Circular);
         loiterAction.setRadius(500);
         loiterAction.setAxis(0);
         loiterAction.setLength(0);
         loiterAction.setDirection(LoiterDirection.Clockwise);
         loiterAction.setDuration(100000);
         loiterAction.setAirspeed(30);
         
         //Creating a 3D location object for the stare point
         Location3D location = new Location3D(1.52, -132.51, 0, AltitudeType.MSL);
         loiterAction.setLocation(location);
         
         //Adding the loiter action to the vehicle action list
         o.getVehicleActionList().add(loiterAction);
         
         //Sending the Vehicle Action Command message to AMASE to be interpreted
         out.write(LMCPFactory.packMessage(o, true));
    }

    /**
    * Reads in messages being sent out by the AMASE Server
    */
    public void readMessages(InputStream in) throws Exception {
        //Use each of the if statements to use the incoming message
        LMCPObject o = LMCPFactory.getObject(in);
        System.out.println(o.getLMCPTypeName());
        if (o instanceof AbstractGeometry) {
            System.out.println(o.toString());
        } else if (o instanceof KeyValuePair) {
            System.out.println(o.toString());
        } else if (o instanceof Location3D) {
            System.out.println(o.toString());
        } else if (o instanceof PayloadAction) {
            System.out.println(o.toString());
        } else if (o instanceof PayloadConfiguration) {
            System.out.println(o.toString());
        } else if (o instanceof PayloadState) {
            System.out.println(o.toString());
        } else if (o instanceof VehicleAction) {
            System.out.println(o.toString());
        } else if (o instanceof Task) {
            System.out.println(o.toString());
        } else if (o instanceof SearchTask) {
            System.out.println(o.toString());
        } else if (o instanceof AbstractZone) {
            System.out.println(o.toString());
        } else if (o instanceof EntityConfiguration) {
            System.out.println(o.toString());
        } else if (o instanceof FlightProfile) {
            System.out.println(o.toString());
        } else if (o instanceof AirVehicleConfiguration) {
            System.out.println(o.toString());
        } else if (o instanceof EntityState) {
            System.out.println(o.toString());
        } else if (o instanceof AirVehicleState) {
            System.out.println(o.toString());
        } else if (o instanceof Wedge) {
            System.out.println(o.toString());
        } else if (o instanceof AreaSearchTask) {
            System.out.println(o.toString());
        } else if (o instanceof CameraAction) {
            System.out.println(o.toString());
        } else if (o instanceof CameraConfiguration) {
            System.out.println(o.toString());
        } else if (o instanceof GimballedPayloadState) {
            System.out.println(o.toString());
        } else if (o instanceof CameraState) {
            System.out.println(o.toString());
        } else if (o instanceof Circle) {
            System.out.println(o.toString());
        } else if (o instanceof GimbalAngleAction) {
            System.out.println(o.toString());
        } else if (o instanceof GimbalConfiguration) {
            System.out.println(o.toString());
        } else if (o instanceof GimbalScanAction) {
            System.out.println(o.toString());
        } else if (o instanceof GimbalStareAction) {
            System.out.println(o.toString());
        } else if (o instanceof GimbalState) {
            System.out.println(o.toString());
        } else if (o instanceof GoToWaypointAction) {
            System.out.println(o.toString());
        } else if (o instanceof KeepInZone) {
            System.out.println(o.toString());
        } else if (o instanceof KeepOutZone) {
            System.out.println(o.toString());
        } else if (o instanceof LineSearchTask) {
            System.out.println(o.toString());
        } else if (o instanceof NavigationAction) {
            System.out.println(o.toString());
        } else if (o instanceof LoiterAction) {
            System.out.println(o.toString());
        } else if (o instanceof LoiterTask) {
            System.out.println(o.toString());
        } else if (o instanceof Waypoint) {
            System.out.println(o.toString());
        } else if (o instanceof MissionCommand) {
            System.out.println(o.toString());
        } else if (o instanceof MustFlyTask) {
            System.out.println(o.toString());
        } else if (o instanceof OperatorSignal) {
            System.out.println(o.toString());
        } else if (o instanceof OperatingRegion) {
            System.out.println(o.toString());
        } else if (o instanceof AutomationRequest) {
            System.out.println(o.toString());
        } else if (o instanceof PointSearchTask) {
            System.out.println(o.toString());
        } else if (o instanceof Polygon) {
            System.out.println(o.toString());
        } else if (o instanceof Rectangle) {
            System.out.println(o.toString());
        } else if (o instanceof RemoveTasks) {
            System.out.println(o.toString());
        } else if (o instanceof ServiceStatus) {
            System.out.println(o.toString());
        } else if (o instanceof SessionStatus) {
            //Example of using an incoming LMCP message
            scenarioTime = ((SessionStatus) o).getScenarioTime();
            System.out.println(o.toString());
        } else if (o instanceof VehicleActionCommand) {
            System.out.println(o.toString());
        } else if (o instanceof VideoStreamAction) {
            System.out.println(o.toString());
        } else if (o instanceof VideoStreamConfiguration) {
            System.out.println(o.toString());
        } else if (o instanceof VideoStreamState) {
            System.out.println(o.toString());
        } else if (o instanceof AutomationResponse) {
            System.out.println(o.toString());
        } else if (o instanceof RemoveZones) {
            System.out.println(o.toString());
        } else if (o instanceof RemoveEntities) {
            System.out.println(o.toString());
        } else if (o instanceof FlightDirectorAction) {
            System.out.println(o.toString());
        } else if (o instanceof WeatherReport) {
            System.out.println(o.toString());
        } else if (o instanceof FollowPathCommand) {
            System.out.println(o.toString());
        } else if (o instanceof PathWaypoint) {
            System.out.println(o.toString());
        } else if (o instanceof StopMovementAction) {
            System.out.println(o.toString());
        } else if (o instanceof WaypointTransfer) {
            System.out.println(o.toString());
        } else if (o instanceof PayloadStowAction) {
            System.out.println(o.toString());
        } else {
            //Don't do anything if the message isn't for AMASE
            System.out.println("Could not read byte");
        }

    }


    /** tries to connect to the server.  If there is a problem (such as the server not running yet) it
     *  pauses, then tries again.  If the server quits and restarts, this method is called by the thread
     *  in order to re-establish communication.
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
                Logger.getLogger(AMASEJavaClient.class.getName()).log(Level.SEVERE, null, ex1);
            }
            return connect(host, port);
        }
        System.out.println("Connected to " + host + ":" + port);
        return socket;
    }

    public static void main(String[] args) {
        new AMASEJavaClient().start();
    }
}
