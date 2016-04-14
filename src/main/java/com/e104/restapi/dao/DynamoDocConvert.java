package com.e104.restapi.dao;

import java.util.List;




import org.json.JSONException;
import org.json.JSONObject;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.e104.Errorhandling.DocApplicationException;
import com.e104.util.DynamoService;

public class DynamoDocConvert {
	private String txid;
	private String fileid;
	private String filePath;
	private String docToPdf;
	private String doMultiAction;
	private String pdfToImg;
	private String doDocumentImageSize;
	private String pdfOnly;
	private String method;
	private List<String> multiAction;
	private List<String> documentImageSize;
	
	public void insertDynamo(JSONObject docConvert) throws DocApplicationException{
		try{
			
			txid = docConvert.getString("txid");
			fileid  = docConvert.getString("fileid");
			filePath  = docConvert.getString("filePath");
			docToPdf  = docConvert.getString("docToPdf");
			doMultiAction  = docConvert.getString("doMultiAction");
			pdfToImg  = docConvert.getString("pdfToImg");
			pdfOnly  = docConvert.getString("pdfOnly");
			method  = docConvert.getString("method");
			doDocumentImageSize  = docConvert.getString("doDocumentImageSize");
			doDocumentImageSize  = docConvert.getString("doDocumentImageSize");
			
			if(docConvert.has("pdfOnly") && docConvert.getBoolean("pdfOnly")){
				pdfOnly  = "true";
				method  = "doc2Pdf";
			}
			else{
				pdfOnly = "false";
				method = "putFile";
			}
			if(docConvert.has("multiAction") && !"".equals(docConvert.getJSONArray("multiAction"))) {				        			
				for (int i=0;i<=docConvert.getJSONArray("convertItems").length();i++ ){
					multiAction.add(docConvert.getJSONArray("convertItems").getJSONObject(i).toString());
				}			        		
			}else{
				multiAction.add("");
			}
			if(docConvert.has("documentImageSize")) {
				for (int i=0;i<=docConvert.getJSONArray("convertItems").length();i++ ){
					documentImageSize.add(docConvert.getJSONArray("documentImageSize").getJSONObject(i).toString());
				}
			}
		}catch(JSONException e){
			throw new DocApplicationException("NotPresent",3);//erroehandler 必填欄位未填
		}
		
		this.doInsertDb();
	}
	
	private void doInsertDb() throws DocApplicationException{
		DynamoService dynamoService = new DynamoService();

		Item putItem = new Item().withPrimaryKey("fileid",fileid).
				withString("txid", txid).
				withString("fileid", fileid).
				withString("filePath", filePath).
				withString("docToPdf",docToPdf).
				withString("doMultiAction", doMultiAction).
				withString("pdfToImg", pdfToImg).
				withString("doDocumentImageSize", doDocumentImageSize).
				withString("pdfOnly",pdfOnly).
				withString("method", method).
				withList("multiAction", multiAction).
				withList("documentImageSize", documentImageSize);
			
		
		dynamoService.putItem("docConvert", putItem);

	}
	
}
