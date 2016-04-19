#/bin/sh
# Create by Clive

S3Bucket="e104-automation-apn1"
SubPath="codedeploy-src"
JDK="jdk-7u80-linux-x64.tar.gz"
TOMCAT="tomcat-7.0.68-104.tgz"
APACHE="apache-2.2.31-104.tgz"
SITE="docapi"

JavaVer="java1.7"

### Prepare Envirment

SRCDIR=/resource

if [ ! -d $SRCDIR ];then
	mkdir -p $SRCDIR
fi

cd $SRCDIR

### Install JDK 7

DESDIR=/opt/jvm

if [ ! -d $DESDIR ];then

    mkdir -p $DESDIR

	aws s3 cp s3://$S3Bucket/$SubPath/servicepkg/jdk/$JDK $SRCDIR

	tar -zxpf $JDK -C $DESDIR
	
	JDK_VER=`find $DESDIR -maxdepth 1 -type d | grep -v ^/opt/jvm$`
	
	ln -s $JDK_VER $DESDIR/$JavaVer

	rm -rf $SRCDIR/*

fi

### Install Tomcat 7

DESDIR=/opt/tomcat

if [ ! -d $DESDIR ];then
        
	mkdir -p $DESDIR

	aws s3 cp s3://$S3Bucket/$SubPath/servicepkg/apache22_tomcat7/$TOMCAT $SRCDIR

	tar -zxpf $TOMCAT -C $DESDIR

	### Config Tomcat 7

	aws s3 cp s3://$S3Bucket/$SubPath/serviceconf/tomcat7/$SITE/bin.tgz $SRCDIR

	tar -zxpf bin.tgz -C $DESDIR/default

	aws s3 cp s3://$S3Bucket/$SubPath/serviceconf/tomcat7/$SITE/conf.tgz $SRCDIR

	tar -zxpf conf.tgz -C $DESDIR/default

	rm -rf $SRCDIR/*

fi

### Install Apache 2.2

DESDIR=/opt/httpd

if [ ! -d $DESDIR ];then

	aws s3 cp s3://$S3Bucket/$SubPath/servicepkg/apache22_tomcat7/$APACHE $SRCDIR

	tar -zxpf $APACHE -C /opt

	### Confog Apache 2.2

	aws s3 cp s3://$S3Bucket/$SubPath/serviceconf/apache22/$SITE/conf.tgz $SRCDIR

	tar -zxpf conf.tgz -C $DESDIR

	rm -rf $SRCDIR/*

fi


