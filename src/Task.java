import java.awt.Point;

import afrl.cmasi.Location3D;

public class Task implements Comparable<Task>{
	public enum TaskType {
		SEARCH,
		MAP,
		REFUEL
	}
	
	private TaskType type;
	private Location3D targetLocation;
	private Location3D startSearchLocation;
	private Location3D endSearchLocation;
	public float priority;
	private Point hashPoint;
	
	public Task(TaskType type, Location3D targetLocation, float priority) {
		this.type = type;
		this.targetLocation = targetLocation;
		this.priority = priority;
	}
	
	public Task(TaskType type, Location3D startSearch, Location3D endSearch, float priority) {
		this.type = type;
		this.startSearchLocation = startSearch;
		this.endSearchLocation = endSearch;
		this.priority = priority;
	}
	
	public Task(TaskType type, Location3D targetLocation, Location3D startSearch, Location3D endSearch, float priority) {
		this.type = type;
		this.targetLocation = targetLocation;
		this.startSearchLocation = startSearch;
		this.endSearchLocation = endSearch;
		this.priority = priority;
	}
	
	public TaskType getTaskType() {
		return this.type;
	}
	
	public void setTaskType(TaskType t) {
		this.type = t;
	}
	
	public void setTargetLocation(Location3D target) {
		this.targetLocation = target;
	}
	
	@Override
	public int compareTo(Task t) {
		return Float.compare(priority, t.priority);
	}
	
}
