package edu.ucr.cs.cs230.rriva002.FractalGenerator;

import java.util.ArrayList;
import java.util.List;

public class Mandelbulb extends Fractal
{
	private double threshold, power, thetaFactor, phiFactor;
	private static final String powerString = "Power", thetaFactorString = "Theta Factor", phiFactorString = "Phi Factor";
	
	//Constructor. Stores the given fractal parameters.
	public Mandelbulb(int iterations, double threshold, double power, double thetaFactor, double phiFactor)
	{
		super(iterations, threshold, new Vector3(0.0, -2.5, 0.0));
		
		this.threshold = threshold;
		this.power = power;
		this.thetaFactor = thetaFactor;
		this.phiFactor = phiFactor;
	}
	
	//Returns the estimated distance from the given position to the fractal's surface.
	public double estimateDistance(Vector3 v)
	{
		Vector3 z = new Vector3(v);
		double radius = z.magnitude(), runningDerivative = 1.0, theta, phi;
		
		for(int i = 0; i < getIterations() && radius < threshold; i++)
		{
			theta = thetaFactor * Math.acos(z.getZ() / radius);
			phi = phiFactor * Math.atan(z.getY() / z.getX());
			runningDerivative = Math.pow(radius, power - 1.0) * power * runningDerivative + 1.0;
			z = new Vector3(Math.sin(theta) * Math.cos(phi), Math.sin(phi) * Math.sin(theta), Math.cos(theta));
			z = Vector3.add(Vector3.scale(z, Math.pow(radius, power)), v);
			radius = z.magnitude();
		}
		
		return 0.5 * Math.log(radius) * radius / runningDerivative;
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
		parameters.add(new String[]{powerString, Double.toString(power)});
		parameters.add(new String[]{thetaFactorString, Double.toString(thetaFactor)});
		parameters.add(new String[]{phiFactorString, Double.toString(phiFactor)});
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
		else if(parameter.equals(powerString))
		{
			//The power must be more than 1.
			if(value <= 1.0)
			{
				return false;
			}
			
			power = value;
		}
		else if(parameter.equals(thetaFactorString))
		{
			thetaFactor = value;
		}
		else if(parameter.equals(phiFactorString))
		{
			phiFactor = value;
		}
		
		return true;
	}
	
	//Returns the name of the fractal.
	public String toString()
	{
		return "Mandelbulb";
	}
	
	//Updates the minimum step distance threshold for ray marching.
	protected void updateMinDistance()
	{
		minDistance = 1.0 / (double) (getIterations() * 100.0);
	}
}
