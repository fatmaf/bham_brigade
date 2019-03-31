import java.awt.Point;
import java.util.HashMap;

import afrl.cmasi.AltitudeType;
import afrl.cmasi.Location3D;

public class Grid {

	 double unknownPriority = 50; 

		
		boolean isProtoBufGrid = false;
		boolean gridInitialised = false;
		double lat_high;
		double lat_low;
		double lon_high;
		double lon_low;

		double cellIncLat;
		double cellIncLon;
		double numCellsLat;
		double numCellsLon;
		double[][] heatmap = null;

		public Grid(int numCellsLat, int numCellsLon, boolean isProtobuf) {
			this.numCellsLat = numCellsLat;
			this.numCellsLon = numCellsLon;
			this.isProtoBufGrid = isProtobuf;
			if (!this.isProtoBufGrid)
			{
				heatmap = new double[(int)this.numCellsLon][(int)this.numCellsLat]; 
				for(int i = 0; i<numCellsLon; i++)
				{
					for(int j = 0; j<numCellsLat; j++)
					{
						heatmap[i][j]= unknownPriority; 
					}
				}
			}

		}

		public boolean pointInGrid(Point p) {
			if (p.getX() < numCellsLon && p.getY() < numCellsLat)
				return true;
			return false;
		}

		public void initialiseGrid(double max_lat, double min_lat, double max_lon, double min_lon) {

			lat_high = max_lat;// high_loc.getLatitude();
			lat_low = min_lat;// low_loc.getLatitude();
			lon_high = max_lon;// high_loc.getLongitude();
			lon_low = min_lon;// low_loc.getLongitude();

			cellIncLat = (lat_high - lat_low) / (double) numCellsLat;
			cellIncLon = (lon_high - lon_low) / (double) numCellsLon;
			gridInitialised = true;
//			System.out.println("initGrid"+lat_high+":"+lon_high+":"+cellIncLat);
		}

		public Point locationToGrid(Location3D loc) {
			int xloc =(int)Math.floor(((loc.getLongitude() - lon_low) / cellIncLon));
			int yloc = (int)Math.floor( ((loc.getLatitude() - lat_low) / cellIncLat));

			// si*i + sj*j
			Point toret = new Point(xloc, yloc);
			return toret;
		}

		public Location3D pointToLocation(Point cell) {
			Location3D location = new Location3D();
			double lat = ((double) cell.y) * cellIncLat + lat_low;
			double lon = ((double) cell.x) * cellIncLon + lon_low;
			location.setLongitude(lon);
			location.setLatitude(lat);
			location.setAltitude(200);
			location.setAltitudeType(AltitudeType.AGL);
//			System.out.println("Cell " + cell.toString() + " = [lon:" + lon + " " + (lon + cellRadiusLon) + ",lat:" + lat
//					+ " " + (lat + cellRadiusLat) + "]");
			return location;
		}

		public Location3D createUpperBoundLocation(Location3D location) {
			Location3D loc = new Location3D(location.getLatitude() + cellIncLat,
					location.getLongitude() + cellIncLon, location.getAltitude(), location.getAltitudeType());

			return loc;
		}
		

		HashMap<String,Location3D> getLocTopBottom(Point cell) {
			Location3D loc1 = pointToLocation(cell);
			Location3D loc2 = createUpperBoundLocation(loc1);
			HashMap<String,Location3D> arr = new HashMap<String,Location3D>();
			arr.put("bottomLeft",loc1);
			arr.put("topRight",loc2);
			return arr;
		}

		HashMap<String,Location3D> getCornerLocations(Point cell) {
			Location3D loc1 = pointToLocation(cell);
			Location3D loc2 = createUpperBoundLocation(loc1);
//			Location3D loc3 = 
			HashMap<String,Location3D> arr = new HashMap<String,Location3D>();
			arr.put("bottomLeft",loc1);
			arr.put("topRight",loc2);
			
			return arr;
		}

				
		// so if we have a resolution
		// and its not a heatmap
		// then you do nothing
		// cell(0,0) = cells (0,0) through (res,res)
		// cell(1,0) = cells(res,0) through (res+res,res)
		// so basically cell(x,y) = cells(x*res,y*res) through (x*res+res,y*res+res)

		HashMap<String,Point> gridToheatMap(Point gridPoint, int res) {
			HashMap<String,Point> pts = new HashMap<String,Point>();

			int x = (int) (gridPoint.getX() * res);
			int y = (int) (gridPoint.getY() * res);
			Point p1 = new Point(x, y);
			x = x + res;
			y = y + res;
			Point p2 = new Point(x, y);
			pts.put("bottomLeft",p1);
			pts.put("topRight",p2);
			return pts;

		}

		// heatmappoint = (x,y)
		// grid point = floor(x/res, y/res)
		Point heatMapToGrid(Point heatMapPoint, int res) {
			int x = (int) heatMapPoint.getX() / res;
			int y = (int) heatMapPoint.getY() / res;
			Point gridPoint = new Point(x, y);
			return gridPoint;
		}


}
