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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Connects to the simulation and sends a fake mission command to every UAV that is requested in the plan request.
 */
public class Online0JavaClient extends Thread {

    /** simulation TCP port to connect to */
    private static int port = 5555;
    /** address of the server */
    private static String host = "localhost";
    /**Array of booleans indicating if loiter command has been sent to each UAV */
    boolean[] uavsLoiter = new boolean[4];
    Polygon estimatedHazardZone = new Polygon();
    boolean missionCommandSent = false;
    boolean hintsSent = false;
    /** scenario clock time */
    private static long scenarioTime = 0;

    public Online0JavaClient() {
    }

    @Override
    public void run() {
        try {
            // connect to the server
            Socket socket = connect(host, port);

            while(true) {
                //Continually read the LMCP messages that AMASE is sending out
                readMessages(socket.getInputStream(), socket.getOutputStream());
                if(scenarioTime >= 3000 && missionCommandSent == false){
                    sendMissionCommand(socket.getOutputStream());
                    missionCommandSent = true;
                }
            }

        } catch (Exception ex) {
            Logger.getLogger(Online0JavaClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Sends mission command to the AMASE Server
     * @param out
     * @throws Exception
     */
    public void sendMissionCommand(OutputStream out) throws Exception {
        VehicleActionCommand o = new VehicleActionCommand();
        o.setVehicleID(1);
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
        Location3D location = new Location3D(1.494, -132.5352, 0, afrl.cmasi.AltitudeType.MSL);
        loiterAction.setLocation(location);

        //Adding the loiter action to the vehicle action list
        o.getVehicleActionList().add(loiterAction);

        //Sending the Vehicle Action Command message to AMASE to be interpreted
        out.write(avtas.lmcp.LMCPFactory.packMessage(o, true));

        VehicleActionCommand p = new VehicleActionCommand();
        p.setVehicleID(2);
        p.setStatus(CommandStatusType.Pending);
        p.setCommandID(2);

        //Setting up the loiter action
        LoiterAction loiterAction2 = new LoiterAction();
        loiterAction2.setLoiterType(LoiterType.Circular);
        loiterAction2.setRadius(250);
        loiterAction2.setAxis(0);
        loiterAction2.setLength(0);
        loiterAction2.setDirection(LoiterDirection.Clockwise);
        loiterAction2.setDuration(15000);
        loiterAction2.setAirspeed(15);

        //Creating a 3D location object f
        Location3D location2 = new Location3D(1.493, -132.5379, 0, afrl.cmasi.AltitudeType.MSL);
        loiterAction2.setLocation(location2);

        //Adding the loiter action to the vehicle action list
        p.getVehicleActionList().add(loiterAction2);

        //Sending the Vehicle Action Command message to AMASE to be interpreted
        out.write(avtas.lmcp.LMCPFactory.packMessage(p, true));
    }

    /**
     * Sends loiter command to the AMASE Server
     * @param out
     * @throws Exception 
     */
    public void sendLoiterCommand (OutputStream out, long vehicleId , Location3D location) throws Exception {
        //Setting up the mission to send to the UAV
         VehicleActionCommand o = new VehicleActionCommand();
         o.setVehicleID(vehicleId);
         o.setStatus(CommandStatusType.Pending);
         o.setCommandID(1);
         
         //Setting up the loiter action
         LoiterAction loiterAction = new LoiterAction();
         loiterAction.setLoiterType(LoiterType.Circular);
         loiterAction.setRadius(250);
         loiterAction.setAxis(0);
         loiterAction.setLength(0);
         loiterAction.setDirection(LoiterDirection.Clockwise);
         loiterAction.setDuration(100000);
         loiterAction.setAirspeed(15);
         
         //Creating a 3D location object for the stare point
         loiterAction.setLocation(location);
         
         //Adding the loiter action to the vehicle action list
         o.getVehicleActionList().add(loiterAction);
         
         //Sending the Vehicle Action Command message to AMASE to be interpreted
         out.write(avtas.lmcp.LMCPFactory.packMessage(o, true));
    }

    /**
     * Sends loiter command to the AMASE Server
     * @param out
     * @throws Exception
     */
    public void sendEstimateReport(OutputStream out, Polygon estimatedShape) throws Exception {
        //Setting up the mission to send to the UAV
        HazardZoneEstimateReport o = new HazardZoneEstimateReport();
        o.setEstimatedZoneShape(estimatedShape);
        o.setUniqueTrackingID(1);
        o.setEstimatedGrowthRate(0);
        o.setPerceivedZoneType(afrl.cmasi.searchai.HazardType.Fire);
        o.setEstimatedZoneDirection(0);
        o.setEstimatedZoneSpeed(0);


        //Sending the Vehicle Action Command message to AMASE to be interpreted
        out.write(avtas.lmcp.LMCPFactory.packMessage(o, true));
    }

    /**
    * Reads in messages being sent out by the AMASE Server
    */
    public void readMessages(InputStream in, OutputStream out) throws Exception {
        //Use each of the if statements to use the incoming message
        LMCPObject o = LMCPFactory.getObject(in);
        //Check if the message is a HazardZoneDetection
        if (o instanceof afrl.cmasi.searchai.HazardZoneDetection) {
            HazardZoneDetection hazardDetected = ((HazardZoneDetection) o);
            //Get location where zone first detected
            Location3D detectedLocation = hazardDetected.getDetectedLocation();
            //Get entity that detected the zone
            int detectingEntity = (int) hazardDetected.getDetectingEnitiyID();

            //Check if hint
            if (detectingEntity == 0) {
                //Do nothing for now, hints will be added later
                return;
            }

            //Note: Polygon points must be in clockwise or counter-clockwise order to get a shape without intersection
            if (uavsLoiter[detectingEntity - 1] == false) {
                //Send the loiter command
                sendLoiterCommand(out, detectingEntity, detectedLocation);

                //Note: Polygon points must be in clockwise or counter-clockwise order to get a shape without intersections
                estimatedHazardZone.getBoundaryPoints().add(detectedLocation);

                //Send out the estimation report to draw the polygon
                sendEstimateReport(out, estimatedHazardZone);

                uavsLoiter[detectingEntity - 1] = true;
                System.out.println("UAV" + detectingEntity + " detected hazard at " + detectedLocation.getLatitude() +
                        "," + detectedLocation.getLongitude() + ". Sending loiter command.");
            }
            if (uavsLoiter[0] == true && uavsLoiter[1] == true && hintsSent == false) {
                Location3D hintLocation2 = new Location3D(1.494, -132.5352, 0, afrl.cmasi.AltitudeType.MSL);
                Location3D hintLocation3 = new Location3D(1.493, -132.5379, 0, afrl.cmasi.AltitudeType.MSL);
                Location3D hintLocation1 = new Location3D(1.4947, -132.5389, 0, afrl.cmasi.AltitudeType.MSL);
                estimatedHazardZone.getBoundaryPoints().add(hintLocation1);
                sendEstimateReport(out, estimatedHazardZone);
                estimatedHazardZone.getBoundaryPoints().add(hintLocation2);
                sendEstimateReport(out, estimatedHazardZone);
                estimatedHazardZone.getBoundaryPoints().add(hintLocation3);
                sendEstimateReport(out, estimatedHazardZone);
                hintsSent = true;
            }
        }
        else if (o instanceof afrl.cmasi.SessionStatus) {
            //Example of using an incoming LMCP message
            scenarioTime = ((SessionStatus) o).getScenarioTime();
            System.out.println(o.toString());
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
                Logger.getLogger(Online0JavaClient.class.getName()).log(Level.SEVERE, null, ex1);
            }
            return connect(host, port);
        }
        System.out.println("Connected to " + host + ":" + port);
        return socket;
    }

    public static void main(String[] args) {
        new Online0JavaClient().start();
    }
}
