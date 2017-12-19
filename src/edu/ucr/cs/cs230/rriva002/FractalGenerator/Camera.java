package edu.ucr.cs.cs230.rriva002.FractalGenerator;

import java.awt.image.BufferedImage;

public class Camera
{
	private BufferedImage image;
	private Vector3 position, direction, up, horizontal, vertical, imageCenter;
	public static final double fieldOfView = Math.toRadians(70.0);
	private static final double imageDistance = 1.0; 
	private double imageWidth, imageHeight, minX, minY, maxX, maxY;
	private int width, height;
	
	//Constructor. The up vector should be orthogonal to the direction vector.
	public Camera(Vector3 position, Vector3 direction, Vector3 up, int width, int height)
	{
		this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		this.width = width;
		this.height = height;
		
		adjustCamera(position, direction, up);
	}

	//Sets the camera's position and direction with the given vectors. The up vector should be
	//orthogonal to the direction vector.
	public void adjustCamera(Vector3 position, Vector3 direction, Vector3 up)
	{
		this.position = position;
		this.direction = Vector3.normalize(direction);
		this.up = Vector3.normalize(up);
		horizontal = Vector3.normalize(Vector3.cross(this.direction, this.up));
		vertical = Vector3.normalize(Vector3.cross(horizontal, this.direction));
		imageCenter = Vector3.add(position, Vector3.scale(this.direction, imageDistance));
		imageWidth = 2.0 * imageDistance * Math.tan(0.5 * fieldOfView);
		imageHeight = imageWidth / ((double) width / (double) height);
		minX = -0.5 * imageWidth;
		minY = -0.5 * imageHeight;
		maxX = 0.5 * imageWidth;
		maxY = 0.5 * imageHeight;
	}
	
	//Sets the specified pixel in the image with the given color.
	public void colorPixel(int x, int y, int color)
	{
		//The y coordinate is reversed since y = 0 is at the top of the image.
		image.setRGB(x, height - y - 1, color);
	}
	
	//Returns the normalized direction of the camera.
	public Vector3 getDirection()
	{
		return direction;
	}
	
	//Returns the height of the image plane in pixels.
	public int getHeight()
	{
		return height;
	}
	
	//Returns the image object.
	public BufferedImage getImage()
	{
		return image;
	}
	
	//Returns the camera's position in three-dimensional space.
	public Vector3 getPosition()
	{
		return position;
	}
	
	//Returns the camera's up vector (specifies which way is "up" on the image plane).
	public Vector3 getUp()
	{
		return up;
	}

	//Returns the width of the image plane in pixels.
	public int getWidth()
	{
		return width;
	}
	
	//Returns a grid of evenly spaced points within the specified pixel.
	public Vector3[][] pixelGrid(double x, double y, int gridSize)
	{
		Vector3[][] grid = new Vector3[gridSize][gridSize];
		Vector3 coordinates;
		double rCorner = (maxX - minX) / (double) width * x + minX, r;
		double sCorner = (maxY - minY) / (double) height * y + minY, s;
		double increment = (maxX - minX) / (double) width / (double) gridSize;
		
		for(int i = 0; i < grid.length; i++)
		{
			for(int j = 0; j < grid[i].length; j++)
			{
				r = rCorner + ((double) j + 0.5) * increment;
				s = sCorner + ((double) i + 0.5) * increment;
				coordinates = Vector3.add(Vector3.scale(horizontal, r), Vector3.scale(vertical, s));
				grid[j][i] = Vector3.add(imageCenter, coordinates);
			}
		}
		
		return grid;
	}
	
	//Returns the position of the center of the specified pixel in three-dimensional space.
	public Vector3 pixelPosition(double x, double y)
	{
		return pixelGrid(x, y, 1)[0][0];
	}
}
