package edu.ucr.cs.cs230.rriva002.FractalGenerator;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class FractalRenderer
{
	private List<Light> lights = new ArrayList<Light>();
	private Light ambientLight = new Light(origin, 0.53, 0.81, 0.92, 1.0);
	private Camera camera;
	private Fractal fractal;
	private Vector3 defaultAxisOfRotation;
	private static final Vector3 origin = new Vector3(0.0, 0.0, 0.0);
	private static final double turnAngle = Math.toRadians(15.0), moveFactor = 0.75;
	private static final double epsilon = Math.pow(10.0, -15.0);
	private int width, height;
	
	//Constructor. Sets camera/light values for the given fractal.
	public FractalRenderer(int width, int height, Fractal fractal)
	{
		this.width = width;
		this.height = height;
		
		setFractal(fractal);
	}
	
	//Moves the camera in the given direction orthogonal to the camera's direction.
	public boolean cameraPan(Vector3 direction)
	{
		//Normalize the direction vector.
		direction = Vector3.normalize(direction);
		
		//The direction must be orthogonal to the camera's direction (or reasonably close to
		//orthogonal).
		if(Math.abs(Vector3.dot(camera.getDirection(), direction)) < epsilon)
		{
			//Determine the distance from the fractal.
			double distanceFromFractal = fractal.estimateDistance(camera.getPosition());
			
			//Scale the direction vector by a constant factor.
			direction = Vector3.scale(direction, distanceFromFractal * moveFactor);
			
			Vector3 position = Vector3.add(camera.getPosition(), direction);
			
			//Move the camera.
			camera.adjustCamera(position, camera.getDirection(), camera.getUp());
			return true;
		}
		
		return false;
	}
	
	//Turn the camera by a constant angle.
	public boolean cameraTurn(Vector3 direction)
	{
		return cameraTurn(direction, turnAngle);
	}
	
	//Turn the camera by the given angle.
	private boolean cameraTurn(Vector3 direction, double angle)
	{
		//Normalize the direction vector.
		direction = Vector3.normalize(direction);
		
		//The direction must be orthogonal to the camera's direction (or reasonably close to
		//orthogonal).
		if(Math.abs(Vector3.dot(camera.getDirection(), direction)) < epsilon)
		{
			//Rotate on the default axis of rotation for left turns.
			Vector3 axis = defaultAxisOfRotation;
			double dot = Vector3.dot(camera.getUp(), direction);
			
			if(1.0 - Math.abs(dot) < epsilon)
			{
				//Rotate around the left or right vector relative to the camera for up or down
				//turns.
				axis = 1.0 - dot < epsilon ? directionRight() : directionLeft();
			}
			else if(Math.abs(1.0 - Vector3.dot(directionRight(), direction)) < epsilon)
			{
				//Negate the default axis of rotation for right turns.
				axis = Vector3.subtract(origin, defaultAxisOfRotation);
			}
			
			//Set up the rotation matrix.
			double x = axis.getX(), y = axis.getY(), z = axis.getZ();
			double s = Math.sin(angle), c = Math.cos(angle), c1 = 1.0 - c;
			Vector3 rowX = new Vector3(c + x * x * c1, x * y * c1 - z * s, x * z * c1 + y * s);
			Vector3 rowY = new Vector3(y * x * c1 + z * s, c + y * y * c1, y * z * c1 - x * s);
			Vector3 rowZ = new Vector3(z * x * c1 - y * s, z * y * c1 + x * s, c + z * z * c1);
			Vector3 camDir = camera.getDirection(), up = camera.getUp();
			double directionX = Vector3.dot(camDir, rowX), directionY = Vector3.dot(camDir, rowY);
			double directionZ = Vector3.dot(camDir, rowZ);
			
			//Multiply the camera's direction and up vectors by the rotation matrix.
			direction = new Vector3(directionX, directionY, directionZ);
			up = new Vector3(Vector3.dot(up, rowX), Vector3.dot(up, rowY), Vector3.dot(up, rowZ));
			
			//Adjust the camera's direction.
			camera.adjustCamera(camera.getPosition(), direction, up);
			return true;
		}
		
		return false;
	}
	
	//Moves the camera forward or backward.
	public boolean cameraZoom(Vector3 direction)
	{
		double dot = Vector3.dot(camera.getDirection(), Vector3.normalize(direction));
		
		//The direction must be parallel to the camera's direction (or reasonably close to
		//parallel).
		if(1.0 - Math.abs(dot) < epsilon)
		{
			//Determine the distance from the fractal and move the camera in the specified
			//direction, scaled by a constant factor.
			double distance = fractal.estimateDistance(camera.getPosition());
			double zoom = dot * distance * (dot > 0.0 ? moveFactor : 1.0 / (1.0 - moveFactor));
			Vector3 zoomVector = Vector3.scale(camera.getDirection(), zoom);
			Vector3 position = Vector3.add(camera.getPosition(), zoomVector);
			
			//Move the camera.
			camera.adjustCamera(position, camera.getDirection(), camera.getUp());
			return true;
		}
		
		return false;
	}
	
	//Turns the camera to face the specified pixel, then moves the camera in the specified
	//direction.
	public boolean cameraZoom(int x, int y, boolean forward)
	{
		Vector3 pos = camera.getPosition(), dir = camera.getDirection(), up = camera.getUp();
		Vector3 pixelPosition = camera.pixelPosition((double) x, (double) height / 2.0);
		Vector3 difference = Vector3.subtract(pixelPosition, camera.getPosition());
		Vector3 toPixel = Vector3.normalize(difference), left = directionLeft();
		Vector3 turnDir = Vector3.dot(left, toPixel) > 0.0 ? left : directionRight();
		double angle = Math.acos(Vector3.dot(toPixel, camera.getDirection()));
		boolean successful = false;
		
		//Turn the camera left or right according to the pixel's x coordinate.
		if(cameraTurn(turnDir, angle))
		{
			//If successful, turn the camera up or down according to the pixel's y coordinate, then
			//zoom the camera.
			pixelPosition = camera.pixelPosition((double) width / 2.0, (double) y);
			toPixel = Vector3.normalize(Vector3.subtract(pixelPosition, camera.getPosition()));
			turnDir = Vector3.dot(camera.getUp(), toPixel) > 0.0 ? directionUp() : directionDown();
			angle = Math.acos(Vector3.dot(toPixel, camera.getDirection()));
			successful = cameraTurn(turnDir, angle);
			successful = successful && cameraZoom(forward ? directionForward() : directionBack());
		}
		
		//Restore the camera's original position and direction if something went wrong.
		if(!successful)
		{
			camera.adjustCamera(pos, dir, up);
		}
		
		return successful;
	}
	
	//Returns a normalized vector pointing backward relative to the camera.
	public Vector3 directionBack()
	{
		return Vector3.subtract(origin, camera.getDirection());
	}
	
	//Returns a normalized vector pointing down relative to the camera.
	public Vector3 directionDown()
	{
		return Vector3.subtract(origin, camera.getUp());
	}

	//Returns a normalized vector pointing forward relative to the camera.
	public Vector3 directionForward()
	{
		return camera.getDirection();
	}

	//Returns a normalized vector pointing left relative to the camera.
	public Vector3 directionLeft()
	{
		return Vector3.cross(camera.getUp(), camera.getDirection());
	}

	//Returns a normalized vector pointing right relative to the camera.
	public Vector3 directionRight()
	{
		return Vector3.cross(camera.getDirection(), camera.getUp());
	}

	//Returns a normalized vector pointing up relative to the camera.
	public Vector3 directionUp()
	{
		return camera.getUp();
	}
	
	//Returns the camera's position in three-dimensional space.
	public Vector3 getCameraPosition()
	{
		return camera.getPosition();
	}
	
	//Returns the ambient light.
	public Light getAmbientLight()
	{
		return ambientLight;
	}
	
	//Returns a list of point lights.
	public List<Light> getLights()
	{
		return lights;
	}
	
	//Renders a fractal image and returns it.
	public BufferedImage render(int antialiasingFactor)
	{
		Thread[] threads = new Thread[Runtime.getRuntime().availableProcessors()];
		Worker worker;
		
		//Create one worker thread for each processor and start it.
		for(int i = 0; i < threads.length; i++)
		{
			worker = new Worker(camera, fractal, ambientLight, lights, antialiasingFactor, i,
				threads.length);
			threads[i] = new Thread(worker);
			
			threads[i].start();
		}
		
		//Wait for all of the threads to finish.
		for(int i = 0; i < threads.length; i++)
		{
			try
			{
    			threads[i].join();
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		
		return camera.getImage();
	}
	
	//Sets the camera and point lights according to the current fractal's default camera position.
	public void resetCamera()
	{
		//Create values for lights on each side of the camera with brightness proportional to their
		//distance from the origin.
		double lightOffsetAngle = Math.toRadians(60.0), s = Math.sin(lightOffsetAngle);
		Vector3 position = fractal.getDefaultCameraPosition(), up = new Vector3(0.0, 0.0, 1.0);
		Vector3 scaledPos = Vector3.scale(position, Math.cos(lightOffsetAngle));
		Vector3 lightPosL = Vector3.add(scaledPos, Vector3.scale(Vector3.cross(position, up), s));
		Vector3 lightPosR = Vector3.add(scaledPos, Vector3.scale(Vector3.cross(up, position), s));
		double brightness = 1.5 * Math.pow(1.25, position.magnitude());
		
		//Create a new camera at the default position.
		camera = new Camera(position, Vector3.subtract(origin, position), up, width, height);
		defaultAxisOfRotation = new Vector3(up);
		
		//Add the point lights.
		lights.clear();
		lights.add(new Light(lightPosL, 1.0, 1.0, 1.0, brightness));
		lights.add(new Light(lightPosR, 1.0, 1.0, 1.0, brightness));
	}
	
	//Sets the ambient light.
	public void setAmbientLight(Light ambientLight)
	{
		this.ambientLight = ambientLight;
	}
	
	//Sets camera/light values for the given fractal.
	public void setFractal(Fractal fractal)
	{
		this.fractal = fractal;
		
		resetCamera();
	}
}
