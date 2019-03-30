import java.io.OutputStream;
import java.util.ArrayList;

import afrl.cmasi.AltitudeType;
import afrl.cmasi.CommandStatusType;
import afrl.cmasi.Location3D;
import afrl.cmasi.MissionCommand;
import afrl.cmasi.SpeedType;
import afrl.cmasi.TurnType;
import afrl.cmasi.Waypoint;

public class UnknownSearchClass {
	public boolean sendMissionCommand = true; 
	long[] fasterUAVs;
	long[] slowerUAVs;
	
	public UnknownSearchClass() {

		fasterUAVs = new long[2];
		slowerUAVs = new long[2];
		fasterUAVs[0] = 1;
		fasterUAVs[1] = 3;
		slowerUAVs[0] = 2;
		slowerUAVs[1] = 4;
//		sendMissionCommand = true;

	}
	public void doUnknownSearch(OutputStream out) throws Exception
	{
		if (sendMissionCommand == true) {

			searchMissionParallel(out, fasterUAVs);

			UAV4(out,slowerUAVs[0]);
			UAV3(out,slowerUAVs[1]);

		}

	}
	public void sendKnownMission(OutputStream out, Location3D loc) throws Exception {

		// replicate these for the numer of UAVs
		MissionCommand o = new MissionCommand();
		o.setFirstWaypoint(1);
		// Setting the UAV to recieve the mission
		o.setVehicleID(1);
		o.setStatus(CommandStatusType.Pending);
		// Setting a unique mission command ID
		o.setCommandID(1);
		// Creating the list of waypoints to be sent with the mission command
		ArrayList<Waypoint> waypoints = new ArrayList<Waypoint>();
		// Creating the first waypoint
		// Note: all the following attributes must be set to avoid issues
		Waypoint waypoint1 = new Waypoint();
		// Setting 3D coordinates

		// example values
		waypoint1.setLatitude(loc.getLatitude());
		waypoint1.setLongitude(loc.getLongitude());
		waypoint1.setAltitude(100);

		waypoint1.setAltitudeType(AltitudeType.MSL);
		// Setting unique ID for the waypoint
		waypoint1.setNumber(1);
		waypoint1.setNextWaypoint(2);
		// Setting speed to reach the waypoint
		waypoint1.setSpeed(30);
		waypoint1.setSpeedType(SpeedType.Airspeed);
		// Setting the climb rate to reach new altitude (if applicable)
		waypoint1.setClimbRate(0);
		waypoint1.setTurnType(TurnType.TurnShort);
		// Setting backup waypoints if new waypoint can't be reached
		waypoint1.setContingencyWaypointA(0);
		waypoint1.setContingencyWaypointB(0);

		// Adding the waypoints to the waypoint list
		waypoints.add(waypoint1);

		// Setting the waypoint list in the mission command
		o.getWaypointList().addAll(waypoints);

		// Sending the Mission Command message to AMASE to be interpreted
		out.write(avtas.lmcp.LMCPFactory.packMessage(o, true));

	}

	
	//searching command parallel search
		public void searchMissionParallel(OutputStream out, long[] fasterUAVS) throws Exception {

			double circleadd = 0.0;
			// int

			for (int n = 0; n < fasterUAVs.length; n++) {

				long id = fasterUAVs[n];
				int no = fasterUAVs.length;

				double lat1 = 53.3547;
				double lat2 = 53.5341;
				double longt2 = -1.618;
				double startlon = -1.9193;
				double lon = -1.9193 + circleadd;
				double longt1 = lon;
				double diflat = lat2 - lat1;
				double diflongt = longt2 - startlon;
				double longtshare = diflongt / no;
				double latshare = diflat / no;
				double latincrement = 0.0157;

				int ct = 0;
				int rd = 1;
				// Setting up the mission to send to the UAV

				MissionCommand o = new MissionCommand();
				o.setFirstWaypoint(1);
				// Setting the UAV to recieve the mission
				o.setVehicleID(id);
				o.setStatus(CommandStatusType.Pending);
				// Setting a unique mission command ID
				o.setCommandID(1);

				// Creating the list of waypoints to be sent with the mission command
				ArrayList<Waypoint> waypoints = new ArrayList<Waypoint>();
				// Creating the first waypoint
				// Note: all the following attributes must be set to avoid issues

				while (lat1 <= lat2) {

					Waypoint waypoint = new Waypoint();
					// Setting 3D coordinates

					if (ct == 0) {
						waypoint.setLatitude(lat1);
						waypoint.setLongitude(longt1);
					} else if (ct == 1) {

						waypoint.setLatitude(lat1);
						longt1 = longt1 + longtshare;
						waypoint.setLongitude(longt1);
					} else if (ct % 2 == 0) {
						lat1 = lat1 + latincrement;
						waypoint.setLatitude(lat1);
						waypoint.setLongitude(longt1);

					}

					else {
						rd++;

						if (rd % 2 == 0) {

							longt1 = longt1 - longtshare;
							waypoint.setLatitude(lat1);
							waypoint.setLongitude(longt1);
						} else {
							longt1 = longt1 + longtshare;
							waypoint.setLatitude(lat1);
							waypoint.setLongitude(longt1);
						}

					}

					waypoint.setAltitude(150);
					waypoint.setAltitudeType(AltitudeType.MSL);
					// Setting unique ID for the waypoint
					waypoint.setNumber(ct);
					waypoint.setNextWaypoint(ct + 1);
					// Setting speed to reach the waypoint
					waypoint.setSpeed(30);
					waypoint.setSpeedType(SpeedType.Airspeed);
					// Setting the climb rate to reach new altitude (if applicable)
					waypoint.setClimbRate(0);
					waypoint.setTurnType(TurnType.TurnShort);
					// Setting backup waypoints if new waypoint can't be reached
					waypoint.setContingencyWaypointA(ct - 1);
					waypoint.setContingencyWaypointB(ct - 2);

					waypoints.add(waypoint);

					ct++;

					// rd++;
				}

				// Mission fuel analysis will goes here.

				// Setting the waypoint list in the mission command
				o.getWaypointList().addAll(waypoints);

				// Sending the Mission Command message to AMASE to be interpreted
				out.write(avtas.lmcp.LMCPFactory.packMessage(o, true));
				System.out.println("Mission sent to UAV " + n + "Diagonal istance is "
						+ computeDistance(lat1, lat2, longt1, longt2) + " meters");
				circleadd = circleadd + longtshare;

			}
		}

		// calculate distance i n meters
		public double computeDistance(double lat1, double lat2, double longt1, double longt2) {
			// I want use haversine formula a =
			// sinsqr(dtheta)+costheta1+costheta2.sinsqr(dtheta)
			// c = 2.atan2(sqrta, sqrt1-a)
			// d= R.c
			/*
			 * double theta1 = Math.toRadians(lat1); double theta2 = Math.toRadians(lat2);
			 * double dtheta = Math.toRadians(lat2-lat1); double dlambda =
			 * Math.toRadians(longt2-longt1);
			 */

			double R = 6371e3; // radius of earth in meters
			double theta1 = lat1;
			double theta2 = lat2;
			double dtheta = lat2 - lat1;
			double dlambda = longt2 - longt1;
			double a = Math.sin(dtheta / 2) * Math.sin(dtheta / 2) + Math.cos(theta1) * Math.cos(theta2)
					+ Math.sin(dlambda / 2) * Math.sin(dlambda / 2);
			double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
			// double c = 2* Math.asin(Math.sqrt(a));
			double d = R * c;
			return d;

		}
		//UAV3
		public void UAV3(OutputStream out,long id) throws Exception {

			double circleadd = 0.0;
			// int

			int no = slowerUAVs.length;

			double lat1 = 53.3547 + 0.022;
			double lat2 = 53.5341;
			double longt2 = -1.618;
			double startlon = -1.7691;

			double diflat = lat2 - lat1;
			double diflongt = longt2 - startlon;
			double longtshare = diflongt;
			double latshare = diflat / no;
			double latincrement = 0.0157;

			double lon = startlon;
			double longt1 = lon;

			int ct = 0;
			int rd = 1;
			// Setting up the mission to send to the UAV

			MissionCommand o = new MissionCommand();
			o.setFirstWaypoint(1);
			// Setting the UAV to recieve the mission
			o.setVehicleID(id);
			o.setStatus(CommandStatusType.Pending);
			// Setting a unique mission command ID
			o.setCommandID(1);

			// Creating the list of waypoints to be sent with the mission command
			ArrayList<Waypoint> waypoints = new ArrayList<Waypoint>();
			// Creating the first waypoint
			// Note: all the following attributes must be set to avoid issues

			while (lat1 <= lat2) {

				Waypoint waypoint = new Waypoint();
				// Setting 3D coordinates

				if (ct == 0) {
					waypoint.setLatitude(lat1);
					waypoint.setLongitude(longt1);
				} else if (ct == 1) {

					waypoint.setLatitude(lat1);
					longt1 = longt1 + longtshare;
					waypoint.setLongitude(longt1);
				} else if (ct % 2 == 0) {
					lat1 = lat1 + latincrement;
					waypoint.setLatitude(lat1);
					waypoint.setLongitude(longt1);

				}

				else {
					rd++;

					if (rd % 2 == 0) {

						longt1 = longt1 - longtshare;
						waypoint.setLatitude(lat1);
						waypoint.setLongitude(longt1);
					} else {
						longt1 = longt1 + longtshare;
						waypoint.setLatitude(lat1);
						waypoint.setLongitude(longt1);
					}

				}

				waypoint.setAltitude(150);
				waypoint.setAltitudeType(AltitudeType.MSL);
				// Setting unique ID for the waypoint
				waypoint.setNumber(ct);
				waypoint.setNextWaypoint(ct + 1);
				// Setting speed to reach the waypoint
				waypoint.setSpeed(30);
				waypoint.setSpeedType(SpeedType.Airspeed);
				// Setting the climb rate to reach new altitude (if applicable)
				waypoint.setClimbRate(0);
				waypoint.setTurnType(TurnType.TurnShort);
				// Setting backup waypoints if new waypoint can't be reached
				waypoint.setContingencyWaypointA(ct - 1);
				waypoint.setContingencyWaypointB(ct - 2);

				waypoints.add(waypoint);

				ct++;

				// rd++;
			}

			// Mission fuel analysis will goes here.

			// Setting the waypoint list in the mission command
			o.getWaypointList().addAll(waypoints);

			// Sending the Mission Command message to AMASE to be interpreted
			out.write(avtas.lmcp.LMCPFactory.packMessage(o, true));
			System.out.println("Mission sent to UAV3 " + "Diagonal istance is "
					+ computeDistance(lat1, lat2, longt1, longt2) + " meters");
			circleadd = circleadd + longtshare;

		}

		public void UAV4(OutputStream out,long id) throws Exception {

			double circleadd = 0.0;

			int no = fasterUAVs.length;

			double lat1 = 53.3547 + 0.022;
			double lat2 = 53.5341;
			double longt2 = -1.618;
			double startlon = -1.9193 + 0.002;
			double lon = -1.9193;
			double longt1 = lon;
			double diflat = lat2 - lat1;
			double diflongt = longt2 - startlon;
			double longtshare = diflongt / no;
			double latshare = diflat / no;
			double latincrement = 0.0157;
			circleadd = circleadd + longtshare;

			int ct = 0;
			int rd = 1;
			// Setting up the mission to send to the UAV

			MissionCommand o = new MissionCommand();
			o.setFirstWaypoint(1);
			// Setting the UAV to recieve the mission
			o.setVehicleID(id);
			o.setStatus(CommandStatusType.Pending);
			// Setting a unique mission command ID
			o.setCommandID(1);

			// Creating the list of waypoints to be sent with the mission command
			ArrayList<Waypoint> waypoints = new ArrayList<Waypoint>();
			// Creating the first waypoint
			// Note: all the following attributes must be set to avoid issues

			while (lat1 <= lat2) {

				Waypoint waypoint = new Waypoint();
				// Setting 3D coordinates

				if (ct == 0) {
					waypoint.setLatitude(lat1);
					waypoint.setLongitude(longt1);
				} else if (ct == 1) {

					waypoint.setLatitude(lat1);
					longt1 = longt1 + longtshare;
					waypoint.setLongitude(longt1);
				} else if (ct % 2 == 0) {
					lat1 = lat1 + latincrement;
					waypoint.setLatitude(lat1);
					waypoint.setLongitude(longt1);

				}

				else {
					rd++;

					if (rd % 2 == 0) {

						longt1 = longt1 - longtshare;
						waypoint.setLatitude(lat1);
						waypoint.setLongitude(longt1);
					} else {
						longt1 = longt1 + longtshare;
						waypoint.setLatitude(lat1);
						waypoint.setLongitude(longt1);
					}

				}

				waypoint.setAltitude(150);
				waypoint.setAltitudeType(AltitudeType.MSL);
				// Setting unique ID for the waypoint
				waypoint.setNumber(ct);
				waypoint.setNextWaypoint(ct + 1);
				// Setting speed to reach the waypoint
				waypoint.setSpeed(30);
				waypoint.setSpeedType(SpeedType.Airspeed);
				// Setting the climb rate to reach new altitude (if applicable)
				waypoint.setClimbRate(0);
				waypoint.setTurnType(TurnType.TurnShort);
				// Setting backup waypoints if new waypoint can't be reached
				waypoint.setContingencyWaypointA(ct - 1);
				waypoint.setContingencyWaypointB(ct - 2);

				waypoints.add(waypoint);

				ct++;

				// rd++;
			}

			// Mission fuel analysis will goes here.

			// Setting the waypoint list in the mission command
			o.getWaypointList().addAll(waypoints);

			// Sending the Mission Command message to AMASE to be interpreted
			out.write(avtas.lmcp.LMCPFactory.packMessage(o, true));
			System.out.println("Mission sent to UAV4 " + "Diagonal istance is "
					+ computeDistance(lat1, lat2, longt1, longt2) + " meters");

		}

}
