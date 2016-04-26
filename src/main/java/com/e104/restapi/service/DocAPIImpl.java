package com.e104.restapi.service;


import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.spy.memcached.MemcachedClient;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.cxf.jaxrs.ext.MessageContext;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.core.Context;

import org.apache.logging.log4j.*;
import org.aspectj.weaver.patterns.ThrowsPattern;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import redis.clients.jedis.Jedis;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.e104.Errorhandling.DocApplicationException;
import com.e104.restapi.dao.DynamoConvert;
import com.e104.restapi.dao.DynamoDeleteFileLog;
import com.e104.restapi.dao.DynamoUsers;
import com.e104.restapi.model.GetFileUrl;
import com.e104.restapi.model.Signature;
import com.e104.restapi.model.UpdateFile;
import com.e104.restapi.model.jsonData;
import com.e104.restapi.service.DocAPIImpl;
import com.e104.util.Config;
import com.e104.util.ContentType;
import com.e104.util.DateUtil;
import com.e104.util.DynamoService;
import com.e104.util.RedisService;
import com.e104.util.S3Service;
import com.e104.util.TraceLog;
import com.e104.util.tools;

public class DocAPIImpl implements IDocAPI{
	private static transient Logger Logger = LogManager.getLogger(DocAPIImpl.class);
	String bucketName = "e104-filetemp";
	String objectKey = "123/456/test.txt";
	tools tools = new tools();
	String trackId ="";
	String caller = "";
	String src;
	TraceLog traceLog = new TraceLog();
	@Context
	private MessageContext context;
	/*
	 * @method addKey
	 * @purpose 不開放使用, 增加key
	 * @param fileid
	 * @param key
	 * @param value
	 * @return String
	 * @throws DocApplicationException
	 */
	@Override
	public String addKey(String fileid, String key, String value) throws DocApplicationException{
		getHeaderValue();
		JSONObject rtn = new JSONObject();
		src = "docapi::core::addKey::";		
		try{
			traceLog.writeKinesisLog(trackId, caller, src, "addKey::Start", rtn);
			if ("".equals(fileid) || "".equals(key) || "".equals(value))
				throw new DocApplicationException("Field value is null", 3);
			DynamoService dynamoService = new DynamoService();
			Map<String, String> updateMap = new HashMap<String,String>();
			updateMap.put(key, value);
			dynamoService.updateItem(Config.document, fileid, updateMap);
				
			rtn.put("txid", tools.generateTxid());
			rtn.put("status", "Success");
			traceLog.writeKinesisLog(trackId, caller, src, "addKey::End", rtn);
		} catch (DocApplicationException e) {
			throw e;
			//logger.error("addKey("+fileid+","+key+","+value+") Exception", e);
		} catch(Exception e){
			e.printStackTrace();
			throw new DocApplicationException(e, 99);
		}
		return rtn.toString();
	}

	@Override
	public String checkFileSpec(String specObj) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String clearFileCache(String cacheSize) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String confirmUpload(String fileid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String copyFile(String fileObj) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String copyFileForMM(String fileObj) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String decryptParam(String param) throws DocApplicationException {
		param = tools.decode(param);
		if (param==null || "".equals(param))
			throw new DocApplicationException("Data Decrypt Fail", 16);
    	return param;
	}

	@Override
	public String encryptParam(String param) throws DocApplicationException {
		try{
			JSONObject obj = new JSONObject(param);
			obj.put("actionTimestamp", System.currentTimeMillis());
	    	param = tools.encode(obj.toString());
		}
    	catch(JSONException e){
    		throw new DocApplicationException("Json format Error", 1);
    	}
    	return param;    
	}
	/**
	 * @method deleteFile
	 * @purpose 刪除檔案
	 * @param fileid
	 * @return String
	 * @throws DocApplicationException 
	 */
	@Override
	public String deleteFile(String fileId, String fileTag, String delExtend) throws DocApplicationException {
		getHeaderValue();
		src = "docapi::core::deleteFile::";		
		
		if(fileId != null && !"".equals(fileId.trim())){
			
			//logger.info("fileId : " + fileId + " , fileTag=>" + fileTag + " , delExtend=>" + delExtend);
			
			JSONObject rtn = new JSONObject();
			String txid = tools.generateTxid();
			int insertCount = 1;
			DynamoService dynamoService = new DynamoService();
			DynamoDeleteFileLog deleteFileLog = new DynamoDeleteFileLog();
			S3Service s3Service = new S3Service();
			try{
				//FileManageDispatch fmd = new FileManageDispatch();
				traceLog.writeKinesisLog(trackId, caller, src, "deleteFile::Start", rtn);	
				
				JSONObject filedetail = new JSONObject(dynamoService.getItem(Config.document, fileId));
				JSONArray extensions = new JSONArray("[\"JPG\",\"jpg\",\"GIF\",\"gif\",\"PNG\",\"png\",\"PPT\",\"ppt\",\"MP4\",\"mp4\",\"FLV\",\"flv\",\"MP3\",\"mp3\",\"wmv\",\"doc\",\"DOC\",\"xls\",\"XLS\",\"avi\",\"AVI\",\"bmp\",\"BMP\",\"pdf\",\"PDF\"]");
				String filepath = filedetail.getString("filepath");
				String path = "";
				int contenttype = filedetail.getInt("contenttype");
				
//					System.out.println("DEBUG deleteFile , " + DateUtil.getDateTimeForLog() + " , finish execute getFileDetail() , fileId : " + fileId + " , fileTag=>" + fileTag + " , delExtend=>" + delExtend + " , txid=>" + txid + " , filepath=>" + filepath + " , contenttype=>" + String.valueOf(contenttype));
				
				if(fileTag != null && !fileTag.equals("")) {
					
//						System.out.println("DEBUG deleteFile , " + DateUtil.getDateTimeForLog() + " , fileTag not empty , fileId : " + fileId + " , fileTag=>" + fileTag + " , delExtend=>" + delExtend);
					
			        // JSONObject js = new JSONObject(fileTag);
					JSONArray fileTagArray = new JSONObject(fileTag).getJSONArray("fileTag");
					for(int i=0;i<fileTagArray.length();i++) {
						String fileTagItemStr = fileTagArray.get(i).toString();
						if(fileTagItemStr.indexOf('-') > -1) {
							path = Config.ROOT + filepath.substring(0,filepath.lastIndexOf('.'))+"_"+fileTagItemStr+".jpg";
//								System.out.println("DEBUG deleteFile , " + DateUtil.getDateTimeForLog() + " , fileId : " + fileId + " , delete filePath=>" + path + " , contenttype=>" + String.valueOf(contenttype));
							insertCount++;
							JSONObject insert = new JSONObject();
							insert.put("number", insertCount);
							insert.put("fileId", fileId);
							insert.put("filePath", path);
							insert.put("deleteTime",DateUtil.getDateTimeForLog());
							deleteFileLog.deleteFileLogToDynamo(insert);
							s3Service.s3Client().deleteObject(new DeleteObjectRequest(Config.bucketName, path));
							//new File(path).delete();
						}else {
							if(contenttype == 2) {								
								
								JSONObject tags = new JSONObject(filedetail.get("tags").toString());

								int isCover = (tags.has(fileTagItemStr))?tags.getJSONObject(fileTagItemStr).getInt("isCover"):0;
								int page = (tags.has(fileTagItemStr))?tags.getJSONObject(fileTagItemStr).getInt("page"):-1;
								int pagecount = (filedetail.has("pagesize"))?filedetail.getInt("pagesize"):0;

								
//									System.out.println("DEBUG deleteFile , " + DateUtil.getDateTimeForLog() + " , fileId : " + fileId + " , contenttype=>" + String.valueOf(contenttype) + " , isCover=>" + String.valueOf(isCover) + " , page=>" + String.valueOf(page) + " , pagecount=>" + String.valueOf(pagecount) + " , tags=>" + tags.toString() + " , fileTag=>" + js.getJSONArray("fileTag").get(i).toString());								
								
								if(isCover == 0 && page == -1 && pagecount >0) {
									for(int j=1;j<=pagecount;j++) {
										path = Config.ROOT + filepath.substring(0,filepath.lastIndexOf('.'))+"_"+fileTagItemStr+"-"+j+".jpg";
										//System.out.println("path:"+path);
//											System.out.println("DEBUG deleteFile , " + DateUtil.getDateTimeForLog() + " , fileId : " + fileId + " , delete filePath=>" + path + " , contenttype=>" + String.valueOf(contenttype));
										insertCount++;
										JSONObject insert = new JSONObject();
										insert.put("number", insertCount);
										insert.put("fileId", fileId);
										insert.put("filePath", path);
										insert.put("deleteTime",DateUtil.getDateTimeForLog());
										deleteFileLog.deleteFileLogToDynamo(insert);
										s3Service.s3Client().deleteObject(new DeleteObjectRequest(Config.bucketName, path));
									}
								}
								else {
									path = Config.ROOT + filepath.substring(0,filepath.lastIndexOf('.'))+"_"+fileTagItemStr+".jpg";
//										System.out.println("DEBUG deleteFile , " + DateUtil.getDateTimeForLog() + " , fileId : " + fileId + " , delete filePath=>" + path + " , contenttype=>" + String.valueOf(contenttype));
									insertCount++;
									JSONObject insert = new JSONObject();
									insert.put("number", insertCount);
									insert.put("fileId", fileId);
									insert.put("filePath", path);
									insert.put("deleteTime",DateUtil.getDateTimeForLog());
									deleteFileLog.deleteFileLogToDynamo(insert);
									s3Service.s3Client().deleteObject(new DeleteObjectRequest(Config.bucketName, path));
								}
							}
							else {
								path = Config.ROOT + filepath.substring(0,filepath.lastIndexOf('.'))+"_"+fileTagItemStr+".jpg";
//									System.out.println("DEBUG deleteFile , " + DateUtil.getDateTimeForLog() + " , fileId : " + fileId + " , delete filePath=>" + path + " , contenttype=>" + String.valueOf(contenttype));
								insertCount++;
								JSONObject insert = new JSONObject();
								insert.put("number", insertCount);
								insert.put("fileId", fileId);
								insert.put("filePath", path);
								insert.put("deleteTime",DateUtil.getDateTimeForLog());
								deleteFileLog.deleteFileLogToDynamo(insert);
								s3Service.s3Client().deleteObject(new DeleteObjectRequest(Config.bucketName, path));
							}
						}
					}
				}else {									
					
					if(contenttype == 2) {
						int pagecount = (filedetail.has("pagesize"))?filedetail.getInt("pagesize"):1;
						
//							System.out.println("DEBUG deleteFile , " + DateUtil.getDateTimeForLog() + " , fileTag is empty and contenttype is '2' , pagecount=>" + pagecount + " , fileId : " + fileId + " , fileTag=>" + fileTag + " , delExtend=>" + delExtend);
						
						for(int j=1;j<=pagecount;j++) {
							path = Config.ROOT + filepath.substring(0,filepath.lastIndexOf('.'))+"-"+j+".jpg";
//								System.out.println("DEBUG deleteFile , " + DateUtil.getDateTimeForLog() + " , loop=>" + j + " , contentype is '2' , delete filePath=> " + path + " , fileId : " + fileId + " , fileTag=>" + fileTag + " , delExtend=>" + delExtend);
							insertCount++;
							JSONObject insert = new JSONObject();
							insert.put("number", insertCount);
							insert.put("fileId", fileId);
							insert.put("filePath", path);
							insert.put("deleteTime",DateUtil.getDateTimeForLog());
							deleteFileLog.deleteFileLogToDynamo(insert);
							s3Service.s3Client().deleteObject(new DeleteObjectRequest(Config.bucketName, path));
						}
						
					}
					
					//path = Config.ROOT + filepath.substring(0,filepath.lastIndexOf('.'));
					for (int j = 0; j < extensions.length(); j++) {
						//path = Config.ROOT + filepath.substring(0,filepath.lastIndexOf('.')) + "."+ extensions.getString(j);
//							System.out.println("DEBUG deleteFile , " + DateUtil.getDateTimeForLog() + " , extensions loop=> " + j + " , delete filePath=> " + path + " , fileId : " + fileId + " , fileTag=>" + fileTag + " , delExtend=>" + delExtend);
						insertCount++;
						JSONObject insert = new JSONObject();
						insert.put("number", insertCount);
						insert.put("fileId", fileId);
						//insert.put("filePath", path);
						insert.put("filePath", filepath);
						insert.put("deleteTime",DateUtil.getDateTimeForLog());
						deleteFileLog.deleteFileLogToDynamo(insert);
						s3Service.s3Client().deleteObject(new DeleteObjectRequest(Config.bucketName, filepath));
					}
					
				}
				
				rtn.put("txid", txid);
		        rtn.put("status","Success");
		        
		        if(contenttype == 3 || contenttype == 4) {
//			        	System.out.println("DEBUG deleteFile , " + DateUtil.getDateTimeForLog() + "addKey(fileId,disabled,1) , contenttype=>" + String.valueOf(contenttype) + "  , fileId : " + fileId + " , fileTag=>" + fileTag + " , delExtend=>" + delExtend);
		        	addKey(fileId,"disabled","1");
		        }
		        
		        //logger.info("rtn.toString() : " + rtn.toString());
		        traceLog.writeKinesisLog(trackId, caller, src, "deleteFile::End", rtn);	
		        return rtn.toString();
			}catch (Exception e1) {				
				//logger.error("fileId=>" + fileId + " , fileTag=>" + fileTag + " , delExtend=>" + delExtend, e1);
				throw new DocApplicationException(e1, 99);
				//return "{\"fileId\":\""+fileId+"\",\"status\":\"Fail\"}";
			}
		}else{
			throw new DocApplicationException("FileId is Null", 3);
		}
			
	}

	@Override
	public String discardFile(String fileId) throws DocApplicationException {
		Logger.info("Enter discardFile(), fileid => " + fileId);
		
		JSONObject rtn = new JSONObject();
		try{
			if(tools.isEmpty(fileId))
				throw new DocApplicationException("fileId is empty", 3);
				
			
			
			JSONObject filedetail = new JSONObject(getFileDetail(fileId, ""));
			
			if(tools.isEmpty(filedetail, "fileId"))
				throw new DocApplicationException("fileId not exists.", 13);
			
			
			String filepath = filedetail.getString("filePath");
			//String dirPath = FilenameUtils.getFullPath(filepath);
			String dirPath = tools.get_file_keypath(filepath);
			
			if(dirPath==null || "".equals(dirPath))
				throw new DocApplicationException("invalid filepath location.", 13);
			
			//dirPath =  + dirPath;
			S3Service s3Service = new S3Service();
			int fileCount = s3Service.deleteFolder(Config.bucketName, dirPath);
			
			addKey(fileId, "disabled", "1");
			
			String discardDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
			addKey(fileId, "discardDate", discardDate);
			
			return rtn.put("status", "Success").put("message", fileCount + " rows deleted.").toString();
			
		}
		catch(DocApplicationException e){
			throw e;
		}
		catch(Exception e){
			//Logger.error("discardFile error", e);
			throw new DocApplicationException("Delete file fail.", 15);
		}
	}

	@Override
	public String generateFileId(String extraNo,String contenttype,String isP ) throws DocApplicationException {
		//Logger.info("Enter generateFileId => " + jsonObj);
		
		try{
			//JSONObject paramObj = new JSONObject(jsonObj);
			
			//String extraNo = paramObj.optString("extraNo").trim();
			//String contenttype = paramObj.optString("contenttype").trim();
			//String isP = paramObj.optString("isP").trim();

			if(!tools.isEmpty(extraNo)){
				//FileManageDispatch fmupdate = new FileManageDispatch();
				DynamoService dynamoService = new DynamoService();
				JSONObject uploadConfig = new JSONObject(
						dynamoService.getUploadByExtraNo(Config.uploadConfig, extraNo));
				
				//JSONObject uploadConfig = fm.findConfig(_extraNo);
				
				if(uploadConfig.length()!=0){
					contenttype = uploadConfig.getString("contenttype");
					JSONObject itemExtra = uploadConfig.getJSONObject("extra");
					isP = itemExtra.has("isP") ? itemExtra.get("isP").toString() : "0";
				}
				else{
					throw new DocApplicationException("no config for extraNo [" + extraNo + "]",13 );
				}
					
			}
			else if(tools.isEmpty(contenttype)){
				throw new DocApplicationException("must provide extraNo or contenttype.",13 );
			}
			
			if(tools.isEmpty(isP))
				isP = "0";
			
			if(!isP.matches("[01]") || !contenttype.matches("[1-5]"))
				throw new DocApplicationException("invalid contenttype or isP format.",13 );
			
			String fileId = UUID.randomUUID().toString().replaceAll("-", "") + isP + contenttype;

			return new JSONObject().put("status", "success").put("fileId", fileId).toString(); 
		}
		catch(JSONException e){
			Logger.error("generateFileId Error", e);
			throw new DocApplicationException("JSON format error",16 );
		}
	}

	@Override
	public String getCheck() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getFile(String Param) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @method getFileDetail
	 * @purpose 取得單一檔案
	 * @param fileid
	 * @param tag
	 * @return String
	 * @throws DocApplicationException
	 */
	@Override
	public String getFileDetail(String fileId, String tag) throws DocApplicationException{
		getHeaderValue();
		if(tools.isEmpty(fileId)){
			//logger.error("fileId is empty, fileId=>" + fileId + " , tag=>" + tag);
			//return new JSONObject().put("error", "fileId is empty.").toString();
			throw new DocApplicationException("fileId is empty.", 1);
		}
		src = "docapi::core::getFileDetail::";	
		if(tag.equals("") || tag == null) tag = "";
		JSONObject rtn = new JSONObject();
		try{
				
			traceLog.writeKinesisLog(trackId, caller, src, "getFileDetail::Start", rtn);
			DynamoService dynamoService = new DynamoService();
			rtn = new JSONObject(dynamoService.getItem(Config.document, fileId));
			
			/*
			rtn = (new JSONArray(dynamoService.getItem("users", fileId))).getJSONObject(0);
			rtn.put("txid", tools.generateTxid());
			rtn.put("status", "Success");
			rtn.put("filePath", tools.generateFilePathForMount(rtn.getString("filepath")));
			rtn.put("fileId", rtn.getString("fileid"));
			rtn.put("fileName", rtn.getString("filename"));
			rtn.put("insertDate", rtn.getString("insertdate"));
			rtn.put("contentType", rtn.get("contenttype"));
			rtn.put("pid", rtn.getString("pid"));
			if(rtn.has("pagesize")) rtn.put("pageSize", rtn.getString("pagesize"));
			if(rtn.has("convert")) rtn.put("convert", rtn.getString("convert"));
			*/
			if(rtn.has("source")) rtn.put("source", rtn.getString("source"));
			else rtn.put("source", "");
			
			// detect tags property, if not exists, create it.
			if(!rtn.has("tags"))
				rtn.put("tags", new JSONObject());
			
		}catch (Exception e1) {
			//logger.error("fileId=>" + fileId + " , tag=>" + tag, e1);
			throw new DocApplicationException(e1, 99); 
		}
		traceLog.writeKinesisLog(trackId, caller, src, "getFileDetail::End", rtn);
		return rtn.toString();
	}

	@Override
	public String getFileList(String Param) {
		// TODO Auto-generated method stub
		return null;
	}

	

	@Override
	public String getQueueLength() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getVersion() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String putfile(Signature jsonData) throws DocApplicationException{
		getHeaderValue();
		
		JSONObject rtn = new JSONObject();
		JSONObject paramObj = new JSONObject();
		JSONObject extra = new JSONObject();
		src = "docapi::core::putfile::";
		try {
		//paramVal is {"apnum":"10400","pid":"10400","content-type":"image/jpeg","Content_Disposition":"123.jpg","extra":{"ectraNo":"111-222-333"},"isP":1, "title":"測試","description":"測試"}
		//paramObj = new JSONObject(this.decryptParam(jsonData));
			paramObj.put("apnum", jsonData.getApnum());
			paramObj.put("pid", jsonData.getPid());
			paramObj.put("Content_Disposition", jsonData.getContentDisposition());
			
			extra.put("convert", jsonData.getExtra().getConvert());
			extra.put("extraNo", jsonData.getExtra().getExtraNo());
			extra.put("multiAction", jsonData.getExtra().getMultiAction());
			extra.put("videoImageSize", jsonData.getExtra().getVideoImageSize());
			paramObj.put("extra", extra);
			
			
			paramObj.put("isP", jsonData.getIsP());
			paramObj.put("contenttype", jsonData.getContenttype());
			paramObj.put("title", jsonData.getTitle());
			paramObj.put("description", jsonData.getDescription());
			
			traceLog.writeKinesisLog(trackId, caller, src, "putfile::Start", paramObj);
		//確認必填欄位
			
		if (!paramObj.has("apnum") || "".equals(paramObj.getString("apnum")) ||
			!paramObj.has("pid") || "".equals(paramObj.getInt("pid")) ||
			!paramObj.has("Content_Disposition") || "".equals(paramObj.getString("Content_Disposition")) ||
		    !paramObj.has("extra") || "".equals(paramObj.getJSONObject("extra")) ||
		    !paramObj.has("isP") || "".equals(paramObj.getInt("isP")) ||
		    !paramObj.has("contenttype") || "".equals(paramObj.getString("contenttype")) ||
		    !paramObj.has("title") || "".equals(paramObj.getString("title")) ||
		    !paramObj.has("description") || "".equals(paramObj.getString("description")))
			throw new DocApplicationException("Empty parameter",3);//erroehandler 必填欄位未填

		String apNum = paramObj.getString("apnum");
		String pid = paramObj.getString("pid");
		String fileName = paramObj.getString("Content_Disposition");
		int isP = paramObj.getInt("isP");
		int contentType = tools.getContentType(paramObj.getString("contenttype"));
		JSONObject extra_json = paramObj.getJSONObject("extra");
		String title = paramObj.getString("title");
		String description = paramObj.getString("description");
		
		
		if(extra_json.has("expireTimestamp") && extra_json.optLong("expireTimestamp") == 0)
			throw new DocApplicationException("NotValid;expireTimestamp shoule be a long type",2);
		
		String extraNo = extra_json.has("extraNo") ? extra_json.getString("extraNo").trim() : "";				
		
		//實體檔案路
		String txid = tools.generateTxid();
		String status = "";
		String filepath_forS3 ="";
		
		// 2014/09/26 檢查 extra 中是否有帶入 fileId, 若不存在才自行建立.
		String fileId = null;
		//JSONObject extraJson = new JSONObject(jsonObj);
		if(extra_json.has("fileId")){
			fileId = extra_json.getString("fileId");
			Logger.info("use fileid passed from frontend => " + fileId);
			
			// check fileId is not in use.
			DynamoService dynamoService = new DynamoService();
			JSONObject user = new JSONObject( dynamoService.getItem(Config.document, fileId));
			
			if(user.length()<=0){
				Logger.info("fileid not in use, check passed.");
			}
			else{
				Logger.error("provided fileid is in use. => " + fileId);
				throw new DocApplicationException("NotValid;provided fileid is in use",2);			
			}
			
		}else{
		    fileId = tools.generateFileId(contentType,paramObj.getInt("isP"));
			Logger.info("create new fileid => " + fileId);
		}


		//filePath產生檔案位置
		String filepath = tools.generateFilePath(fileId);
		
		//long time1 = 0L ;
		//NumberFormat nf = NumberFormat.getInstance();
		//nf.setMaximumFractionDigits(5);
		//判斷filename是否為null or 空值, 如filename有資料則進行檔案存檔
        if (fileName != null && !"".equals(fileName)) {
        	//Db內串出filepath&fileName
			filepath_forS3 = filepath + fileId + fileName.substring(fileName.lastIndexOf("."),fileName.length()).toLowerCase();
			status = "Success";
        }
        
        if(status.equals("Success")){
        	
        	String now = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
        	
        	// filepath_forMount => /104plus/xxx/xxx/xxx/fileid.ext
        	//String filepath_forMount = tools.generateFilePath(fileid)+fileid + fileName.substring(fileName.lastIndexOf("."),fileName.length()).toLowerCase();
        	//filepath_forMount = tools.generateFilePathForMount(filepath_forMount);
        	
			/*
			* 基本上, 最終轉檔狀態還是寫在 'users' collection, 此處僅是 trigger & job 在讀取的參照檔
			* 以避免取狀態時, 要檢查兩個 collection 的效率問題.
			* 
			* convert item 具有 priority 是為了工作相依性, 在排序後能夠執行.
        	*/
        	// 將轉檔工作轉換成  json tasks object.
        	JSONObject convert = new JSONObject();
        	convert.put("fileId", fileId);
        	convert.put("contentType", contentType);
        	convert.put("apnum", apNum);
        	convert.put("filePath", filepath_forS3); 
        	convert.put("insertDate", now);
        	convert.put("triggerDate", now);
        	convert.put("status", new JSONObject());		// 預先建立轉檔狀態欄位.
        	convert.put("convertLists", new JSONObject());
        	JSONArray convertItems = new JSONArray();
        	
        	convert.put("convertItems", convertItems);		// 預先建立轉檔項目欄位.
        	
        	
        	JSONObject videoQualityObj = null;
        	// add quality property for video type.
        	if(contentType == ContentType.Video || contentType == ContentType.WbVideo){

        		JSONArray videoQuality = extra_json.has("videoQuality")? extra_json.getJSONArray("videoQuality") : new JSONArray();
        		
        		Logger.info("put video and videoQuality is => " + videoQuality);
        		
        		if(videoQuality.length() == 0){
        			videoQuality.put("480p");
        			Logger.info("putFile no specify videoQuality, set default quality [480p].");
        		}
        		
        		videoQualityObj = new JSONObject();
        		
        		for(int i=0; i<videoQuality.length(); i++){
        			String quality = videoQuality.getString(i);
        			if(quality.equals("480p") || quality.equals("720p")){
        				videoQualityObj.put(quality, new JSONObject().put("status", "pending"));
        			}		        				
        		}		        
        		
        		convert.put("videoQuality", videoQualityObj);
        	}
        	
        	
        	// -- 之後若支援 multiAction 以外的轉檔類型時, 每個種類都需要加上 order, 
        	// -- 因 tag 有相依性, 在 job 針對 order 進行 sorting 之後才依序 convert.
        	
        	
        	JSONArray maArray = null;
        	JSONArray syncActions = new JSONArray();		// 立即轉檔的項目
        	JSONArray asyncActions = new JSONArray();		// 不需立即轉檔的項目
        	//TODO Johnson做法改變，以往單點與套餐2選一，如今可以混用
        	/*
        	if(extra_json.has("multiAction") && !tools.isEmpty(extra_json.getString("multiAction"))){
        		JSONObject maConvert = new JSONObject();
        		maConvert.put("itemName", "maConvert");			// itemName 用以識別轉檔項目
        		
        		//maConvert.put("priority", 60);
        		maArray = extra_json.getJSONArray("multiAction");
        		
        		// 若有提供 extraNo 則只保存 extraNo.
        		if(!tools.isEmpty(extraNo))
        			maConvert.put("extraNo", extraNo);
        		else
        			maConvert.put("multiAction", maArray);	        			
    			
    			convertItems.put(maConvert.toString());
    			
        	}		 */    
        	JSONObject maConvert = new JSONObject();
        	JSONObject convertList = new JSONObject();
        	
    		maConvert.put("itemName", "maConvert");		
    		//System.out.println(extra_json.getJSONArray("multiAction"));
        	if(extra_json.has("multiAction") && !tools.isEmpty(extra_json.getJSONArray("multiAction").toString())){
        		maArray = extra_json.getJSONArray("multiAction");
        		maConvert.put("multiAction", maArray);
        		convertList.put("multiAction", maArray.toString());
        	}
        	
        	if(!"".equals(extraNo)){
        		maConvert.put("extraNo", extraNo);
        		convertList.put("extraNo", extraNo.toString());
        	}
        	convert.put("convertLists",convertList);
        	convertItems.put(maConvert);
        	
        	// 若上傳的檔案類型為圖片, 因支援同步、非同步轉檔參數, // --預先分析轉檔請求相依性. 
        	// (相依性可能因多層 parent, 分析複雜, 基於上傳效率及程式精簡, 還是要求於前期上傳時的參數就要正確設置.)
        	if(contentType == ContentType.Image && maArray != null){			        	
        		
    			Logger.info("file is image type, start to classify sync & async multiAction..");
    			
	        	// 將 multi action 參數進行分類.
	        	for(int i=0; i<maArray.length(); i++){
	        		JSONObject action = maArray.getJSONObject(i);
	        		// if(action.has("async") && action.get("async").toString().equals("true")){
	        		if(action.has("async") && action.getBoolean("async")){
	        			// 非同步不需紀錄, return 前再 trigger 轉檔即可.
	        			asyncActions.put(action);	// 開發 debug 階段檢視資訊用, 後續可以拿掉.
	        		}
	        		else
	        		{
	        			syncActions.put(action);	// 預設均為同步
	        		}
	        	}
	        	Logger.info("analize sync/async image convert type: fileid => " + fileId + ", total => " + maArray.length() + ", sync => " + syncActions.length() + ", async => " + asyncActions.length());		        				        		
        	}
        	else{
    			Logger.info("putFile target is not type of image or without multiAction param.");
    		}
        	
        	// String db_filepath = "";
        	JSONObject insert = new JSONObject();
    		insert.put("pid", pid);
    		insert.put("fileId", fileId);
    		insert.put("contentType", contentType);
    		insert.put("filename", fileName);
    		//modify by JasonHsiao on 2013-07-29 , 附檔名轉小寫
    		//2014-01-09 fix for generateFilePath don't use parma contentType
//	    		db_filepath = tools.generateFilePath(fileid)+fileid + fileName.substring(fileName.lastIndexOf("."),fileName.length()).toLowerCase();
    		insert.put("filePath", filepath_forS3);   		
    		insert.put("apnum", apNum);
    		insert.put("title", title);
    		insert.put("description", description);
    		insert.put("insertDate", now);
    		insert.put("imgStatus","pending");
    		
    		if(contentType == ContentType.Video || contentType == ContentType.WbVideo)
    			insert.put("videoQuality", videoQualityObj);
    		
    		//modify by JasonHsiao on 2013-09-09 , set default convert value to 'pending'
    		//modify by JJ on 2014-02-06, set image convert = success
    		// if ( contentType == 1) {
    		if ( contentType == 1 && asyncActions.length() == 0) {
    			insert.put("convert","success");
        	} else {
        		insert.put("convert","pending");
    		}

    		//modify by JasonHsiao on 2013-09-14 , add column isP => 型態int
    		insert.put("isP", isP);
    		
    		
			
			if(extra_json.has("source")) insert.put("source",extra_json.getString("source"));

        	//FileManageDispatch fmd = new FileManageDispatch();
			//String insertUsersResult = fmd.fileInsert(insert,"users");
        	DynamoUsers dynamoUsers = new DynamoUsers();
        	dynamoUsers.setTrackId(trackId);
        	dynamoUsers.setCaller(caller);	
			if ("".equals(dynamoUsers.insertUsersToDynamo(insert)))
				throw new DocApplicationException("insert data fail", 12);
			
        	///Dynamo回傳確認
        	//if(!isEmpty(insertUsersResult) && insertUsersResult.equals("500"))
        	//	throw new Exception("ERROR FileManage putFile , " + DateUtil.getDateTimeForLog() + " , fileInsert to 'users' collection return 500 String, insertObj is=>" + insert.toString());
			     
			DynamoConvert dynamoconvert = new DynamoConvert();
			dynamoconvert.insertDynamo(convert);
			//String insertConvertResult = fmd.fileInsert(convert,"convert");
        	//modify by JasonHsiao on 2013-06-26 for return String "500" handle
			//if(!isEmpty(insertConvertResult) && insertConvertResult.equals("500"))
			//	throw new Exception("ERROR FileManage putFile , " + DateUtil.getDateTimeForLog() + " , fileInsert to 'convert' collection return 500 String, insertObj is=>" + convert.toString());
			
			
			Logger.info("convert info inserted.");
        						
    		//System.out.println("DEBUG FileManage putFile , " + DateUtil.getDateTimeForLog() + " , complete insert to MongoDB , insert data=>" + insert.toString() + " , extra_json=>" + extra_json.toString());
    		
    		//FileConvert fc = new FileConvert();
    		//TODO Johnson未來要回來加上這行，因為現在無法執行ffmpage
			ImageProcess ir = new ImageProcess();
    		
    		//QueueService qs = new QueueService();
	        
	        switch(contentType){
		       
		        case ContentType.Doc:	//是否需要文轉檔
		        	//modify by JasonHsiao on 2013-08-13 , change DocToPDF , pdfToImg , multiAction to queue , run in jar
		        	if(extra_json.has("convert")&& extra_json.getString("convert").equals("true")){
		        		
		        		//System.out.println("DEBUG FileManage putFile , " + DateUtil.getDateTimeForLog() + " , contenttype is doc and convert is true , start execute convert fileId=>" + fileid + " , extra_json=>" + extra_json.toString());
		        		 
		        		//insert data to docConvert table for jar to execute docConvertToPdf,multiAction,pdfToImg
		        		JSONObject insertDocConvert = new JSONObject();
		        		insertDocConvert.put("txid", txid);
		        		insertDocConvert.put("fileId", fileId);
		        		insertDocConvert.put("filePath", filepath_forS3);
		        		insertDocConvert.put("docToPdf", "pending");
		        		insertDocConvert.put("doMultiAction", "pending");
		        		insertDocConvert.put("pdfToImg", "pending");
		        		//modify by JasonHsiao on 2013-09-03 , add doDocumentImageSize column in docConvert collection
		        		insertDocConvert.put("doDocumentImageSize", "pending");
//			        		insertDocConvert.put("method", "putFile");
		        		
		        		if(extra_json.has("pdfOnly") && extra_json.getBoolean("pdfOnly")){
		        			insertDocConvert.put("pdfOnly", "true");
		        			insertDocConvert.put("method", "doc2Pdf");
		        			Logger.info("========= pdfOnly =========");
		        		}
		        		else{
		        			insertDocConvert.put("pdfOnly", "false");
		        			insertDocConvert.put("method", "putFile");
		        		}

		        		if(extra_json.has("multiAction") && !"".equals(extra_json.getJSONArray("multiAction"))) {				        			
		        			insertDocConvert.put("multiAction", extra_json.getJSONArray("multiAction"));				        		
		        		}else{
		        			insertDocConvert.put("multiAction", "");
		        		}

		        		if(extra_json.has("documentImageSize")) {
		        			JSONArray documentImageSizeArray = extra_json.getJSONArray("documentImageSize");
		        			insertDocConvert.put("documentImageSize", documentImageSizeArray);
		        		}else{
		        			insertDocConvert.put("documentImageSize", "");
		        		}
		        		
		        		//insert to 'docConvert' collection
		        		//System.out.println("DEBUG FileManage putFile , " + DateUtil.getDateTimeForLog() + " , Before insert 'docConvert' collection =>" + insertDocConvert.toString());
		        		//String insertResponse = fmd.fileInsert(insertDocConvert, "docConvert");
		        		//System.out.println("DEBUG FileManage putFile , " + DateUtil.getDateTimeForLog() + " , After insert 'docConvert' collection =>" + insertDocConvert.toString() + " , response=>" + insertResponse);
						
		        		
		        		//if(insertResponse != null && !"".equals(insertResponse)){
						//	if("500".equals(insertResponse)){										
						//		throw new Exception("ERROR FileManage putFile , " + DateUtil.getDateTimeForLog() + " , fileInsert to 'docConvert' collection return 500 String , start throw MongoDB Exception , insert query is=>" + insertDocConvert.toString());
						//	}				
						//}
		        		
						//saveToQueue docConvertToPdf
		        		// QueueService qs = new QueueService();
		        		JSONObject toPdf_json = new JSONObject();
		        		toPdf_json.put("txid", txid);
		        		//System.out.println("DEBUG FileManage putFile , " + DateUtil.getDateTimeForLog() + " , Before saveToQueue toPdf_json=>" + toPdf_json.toString() + " , groupName=>docConvertToPdf");
		        		// String saveToQueueResult = qs.saveToQueue(toPdf_json.toString(), "docConvertToPdf");
		        		
		        	}
		        	break;
		        case ContentType.Video:	//是否需要影片轉檔
		        	
		        	if(extra_json.has("convert") && extra_json.getString("convert").equals("true")){
		        		//System.out.println("DEBUG FileManage putFile , " + DateUtil.getDateTimeForLog() + " ,  ENTER VIDEO CONVERT ,fileid=>" + fileid + " , extra_json=>" + extra_json);				        		
		        		//convertVideo(fileid,extra);	
		        	}
		        	break;
		        case ContentType.WbVideo:	//是否需要影片轉檔
		        	/*暫時Pass
		        	if(extra_json.has("convert") && extra_json.getString("convert").equals("true")){
		        		//System.out.println("DEBUG FileManage putFile , " + DateUtil.getDateTimeForLog() + " ,  ENTER WbVideo CONVERT ,fileid=>" + fileid + " , extra_json=>" + extra_json);				        						        		
							convertVideo(fileid,extra_json.toString());
		        	}*/
		        	break;
		        case ContentType.Audio:
		        	/*暫時Pass
		        	if(extra_json.has("convert") && extra_json.getString("convert").equals("true")){				        		
		        		//System.out.println("DEBUG FileManage putFile , " + DateUtil.getDateTimeForLog() + " ,  ENTER Audio CONVERT ,fileid=>" + fileid + " , extra_json=>" + extra_json);				        		
		        		fc.audioConvert(fileid);
		        	}*/
		        	break;			        
		        case ContentType.WbAudio:
		        	/*暫時Pass
		        	if(extra_json.has("convert") && extra_json.getString("convert").equals("true")){				        		
		        		//System.out.println("DEBUG FileManage putFile , " + DateUtil.getDateTimeForLog() + " ,  ENTER WbAudio CONVERT ,fileid=>" + fileid + " , extra_json=>" + extra_json);			        		
		        		fc.audioConvert(fileid);
		        	}*/
		        	break;
		        }
		        if(contentType!= ContentType.Doc){
			        if(extra_json.has("multiAction")&& !"".equals(extra_json.getJSONArray("multiAction").toString())) {					        	
			        	//System.out.println("DEBUG FileManage putFile , " + DateUtil.getDateTimeForLog() + " ,  ENTER contentType!= ContentType.Doc ,fileid=>" + fileid + " , multiAction=>" + extra_json.getString("multiAction") + " , extra_json=>" + extra_json);
			        	
//				        	//rtn.put("url", new JSONObject(ir.multiAction(fileid, extra_json.getString("multiAction"))));
			        	//rtn.put("url", new JSONObject(ir.multiAction(fileid, syncActions.toString())));
			        	
			        	String multiAction = syncActions.length() > 0 ? syncActions.toString() : extra_json.getJSONArray("multiAction").toString();
			        	rtn.put("url", new JSONObject(ir.multiAction(fileId, multiAction)));
			        	Logger.info(multiAction.length() + " sync multiAction items processed => " + multiAction.toString());
			        	
			        	// rtn.put("url", new JSONObject(ir.multiAction(fileid, syncActions.toString())));
//				        	rtn.put("url", new JSONObject(ir.multiAction(fileid, syncActions.toString())));
//				        	logger.info(syncActions.length() + " sync multiAction items processed => " + syncActions.toString());
			        	
			        	if(asyncActions.length() > 0){
			        		
			        		// maConvert
				        	JSONObject queueItem = new JSONObject();
				        	Logger.info("put " + asyncActions.length() + " async multiAction items to queue 'maConvert' => " + asyncActions.toString());
				        	/*
				        	{
				        		 *   fileId:'fileId',
				        		 *   tags:['xxx','xxx'],     // 指定要轉檔的 JSONArray tags 清單, 空值(預設)為全部重新轉檔. 
				        		 *   includeSuccess          // 己成功的 tag 是否需要重新轉檔, 預設 false
				        		 * }
				        	*/
				        	
				        	queueItem.put("fileId", fileId);
//					        	String saveToQueueResult = qs.saveToQueue(queueItem.toString(), "maConvert");
				        	//TODO Johnson 送Queue步驟待確認，是否還需要
				        	/*String saveToQueueResult = qs.saveToQueue(queueItem.toString(), Config.QName_MA); 
				        	if(saveToQueueResult != null && !"".equals(saveToQueueResult) && saveToQueueResult.indexOf("Exception") > -1){
				        		Logger.error("async saveToqueue Error => " + saveToQueueResult + ", fileId => " + fileid);
			        		}
				        	else{
				        		Logger.info("async queue result => " + saveToQueueResult);
				        	}*/
			        	}
			        }
		        }
		        
		        if(extra_json.has("expireTimestamp")){
		        	String expireTimestamp = String.valueOf(extra_json.getLong("expireTimestamp"));
		        	Logger.info("putFile with expireTimestamp: " + expireTimestamp);
		        	this.setExpireTimestamp(fileId, expireTimestamp);
		        }
		        			        
		        //回傳值
	        	rtn.put("fileId", fileId);
	        	rtn.put("fileName", fileName);
	        	rtn.put("filePath",filepath_forS3);
	        	rtn.put("contenttype", tools.getContentType(contentType));
	        	//rtn.put("esbtime",time1+"");
        }
        
		
		}catch(JSONException e){
			throw new DocApplicationException("Json format error",3);//erroehandler 必填欄位未填
		}catch (DecoderException e) {
			throw new DocApplicationException("DB operation failed",13);
		}catch(DocApplicationException e){
			throw e;
		}catch(Exception e){
			throw new DocApplicationException("Service Error", 99);
		} 

		
		traceLog.writeKinesisLog(trackId, caller, src, "putfile::End", rtn);
		return rtn.toString();
	}

	@Override
	public String removeKey(String fileId, String key)throws DocApplicationException {
		JSONObject rtn = new JSONObject();
		try{
		DynamoService dynamoService = new DynamoService();
		dynamoService.deleteAttribute(Config.document, fileId, key);
		
		rtn.put("txid", tools.generateTxid());
		rtn.put("status", "Success");
		} catch (Exception e) {
			throw new DocApplicationException("FileId or key is not exist", 12);
			//System.out.println("com.e104.DocumentManagement removeKey("+fileid+","+key+") Exception : "+e);
		}
		return rtn.toString();
	}
	
	/**
	 * set the expire date of this fileid. 
	 * if a fileid expired, all its own files will be deleted by AP. 
	 * @param fileId
	 * @param timestamp
	 * millisecond since 1970/01/01 00:00:00
	 * @return
	 * @throws JSONException
	 */
	@Override
	public String setExpireTimestamp(String fileId,String timestamp) {
			Logger.info("Enter setExpireTimestamp(), fileid => " + fileId + ", timestamp => " + timestamp);
			
			JSONObject rtn = new JSONObject();
			try{
				if(tools.isEmpty(fileId) || tools.isEmpty(timestamp)){
					Logger.error("fileId is empty.");
					return rtn.put("status", "fail").put("error", "fileId or timestamp is empty.").toString();
				}
				
				Date expireDate;
				try{
					expireDate = new Date(Long.parseLong(timestamp)*1000);
				}
				catch(NumberFormatException e){
					Logger.error("invalid timestamp format", e);
					return rtn.put("status", "fail").put("error", "invalid timestamp format.").toString();
				}
				
				String expireDateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(expireDate);
				
				Logger.info("add expireTimestamp for " + fileId + " > " + timestamp + "(" + expireDateStr +")");
				addKey(fileId, "expireTimestamp", timestamp);
				
				return rtn.put("status", "success").put("message", "until " + expireDateStr).toString();
			}
			catch(Exception e){
				Logger.error("setExpireTimestamp fail", e);
				return rtn.put("status", "fail").put("error", "setExpireTimestamp fail.").toString();
			}
		
	}
	/**
	 * @method updateFile
	 * @purpose 更新檔案desc資訊
	 * @param fileid
	 * @param desc
	 * @return String
	 * @throws DocApplicationException 
	 */
	@Override
	public String updateFile(UpdateFile jsonObjData) throws DocApplicationException {
		getHeaderValue();	
		src = "docapi::core::updateFile::";
		JSONObject rtn = new JSONObject();
		try{	
			traceLog.writeKinesisLog(trackId, caller, src, "updateFile::Start", rtn);	
			Map<String, String> update =new  HashMap<String, String>();
			update.put("title", jsonObjData.getTitle());
			update.put("description", jsonObjData.getDescription());
			
			//execute update
			new DynamoService().updateItem(Config.document, jsonObjData.getFileId(), update);
			
			traceLog.writeKinesisLog(trackId, caller, src, "updateFile::End", rtn);
			rtn.put("txid", tools.generateTxid());
			rtn.put("status", "Success");
		}catch (Exception e1) {
			//System.out.println("com.e104.DocumentManagement updateFile("+fileId+","+description+") Exception : "+e1);
			throw new DocApplicationException(e1, 99);
		}
			return rtn.toString();
	
	}

	@Override
	public String doc2img(String Param) {
		// TODO Auto-generated method stub
		return null;
	}
	/**
	 * @method getStatus
	 * @purpose 取得轉檔status (文轉圖, 影片轉檔)
	 * @param fileId
	 * @return String {"progress":"100","status":"Success","maUrl":""}	
	 */
	@Override
	public String getStatus(String fileId) {
		
		//public String getStatus(String fileId) {
			
		if(fileId != null && !"".equals(fileId.trim())){
			
			//logger.info("ENTER FileCOnvert getStatus() " + DateUtil.getDateTimeForLog() + " , fileId=>" + fileId);
			
			try{
				JSONObject rtn = new JSONObject();
				//FileManage fm = new FileManage();
				
				DynamoService dynamoService = new DynamoService();
				
				
				JSONObject filedetail = new JSONObject(dynamoService.getItem(Config.document, fileId));
				int contenttype = 0;
				if(filedetail.has("contenttype")){
					contenttype = filedetail.getInt("contenttype");
				}else{
					rtn.put("progress", "0");
					rtn.put("status", "not found");
					rtn.put("maUrl", "");
					return rtn.toString();
				}
				
				String progress = "";
				String maUrl = "";
				
				
				progress = String.valueOf(filedetail.getInt("progress"));
				try {
					if(contenttype >1 ){
						//contenttype doc got to mongoDB get multiAction url from collection 'docConvert'
						if(contenttype == 2){
							//contenttype == 2
							//2013-08-26 modify by JasonHsiao,contenttype == 2 , mc key is 'doc:{fileId}'
							//progress = getConvertStatusFromMC("doc:" + fileId);
							//TODO Doc先跳過
							/*
							if((progress != null) && (!"".equals(progress)) && (Integer.valueOf(progress) >= 15)){
								//System.out.println("DEBUG FileCOnvert getStatus() " + DateUtil.getDateTimeForLog() + " , contenttype is 2 and progress >= 15 , start get 'maUrl' value from collection 'docConvert' fileId=>" + fileId + " , progress=>" + progress);
								FileManageDispatch fmd = new FileManageDispatch();
								JSONObject jasonQuery = new JSONObject();
								jasonQuery.put("fileid", fileId);
								String fileSelectResult = fmd.fileSelect(jasonQuery,"docConvert");								
								
								if(fileSelectResult != null && !"".equals(fileSelectResult)){
									if("500".equals(fileSelectResult)){
										logger.error("ERROR FileCOnvert getStatus() , " +  DateUtil.getDateTimeForLog() + " response from mongoDB is '500' , query=>" + jasonQuery.toString() + " , tableName=>docConvert");
										throw new Exception("ERROR FileCOnvert getStatus() , " +  DateUtil.getDateTimeForLog() + " response from mongoDB is '500' , query=>" + jasonQuery.toString() + " , tableName=>docConvert");
									}else{
										
										//got data from mongoDB , set maUrl
										JSONArray jsonArray = new JSONArray(fileSelectResult);
										if(jsonArray.getJSONObject(0).has("maUrl")){
											maUrl = jsonArray.getJSONObject(0).get("maUrl").toString();
											//System.out.println("DEBUG FileCOnvert getStatus() " + DateUtil.getDateTimeForLog() + " , has 'maUrl' key from collection 'docConvert' fileId=>" + fileId + " , progress=>" + progress + " , result=>" + fileSelectResult);
										}else{
											maUrl = "";
											//System.out.println("DEBUG FileCOnvert getStatus() " + DateUtil.getDateTimeForLog() + " , no 'maUrl' key from collection 'docConvert' fileId=>" + fileId + " , progress=>" + progress + " , result=>" + fileSelectResult);
										}
									}									
								}else{
									logger.error("ERROR FileCOnvert getStatus() " + DateUtil.getDateTimeForLog() + " , 'maUrl' value from collection 'docConvert' is null fileId=>" + fileId + " , progress=>" + progress);
								}
								
							}else{
							//	logger.info("DEBUG FileCOnvert getStatus() " + DateUtil.getDateTimeForLog() + " , contenttype is 2 and progress < 15 , return progress fileId=>" + fileId + " , progress=>" + progress);
							}*/
						}else{
							//contenttype > 1 and not 2
							//progress = getConvertStatusFromMC(fileId);
							if(progress == null){
								progress = "";
							}
							//System.out.println("DEBUG FileCOnvert getStatus() " + DateUtil.getDateTimeForLog() + " , contenttype > 1 and is not 2 , got result progress fileId=>" + fileId + " , progress=>" + progress);
						}
					}else{
						//System.out.println("DEBUG FileCOnvert getStatus() " + DateUtil.getDateTimeForLog() + " , contenttype is 1 set progress to 100 , fileId=>" + fileId + " , progress=>" + progress);
						progress = "100"; 
					}
				} catch (Exception e) {
					//logger.error("ERROR FileCOnvert getStatus() " + DateUtil.getDateTimeForLog() + " , error when execute getConvertStatus() , fileId=>" + fileId + " , " + e.toString());
					e.printStackTrace();
				}	
			
				rtn.put("status", "Success");
				rtn.put("maUrl", maUrl);
				
				if(progress==null || progress.equals("")){
					//System.out.println("DEBUG FileCOnvert getStatus() " + DateUtil.getDateTimeForLog() + " , progress is null or empty , fileId=>" + fileId);
					if(filedetail.getString("convert").equalsIgnoreCase("success")){
						progress="100";
						//System.out.println("DEBUG FileCOnvert getStatus() " + DateUtil.getDateTimeForLog() + " , progress is null or empty , filedetail 'convert' is 'success' , progress set to 100 , fileId=>" + fileId);
					}else{
						progress= "-1" ;
						//System.out.println("DEBUG FileCOnvert getStatus() " + DateUtil.getDateTimeForLog() + " , progress is null or empty , filedetail 'convert' is not 'success' , progress set to -1 , fileId=>" + fileId);
					}
				}
				
				rtn.put("progress", progress);
				rtn.put("maUrl", maUrl);
				/* 2013-11-15 fix return error tag status -> Fail. */
				if ( Integer.parseInt(progress) < 0 ) {
					rtn.put("status", "Fail");
				} else {
					rtn.put("status", "Success");
				}
				
				//logger.info("EXIT FileConvert getStatus() " + DateUtil.getDateTimeForLog() + " , fileId=>" + fileId + " , return=>" + rtn.toString());
				
				return rtn.toString();
			}catch(Exception e){
				//logger.error("ERROR FileConvert getStatus() " + DateUtil.getDateTimeForLog() + " , caught Exception " + e.toString());
				e.printStackTrace();
				
				//log4j
		    	java.io.StringWriter sw1 = new java.io.StringWriter();
				java.io.PrintWriter pw1 = new java.io.PrintWriter(sw1);
				e.printStackTrace(pw1);	
				//logger.error(sw1.getBuffer().toString());
				
				return "{\"error\":\"getStatus Exception\"}";
			}
		}else{
			//logger.error("ERROR FileCOnvert getStatus() " + DateUtil.getDateTimeForLog() + " , empty parameter fileId");
			return "{\"error\":\"empty parameter fileId\"}";
		}
			
		
	}

	@Override
	public String setConvertStatus(String Param) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String updateData(String Param) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String videoConvert(String Param) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String audioConvert(String Param) {
		// TODO Auto-generated method stub
		return null;
	}

	//doing##############################################################3
		@Override
		public String getFileUrlnoRedis(String Object) throws DocApplicationException {
			
			JSONArray rtn = new JSONArray(); //回傳的JSONArray
			tools tools = new tools();
			//String jsonObject = tools.decode(Object);
			
			DynamoService dynamoService = new DynamoService();
			
			String returnStr ="";
			
			try{
				JSONObject jsonObj = new JSONObject(Object);
				System.out.println("ok"+jsonObj.getJSONArray("getFileArr").toString());
			
			
			if((Object != null && !"".equals(Object.trim())) && 
				(jsonObj.has("timestamp") && 
			    !"".equals(jsonObj.getString("timestamp"))) &&
			    (jsonObj.has("getFileArr") && 
			    !"".equals(jsonObj.getJSONArray("getFileArr").toString()))){
			
				String timestamp = jsonObj.getString("timestamp");
				//JSONArray jsonarr =  jsonObj.getJSONArray("getFileArr");
				// 針對不在 cache 中的資料進行 mongo 查詢.
				JSONArray userData = jsonObj.getJSONArray("getFileArr");
				
				
				JSONArray users = new JSONArray(dynamoService.getItems(Config.document,userData));
				JSONObject jomongos= new JSONObject();	 // 從 mongo 中查詢到, 且未被 disable 的資料
					
				
					
					for(int i = 0; i < users.length(); i++){
						//判斷資料是否被砍
						System.out.println(users.getJSONObject(i));
						JSONObject user = users.getJSONObject(i);
						if(user.has("disabled") && user.get("disabled").toString().equals("1")) continue;
						jomongos.put(user.getString("fileid"), user);
					}		
					//將JSON轉為jsonarray
					//String msql="";
					JSONArray jsonarr1 = jsonObj.getJSONArray("getFileArr");
					
					/*replace all fileid_uuid xxxxxxaa to fileid*/
					// modify by jj on 2013-12-04 fix replace UUIDaa to fileId 
					
					JSONObject fileidUUIDaaObjMap = new JSONObject();
					JSONObject queryFileIdAndUUIDaaMap = new JSONObject();	 // 用於紀錄 fileid <-> filidaa 對應. 於 getFileUrl 解析完成後置換回來
					
					// logger.info("htmlToLink: getFileUrl replace before"+ jsonarr1.toString());
					for(int i=0;i<jsonarr1.length();i++){
						String fileid_temp = jsonarr1.getJSONObject(i).getString("fileId");
						
						
						if(!tools.isEmpty(fileid_temp)){		

							String real_fileid = null;
							
							if(fileid_temp.endsWith("aa")){
								
								String fileid_aa = fileid_temp;			// 重新正名區域變數, 避免混淆.
								
//								JSONObject fileidFileaaMapObj = new JSONObject(); 
								// fileid aa 的 key 不像 getFileUrl 的參數那麼多, 因此 key 值採用簡單處理 (不用json string 來呈現).
								String cacheKey = "fUrl:aa:" + fileid_aa;
								
								
								
								// 若 aa fileid 找不到對應的 cache, 就到 mongo 抓.
								// if(real_fileid == null){
								if(tools.isEmpty(real_fileid)){	
									JSONObject uuidaaObj = new JSONObject(tools.html_img_fileid(fileid_aa));
//									logger.info("load fileidaa [" + fileid_aa + "] => " + uuidaaObj.toString());
									
									if(!tools.isEmpty(uuidaaObj, "fileId")){
										real_fileid = uuidaaObj.getString("fileId");
									}

									// uuidaaObj 有可能有資料, 卻沒有 fileId, 因為 putFile 還在執行中就收到 getFileUrl 請求了.
									fileidUUIDaaObjMap.put(tools.isEmpty(real_fileid) ? fileid_aa : real_fileid, uuidaaObj);
//									fileidUUIDaaObjMap.put(fileid_aa, uuidaaObj);		// user always query by fileidaa.
									
//									logger.info("fileidUUIDaaObjMap => " + fileidUUIDaaObjMap.toString());
									
//									real_fileid = tools.html_img_fileid(fileid_temp);
									
//									if(!isEmpty(real_fileid)){
//										// 設置 aa fileid 對應的實際 fileid.
//										setUrlCache(cacheKey, real_fileid);
//									}
								}							
								
								// 若 real_fileid 仍是空值, 表示 fileidaa 在 htmllink 中也不存在
								if(tools.isEmpty(real_fileid)){
									// 對應的 fileid 不存在, 以 fileidaa 做為 fileid 以讓後續程序能執行.
									
									// 若轉貼連  putFile 還在執行中就收到 getFileUrl 請求了.
									// 這時有可能 real_fileid 是空的, 但 uuidaaObj 有值, 
									// 這裡將 real_fileid 換成 uuidaa 讓後續能夠呈現目前 fileidUuidaaMap 的轉檔狀態 (存放於 fileidUUIDaaObjMap 中的 uuidaaObj)
									real_fileid = fileid_temp;
								}
								
//								fileidFileaaMapObj.put(real_fileid, fileid_temp);
//								fileidUUIDaaObjMap.put(real_fileid, fileidFileaaMapObj);
								// logger.info("htmlToLink getFileUrl fileid_temp-->"+ fileid_temp+" real_fileid-->" +real_fileid);
								// logger.info("[doc debug] getFileUrl fileid_aa-->"+ fileid_temp+" real_fileid-->" +real_fileid);
							} else {
								real_fileid = fileid_temp;
							}
							jsonarr1.getJSONObject(i).put("fileId", real_fileid);
						}
					}
					
//					if(fileidFileaaMapArr.length() > 0)		// 降低 log 量.
//						logger.info("fileid_aa <--> fileid => " + fileidFileaaMapArr.toString());
					
					if(fileidUUIDaaObjMap.length() > 0)
						Logger.info("fileid <-> uuidaa map => " + fileidUUIDaaObjMap.toString());
					
					Map<String, JSONObject> cachedUrlMap = new HashMap<String, JSONObject>();	// 存放 fid <-> url data 的對應
					List<String> keys = new ArrayList<String>();
					
					
					
					// 針對不在 cache 中的資料進行 mongo 查詢.
					
					JSONArray jsonarr = new JSONArray(jsonarr1.toString());
//					
					
					StringBuilder sqlBuilder = new StringBuilder();
					boolean hasCacheUrl = false;
					
					
					// 效能考量, 查詢 mongo 時, 先濾除重覆的 fileid.
					Set<String> distinctFileIds = new HashSet<String>();
					
					// 將不在 cache 中的 fid 清單找出, 用於 search mongo.
					Iterator<String> keyObjs = cachedUrlMap.keySet().iterator();
					
					while(keyObjs.hasNext()){
						String keyObj = keyObjs.next();
						//if (deBugMode) 
						//	logger.info("keyObjs value=> "+keyObj);
						JSONObject cachedUrlResult = cachedUrlMap.get(keyObj);					
						
						if(cachedUrlResult == null){
							String fileId = new JSONObject(keyObj.replace("fUrl:", "")).getString("fileId");
							distinctFileIds.add(fileId);
						}else{
							hasCacheUrl = true;
							// System.out.println("cached url => " + cachedUrlResult.toString());
						}
					}
					
					// 僅針對 distincted file list 做查詢.
					Iterator<String> uncachedFileIds = distinctFileIds.iterator();
					while(uncachedFileIds.hasNext()){
						String uncachedFileId = uncachedFileIds.next();
						
						if (sqlBuilder.length() > 0){
							sqlBuilder.append(",");
						}				
						sqlBuilder.append("\"").append(uncachedFileId).append("\"");
					}		
					
					
					
					String mongoResult = null;
					
					//JSONObject jomongos= new JSONObject();	 // 從 mongo 中查詢到, 且未被 disable 的資料
					
//					SimpleDateFormat formatter = new SimpleDateFormat(DateUtil.DATE_FORMAT_1);
						
						
						//找不到資料
//						if(jomongos.length()==0){
						if(jomongos.length()==0 && !hasCacheUrl){	// 若 cached url 也是空的才回傳找不到資料.
//							JSONArray noDataRtn = new JSONArray(); 
//							JSONObject tmp= generateGetFileDetailErrorObject("", "fileid not found");							
//							noDataRtn.put(tmp);
							
							JSONArray noDataRtn = new JSONArray(); 
							
							//為每個 fileid 都產生 fileid not found 的訊息.
							for(int i=0;i<jsonarr1.length();i++){
								String fileid = jsonarr1.getJSONObject(i).getString("fileId");
								// JSONObject tmp = generateGetFileDetailErrorObject(fileid, "fileid not found");
								JSONObject tmp;
								// {"4ee65980bb974b3da4a586c302996f79aa":{"UUIDaa":"4ee65980bb974b3da4a586c302996f79aa","convert":"pending"}}
								if(fileidUUIDaaObjMap.has(fileid)){
									JSONObject fileidUuidMapObj = fileidUUIDaaObjMap.getJSONObject(fileid);
									String msg = fileidUuidMapObj.has("msg")?fileidUuidMapObj.getString("msg"):"";
									tmp = tools.generateGetFileDetailErrorObject(fileid, msg);
									tmp.put("convert", fileidUuidMapObj.getString("convert"));								 
								}
								else{
									tmp = tools.generateGetFileDetailErrorObject(fileid, "fileid not found");
								}
								
								noDataRtn.put(tmp);
							}
							return noDataRtn.toString();
						}
					
					
		
					
					// 輸出資料		
					for(int i=0;i<jsonarr.length();i++){
						JSONObject paramObj = jsonarr.getJSONObject(i);
						String fileId = paramObj.getString("fileId");	
						//if (deBugMode) 
						//	logger.info("fileId value =>"+fileId);
//						String fileTag = paramObj.has("fileTag") ? paramObj.getString("fileTag") : "";
//						
//						JSONObject keyObj = new JSONObject()
//						.put("fileId", paramObj.getString("fileId"))					
//						.put("fileTag", fileTag);
//						
//						JSONObject cachedUrlResult = cachedUrlMap.get(keyObj.toString());
						
						
							// 採用 mongo data
							if(jomongos.has(fileId)){
								
								JSONObject obj = jomongos.getJSONObject(fileId); 
								
								// 若在 getFileUrl 中的 timestamp 值為 0, 則回傳公開的 url.
								if(timestamp.equals("0"))
									obj.put("isP", 1);
								
								JSONObject tmp = tools.resolveSingleFileUrl(fileId, obj, paramObj, timestamp, fileidUUIDaaObjMap, queryFileIdAndUUIDaaMap);
														
								rtn.put(tmp);	
								
								// process url response cache.								
								
							}
							else{
								JSONObject tmp= tools.generateGetFileDetailErrorObject(fileId, "fileid not found");
								rtn.put(tmp);
							}
							Logger.info("rtn value =>"+rtn.toString());
						
					}
					
//					JSONObject cost = new JSONObject().put("cost", String.valueOf(System.currentTimeMillis() - cost_start) + "ms");
//					rtn.put(cost);
					
					returnStr = rtn.toString();	
				}else{
					JSONArray errorRtn = new JSONArray(); 
					JSONObject tmp= tools.generateGetFileDetailErrorObject("", "empty parameter");
					errorRtn.put(tmp);
					return errorRtn.toString();
				}								
			}catch (JSONException e1) {		
				Logger.error("jsonObj=>" + Object, e1);
				e1.printStackTrace();
				
				throw new DocApplicationException("Json格式轉換失敗", 1);
				//TODO Johnson 新的error handler舊的拿掉
				/*try{
					JSONArray errorRtn = new JSONArray(); 
					JSONObject tmp= tools.generateGetFileDetailErrorObject("", "getFileUrl Exception");
					errorRtn.put(tmp);
					return errorRtn.toString();
				}catch(Exception e){
					Logger.error("jsonObj=>" + Object , e);
				};*/
			}catch(Exception e1){
				throw new DocApplicationException(e1,3);
			}
					
			
			return returnStr;
				
			
		}

		@Override
		public String signature(Signature jsonData) throws DocApplicationException {
			getHeaderValue();
			JSONObject returnObject = new JSONObject();

			try {
			/*
			//paramVal is {"apnum":"10400","pid":"10400","content-type","image/jpeg","filename":"123","extra":"1234"}
			paramObj = new JSONObject(this.decryptParam(param));
			
			//mongoDb data check
			if (!paramObj.has("apnum")||"".equals(paramObj.getString("apnum")))
				throw new DocApplicationException("NotPresent",3);//erroehandler 必填欄位未填
			if (!paramObj.has("pid")||"".equals(paramObj.getString("pid")))
				throw new DocApplicationException("NotPresent",3);//erroehandler 必填欄位未填
			if (!paramObj.has("Content_Disposition")||"".equals(paramObj.getString("Content_Disposition")))
				throw new DocApplicationException("NotPresent",3);//erroehandler 必填欄位未填
			if (!paramObj.has("extra")||"".equals(paramObj.getString("extra")))
				throw new DocApplicationException("NotPresent",3);//erroehandler 必填欄位未填
			if (!paramObj.has("isP")||"".equals(paramObj.getInt("isP")))
				throw new DocApplicationException("NotPresent",3);//erroehandler 必填欄位未填
			
			//singedurl
			if (!paramObj.has("content-type")||"".equals(paramObj.getString("content-type")))
				throw new DocApplicationException("NotPresent",3);//erroehandler 必填欄位未填
			
			
			
			
			//userConfig Data query
			DynamoService dynamoService = new DynamoService();
			//獲取型態類別
			int contentType = tools.getContentType(paramObj.getString("content-type"));
			String fileid = tools.generateFileId(contentType,paramObj.getInt("isP"));
			//filePath
			String filepath = tools.generateFilePath(fileid);
			//fileName
			String fileName = paramObj.getString("Content_Disposition");
			
			//Db內串出filepath&fileName
			String filepath_forS3 = filepath + fileid + fileName.substring(fileName.lastIndexOf("."),fileName.length()).toLowerCase();
			
			Item putItem = new Item().withPrimaryKey("fileid",fileid).
			withString("apnum", paramObj.getString("apnum")).
			withNumber("contenttype", contentType).
			withString("convert", "pending").
			withString("fileid",fileid).
			withString("filename", fileName).
			withString("filepath", filepath_forS3).
			withString("imgstatus", "pending").
			withString("insertdate", new SimpleDateFormat().format(new java.util.Date())).
			withNumber("isP",paramObj.getInt("isP")).
			withString("pid", paramObj.getString("pid")).
			
		
			//非必填項目
			withString("source", "http://localhost:8080/DreamsAdmin/Dream/DreamFwdAction_activityBroadcasting.action?dreamType=2").
			withString("description", "description").
			withString("title", "title");
			*/
			/*if(contentType == ContentType.Video || contentType == ContentType.WbVideo)
				putItem.withString("videoQuality", videoQualityObj);
			
			*/
			
			//dynamoService.putItem("users", putItem);
			JSONObject putObj = new JSONObject(putfile(jsonData));
			String filepath_forS3=putObj.getString("filePath");
			String fileName = putObj.getString("fileName");
			System.out.println(tools.getCurrentUTCTimestamp((byte)1));
			//String extra = putObj.getString("extra");
			 //去掉“-”符号 
			String policy_document =
				      "{\"expiration\": \""+tools.getCurrentUTCTimestamp((byte)1)+"\"," +
				        "\"conditions\": [" +
				          "{\"bucket\": \""+Config.bucketName+"\"}," +
				          "[\"starts-with\", \"$key\", \""+filepath_forS3+"\"]," +
				          "{\"acl\": \"authenticated-read\"}," +
				          //"{\"Content-Disposition\": \""+ fileName +"\"},"+
				          //"{\"acl\": \"public-read\"},"+
				          "[\"starts-with\", \"$Content-Type\", \""+ putObj.getString("contenttype") +"\"]" +
				        "]" +
				      "}";
			// "{\"Content-Disposition\": \""+ fileName +"\"},"此檔案先不加
			
			//"[\"starts-with\", \"$Content-Type\", \"image/\"]," +
			
			 // Calculate policy and signature values from the given policy document and AWS credentials.
			Base64 Base64 =  new Base64();
			
			
			String signature="";
			
				String policy = Base64.encodeToString(policy_document.getBytes("UTF-8")).replaceAll("\n","").replaceAll("\r","");
				//String policy = Base64.encodeBase64(policy_document.getBytes("UTF-8")).toString().replaceAll("\n","").replaceAll("\r","");
			
				Mac hmac = Mac.getInstance("HmacSHA1");
				
					hmac.init(new SecretKeySpec(new DefaultAWSCredentialsProviderChain().getCredentials().getAWSSecretKey().getBytes("UTF-8"), "HmacSHA1"));
				
				//Map<String, String> cachedUrlMap = new HashMap<String, String>();	
				
				signature = Base64.encodeToString(hmac.doFinal(policy.getBytes("UTF-8"))).replaceAll("\n", "");
				//signature = Base64.encodeBase64(hmac.doFinal(policy.getBytes("UTF-8"))).toString().replaceAll("\n", "");
					
				returnObject.put("policyDocument", policy);
				returnObject.put("signature", signature);
				returnObject.put("objectKey", filepath_forS3);
				returnObject.put("bucketName", Config.bucketName);
				returnObject.put("contentDisposition", fileName);
			
			} catch (InvalidKeyException | UnsupportedEncodingException |
					NoSuchAlgorithmException | NullPointerException | JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new DocApplicationException(e,11);
			}
			return returnObject.toString();
		}

		@Override
		public String putfile12(jsonData jsonData)
				throws DocApplicationException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getFileUrl( GetFileUrl jsonData) throws DocApplicationException {
			getHeaderValue();
			JSONArray rtn = new JSONArray(); //回傳的JSONArray
			tools tools = new tools();
			JSONObject jsonObj = new JSONObject();
			String returnStr ="";
			JSONObject extra = new JSONObject();
			
			src = "docapi::core::getFileUrl::";
			
			
			try{
				
				jsonObj.put("getFileArr", jsonData.getGetFileArr())
				.put("timestamp", jsonData.getTimestamp()).toString();
				
				
				DynamoService dynamoService = new DynamoService();
				
				
				//JSONObject jsonObj = new JSONObject(Object);
				//System.out.println("ok"+jsonObj.getJSONArray("getFileArr").toString());
			
				//Object.getFileId()
				System.out.print(jsonObj.getJSONArray("getFileArr").toString());
			if(jsonData == null || jsonObj.isNull("timestamp") || jsonObj.isNull("getFileArr") ||
					(jsonObj.has("timestamp") && "".equals(jsonObj.getString("timestamp"))) ||
					(jsonObj.has("getFileArr") && jsonObj.getJSONArray("getFileArr").length()<=0))
				throw new DocApplicationException("Empty parameter", 3);
			
			
			String timestamp = jsonObj.getString("timestamp");
			//JSONArray jsonarr =  jsonObj.getJSONArray("getFileArr");
			// 針對不在 cache 中的資料進行 mongo 查詢.
			JSONArray userData = jsonObj.getJSONArray("getFileArr");
			
		
			extra.put("getFileArr",userData).put("timestamp", timestamp);
			//extra.put("timestamp",timestamp);
			
			//System.out.println(context.getHttpServletRequest().getHeader("X-caller"));
			traceLog.writeKinesisLog(trackId, caller, src, "getFileurl::Start",extra);
			
			
			JSONArray users = new JSONArray(dynamoService.getItems(Config.document,userData));
			JSONObject jomongos= new JSONObject();	 // 從 mongo 中查詢到, 且未被 disable 的資料
			
			
				
				for(int i = 0; i < users.length(); i++){
					//判斷資料是否被砍
					System.out.println(users.getJSONObject(i));
					JSONObject user = users.getJSONObject(i);
					if(user.has("disabled") && user.get("disabled").toString().equals("1")) continue;
					jomongos.put(user.getString("fileId"), user);
					Logger.info("apnum={},filepath={}",users.getJSONObject(i));
				}		
				//將JSON轉為jsonarray
				//String msql="";
				JSONArray jsonarr1 = jsonObj.getJSONArray("getFileArr");
				
				/*replace all fileid_uuid xxxxxxaa to fileid*/
				// modify by jj on 2013-12-04 fix replace UUIDaa to fileId 
				
				JSONObject fileidUUIDaaObjMap = new JSONObject();
				JSONObject queryFileIdAndUUIDaaMap = new JSONObject();	 // 用於紀錄 fileid <-> filidaa 對應. 於 getFileUrl 解析完成後置換回來
				
				// logger.info("htmlToLink: getFileUrl replace before"+ jsonarr1.toString());
				for(int i=0;i<jsonarr1.length();i++){
					String fileid_temp = jsonarr1.getJSONObject(i).getString("fileId");
					
					
					if(!tools.isEmpty(fileid_temp)){		

						String real_fileid = null;
						
						if(fileid_temp.endsWith("aa")){
							
							String fileid_aa = fileid_temp;			// 重新正名區域變數, 避免混淆.
							
//								JSONObject fileidFileaaMapObj = new JSONObject(); 
							// fileid aa 的 key 不像 getFileUrl 的參數那麼多, 因此 key 值採用簡單處理 (不用json string 來呈現).
							String cacheKey = "fUrl:aa:" + fileid_aa;
							
							try{
								RedisService redisService = new RedisService();
								//MemcachedClient redis = redisService.redisClient(); 
								Jedis redis =  redisService.jedisClient();
								//redis = Redis.getInstance("FileManage", "RE600001");
								// redis.open();
								
								String cached_aa_fileid = redis.get(cacheKey).toString();
								if(!tools.isEmpty(cached_aa_fileid)){
									queryFileIdAndUUIDaaMap.put(cached_aa_fileid, fileid_aa);		// 用於紀錄 fileid <-> filidaa 對應. 於 getFileUrl 解析完成後置換回來
									real_fileid = cached_aa_fileid;
								}
							}
							catch(Exception e){
								Logger.error("fail to get aa url cache from redis.", e);
								throw new DocApplicationException("Redis Error", 14);
							}
							finally{
//									if(redis != null)
//										redis.close();
							}
							
							
							// 若 aa fileid 找不到對應的 cache, 就到 mongo 抓.
							// if(real_fileid == null){
							if(tools.isEmpty(real_fileid)){	
								JSONObject uuidaaObj = new JSONObject(tools.html_img_fileid(fileid_aa));
//									logger.info("load fileidaa [" + fileid_aa + "] => " + uuidaaObj.toString());
								
								if(!tools.isEmpty(uuidaaObj, "fileId")){
									real_fileid = uuidaaObj.getString("fileId");
								}

								// uuidaaObj 有可能有資料, 卻沒有 fileId, 因為 putFile 還在執行中就收到 getFileUrl 請求了.
								fileidUUIDaaObjMap.put(tools.isEmpty(real_fileid) ? fileid_aa : real_fileid, uuidaaObj);
//									fileidUUIDaaObjMap.put(fileid_aa, uuidaaObj);		// user always query by fileidaa.
								
//									logger.info("fileidUUIDaaObjMap => " + fileidUUIDaaObjMap.toString());
								
//									real_fileid = tools.html_img_fileid(fileid_temp);
								
//									if(!isEmpty(real_fileid)){
//										// 設置 aa fileid 對應的實際 fileid.
//										setUrlCache(cacheKey, real_fileid);
//									}
							}							
							
							// 若 real_fileid 仍是空值, 表示 fileidaa 在 htmllink 中也不存在
							if(tools.isEmpty(real_fileid)){
								// 對應的 fileid 不存在, 以 fileidaa 做為 fileid 以讓後續程序能執行.
								
								// 若轉貼連  putFile 還在執行中就收到 getFileUrl 請求了.
								// 這時有可能 real_fileid 是空的, 但 uuidaaObj 有值, 
								// 這裡將 real_fileid 換成 uuidaa 讓後續能夠呈現目前 fileidUuidaaMap 的轉檔狀態 (存放於 fileidUUIDaaObjMap 中的 uuidaaObj)
								real_fileid = fileid_temp;
							}
							
//								fileidFileaaMapObj.put(real_fileid, fileid_temp);
//								fileidUUIDaaObjMap.put(real_fileid, fileidFileaaMapObj);
							// logger.info("htmlToLink getFileUrl fileid_temp-->"+ fileid_temp+" real_fileid-->" +real_fileid);
							// logger.info("[doc debug] getFileUrl fileid_aa-->"+ fileid_temp+" real_fileid-->" +real_fileid);
						} else {
							real_fileid = fileid_temp;
						}
						jsonarr1.getJSONObject(i).put("fileId", real_fileid);
					}
				}
				
//					if(fileidFileaaMapArr.length() > 0)		// 降低 log 量.
//						logger.info("fileid_aa <--> fileid => " + fileidFileaaMapArr.toString());
				
				if(fileidUUIDaaObjMap.length() > 0)
					Logger.info("fileid <-> uuidaa map => " + fileidUUIDaaObjMap.toString());
				
				Map<String, JSONObject> cachedUrlMap = new HashMap<String, JSONObject>();	// 存放 fid <-> url data 的對應
				List<String> keys = new ArrayList<String>();
				
				
				
				// 針對不在 cache 中的資料進行 mongo 查詢.
				
				JSONArray jsonarr = new JSONArray(jsonarr1.toString());
//					
				
				StringBuilder sqlBuilder = new StringBuilder();
				boolean hasCacheUrl = false;
				
				
				// 效能考量, 查詢 mongo 時, 先濾除重覆的 fileid.
				Set<String> distinctFileIds = new HashSet<String>();
				
				// 將不在 cache 中的 fid 清單找出, 用於 search mongo.
				Iterator<String> keyObjs = cachedUrlMap.keySet().iterator();
				
				while(keyObjs.hasNext()){
					String keyObj = keyObjs.next();
					//if (deBugMode) 
					//	logger.info("keyObjs value=> "+keyObj);
					JSONObject cachedUrlResult = cachedUrlMap.get(keyObj);					
					
					if(cachedUrlResult == null){
						String fileId = new JSONObject(keyObj.replace("fUrl:", "")).getString("fileId");
						distinctFileIds.add(fileId);
					}else{
						hasCacheUrl = true;
						// System.out.println("cached url => " + cachedUrlResult.toString());
					}
				}
				
				// 僅針對 distincted file list 做查詢.
				Iterator<String> uncachedFileIds = distinctFileIds.iterator();
				while(uncachedFileIds.hasNext()){
					String uncachedFileId = uncachedFileIds.next();
					
					if (sqlBuilder.length() > 0){
						sqlBuilder.append(",");
					}				
					sqlBuilder.append("\"").append(uncachedFileId).append("\"");
				}		
				
				
				
				String mongoResult = null;
				
				//JSONObject jomongos= new JSONObject();	 // 從 mongo 中查詢到, 且未被 disable 的資料
				
//					SimpleDateFormat formatter = new SimpleDateFormat(DateUtil.DATE_FORMAT_1);
					
					
					//找不到資料
//						if(jomongos.length()==0){
					if(jomongos.length()==0 && !hasCacheUrl){	// 若 cached url 也是空的才回傳找不到資料.
//							JSONArray noDataRtn = new JSONArray(); 
//							JSONObject tmp= generateGetFileDetailErrorObject("", "fileid not found");							
//							noDataRtn.put(tmp);
						
						JSONArray noDataRtn = new JSONArray(); 
						
						//為每個 fileid 都產生 fileid not found 的訊息.
						for(int i=0;i<jsonarr1.length();i++){
							String fileid = jsonarr1.getJSONObject(i).getString("fileId");
							// JSONObject tmp = generateGetFileDetailErrorObject(fileid, "fileid not found");
							JSONObject tmp;
							// {"4ee65980bb974b3da4a586c302996f79aa":{"UUIDaa":"4ee65980bb974b3da4a586c302996f79aa","convert":"pending"}}
							if(fileidUUIDaaObjMap.has(fileid)){
								JSONObject fileidUuidMapObj = fileidUUIDaaObjMap.getJSONObject(fileid);
								String msg = fileidUuidMapObj.has("msg")?fileidUuidMapObj.getString("msg"):"";
								tmp = tools.generateGetFileDetailErrorObject(fileid, msg);
								tmp.put("convert", fileidUuidMapObj.getString("convert"));								 
							}
							else{
								tmp = tools.generateGetFileDetailErrorObject(fileid, "fileid not found");
							}
							
							noDataRtn.put(tmp);
						}
						return noDataRtn.toString();
					}
				
				
	
				
				// 輸出資料		
				for(int i=0;i<jsonarr.length();i++){
					JSONObject paramObj = jsonarr.getJSONObject(i);
					String fileId = paramObj.getString("fileId");	
					//if (deBugMode) 
					//	logger.info("fileId value =>"+fileId);
//						String fileTag = paramObj.has("fileTag") ? paramObj.getString("fileTag") : "";
//						
//						JSONObject keyObj = new JSONObject()
//						.put("fileId", paramObj.getString("fileId"))					
//						.put("fileTag", fileTag);
//						
//						JSONObject cachedUrlResult = cachedUrlMap.get(keyObj.toString());
					
					
						// 採用 mongo data
						if(jomongos.has(fileId)){
							
							JSONObject obj = jomongos.getJSONObject(fileId); 
							
							// 若在 getFileUrl 中的 timestamp 值為 0, 則回傳公開的 url.
							if(timestamp.equals("0"))
								obj.put("isP", 1);
							
							JSONObject tmp = tools.resolveSingleFileUrl(fileId, obj, paramObj, timestamp, fileidUUIDaaObjMap, queryFileIdAndUUIDaaMap);
													
							rtn.put(tmp);	
							
							// process url response cache.								
							
						}
						else{
							JSONObject tmp= tools.generateGetFileDetailErrorObject(fileId, "fileid not found");
							rtn.put(tmp);
						}
						Logger.info("rtn value =>"+rtn.toString());
					
				}
				
//					JSONObject cost = new JSONObject().put("cost", String.valueOf(System.currentTimeMillis() - cost_start) + "ms");
//					rtn.put(cost);
				traceLog.writeKinesisLog(trackId, caller, src, "getFileurl::End",new JSONObject().put("getFileArr",rtn));
				returnStr = rtn.toString();	
												
			}catch (JSONException e1) {		
				Logger.error("jsonObj=>" + jsonData.toString(), e1);
				e1.printStackTrace();
				
				throw new DocApplicationException("Json格式轉換失敗", 1);
				//TODO Johnson 新的error handler舊的拿掉
				/*try{
					JSONArray errorRtn = new JSONArray(); 
					JSONObject tmp= tools.generateGetFileDetailErrorObject("", "getFileUrl Exception");
					errorRtn.put(tmp);
					return errorRtn.toString();
				}catch(Exception e){
					Logger.error("jsonObj=>" + Object , e);
				};*/
			}catch(DocApplicationException e){
				throw e;
			}catch(Exception e1){
				throw new DocApplicationException(e1,3);
			}
			return returnStr;
		}

		@Override
		public String healthCheck() {
			return "ok";
		}
		
		@Override
		public String test(Signature jsonData) throws DocApplicationException {
			// TODO Auto-generated method stub
			return null;
		}
		
		
		
/*
		@Override
		public String wbGetStatus(Signature processId)
			
			//取得process 
			String wbStr = this.getProcessById(processId);
			
			//System.out.println("WBFileConvert getStatus("+processId+")=>"+process);
			logger.info("Enter WBFileConvert getStatus, " + DateUtil.getDateTimeForLog() + "processId==>"+ processId);
			
			JSONObject wb;
			JSONObject wb_tags;
			JSONObject wb_status = null;		
			try {
				wb = new JSONObject(wbStr);
				//process 目前狀態
				wb_status = new JSONObject(wb.get("status").toString());
				if(wb_status.getString("status").equalsIgnoreCase("Fail")){
					//失敗
					wb_status.put("status", "Fail");
					wb_status.put("progress", "0");
				}else if(wb_status.getString("status").equalsIgnoreCase("Success") && wb_status.getString("stage").equalsIgnoreCase("FileConvert")){
					//成功
					wb_status.put("status", "Success");
					wb_status.put("progress", "100");
				}else{
					//需判斷狀態   {"tags":{"tag2":"a6b847b6d0ce4d798e460d8e36a3a239","tag1":"13423b84c9714de59c8e7ec487dabfa1"}}
					wb_tags = new JSONObject(wb.get("tags").toString());
					Iterator keyIter = wb_tags.keys();
					JSONArray status_arr = new JSONArray();//processId 下所有檔案狀態JSONArray				
					// loop tags 取得process下所有檔案資訊
					while (keyIter.hasNext()) {
						String key = (String) keyIter.next();//tag名稱
						String fileId = wb_tags.getString(key);//對應的fileId
						//取得檔案detail 
						JSONObject f_detail = new JSONObject(fm.getFileDetail(fileId,"").toString());
						f_detail.put("fileTag",key);
						status_arr.put(f_detail);
					}
					//status_arr=> [{"filepath":"/104plus/WB123/5/a6b847b6d0ce4d798e460d8e36a3a239.flv","desc":"","status":"Success","convert":"Success","pid":"WB123","fileid":"a6b847b6d0ce4d798e460d8e36a3a239","fileTag":"tag2","contenttype":5,"insertdate":"2013-03-04 11:34:59","apnum":"0","imgstatus":"Success","title":"","_id":{"$oid":"51341663e4b07d3aa3425566"},"txid":"7a6955c5-1d0d-4dac-b008-cd156ffcc3e0","filename":"CLASS_QA_GT2013022312100380135_video_right_2SR.flv"},{"filepath":"/104plus/WB123/5/13423b84c9714de59c8e7ec487dabfa1.flv","desc":"","status":"Success","convert":"Success","pid":"WB123","fileid":"13423b84c9714de59c8e7ec487dabfa1","fileTag":"tag1","contenttype":5,"insertdate":"2013-03-04 11:34:59","apnum":"0","imgstatus":"Success","title":"","_id":{"$oid":"51341663e4b07d3aa3425565"},"txid":"1b494107-77a1-4f2d-a362-39305d5bc577","filename":"CLASS_QA_GT2013022312100380135_video_left_2SR.flv"}]

					
					//stage uplade->ImageProcess
					// 計算進度
					double s_num = 0;	// video snap (imgstatus) success number
					double p_num = 0;	// video snap (imgstatus) pending number
										// error will return.
					
					if(wb_status.getString("stage").equalsIgnoreCase("Upload")){
						//rtn.put("stage", status_arr);
						//計算success & pend				
						for (int i = 0; i < status_arr.length(); i++) {
							if(status_arr.getJSONObject(i).getString("imgstatus").equalsIgnoreCase("Fail")){
								//fail
								wb_status.put("stage", "ImageProcess");
								wb_status.put("status", "Fail");
								wb_status.put("progress", "0");
								break;
							}else if(status_arr.getJSONObject(i).getString("imgstatus").equalsIgnoreCase("Success")){
								s_num=s_num+1;
							}else{
								p_num=p_num+1;
							}
						}
						
						if(wb_status.getString("status").equalsIgnoreCase("Fail")){//失敗
							wb_status.put("stage", "ImageProcess");
							wb_status.put("status", "Fail");
							wb_status.put("progress", "0");
						}else if(p_num >0 ){//處理中
							wb_status.put("stage", "ImageProcess");
							wb_status.put("status", "Inprocess");
							double progress = s_num/(p_num+s_num)*50;
							wb_status.put("progress", progress);						
						}else{//成功
							wb_status.put("stage", "ImageProcess");
							wb_status.put("status", "Success");
						}			
					}	

					
					//stage ImageProcess->FileConvert
					// 計算進度
					
					if((wb_status.has("progress") && !wb_status.get("progress").toString().equals("100")) || 
						(wb_status.getString("stage").equalsIgnoreCase("ImageProcess") && wb_status.getString("status").equalsIgnoreCase("Success"))){
						s_num = 0;
						p_num = 0;
						//rtn.put("stage", status_arr);
						//計算success & pend				
						for (int i = 0; i < status_arr.length(); i++) {
							if(status_arr.getJSONObject(i).getString("convert").equalsIgnoreCase("Fail")){
								//fail
								wb_status.put("stage", "FileConvert");
								wb_status.put("status", "Fail");
								wb_status.put("progress", "0");
								break;
							}else if(status_arr.getJSONObject(i).getString("convert").equalsIgnoreCase("Success")){
								s_num=s_num+1;
							}else{
								p_num=p_num+1;
							}
							
						}
						if(wb_status.getString("status").equalsIgnoreCase("Fail")){//失敗
							wb_status.put("stage", "FileConvert");
							wb_status.put("status", "Fail");
							wb_status.put("progress", "0");						
						}else if(p_num >0){//轉檔中
							//目前預設白板不用轉檔
							//status_obj.put("stage", "FileConvert");
							//status_obj.put("status", "Success");						
							
							wb_status.put("stage", "FileConvert");
							wb_status.put("status", "Inprocess");
							double progress = 50+((s_num/(p_num+s_num))*50);
							
							wb_status.put("progress", progress);						
							//status_obj.put("progress", 100);
						}else{//成功
							wb_status.put("stage", "FileConvert");
							wb_status.put("status", "Success");
							wb_status.put("progress", 100);
						}								
					}
				}
				//更新DB狀態
				this.updateProcessStatus(processId, wb_status.toString());			
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		    	java.io.StringWriter sw = new java.io.StringWriter();
				java.io.PrintWriter pw = new java.io.PrintWriter(sw);
				e.printStackTrace(pw);
				logger.error(sw.getBuffer().toString());
			}
			logger.info("Exit WBFileConvert getStatus, " + DateUtil.getDateTimeForLog() + "status_obj==>"+ wb_status.toString());
			return wb_status.toString();
		}
		*/
/**
 * Get Request Header Value
 * */
private void getHeaderValue(){
	if (context!=null){
		trackId = context.getHttpServletRequest().getHeader("X-Custom-Tracer-Id");
		caller = context.getHttpServletRequest().getHeader("X-Custom-Caller");
		if (caller == null) {  
			caller = context.getHttpServletRequest().getRemoteAddr();  
	   }
	}
}

@Override
public String action() {
	while (true)
	{
	 double x = Math.random();
	 double y = Math.random();
	 double z;
	 x = Math.toRadians(x);
	 y = Math.toRadians(y);
	 z =( Math.sin(x)/Math.random() + Math.sin(y)*Math.random())*Math.random();
	 //return "Z is"+z;
	}
}


		
}
