package com.e104.util;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.NameMap;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;
import com.e104.errorhandling.DocApplicationException;

public class DynamoService {
	
	public static DynamoDB  dynamoinit(){

		DynamoDB dynamoDB = new DynamoDB((AmazonDynamoDB) new AmazonDynamoDBClient(
				new ProfileCredentialsProvider()).withRegion(Regions.AP_NORTHEAST_1)
			    .withEndpoint("dynamodb.ap-northeast-1.amazonaws.com"));
			    
			    
		
		return dynamoDB;
	}
	
	public String getItem(String tableName,String fileId) throws DocApplicationException{
		DynamoDB dynamoDB = dynamoinit();
		/*Map<String, String> map = new HashMap<String, String>();
		map.put(key, value)
		dynamoDB.getItem(new GetItemRequest().withTableName("mytable").withKey((
	            new Key().withHashKeyElement(new AttributeValue().withS("1"))));
		
		GetItemSpec spec = new GetItemSpec()
		 .withKeyConditionExpression("Id = :v_id")
		    .withValueMap(new ValueMap()
		        .withString(":v_id", "Amazon DynamoDB#DynamoDB Thread 1"));
	    .withPrimaryKey("Id", 206).withNameMap(new Map<"", "">)
	    .withProjectionExpression("Id, Title, RelatedItems[0], Reviews.FiveStar")
	    .withConsistentRead(true);

	Item item = table.getItem(spec);*/
		String userData=null;
		try{
			userData = dynamoDB.getTable(tableName).getItem("fileid", fileId).toJSON();
		
		}catch(NullPointerException e){
			e.printStackTrace();
			throw new DocApplicationException(e,11);
		}
		return userData;
	}
	
	public String getUploadByExtraNo(String tableName,String extraNo) throws DocApplicationException{
		DynamoDB dynamoDB = dynamoinit();
		String userData=null;
		try{
			userData = dynamoDB.getTable(tableName).getItem("extraNo", extraNo).toJSON();
		
		}catch(NullPointerException e){
			e.printStackTrace();
			throw new DocApplicationException(e,11);
		}
		return userData;
	}
	
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
			e.printStackTrace();
			throw new DocApplicationException(e,11);
		}
		return userData;
	}
	
public String getItems(String tableName,JSONArray fileIds){
	//DynamoDB dynamoDB = dynamoinit();
	JSONArray userData = new JSONArray();
	
	
	for (int i=0;i<fileIds.length();i++){
		try {
			//System.out.println(getItem(tableName,fileIds.getJSONObject(i).getString("fileId")));
			userData.put(new JSONObject(getItem(tableName,fileIds.getJSONObject(i).getString("fileId"))));
		} catch (JSONException | DocApplicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	return userData.toString();
	}
	

public String GetItemRequest(String fileid,int isp){
	DynamoDB  dynamoDB = dynamoinit();
	
     // item.put("fileid", new AttributeValue(fileid));
      //item.put("fileid", new AttributeValue("906eb1c4667544219607c522fe5332e811"));
      //item.put("convert", new AttributeValue("success"));
      //Map<String, AttributeValue> item1 = new HashMap<String, AttributeValue>();
      //item.put("convert", new AttributeValue("success"));
      //item.put("isP", new AttributeValue(Integer.toString(isp)));

		//Map<String, String> item = new HashMap<String, String>();
		
		//item.put("isP", String.valueOf(isp));
		
     // GetItemSpec spec = new GetItemSpec().withPrimaryKey("fileid",fileid).withNameMap(item);
		//GetItemRequest getItemRequest = new GetItemRequest().withTableName("users").withKey(item);
				//.withExpressionAttributeNames(expressionAttributeNames);
	//	 System.out.println(dynamoDB.geti.getItem(getItemRequest));
		
     // System.out.println(dynamoDB.getTable("users").getItem("fileid","1e411903e05b4456bcfe01c7288dcde511")
    		//  .a);
      
      //GetItemResult item = dynamoDBClient.getItem(getItemRequest);
      return "";
}

	public void putItem(String tableName, Item putItem){
		DynamoDB dynamoDB = dynamoinit();
		/*AmazonDynamoDBClient dynamoDB = new AmazonDynamoDBClient(
				new ProfileCredentialsProvider()).withRegion(Regions.AP_NORTHEAST_1)
			    .withEndpoint("dynamodb.ap-northeast-1.amazonaws.com");
		*/
		System.out.println(dynamoDB.getTable(tableName).putItem(putItem).getPutItemResult());
		
		
	} 

	public static void main(String args[]) {
		System.out.println("Start");
		DynamoService dynamoService = new DynamoService();
		JSONArray userData = new JSONArray();
		//userData.put(new JSONObject("{\"fileid\":\"1e411903e05b4456bcfe01c7288dcde511\"},{\"fileid\":\"906eb1c4667544219607c522fe5332e811\"}"));
		//System.out.println(dynamoService.dynamoGetItems("users",userData));
		//dynamoService.GetItemRequest("1e411903e05b4456bcfe01c7288dcde511", 1);\
		try {
			dynamoService.updateItem("convert", "57453ecace804a83ad067f661e833f0005", "status", new JSONObject().put("channelGridL", "failed"));
		} catch (DocApplicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
	
}
