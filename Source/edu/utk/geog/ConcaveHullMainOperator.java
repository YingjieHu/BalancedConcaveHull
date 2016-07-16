package edu.utk.geog;

import java.io.File;
import java.io.FileWriter;

import org.json.JSONObject;

import edu.princeton.cs.algs4.StdOut;



public class ConcaveHullMainOperator
{

		public static void main(String[] args)
		{
				try
				{
						// Read parameters from the configuration file into memory
						ParameterReader parameterReader = new ParameterReader();
						JSONObject parameterObject = parameterReader.readConfigParameters();
						if(parameterObject ==  null) return;   
						
						double lambda = -1;
						if(parameterObject.getBoolean("automated"))
						{
							LambdaFinder lambdaFinder = new LambdaFinder();
							lambda = lambdaFinder.findTheBestLambda(parameterObject);
						}
						else
						{
							lambda = parameterObject.getDouble("lambda");
							StdOut.println("Using the manually specified lambda value: "+lambda+" ... ");
						}
						
						String inputFolderString = parameterObject.getString("inputPath");
						File inputFolder = new File(inputFolderString);
						String[] inputFileStrings = inputFolder.list();
						
						String outputFolderString = parameterObject.getString("outputPath");
						for(int i=0;i<inputFileStrings.length;i++)
						{
								// initialize Esri JSON object
								EsriJSONInitialiser esriJSONInitialiser = new EsriJSONInitialiser();
								JSONObject esriJsonObject = esriJSONInitialiser.initializeEsriJsonObject(parameterObject);
								if(esriJsonObject == null) return;
								
								// clustering and construct shapes
								ShapeGenerator shapeGenerator = new ShapeGenerator();
								JSONObject resultObject = shapeGenerator.generateConcaveShape(inputFolderString+"/"+inputFileStrings[i], lambda, parameterObject);
								if(resultObject ==  null) return;
								
								
								// write the clustering result into output file
								esriJsonObject.put("features", resultObject.getJSONArray("features"));	
								String outputFileName = outputFolderString+"/"+inputFileStrings[i].replace("csv", "json");
								File outputFile = new File(outputFileName);
								if(outputFile.exists())
								{
									outputFile.delete();
									outputFile.createNewFile();
								}
								FileWriter outputFileWriter = new FileWriter(outputFile);
								outputFileWriter.write(esriJsonObject.toString());
								outputFileWriter.close();
										
								System.out.println("File "+inputFileStrings[i]+" has been completed.");
						}
						
						System.out.println("All files has been successfully completed.");

				} 
				catch (Exception e)
				{
						// TODO: handle exception
				}
				

		}

}
