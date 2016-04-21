package com.e104.restapi.dao;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.json.JSONException;
import org.json.JSONObject;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.util.BinaryUtils;
import com.e104.Errorhandling.DocApplicationException;
import com.e104.util.DynamoService;
import com.e104.util.TraceLog;
import com.e104.util.tools;


public class DynamoUsers {
	private String pid;
	private byte[] fileid;
	private String filename;
	private String filepath;
	private String apnum;
	private String description;
	private String insertdate;
	private String imgstatus;
	private  Map<String, String> videoQuality;
	private String convert;
	private String title;
	private String source;
	private String expireTimestamp;
	private int contenttype;
	private int isP;
	private String src = "docapi::core::DynamoUsers::";
	public String trackId = "";
	public String caller = "";
	
	public void setTrackId(String trackId){
		this.trackId = trackId;
	}
	
	public void setCaller(String caller){
		this.caller = caller;
	}
	TraceLog traceLog = new TraceLog();
	public String insertUsersToDynamo(JSONObject users) throws DocApplicationException{
		traceLog.writeKinesisLog(trackId, caller, src+"insertUsersToDynamo", "paser users Data", new JSONObject());
		try{
			pid = users.getString("pid");
			fileid =  Hex.decodeHex(users.getString("fileid").toCharArray());
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
				videoQuality = new HashMap<String,String>(new tools().json2MapObj(users.getJSONObject("videoQuality")));
			if(users.has("expireTimestamp"))
				expireTimestamp = String.valueOf(users.getLong("expireTimestamp"));
			
		}catch(JSONException e){
			throw new DocApplicationException("Json格式轉換失敗",1);
		} catch (DecoderException e) {
			throw new DocApplicationException("Decoder失敗",13);
		}
		
		return this.doInsertDb();
			
	}
	
	private String doInsertDb() throws DocApplicationException{
		traceLog.writeKinesisLog(trackId, caller, src+"doInsertDb", "insert users table", new JSONObject());
		String rtn="";
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
					putItem.withMap("videoQuality", videoQuality);
				if(expireTimestamp!=null)
					putItem.withString("expireTimestamp", expireTimestamp);
				
				rtn =dynamoService.putItem("users", putItem);

		}catch(Exception e){
			throw new DocApplicationException(e,12);
		
		}
		return rtn;
	}
	 
}
