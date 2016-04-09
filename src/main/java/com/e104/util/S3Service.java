package com.e104.util;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;

public class S3Service {

	public AmazonS3 s3Client(){
		
		return new AmazonS3Client(new ProfileCredentialsProvider());
	}
}
