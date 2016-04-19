#!/bin/sh

Result=0

while [ $Result -eq 0 ]
do

	Result=`curl --max-time 10 -s http://localhost/monitor/check.jsp | grep -c Hello`
		
	sleep 1

done

