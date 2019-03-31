import java.awt.Point;

import afrl.cmasi.Location3D;

public class Task implements Comparable<Task>{
	public enum TaskType {
		SEARCH,
		MAP,
		REFUEL
	}
	
	private TaskType type;
	public Location3D targetLocation;
	public Location3D startSearchLocation;
	public Location3D endSearchLocation;
	public float priority;
	public Point hashPoint;
	
	public int count = 0;
	
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
	
	public Boolean isFinished() {
		count++;
		if(count % 50 == 0) {
			return true;
		}
		return false;
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
