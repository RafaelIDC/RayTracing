package edu.cg.scene.lightSources;

import edu.cg.algebra.Point;
import edu.cg.algebra.Vec;

public abstract class Light {
	protected Vec intensity = new Vec(1, 1, 1); //white color

	@Override
	public String toString() {
		String endl = System.lineSeparator();
		return "Intensity: " + intensity + endl;
	}
	
	public Light initIntensity(Vec intensity) {
		this.intensity = intensity;
		return this;
	}

	public abstract Vec hitToLight(Point hitPoint);

	public abstract Vec getIntensity(Point hitPoint);

	public abstract Point getPosition();
	
	//TODO: add some methods
}
