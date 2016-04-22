package com.e104.util;

import org.apache.commons.codec.DecoderException;
import org.aspectj.weaver.patterns.ThisOrTargetAnnotationPointcut;
import org.json.JSONException;
import org.json.JSONObject;

import scala.reflect.internal.Trees.This;

import com.e104.Errorhandling.DocApplicationException;

/**
 * this class wrap 'convert' collection to provide some helper functions.
 * @author louis.tsao
 *
 */
public class Convert {
	
	private JSONObject json;

	//
	private DynamoService db;
	private String fileid;

	private Convert(JSONObject json, DynamoService db){
		this.json = json;
		this.db = db;
	};

	public static Convert fromDB(String fileId){
		try{
			
			DynamoService db = new DynamoService();
			//TODO Johnson 改成DynamoService 模式
			//JSONObject data = db.findConvert(fileId);
			JSONObject data = new JSONObject(db.getItem("convert", fileId));
			if(data != null)
				return new Convert(data, db);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}	
	
	public JSONObject getJSONObject(){
		return this.json;
	}
	
	public String getFileId() throws JSONException{
		return this.json.getString("fileid");
	}
	
	public JSONObject getStatusObject() throws JSONException{
		return this.json.has("status") ? this.json.getJSONObject("status") : null;
	}
	
	public String getStatus(String tag) throws JSONException{
		JSONObject statusObj = getStatusObject();
		return statusObj != null && statusObj.has(tag) ? statusObj.getString(tag) : null; 
	}
	
	public Convert setStatus(String tag, String status) throws JSONException{
		JSONObject statusObj = getStatusObject();
		if(statusObj == null){
			statusObj = new JSONObject();
			this.json.put("status", statusObj);
		}
		
		statusObj.put(tag, status); 
		
		return this;
	}
	
	/**
	 * 檢查 mongo convert collection 中的 status 來更新 users.convert 欄位.
	 * @param fileId
	 * @return
	 * @throws JSONException
	 * @throws DocApplicationException 
	 */
	public boolean syncUserConvertField() throws JSONException, DocApplicationException{
		// 檢查 convert.status 各 tag 的狀態, 若是均是 success, 才 on success, 否則 fail.
		//TODO Johnson 改成DynamoDB取fileid模式
		//JSONObject user = db.findUser(this.getFileId());		
		JSONObject user;
		try {
			user = new JSONObject(db.getItem(config.document, this.getFileId()));
		
			if(new tools().isEmpty(user.toString())) {
				// logger.error("fileid in users collection not found => " + fileId);
				return false;
			}
			
			JSONObject status = this.getStatusObject();
			
			boolean allSuccess = true;
			for(String tagName : JSONObject.getNames(status)){
				// convert collection 中的 status 欄位存放的都是小寫.
				boolean success = status.getString(tagName).equals("success");				
				if(!success) allSuccess = false;
			}
					
			// 更新 users.convert 欄位.
			user.put("convert", allSuccess ? "success" : "fail");
			//TODO Johnson Dynamo Update實作還沒做
			//return db.updateUser(user);
		} catch (DecoderException e) {
			throw new DocApplicationException("DB operation failed",13);
		}
		return true;
		// logger.info("users convert status updated, fileid => " + fileId);
		
		// return true;
	}
	
	public boolean updateTriggerDate() throws JSONException{
		String now = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
    	this.json.put("triggerDate", now);
    	return this.updateDB();
	}
	
	public boolean updateDB() throws JSONException{
		//TODO Johnson Dynamo更新實作方式改變return db.upadateConvert(this.json);
		
		boolean data=false;
		try {
			data = "".equals(db.updateItem("convert", this.getFileId(), "status", new JSONObject(this.json)))? true: false;
		} catch (DocApplicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return data;
		
	}
}
