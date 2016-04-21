package com.e104.util;

import java.math.BigInteger;
import java.net.URL;
import java.security.MessageDigest;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import javax.ws.rs.core.HttpHeaders;

import net.spy.memcached.MemcachedClient;













//import org.apache.catalina.util.Base64;
import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;















import scala.reflect.internal.Trees.New;
import scala.reflect.internal.Trees.This;

import com.e104.util.Config;











import com.amazonaws.HttpMethod;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.e104.Errorhandling.DocApplicationException;
import com.e104.enums.Protocol;
import com.e104.util.ContentType;

public class tools {
	private static transient Logger Logger =LogManager.getLogger(tools.class);
	public boolean isEmpty(String str){
    	return str == null || str.trim().equals("");
    }
    
	public boolean isEmpty(JSONObject obj, String key){
		try {
			return obj == null || !obj.has(key) || obj.get(key).toString().trim().equals("");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return true;
		}
	}
	
	/**
	 * 
	 * @param UUIDaa
	 * @return
	 * <pre>
	 * {
	 * fileId:"1ca697ae90124a06a96e821cbdd48d9201"
	 * UUIDaa:"735f346fd98f406e956bbe75d31e0519aa"
	 * convert:"succcess, fail, pending"
	 * msg:""
	 * }
	 * </pre>
	 * @throws Exception
	 */
	public String html_img_fileid(String UUIDaa) throws Exception {
		//�Ȯɪ����R�^�r��
		/*
		JSONObject rtn = new JSONObject();
		
		rtn.put("UUIDaa", UUIDaa);
		rtn.put("convert", "fail");
		
		// logger.info("Enter html_img_fileid--> fileid_uuid--> "+fileid_uuid);
		JSONObject UUIDaa_obj = new JSONObject().put("UUIDaa", UUIDaa);
		String mongoRet = mongo_select_RPC(Config.DB_NAME,Config.TABLENAMETXIDFILEIDMAP,UUIDaa_obj.toString());
		
		if(!isEmpty(mongoRet)) {
			JSONArray mongoArray = new JSONArray(mongoRet);
			if(mongoArray.length() > 0){
				JSONObject mongoObj = mongoArray.getJSONObject(0);
				
				if(!isEmpty(mongoObj, "saveImgStatus")) 
					rtn.put("convert", mongoObj.getString("saveImgStatus").toLowerCase());
				
				if(!isEmpty(mongoObj, "fileId")) 
					rtn.put("fileId", mongoObj.getString("fileId"));
				
				if(!isEmpty(mongoObj, "msg")) 
					rtn.put("msg", mongoObj.getString("msg"));
			} else{
				logger.error("UUIDaa " + UUIDaa + " not found.");
				rtn.put("msg", "UUIDaa " + UUIDaa + " not found.");
			}
		} else{
			logger.error("UUIDAA_DBERR: query UUIDaa (" + UUIDaa + ") from mongo result => " + mongoRet);
			rtn.put("msg", "UUIDAA_DBERR");
		}
		return rtn.toString();*/
		return UUIDaa;
	}
	
	public void setUrlCache(String key, String value) throws DocApplicationException{
		MemcachedClient redis = new RedisService().redisClient();
		try{
			
//			redis.open();			
			redis.set(key, 3600, value);
			
			Logger.info(String.format("url has been set cache => [%s]:[%s]", key, value));
		}
		catch(Exception e){
			Logger.error("fail to put url cache to redis.", e);
		}
		finally{
//			if(redis != null)
//				redis.close();
		}
	}
	
	
	public JSONObject generateGetFileDetailErrorObject(String fileId, String errorMsg) throws JSONException{
		// private JSONObject generateGetFileDetailErrorObject(String mongoDBStartTime, String mongoDBEndTime, String mongoDB_timeDifference, String methodEnternceTime, String errorMsg) throws JSONException{
			
			JSONObject tmp= new JSONObject();
			tmp.put("fileId",fileId);
			tmp.put("convert","");
			tmp.put("tag","");
			tmp.put("url",new JSONArray());
			
//			tmp.put("mongoStartTime", mongoDBStartTime);
//			tmp.put("mongoEndTime", mongoDBEndTime);
//			tmp.put("mongoTimeDifference", mongoDB_timeDifference);
//			tmp.put("methodEnternce", methodEnternceTime);
			//String methodExistTime = DateUtil.getDateTimeForLog();
			//tmp.put("methodExit", methodExistTime);
			
			tmp.put("msg ",errorMsg);
	        tmp.put("fileExtension", "");
	        return tmp;
		}
	
	
	public String get_file_extension(String srcFileName) throws Exception{
		String fileExtension = "";
		// logger.info("get_file_extension srcFileName==>"+srcFileName);
		int startInt = srcFileName.lastIndexOf(".")+1;
		if (startInt >1) {
			fileExtension = srcFileName.substring(startInt, srcFileName.length());
			//fileExtension = FilenameUtils.getExtension(srcFileName);
		}
		// logger.info("get_file_extension fileExtension==>"+fileExtension);
		return fileExtension;
	}
	
	public String get_file_keypath(String srcFileName) throws Exception{
		String fileKeyPath = "";
		// logger.info("get_file_extension srcFileName==>"+srcFileName);
		int startInt = srcFileName.lastIndexOf(".");
		if (startInt >1) {
			fileKeyPath = srcFileName.substring(0, startInt);
			//fileExtension = FilenameUtils.getExtension(srcFileName);
		}
		// logger.info("get_file_extension fileExtension==>"+fileExtension);
		return fileKeyPath;
	}
	
	/**
	 * 將string做md5編碼
	 * @param in 要加密的string
	 * @return
	 * @throws Exception
	 */
	public String md5(String in) throws Exception{
		MessageDigest md = MessageDigest.getInstance("MD5");
		md.update(in.getBytes());
		return toHexString(md.digest());
	}
	
	/**
	 * 將byte[]轉為Hex string
	 * @param in 輸入byte[]
	 * @return
	 * @throws Exception
	 */
	public String toHexString(byte[] in) throws Exception{
		StringBuilder hexString = new StringBuilder();
		for (int i = 0; i < in.length; i++){
			String hex = Integer.toHexString(0xFF & in[i]);
			if (hex.length() == 1){
				hexString.append('0');
			}
			hexString.append(hex);
		}
		return hexString.toString();
	}
	
	/**
	 * @method generateFileURLforPublic
	 * @purpose 產生(圖片)access url
	 * @param filepath eg : /104plus/1603/1/adsfasdfsadf.jpg
	 * @param timestamp eg : 1347858547722 有效期限 
	 * @param isP (0:非公開 1:公開)
	 * @param target 指定目標檔案標的, 可以是 orign 或 pdf.	
	 * @return String
	 * @throws Exception 
	 * @history 
	 */
	public String generateFileURLforPublic(String filepath, long timestamp,int isP, Protocol protocol) throws Exception {
		//2014-01-09 fix for md5 encrypt
		//System.out.println("filepath==>"+ filepath);
		int startInt = filepath.lastIndexOf("/") + 1;			
		int endInt = filepath.length();
		String tagFileName = filepath.substring(startInt, endInt);
		String path = filepath.substring(0, startInt);
		
		// urlNoProtocol => file.104.com.tw/DocumentManagementTomcatAccess/imgs (url for encrypt)
		String urlNoProtocol = Config.FILE_ACCESS_URL.substring(Config.FILE_ACCESS_URL.indexOf("://") + 3);
		urlNoProtocol += path;
		
		// String url = protocol == Protocol.HTTPS ? Config.FILE_ACCESS_SSL_URL : Config.FILE_ACCESS_URL;
		// url += path;
		
		// url for output.
		String url = urlNoProtocol;
		switch(protocol){
			case HTTP:
				url = "http://" + url;
				break;
			case HTTPS:
				url = "https://" + url;
				break;
			case COMMON:
				url = "//" + url;
				break;
			default:
				url = "http://" + url;
		}
		 
		//String url = Config.ACCESS_URL + path;
		
		// logger.info("INFO generateFileURLforPublic(), Encrypt url==>"+url+" timestamp:"+ timestamp+ " isP:"+ isP);
		try {
			//TODO Johnson Timestamp可能不用改用S3的Signed URL
			AmazonS3 s3client = new AmazonS3Client(new ProfileCredentialsProvider()); 
		       
			java.util.Date expiration = new java.util.Date();
			//long msec = expiration.getTime();
			//msec += 1000 * 60 * 60; // 1 hour.
			expiration.setTime(timestamp*1000);
			
			
			GeneratePresignedUrlRequest generatePresignedUrlRequest = 
			              new GeneratePresignedUrlRequest( Config.bucketName, filepath);
			generatePresignedUrlRequest.setMethod(HttpMethod.GET); // Default.
			generatePresignedUrlRequest.setExpiration(expiration);
			             
			URL s = s3client.generatePresignedUrl(generatePresignedUrlRequest);
			
			return s.toString();
			/*if  (isP == 0) {]'\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
				JSONObject v_json = new JSONObject("{\"timestamp\":\""+String.valueOf(timestamp)+"\",\"isP\":\""+String.valueOf(isP)+"\"}");
				String encryptVal = xorEncrypt(v_json.toString());
				//2014-01-09 fix for md5 encrypt url
				// logger.info("INFO generateFileURLforPublic() , return:" + url + tagFileName + "?"+md5(url+Config.MD5_PWD+timestamp)+"&v="+encryptVal);
				return url + tagFileName + "?"+md5(urlNoProtocol+Config.MD5_PWD+timestamp)+"&v="+encryptVal;
			} else { 
				JSONObject v_json = new JSONObject("{\"isP\":\""+String.valueOf(isP)+"\"}");
				String encryptVal = xorEncrypt(v_json.toString());
				//2014-01-09 fix for md5 encrypt url
				// logger.info("INFO generateFileURLforPublic() , return:" + url + tagFileName +  "?"+md5(url+Config.MD5_PWD_ISP1)+"&v="+encryptVal);
				return url + tagFileName + "?"+md5(urlNoProtocol+Config.MD5_PWD_ISP1)+"&v="+encryptVal;
			}*/
			
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("ERROR generateFileURLforPublic() , filepath=>" + filepath + " , timestamp=>" + String.valueOf(timestamp) + " , isP=>" + isP);
			//System.out.println("ERROR generateFileURLforPublic() , filepath=>" + filepath + " , timestamp=>" + String.valueOf(timestamp) + " , isP=>" + isP);
			return "";
		}
	}
	
	/**
	 * @method xorEncrypt
	 * @author jason.hsiao
	 * @createTime 2013-09-14
	 * @purpose xor 加密
	 * @param password
	 * @return String
	 * @throws Exception
	 */
	public String xorEncrypt(String text) throws Exception{		
		BigInteger bi_text = new BigInteger(text.getBytes());
		BigInteger bi_r0 = new BigInteger(Config.SEED);
		BigInteger bi_r1 = bi_r0.xor(bi_text);
		return bi_r1.toString(Config.RADIX);
	}

	/**
	 * @method generateFileURLforRTMPT
	 * @purpose 產生(trmpt)access url
	 * @param obj eg : {"filepath":"/104plus/83c/0ea/ae5/7a1a3e045a0046dbab761f221c7c0b6114.mp3","videoDefinition":["128k"],"isP":1,"convert":"Success","pid":"jasontest","fileid":"7a1a3e045a0046dbab761f221c7c0b6114","contenttype":4,"mediaType":"m4a","apnum":"0","insertdate":"2013-11-12 16:51:54","title":"測試","imgstatus":"pending","_id":{"$oid":"5281ec2ae4b0dc955010483b"},"description":"測試","filename":"11.mp3"}
	 * @param sec eg : -1384237188 有效期限 
	 * @param quality "480p" or "720p" (for video only)
	 * @return JSONObject
	 * @history 
	 */
	public JSONObject generateFileURLforRTMPT (JSONObject obj, int sec, String quality) {
		mediaUtil media_util = new mediaUtil();
		JSONObject tmp_res = null;
		try {
			tmp_res = new JSONObject(media_util.getUrl(obj, sec, quality));
			Logger.info("INFO generateFileURLforRTMPT() , tmp_res=>" + tmp_res.toString());
			//System.out.println("INFO generateFileURLforRTMPT() , tmp_res=>" + tmp_res.toString());
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("ERROR generateFileURLforRTMPT() , obj=>" + obj + " , sec=>" + sec);
			//System.out.println("ERROR generateFileURLforRTMPT() , obj=>" + obj + " , sec=>" + sec);
		}
		return tmp_res;
		
	}
	
public String decode(String data){
	String decodeString="";
	//改回來
	decodeString = new String(Base64.decodeBase64(data));
	return decodeString;
}

public String encode(String data){
	String encodeString="";
	encodeString = Base64.encodeBase64String(data.getBytes());
	//encodeString = org.apache.commons.codec.binary.Base64.encodeBase64(data.getBytes()).toString();
	return encodeString;
}
	
public JSONObject resolveSingleFileUrl(String fileId, JSONObject obj, JSONObject paramObj, String timestamp, JSONObject fileidUUIDaaObjMap, JSONObject queryFileIdAndUUIDaaMap) throws NumberFormatException, Exception{

		
		List<String> urlArr= new ArrayList<String>();
		
		int isP = 0; //是否為公開,預設為0[非公開]							
		
		long sec = (Long.parseLong(timestamp)-(new java.util.Date().getTime()))/1000;
		
		// List<String> urlArr= new ArrayList<String>();				
		
		
		//2014-01-09 fix for md5 encrypt 
		//start
		//end
		
		String filepath = this.get_file_keypath(obj.getString("filepath"));
		//String filepath = obj.getString("filepath");
		// String filetype = "."+FilenameUtils.getExtension(obj.getString("filepath"));
		
		//String filename = obj.getString("filename");	// 從 filename 取 extension 會有大小寫區分.
		String extFromFileName = this.get_file_extension(obj.getString("filepath"));
		
		
		// String fileCheckExist = "";
		
		
		// parse protocol argument	(add by louis on 2014-04-16)
		Protocol protocol = Protocol.NONE;
		
		if(paramObj.has("protocol")){
			String _protocol = paramObj.getString("protocol");
			
			if(!isEmpty(_protocol)){
				if(_protocol.equals("http"))
					protocol = Protocol.HTTP;
				else if(_protocol.equals("https"))
					protocol = Protocol.HTTPS;
				else if(_protocol.equals("rtmpt"))
					protocol = Protocol.RTMPT;
				else if(_protocol.equals("common"))
					protocol = Protocol.COMMON;
				else
					protocol = Protocol.UNKNOWN;
			}
		}
		
		//modify by JasonHsiao on 2013-09-04
		if(obj.has("isP")){
			int isPValue = obj.getInt("isP");
			if(isPValue == 1){
				isP = isPValue;
			}								
		}
		
		String fileTag = paramObj.has("fileTag") ? paramObj.getString("fileTag") : "";
		
		JSONObject tmp= new JSONObject();
		
		// extension 在組 url 時需要被用到, 所以先取得 tags 資訊.
		/*
         * tag 寬高資訊的錯誤代碼意義:
         * 
         * -1:	指定的 tag 不存在.`
         * -2:	tags 欄位不存在.
         * -3:	指定的 tag 不存在寬或高的資訊(欄位).
         */
        
        // 若請求參數的 tag 名稱是空值 (原圖), 則以 "origin" 作為 key 來抓取對應的 tag 資訊 (原圖寬高資訊是固定以這個 key 來作為識別).
        String _tag = isEmpty(fileTag) ? "origin" : fileTag;
        if(obj.has("tags"))
        {					 
        	JSONObject tags = obj.getJSONObject("tags");
        	// 以 -1 來表示有 tags 但無寬高資訊. (在 putFile content type = 1 的原圖不可讀時, 也會將值設為  -1.
        	if(tags.has("origin")){
        		JSONObject origin = obj.getJSONObject("tags").getJSONObject("origin");
        		tmp.put("origWidth", origin.has("width")?origin.getInt("width"):-3);
            	tmp.put("origHeight", origin.has("height")?origin.getInt("height"):-3);
        	}
        	else{
        		tmp.put("origWidth", -1);
            	tmp.put("origHeight", -1);
        	}
        	
        	if(tags.has(_tag)){
        		JSONObject tag = obj.getJSONObject("tags").getJSONObject(_tag);
        		tmp.put("width", tag.has("width")?tag.getInt("width"):-3);
            	tmp.put("height", tag.has("height")?tag.getInt("height"):-3);
            	
            	// tags 中 _tag 如果沒有 extension 資訊, 就採用原檔副檔名.
            	// String tagExtension = tag.has("extension") ? tag.getString("extension") : extFromFileName;
            	// 若 extension 為 "tmp", 就設為原始副檔名.
            	// if(tagExtension.equals("tmp")) tagExtension = extFromFileName;
            	
            	tmp.put("fileExtension", tag.has("extension") ? tag.getString("extension") : extFromFileName);
        	}
        	else{
        		tmp.put("width", -1);
        		tmp.put("height", -1);
        		tmp.put("fileExtension", extFromFileName);
        	}
		}
        else{
        	// 以 -2 來表示 tags 不存在.
        	tmp.put("origWidth", -2);
        	tmp.put("origHeight", -2);
        	tmp.put("width", -2);
        	tmp.put("height", -2);
        	tmp.put("fileExtension", extFromFileName);
        }
                
        

 		// 指定 target 參數時, 直接設置相對應檔案.
 		String targetFileName = null;
 		if(paramObj.has("target")){			
 			String target = paramObj.getString("target");
 			Logger.info("user call getFileUrl with target parameter => " + target);
 			
 			if(target.equals("origin")){
 				targetFileName = obj.getString("filepath");
 			}
 			// 僅 Doc Type 允許取得 PDF.
 			else if(target.equals("pdf") && obj.getInt("contenttype") == ContentType.Doc){
 				targetFileName = filepath + ".pdf";
 			}
 		}
     		
        // 若指定了 target= {"origin" | "pdf"}, 則以 http, https 或 common 型式輸出指定檔案 {原始檔 or PDF檔}.
        if(targetFileName != null){
			
			Protocol targetProtocol = Protocol.HTTP;
			if(protocol != Protocol.HTTP && protocol != Protocol.HTTPS && protocol != Protocol.COMMON)
				targetProtocol = protocol;
			
			urlArr.add(this.generateFileURLforPublic(targetFileName, Long.parseLong(timestamp), isP, targetProtocol));
			
			// 放入原圖長寬資訊.
			if(tmp.has("origWidth")) tmp.put("width", tmp.getInt("origWidth"));
			if(tmp.has("origHeight")) tmp.put("height", tmp.getInt("origHeight"));
		}
		else{
	        String filetype = "."+ tmp.getString("fileExtension");
	          
			JSONObject tmp_obj = null;
			switch(obj.getInt("contenttype")){
				case ContentType.Image://圖片																		
					if(!isEmpty(fileTag)) {
						filepath += "_" + fileTag;
					}
					
					// fileCheckExist = filepath + filetype;
					
					urlArr.add(this.generateFileURLforPublic(filepath + filetype, Long.parseLong(timestamp), isP, protocol));
					
					//urlArr.add(tools.generateFileURLforPublic(filepath + filetype,Long.parseLong(timestamp), isP, protocol));
					
				break;
				case ContentType.Doc://文件
					//System.out.println("DEBUG FileManager getFileUrl , ENTER adding url String , contenttype=>2 , queryFileId=>" + msql + " , mongoResult=>" + mongoResult);
					
					//判斷是否有page
					//modify by JasonHsiao on 2013-07-04 check if there is pagesize
					if(obj.has("pagesize")){
						if(obj.get("pagesize").toString().equals("")){//給原始檔  轉檔失敗	
							//modify by JasonHsiao on 2013-09-14
							//2014-01-09 fix for md5 encrypt
							
							// fileCheckExist = filepath + filetype;
							urlArr.add(this.generateFileURLforPublic(filepath + filetype,Long.parseLong(timestamp), isP, protocol));
							//System.out.println("ERROR FileManager getFileUrl , has PageSize key but empty value , convert pageSize Fail!! , contenttype=>2 , queryFileId=>" + msql + " , mongoResult=>" + mongoResult);
							
							break;
						}			
						
						if(!isEmpty(fileTag)) {
							//判斷filetag 是否為cover 或單一頁
							filepath += "_" + fileTag;				
							
							// JSONObject tags = (obj.has("tags"))?new JSONObject(obj.get("tags").toString()):null;
							
							JSONObject tags = null;
							
							try{
								tags = (obj.has("tags"))?new JSONObject(obj.get("tags").toString()):null;
							}
							catch(Exception e){
								Logger.error("cannot parse tags into json object", e);
							}
							
							//modify by JasonHsiao on 2013-08-19 bug fix when tags is null
							if(tags != null && tags.has(fileTag)){
								JSONObject fileTagObj = tags.getJSONObject(fileTag);
								String isCovert = fileTagObj.get("isCover").toString();
								String page = fileTagObj.get("page").toString();
								
								if(isCovert.equals("1") || !page.equals("-1")){
									if(!page.equals("-1") && isCovert.equals("0")){//單一頁													
										//整份
										// fileCheckExist = filepath + "-" + page+".jpg";
										String urlString = this.generateFileURLforPublic(filepath + "-" + page+".jpg",Long.parseLong(timestamp), isP, protocol);
										urlArr.add(urlString);
										//System.out.println("DEBUG FileManager getFileUrl , page url String , contenttype=>2 , queryFileId=>" + msql + " , mongoResult=>" + mongoResult + " , urlString=>" + urlString);													
									}else{
										//封面
										// fileCheckExist = filepath + ".jpg";
										String urlString = this.generateFileURLforPublic(filepath + ".jpg",Long.parseLong(timestamp), isP, protocol);
										urlArr.add(urlString);
										//System.out.println("DEBUG FileManager getFileUrl , cover url String , contenttype=>2 , queryFileId=>" + msql + " , mongoResult=>" + mongoResult + " , urlString=>" + urlString);
									}
									break;
								}
							}											
						}
						
						// fileCheckExist = filepath + "-" + "1" + ".jpg";
						//原檔取出需要改回來
						/*for(int x = 1; x <= obj.getInt("pagesize"); x++){
							urlArr.add(this.generateFileURLforPublic(filepath +"-"+x+".jpg",Long.parseLong(timestamp), isP, protocol));						
						}	*/
						urlArr.add(this.generateFileURLforPublic(filepath +".doc",Long.parseLong(timestamp), isP, protocol));	
					}else{
						//System.out.println("WARN FileManager getFileUrl() " + DateUtil.getDateTimeForLog() + " ,  missing PageSize key!! , contenttype=>2 , queryFileId=>" + msql + " , mongoResult=>" + mongoResult);
						//原檔取出需要改回來
						urlArr.add(this.generateFileURLforPublic(filepath +".doc",Long.parseLong(timestamp), isP, protocol));	
						//Logger.warn("WARN FileManager getFileUrl() " + DateUtil.getDateTimeForLog() + " ,  missing PageSize key!! , contenttype=>2 , user object => " + obj);
					}
				break;
				case ContentType.Audio:
					// modify by jj on 2013-11-12
					/*
					fileCheckExist = filepath + "_v1_128k.m4a";
					tmp_obj = tools.generateFileURLforRTMPT(obj, (int)sec);
				    urlArr.add(tmp_obj.getString("url"));
				    */
					
				    //ready for audio url rtmpt and http switch (default rtmpt
					
					
					switch(protocol){
						case NONE:
							tmp_obj = this.generateFileURLforRTMPT(obj, (int)sec, null);
						    urlArr.add(tmp_obj.getString("url"));
							break;
							
						case HTTP:
						case HTTPS:
						case COMMON:
							//要改回m4a和Filepath
							filetype = ".mp3";
							//filepath = filepath +"_v1_128k";
							//2014-01-09 fix for md5 encrypt											
							// fileCheckExist = filepath + "_v1_128k.m4a";
				    		urlArr.add(this.generateFileURLforPublic(filepath + filetype,Long.parseLong(timestamp), 1, protocol));
							break;
						case RTMPT:
							tmp_obj = this.generateFileURLforRTMPT(obj, (int)sec, null);
						    urlArr.add(tmp_obj.getString("url"));
							break;
							
						default:
							Logger.error("ERROR on FileManager getFileUrl("+paramObj+", "+ timestamp+") , " + DateUtil.getDateTimeForLog() + " error protocol.");
							//logger.error("ERROR on FileManager getFileUrl("+jsonObj+", "+ timestamp+") , " + DateUtil.getDateTimeForLog() + "get audio protocol value error -->"+ jsonarr.getJSONObject(i).getString("protocol"));
					}								
				    
				break;
				case ContentType.WbVideo:									
				case ContentType.Video://影片
					
					String quality = paramObj.optString("quality", "480p");
					JSONObject videoQuality = obj.optJSONObject("videoQuality");
					
					if(videoQuality != null){
						// 具有 videoQuality 屬性的話, 若請求未指定 quality 就抓第一個影片.
						if(!videoQuality.has(quality)){
							Logger.warn("specified video quality [" + quality + "] does not exist, automatically pick first quality as output.");
							if(videoQuality.length() > 0){
								String detectedQuality = (String)videoQuality.keys().next();
								Logger.warn("use [" + detectedQuality + "] instead [" + quality + "].");
								quality = detectedQuality;
							}
						}
					}
					
					if(!isEmpty(fileTag)) {//取截圖 url
						filepath += "_"+fileTag;
						filetype = ".jpg";
						
						// fileCheckExist = filepath + filetype;
						urlArr.add(this.generateFileURLforPublic(filepath + filetype,Long.parseLong(timestamp), 1, protocol));
						
					} 
					else {//取原檔	
						
						switch(protocol){
							case NONE:
								filetype = ".mp4";
								// filepath = filepath +"_v1_480p";
								filepath = filepath +"_v1_" + quality;
								//2014-01-09 fix for md5 encrypt												
								// fileCheckExist = filepath + filetype;
								urlArr.add(this.generateFileURLforPublic(filepath + filetype,Long.parseLong(timestamp), 1, protocol));
								break;
								
							case HTTP:
							case HTTPS:
							case COMMON:
								filetype = ".wmv";//要改回mp4
								// filepath = filepath +"_v1_480p";
								//TODO Johnson 記得補回來，現在還沒有轉檔
								//filepath = filepath +"_v1_" + quality;
								
								//2014-01-09 fix for md5 encrypt												
								// fileCheckExist = filepath + "_v1_480p.mp4";
								urlArr.add(this.generateFileURLforPublic(filepath + filetype,Long.parseLong(timestamp), 1, protocol));
								break;
								
							case RTMPT:
								// fileCheckExist = filepath + "_v1_480p.mp4";
								tmp_obj = this.generateFileURLforRTMPT(obj, (int)sec, quality);
							    urlArr.add(tmp_obj.getString("url"));
								break;
								
							default:
								Logger.error("ERROR on FileManager getFileUrl("+paramObj+", "+ timestamp+") , " + DateUtil.getDateTimeForLog() + " error protocol.");
						}										
					}
				break;
				case ContentType.WbAudio://白板音檔
				break;
			}
		}
		//add by JasonHsiao on 2013-6-26
		String convertDB_data = "";
		Object convertObj = null;
		if(obj.has("convert")){
			convertObj = obj.get("convert");
			if(convertObj != null){
				convertDB_data = convertObj.toString();
			}	
		}
		
		tmp.put("convert",convertDB_data.toLowerCase());
		
        //String fileExist = this.tools.get_file_exists(Config.ROOT + fileCheckExist);
        //String fileExtension = this.tools.get_file_extension(filename);
		
        //System.out.println("fileExtension==>"+ fileExtension);

		if(fileidUUIDaaObjMap.has(fileId)){
//			logger.info(fileId + " in map.");
			JSONObject uuidaaObj = fileidUUIDaaObjMap.getJSONObject(fileId);
			tmp.put("fileId", uuidaaObj.getString("UUIDaa"));
			
			if(isEmpty(convertDB_data)){		// 以 user 為主, 無 convert 資料才採用 uuidaaObj 的轉檔資訊.
				tmp.put("convert", uuidaaObj.getString("convert"));
			}
			
			if(uuidaaObj.has("msg"))
				tmp.put("msg", uuidaaObj.getString("msg"));
		}
		// 當 redis 具有 fUrl:aa:{fileidaa} 的 key 時, fileidUUIDaaObjMap 會是 empty, 
		// 這時會以這個 map 來將 fileid 跟 fileidaa 做交換. 回傳 user 請求時的 fileid.
		else if(queryFileIdAndUUIDaaMap.has(fileId)){						
			tmp.put("fileId", queryFileIdAndUUIDaaMap.getString(fileId));
		}
		else{
//			logger.info(fileId + " not in map.");
			tmp.put("fileId",fileId);
		}
		
//		{
//			 fileId:"1ca697ae90124a06a96e821cbdd48d9201"
//			 UUIDaa:"735f346fd98f406e956bbe75d31e0519aa"
//			 convert:"succcess, fail, pending"
//			 msg:""
//			 }
		
		//2014-01-24 replace filed to fileidaa
//		boolean flag = false;
//		for (int p = 0; p < fileidFileaaMapArr.length(); p++){
//            if (fileidFileaaMapArr.getJSONObject(p).has(fileId)){
//        		tmp.put("fileId",fileidFileaaMapArr.getJSONObject(p).get(fileId).toString());
//            	flag = true;
//            	// logger.info("replace fileid ==> "+fileId+" to fileidaa ==> "+fileidFileaaMapArr.getJSONObject(p).get(fileId).toString());	
//            }
//        }
//		if (flag == false) {
//			tmp.put("fileId",fileId);
//		}
		
		//add by JasonHsiao on 2013-6-26
//		tmp.put("convert",convertDB_data.toLowerCase());
		tmp.put("tag", fileTag);
		tmp.put("url", urlArr);
//        tmp.put("fileExtension", fileExtension);
        
        
        
        
        return tmp;
	}	

//產生/104plus/開始的file path
	public String generateFilePathForMount(String filepath){
		try{		
			filepath = filepath.substring(filepath.lastIndexOf("/104plus"),filepath.length());			
		}catch(Exception e){}
		return filepath;
	}
	
	//checkContentType
	public int getContentType(String ContentTypeString){
		//待續，需補寫其他型態
		switch (ContentTypeString.toLowerCase()) {
		case "image/jpeg":
			return ContentType.Image;
		case "video/mpeg":
			return ContentType.Video;
		case "application/msword":
			return ContentType.Doc;
		case "audio/mpeg":
			return ContentType.Audio;
			
		default:
			return 0;//找不到型態
		}
		
	}
	
	//checkContentType
		public String getContentType(int ContentTypeString){
			//待續，需補寫其他型態
			switch (ContentTypeString) {
			case ContentType.Image:
				return "image/jpeg";
			case ContentType.Video:
				return "video/mpeg";
			case ContentType.Doc:
				return "application/msword";
			
			default:
				return "";//找不到型態
			}
			
		}
	//產生txid
	public String generateTxid(){
		return UUID.randomUUID().toString();
	}
	
	
	/**
	 * @method generateFileId
	 * @param contenttype
	 * @param isP
	 * @return String
	 * @history 1:modify by JasonHsiao on 2013-09-14
	 * 				A:新增參數isP=>0非公開1公開
	 * 				B:依照isP值修改s_contenttype第一碼
	 */
	public String generateFileId(int contenttype,int isP){
		String s_contenttype = String.valueOf(contenttype);
		if(s_contenttype.length() == 1) {
			if(isP == 1){
				//公開
				s_contenttype = "1" + s_contenttype;
			}else{
				//非公開
				s_contenttype = "0" + s_contenttype;
			}
		}
		//fileId insert DynamoDB need Binary Type
		return UUID.randomUUID().toString().replaceAll("-", "")+s_contenttype;
		//return UUID.randomUUID().toString().replaceAll("-", "")+s_contenttype;
	}
	
	public String generateFilePath(String fid){
		String returnPath = "";
		try{
			fid = this.md5(fid);
			String layer1 = fid.substring(0,3);
			String layer2 = fid.substring(3,6);
			String layer3 = fid.substring(6,9);
			returnPath =  layer1 + "/" + layer2 + "/" + layer3 + "/";
		
			//File directory = new File(returnPath);
			//directory.mkdirs();
		}catch(Exception e){}
		return returnPath;
	}
	
	/**
	 * 產生check tag
	 * @param tag (a~z, A~Z, 0~9 的英數字)
	 * @return
	 */
	public boolean checkTag(String tag){
		boolean msg = false;
		try {
			for(int i=0;i<tag.length();i++) {
				if(((int)tag.charAt(i)>=48 && (int)tag.charAt(i)<=57) || ((int)tag.charAt(i)>=65 && (int)tag.charAt(i)<=90) || ((int)tag.charAt(i)>=97 && (int)tag.charAt(i)<=122)|| ((int)tag.charAt(i)==45)) {
					msg = true;
				}
				else {
					msg = false;
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return msg;
	}
	
	public Map<String, String> json2Map(JSONObject object){
		Iterator<String> keysItr = object.keys();
		Map<String,String> data = new HashMap<String,String>();
		while(keysItr.hasNext()) {
			String key = keysItr.next();
		    String value = object.getString(key);
		    data.put(key, value);
		}
		
		return data;
	}
	
	public Map<String, String> json2MapObj(JSONObject object){
		Iterator<String> keysItr = object.keys();
		Map<String,String> data = new HashMap<String,String>();
		while(keysItr.hasNext()) {
			String key = keysItr.next();
		    String value = object.getJSONObject(key).toString();
		    data.put(key, value);
		}
		
		return data;
	}
	
	public String getUploadConfig(String extraNo){
		DynamoService dynamoService = new DynamoService();
		//dynamoService.
		return "";
	}
	
	
	
	public String getCurrentUTCTimestamp(Byte timeZoneType) {
		SimpleDateFormat dateFormatter;
		final TimeZone utc = TimeZone.getTimeZone("UTC");
		long returnTime = new java.util.Date().getTime();
		switch (timeZoneType) {
		//RFC3999Date格式
		case 1:
			dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			//S3 Policy expiration Time
			returnTime+=(30*1000);
			break;

		default:
			dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS z");
			break;
		}
		dateFormatter.setTimeZone(utc);

	    return dateFormatter.format(returnTime);
	}
	/*
	public String getProcessById(String processId) {// 取得processId內容
		Logger.info("Enter WBFileConvert getProcessById, " + DateUtil.getDateTimeForLog() + ", processId==>"+ processId);
		MongoDBDispatch db_object = new MongoDBDispatch("wb");
		JSONObject res = null;
		JSONObject query = new JSONObject();
		try {
			query.put("processId", processId);
			res = (new JSONArray(db_object.fileSelect(query))).getJSONObject(0);
		} catch (JSONException e) {
			// 回傳失敗結果
			// TODO Auto-generated catch block
			e.printStackTrace();
	    	java.io.StringWriter sw = new java.io.StringWriter();
			java.io.PrintWriter pw = new java.io.PrintWriter(sw);
			e.printStackTrace(pw);
			logger.error("Exception WBFileConvert getProcessById, Exception==>"+ e.toString());
		}
		logger.info("Exit WBFileConvert getProcessById, " + DateUtil.getDateTimeForLog() + ", res.toString()==>"+ res.toString());
		return res.toString();
	}
	*/
	  public static void main(String[] args) {
	        System.out.println(new tools().getCurrentUTCTimestamp((byte)1)); // Display the string.
	        System.out.println(new tools().getCurrentUTCTimestamp((byte)0)); // Display the string.
	    }
	
}
