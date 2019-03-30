import afrl.cmasi.AltitudeType;
import afrl.cmasi.CommandStatusType;
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
import afrl.cmasi.searchai.HazardZoneDetection;
import afrl.cmasi.searchai.HazardZoneEstimateReport;
import afrl.cmasi.AirVehicleState;
import afrl.cmasi.Polygon;
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

public class FollowFire extends Thread {

    /** simulation TCP port to connect to */
    private static int port = 5555;
    /** address of the server */
    private static String host = "localhost";
    /**Array of booleans indicating if loiter command has been sent to each UAV */
    HashMap<Long, ArrayList<Boolean>> state = new HashMap<>();
    HashMap<Long, Boolean> searching = new HashMap<>();
    Polygon hazardPolygon = new Polygon();
    HashMap<Long, Location3D> detectedLocs = new HashMap<>();

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
            Logger.getLogger(FollowFire.class.getName()).log(Level.SEVERE, null, ex);
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
        		initialise_uav(id, avs);
        	}

        	if(!searching.get(id)) {
        		if(state.get(id).get(0) != state.get(id).get(1)) {
        			hazardPolygon.getBoundaryPoints().add(detectedLocs.get(id));
        			sendEstimateReport(out);
        		}
        		changeHeading(avs, id, out);
        	}
        }

        if (o instanceof afrl.cmasi.searchai.HazardZoneDetection) {
        	System.out.println("handled");
        	HazardZoneDetection hzd = ((HazardZoneDetection)o);
        	if(hzd.getDetectedHazardZoneType().getValue() != 0) {
        		handleHazard(hzd);
        	}
        }
    }

    public void initialise_uav(long id, AirVehicleState avs) {
    	ArrayList<Boolean> l = new ArrayList<>();
    	l.add(false);
    	l.add(false);
    	state.put(id, l);
    	searching.put(id, true);
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
                Logger.getLogger(FollowFire.class.getName()).log(Level.SEVERE, null, ex1);
            }
            return connect(host, port);
        }
        System.out.println("Connected to " + host + ":" + port);
        return socket;
    }

    public static void main(String[] args) {
        new FollowFire().start();
    }
}
