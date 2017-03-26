package edu.ucr.cs.cs230.rriva002.FractalGenerator;

import java.util.List;

public abstract class Fractal
{
	private int iterations;
	protected double minDistance, maxDistance;
	protected static final String iterationsString = "Iterations";
	protected Vector3 defaultCameraPosition;
	
	//Constructor. Accepts values common to all fractal objects.
	public Fractal(int iterations, double maxDistance, Vector3 defaultCameraPosition)
	{
		this.maxDistance = maxDistance;
		this.defaultCameraPosition = defaultCameraPosition;
		
		setIterations(iterations);
	}
	
	//Returns the estimated distance from the given position to the fractal's surface.
	public abstract double estimateDistance(Vector3 z);
	
	//Returns the default camera position for this fractal.
	public Vector3 getDefaultCameraPosition()
	{
		return defaultCameraPosition;
	}
	
	//Returns ambient color values.
	public abstract double[] getAmbientColor();
	
	//Returns diffuse color values.
	public abstract double[] getDiffuseColor();
	
	//Returns the number of iterations to perform.
	public int getIterations()
	{
		return iterations;
	}
	
	//Returns the minimum step distance threshold for ray marching.
	public double getMinDistance()
	{
		return minDistance;
	}
	
	//Returns the maximum ray marching distance.
	public double getMaxDistance()
	{
		return maxDistance;
	}
	
	//Returns the fractal's parameters.
	public abstract List<String[]> getParameters();
	
	//Sets the number of iterations. The minimum step distance threshold is also updated.
	public void setIterations(int iterations)
	{
		this.iterations = iterations;
		
		updateMinDistance();
	}
	
	//Sets the specified parameter to the given value. Returns true if successful.
	public abstract boolean setParameter(String parameter, double value);
	
	//Returns the name of the fractal.
	public abstract String toString();
	
	//Updates the minimum step distance threshold for ray marching.
	protected abstract void updateMinDistance();
}
