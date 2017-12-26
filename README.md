# Web-based quiz game

This is a web-based quiz game.

![quiz](https://user-images.githubusercontent.com/7523395/34353580-4e55baa4-ea29-11e7-9e03-906e91948b12.gif)

## Dependencies
* Apache Tomcat
* [jQuery](https://jquery.com/)
* [Bootstrap](http://getbootstrap.com/)
* Apache ant
* Java JDK

```shell
apt-get install ant
apt-get install openjdk-8-jdk
```

## Installing / Getting started (Linux)
  1) Download Apache Tomcat (tested under Apache Tomcat 8.5.24)
  2) Copy the files to /opt/tomcat
  3) Adopt the tomcat users:
  
      File: opt/tomcat/conf/tomcat-users.xml
      ```xml
      <tomcat-users>
        <role rolename="manager-gui"/>
        <user username="admin" password="admin" roles="manager-gui,admin-gui,manager-script,admin-script"/>
      </tomcat-users>
      ```
   4) starting tomcat using: 
   ```shell 
   /opt/tomcat/bin/startup.sh 
   ```
   5) Go to the repository directory and run ant
   6) At the first deployment you have to deploy the .war file by hand. Use the build-in manager. This manager is avaliable under <IP_ADDRESS>:8080/manager/html. Then go to "WAR file to deploy" and select the .war file to deploy it.

## Using
  Use your browser and go to "http://<IP_ADDRESS>:8080/webquiz/"

## Nice To Know

### Questions
You can define your own questions. Have a look at "/WebContent/WEB-INF/catalog/". Create a new XML-file with your own questions.

## Author
Christian Högerle and Thomas Buck

## Licensing
The code in this project is licensed under MIT license.
