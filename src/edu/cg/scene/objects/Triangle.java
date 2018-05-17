package edu.cg.scene.objects;

import edu.cg.UnimplementedMethodException;
import edu.cg.algebra.Hit;
import edu.cg.algebra.Point;
import edu.cg.algebra.Ray;
import edu.cg.algebra.Vec;

import java.util.Vector;

public class Triangle extends Shape {
	private Point p1, p2, p3;

	private  Plain plain;


	public Triangle() {
		p1 = p2 = p3 = null;
	}
	
	public Triangle(Point p1, Point p2, Point p3) {
		this.p1 = p1;
		this.p2 = p2;
		this.p3 = p3;

		Vec u = p2.sub(p1);
		Vec v = p3.sub(p1);
		Vec normal = u.cross(v).normalize();

		double a = normal.x;
		double b = normal.y;
		double c = normal.z;
		double d = p1.x * normal.x + p1.y * normal.y + p1.z * normal.z;

		this.plain = new Plain(a, b, c, -d);
	}
	
	@Override
	public String toString() {
		String endl = System.lineSeparator();
		return "Triangle:" + endl +
				"p1: " + p1 + endl + 
				"p2: " + p2 + endl +
				"p3: " + p3 + endl;
	}

	@Override
	public Hit intersect(Ray ray) {
		Hit plainHit = this.plain.intersect(ray);
		if(plainHit == null)
			return null;		// no intersection with the plain

		//Point intersectPoint = ray.source().add(ray.direction().mult(plainHit.t()));
		Point intersectPoint = ray.getHittingPoint(plainHit);

		Vec v1 = this.p1.sub(ray.source());
		Vec u1 = this.p2.sub(ray.source());
		Vec N1 = (u1.cross(v1)).normalize();

		Vec v2 = this.p2.sub(ray.source());
		Vec u2 = this.p3.sub(ray.source());
		Vec N2 = (u2.cross(v2)).normalize();

		Vec v3 = this.p3.sub(ray.source());
		Vec u3 = this.p1.sub(ray.source());
		Vec N3 = (u3.cross(v3)).normalize();

		Vec d = intersectPoint.sub(ray.source());

		if((d.dot(N1) >= 0) && (d.dot(N2) >= 0) && (d.dot(N3) >= 0)){
			return new Hit(plainHit.t(), plainHit.getNormalToSurface());
		}

		if((d.dot(N1) < 0) && (d.dot(N2) < 0) && (d.dot(N3) < 0)){
			return new Hit(plainHit.t(), plainHit.getNormalToSurface());
		}

		return null;		// the intersection isn't inside the triangle.


	}
}
