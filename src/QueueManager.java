import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;

import afrl.cmasi.Location3D;

public class QueueManager {
	
	private ArrayList<Task> searchTasks;
	private ArrayList<Task> fireTasks;
	
	public QueueManager() {
		this.searchTasks = new ArrayList<>();
		this.fireTasks = new ArrayList<>();
	}
	
	private Task getNextSearchTask() {
		return this.searchTasks.remove(0);
	}
	
	private Task getNextFireTask() {
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
	
	public void notifyOfFire(Task currentTask, Location3D location) {
		currentTask.setTaskType(Task.TaskType.MAP);
		currentTask.setTargetLocation(location);
		addNewFireTask(currentTask);
	}
	
	public void setupWithCells(HashMap<Point, HashMap<String, Location3D>> points) {
		// public Task(TaskType type, Location3D startSearch, Location3D endSearch, float priority) 
		for(Point p: points.keySet()) {
			Task t = new Task(Task.TaskType.SEARCH, points.get(p).get("bottomLeft"), points.get(p).get("topRight"), 50);
			addNewSearchTask(t);
		}
	}
	
	public void updatePriorities(HashMap<Point, Double> points){
		for(Task t: searchTasks) {
			t.priority = points.get(t.hashPoint);
		}
		
		for(Task t: fireTasks) {
			t.priority = points.get(t.hashPoint);
		}
		
		Collections.sort(searchTasks);
		Collections.sort(fireTasks);
		
	}
	// Check sort is correctly putting highest at start.
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
