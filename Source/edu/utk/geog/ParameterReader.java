package edu.utk.geog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.json.JSONObject;

import edu.princeton.cs.algs4.StdOut;

class ParameterReader
{
	  	public JSONObject readConfigParameters()
	  	{
	  			System.out.println("Loading the configuration parameters...");
	  			
	  			try
				{
						File configFile = new File("config.json");
						FileReader configFileReader = new FileReader(configFile);
						BufferedReader configBufferedReader = new BufferedReader(configFileReader);
						
						StringBuffer configContent = new StringBuffer();
						String thisInputLine = null;
						while((thisInputLine = configBufferedReader.readLine()) != null)
						{
								int hashIndex = thisInputLine.indexOf("#");
								if(hashIndex != -1)
								{
										thisInputLine = thisInputLine.substring(0, hashIndex);
								}
								configContent.append(thisInputLine);
						}
						configBufferedReader.close();
						
						StdOut.println(configContent.toString());
						JSONObject parameterObject = new JSONObject(configContent.toString());
						
						// examine if the config file is valid
						if(parameterObject.isNull("inputPath") || (parameterObject.getString("inputPath").length() == 0))
						{
								System.out.println("Empty path for the input directory; please check the configuration file.");
								return null;
						}
						
						if(parameterObject.isNull("outputPath") || (parameterObject.getString("outputPath").length() == 0))
						{
								System.out.println("Empty path for the output directory; please check the configuration file.");
								return null;
						}
						
						
						if(parameterObject.isNull("xIndex") || (parameterObject.getInt("xIndex")==-1))
						{
								System.out.println("The index for longitude is not available; please check the configuration file.");
								return null;
						}
						
						if(parameterObject.isNull("yIndex") || (parameterObject.getInt("yIndex")==-1))
						{
								System.out.println("The index for latitude is not available; please check the configuration file.");
								return null;
						}
						
						if(parameterObject.isNull("labelIndex") || (parameterObject.getInt("labelIndex")==-1))
						{
								System.out.println("The index for data label is not available; please check the configuration file.");
								return null;
						}
						
						
						if((!parameterObject.getBoolean("automated")) && (parameterObject.isNull("lambda") || (parameterObject.getDouble("lambda")<1) || (parameterObject.getDouble("lambda")>100)))
						{
								System.out.println("The lambda value for the concave hull should be between 1 and 100; please check the configuration file.");
								return null;
						}
						
						if(parameterObject.isNull("spatialReference"))
						{
								System.out.println("The spatial reference of the output is missing; please check the configuration file.");
								return null;
						}
						
						System.out.println(parameterObject.toString());
						System.out.println("Configuration parameters have been successfully loaded...");
						System.out.println("---------------------------------------------------");
						
						return parameterObject;
						
						
				} 
	  			catch (Exception e)
				{
	  					e.printStackTrace();
						System.out.println("An issue has happened with the configuration file config.json; Please double check this file to ensure it is correct.");
						return null;
				}

	  	}
		

}
