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

public class FollowFire2 extends Thread {

    /** simulation TCP port to connect to */
    private static int port = 5555;
    /** address of the server */
    private static String host = "localhost";
    /**Array of booleans indicating if loiter command has been sent to each UAV */
    HashMap<Long, ArrayList<Boolean>> state = new HashMap<>();
    HashMap<Long, Boolean> searching = new HashMap<>();
    Polygon hazardPolygon = new Polygon();
    HashMap<Long, Location3D> detectedLocs = new HashMap<>();
    double R_EARTHKM = 6372.8;
    KeepInZone keepinzone;
    HashMap<Long, Boolean> hasTurned = new HashMap<>();
    Location3D low_loc;
    Location3D high_loc;
    
    @Override
    public void run() {
        try {
            // connect to the server
            Socket socket = connect(host, port);

            while(true) {
                //Continually read the LMCP messages that AMASE is sending out
                readMessages(socket.getInputStream(), socket.getOutputStream());
            }

        } catch (Exception ex) {
            Logger.getLogger(FollowFire2.class.getName()).log(Level.SEVERE, null, ex);
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
        	if(!state.containsKey(id)) {
        		initialise_uav(id, avs, out);
        	}

        	if(!searching.get(id)) {
        		if(state.get(id).get(0) != state.get(id).get(1)) {
        			hazardPolygon.getBoundaryPoints().add(detectedLocs.get(id));
        			//sendEstimateReport(out);
        		}
        		changeHeading(avs, id, out);
        	} else {
        		if(!checkValidPos(avs, id)) {
        			if(!hasTurned.containsKey(id) || !hasTurned.get(id)) {
        				reverseHeading(avs, id, out);
        				hasTurned.put(id, true);
        				System.out.println("reversing");
        			}
        		} else {
        			if(hasTurned.containsKey(id)) {
        				hasTurned.remove(id);
        			}
        		}
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
    
    public void reverseHeading(AirVehicleState avs, long id, OutputStream out) throws IOException, Exception {
 
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
    	
    	state.get(id).set(1, state.get(id).get(0));
    	state.get(id).set(0, false);
    }
    
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

    public void initialise_uav(long id, AirVehicleState avs, OutputStream out) throws IOException, Exception {
    	ArrayList<Boolean> l = new ArrayList<>();
    	l.add(false);
    	l.add(false);
    	state.put(id, l);
    	searching.put(id, true);
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
    	
    	state.get(id).set(1, state.get(id).get(0));
    	state.get(id).set(0, false);
    	
    }

    public void changeHeading(AirVehicleState avs, long id, OutputStream out) throws IOException, Exception {
    	int heading;
    	if(state.get(id).get(0)) {
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
    	
    	state.get(id).set(1, state.get(id).get(0));
    	state.get(id).set(0, false);

    }

    public void handleHazard(HazardZoneDetection hzd) {
    	long id = hzd.getDetectingEnitiyID();
    	if(id == 2 || id == 3) {
    		System.out.println("here");
    	}
    	state.get(id).set(0, true);
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
                Logger.getLogger(FollowFire2.class.getName()).log(Level.SEVERE, null, ex1);
            }
            return connect(host, port);
        }
        System.out.println("Connected to " + host + ":" + port);
        return socket;
    }

    public static void main(String[] args) {
        new FollowFire2().start();
    }
}
