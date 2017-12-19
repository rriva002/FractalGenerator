package edu.ucr.cs.cs230.rriva002.FractalGenerator;

import java.util.ArrayList;
import java.util.List;

public class Mandelbox extends Fractal
{
	private double scale, minRadiusSquared, boxFoldFactor;
	private static final double defaultCameraAngle = Math.toRadians(-40.0);
	private static final String scaleString = "Scale", minRadiusString = "Inner Radius";
	private static final String boxFoldString = "Box Fold Factor";
	
	//Constructor. Stores the given fractal parameters.
	public Mandelbox(int iterations, double scale, double minRadius, double boxFoldFactor)
	{
		//The maximum ray marching distance and default camera position aren't set here due to their
		//dependence on other values.
		super(iterations, Double.NaN, null);
		
		this.scale = scale;
		this.minRadiusSquared = minRadius * minRadius;
		this.boxFoldFactor = boxFoldFactor;
		
		//Update the default camera position and maximum ray marching distance.
		updateParameterDependents();
	}
	
	//Folds each component of the given vector across the sides of a cube.
	private Vector3 boxFold(Vector3 v)
	{
		double[] components = new double[]{v.getX(), v.getY(), v.getZ()};
		
		for(int i = 0; i < components.length; i++)
		{
			if(components[i] > 1.0)
			{
				components[i] = 2.0 - components[i];
			}
			else if(components[i] < -1.0)
			{
				components[i] = -2.0 - components[i];
			}
		}
		
		return new Vector3(components[0], components[1], components[2]);
	}

	//Returns the estimated distance from the given position to the fractal's surface.
	public double estimateDistance(Vector3 v)
	{
		Vector3 z = new Vector3(v);
		double sphereRadius, runningDerivative = 1.0;
		
		for(int i = 0; i < getIterations(); i++)
		{
			z = Vector3.scale(boxFold(z), boxFoldFactor);
			sphereRadius = sphereFold(z);
			z = Vector3.add(Vector3.scale(z, scale * sphereRadius), v);
			runningDerivative *= Math.abs(boxFoldFactor * sphereRadius * scale);
			runningDerivative++;
		}
		
		return z.magnitude() / Math.abs(runningDerivative);
	}
	
	//Returns ambient color values.
	public double[] getAmbientColor()
	{
		return new double[]{0.3, 0.3, 0.3};
	}
	
	//Returns diffuse color values.
	public double[] getDiffuseColor()
	{
		return new double[]{1.0, 1.0, 1.0};
	}
	
	//Returns the fractal's parameters.
	public List<String[]> getParameters()
	{
		List<String[]> parameters = new ArrayList<String[]>();
		
		parameters.add(new String[]{iterationsString, Integer.toString(getIterations())});
		parameters.add(new String[]{scaleString, Double.toString(scale)});
		parameters.add(new String[]{minRadiusString, Double.toString(Math.sqrt(minRadiusSquared))});
		parameters.add(new String[]{boxFoldString, Double.toString(boxFoldFactor)});
		return parameters;
	}
	
	//Sets the specified parameter to the given value. Returns true if successful.
	public boolean setParameter(String parameter, double value)
	{
		if(parameter.equals(iterationsString))
		{
			//The iterations must be 1 or more.
			if((int) value < 1)
			{
				return false;
			}
			
			setIterations((int) value);
		}
		else if(parameter.equals(scaleString))
		{
			//The absolute value of the scale must be more than 1. 
			if(Math.abs(value) <= 1.0)
			{
				return false;
			}
			
			scale = value;
			
			//Update the default camera position and maximum ray marching distance.
			updateParameterDependents();
		}
		else if(parameter.equals(minRadiusString))
		{
			//The minimum sphere radius must be between 0 and 1.
			if(value < 0.0 || value >= 1.0)
			{
				return false;
			}
			
			minRadiusSquared = value * value;
		}
		else if(parameter.equals(boxFoldString))
		{
			//The absolute value of the box fold factor must be 0.5 or more.
			if(Math.abs(value) < 0.5)
			{
				return false;
			}
			
			boxFoldFactor = value;
			
			//Update the default camera position and maximum ray marching distance.
			updateParameterDependents();
		}
		
		return true;
	}
	
	//Fold the given vector across the inner or outer radius of a sphere.
	private double sphereFold(Vector3 v)
	{
		double magnitudeSquared = Vector3.dot(v, v);
		
		if(magnitudeSquared < minRadiusSquared)
		{
			return 1.0 / minRadiusSquared;
		}
		else if(magnitudeSquared < 1.0)
		{
			return 1.0 / magnitudeSquared;
		}
		
		return 1.0;
	}
	
	//Returns the name of the fractal.
	public String toString()
	{
		return "Mandelbox";
	}
	
	//Updates the minimum step distance threshold for ray marching.
	protected void updateMinDistance()
	{
		minDistance = 1.0 / (double) (getIterations() * 10.0);
	}
	
	//Sets the default camera position and maximum ray marching distance according to the size of
	//the Mandelbox.
	private void updateParameterDependents()
	{
		double cosine = Math.cos(defaultCameraAngle), sine = Math.sin(defaultCameraAngle);
		double tangent = Math.tan(Camera.fieldOfView / 2.0);
		double halfSideLength = 2.0 * (scale < -1.0 ? 1.0 : (scale + 1.0) / (scale - 1.0));
		Vector3 position = new Vector3(cosine, sine, 0.0);
		
		//This needs to be modified to fully account for the box fold factor.
		halfSideLength *= 1.0 * (boxFoldFactor > 0.0 ? 1.0 : (scale - 1.0) / (scale + 1.0));
		maxDistance = new Vector3(halfSideLength, halfSideLength, halfSideLength).magnitude();
		defaultCameraPosition = Vector3.scale(position, maxDistance + halfSideLength / tangent);
	}
}
