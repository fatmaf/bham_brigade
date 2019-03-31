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
import afrl.cmasi.FlightDirectorAction;
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
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Connects to the simulation and sends a fake mission command to every UAV that is requested in the plan request.
 */
public class GridSweep extends Thread {

    /** simulation TCP port to connect to */
    private static int port = 5555;
    /** address of the server */
    private static String host = "localhost";
    private static long scenarioTime = 0;
    /**Array of booleans indicating if loiter command has been sent to each UAV */
    boolean[] uavsLoiter = new boolean[4];
    Polygon estimatedHazardZone = new Polygon();
    // that is the number of faster drones to conduct the fast search
    int numberOfUAVsSearch = 1;
    Boolean sendMissionCommand = true;
    double clat = 53.3783;
    double clongt = -1.7616;
    
    HashMap<Long, ArrayList<Boolean>> state = new HashMap<>();
    HashMap<Long, Boolean> searching = new HashMap<>();
    Polygon hazardPolygon = new Polygon();
    HashMap<Long, Location3D> detectedLocs = new HashMap<>();

    
    public GridSweep() {
    	
    	
    }
    


    @Override
    public void run() {
    	
    
        try {
            // connect to the server
        	
            Boolean sensorCommandSent = false;
            Boolean loiterCommandSent = false;
          
            
            Socket socket = connect(host, port);
            if(sendMissionCommand==true) {
            	
            	askUAVToSweep(socket.getOutputStream(), 1, 53.3471, -2.0076,53.4262,-1.8879, 0.015);
            	
            	
            
            }
            
            
         //  makingCross(socket.getOutputStream(),numberOfUAVsSearch,0.02, 0.02);
           
           //sending UAV to a point
           //sendKnownMission(socket.getOutputStream());
            
            while(true) {
            	//start_search(out);
            	
            	 
                //Continually read the LMCP messages that AMASE is sending out
                readMessages(socket.getInputStream(), socket.getOutputStream());
               
                
                
            }

        } catch (Exception ex) {
            Logger.getLogger(GridSweep.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
            }

    /**
     * Sends loiter command to the AMASE Server
     * @param out
     * @throws Exception 
     */
    
    
    //calculate distance i n meters
    public double computeDistance(double lat1, double lat2, double longt1, double longt2) {
    //I want use haversine formula a = sinsqr(dtheta)+costheta1+costheta2.sinsqr(dtheta)	 
    	// c = 2.atan2(sqrta, sqrt1-a)
    	//d= R.c
    	/* 
    	 * double theta1 = Math.toRadians(lat1);
    	double theta2 = Math.toRadians(lat2);
    	double dtheta = Math.toRadians(lat2-lat1);
    	double dlambda = Math.toRadians(longt2-longt1); 
    	 * */
    	
    	double R = 6371e3; //radius of earth in meters
    	double theta1 = lat1;
    	double theta2 = lat2;
    	double dtheta = lat2-lat1;
    	double dlambda = longt2-longt1;
    	double a = Math.sin(dtheta/2)*Math.sin(dtheta/2) + 
    			   Math.cos(theta1)*Math.cos(theta2) +
    			   Math.sin(dlambda/2) * Math.sin(dlambda/2);
    	double c = 2* Math.atan2(Math.sqrt(a),Math.sqrt(1-a));
    	//double c = 2* Math.asin(Math.sqrt(a));
    	double d = R*c;
    	return d;
   
    }
    

    /**
     * Sends loiter command to the AMASE Server
     * @param out
     * @throws Exception
     */
    
   
    
   


//ASK UAV to sweep
public void askUAVToSweep(OutputStream out, int id, double lat1, double lon1, double lat2, double lon2, double latinc) throws Exception {
	
	double circleadd = 0.0;
	//int
		
		int no =numberOfUAVsSearch;
		/*
		 double lat1 =	53.2951;
		  double lat2 =	53.3395;
		  double longt2 = -1.9093;
		  double startlon = -2.0071;
		 */ 
		
		 double lati1 =	lat1;
		  double lati2 =	lat2;
		  double longt2 = lon2;
		  double startlon = lon1;
		  
		  double diflat = lat2 - lat1; 
		  double diflongt = longt2 - startlon;
		  double longtshare = diflongt;
		  double latshare = diflat/no;
		  double latincrement = latinc;
	
		  double lon =startlon;
		  double longt1 = lon;
		 
		  int ct = 0;
		  int rd = 1;
	    //Setting up the mission to send to the UAV
	  
     MissionCommand o = new MissionCommand();
     o.setFirstWaypoint(1);
     //Setting the UAV to recieve the mission
     o.setVehicleID(id);
     o.setStatus(CommandStatusType.Pending);
     //Setting a unique mission command ID
     o.setCommandID(1);
     
     //Creating the list of waypoints to be sent with the mission command
     ArrayList<Waypoint> waypoints = new ArrayList<Waypoint>();
     //Creating the first waypoint
     //Note: all the following attributes must be set to avoid issues
     
     while(lat1<=lat2){
    	 
     Waypoint waypoint = new Waypoint();
     //Setting 3D coordinates
    
     if(ct == 0 ){
         waypoint.setLatitude(lat1);
         waypoint.setLongitude(longt1);
		 }
		 else if(ct == 1){
			
			waypoint.setLatitude(lat1);
			longt1 =longt1+longtshare;
			waypoint.setLongitude(longt1);
		 }
		 else if(ct % 2 == 0){
			 lat1=lat1+latincrement;
			 waypoint.setLatitude(lat1);
			 waypoint.setLongitude(longt1);
			 
		 }
     
		 else{
			 rd++;
			 
			 if(rd % 2==0) {
				
				 longt1 = longt1-longtshare;	
					waypoint.setLatitude(lat1);
					waypoint.setLongitude(longt1);
			 }
			 else {
				 longt1 = longt1+longtshare;	
					waypoint.setLatitude(lat1);
					waypoint.setLongitude(longt1);
			 }
			
			
		 }
     
         
     waypoint.setAltitude(150);
     waypoint.setAltitudeType(AltitudeType.MSL);
     //Setting unique ID for the waypoint
     waypoint.setNumber(ct);
     waypoint.setNextWaypoint(ct+1);
     //Setting speed to reach the waypoint
     waypoint.setSpeed(30);
     waypoint.setSpeedType(SpeedType.Airspeed);
     //Setting the climb rate to reach new altitude (if applicable)
     waypoint.setClimbRate(0);
     waypoint.setTurnType(TurnType.TurnShort);
     //Setting backup waypoints if new waypoint can't be reached
     waypoint.setContingencyWaypointA(ct-1);
     waypoint.setContingencyWaypointB(ct-2);
     
     waypoints.add(waypoint);
     
     ct++;
     
     //rd++;
     }
    
     //Mission fuel analysis will goes here.
   
     
     //Setting the waypoint list in the mission command
     o.getWaypointList().addAll(waypoints);
     
     //Sending the Mission Command message to AMASE to be interpreted
     out.write(avtas.lmcp.LMCPFactory.packMessage(o, true));
     System.out.println("Mission sent to UAV3 "+id+" "+ "Diagonal istance is "+computeDistance(lat1,lat2,longt1,longt2)+" meters");
     circleadd = circleadd + longtshare;
	 
		
	}


public void readMessages(InputStream in, OutputStream out) throws Exception {
    //Use each of the if statements to use the incoming message
    LMCPObject o = LMCPFactory.getObject(in);
    //Check if the message is a HazardZoneDetection
    int detection = 0;
    
    	
    if (o instanceof afrl.cmasi.searchai.HazardZoneDetection) {
    	
    	//detection++;
        HazardZoneDetection hazardDetected = ((HazardZoneDetection) o);
        //Get location where zone first detected
        //stop the search
       // sendMissionCommand = false;
        Location3D detectedLocation = hazardDetected.getDetectedLocation();
        //Get entity that detected the zone
       
        int detectingEntity = (int) hazardDetected.getDetectingEnitiyID();
      
      //  estimatedHazardZone.getBoundaryPoints().add(detectedLocation);
        System.out.println("UAV"+detectingEntity+" detected fire" +"at Lat: "+detectedLocation.getLatitude() + "and Lon: "+detectedLocation.getLongitude());
       
      
      
   }
    
    
}

//check for charge

public Boolean checkingForCharge(OutputStream out,InputStream in, int id)throws Exception{
 	Boolean go = false;
 	
 LMCPObject o = LMCPFactory.getObject(in);
 
 if (o instanceof afrl.cmasi.EntityState) {  
    	EntityState myVehicle = ((EntityState) o);
    	myVehicle.setID((id));
    	float energyAvail = ((afrl.cmasi.EntityState) o).getEnergyAvailable();
    	float pitch = ((afrl.cmasi.EntityState) o).getPitch();
    	float energyrate = ((afrl.cmasi.EntityState) o).getActualEnergyRate();
    	long vehicle_ID = ((afrl.cmasi.EntityState) o).getID();
    	float vx =((afrl.cmasi.EntityState) o).getU();
    	float vy =((afrl.cmasi.EntityState) o).getV();
    	float vz = ((afrl.cmasi.EntityState) o).getW();
    	float ax = ((afrl.cmasi.EntityState) o).getU();
    	float ay = ((afrl.cmasi.EntityState) o).getVdot();
    	float az = ((afrl.cmasi.EntityState) o).getWdot();
    	Location3D loc = ((afrl.cmasi.EntityState) o).getLocation();
    	long time =((afrl.cmasi.EntityState) o).getTime();
    	
    	//distance fro
    	Location3D myLoc = ((afrl.cmasi.EntityState) o).getLocation();
    	double distanceToChargingpoint = computeDistance(myLoc.getLatitude(),clat,myLoc.getLongitude(),clongt);
    	double rate = 20/energyrate; //that is rate in m/%
    	
    	System.out.println("Avail in percent "+energyrate);
    	
    		if(energyAvail <= 50) {
    			
    			//stop the current mission
    			//sendMissionCommand = false;
    		// send UAV for charging
    		go = true;
    		
    		//goForCharge(out,((afrl.cmasi.EntityState) o).getID(), c);
    		
    		}else {
    			go = false;
    		}
    	}
 return go;

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
                Logger.getLogger(GridSweep.class.getName()).log(Level.SEVERE, null, ex1);
            }
            return connect(host, port);
        }
        System.out.println("Sagir Server Connected to " + host + ":" + port);
        return socket;
    }

    public static void main(String[] args) {
        new GridSweep().start();
    }
}
