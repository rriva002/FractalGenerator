package edu.ucr.cs.cs230.rriva002.FractalGenerator;

import java.util.HashMap;
import java.util.Map;

public class Light
{
	public static enum Parameter{X, Y, Z, RED, GREEN, BLUE, BRIGHTNESS};
	private Vector3 position;
	private double red, green, blue, brightness;
	
	//Constructor. Creates a light with the given values.
	public Light(Vector3 position, double red, double green, double blue, double brightness)
	{
		this.position = position;
		this.brightness = Math.max(0.0, brightness);
		this.red = Math.max(0.0, Math.min(red, 1.0));
		this.green = Math.max(0.0, Math.min(green, 1.0));
		this.blue = Math.max(0.0, Math.min(blue, 1.0));
	}
	
	//Constructor. Creates a light with the values specified in the given map.
	public Light(Map<Light.Parameter, Double> parameters)
	{
		this.position = new Vector3(parameters.get(Parameter.X), parameters.get(Parameter.Y), parameters.get(Parameter.Z));
		this.brightness = Math.max(0.0, parameters.get(Parameter.BRIGHTNESS));
		this.red = Math.max(0.0, Math.min(parameters.get(Parameter.RED), 1.0));
		this.green = Math.max(0.0, Math.min(parameters.get(Parameter.GREEN), 1.0));
		this.blue = Math.max(0.0, Math.min(parameters.get(Parameter.BLUE), 1.0));	
	}
	
	//Returns the light's color.
	public double[] getColor()
	{
		return new double[]{red * brightness, green * brightness, blue * brightness};
	}
	
	//Returns the light's values in a map.
	public Map<Parameter, Double> getParameters()
	{
		Map<Parameter, Double> parameters = new HashMap<Parameter, Double>(Parameter.values().length);
		
		parameters.put(Parameter.X, position.getX());
		parameters.put(Parameter.Y, position.getY());
		parameters.put(Parameter.Z, position.getZ());
		parameters.put(Parameter.RED, red);
		parameters.put(Parameter.GREEN, green);
		parameters.put(Parameter.BLUE, blue);
		parameters.put(Parameter.BRIGHTNESS, brightness);
		return parameters;
	}
	
	//Returns the light's position in three-dimensional space.
	public Vector3 getPosition()
	{
		return position;
	}
}
