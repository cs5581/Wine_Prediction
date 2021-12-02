# Wine_Prediction
Performing Training and Prediction on Wine Quality DataSets

Creating EMR Cluster for Training


In order to begin training the data set, we must first begin to build our EMR clusters. We set the condition to have 4 instances(1 master and 3 slaves). The configuration when building the cluster is as follows: software config -> emr - 5.30.1, Spark 2.4.7 on Hadoop 2.10.1; hardware config -> instance type: m5.xlarge, 4 instances(1 master, 3 slaves).

Once the cluster is built and ready to connect we run: 
- Sudo yum update
command to update the cluster to make sure all software versions are up to date. 

- Aws configure 
- This command will allow you to enter the aws secret access key id and the aws access key id from vocareum

Before the java classes are implemented and run, the maven project build has to be created. It can be created by using the following commands 

- sudo wget https://repos.fedorapeople.org/repos/dchen/apache-maven/epel-apache-maven.repo -O /etc/yum.repos.d/epel-apache-maven.repo

- sudo sed -i s/$releasever/6/g /etc/yum.repos.d/epel-apache-maven.repo

- sudo yum install -y apache-maven

- mvn archetype:generate -DgroupId=com.mycompany.app -DartifactId=my-app -DarchetypeArtifactId=maven-archetype-quickstart -DarchetypeVersion=1.4 -DinteractiveMode=false

Make sure to pull the data sets from your s3 container by running the following commands in the same directory as your pom file

- wget https://bucketsetforassignment2.s3.amazonaws.com/ValidationDataset.csv
- wget https://bucketsetforassignment2.s3.amazonaws.com/TrainingDataset.csv

After getting all of our datasets, we must next step is to push all the .csv to the hadoop file system

- hadoop fs -put TrainingDataset.csv /user/hadoop/TrainingDataset.csv
- hadoop fs -put ValidationDataset.csv /user/hadoop/ValidationDataset.csv

In order to build the project and run the java applications via spark we need to first run 

- mvn clean compile assembly:single

Once the project is built and the program is compiled we can begin to execute the application

- spark-submit target/my-app-1.0-SNAPSHOT-jar-with-dependencies.jar

Our next step is to transfer the training model folder that maven builds to our local machine so we can use it to predict on a single ec2 instance



Creating EC2 Instance for Prediction 

For this procedure I went ahead and downloaded the WinSCP program and transferred the files to my local machine using this program. Be sure to have a copy of the .ppk key and the host name as you are going to need it to connect to the master of the cluster if you decide to transfer files via WinSCP. Once the files are successfully transferred to your local machine we can begin to create the Ec2 instance to run the prediction model. 

To build the Ec2 instance we have to install Scala, Spark and Docker. To install scala we run the command:

- wget http://downloads.typesafe.com/scala/2.11.6/scala-2.11.6.tgz

We need to update the path environment variables and we can do this by running the following commands:

- tar -xzvf scala-2.11.6.tgz
- vim ~/.bashrc  copy following lines into file and then save it  
- export SCALA_HOME=/home/ec2-user/scala-2.11.6  
- export PATH=$PATH:/home/ec2-user/scala-2.11.6/bin  
- source ~/.bashrc

Next to install spark we run:

     - wget https://archive.apache.org/dist/spark/spark-2.4.5/spark-2.4.5-bin-hadoop2.7.tgz
     - sudo tar xvf spark-2.4.5-bin-hadoop2.7.tgz -C /opt 
     - sudo chown -R ec2-user:ec2-user /opt/spark-2.4.5-bin-hadoop2.7 
     - sudo ln -fs spark-2.4.5-bin-hadoop2.7 /opt/spark

We also need to update the path environment for spark and we can do so by running:

- vim ~/.bash_profile  
- export SPARK_HOME=/opt/spark  
- PATH=$PATH:$SPARK_HOME/bin  
- export PATH  
- source ~/.bash_profile

Next, we use WinSCP in order to transfer the jar file, the validation data set and the tar file for the training model. Once those are transferred over we need to extract the training model from the tar file by running the command:

- tar -xzvf model.tar.gz

Once everything is transferred over, we need to disable unnecessary log4j

- cp $SPARK_HOME/conf/log4j.properties.template $SPARK_HOME/conf/log4j.properties 

- vi $SPARK_HOME/conf/log4j.properties 

- log4j.rootCategory=ERROR, console

**Note: On line 19, the info keyword is replaced for the error keyword as the text above

We must also make sure to have the correct version of java installed.

- sudo yum install java-1.8.0-openjdk-devel

By running: 

- spark-submit  my-app-1.0-SNAPSHOT-jar-with-dependencies.jar

We’ll be able to see the program run and give us the results of the prediction. Next, we move onto running the programming through Docker.



Creating Docker Image for Prediction

Before running the application through docker, we must first install it. Run:

- sudo amazon-linux-extras install docker

To start the docker service we run:

- sudo service docker start


Now to create a docker image, we have to create a docker file. We do this by running: 

- touch Dockerfile

We need to edit this file according to the way the project was built. Once your docker file is edited we can begin to build the docker image. We run the command:


- Sudo docker build --tag wine_1

To verify that the image was created correctly, we run the command: 

- sudo docker images --filter reference=wine_1

We tag our image to our repository online by running command: 

- Sudo docker tag wine_1 1421623/wine_prediction

Push and pull the new image from our repo and finally run the image:

- Sudo docker push 1421623/wine_prediction
- Sudo docker pull 1421623/wine_prediction
- Sudo docker run -v TestDataset.csv 1421623/win_prediction
