import java.awt.Point;
import java.util.HashMap;

import afrl.cmasi.Location3D;

public class PrioritiseCells {
	
	Grid grid = null;
	Grid heatmapGrid = null;
	double resolution = 4; // heatmap cells per grid cell

	public PrioritiseCells(double res) {
//		heatmapGrid = null;
//		grid = new Grid(100,100,false);
		resolution = res; 
		ListenerChannel listenerChannel = new ListenerChannel();
		listenerChannel.run(x -> {
//			System.out.println("Received update at simulation time :" + x.getTime());
			if (heatmapGrid == null) {
				final int rows = x.getSize(0);
				final int columns = x.getSize(1);
				grid = new Grid((int) (columns / this.resolution), (int) (rows / this.resolution), false);
				heatmapGrid = new Grid(columns, rows, true);

				heatmapGrid.initialiseGrid(x.getMaxLat(), x.getMinLat(), x.getMaxLong(), x.getMinLong());
				grid.initialiseGrid(x.getMaxLat(), x.getMinLat(), x.getMaxLong(), x.getMinLong());
				heatmapGrid.heatmap = new double[rows][columns];
				int index = 0;
				for (int i = 0; i < rows; i++)
					for (int j = 0; j < columns; j++) {
						heatmapGrid.heatmap[i][j] = x.getMap(index);
						index++;
					}

//			System.out.println(heatmap);
			} else {
				int rows = (int) heatmapGrid.numCellsLon;
				int columns = (int) heatmapGrid.numCellsLat;
				// update heatmap only
				heatmapGrid.heatmap = new double[rows][columns];
				int index = 0;
				for (int i = 0; i < rows; i++)
					for (int j = 0; j < columns; j++) {
						heatmapGrid.heatmap[i][j] = x.getMap(index);
						index++;
					}

			}
		});

	}

	public boolean isInitialised()
	{
		return grid!=null;
	}
	public HashMap<Point, HashMap<String, Location3D>> getInitialGridPoints() {
		HashMap<Point, HashMap<String, Location3D>> initialPoints = new HashMap<Point, HashMap<String, Location3D>>();
//		if(grid!=null) {
//			System.out.println("grid not null");
		for (int i = 0; i < grid.numCellsLon; i++) {
			for (int j = 0; j < grid.numCellsLat; j++) {
				Point p = new Point(i, j);
				HashMap<String, Location3D> rectPoints = grid.getLocTopBottom(p);
				initialPoints.put(p, rectPoints);

			}
//			}
		}
//		System.out.println("Returning initial points");
//		System.out.println(initialPoints.get(new Point(0,0)).toString());
		return initialPoints;
	}

	public HashMap<Point, Double> getGridPriorities() {
		HashMap<Point, Double> priorities = new HashMap<Point, Double>();
		for (int i = 0; i < grid.numCellsLon; i++) {
			for (int j = 0; j < grid.numCellsLat; j++) {
				Point p = new Point(i, j);
				priorities.put(p, grid.heatmap[i][j]);

			}
		}
		return priorities;

	}

	// doesnt really do anything right now
	// cuz I'm confused
	public void scanHeatMap() {
		// for each point in the grid
		for (int i = 0; i < grid.numCellsLon; i++) {
			for (int j = 0; j < grid.numCellsLat; j++) {
				HashMap<String, Point> points = grid.gridToheatMap(new Point(i, j),
						(int) this.resolution);
				double sum = scanHeatMapCells(points.get("bottomLeft"), points.get("topRight"));
				// updated
				double percentage = sum/(this.resolution*this.resolution); 
				double priority = 0; 
				if(percentage < 100)
				{
					priority = 50.0*percentage/100.0; 
				}
				grid.heatmap[i][j]=priority;
				
			}
		}
		// go over the heat map
		// add up the doubles

	}

	public double scanHeatMapCells(Point bottomLeft, Point topRight) {
		double sum = 0;
		for (int i = (int) bottomLeft.getX(); i < topRight.getX(); i++) {
			for (int j = (int) bottomLeft.getY(); i < topRight.getY(); i++) {
				sum += this.heatmapGrid.heatmap[i][j];
			}
		}
		return sum;
	}

}
