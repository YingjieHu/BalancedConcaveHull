# BalancedConcaveHull
Constructing concave hulls using point locations with balanced emptiness and complexity


* Author: Yingjie Hu
* Email: yjhu.geo@gmail.com


### Overall description 
Many GIS applications need constructing concave hulls from point locations. Depending on the different normalized length parameters (i.e., lambdas), different concave hulls can be constructed from the same group of points. This program aims at finding the best lambda value and constructing a concave hull with a balanced complexity and emptiness. This program can achieve the following effect:
![Balanced Concave Hull](http://stko-exp.geog.ucsb.edu/urbanAOIs/fig/lambdaPolygonSmall.jpg)

Meanwhile, the relation between the lambda and the balance value can be visualized as a figure (similar to the following) to help us acquire an intuitive understanding of the concave hulls:
![Balanced Concave Hull](http://stko-exp.geog.ucsb.edu/urbanAOIs/fig/lambdaCurveSmall.jpg)

The idea of achieving the balance between complexity and emptiness is from:
  Akdag, F., Eick, C. F., & Chen, G. (2014, June). Creating polygon models for spatial clusters. In   International Symposium on Methodologies for Intelligent Systems (pp. 493-499). Springer International   Publishing. 

For more details about the calculation of the complexity, please refer to:
  Brinkhoff, T., Kriegel, H. P., Schneider, R., & Braun, A. (1995, December). Measuring the Complexity of   Polygonal Objects. In ACM-GIS (p. 109).

A previous version of this program has also been used in the research project **Urban Areas of Interest (AOI)** which can be accessed at: http://stko-exp.geog.ucsb.edu/urbanAOIs/


### Repository organization
The "Source" folder contains the source java files, and the "Release" folder contains the compiled program which can be directly used. Two example data files have also been provided.

### How to run the compiled program?
Open a cmd line in the current folder, and execute: "java -jar BalancedConcave.jar". To  increase the allocated memory size, use "java -jar -Xmx2G BalancedConcave.jar". You will need Java 1.8 to run this program.

Input of the program: locations of the points in CSV format. The input data must contain three fields: "x", "y", and "label" (indicating which group this point belongs to; a clustering method, such as DBSCAN, can be firstly employed to find these labels; see my other Git repository). Sample input data can be found in the folder "Input".

The output of the program: the output is EsriJSON files in the output folder, and each input file will have a corresponding output file. These EsriJSON files can be converted into Shapefile using the tool "json to features" in ArcGIS Toolbox. A file called "lambdaTestResult.csv" will also be generated, which allows you to visualize the relations between lambda and balanced value.

Configuration file (config.json): This file is very important for running the program as it specifies the key parameters. Detailed explaination for each parameter can be found in the config.json file. You can change these parameters to fit your application. If you have questions with the parameters, please send me an email.



### Citation
If you use this program in your research, I would really appreciate if you could cite our following paper:

Hu, Y., Gao, S., Janowicz, K., Yu, B., Li, W., & Prasad, S. (2015). Extracting and understanding urban areas of interest using geotagged photos. Computers, Environment and Urban Systems, 54, 240-254.

Thank you!




