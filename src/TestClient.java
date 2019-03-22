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
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Connects to the simulation and sends a fake mission command to every UAV that is requested in the plan request.
 */
public class TestClient extends Thread {

    /** simulation TCP port to connect to */
    private static int port = 5555;
    /** address of the server */
    private static String host = "localhost";
    private static long scenarioTime = 0;
    /**Array of booleans indicating if loiter command has been sent to each UAV */
    boolean[] uavsLoiter = new boolean[4];
    Polygon estimatedHazardZone = new Polygon();
    int numberOfUAVs = 4;
    Boolean sendMissionCommand = true;
    double x,y;
    public TestClient(double x, double y) {
    	
    	this.x=x; 
        this.y=y;
    }
    


    @Override
    public void run() {
    	
    	
        try {
            // connect to the server
        	
            Boolean sensorCommandSent = false;
            Boolean loiterCommandSent = false;
            
            Socket socket = connect(host, port);
            if(sendMissionCommand==true) {}
            sendMissionCommand(socket.getOutputStream(),numberOfUAVs);
            while(true) {
            	//start_search(out);
            	
            	 
                //Continually read the LMCP messages that AMASE is sending out
                readMessages(socket.getInputStream(), socket.getOutputStream());
               
                
                
            }

        } catch (Exception ex) {
            Logger.getLogger(TestClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
            }

    /**
     * Sends loiter command to the AMASE Server
     * @param out
     * @throws Exception 
     */
    public void sendLoiterCommand (OutputStream out,InputStream in, long vehicleId , Location3D location) throws Exception {
    	
   	
    	//LMCPObject o1 = LMCPFactory.getObject(in);
    	//Setting up the mission to send to the UAV
         VehicleActionCommand o = new VehicleActionCommand();
         o.setVehicleID(vehicleId);
         o.setStatus(CommandStatusType.Pending);
         o.setCommandID(1);
         
        // Location3D newloc = new Location3D();
        // newloc.setLatitude(location.getLatitude()+0.002);
        // newloc.setLongitude(location.getLongitude()+0.002);
         //Setting up the loiter action
         LoiterAction loiterAction = new LoiterAction();
         loiterAction.setLoiterType(LoiterType.Circular);
         loiterAction.setRadius(10);
         loiterAction.setAxis(0);
         loiterAction.setLength(0);
         loiterAction.setDirection(LoiterDirection.Clockwise);
         loiterAction.setDuration(100);
         loiterAction.setAirspeed(15);
         
         //Creating a 3D location object for the stare point
         loiterAction.setLocation(location);
         
         //Adding the loiter action to the vehicle action list
         o.getVehicleActionList().add(loiterAction);
         
         //Sending the Vehicle Action Command message to AMASE to be interpreted
         out.write(avtas.lmcp.LMCPFactory.packMessage(o, true));
         
         
        /*
    	while (o1 instanceof afrl.cmasi.searchai.HazardZoneDetection) {
    	    	
    	        HazardZoneDetection hazardDetected = ((HazardZoneDetection) o1);
    	        //Get location where zone first detected
    	        sendMissionCommand = false;
    	        Location3D detectedLocation = hazardDetected.getDetectedLocation();
    	        //Get entity that detected the zone
    	       
    	        int detectingEntity = (int) hazardDetected.getDetectingEnitiyID();
    	      
    	      
    	
         
         sendLoiterCommand(out,in, detectingEntity, detectedLocation);}
         */
    }
    
    
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
    
    public void planForMapping() {
    	
    }
    
    
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
   
    
public void start_Search(OutputStream out) throws Exception{
    	
    	double lat1 =	1.4854;
		  double lat2 =	1.5017;
		  double longt2 = -132.528;
		  double longt1 = -132.5472;
		  double diflat = lat2 - lat1; 
		  double diflongt = longt2 - longt1;
		  int numberOfUAVs = 2;
		  double longtshare = diflongt/numberOfUAVs;
		  double latshare = diflat/numberOfUAVs;
		  int UAV_id1 = 1;
		  int UAV_id2 = 2;
		  double latincrement = 0.002; //note for is a constant defending on our altitude and sensor coverage.
		  
    //Setting up the mission to send to the UAV 1
     MissionCommand o = new MissionCommand();
     o.setFirstWaypoint(1);
     //Setting the UAV to recieve the mission
     o.setVehicleID(2);
     o.setStatus(CommandStatusType.Pending);
     //Setting a unique mission command ID
     o.setCommandID(1);
     //Creating the list of waypoints to be sent with the mission command
     ArrayList<Waypoint> waypoints = new ArrayList<Waypoint>();
     //Creating the first waypoint
     //Note: all the following attributes must be set to avoid issues
     Waypoint waypoint1 = new Waypoint();
     //Setting 3D coordinates
     waypoint1.setLatitude(lat1);
     waypoint1.setLongitude(longt1);
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
     
     
     
     Waypoint waypoint2 = new Waypoint();
     //Setting 3D coordinates
     waypoint2.setLatitude(lat1+0.002);
     waypoint2.setLongitude(longt1+0.002);
     waypoint2.setAltitude(100);
     waypoint2.setAltitudeType(AltitudeType.MSL);
     //Setting unique ID for the waypoint
     waypoint2.setNumber(1);
     waypoint2.setNextWaypoint(2);
     //Setting speed to reach the waypoint
     waypoint2.setSpeed(35);
     waypoint2.setSpeedType(SpeedType.Airspeed);
     //Setting the climb rate to reach new altitude (if applicable)
     waypoint2.setClimbRate(0);
     waypoint2.setTurnType(TurnType.TurnShort);
     //Setting backup waypoints if new waypoint can't be reached
     waypoint2.setContingencyWaypointA(0);
     waypoint2.setContingencyWaypointB(0);
     
     
     
     waypoints.add(waypoint1);
     waypoints.add(waypoint2);
     o.getWaypointList().addAll(waypoints);
     out.write(avtas.lmcp.LMCPFactory.packMessage(o, true));
     
     System.out.println("Mission sent to UAV1");

    }

	//call UAV function

public void callUAV(OutputStream out, Location3D location, int id) throws Exception {
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
     Waypoint waypoint1 = new Waypoint();
     //Setting 3D coordinates
     waypoint1.setLatitude(location.getLatitude()-0.000001);
     waypoint1.setLongitude(location.getLongitude()-0.000001);
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
     
    
     
     //Adding the waypoints to the waypoint list
     waypoints.add(waypoint1);
   
     
     //Setting the waypoint list in the mission command
     o.getWaypointList().addAll(waypoints);
     
     //Sending the Mission Command message to AMASE to be interpreted
     out.write(avtas.lmcp.LMCPFactory.packMessage(o, true));
    // System.out.println("current vehcle latitude"+o.getLatitude);
	}





//searching command
public void sendMissionCommand(OutputStream out, int numberOfUAVs) throws Exception {
	
	double circleadd = 0.0;
	//int
	for(int n=1; n<=numberOfUAVs; n++) {
		
		
		  double lat1 =	53.4463;
		  double lat2 =	53.4914;
		  double longt2 = -1.8341;
		  double startlon = -1.7584;
		  double lon =-1.7584+circleadd;
		  double longt1 = lon;
		  double diflat = lat2 - lat1; 
		  double diflongt = longt2 - startlon;
		  double longtshare = diflongt/numberOfUAVs;
		  double latshare = diflat/numberOfUAVs;
	
		  int UAV_id1 = 1;
		  int UAV_id2 = 2;
		  double latincrement = 0.0017;
		  int ct = 0;
		  int rd = 1;
	    //Setting up the mission to send to the UAV
	  
     MissionCommand o = new MissionCommand();
     o.setFirstWaypoint(1);
     //Setting the UAV to recieve the mission
     o.setVehicleID(n);
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
		
		 
		
		
     
     waypoint.setAltitude(100);
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
    
     //Adding the waypoints to the waypoint list
   
     
     //Setting the waypoint list in the mission command
     o.getWaypointList().addAll(waypoints);
     
     //Sending the Mission Command message to AMASE to be interpreted
     out.write(avtas.lmcp.LMCPFactory.packMessage(o, true));
     System.out.println("Mission sent to UAV "+n + "Diagonal istance is "+computeDistance(lat1,lat2,longt1,longt2)+" meters");
     circleadd = circleadd + longtshare;
	 }//en of for statement
}

//mission for UAV 2

public static double orientation(TestClient p, TestClient q,TestClient r) 
{ 
    double val = (q.y - p.y) * (r.x - q.x) - 
              (q.x - p.x) * (r.y - q.y); 
   
    if (val == 0) return 0;  // collinear 
    return (val > 0)? 1: 2; // clock or counterclock wise 
} 

public static void convexHull(TestClient points[], int n) 
{ 
    // There must be at least 3 points 
    if (n < 3) return; 
   
    // Initialize Result 
    Vector<TestClient> hull = new Vector<TestClient>(); 
   
    // Find the leftmost point 
    int l = 0; 
    for (int i = 1; i < n; i++) 
        if (points[i].x < points[l].x) 
            l = i; 
   
    // Start from leftmost point, keep moving  
    // counterclockwise until reach the start point 
    // again. This loop runs O(h) times where h is 
    // number of points in result or output. 
    int p = l, q; 
    do
    { 
        // Add current point to result 
        hull.add(points[p]); 
   
        // Search for a point 'q' such that  
        // orientation(p, x, q) is counterclockwise  
        // for all points 'x'. The idea is to keep  
        // track of last visited most counterclock- 
        // wise point in q. If any point 'i' is more  
        // counterclock-wise than q, then update q. 
        q = (p + 1) % n; 
          
        for (int i = 0; i < n; i++) 
        { 
           // If i is more counterclockwise than  
           // current q, then update q 
           if (orientation(points[p], points[i], points[q]) 
                                               == 2) 
               q = i; 
        } 
   
        // Now q is the most counterclockwise with 
        // respect to p. Set p as q for next iteration,  
        // so that q is added to result 'hull' 
        p = q; 
   
    } while (p != l);  // While we don't come to first  
                       // point 
   
    // Print Result 
   for (TestClient temp : hull) 
       System.out.println("(" + temp.x + ", " + 
                           temp.y + ")"); 
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
        sendMissionCommand = false;
        Location3D detectedLocation = hazardDetected.getDetectedLocation();
        //Get entity that detected the zone
       
        int detectingEntity = (int) hazardDetected.getDetectingEnitiyID();
      
        estimatedHazardZone.getBoundaryPoints().add(detectedLocation);
        //check if this is a first detetcion
       // System.out.println(estimatedHazardZone);
        /*
        int n=100;
        for(int i=1; i<=n;i++) {
        TestClient points[] = new TestClient[n];
        double x= detectedLocation.getLatitude();
        double y=detectedLocation.getLongitude();
        points[i] = new TestClient(x, y);
        int arlen = points.length; 
        convexHull(points, arlen); 
        }
        */
        
       if(detection ==0) {
        //call the rest of UAVs
        for(int i=0; i<=numberOfUAVs; i++) {
        	if(i!=detectingEntity) {
        
        callUAV(out, detectedLocation, i);
        sendLoiterCommand(out, in, i, detectedLocation);
        //setting gimbal effect to the camera
        GimbalStareAction gimbalStareAction = new GimbalStareAction();
		 gimbalStareAction.setPayloadID(1);
		 gimbalStareAction.setDuration(1000);
		 gimbalStareAction.setStarepoint(detectedLocation);
		 sendLoiterCommand(out, in, detectingEntity, detectedLocation);
        	}else {
        		System.out.println("I am the caller UAV" + detectingEntity);
        		 sendLoiterCommand(out, in, detectingEntity, detectedLocation);
        	}
        	
        }
       
        
       }//endof if
       else {
    	   
    	   //if detection is not zero, means don't call others
    	   sendLoiterCommand(out, in, detectingEntity, detectedLocation); 
       	
       }
       detection++;
       //Check if the UAV has already been sent the loiter command and proceed if it hasn't
       if (uavsLoiter[detectingEntity - 1] == false) {
           //Send the loiter command
          // sendLoiterCommand(out, in, detectingEntity, detectedLocation);

           //Note: Polygon points must be in clockwise or counter-clockwise order to get a shape without intersections
        //   estimatedHazardZone.getBoundaryPoints().add(detectedLocation);

           //Send out the estimation report to draw the polygon
           

           uavsLoiter[detectingEntity - 1] = true;
           System.out.println("UAV" + detectingEntity + " detected hazard at " + detectedLocation.getLatitude() +
                   "," + detectedLocation.getLongitude() + ". Sending loiter command.");
       }
       
       sendEstimateReport(out, estimatedHazardZone);
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
                Logger.getLogger(TestClient.class.getName()).log(Level.SEVERE, null, ex1);
            }
            return connect(host, port);
        }
        System.out.println("Sagir Server Connected to " + host + ":" + port);
        return socket;
    }

    public static void main(String[] args) {
        new TestClient(0,0).start();
    }
}
