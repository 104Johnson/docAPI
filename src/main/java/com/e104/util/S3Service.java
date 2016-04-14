package com.e104.util;

import java.util.List;

import scala.reflect.internal.Trees.This;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.e104.Errorhandling.DocApplicationException;

public class S3Service {

	public AmazonS3 s3Client(){
		
		return new AmazonS3Client(new ProfileCredentialsProvider());
	}
	
	/**
	 * This method first deletes all the files in given folder and than the
	 * folder itself
	 * @throws DocApplicationException 
	 */
	public int deleteFolder(String bucketName, String folderName) throws DocApplicationException {
		AmazonS3 client = s3Client();
		List<S3ObjectSummary> fileList=null;
		String keyRoot = folderName.split("/")[0];
		try{
			fileList = client.listObjects(bucketName, folderName).getObjectSummaries();
			for (S3ObjectSummary file : fileList) {
				client.deleteObject(bucketName, file.getKey());
			}
			
			System.out.println(keyRoot);
			client.deleteObject(bucketName, keyRoot);
			
		}catch(Exception e){
			throw new DocApplicationException("S3 delete fail", 15);
		}
		return fileList.size();
	}
}
