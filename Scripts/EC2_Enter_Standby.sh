#!/bin/sh

AutoScalingGroup="AutoScalingGroup_DocApi"
REGION="ap-northeast-1"

ID=`curl http://169.254.169.254/latest/meta-data/instance-id`
STLALTUS=`aws autoscaling describe-auto-scaling-instances --region $REGION --instance-ids $ID | grep -c InService`

if [ $STLALTUS -eq 1 ];then

	aws autoscaling enter-standby --region $REGION --instance-ids $ID --auto-scaling-group-name $AutoScalingGroup --should-decrement-desired-capacity

fi

