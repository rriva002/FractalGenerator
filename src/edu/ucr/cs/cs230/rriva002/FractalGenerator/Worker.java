package edu.ucr.cs.cs230.rriva002.FractalGenerator;

import java.util.List;

public class Worker implements Runnable
{
	private List<Light> lights;
	private Camera camera;
	private Fractal fractal;
	private Light ambientLight;
	private double maxDistance;
	private int antialiasingFactor, id, threads;
	
	//Constructor. Stores values for rendering a fractal.
	public Worker(Camera camera, Fractal fractal, Light ambientLight, List<Light> lights,
		int antialiasingFactor, int id, int threads)
	{
		this.lights = lights;
		this.camera = camera;
		this.fractal = fractal;
		this.ambientLight = ambientLight;
		this.maxDistance = camera.getPosition().magnitude() + fractal.getMaxDistance();
		this.antialiasingFactor = antialiasingFactor;
		this.id = id;
		this.threads = threads;
	}
	
	//Returns the ambient intensity at the given position on the fractal's surface. Currently not
	//calculated correctly; this very rough estimate is the product of how open the surface is to
	//the environment and how close the normal is to pointing directly away from the origin.
	private double ambientIntensity(Vector3 intersection, Vector3 gradient, Vector3 normal)
	{
		double openness = Math.min(1.0, gradient.magnitude() / fractal.getMinDistance());
		double awayness = Math.max(0.0, Vector3.dot(normal, Vector3.normalize(intersection)));
		return openness * awayness;
	}
	
	//Returns the gradient at the given position on the fractal's surface.
	private Vector3 gradient(Vector3 v, double distance)
	{
		Vector3 x = new Vector3(distance, 0.0, 0.0), y = new Vector3(0.0, distance, 0.0);
		Vector3 z = new Vector3(0.0, 0.0, distance);
		Vector3 vPlusX = Vector3.add(v, x), vMinusX = Vector3.subtract(v, x);
		Vector3 vPlusY = Vector3.add(v, y), vMinusY = Vector3.subtract(v, y);
		Vector3 vPlusZ = Vector3.add(v, z), vMinusZ = Vector3.subtract(v, z);
		double normalX = fractal.estimateDistance(vPlusX) - fractal.estimateDistance(vMinusX);
		double normalY = fractal.estimateDistance(vPlusY) - fractal.estimateDistance(vMinusY);
		double normalZ = fractal.estimateDistance(vPlusZ) - fractal.estimateDistance(vMinusZ);
		return new Vector3(normalX, normalY, normalZ);
	}
	
	//Renders a pixel.
	private void renderPixel(int x, int y)
	{
		Vector3[][] grid = camera.pixelGrid(x, y, antialiasingFactor);
		double red = 0.0, green = 0.0, blue = 0.0;
		int color;
		
		for(int i = 0; i < grid.length; i++)
		{
			for(int j = 0; j < grid[i].length; j++)
			{
				color = renderSubpixel(grid[j][i]);
				red += (double) (color >> 16);
    			green += (double) ((color >> 8) & 0xFF);
    			blue += (double) (color & 0xFF);
			}
		}
		
		red = Math.round(red / Math.pow((double) antialiasingFactor, 2.0));
    	green = Math.round(green / Math.pow((double) antialiasingFactor, 2.0));
    	blue = Math.round(blue / Math.pow((double) antialiasingFactor, 2.0));
    	
		camera.colorPixel(x, y, ((int) red << 16) + ((int) green << 8) + (int) blue);
	}
	
	//Determines the color at the given point on the image plane.
	private int renderSubpixel(Vector3 position)
	{
		//Vector3 pixelPosition = camera.pixelPosition((double) x, (double) y);
		Vector3 direction = Vector3.subtract(position, camera.getPosition());
		Ray ray = new Ray(camera.getPosition(), direction);
		Vector3 intersection = ray.march(fractal.getMinDistance(), maxDistance, fractal);
		double[] colors = ambientLight.getColor();
		int color = 0;
		
		//Determines the lighting at the ray's point of intersection if it intersected.
		if(ray.intersected())
		{
			//Estimate the ambient intensity.
			Vector3 gradient = gradient(intersection, fractal.getMinDistance());
			Vector3 normal = Vector3.normalize(gradient), lightVector, scaledNormal;
			double[] ambientFractalColor = fractal.getAmbientColor();
			double[] diffuseFractalColor = fractal.getDiffuseColor(), lightColor;
			double intensity = ambientIntensity(intersection, gradient, normal), lightDistance, dot;
			
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
				scaledNormal = Vector3.scale(normal, fractal.getMinDistance());
				ray = new Ray(Vector3.add(intersection, scaledNormal), lightVector);
				
				//Determine whether the light is obstructed.
				ray.march(fractal.getMinDistance(), lightDistance, fractal);
				
				//If no obstruction exists, color the surface according to the light's color and
				//intensity.
				if(!ray.intersected())
				{
					dot = Vector3.dot(normal, ray.getDirection());
					intensity = Math.max(0.0, dot) / Math.pow(lightDistance, 2.0);
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
		
		return color;
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
