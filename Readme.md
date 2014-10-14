# Installation
1. Install Apache Tomcat (http://tomcat.apache.org/download-80.cgi)
2. Download the virtuoso jdbc Driver (virtjdbc3.jar or virtjdbc4.jar) from http://virtuoso.openlinksw.com/dataspace/doc/dav/wiki/Main/VOSDownload
3. Place the driver in in $TOMCAT_HOME/libs

4. Install PostgreSQL (https://wiki.postgresql.org/wiki/Detailed_installation_guides)
or any other SQL Database. This database will be used as cache storage
5. Download the database specific JDBC driver (for PostgreSQL http://jdbc.postgresql.org/download.html) and place it in $TOMCAT_HOME/libs

6. Next set up a new user for the cache Database : For PostgreSQL this can be done with the following commands :
   1. CREATE ROLE virtuoso_user WITH LOGIN ENCRYPTED PASSWORD 'virtuoso_pw';
   2. CREATE DATABASE virtuoso_cache WITH OWNER virtuoso_user;

   username and password are of course free to choose but have to be changed also in [context.xml](../master/web/META-INF/context.xml)
7. Deploy this application to tomcat and restart tomcat in order to add the 2 jdbc drivers to the classpath


