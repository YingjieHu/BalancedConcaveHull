package edu.utk.geog;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.ml.clustering.DoublePoint;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opensphere.geometry.algorithm.ConcaveHull;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import edu.princeton.cs.algs4.In;

public class ShapeGenerator
{
		
		public JSONObject generateConcaveShape(String inputCSVFileString, double lambda, JSONObject parameterObject)
		{
				
				System.out.println("Constructing concave hulls for file "+inputCSVFileString+"...");
				
				try
				{
						In inputCSVStream = new In(inputCSVFileString);					
						
						int lngIndex = parameterObject.getInt("xIndex");
						int latIndex = parameterObject.getInt("yIndex");
						int clusterIndex = parameterObject.getInt("labelIndex");
						
						List<DoublePoint> dataPointList = null;
						String lastClusterID = null;
						String inputLine = null;			
						
						//JSONObject resultObject = new JSONObject();
						JSONArray featuresArray = new JSONArray();				
						
						while((inputLine = inputCSVStream.readLine()) != null)
						{
								String[] info = inputLine.split(",");
								
								String thisClusterID = info[clusterIndex];
								if(!thisClusterID.equals(lastClusterID))
								{
										if(dataPointList != null && dataPointList.size()>=3)
										{
												JSONObject featureObject = createConcaveHull(dataPointList, lastClusterID, lambda);
												featuresArray.put(featureObject);
										}
										
										dataPointList = new ArrayList<DoublePoint>(100); 
										lastClusterID = thisClusterID;
										System.out.println("Begin to process "+lastClusterID+"...");
										
								}
							    
								double[] thisCoordDouble = new double[2];
    							thisCoordDouble[0] = Double.parseDouble(info[lngIndex]);
    							thisCoordDouble[1] = Double.parseDouble(info[latIndex]);
    							
    							dataPointList.add(new DoublePoint(thisCoordDouble));
    						
						}
						inputCSVStream.close();
						
						// add the last object
						if(dataPointList != null && dataPointList.size()>=3)
						{
								JSONObject featureObject = createConcaveHull(dataPointList, lastClusterID, lambda);
								featuresArray.put(featureObject);
						}
						
						JSONObject resultObject = new JSONObject("{\"features\":"+ featuresArray.toString()+"}");
						
						//System.out.println(resultObject.toString());
						System.out.println("Concave hull construction have finished...");
						System.out.println("---------------------------------------------------");
						return resultObject;
						
						
				} 
				catch (Exception e)
				{
						e.printStackTrace();
				}
				
				return null;
		}
		
		
	
		
		
		JSONObject createConcaveHull(List<DoublePoint> pointsInCluster, String clusterId, double lambda)
		{
    			try 
    			{		
    				GeometryFactory gf = new GeometryFactory();
    				int numberOfPointsInCluster = pointsInCluster.size();
    				
    				 Coordinate[] vertices = new Coordinate[numberOfPointsInCluster];
    				 Point[] pointArray = new Point[numberOfPointsInCluster];
    				 
    				 for(int i=0;i<numberOfPointsInCluster;i++)
    				 {
    					 double[] thisCoords = pointsInCluster.get(i).getPoint();
    					 vertices[i] = new Coordinate(thisCoords[0],thisCoords[1]);
    					 pointArray[i] = gf.createPoint(vertices[i]);
    				 }
    				 
    				GeometryCollection allPointCollection = gf.createGeometryCollection(pointArray); 
    				Geometry pointConvexHull = allPointCollection.convexHull();
    				
    				// calculate the longest edge of the convex hull
    		        Coordinate[] convexCoordinatesArray = pointConvexHull.getCoordinates();
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
    				
    				 double edgeThreshold =  longestEdgeOfConvexhull * 0.01 * lambda;
    				 
    				 ConcaveHull concaveHull = new ConcaveHull(allPointCollection,edgeThreshold);
    				 Geometry concaveHullResultGeometry = concaveHull.getConcaveHull();
    				 
    				 JSONObject thisFeatureObject = new JSONObject();
    				 thisFeatureObject.put("attributes", new JSONObject("{\"Cluster\" : \""+clusterId+"\", \"PointCount\": \""+pointsInCluster.size()+"\"}")); 
    				 
    				 JSONObject geometryObject = new JSONObject();
    				 JSONArray ringsJsonArray = new JSONArray();
    				 JSONArray coordsArray = new JSONArray();
    
    				 Coordinate[] concaveCoords = concaveHullResultGeometry.getCoordinates();
    				 
    				 for(int j=0;j<concaveCoords.length;j++)
    				 {
    					 JSONArray thisCoordArray = new JSONArray();
    					 thisCoordArray.put(concaveCoords[j].x);
    					 thisCoordArray.put(concaveCoords[j].y);
    					 coordsArray.put(thisCoordArray);
    				 }   				
    				 ringsJsonArray.put(coordsArray);
    				 geometryObject.put("rings", ringsJsonArray);
    				 thisFeatureObject.put("geometry", geometryObject);
    				 
    				 return thisFeatureObject;
    
    			} 
    			catch (Exception e) 
    			{
    				e.printStackTrace();
    			}
    			return null;
		}

}
