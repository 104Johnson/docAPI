package com.e104.util;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;







import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PrimaryKey;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.NameMap;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;
import com.e104.errorhandling.DocApplicationException;

public class DynamoService {
	
	public static DynamoDB  dynamoinit(){

		DynamoDB dynamoDB = new DynamoDB((AmazonDynamoDB) new AmazonDynamoDBClient(
				new ProfileCredentialsProvider()).withRegion(Regions.AP_NORTHEAST_1)
			    .withEndpoint("dynamodb.ap-northeast-1.amazonaws.com"));   
		
		return dynamoDB;
	}
	
	public String getItem(String tableName,String fileId) {
		DynamoDB dynamoDB = dynamoinit();
		
		Item userData=null;
		String rtn="";
	        PrimaryKey pKey = new PrimaryKey("fileid",ByteBuffer.wrap(fileId.getBytes()));
	        userData = dynamoDB.getTable(tableName).getItem(pKey);
	        if(userData!=null && !"".equals(userData))
	        	rtn = new JSONObject(userData.toJSON()).put("fileid", fileId).toString();
		
		return rtn;
	}
	
	
	public String getUploadByExtraNo(String tableName,String extraNo) throws DocApplicationException{
		DynamoDB dynamoDB = dynamoinit();
		String userData=null;
		try{
			userData = dynamoDB.getTable(tableName).getItem("extraNo", extraNo).toJSON();
		
		}catch(NullPointerException e){
			throw new DocApplicationException("NullPointerException",99);
		}
		return userData;
	}
	/**
	 * @method updateItem
	 * @purpose 更新DB資料
	 * @param tableName
	 * @param fileId
	 * @param key(Jsonformat Key)
	 * @param jsonObj(Jsonformat Value)
	 * @return userData
	 * @throws DocApplicationException
	 */
	public String updateItem(String tableName,String fileId,String key,JSONObject jsonObj) throws DocApplicationException{
		DynamoDB dynamoDB = dynamoinit();
		String userData=null;
		try{
			
			    UpdateItemSpec updateItemSpec = new UpdateItemSpec()
	            .withPrimaryKey("fileid", fileId)
	            .withUpdateExpression("set #key = :value")
	            .withConditionExpression("#p = :val2")
	            .withNameMap(new NameMap()
	                .with("#key", key))
	            .withValueMap(new ValueMap()
	            	.withMap(":value", new tools().json2Map(jsonObj)))
	            .withReturnValues(ReturnValue.UPDATED_NEW);
			userData = dynamoDB.getTable(tableName).updateItem(updateItemSpec).getUpdateItemResult().toString();
		
		}catch(NullPointerException e){
			throw new DocApplicationException("NullPointerException",99);
		}
		return userData;
	}
	/**
	 * @method updateItem
	 * @purpose 更新DB資料
	 * @param tableName
	 * @param fileId
	 * @param key(Jsonformat Key)
	 * @param value(Jsonformat Value)
	 * @return userData
	 * @throws DocApplicationException
	 */
	public String updateItem(String tableName,String fileId,Map<String, String> value) throws DocApplicationException{
		DynamoDB dynamoDB = dynamoinit();
		String userData=null;
		try{
		
			Iterator<Entry<String, String>> iter = value.entrySet().iterator(); 
			while (iter.hasNext()) { 
				Map.Entry<String,String> entry = (Map.Entry<String,String>) iter.next(); 
			    UpdateItemSpec updateItemSpec = new UpdateItemSpec()
	            .withPrimaryKey("fileid", fileId.getBytes())
	            .withUpdateExpression("set #key = :value")
	            .withNameMap(new NameMap()
                	.with("#key", entry.getKey().toString()))
                	.withValueMap(new ValueMap()
                	.withString(":value", entry.getValue().toString()))
	            .withReturnValues(ReturnValue.UPDATED_NEW);
			userData = dynamoDB.getTable(tableName).updateItem(updateItemSpec).getUpdateItemResult().toString();
			}
		}catch(NullPointerException e){
			throw new DocApplicationException("NullPointerException",99);
		}
		return userData;
	}
	
public String getItems(String tableName,JSONArray fileIds) throws DocApplicationException{
	//DynamoDB dynamoDB = dynamoinit();
	JSONArray userData = new JSONArray();
	String itemData;
	
	for (int i=0;i<fileIds.length();i++){
		try {
			//System.out.println(getItem(tableName,fileIds.getJSONObject(i).getString("fileId")));
			itemData = getItem(tableName,fileIds.getJSONObject(i).getString("fileId"));
			if (!"".equals(itemData))
				userData.put(new JSONObject(itemData));
		} catch (JSONException e) {
			throw new DocApplicationException("Json格式轉換失敗",1);
		}
		
	}
	
	return userData.toString();
	}
	


	public String putItem(String tableName, Item putItem) throws DocApplicationException{
		DynamoDB dynamoDB = dynamoinit();
		try{
			return dynamoDB.getTable(tableName).putItem(putItem).getPutItemResult().toString();
		}catch(Exception e){
			throw new DocApplicationException(e,12);
		}
		
		
	} 
	

	public static void main(String args[]) {
		System.out.println("Start");
		DynamoService dynamoService = new DynamoService();
		//JSONArray userData = new JSONArray();
		//userData.put(new JSONObject("{\"fileid\":\"1e411903e05b4456bcfe01c7288dcde511\"},{\"fileid\":\"906eb1c4667544219607c522fe5332e811\"}"));
		//System.out.println(dynamoService.dynamoGetItems("users",userData));
		//dynamoService.GetItemRequest("1e411903e05b4456bcfe01c7288dcde511", 1);\
		try {
			//dynamoService.updateItem("convert", "57453ecace804a83ad067f661e833f0005", "status", new JSONObject().put("channelGridL", "failed"));
			Map<String, String> ttMap = new HashMap<>();
			ttMap.put("status", "fail");
			ttMap.put("title", "測試2");
			dynamoService.updateItem("users", "97f0ea35e77841f9823d7470514a8baa11", ttMap);
		} catch (DocApplicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
	
}
