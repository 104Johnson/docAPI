#!/bin/sh

REGION="ap-northeast-1"

ID=`curl http://169.254.169.254/latest/meta-data/instance-id`
AutoScalingGroup=`aws autoscaling --region ap-northeast-1 describe-auto-scaling-instances --instance-ids $ID | grep AutoScalingGroupName | awk -F "\"" '{print $4}'`

STLALTUS=`aws autoscaling describe-auto-scaling-instances --region $REGION --instance-ids $ID | grep -c InService`

if [ $STLALTUS -eq 1 ];then

	aws autoscaling enter-standby --region $REGION --instance-ids $ID --auto-scaling-group-name $AutoScalingGroup --should-decrement-desired-capacity

fi

