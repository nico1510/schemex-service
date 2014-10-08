Install Apache Tomcat
Download jdbc Driver (virtjdbc3.jar or virtjdbc4.jar) from http://virtuoso.openlinksw.com/dataspace/doc/dav/wiki/Main/VOSDownload
Place virtjdbc*.jar in $TOMCAT_HOME/libs

Install PostgreSQL (https://wiki.postgresql.org/wiki/Detailed_installation_guides)
or any other SQL Database. This database will be used as cache storage
Download the database specific JDBC driver (for PostgreSQL http://jdbc.postgresql.org/download.html) and place it in $TOMCAT_HOME/libs

Next set up a new user for the cache Database : For PostgreSQL this can be done with the following commands :
 CREATE ROLE virtuoso_user WITH LOGIN ENCRYPTED PASSWORD 'virtuoso_pw';
 CREATE DATABASE virtuoso_cache WITH OWNER virtuoso_user;


