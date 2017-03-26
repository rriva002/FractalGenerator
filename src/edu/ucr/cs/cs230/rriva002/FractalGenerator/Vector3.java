package edu.ucr.cs.cs230.rriva002.FractalGenerator;

public class Vector3
{
	private double x, y, z;
	
	//Constructor. Creates a new vector with the given components.
	public Vector3(double x, double y, double z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	//Constructor. Creates a copy of the given vector.
	public Vector3(Vector3 v)
	{
		this.x = v.x;
		this.y = v.y;
		this.z = v.z;
	}
	
	//Returns true if the given vector is equal to this one.
	public boolean equals(Vector3 v)
	{
		return x == v.x && y == v.y && z == v.z;
	}
	
	//Returns the X value.
	public double getX()
	{
		return x;
	}
	
	//Returns the Y value.
	public double getY()
	{
		return y;
	}
	
	//Returns the Z value.
	public double getZ()
	{
		return z;
	}
	
	//Returns the magnitude of the vector.
	public double magnitude()
	{
		return Math.sqrt(Vector3.dot(this, this));
	}
	
	//Returns a string representation of the vector.
	public String toString()
	{
		return "(" + x + ", " + y + ", " + z + ")";
	}
	
	//Returns the sum of two vectors.
	public static Vector3 add(Vector3 a, Vector3 b)
	{
		return new Vector3(a.x + b.x, a.y + b.y, a.z + b.z);
	}
	
	//Returns the cross product of two vectors.
	public static Vector3 cross(Vector3 a, Vector3 b)
	{
		return new Vector3(a.y * b.z - a.z * b.y, a.z * b.x - a.x * b.z, a.x * b.y - a.y * b.x);
	}
	
	//Returns the dot product of two vectors.
	public static double dot(Vector3 a, Vector3 b)
	{
		return a.x * b.x + a.y * b.y + a.z * b.z;
	}
	
	//Returns the given vector with components divided by the vector's magnitude.
	public static Vector3 normalize(Vector3 v)
	{
		return Vector3.scale(v, 1.0 / v.magnitude());
	}
	
	//Scales a vector by the given value.
	public static Vector3 scale(Vector3 v, double d)
	{
		return new Vector3(v.x * d, v.y * d, v.z * d);
	}
	
	//Subtracts one vector from another.
	public static Vector3 subtract(Vector3 a, Vector3 b)
	{
		return new Vector3(a.x - b.x, a.y - b.y, a.z - b.z);
	}
}
