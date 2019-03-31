import java.awt.Point;

import afrl.cmasi.Location3D;
import afrl.cmasi.VehicleActionCommand;

public class UAVInfo {
	Location3D currentLocation = null;
	double currentEnergyRate;
	double currentEnergy;
	double maxSpeed = -1;
	long id;
	String entityType;
	VehicleActionCommand currentCommand = null;
	Location3D targetLocation = null;
	Point currentCell = null;
	Point commandCell = null;
	Task currentTask;

	public UAVInfo(long id) {
		this.id = id;
	}
	
	public void setCurrentTask(Task t) {
		this.currentTask = t;
	}
	
	public Task getCurrentTask() {
		return this.currentTask;
	}
	
}
