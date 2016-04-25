package com.e104.restapi.dao;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;




import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.json.JSONException;
import org.json.JSONObject;



import com.amazonaws.services.dynamodbv2.document.Item;
import com.e104.Errorhandling.DocApplicationException;
import com.e104.util.Config;
import com.e104.util.DynamoService;
import com.e104.util.tools;

public class DynamoConvert {
	private byte[] fileId;
	private int contentType;
	private String apnum;
	private String filePath;
	private String insertDate;
	private String triggerDate;
	private Map<String, String> convertLists = new HashMap<String, String>();
	private Map<String, String> status = new HashMap<String, String>();
	private List<String> convertItems = new ArrayList<>();
	private Map<String, String> videoQuality;
	tools tools = new tools();
	public void insertDynamo(JSONObject convert) throws DocApplicationException{
		try{
		fileId =  Hex.decodeHex(convert.getString("fileId").toCharArray());
		contentType = convert.getInt("contentType");
		apnum = convert.getString("apnum");
		filePath = convert.getString("filePath");
		insertDate = convert.getString("insertDate");
		triggerDate = convert.getString("triggerDate");
		
		
		convertLists = tools.json2Map(convert.getJSONObject("convertLists"));
		
		//convertLists = convert.getJSONObject("multiAction")
		
		for (int i=0;i<convert.getJSONArray("convertItems").length();i++ ){
			convertItems.add(convert.getJSONArray("convertItems").getJSONObject(i).toString());
		}
		
		status = tools.json2Map(convert.getJSONObject("status"));
		if (convert.has("videoQuality"))
			videoQuality = new HashMap<String,String>(tools.json2MapObj(convert.getJSONObject("videoQuality")));
		}catch(JSONException e){
			throw new DocApplicationException("NotPresent",12);//erroehandler 必填欄位未填
		} catch (DecoderException e) {
			throw new DocApplicationException("Decoder失敗",1);
		}
		
		this.doInsertDb();
		
	}
	private void doInsertDb() throws DocApplicationException{
		DynamoService dynamoService = new DynamoService();
		try{
		Item putItem = new Item().withPrimaryKey("fileId",fileId).
				withNumber("contentType", contentType).
				withString("apnum", apnum).
				withString("filePath",filePath).
				withString("insertDate", insertDate).
				withString("triggerDate", triggerDate).
				withMap("status", status).
				withList("convertItems",convertItems).
				withMap("convertLists", convertLists);
		//非必填項目
		if(videoQuality!=null) 
			putItem.withMap("videoQuality", videoQuality);
			
		
		dynamoService.putItem(Config.convert, putItem);
		}catch(Exception e){
			throw new DocApplicationException("NotPresent",3);//erroehandler 必填欄位未填
		}
	}
	
	
}
