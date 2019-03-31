import afrl.cmasi.AltitudeType;
import afrl.cmasi.CommandStatusType;
import afrl.cmasi.FlightDirectorAction;
import afrl.cmasi.GimbalStareAction;
import afrl.cmasi.KeepInZone;
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
import afrl.cmasi.searchai.HazardZoneDetection;
import afrl.cmasi.searchai.HazardZoneEstimateReport;
import afrl.cmasi.AbstractGeometry;
import afrl.cmasi.AirVehicleState;
import afrl.cmasi.Polygon;
import afrl.cmasi.Rectangle;
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
import java.util.HashMap;
import java.util.Random;

public class Challenge3 extends Thread {

    enum State {
    	SEARCHING,
    	MAPPING
    }
    /** simulation TCP port to connect to */
    private static int port = 5555;
    /** address of the server */
    private static String host = "localhost";
    /**Array of booleans indicating if loiter command has been sent to each UAV */
    HashMap<Long, ArrayList<Boolean>> flags = new HashMap<>();
    HashMap<Long, State> state = new HashMap<>();
    HashMap<Long, Boolean> searching = new HashMap<>();
    Polygon hazardPolygon = new Polygon();
    HashMap<Long, Location3D> detectedLocs = new HashMap<>();
    double R_EARTHKM = 6372.8;
    KeepInZone keepinzone;
    HashMap<Long, Boolean> hasTurned = new HashMap<>();
    Location3D low_loc;
    Location3D high_loc;
    ArrayList<UAVInfo> uavs = new ArrayList<>();
    
    QueueManager qm = new QueueManager();
    PrioritiseCells prioritiser = new PrioritiseCells();
    

    
    @Override
    public void run() {
        try {
            // connect to the server
            Socket socket = connect(host, port);
            
            qm.setupWithCells(priotiser.getInitialGridPoints());
            

            while(true) {
                //Continually read the LMCP messages that AMASE is sending out
                readMessages(socket.getInputStream(), socket.getOutputStream());
            }

        } catch (Exception ex) {
            Logger.getLogger(challenge2.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
    * Reads in messages being sent out by the AMASE Server
    */
    public void readMessages(InputStream in, OutputStream out) throws Exception {
        //Use each of the if statements to use the incoming message
        LMCPObject o = LMCPFactory.getObject(in);
        if (o instanceof afrl.cmasi.AirVehicleState) {
        	AirVehicleState avs = ((AirVehicleState)o);
        	long id = avs.getID();
        	UAVInfo currentUAV = getUAVInfo(id);
        	if(!flags.containsKey(id)) {
        		initialise_uav(id, avs, out);
        	}
        	
    		if(flags.get(id).get(0) != flags.get(id).get(1)) { // Crossed boundary
    			if(currentUAV.currentTask.getTaskType() == Task.TaskType.MAP) {
    				changeHeading(avs, id, out);
    			} else if(currentUAV.currentTask.getTaskType() == Task.TaskType.SEARCH) {
    				qm.notifyOfFire(currentUAV.currentTask, detectedLocs.get(id));
    				
    				qm.updatePriorities(prioritise.getGridPrioritise())
    				
    				currentUAV.setCurrentTask(qm.requestNewTask(currentUAV));
    			} else {
    				// Refuel
    			}
        		
        	} else {
        		if(currentUAV.currentTask.getTaskType() == Task.TaskType.MAP) {
    				changeHeading(avs, id, out);
    			} else if(currentUAV.currentTask.getTaskType() == Task.TaskType.SEARCH) {
    				// leave in jsut in case
    			} else {
    				// Refuel
    			}
        	}
    		if(currentUAV.currentTask.isFinished()) { // Needs to be better, currently just count
    			if(currentUAV.currentTask == Task.TaskType.SEARCH) {
    				currentUAV.currentTask.priority = 0;
    				qm.addNewSearchTask(currentUAV.currentTask);
    			} else if(currentUAV.currentTask == Task.TaskType.SEARCH) {
    				currentUAV.currentTask.priority = 0;
    				qm.addNewFireTask(currentUAV.currentTask);
    			}
				currentUAV.setCurrentTask(qm.requestNewTask(currentUAV));
			}
        }

        if (o instanceof afrl.cmasi.searchai.HazardZoneDetection) {
        	HazardZoneDetection hzd = ((HazardZoneDetection)o);
        	if(hzd.getDetectedHazardZoneType().getValue() != 0) {
        		handleHazard(hzd);
        	}
        }
       if (o instanceof afrl.cmasi.KeepInZone) {
            KeepInZone boundary = ((KeepInZone) o);
            setLimitsUsingKeepInZone(boundary);
        }
    }
    
    public void startCurrentTask(OutputStream out, UAVInfo uav) throws Exception {
    	if(uav.currentTask.getTaskType() == Task.TaskType.SEARCH) {
    		Task currentTask = uav.getCurrentTask();
    		askUAVToSweep(out, uav.id, currentTask.startSearchLocation.getLatitude(),currentTask.startSearchLocation.getLongitude(),
    				currentTask.endSearchLocation.getLongitude(),currentTask.endSearchLocation.getLatitude(), 0.015);
    	} else if(uav.currentTask.getTaskType() == Task.TaskType.MAP) {
    		// If fire is found en route, will start mapping that fire instead of target.
    		sendToWayPoint(out, uav.id, uav.currentTask.targetLocation, 30);
    	} else {
    		//Refuel
    	}
    }
    

    public Boolean isTaskFinished(AirVehicleState avs, State state) {
    	return false;
    }
    
    public UAVInfo getUAVInfo(long id) {
    	for(UAVInfo uav : uavs) {
    		if(uav.id == id) {
    			return uav; // COULD CAUSE AN ERROR
    		}
    	}
    	return null;
    }

    public void initialise_uav(long id, AirVehicleState avs, OutputStream out) throws IOException, Exception {
    	ArrayList<Boolean> l = new ArrayList<>();
    	l.add(false);
    	l.add(false);
    	flags.put(id, l);
    	//state.put(id, State.SEARCHING);
    	uavs.add(new UAVInfo(id));
    	setInitialHeading(id, out, false);
    }
    
    public Boolean checkValidPos(AirVehicleState avs, long id) throws Exception {
    	
      double currLat = avs.getLocation().getLatitude();
      double currLong = avs.getLocation().getLongitude();
      
      if(currLat < low_loc.getLatitude() || high_loc.getLatitude() < currLat) {
    	  return false;
      }
      if(currLong < low_loc.getLongitude() || high_loc.getLongitude() < currLong) {
    	  return false;
      }
      return true;
    }
    
    public void setInitialHeading(long id, OutputStream out, Boolean keepingIn) throws IOException, Exception {
    	int h = (int) (((id-1) * 90)+45);    	
    	
    	VehicleActionCommand vac = new VehicleActionCommand();
    	vac.setVehicleID(id);
    	vac.setStatus(CommandStatusType.Pending);
    	vac.setCommandID(1);

    	FlightDirectorAction fda = new FlightDirectorAction();
    	fda.setHeading(h);
    	vac.getVehicleActionList().add(fda);

    	out.write(avtas.lmcp.LMCPFactory.packMessage(vac, true));
    	
    	flags.get(id).set(1, flags.get(id).get(0));
    	flags.get(id).set(0, false);
    	
    }

    public void changeHeading(AirVehicleState avs, long id, OutputStream out) throws IOException, Exception {
    	int heading;
    	if(flags.get(id).get(0)) {
    		heading = 100;
    	} else {
    		heading = -100;
    	}

    	VehicleActionCommand vac = new VehicleActionCommand();
    	vac.setVehicleID(id);
    	vac.setStatus(CommandStatusType.Pending);
    	vac.setCommandID(1);

    	FlightDirectorAction fda = new FlightDirectorAction();
    	fda.setHeading(avs.getHeading() + heading);
    	vac.getVehicleActionList().add(fda);

    	out.write(avtas.lmcp.LMCPFactory.packMessage(vac, true));
    	
    	flags.get(id).set(1, flags.get(id).get(0));
    	flags.get(id).set(0, false);

    }

    public void handleHazard(HazardZoneDetection hzd) {
    	long id = hzd.getDetectingEnitiyID();
    	if(id == 2 || id == 3) {
    		System.out.println("here");
    	}
    	flags.get(id).set(0, true);
    	detectedLocs.put(id, hzd.getDetectedLocation());
    	
    	if(searching.containsKey(id)) {
    		searching.put(id, false);
    	}
    }

    public void sendEstimateReport(OutputStream out) throws Exception {
        //Setting up the mission to send to the UAV
        HazardZoneEstimateReport o = new HazardZoneEstimateReport();
       
        o.setEstimatedZoneShape(hazardPolygon);
        o.setUniqueTrackingID(1);
        o.setEstimatedGrowthRate(0);
        o.setPerceivedZoneType(afrl.cmasi.searchai.HazardType.Fire);
        o.setEstimatedZoneDirection(0);
        o.setEstimatedZoneSpeed(0);


        //Sending the Vehicle Action Command message to AMASE to be interpreted
        out.write(avtas.lmcp.LMCPFactory.packMessage(o, true));
    }
    

    public void setLimitsUsingKeepInZone(KeepInZone keepinzone) throws Exception {
        Rectangle bounds = (Rectangle) keepinzone.getBoundary();
        setLimitsUsingRect(bounds);
        System.out.println("Processed KeepInZone");
    }
    
    public void setLimitsUsingRect(Rectangle bounds) throws Exception {
        Location3D centerPoint = bounds.getCenterPoint();
        float w = bounds.getWidth();
        float h = bounds.getHeight();
        float r = bounds.getRotation();
        if (r != 0) {
            throw new Exception("Need to rotate to get zone");
        }
        low_loc = newLocation(centerPoint, w / -2f, h / -2f);
        high_loc = newLocation(centerPoint, w / 2f, h / 2f);
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
    
    private static int getRandomNumberInRange(int min, int max) {

		if (min >= max) {
			throw new IllegalArgumentException("max must be greater than min");
		}

		Random r = new Random();
		return r.nextInt((max - min) + 1) + min;
	}
    
    public void askUAVToSweep(OutputStream out, long id, double lat1, double lon1, double lat2, double lon2, double latinc) throws Exception {
    	
    	double circleadd = 0.0;
    	//int
    		
    		//int no =numberOfUAVsSearch;
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
    		  //double latshare = diflat/no;
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
         circleadd = circleadd + longtshare;
    	 
    		
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
		getUAVInfo(vehicleId).currentCommand = mc;
		out.write(avtas.lmcp.LMCPFactory.packMessage(mc, true));
	}
    
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
                Logger.getLogger(Challenge3.class.getName()).log(Level.SEVERE, null, ex1);
            }
            return connect(host, port);
        }
        System.out.println("Connected to " + host + ":" + port);
        return socket;
    }

    public static void main(String[] args) {
        new Challenge3().start();
    }
}



/*if(!checkValidPos(avs, id)) {
if(!hasTurned.containsKey(id) || !hasTurned.get(id)) {
	reverseHeading(avs, id, out);
	hasTurned.put(id, true);
	System.out.println("reversing");
}
} else {
if(hasTurned.containsKey(id)) {
	hasTurned.remove(id);
}
}*/


/*public void reverseHeading(AirVehicleState avs, long id, OutputStream out) throws IOException, Exception {

VehicleActionCommand vac = new VehicleActionCommand();
vac.setVehicleID(id);
vac.setStatus(CommandStatusType.Pending);
vac.setCommandID(1);

int h;
if(id == 1 || id == 2){
h = 90;
} else {
h = -90;
}
FlightDirectorAction fda = new FlightDirectorAction();
fda.setHeading(avs.getHeading()+h);
vac.getVehicleActionList().add(fda);

out.write(avtas.lmcp.LMCPFactory.packMessage(vac, true));

flags.get(id).set(1, flags.get(id).get(0));
flags.get(id).set(0, false);
}*/