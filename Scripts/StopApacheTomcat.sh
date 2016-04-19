#!/bin/sh

/opt/httpd/bin/apachectl stop
sleep 5
/opt/tomcat/default/bin/tomcat.sh stop default
sleep 5
