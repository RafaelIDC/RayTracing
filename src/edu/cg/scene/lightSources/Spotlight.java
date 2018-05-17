package edu.cg.scene.lightSources;

import edu.cg.algebra.Point;
import edu.cg.algebra.Ray;
import edu.cg.algebra.Vec;

public class Spotlight extends PointLight {
	private Vec direction;
	private double angle = 0.866; //cosine value ~ 30 degrees
	
	public Spotlight initDirection(Vec direction) {
		this.direction = direction;
		return this;
	}
	
	public Spotlight initAngle(double angle) {
		this.angle = angle;
		return this;
	}
	
	@Override
	public String toString() {
		String endl = System.lineSeparator();
		return "Spotlight: " + endl +
				description() + 
				"Direction: " + direction + endl +
				"Angle: " + angle + endl;
	}
	
	@Override
	public Spotlight initPosition(Point position) {
		return (Spotlight)super.initPosition(position);
	}
	
	@Override
	public Spotlight initIntensity(Vec intensity) {
		return (Spotlight)super.initIntensity(intensity);
	}
	
	@Override
	public Spotlight initDecayFactors(double q, double l, double c) {
		return (Spotlight)super.initDecayFactors(q, l, c);
	}

	public Vec getIntensity(Point hitPoint){

		Vec dirToPoint = new Vec(position, hitPoint).normalize();
		Vec D = direction.normalize();
		double angleToPoint = D.dot(dirToPoint);

		if (angleToPoint < angle)
			return new Vec(0);

		double d = hitPoint.dist(position);
		double calc = kc + (kl * d) + (kq * Math.pow(d, 2));
		double dotCalc = D.dot(dirToPoint);

		return (intensity.mult(dotCalc)).mult(1 / calc);
	}

	public Vec hitToLight(Point hitPoint)
	{
		return direction.neg().normalize();
	}

	public Point getPosition(){
		return position;
	}
}
