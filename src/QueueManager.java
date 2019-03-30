import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

public class QueueManager {
	
	private ArrayList<Task> searchTasks;
	private ArrayList<Task> fireTasks;
	
	public QueueManager() {
		this.searchTasks = new ArrayList<>();
		this.fireTasks = new ArrayList<>();
	}
	
	public Task getNextSearchTask() {
		return this.searchTasks.remove(0);
	}
	
	public Task getNextFireTask() {
		return this.fireTasks.remove(0);
	}
	
	public void addNewSearchTask(Task t) {
		searchTasks.add(t);
		Collections.sort(searchTasks);
	}
	
	public void addNewFireTask(Task t) {
		fireTasks.add(t);
		Collections.sort(fireTasks);
	}
	
	public Task requestNewTask(UAVInfo uav) {
		if(uav.entityType.equals("FixedWing")) {
			if(searchTasks.size() != 0) {
				return getNextSearchTask();
			} else if(fireTasks.size() != 0) {
				return getNextFireTask();
			} else {
				return null;
			}
		} else {
			if(fireTasks.size() != 0) {
				return getNextFireTask();
			} else if(searchTasks.size() != 0) {
				return getNextSearchTask();
			} else {
				return null;
			}
		}
	}
}
