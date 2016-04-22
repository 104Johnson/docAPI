package com.e104.restapi.dao;

import org.json.JSONException;
import org.json.JSONObject;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.e104.Errorhandling.DocApplicationException;
import com.e104.util.Config;
import com.e104.util.DynamoService;
import com.e104.util.TraceLog;

public class DynamoDeleteFileLog {
	private byte number;
	private byte[] fileId;
	private String filePath;
	private String deleteTime;
	private String src = "docapi::core::DynamoDeleteFileLog::";
	
	public String trackId = "";
	public String caller = "";
	
	public void setTrackId(String trackId){
		this.trackId = trackId;
	}
	
	public void setCaller(String caller){
		this.caller = caller;
	}
	
	TraceLog traceLog = new TraceLog();
	public String deleteFileLogToDynamo(JSONObject dynamoDeleteFileLog) throws DocApplicationException{
		traceLog.writeKinesisLog(trackId, caller, src+"deleteFileLogToDynamo", "paser dynamoDeleteFileLog Data", new JSONObject());
		try{
			number = (byte)dynamoDeleteFileLog.getInt("number");
			fileId =  dynamoDeleteFileLog.getString("fileId").getBytes();
			filePath = dynamoDeleteFileLog.getString("filePath");
			deleteTime = dynamoDeleteFileLog.getString("deleteTime");
		
		}catch(JSONException e){
			throw new DocApplicationException("Json格式轉換失敗",1);
		}
		
		return this.doDeleteDb();
			
	}
	
	private String doDeleteDb() throws DocApplicationException{
		traceLog.writeKinesisLog(trackId, caller, src+"doInsertDb", "insert DeleteFileLog table", new JSONObject());
		String rtn="";
		try{
		DynamoService dynamoService = new DynamoService();

		Item putItem = new Item().withPrimaryKey("fileId",fileId).
				withNumber("number", number).
				withString("filePath", filePath).
				withString("deleteTime", deleteTime);
				
				
				rtn =dynamoService.putItem(Config.deleteFileLog, putItem);

		}catch(Exception e){
			throw new DocApplicationException(e,12);
		
		}
		return rtn;
	}
}
