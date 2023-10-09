Service Insight is a Spring 4.x, Servlet 3 Maven organized web application!

The application uses jetty as a local, development web app container.

To build and run at the commandline, resulting in a running application at the URL: http://localhost:8080/si
mvn -Dspring.profiles.active=local jetty:run

wow. wasn't that tough! not really...

HOWEVER, the app wants to connect to a local MySQL database. You can reconfigure this in src/main/resources/serviceinsight.properties

To use the configured authenticated MySQL user, you need to execute these commands in MySQL, as the db root user, one time:

create database service_insight;
CREATE USER 'siuser'@'localhost' IDENTIFIED BY 'L0gicalis';
GRANT ALL PRIVILEGES ON service_insight.* TO 'siuser'@'localhost' WITH GRANT OPTION;
CREATE USER 'siuser'@'%' IDENTIFIED BY 'L0gicalis';
GRANT ALL PRIVILEGES ON service_insight.* TO 'siuser'@'%' WITH GRANT OPTION;

LMSTS environment (PROD) deployment
===================================

Service Insight depends on the PartnerCenter (PCC) client project. That project deploys to a devlab maven repo accessible on our VPN (http://10.128.14.32/maven/snapshot). BUT, when we try to deploy to production, you've either got to xfer a built war file for deployment or install the pcc project locally "mvn install" it so that when the project you want to deploy builds, it can find the "fresher" local repo instead of trying to go out to 10.128.14.32, which it can't reach.

SO, you need to fetch and build the pcc project, and deploy it into a local maven repository (your file system) and make that available to the dependent project. Here's how to do that:

mkdir ~/.m2/maven/snapshot

git clone https://jsanchez@bitbucket.org/logicalis/partnercenterclient.git

cd partnercenterclient

mvn -Dmaven.test.skip=true clean package

mvn -Dmaven.test.skip=true deploy:deploy-file -DgroupId=com.logicalis.pcc -DartifactId=pcc-client -Dversion=1.0-WEB-SNAPSHOT -Dpackaging=jar -Dfile=./target/pcc-client-1.0-WEB-SNAPSHOT.jar -DrepositoryId=logicaliscloud.repo.snapshot -DgeneratePom=true -Durl=file:///home/jsanchez/.m2/maven/snapshot


note: it's important to build the expected version (1.0-WEB-SNAPSHOT) the dependent project is looking for....


Now, build your dependent project this way:

edit the Service Insight pom.xml and temporarily change the pcc project repository devlab URL

-  <url>http://10.128.14.32/maven/snapshot</url>

+  <url>file:///home/jsanchez/.m2/maven/snapshot</url>

 
Now, when you build your dependent project it will pick up the exact repo artifacts locally.

For ServiceInsight, build and deployment goes like this:

make a copy of the war in Tomcat for a backup
build the application:

mvn -Dmaven.test.skip=true clean package

Stop Tomcat, replace the war file and restart Tomcat!
