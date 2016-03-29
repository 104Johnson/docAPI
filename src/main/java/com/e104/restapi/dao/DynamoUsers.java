package com.e104.restapi.dao;


import org.json.JSONException;
import org.json.JSONObject;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.e104.errorhandling.DocApplicationException;
import com.e104.util.DynamoService;

public class DynamoUsers {
	private String pid;
	private String fileid;	
	private String filename;
	private String filepath;
	private String apnum;
	private String description;
	private String insertdate;
	private String imgstatus;
	private String videoQuality;
	private String convert;
	private String title;
	private String source;
	private String expireTimestamp;
	private int contenttype;
	private int isP;
	
	public void insertDynamo(JSONObject users) throws DocApplicationException{
		try{
			pid = users.getString("pid");
			fileid = users.getString("fileid");
			contenttype = users.getInt("contenttype");
			filename = users.getString("filename");
			filepath = users.getString("filepath");
			apnum = users.getString("apnum");
			description = users.getString("description");
			title = users.getString("title");
			insertdate = users.getString("insertdate");
			imgstatus = users.getString("imgstatus");
			convert = users.getString("convert");
			isP = users.getInt("isP");
			
			if(users.has("source")) 
				source = users.getString("source");
			if(users.has("videoQuality"))
				videoQuality = users.getString("videoQuality");
			if(users.has("expireTimestamp"))
				expireTimestamp = String.valueOf(users.getLong("expireTimestamp"));
			
		}catch(JSONException e){
			throw new DocApplicationException("NotPresent",3);//erroehandler 必填欄位未填
		}
		
		this.doInsertDb();
			
	}
	
	private void doInsertDb() throws DocApplicationException{
		try{
		DynamoService dynamoService = new DynamoService();

		Item putItem = new Item().withPrimaryKey("fileid",fileid).
				withString("apnum", apnum).
				withNumber("contenttype", contenttype).
				withString("convert", convert).
				withString("filename", filename).
				withString("filepath", filepath).
				withString("imgstatus", imgstatus).
				withString("insertdate",insertdate).
				withNumber("isP",isP).
				withString("pid", pid).
				withString("description", description).
				withString("title", title);
				
		
				//非必填項目
				if(source!=null) 
					putItem.withString("source", source);
				if(videoQuality!=null)
					putItem.withString("videoQuality", videoQuality);
				if(expireTimestamp!=null)
					putItem.withString("expireTimestamp", expireTimestamp);
				
		
		
		dynamoService.putItem("users", putItem);
		}catch(Exception e){
			throw new DocApplicationException("NotPresent",3);//erroehandler 必填欄位未填
		
		}
	}
}
