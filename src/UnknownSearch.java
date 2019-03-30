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
public class UnknownSearch extends Thread {

    /** simulation TCP port to connect to */
    private static int port = 5555;
    /** address of the server */
    private static String host = "localhost";
    private static long scenarioTime = 0;
    /**Array of booleans indicating if loiter command has been sent to each UAV */
    boolean[] uavsLoiter = new boolean[4];
    Polygon estimatedHazardZone = new Polygon();
    // that is the number of faster drones to conduct the fast search
    int numberOfUAVsSearch = 2;
    Boolean sendMissionCommand = true;
    
    public UnknownSearch() {
    	
    	
    }
    


    @Override
    public void run() {
    	
    
        try {
            // connect to the server
        	
            Boolean sensorCommandSent = false;
            Boolean loiterCommandSent = false;
            
            Socket socket = connect(host, port);
            if(sendMissionCommand==true) {}
            UAV4(socket.getOutputStream());
            searchMissionParallel(socket.getOutputStream(),numberOfUAVsSearch);
            UAV3(socket.getOutputStream());
            
            
         //  makingCross(socket.getOutputStream(),numberOfUAVsSearch,0.02, 0.02);
           
           //sending UAV to a point
           //sendKnownMission(socket.getOutputStream());
            
            while(true) {
            	//start_search(out);
            	
            	 
                //Continually read the LMCP messages that AMASE is sending out
                readMessages(socket.getInputStream(), socket.getOutputStream());
               
                
                
            }

        } catch (Exception ex) {
            Logger.getLogger(UnknownSearch.class.getName()).log(Level.SEVERE, null, ex);
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
         loiterAction.setDirection(LoiterDirection.CounterClockwise);
         loiterAction.setDuration(10000);
         loiterAction.setAirspeed(35);
         
         //Creating a 3D location object for the stare point
         loiterAction.setLocation(location);
         
         //Adding the loiter action to the vehicle action list
         o.getVehicleActionList().add(loiterAction);
         
         //Sending the Vehicle Action Command message to AMASE to be interpreted
         out.write(avtas.lmcp.LMCPFactory.packMessage(o, true));
         
         
       
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
     waypoint1.setLatitude(location.getLatitude());
     waypoint1.setLongitude(location.getLongitude());
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

// method for making 4 UAVs to make a cross formation  

public void makingCross(OutputStream out,int numberOfUAVsSearch, double latincrement, double longincrement) throws Exception {
	
	//Location3D location = new Location3D(53.4811,-1.8119, 0, afrl.cmasi.AltitudeType.MSL);
	/*
	double lat = loc.getLatitude();
	double lon = loc.getLongitude();
	*/
	
	
	
	double lat = 53.4783;
	double lon = -1.808;
	
	int rd =1;
	
	for(int i =1; i<= numberOfUAVsSearch; i++) {
		
	MissionCommand o = new MissionCommand();
    o.setFirstWaypoint(1);
    //Setting the UAV to recieve the mission
    o.setVehicleID(i);
    o.setStatus(CommandStatusType.Pending);
    //Setting a unique mission command ID
    o.setCommandID(1);
    //Creating the list of  waypoints to be sent with the mission command
    ArrayList<Waypoint> waypoints = new ArrayList<Waypoint>();
    //Creating the first waypoint
    //Note: all the following attributes must be set to avoid issues
    Waypoint waypoint1 = new Waypoint();
    //Setting 3D coordinates
    
   
    
    if(i ==1 ) {
    	
	 	waypoint1.setLatitude(lat);
	    waypoint1.setLongitude(lon);
	    waypoint1.setAltitude(100);
	    System.out.println("way point 1");
	    
    
    	}
    else if(i ==2 ) {
		
		waypoint1.setLatitude(lat+(latincrement/2));
	    waypoint1.setLongitude(lon-(longincrement/2));
	    waypoint1.setAltitude(100);
	    System.out.println("way point 2");
		
	}
    else if(i==3) {
		
		waypoint1.setLatitude(lat+latincrement);
	    waypoint1.setLongitude(lon);
	    waypoint1.setAltitude(100);
	    System.out.println("way point 3");
		
	}
    else {
    	
    	   	
		    waypoint1.setLatitude(lat+(latincrement/2));
		    waypoint1.setLongitude(lon+(longincrement/2));
		    waypoint1.setAltitude(100);
		    System.out.println("way point 4");
		    
    	}
    
    
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
    
    rd++;
    
	}
	
}


public void sendKnownMission(OutputStream out) throws Exception {
	
	
	//replicate these for the numer of UAVs
	  MissionCommand o = new MissionCommand();
      o.setFirstWaypoint(1);
      //Setting the UAV to recieve the mission
      o.setVehicleID(1);
      o.setStatus(CommandStatusType.Pending);
      //Setting a unique mission command ID
      o.setCommandID(1);
      //Creating the list of  waypoints to be sent with the mission command
      ArrayList<Waypoint> waypoints = new ArrayList<Waypoint>();
      //Creating the first waypoint
      //Note: all the following attributes must be set to avoid issues
      Waypoint waypoint1 = new Waypoint();
      //Setting 3D coordinates
      
      //example values 
      waypoint1.setLatitude(53.4783);
      waypoint1.setLongitude(-1.808);
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
      
	
 }

//UAV3
public void UAV3(OutputStream out) throws Exception {
	
	double circleadd = 0.0;
	//int
	
	 
	
		
		
		int no =numberOfUAVsSearch;
		
		 double lat1 =	53.4463+0.002;
		  double lat2 =	53.4914;
		  double longt2 = -1.8341;
		  double startlon = -1.7584;
		  double lon =-1.7584+circleadd;
		  double longt1 = lon;
		  double diflat = lat2 - lat1; 
		  double diflongt = longt2 - startlon;
		  double longtshare = diflongt/no;
		  double latshare = diflat/no;
		  double latincrement = 0.0057;
	
		  
		 
		  int ct = 0;
		  int rd = 1;
	    //Setting up the mission to send to the UAV
	  
     MissionCommand o = new MissionCommand();
     o.setFirstWaypoint(1);
     //Setting the UAV to recieve the mission
     o.setVehicleID(3);
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
     System.out.println("Mission sent to UAV3 "+ "Diagonal istance is "+computeDistance(lat1,lat2,longt1,longt2)+" meters");
     circleadd = circleadd + longtshare;
	 
		
	}

public void UAV4(OutputStream out) throws Exception {
	
	
	double circleadd =0.0;
	
	
		
		
		int no =numberOfUAVsSearch;
		
		 double lat1 =	53.4463+0.002;
		  double lat2 =	53.4914;
		  double longt2 = -1.7584;
		  double startlon = -1.8341+0.002;
		  double lon =-1.8341;
		  double longt1 = lon;
		  double diflat = lat2 - lat1; 
		  double diflongt = longt2 - startlon;
		  double longtshare = diflongt/no;
		  double latshare = diflat/no;
		  double latincrement = 0.0057;
		  circleadd = circleadd + longtshare;
		 
		  
		 
		  int ct = 0;
		  int rd = 1;
	    //Setting up the mission to send to the UAV
	  
     MissionCommand o = new MissionCommand();
     o.setFirstWaypoint(1);
     //Setting the UAV to recieve the mission
     o.setVehicleID(4);
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
     System.out.println("Mission sent to UAV3 "+ "Diagonal istance is "+computeDistance(lat1,lat2,longt1,longt2)+" meters");
    
	 
		
	}
	



//searching command parallel search
public void searchMissionParallel(OutputStream out, int numberOfUAVsSearch) throws Exception {
	
	double circleadd = 0.0;
	//int
	
	 
	
	for(int n=1; n<=numberOfUAVsSearch; n++) {		
		
		int no =numberOfUAVsSearch;
		
		 double lat1 =	53.4463;
		  double lat2 =	53.4914;
		  double longt2 = -1.8341;
		  double startlon = -1.7584;
		  double lon =-1.7584+circleadd;
		  double longt1 = lon;
		  double diflat = lat2 - lat1; 
		  double diflongt = longt2 - startlon;
		  double longtshare = diflongt/no;
		  double latshare = diflat/no;
		  double latincrement = 0.0057;
	
		  
		 
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
     System.out.println("Mission sent to UAV "+n + "Diagonal istance is "+computeDistance(lat1,lat2,longt1,longt2)+" meters");
     circleadd = circleadd + longtshare;
	 
		
	}
	}
		
	
	
	
	/*
	int id3 =3;
	int id4 =4;
	
	
	if(id3==3) {
		
		
		 double lat1 =	53.4463;
		  double lat2 =	53.4914;
		  double longt2 = -1.8341;
		  double startlon = -1.7584;
		  
		 
		 
		  double diflat = lat2 - lat1; 
		  double diflongt = longt2 - startlon;
		  double longtshare = diflongt/numberOfUAVsSearch;
		  double latshare = diflat/numberOfUAVsSearch;
		  
		  double lon =longtshare/2+circleadd;
		  double longt1 = longtshare/2;
		  lat1 = lat2/2;
		  longt1 = longtshare/2;
		  
		  double latincrement = 0.0057;
	
		  
		 
		  int ct = 0;
		  int rd = 1;
	    //Setting up the mission to send to the UAV
	  
    MissionCommand o = new MissionCommand();
    o.setFirstWaypoint(1);
    //Setting the UAV to recieve the mission
    o.setVehicleID(3);
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
    System.out.println("Mission sent to UAV "+id3 + "Diagonal istance is "+computeDistance(lat1,lat2,longt1,longt2)+" meters");
    circleadd = circleadd + longtshare;
	 }
	
	if(id4==4) {
		
		
		 double lat1 =	53.4463;
		  double lat2 =	53.4914;
		  double longt2 = -1.8341;
		  double startlon = -1.7584;
		  
		 
		 
		  double diflat = lat2 - lat1; 
		  double diflongt = longt2 - startlon;
		  double longtshare = diflongt/numberOfUAVsSearch;
		  double latshare = diflat/numberOfUAVsSearch;
		  
		  double lon =longtshare/2+circleadd;
		  double longt1 = longtshare/2;
		  lat1 = lat2/2;
		  longt1 = longtshare/2;
		  
		  double latincrement = 0.0057;
		  
		 
		  int ct = 0;
		  int rd = 1;
	    //Setting up the mission to send to the UAV
	  
   MissionCommand o = new MissionCommand();
   o.setFirstWaypoint(1);
   //Setting the UAV to recieve the mission
   o.setVehicleID(id4);
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
   System.out.println("Mission sent to UAV "+id3 + "Diagonal istance is "+computeDistance(lat1,lat2,longt1,longt2)+" meters");
   circleadd = circleadd + longtshare;
	 }
		
	
	
}


public void searchMissionExpand2(OutputStream out, int numberOfUAVsExpand) throws Exception {
	
	double circleadd = 0.0;
	//int
	
	 
	
	for(int n=1; n<=numberOfUAVsSearch; n++) {
		
		
		 double lat1 =	53.4463;
		  double lat2 =	53.4914;
		  double longt2 = -1.8341;
		  double startlon = -1.7584;
		  double lon =-1.7584+circleadd;
		  double longt1 = lon;
		  double diflat = lat2 - lat1; 
		  double diflongt = longt2 - startlon;
		  double longtshare = diflongt/numberOfUAVsSearch;
		  double latshare = diflat/numberOfUAVsSearch;
		  double latincrement = 0.0057;
	
		  
		 
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
     System.out.println("Mission sent to UAV "+n + "Diagonal istance is "+computeDistance(lat1,lat2,longt1,longt2)+" meters");
     circleadd = circleadd + longtshare;
	 }
		
		*/
	
	




public void searchMissionCreepline(OutputStream out, int numberOfUAVsSearch) throws Exception {
	
	double circleadd = 0.0;
	//int
	for(int n=1; n<=numberOfUAVsSearch; n++) {
		
		
		  double lat1 =	53.4463;
		  double lat2 =	53.4914;
		  double longt2 = -1.8341;
		  double startlon = -1.7584;
		  double lon =-1.7584+circleadd;
		  double longt1 = lon;
		  double diflat = lat2 - lat1; 
		  double diflongt = longt2 - startlon;
		  double longtshare = diflongt/numberOfUAVsSearch;
		  double latshare = diflat/numberOfUAVsSearch;
	
		  
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
     
         
     waypoint.setAltitude(700);
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
     System.out.println("Mission sent to UAV "+n + "Diagonal istance is "+computeDistance(lat1,lat2,longt1,longt2)+" meters");
     circleadd = circleadd + longtshare;
	 }//en of for statement
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
       
        
       if(detection ==0) {
        //call the rest of UAVs
        for(int i=0; i<=numberOfUAVsSearch; i++) {
        	
        	if(i!=detectingEntity && i !=detectingEntity) {
        
        		if(i==1) {
       callUAV(out, detectedLocation, 3);
       
        		}
        		else {
        			callUAV(out, detectedLocation, 4);	
        			
        		}
       
        		//makingCross(out,numberOfUAVsSearch,detectedLocation,0.002, 0.002);
        
		break;
        	}else {
        		
        		// sendLoiterCommand(out, in, i, detectedLocation);
        		System.out.println("I am the caller UAV" + detectingEntity);
        		 
        	}
        	
        }
       
        
       }//endof if
       else {
    	   
    	   //if detection is not zero, means don't call others
    	  // sendLoiterCommand(out, in, detectingEntity, detectedLocation); 
       	
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
                Logger.getLogger(UnknownSearch.class.getName()).log(Level.SEVERE, null, ex1);
            }
            return connect(host, port);
        }
        System.out.println("Sagir Server Connected to " + host + ":" + port);
        return socket;
    }

    public static void main(String[] args) {
        new UnknownSearch().start();
    }
}
