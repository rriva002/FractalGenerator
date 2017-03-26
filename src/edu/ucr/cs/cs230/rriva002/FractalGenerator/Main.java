package edu.ucr.cs.cs230.rriva002.FractalGenerator;

public class Main
{
	public static void main(String[] args)
	{
		//Create a GUI with the given dimensions and fractals.
		int width = 800, height = 600, iterations = 10;
		Fractal mandelbulb = new Mandelbulb(iterations, 1.25331, 8.0, 8.0, 8.0);
		Fractal mandelbox = new Mandelbox(iterations, 2.0, 0.5, 1.0);
		GUI gui = new GUI(width, height, new Fractal[]{mandelbulb, mandelbox});
		
		//Show the GUI.
		gui.show();
	}
}
