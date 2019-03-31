import java.awt.Point;

import afrl.cmasi.Location3D;

public class Task implements Comparable<Task> {
	public enum TaskType {
		SEARCH, MAP, REFUEL
	}

	private TaskType type;
	public Location3D targetLocation;
	public Location3D startSearchLocation;
	public Location3D endSearchLocation;
	public Double priority;
	public Point hashPoint;
	boolean hasReachedTask = false;
	public int count = 0;

	public Task(TaskType type, Location3D targetLocation, double priority, Point hashpoint) {
		this.type = type;
		this.targetLocation = targetLocation;
		this.priority = priority;
		this.hashPoint = hashpoint;
	}

	public Task(TaskType type, Location3D startSearch, Location3D endSearch, double priority, Point hashpoint) {
		this.type = type;
		this.startSearchLocation = startSearch;
		this.endSearchLocation = endSearch;
		this.priority = priority;
		this.hashPoint = hashpoint;
	}

	public Task(TaskType type, Location3D targetLocation, Location3D startSearch, Location3D endSearch, double priority,
			Point hashpoint) {
		this.type = type;
		this.targetLocation = targetLocation;
		this.startSearchLocation = startSearch;
		this.endSearchLocation = endSearch;
		this.priority = priority;
		this.hashPoint = hashpoint;
	}

	public TaskType getTaskType() {
		return this.type;
	}

	public Boolean isFinished(Location3D currLoc) {
		double dist = 200;
		if (hasReachedTask)
			count++;
		if (this.type == TaskType.SEARCH) {
			if (Math.abs(haversine(this.startSearchLocation, currLoc)) < dist) {
				hasReachedTask = true;
			}
			if (hasReachedTask) {
				// compare current location to end search
				if (Math.abs(haversine(this.endSearchLocation, currLoc)) < dist) {
					System.out.println("Search finished");
					return true;
				} else {
					return false;
				}
			}

		} else if (this.type == TaskType.MAP) {

			if (!hasReachedTask && Math.abs(haversine(this.targetLocation, currLoc)) < dist) {
				hasReachedTask = true;
				// firecounter flag = true
			}
			if (hasReachedTask) {
				if (count % 50 == 0) {

					return true;
				}
				return false;
			}
		} else {
			// dosomething here
			return true;
		}
		return false;
	}

	public void setTaskType(TaskType t) {
		this.type = t;
		hasReachedTask = false;
	}

	public void setTargetLocation(Location3D target) {
		this.targetLocation = target;
	}

	@Override
	public int compareTo(Task t) {
		return Double.compare(priority, t.priority);
	}

	// haversine in meters
	public double haversine(Location3D loc1, Location3D loc2) {
		double lat1 = loc1.getLatitude();
		double lon1 = loc1.getLongitude();
		double lat2 = loc2.getLatitude();
		double lon2 = loc2.getLongitude();
		double R = 6372.8;
		// earths rad in km
		double dLat = Math.toRadians(lat2 - lat1);
		double dLon = Math.toRadians(lon2 - lon1);
		lat1 = Math.toRadians(lat1);
		lat2 = Math.toRadians(lat2);
		double a = Math.pow(Math.sin(dLat / 2), 2) + Math.pow(Math.sin(dLon / 2), 2) * Math.cos(lat1) * Math.cos(lat2);
		double c = 2 * Math.asin(Math.sqrt(a));
		return R * 1000.0 * c;
	}
}
