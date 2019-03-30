import afrl.cmasi.Location3D;

public class Task {
	public enum TaskType {
		SEARCHING,
		MAPPING,
		REFUELING
	}
	
	private TaskType type;
	private Location3D targetLocation;
	private Location3D endSearchLocation;
	public float priority;
	
	public Task(TaskType type, Location3D targetLocation, float priority) {
		this.type = type;
		this.targetLocation = targetLocation;
		this.priority = priority;
	}
	
	public Task(TaskType type, Location3D targetLocation, Location3D endSearch, float priority) {
		this.type = type;
		this.targetLocation = targetLocation;
		this.endSearchLocation = endSearch;
		this.priority = priority;
	}
}
