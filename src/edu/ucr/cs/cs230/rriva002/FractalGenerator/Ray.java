package edu.ucr.cs.cs230.rriva002.FractalGenerator;

public class Ray
{
	private Vector3 endpoint, direction;
	private boolean hit = false;
	
	//Constructor. Stores the endpoint and normalized direction.
	public Ray(Vector3 endpoint, Vector3 direction)
	{
		this.endpoint = endpoint;
		this.direction = Vector3.normalize(direction);
	}
	
	//Returns the direction of the ray.
	public Vector3 getDirection()
	{
		return direction;
	}
	
	//Returns the ray's endpoint.
	public Vector3 getEndpoint()
	{
		return endpoint;
	}
	
	//Returns whether the ray hit something. 
	public boolean intersected()
	{
		return hit;
	}
	
	//Returns the ray's point of intersection.
	public Vector3 march(double minDistance, double maxDistance, Fractal fractal)
	{
		double distance, totalDistance = 0.0;
		Vector3 position;
		
		hit = false;
		
		//Run until the maximum distance is reached.
		while(totalDistance < maxDistance)
		{
			//Estimate the distance to the fractal from the current position along the ray.
			position = Vector3.add(endpoint, Vector3.scale(direction, totalDistance));
			distance = fractal.estimateDistance(position);
			
			//Record a hit and break if the step distance is below the minimum distance threshold.
			if(distance < minDistance)
			{
				hit = true;
				break;
			}
			
			//Add the step distance to the total distance.
			totalDistance += distance;
		}
		
		return Vector3.add(endpoint, Vector3.scale(direction, totalDistance));
	}
}
