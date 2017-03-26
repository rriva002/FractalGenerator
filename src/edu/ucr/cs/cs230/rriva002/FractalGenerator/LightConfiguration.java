package edu.ucr.cs.cs230.rriva002.FractalGenerator;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class LightConfiguration
{
	private FractalRenderer fractalRenderer;
	private static final String ambientLight = "< Ambient Light";
	private static final int rowsBeforeLights = 2, rowsAfterLights = 1, columns = Light.Parameter.values().length + 1;
	
	//Constructor. Stores the fractal renderer.
	public LightConfiguration(FractalRenderer fractalRenderer)
	{
		this.fractalRenderer = fractalRenderer;
	}
	
	//Adds a row for the ambient light to the panel.
	private void addAmbientLightRow(final JPanel panel)
	{
		Light.Parameter parameter;
		JLabel label = new JLabel(ambientLight);
		
		//Add each of the ambient light's color and brightness parameters to the row.
		for(int i = 0; i < Light.Parameter.values().length; i++)
		{
			parameter = Light.Parameter.values()[i];
			
			if(i < 3)
			{
				panel.add(new JLabel(" "));
			}
			else
			{
				panel.add(createTextField(parameter, fractalRenderer.getAmbientLight().getParameters().get(parameter)));
			}
		}
		
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setName(ambientLight);
		panel.add(label);
	}
	
	//Adds the column labels to the panel.
	private void addFirstRow(JPanel panel)
	{
		JLabel label;
		
		//Add each label to the row.
		for(String header : new String[]{"X", "Y", "Z", "Red", "Green", "Blue", "Brightness", " "})
		{
			label = new JLabel(header);
			label.setHorizontalAlignment(SwingConstants.CENTER);
			
			//Add tooltips to each color and brightness label.
			if(header.equals("Red") || header.equals("Green") || header.equals("Blue"))
			{
				label.setToolTipText("Must be in the range [0, 1].");
			}
			else if(header.equals("Brightness"))
			{
				label.setToolTipText("Must be zero or greater.");
			}
			
			panel.add(label);
		}
	}
	
	//Adds a row to add a new point light to the panel.
	private void addLastRow(final JPanel panel)
	{
		final JButton button = new JButton("Add");
		
		//Create a text field for each light parameter.
		for(Light.Parameter parameter : Light.Parameter.values())
		{
			panel.add(createTextField(parameter, 0));
			((JTextField) panel.getComponent(panel.getComponentCount() - 1)).setText("");
		}
		
		button.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent actionEvent)
			{
				Map<Light.Parameter, Double> parameters = new HashMap<Light.Parameter, Double>(Light.Parameter.values().length);
				JTextField textField;
				
				//If the add button is clicked, check each value.
				for(int i = 0; i < Light.Parameter.values().length; i++)
				{
					textField = (JTextField) panel.getComponent(panel.getComponentCount() - columns + i);
					
					//Return if a value was invalid.
					if(!checkField(Light.Parameter.values()[i], textField))
					{
						return;
					}
					
					//Store the value if the parameter's value is valid.
					parameters.put(Light.Parameter.values()[i], Double.parseDouble(textField.getText()));
				}
				
				//Clear the row.
				for(int i = 0; i < Light.Parameter.values().length; i++)
				{
					((JTextField) panel.getComponent(panel.getComponentCount() - rowsAfterLights * columns + i)).setText("");
				}
				
				//Add a row containing the new light's values to the panel.
				addRow(panel, parameters, panel.getComponentCount() / columns - rowsBeforeLights - 1);
				panel.validate();
			}
		});
		
		panel.add(button);
	}
	
	//Adds a row for an existing point light. 
	private void addRow(final JPanel panel, Map<Light.Parameter, Double> parameters, int id)
	{
		final JButton button = new JButton("Delete");
		Light.Parameter parameter;
		
		//Add a text field for each parameter containing its corresponding value.
		for(int i = 0; i < Light.Parameter.values().length; i++)
		{
			parameter = Light.Parameter.values()[i];
			
			panel.add(createTextField(parameter, parameters.get(parameter)), (id + rowsBeforeLights) * columns + i);
		}
		
		button.setName(Integer.toString(id));
		button.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent actionEvent)
			{
				int index = Integer.parseInt(button.getName()) + rowsBeforeLights;
				
				//If the delete button was clicked, remove the row.
				for(int i = 0; i < columns; i++)
				{
					panel.remove(index * columns);
				}
				
				panel.validate();
				
				//Adjust the index for each light's row after the deleted one.
				for(int i = (index + 1) * columns - 1; i < panel.getComponentCount() - columns * rowsAfterLights; i += columns)
				{
					panel.getComponent(i).setName(Integer.toString(index - rowsBeforeLights));
					index++;
				}
			}
		});
		
		panel.add(button, (id + rowsBeforeLights + 1) * columns - 1);
	}
	
	//Validates the given text field according to the given parameter.
	private boolean checkField(Light.Parameter parameter, JTextField textField)
	{
		try
		{
			double value = Double.parseDouble(textField.getText());
			
			switch(parameter)
			{
				//Colors must be between 0 and 1, and the brightness must be 0 or more.
				case RED:
					if(value < 0.0 || value > 1.0)
					{
						throw new Exception("Colors must be in the range [0, 1].");
					}
					break;
				case GREEN:
					if(value < 0.0 || value > 1.0)
					{
						throw new Exception("Colors must be in the range [0, 1].");
					}
					break;
				case BLUE:
					if(value < 0.0 || value > 1.0)
					{
						throw new Exception("Colors must be in the range [0, 1].");
					}
					break;
				case BRIGHTNESS:
					if(value < 0.0)
					{
						throw new Exception("Brightness must be 0 or more.");
					}
					break;
				default:
					break;
			}
		}
		catch(Exception e)
		{
			//If invalid, set the text field's text to red.
			textField.setForeground(Color.RED);
			return false;
		}
		
		return true;
	}
	
	//Closes the light editor window and re-enables the main GUI.
	private void closeWindow(JFrame frame, JFrame mainWindow)
	{
		frame.dispose();
		mainWindow.setEnabled(true);
    	mainWindow.requestFocus();
	}
	
	//Creates the light editor window and displays it.
	public void configure(final JFrame mainWindow)
	{
		final JFrame frame = new JFrame("Lights");
		final JPanel panel = new JPanel();
		Box box = Box.createVerticalBox(), bottomBox = Box.createHorizontalBox();
		JLabel cameraLabel = new JLabel("Camera position: " + fractalRenderer.getCameraPosition());
		JButton accept = new JButton("Accept"), cancel = new JButton("Cancel");
		
		//Add the first row and ambient light row.
		panel.setLayout(new GridLayout(0, columns));
		addFirstRow(panel);
		addAmbientLightRow(panel);
		
		//Add a row for each point light.
		for(int i = 0; i < fractalRenderer.getLights().size(); i++)
		{
			addRow(panel, fractalRenderer.getLights().get(i).getParameters(), i);
		}
		
		//Add the row for a new light.
		addLastRow(panel);
		
		accept.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent actionEvent)
			{
				if(verifyInput(panel))
				{
					//If the Accept button is clicked, update the lights and close the window.
					updateLights(panel);
					closeWindow(frame, mainWindow);
				}
			}
		});
		
		cancel.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent actionEvent)
			{
				//If the Cancel button is clicked, close the window.
				closeWindow(frame, mainWindow);
			}
		});
		
		frame.addWindowListener(new WindowAdapter()
		{
			@Override
            public void windowClosing(WindowEvent e)
            {
				//If the X is clicked, close the window.
				closeWindow(frame, mainWindow);
            }
		});
		
		//Add the light editor's components and show it.
		bottomBox.add(accept);
		bottomBox.add(cancel);
		box.add(panel);
		cameraLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		box.add(cameraLabel);
		box.add(bottomBox);
		frame.add(box);
		frame.pack();
		frame.setResizable(false);
        frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
	
	//Returns a text field with the given value for the specified parameter.
	private JTextField createTextField(final Light.Parameter parameter, double value)
	{
		final JTextField textField = new JTextField();
		
		textField.setName(parameter.toString());
		textField.setText(Double.toString(value));
		textField.addFocusListener(new FocusListener()
		{
			@Override
			public void focusGained(FocusEvent focusEvent)
			{
				//Set the text color to black when the text box gains focus.
				textField.setForeground(Color.BLACK);
			}

			@Override
			public void focusLost(FocusEvent focusEvent)
			{
				//Validate the text field when it loses focus.
				checkField(parameter, textField);
			}
		});
		
		return textField;
	}
	
	//Updates the lights according to the values in the light editor.
	private void updateLights(JPanel panel)
	{
		Map<Light.Parameter, Double> parameters;
		int offset;
		
		//Delete the existing point lights.
		fractalRenderer.getLights().clear();
		
		//Create a light according to each of the point light rows listed in the editor.
		for(int i = (rowsBeforeLights - 1) * columns; i < panel.getComponentCount() - rowsAfterLights * columns; i++)
		{
			parameters = new HashMap<Light.Parameter, Double>(Light.Parameter.values().length);
			offset = panel.getComponent(i + columns - 1).getName().equals(ambientLight) ? 3 : 0;
			i += offset;
			
			for(int j = offset; j < columns - 1; j++)
			{
				double value = Double.parseDouble(((JTextField) panel.getComponent(i)).getText());
				
				parameters.put(Light.Parameter.values()[j], value);
				i++;
			}
			
			//Set the ambient light values.
			if(panel.getComponent(i).getName().equals(ambientLight))
			{
				parameters.put(Light.Parameter.X, 0.0);
				parameters.put(Light.Parameter.Y, 0.0);
				parameters.put(Light.Parameter.Z, 0.0);
				fractalRenderer.setAmbientLight(new Light(parameters));
			}
			else
			{
				fractalRenderer.getLights().add(new Light(parameters));
			}
		}
	}
	
	//Returns true if the values in the light editor are valid.
	public boolean verifyInput(JPanel panel)
	{
		int offset;
		
		for(int i = (rowsBeforeLights - 1) * columns; i < panel.getComponentCount() - rowsAfterLights * columns; i++)
		{
			offset = panel.getComponent(i + columns - 1).getName().equals(ambientLight) ? 3 : 0;
			i += offset;
			
			for(int j = offset; j < columns - 1; j++)
			{
				if(((JTextField) panel.getComponent(i)).getForeground().equals(Color.RED))
				{
					return false;
				}
				
				i++;
			}
		}
		
		return true;
	}
}
