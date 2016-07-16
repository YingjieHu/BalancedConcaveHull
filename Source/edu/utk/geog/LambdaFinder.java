package edu.utk.geog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.json.JSONObject;
import org.opensphere.geometry.algorithm.ConcaveHull;

import com.vividsolutions.jts.algorithm.Angle;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.triangulate.DelaunayTriangulationBuilder;

import edu.princeton.cs.algs4.StdOut;

public class LambdaFinder 
{
	public double findTheBestLambda(JSONObject parameterObject)
	{
		double bestLambda = 0;
		double lowestFitnessValue = 100;
		try
		{
			        StdOut.println("Begin to find the best lambda value...");
				File outputFile = new File(parameterObject.getString("outputPath")+"/lambdaTestResult.csv");
				if(outputFile.exists())
				{
						outputFile.delete();
						outputFile.createNewFile();
				}
				FileWriter outputFileWriter = new FileWriter(outputFile, true);
				String newLineSymbol = System.getProperty("line.separator");
				
				String inputFolderName = parameterObject.getString("inputPath");
				
				double lambdaValue = 1;
				for(lambdaValue = 1; lambdaValue <=100;lambdaValue += 1)
				{
						double avgFitness = calculateAvergeFitnessOfAll(lambdaValue, inputFolderName, parameterObject);
						System.out.println("when lambda is "+lambdaValue+", fitness is "+avgFitness);
						
						outputFileWriter.append(lambdaValue+","+avgFitness+newLineSymbol);
						
						if(avgFitness < lowestFitnessValue) 
						{
							lowestFitnessValue = avgFitness;
							bestLambda = lambdaValue;
						}
				}
				outputFileWriter.close();
				
				StdOut.println("The best lambda is "+bestLambda);
		} 
		catch (Exception e)
		{
				e.printStackTrace();
		}	
		
		return bestLambda;
	}
	
	public static double calculateAvergeFitnessOfAll(double lambdaValue, String inputFolderName, JSONObject parameterObject)
	{
			try
			{
					File clusterFolder = new File(inputFolderName);
					File[] clusterFiles = clusterFolder.listFiles();
					
					int totalPolygonCount = 0;
					double avgFitness = 0;
					
					for(int i=0;i<clusterFiles.length;i++)
					{		
							// read the cluster files one by one
							File thisClusterFile = clusterFiles[i];
							FileReader thisClusterFileReader = new FileReader(thisClusterFile);
							BufferedReader thisClusterBufferedReader = new BufferedReader(thisClusterFileReader);
							
							String inputLine = null;
							Hashtable<String, Vector<Coordinate>> clusterHashtable = new Hashtable<>();
							while((inputLine = thisClusterBufferedReader.readLine())!= null)
							{
									String[] thisRecordInfo = inputLine.split(",");
									double longitude = Double.parseDouble(thisRecordInfo[parameterObject.getInt("xIndex")]);
									double latitude = Double.parseDouble(thisRecordInfo[parameterObject.getInt("yIndex")]);
									String clusterName = thisRecordInfo[parameterObject.getInt("labelIndex")];
									
									Vector<Coordinate> thisClusterVector = clusterHashtable.get(clusterName);
									if(thisClusterVector ==  null)
									{
											thisClusterVector = new Vector<>();
									}
									thisClusterVector.add(new Coordinate(longitude, latitude));
									clusterHashtable.put(clusterName, thisClusterVector);		
							}
							thisClusterBufferedReader.close();
							
							
							// now all cluster points have been stored in the hashtable
							Enumeration<String> clusterNameEnumeration = clusterHashtable.keys();
							while(clusterNameEnumeration.hasMoreElements())
							{
									String thisClusterName = clusterNameEnumeration.nextElement();
									Vector<Coordinate> clusterCoordinatesVector = clusterHashtable.get(thisClusterName);
									
									if(clusterCoordinatesVector.size()<3) continue;
									
									double thisFitnessValue = calculatePolygonFitness(clusterCoordinatesVector, lambdaValue);
									
									/*if(totalPolygonCount == 200)
									{
										StdOut.println("test");
									}*/
									
									if(thisFitnessValue != Double.NEGATIVE_INFINITY && !Double.isNaN(thisFitnessValue))
									{
										avgFitness += thisFitnessValue;
										totalPolygonCount++;
										//StdOut.println(avgFitness+","+totalPolygonCount);
									}
									
							}
							
					}
					
					avgFitness = avgFitness / totalPolygonCount;
					
					return avgFitness;
			} 
			catch (Exception e)
			{
					e.printStackTrace();
			}
			
			return -1;
	}
	
	
	public static double calculatePolygonFitness(Vector<Coordinate> pointVector, double lambdaValue)
	{
			try
			{
					// first construct delaunay triangles and convex hull
					
					DelaunayTriangulationBuilder cdt = new DelaunayTriangulationBuilder();
					cdt.setSites(pointVector);
					cdt.setTolerance(0.00001);
			        
			        //QuadEdgeSubdivision triangleDivision = cdt.getSubdivision();
			        GeometryFactory gf = new GeometryFactory();
			        //GeometryCollection triangleCollection = ( GeometryCollection)triangleDivision.getTriangles(gf);
					GeometryCollection triangleCollection = ( GeometryCollection)cdt.getTriangles(gf);
					Geometry convexHull = triangleCollection.convexHull();
			        
			        // calculate the longest edge of the convex hull
			        Coordinate[] convexCoordinatesArray = convexHull.getCoordinates();
			        double longestEdgeOfConvexhull = -1.0; 
			        for(int i=0;i<(convexCoordinatesArray.length-1);i++)
			        {
			        		Coordinate coord1 = convexCoordinatesArray[i];
			        		Coordinate coord2 = convexCoordinatesArray[i+1];
			        		
			        		double distance = Math.sqrt((coord1.x - coord2.x)*(coord1.x - coord2.x) + (coord1.y - coord2.y)*(coord1.y - coord2.y));
			        		if(distance > longestEdgeOfConvexhull)
			        				longestEdgeOfConvexhull = distance;
			        }
			        // finish the longest edge
			        
			        
			        // now begin to calculate the new polygon based on the threshold
			        double thresholdValue = (lambdaValue*1.0)/(100*1.0)*longestEdgeOfConvexhull;
			        GeometryFactory gf2 = new GeometryFactory();
			        		
					 Point[] pointArray = new Point[pointVector.size()];
					 for(int i=0;i<pointVector.size();i++)
					 {
						 pointArray[i] = gf.createPoint(pointVector.get(i));
					 }
					
					 // try to generate a concave hull
					 Geometry concaveHullResultGeometry = null; 
					 try
					{
						 ConcaveHull concaveHull = new ConcaveHull(gf2.createGeometryCollection(pointArray),thresholdValue);
						 concaveHullResultGeometry = concaveHull.getConcaveHull();
					} 
					 catch (Exception e)
					{
						// StdOut.println("Dropped");
						// in cases that concave hull cannot be generated, we drop this special care
					}
					 
					 if(concaveHullResultGeometry == null) return Double.NEGATIVE_INFINITY;
					
					 
			        // new polygon has finished
					 
					 
					 // begin to calculate the empitiness and complexity
					 // 1. emptiness
					 int triangleTotalNumber = triangleCollection.getNumGeometries();
					 double avgTriangleArea = 0;
					 for(int i=0;i<triangleTotalNumber;i++)
					 {
							 Geometry thisTriangle = triangleCollection.getGeometryN(i);
							 avgTriangleArea += thisTriangle.getArea();
					 }
					 avgTriangleArea = avgTriangleArea / triangleTotalNumber;
					
					 double thetaValue = avgTriangleArea * 1.25;
					 double emptiness = 0;
					 for(int i=0;i<triangleTotalNumber;i++)
					 {
							 Geometry thisTriangle = triangleCollection.getGeometryN(i);
							 double thisAreaValue= thisTriangle.getArea();
							 if((thisAreaValue>thetaValue) && (concaveHullResultGeometry.covers(thisTriangle)))
							 {
									 emptiness += (thisAreaValue - thetaValue);
							 }
					 }
					 emptiness  = emptiness / (convexHull.getArea());
					 // emptiness finished
					 
					 //2. complexity
					    //calculate frequency
					 Coordinate[] concaveHullCoords = concaveHullResultGeometry.getCoordinates();
					 int concaveNotchNum = 0;
					 int concaveVerticeNum = concaveHullResultGeometry.getNumPoints();
					 for(int i=0;i<(concaveHullCoords.length-2);i++)
					 {
							 double interiorAngleValue = Angle.interiorAngle(concaveHullCoords[i], concaveHullCoords[i+1], concaveHullCoords[i+2]);
							 if(interiorAngleValue>Math.PI)
									 concaveNotchNum++;
					 }
					 double normalizedNotch = (concaveNotchNum*1.0)/(concaveVerticeNum*1.0-3);
					 double polygonFrequency = 16* Math.pow((normalizedNotch-0.5), 4) -  8* Math.pow((normalizedNotch-0.5), 2) +1;
					   // frequency finish
					 
					   // amplitude
					 double polygonAmplitude = (concaveHullResultGeometry.getLength() - convexHull.getLength()) / convexHull.getLength();
					 
					 //deviation from convexhull
					 double polygonDeviation = (convexHull.getArea() - concaveHullResultGeometry.getArea()) / convexHull.getArea();
					 
					 double complexityValue = 0.8 * polygonAmplitude * polygonFrequency + 0.2*polygonDeviation;
					 // complexity finished
					 
					 double fitness = emptiness + 0.45*complexityValue;  
					 
					 return fitness;
					
			} 
			catch (Exception e)
			{
					e.printStackTrace();
			}
			
			return -1;
	}
	
}
