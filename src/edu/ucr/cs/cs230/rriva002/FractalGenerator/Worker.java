package edu.ucr.cs.cs230.rriva002.FractalGenerator;

import java.util.List;

public class Worker implements Runnable
{
	private List<Light> lights;
	private Camera camera;
	private Fractal fractal;
	private Light ambientLight;
	private double maxDistance;
	private int id, threads;
	
	//Constructor. Stores values for rendering a fractal.
	public Worker(Camera camera, Fractal fractal, Light ambientLight, List<Light> lights, int id, int threads)
	{
		this.lights = lights;
		this.camera = camera;
		this.fractal = fractal;
		this.ambientLight = ambientLight;
		this.maxDistance = camera.getPosition().magnitude() + fractal.getMaxDistance();
		this.id = id;
		this.threads = threads;
	}
	
	//Returns the gradient at the given position on the fractal's surface.
	private Vector3 gradient(Vector3 v, double distance)
	{
		Vector3 x = new Vector3(distance, 0.0, 0.0);
		Vector3 y = new Vector3(0.0, distance, 0.0);
		Vector3 z = new Vector3(0.0, 0.0, distance);
		double normalX = fractal.estimateDistance(Vector3.add(v, x)) - fractal.estimateDistance(Vector3.subtract(v, x));
		double normalY = fractal.estimateDistance(Vector3.add(v, y)) - fractal.estimateDistance(Vector3.subtract(v, y));
		double normalZ = fractal.estimateDistance(Vector3.add(v, z)) - fractal.estimateDistance(Vector3.subtract(v, z));
		return new Vector3(normalX, normalY, normalZ);
	}
	
	//Renders a pixel.
	private void renderPixel(int x, int y)
	{
		Vector3 pixelPosition = camera.pixelPosition((double) x, (double) y);
		Vector3 direction = Vector3.subtract(pixelPosition, camera.getPosition());
		Ray ray = new Ray(camera.getPosition(), direction);
		Vector3 intersection = ray.march(fractal.getMinDistance(), maxDistance, fractal);
		double[] colors = ambientLight.getColor();
		int color = 0;
		
		//Determines the lighting at the ray's point of intersection if it intersected.
		if(ray.intersected())
		{
			//Estimate the ambient intensity.
			Vector3 gradient = gradient(intersection, fractal.getMinDistance()), normal = Vector3.normalize(gradient), lightVector;
			double[] lightColor, ambientFractalColor = fractal.getAmbientColor(), diffuseFractalColor = fractal.getDiffuseColor();
			double lightDistance, intensity = Math.min(1.0, gradient.magnitude() / fractal.getMinDistance())
				* Math.max(0.0, Vector3.dot(normal, Vector3.normalize(intersection)));
			
			//Color the surface according to the ambient intensity and color.
			for(int i = 0; i < colors.length; i++)
			{
				colors[i] *= intensity * ambientFractalColor[i];
			}
			
			//Color the surface according to each point light.
			for(Light light : lights)
			{
				lightVector = Vector3.subtract(light.getPosition(), intersection);
				lightDistance = lightVector.magnitude();
				ray = new Ray(Vector3.add(intersection, Vector3.scale(normal, fractal.getMinDistance())), lightVector);
				
				//Determine whether the light is obstructed.
				ray.march(fractal.getMinDistance(), lightDistance, fractal);
				
				//If no obstruction exists, color the surface according to the light's color and intensity.
				if(!ray.intersected())
				{
					intensity = Math.max(0.0, Vector3.dot(normal, ray.getDirection())) / Math.pow(lightDistance, 2.0);
					lightColor = light.getColor();
					
					for(int i = 0; i < colors.length; i++)
					{
						colors[i] += intensity * lightColor[i] * diffuseFractalColor[i];
					}
				}
			}
		}
		
		//Determine the pixel's color.
		for(int i = 0; i < colors.length; i++)
		{
			color += (int) Math.round((double) 0xFF * Math.min(1.0, colors[i])) << 16 - 8 * i;
		}
		
		camera.colorPixel(x, y, color);
	}
	
	//Render each pixel assigned to the worker.
	public void run()
	{
		for(int i = id; i < camera.getHeight(); i += threads)
		{
			for(int j = 0; j < camera.getWidth(); j++)
			{
				renderPixel(j, i);
			}
		}
	}
}
