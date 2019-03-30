import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import afrl.cmasi.AirVehicleState;
import afrl.cmasi.CommandStatusType;
import afrl.cmasi.FlightDirectorAction;
import afrl.cmasi.Location3D;
import afrl.cmasi.Polygon;
import afrl.cmasi.VehicleActionCommand;
import afrl.cmasi.searchai.HazardZoneDetection;
import afrl.cmasi.searchai.HazardZoneEstimateReport;

public class FollowFireClass {
	   HashMap<Long, ArrayList<Boolean>> state = new HashMap<>();
	    HashMap<Long, Boolean> searching = new HashMap<>();
	    Polygon hazardPolygon = new Polygon();
	    HashMap<Long, Location3D> detectedLocs = new HashMap<>();
	    
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
	    public void processAirVehicleState(AirVehicleState avs, OutputStream out) throws Exception
	    {
	       
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
	    public void handleHazard(HazardZoneDetection hzd) {
	    	long id = hzd.getDetectingEnitiyID();
	    	state.get(id).set(0, true);
	    	detectedLocs.put(id, hzd.getDetectedLocation());
	    	
	    	if(searching.containsKey(id)) {
	    		searching.put(id, false);
	    	}
	    }


}
