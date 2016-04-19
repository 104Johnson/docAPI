#!/bin/sh

/opt/httpd/bin/apachectl start
sleep 5
/opt/tomcat/default/bin/tomcat.sh start default
sleep 5
