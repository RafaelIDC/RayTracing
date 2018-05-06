package edu.cg.scene.objects;

import edu.cg.UnimplementedMethodException;
import edu.cg.algebra.*;

public class Dome extends Shape {
	private Sphere sphere;
	private Plain plain;
	
	public Dome() {
		sphere = new Sphere().initCenter(new Point(0, -0.5, -6));
		plain = new Plain(new Vec(-1, 0, -1), new Point(0, -0.5, -6));
	}

	public Dome(Point center, double radius, double plainDistance, Vec directionFromCenter){
		double a = directionFromCenter.x;
		double b = directionFromCenter.y;
		double c = directionFromCenter.z;

		Point pointOnPlain = new Point (center.x + plainDistance * directionFromCenter.x,
							  			center.y + plainDistance * directionFromCenter.y,
							  			center.z + plainDistance*directionFromCenter.z);

		plain = new Plain(new Vec(a,b,c), pointOnPlain);

		sphere = new Sphere().initCenter(center).initRadius(radius);
	}
	
	@Override
	public String toString() {
		String endl = System.lineSeparator();
		return "Dome:" + endl + 
				sphere + plain + endl;
	}

	/*We draw the part of the sphere which is above the surface (N > 0)*/
	@Override
	public Hit intersect(Ray ray) {

		Hit hitSphere = sphere.intersect(ray);

		if (hitSphere == null)
			return null;

		Point closestSphereIntersect = ray.add(hitSphere.t());

		//If the intersection is on or above the plain
		double position = plain.testPoint(closestSphereIntersect);
		if (position >= 0)
		{
			return hitSphere;
		}

		//If the intersection is below the plain
		//plain = new Plain(plain.normal().neg().x, plain.normal().neg().y, plain.normal().neg().z, plain.getD());
		Hit hitPlain = plain.intersect(ray);
		//plain = new Plain(plain.normal().neg().x, plain.normal().neg().y, plain.normal().neg().z, plain.getD());

		//If the ray is parallel to the plain, below
		if (hitPlain == null)
			return null;

		//Otherwise, find intersection point
		Point plainIntersect = ray.add(hitPlain.t());

		//Draw the plain if it is within the intersection circle
		if (Ops.dist(plainIntersect, sphere.getCenter()) <= sphere.getRadius())
			return hitPlain;

		//The intersection is out of sphere bounds
		return null;

		//throw new UnimplementedMethodException("intersect(Ray)");
	}
}
