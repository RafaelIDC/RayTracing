package edu.cg.scene;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import edu.cg.Logger;
import edu.cg.algebra.*;
import edu.cg.scene.lightSources.Light;
import edu.cg.scene.objects.Surface;

public class Scene {
	private String name = "scene";
	private int maxRecursionLevel = 1;
	private int antiAliasingFactor = 1; //gets the values of 1, 2 and 3
	private boolean renderRefractions = false;
	private boolean renderReflections = false;
	
	private Point camera = new Point(0, 0, 5);
	private Vec ambient = new Vec(1, 1, 1); //white
	private Vec backgroundColor = new Vec(0, 0.5, 1); //blue sky
	private List<Light> lightSources = new LinkedList<>();
	private List<Surface> surfaces = new LinkedList<>();
	
	
	//MARK: initializers
	public Scene initCamera(Point camera) {
		this.camera = camera;
		return this;
	}
	
	public Scene initAmbient(Vec ambient) {
		this.ambient = ambient;
		return this;
	}
	
	public Scene initBackgroundColor(Vec backgroundColor) {
		this.backgroundColor = backgroundColor;
		return this;
	}
	
	public Scene addLightSource(Light lightSource) {
		lightSources.add(lightSource);
		return this;
	}
	
	public Scene addSurface(Surface surface) {
		surfaces.add(surface);
		return this;
	}
	
	public Scene initMaxRecursionLevel(int maxRecursionLevel) {
		this.maxRecursionLevel = maxRecursionLevel;
		return this;
	}
	
	public Scene initAntiAliasingFactor(int antiAliasingFactor) {
		this.antiAliasingFactor = antiAliasingFactor;
		return this;
	}
	
	public Scene initName(String name) {
		this.name = name;
		return this;
	}
	
	public Scene initRenderRefarctions(boolean renderRefarctions) {
		this.renderRefractions = renderRefarctions;
		return this;
	}
	
	public Scene initRenderReflections(boolean renderReflections) {
		this.renderReflections = renderReflections;
		return this;
	}
	
	//MARK: getters
	public String getName() {
		return name;
	}
	
	public int getFactor() {
		return antiAliasingFactor;
	}
	
	public int getMaxRecursionLevel() {
		return maxRecursionLevel;
	}
	
	public boolean getRenderRefractions() {
		return renderRefractions;
	}
	
	public boolean getRenderReflections() {
		return renderReflections;
	}
	
	@Override
	public String toString() {
		String endl = System.lineSeparator(); 
		return "Camera: " + camera + endl +
				"Ambient: " + ambient + endl +
				"Background Color: " + backgroundColor + endl +
				"Max recursion level: " + maxRecursionLevel + endl +
				"Anti aliasing factor: " + antiAliasingFactor + endl +
				"Light sources:" + endl + lightSources + endl +
				"Surfaces:" + endl + surfaces;
	}
	
	private static class IndexTransformer {
		private final int max;
		private final int deltaX;
		private final int deltaY;
		
		IndexTransformer(int width, int height) {
			max = Math.max(width, height);
			deltaX = (max - width) / 2;
			deltaY = (max - height) / 2;
		}
		
		Point transform(int x, int y) {
			double xPos = (2*(x + deltaX) - max) / ((double)max);
			double yPos = (max - 2*(y + deltaY)) / ((double)max);
			return new Point(xPos, yPos, 0);
		}
	}
	
	private transient IndexTransformer transformer = null;
	private transient ExecutorService executor = null;
	private transient Logger logger = null;
	
	private void initSomeFields(int imgWidth, int imgHeight, Logger logger) {
		this.logger = logger;
		//TODO: initialize your additional field here.
	}
	
	
	public BufferedImage render(int imgWidth, int imgHeight, Logger logger)
			throws InterruptedException, ExecutionException {
		
		initSomeFields(imgWidth, imgHeight, logger);
		
		BufferedImage img = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_RGB);
		transformer = new IndexTransformer(imgWidth, imgHeight);
		int nThreads = Runtime.getRuntime().availableProcessors();
		nThreads = nThreads < 2 ? 2 : nThreads;
		this.logger.log("Initialize executor. Using " + nThreads + " threads to render " + name);
		executor = Executors.newFixedThreadPool(nThreads);
		
		@SuppressWarnings("unchecked")
		Future<Color>[][] futures = (Future<Color>[][])(new Future[imgHeight][imgWidth]);
		
		this.logger.log("Starting to shoot " +
			(imgHeight*imgWidth*antiAliasingFactor*antiAliasingFactor) +
			" rays over " + name);
		
		for(int y = 0; y < imgHeight; ++y)
			for(int x = 0; x < imgWidth; ++x)
				futures[y][x] = calcColor(x, y);
		
		this.logger.log("Done shooting rays.");
		this.logger.log("Waiting for results...");
		
		for(int y = 0; y < imgHeight; ++y)
			for(int x = 0; x < imgWidth; ++x) {
				Color color = futures[y][x].get();
				img.setRGB(x, y, color.getRGB());
			}
		
		executor.shutdown();
		
		this.logger.log("Ray tracing of " + name + " has been completed.");
		
		executor = null;
		transformer = null;
		this.logger = null;
		
		return img;
	}

	//Shoots a ray from camera to point x,y on screen,
	//Gets the color in pixel x,y
	private Future<Color> calcColor(int x, int y) {
		return executor.submit(() -> {
			//TODO: change this method implementation to implement super sampling

			Point pointOnScreenPlain = transformer.transform(x, y);
			Ray ray = new Ray(camera, pointOnScreenPlain);
			return calcColor(ray, 0).toColor();
		});
	}
	
	//recursionLevel = the number of recursions already performed (how deep are we)

	private Vec calcColor(Ray ray, int recursionLevel) {

		double t_min = Double.MAX_VALUE;
		Surface hitSurface = null;
		Hit hit = null;
		Hit hitTemp = null;

		//Find the closest surface for this ray
		for (Surface s : surfaces)
		{
			hitTemp = s.intersect(ray);
			if (hitTemp != null && hitTemp.t() < t_min)
			{
				t_min = hitTemp.t();
				hit = hitTemp;
				hit.setSurface(s);
			}
		}

		//No surfaces found: paint background
		if (hit == null)
			return new Vec(backgroundColor);

		//An intersection was found
		hitSurface = hit.getSurface();
		Vec	diffuse = new Vec(0,0,0);
		Vec	specular = new Vec(0,0,0);
		Point hitPoint = ray.getHittingPoint(hit);

		for (Light L : lightSources) {
			boolean shadow = false;
			Vec dirToLight = L.hitToLight(hitPoint);
			Ray shadowRay = new Ray(hitPoint.add(Ops.epsilon, dirToLight), dirToLight);
			double tToLight = (L.getPosition().x - hitPoint.x) / (shadowRay.direction().x + Double.MIN_VALUE);

			for (Surface s : surfaces) {

				Hit shadowHit = s.intersect(shadowRay);

				//Check if the object the shadow hit is not *behind* the light
				//if t(object) > t(light) then use the light

				if (s != hitSurface && shadowHit != null) {
					//compare t of shadow ray to light, and t of shadow ray to intersected object

					if (shadowHit.t() < tToLight)
					{
						shadow = true;
						break;
					}
				}
			}

			if (shadow)
				continue;

			Vec intensity = L.getIntensity(hitPoint);

			//Add diffuse
			double NL = hit.getNormalToSurface().dot(L.hitToLight(hitPoint));
			NL = (NL < 0 ? 0 : NL);
			Vec KdNL = hitSurface.Kd(hitPoint).mult(NL);
			Vec KdNLI = intensity.mult(KdNL);
			diffuse = diffuse.add(KdNLI);

			//Add specular
			double VR = ray.direction().dot(Ops.reflect(L.hitToLight(hitPoint),hit.getNormalToSurface()));
			VR = Math.pow(VR, hitSurface.shininess());
			specular = specular.add(hitSurface.Ks().mult(VR).mult(intensity));
		}

		Vec res = ambient.mult(hitSurface.Ka())
				.add(diffuse)
				.add(specular);

		recursionLevel++;
		if (recursionLevel == getMaxRecursionLevel())
			return res;

		Vec reflectionDir = new Vec(Ops.reflect(ray.direction(),hit.getNormalToSurface()));
		Ray reflection = new Ray(hitPoint, reflectionDir);
		reflection = new Ray(reflection.add(Ops.epsilon), reflectionDir);
		return res.add(calcColor(reflection, recursionLevel).mult(hitSurface.reflectionIntensity()));
	}
}
