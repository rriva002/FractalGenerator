package edu.ucr.cs.cs230.rriva002.FractalGenerator;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class GUI implements KeyListener
{
	private final JFrame frame = new JFrame("Fractal Generator");
	private final Box controlBox = Box.createHorizontalBox();
	private final JComboBox<Fractal> fractalSelector = new JComboBox<Fractal>();
	private final JButton renderButton = new JButton("Render");
	private final JLabel imageLabel = new JLabel(), infoLabel = new JLabel(" ");
	private FractalRenderer fractalRenderer;
	private static final String labelPrefix = " ", labelSuffix = ": ", aaLabel = "Antialiasing";
	private static final int parameterBoxIndex = 2;
	private int width, height, antialiasingFactor = 1;
	private boolean controlsEnabled = false;
	
	//Constructor. Adds the given fractals to the combo box.
	public GUI(int width, int height, Fractal[] fractals)
	{
		this.width = width;
		this.height = height;
		fractalRenderer = new FractalRenderer(width, height, fractals[0]);
		
		for(Fractal fractal : fractals)
		{
			fractalSelector.addItem(fractal);
		}
	}
	
	//Creates a control box for the selected fractal's parameters and adds it to the GUI.
	private void addParameterBox()
	{
		Box parameterBox = Box.createHorizontalBox();
		final JTextField antialiasing = new JTextField(Integer.toString(antialiasingFactor));
		KeyListener keyListener = new KeyListener()
		{
			@Override
			public void keyPressed(KeyEvent keyEvent)
			{
				//Render a fractal when the enter key is pressed.
				switch(keyEvent.getKeyCode())
				{
					case KeyEvent.VK_ENTER:
						generateFractal();
						break;
					default:
						break;
				}
			}
			
			@Override
			public void keyReleased(KeyEvent keyEvent)
			{
				
			}
			
			@Override
			public void keyTyped(KeyEvent keyEvent)
			{
				
			}
		};
		
		antialiasing.addFocusListener(new FocusListener()
		{
			@Override
			public void focusGained(FocusEvent focusEvent)
			{
				//Change the text color to black and select the box's contents.
				antialiasing.setForeground(Color.BLACK);
				antialiasing.selectAll();
			}

			@Override
			public void focusLost(FocusEvent focusEvent)
			{
				
			}
		});
		
		//Add the antialiasing label and text box to the parameter box.
		antialiasing.addKeyListener(keyListener);
		parameterBox.add(new JLabel(labelPrefix + aaLabel + labelSuffix));
		parameterBox.add(antialiasing);
		
		//Add a label and text field for each parameter.
		for(String[] parameter : ((Fractal) fractalSelector.getSelectedItem()).getParameters())
		{
			final JTextField textField = new JTextField(parameter[1]);
			
			textField.addFocusListener(new FocusListener()
			{
				@Override
				public void focusGained(FocusEvent focusEvent)
				{
					textField.setForeground(Color.BLACK);
					textField.selectAll();
				}

				@Override
				public void focusLost(FocusEvent focusEvent)
				{
					
				}
			});
			
			textField.addKeyListener(keyListener);
			parameterBox.add(new JLabel(labelPrefix + parameter[0] + labelSuffix));
			parameterBox.add(textField);
		}
		
		//Add the parameter box to the main control box.
		controlBox.add(parameterBox, parameterBoxIndex);
		controlBox.validate();
	}
	
	//Shows the light editor.
	private void configureLights()
	{
		LightConfiguration lightConfiguration = new LightConfiguration(fractalRenderer);
		
		//Disable the main GUI while the light editor is open.
		frame.setEnabled(false);
		lightConfiguration.configure(frame);
	}
	
	//Renders a fractal image.
	private void generateFractal()
	{
		//Disable the control box while the fractal is rendering.
		setControlBoxEnabled(false);
		infoLabel.setText("Rendering...");
		
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				//Start the timer.
				long start = new Date().getTime();
				int labelEnd;
				double value, oldValue;
				boolean valid = true, parameterChanged = false;
				Fractal fractal = (Fractal) fractalSelector.getSelectedItem();
				List<String[]> parameters = fractal.getParameters();
				List<String> invalidParameters = new ArrayList<String>();
				Box parameterBox = (Box) controlBox.getComponent(parameterBoxIndex);
				JTextField textField;
				String parameter;
				
				//Verify each parameter in the parameter box.
				for(int i = 0; i < parameterBox.getComponentCount(); i += 2)
				{
					parameter = ((JLabel) parameterBox.getComponent(i)).getText();
					labelEnd = parameter.length() - labelSuffix.length();
					parameter = parameter.substring(labelPrefix.length(), labelEnd);
					textField = (JTextField) parameterBox.getComponent(i + 1);
					
					try
					{
						if(parameter.equals(aaLabel))
						{
							//Verify that the antialiasing factor is an integer of value 1 or more.
							antialiasingFactor = Integer.parseInt(textField.getText());
							
							if(antialiasingFactor < 1)
							{
								throw new Exception("Invalid value for " + parameter + ".");
							}
							
							continue;
						}
						else if(parameter.equals(Fractal.iterationsString))
						{
							//Verify that the number of iterations is an integer.
							value = Integer.parseInt(textField.getText());
						}
						else
						{
							//Verify that the parameter's value is a number and determine whether or
							//not it's been changed.
							oldValue = Double.parseDouble(parameters.get(i / 2 - 1)[1]);
							value = Double.parseDouble(textField.getText());
							parameterChanged = parameterChanged || oldValue != value;
						}
						
						//Verify that the parameter's value is valid.
						if(!fractal.setParameter(parameter, value))
						{
							throw new Exception("Invalid value for " + parameter + ".");
						}
					}
					catch(Exception exception)
					{
						valid = false;
						
						//Set the text color to red and keep track of the parameters with invalid
						//values.
						textField.setForeground(Color.RED);
						invalidParameters.add(parameter);
					}
				}
				
				//Render the fractal if all the parameter values were valid.
				if(valid)
				{
					//Reset the camera if any of the fractal-specific parameters were changed.
					if(parameterChanged)
					{
						fractalRenderer.resetCamera();
					}
					
					//Render the fractal and display the rendering time.
					//imageLabel.setIcon(new ImageIcon(antialias(fractalRenderer.render())));
					imageLabel.setIcon(new ImageIcon(fractalRenderer.render(antialiasingFactor)));
					
					double seconds = (double) (new Date().getTime() - start) / 1000.0;
					
					infoLabel.setText("Rendered in " + seconds + " seconds");
					
					controlsEnabled = true;
				}
				else
				{
					//Display a message telling which parameters had invalid values.
					String invalid = "", errorMessage = "Invalid value";
					
					if(invalidParameters.size() > 1)
					{
						for(int i = 0; i < invalidParameters.size() - 1; i++)
						{
							invalid += (invalid.isEmpty() ? "" : ", ") + invalidParameters.get(i);
						}
						
						invalid += " and ";
						errorMessage += "s";
					}
					
					invalid += invalidParameters.get(invalidParameters.size() - 1);
					errorMessage += " for " + invalid + ".";
					
					infoLabel.setText(errorMessage);
				}
				
				//Re-enable the control box.
				renderButton.requestFocusInWindow();
				setControlBoxEnabled(true);
			}
		});
	}

	@Override
	public void keyPressed(KeyEvent keyEvent)
	{
		//Enable the keyboard controls after a fractal has been rendered.
		if(controlsEnabled)
		{
			boolean successful = false;
			
			switch(keyEvent.getKeyCode())
			{
				case KeyEvent.VK_ENTER:
					//Render the fractal if the enter key is pressed.
					successful = true;
					break;
				case KeyEvent.VK_UP:
					//Turn the camera up if the up key is pressed.
					successful = fractalRenderer.cameraTurn(fractalRenderer.directionUp());
					break;
				case KeyEvent.VK_DOWN:
					//Turn the camera down if the down key is pressed.
					successful = fractalRenderer.cameraTurn(fractalRenderer.directionDown());
					break;
				case KeyEvent.VK_LEFT:
					//Turn the camera left if the left key is pressed.
					successful = fractalRenderer.cameraTurn(fractalRenderer.directionLeft());
					break;
				case KeyEvent.VK_RIGHT:
					//Turn the camera right if the right key is pressed.
					successful = fractalRenderer.cameraTurn(fractalRenderer.directionRight());
					break;
				case KeyEvent.VK_W:
					//Move the camera forward if the W key is pressed.
					successful = fractalRenderer.cameraZoom(fractalRenderer.directionForward());
					break;
				case KeyEvent.VK_S:
					//Move the camera backward if the S key is pressed.
					successful = fractalRenderer.cameraZoom(fractalRenderer.directionBack());
					break;
				case KeyEvent.VK_A:
					//Move the camera left if the A key is pressed.
					successful = fractalRenderer.cameraPan(fractalRenderer.directionLeft());
					break;
				case KeyEvent.VK_D:
					//Move the camera right if the D key is pressed.
					successful = fractalRenderer.cameraPan(fractalRenderer.directionRight());
					break;
				case KeyEvent.VK_E:
					//Move the camera up if the E key is pressed.
					successful = fractalRenderer.cameraPan(fractalRenderer.directionUp());
					break;
				case KeyEvent.VK_C:
					//Move the camera down if the C key is pressed.
					successful = fractalRenderer.cameraPan(fractalRenderer.directionDown());
					break;
				default:
					break;
			}
			
			//Render the fractal.
			if(successful)
			{
				generateFractal();
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent keyEvent)
	{
		
	}

	@Override
	public void keyTyped(KeyEvent keyEvent)
	{
		
	}
	
	//Enables or disables the control box's components.
	private void setControlBoxEnabled(boolean enabled)
	{
		//Enable or disable the fractal selector combo box.
		fractalSelector.setEnabled(enabled);
		
		Box parameterBox = (Box) controlBox.getComponent(parameterBoxIndex);
		
		//Enable or disable each text box in the parameter box.
		for(int i = 1; i < parameterBox.getComponentCount(); i += 2)
		{
			parameterBox.getComponent(i).setEnabled(enabled);
		}
		
		//Enable or disable the buttons.
		controlBox.getComponent(controlBox.getComponentCount() - 2).setEnabled(enabled);
		renderButton.setEnabled(enabled);
	}
	
	//Creates the GUI and displays it.
	public void show()
	{
		Box box = Box.createVerticalBox();
		JButton lightButton = new JButton("Lights");
		BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		
		fractalSelector.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent actionEvent)
			{
				if(controlBox.getComponentCount() > parameterBoxIndex + 1)
				{
					controlBox.remove(parameterBoxIndex);
				}
				
				//Add the appropriate parameter box and sets the fractal according to the combo box.
				addParameterBox();
				fractalRenderer.setFractal((Fractal) fractalSelector.getSelectedItem());
			}
		});
		
		renderButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent actionEvent)
			{
				//Render the fractal when the Render button is clicked.
				generateFractal();
			}
		});
		
		lightButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent actionEvent)
			{
				//Show the light editor when the Lights button is clicked.
				configureLights();
			}
		});
		
		imageLabel.addMouseListener(new MouseListener()
		{
			@Override
			public void mouseClicked(MouseEvent mouseEvent)
			{
				//Enable the mouse controls after a fractal has been rendered.
				if(controlsEnabled)
				{
					//The y value is reversed since y = 0 is at the top of the image.
					int x = mouseEvent.getX();
					int y = (height - mouseEvent.getY() - 1);
					boolean successful = false;
					
					switch(mouseEvent.getButton())
					{
						case MouseEvent.BUTTON1:
							//Turn to the clicked pixel and zoom in on a left click.
							successful = fractalRenderer.cameraZoom(x, y, true);
							break;
						case MouseEvent.BUTTON3:
							//Turn to the clicked pixel and zoom out on a right click.
							successful = fractalRenderer.cameraZoom(x, y, false);
							break;
						default:
							break;
					}
					
					//Render the fractal.
					if(successful)
					{
						generateFractal();
					}
				}
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				
			}

			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				
			}

			@Override
			public void mouseReleased(MouseEvent mouseEvent)
			{
				
			}
		});
		
		//Add all of the components to the frame and show it.
		controlBox.add(new JLabel(" Fractal Type: "));
		controlBox.add(fractalSelector);
		fractalSelector.setSelectedItem(fractalSelector.getItemAt(0));
		lightButton.addKeyListener(this);
		controlBox.add(lightButton);
		renderButton.addKeyListener(this);
		controlBox.add(renderButton);
		box.add(controlBox);
		imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		imageLabel.setIcon(new ImageIcon(bufferedImage));
		imageLabel.addKeyListener(this);
		box.add(imageLabel);
		infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		box.add(infoLabel);
		frame.add(box);
		frame.addKeyListener(this);
		frame.pack();
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
}
