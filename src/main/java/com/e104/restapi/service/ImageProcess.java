package com.e104.restapi.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.e104.errorhandling.DocApplicationException;
import com.e104.restapi.model.GetFileArr;
import com.e104.restapi.model.GetFileUrl;
import com.e104.util.Config;
import com.e104.util.Convert;
import com.e104.util.DateUtil;
import com.e104.util.DynamoService;
//TODO Johnson確認是否還需要import
/*
import com.e104.util.FileConvert;
import com.e104.util.FileManage;
import com.e104.util.VideoService;
*/
import com.e104.util.tools;

public class ImageProcess {
	private static transient Logger logger = LogManager.getLogger(ImageProcess.class);
	tools tools = new tools();
	/**
	 * 用於 multiaction 解析 tag 檔案相依性資訊的工具.
	 * @param fileDetail
	 * @param refTag
	 * @param tag
	 * @param toType
	 * @return
	 * @throws JSONException 
	 */
	private Map<String, String> resolveFilePathAndExtension(JSONObject fileDetail, String refTag, String tag, String fromType, String toType) throws JSONException{
		
		logger.info(String.format("resolveFilePathAndExtension: fileId => %s, refTag => %s, tag => %s, fromType => %s, toType => %s", fileDetail.getString("fileid"), refTag, tag, fromType, toType));
		
		int contenttype = fileDetail.getInt("contenttype");
		
		String origFilepath = Config.ROOT + fileDetail.getString("filepath");// ex:/mnt/mfs/stream/104plus/104/2/898ee95e60624be093b1019f1679dd36.ppt
		String refFilepath = tools.isEmpty(refTag) ? origFilepath : origFilepath.substring(0,origFilepath.lastIndexOf('.'))+ "_" + refTag + origFilepath.substring(origFilepath.lastIndexOf('.'));
		String targetFilepath = null;
		// if(fileTag != null && !fileTag.equals(""))	filepath = filepath.substring(0,filepath.lastIndexOf('.'))+ "_" + fileTag + filepath.substring(filepath.lastIndexOf('.'));
		
		//modify by JasonHsiao on 2013-08-23,bug fix when if contenttype != 1 (use .jpg) else (use origin file extension)
		String origFileExtension = "jpg";
		String refFileExtension = origFileExtension;		// 設預設參考類型為原始上傳的檔案類型
		String targetFileExtension = refFileExtension;		// 設預設參考類型為原始上傳的檔案類型
		
		if(contenttype != 1){
			//contenttype != 1 時將附檔名取代成.jpg
			refFilepath = refFilepath.substring(0,refFilepath.lastIndexOf('.')) + ".jpg";	
		}else{
			// 若上傳的類檔是 "影像" (contenttype = 1), 動態檢查參考檔案類型, 並且允許指定目標檔案類型.
			// origFileExtension = origFilepath.substring(origFilepath.lastIndexOf('.'));
			origFileExtension = origFilepath.substring(origFilepath.lastIndexOf('.') + 1);	
			
			// 取出 refTag 的 file extension, 用以決定實體參照檔案完整路徑.
			JSONObject tags = fileDetail.getJSONObject("tags");
			
			if(tags.has(refTag) && tags.getJSONObject(refTag).has("extension"))
				refFileExtension = tags.getJSONObject(refTag).getString("extension");
			else
				refFileExtension = origFileExtension;
			
			// 未指定目標類型, 則輸出類型與參考類型一致.
			// targetFileExtension = isEmpty(toType) ? refFileExtension : toType;
			
			// 若未指定 fromType 或 fromType 符合參照檔類型, 才依照 toType 轉換檔案類型.
			
			if(!tools.isEmpty(toType) &&  (tools.isEmpty(fromType) || refFileExtension.toLowerCase().equals(fromType.toLowerCase())))
				targetFileExtension = toType;
			else
				targetFileExtension = refFileExtension;
			
			
			refFilepath = refFilepath.substring(0,refFilepath.lastIndexOf('.')) + "." + refFileExtension;
			
		}
		
		targetFilepath = origFilepath.substring(0,origFilepath.lastIndexOf(".")) + "_" + tag + "." + targetFileExtension;
		//.jpg轉檔透明度會失效，改為轉成png
		String modTargetFilepath = targetFilepath.toLowerCase().endsWith(".jpg") ? targetFilepath.substring(0,targetFilepath.toLowerCase().lastIndexOf(".jpg")) + ".png":targetFilepath;
		Map<String, String> map = new HashMap<String, String>();
		
		map.put("FILE_PATH_ORIGIN", origFilepath);
		map.put("FILE_PATH_REF", refFilepath);
		
		map.put("FILE_PATH_TARGET", modTargetFilepath);
		map.put("FILE_PATH_ORIGIN_TARGET", targetFilepath);
		
		
		map.put("FILE_EXT_ORIGIN", origFileExtension);
		map.put("FILE_EXT_REF", refFileExtension);
		map.put("FILE_EXT_TARGET", targetFileExtension);
		
		// debug log.
		logger.info("FILE_PATH_ORIGIN => " + origFilepath);
		logger.info("FILE_PATH_REF => " + refFilepath);
		logger.info("FILE_PATH_REF => " + modTargetFilepath);		
		logger.info("FILE_PATH_ORIGIN_TARGET => " + targetFilepath);		
		logger.info("FILE_EXT_ORIGIN => " + origFileExtension);
		logger.info("FILE_EXT_REF => " + refFileExtension);
		logger.info("FILE_EXT_TARGET => " + targetFileExtension);
		
		return map;
	}
	/**
	 * @method multiAction
	 * @param fileId
	 * @param jsonObj
	 * @return JSONObject
	 * @history 1:modify by JasonHsiao on 2013-08-01
	 * 				A:Add log
	 *			2. modify by JasonHsiao on 2013-10-14
	 *				A:change to log4j
	 */
	public String multiAction(String fileId,String jsonObj){		
		// private FileManageDispatch db = new FileManageDispatch();
		
		JSONObject rtn = new JSONObject();
		
		if((fileId != null) && (!"".equals(fileId.trim())) && (jsonObj != null && !"".equals(jsonObj.trim()))){
			
			//System.out.println("Enter ImageProcess multiAction , " + DateUtil.getDateTimeForLog() + " fileId=>" + fileId + ", jsonObj=>" + jsonObj);			
			logger.info("Enter ImageProcess multiAction , " + DateUtil.getDateTimeForLog() + " fileId=>" + fileId + ", jsonObj=>" + jsonObj);
			
			try {
				
				JSONArray actionArr = new JSONArray(jsonObj);			
				JSONObject filelist = new JSONObject();		
				
				Convert convert = Convert.fromDB(fileId);
				DynamoService dynamoService = new DynamoService();
				JSONObject statusObj = new JSONObject();
				//call API
				for(int i=0;i<actionArr.length();i++){
					JSONObject obj = actionArr.getJSONObject(i);
					String tag = obj.getString("tag");
					
					//isRefNewFileId
					String doActionFileId=fileId;
					if(obj.has("isRefNewFileId") && obj.get("isRefNewFileId").toString().equals("1")){
						if(filelist.has(obj.getString("refTag"))){
							doActionFileId = filelist.getJSONObject(obj.getString("refTag")).getJSONObject("new").get("fileId").toString();
						}
					}
					
					JSONObject res = this.doAction(doActionFileId,obj);
					
					
					String convertStatus = "fail";
					
					
					if(res!=null){
						//{ "tag1":{"origin": {"fileId": "xx", "url": "xxx" }}
						filelist.put(tag, res);
						// update convert collection
						if(res.has("status") && res.getString("status").equalsIgnoreCase("success")){
							convertStatus = "success";
						}		
						statusObj.put(tag, convertStatus);
					}else{
						filelist.put(obj.getString("tag"), res);	
						
						JSONObject err = new JSONObject();
						err.put("code", "10001");
						err.put("httpstatus", "500");
						err.put("type", "APIError");
						err.put("message", "method error : "+obj.getString("method"));
						
						err.put("fileList", filelist);		// 2014-11-03  發生錯誤時, 將 fileList 放置在 error 下.
						
						rtn.put("error",err);
						rtn.put("res", res);						
						
						System.out.println("ERROR ImageProcess multiAction , " + DateUtil.getDateTimeForLog() + " , errorMethod=>" + obj.getString("method") + " , error=>" + err + " , res=>" + res);
						//Result: [flush, flushBuffer, write]
						return rtn.toString();
					}
					
					// update 'convert' collection
					if(convert != null){
						//TODO Johnson確認改為Dynamo後，作法異動上有無問題
						//convert.setStatus(tag, convertStatus);
						//convert.updateDB();
						
						
						
					}
				}
				//TODO Johnson Update status in 'convert' collection,Check update is need 
				//dynamoService.updateItem("convert", fileId, "status", statusObj);
				
				
				// "filelist":{ "tag1":{"origin":{"fileId": "xxx", "url": "xxx"}, "new":{"fileId": "xxx","url": "xxx"} }		
				rtn.put("status", "Success");
				rtn.put("fileList", filelist);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("ERROR ImageProcess multiAction , " + DateUtil.getDateTimeForLog() + " " + e.toString());
				return "{\"status\":\"multiAction Exception\",\"error\":" + e.toString() + "}";
			}
		}else{
			logger.error("ERROR on ImageProcess , " + DateUtil.getDateTimeForLog() + " , multiAction() empty parameter");
			return "{\"status\":\"multiAction Exception\",\"error\":\"empty parameter\"}";
		}

		logger.info("Exit ImageProcess multiAction , " + DateUtil.getDateTimeForLog() + " return=>" + rtn.toString() + ", fileId=>" + fileId + ", jsonObj=>" + jsonObj);

		return rtn.toString();
	}
	
	/**
	 * @method doAction
	 * @param fileId
	 * @param obj
	 * @return JSONObject
	 * @history 1:modify by JasonHsiao on 2013-07-24
	 * 				A:finish undo video stub for videoImageSize
	 * 			2:modify by JasonHsiao on 2013-08-01
	 * 				A:add log
	 *			4. modify by JasonHsiao on 2013-10-14
	 *				A:change to log4j
	 */
	private JSONObject doAction(String fileId,JSONObject obj) {		
		// -- obj 將會多出 toType 參數來指定目標的檔案類型.  resize & picCrop method 可支援轉換成對應的類型.
		JSONObject rtn = new JSONObject();
		
		//System.out.println("Enter ImageProcess doAction , " + DateUtil.getDateTimeForLog() + " fileId=>" + fileId + ", obj=>" + obj);
		logger.info("Enter ImageProcess doAction , " + DateUtil.getDateTimeForLog() + " fileId=>" + fileId + ", obj=>" + obj);
		
		try {
			String tag =  obj.getString("tag");//tag 為產生檔案時，使用者自定義的名稱
			if(!tools.checkTag(tag)) {
				rtn.put("error","tag不合法");
				logger.error("DEBUG ImageProcess doAction , " + DateUtil.getDateTimeForLog() + " tag invalid!! fileId=>" + fileId + ", obj=>" + obj);
				return rtn;
			}
			
			
			String toTypeObj = obj.has("toTypeObj") ? obj.getJSONObject("toTypeObj").toString() : null;
			
			
			String fileTag =  (obj.has("refTag"))?obj.getString("refTag"):"";//來源檔案標籤
			
			fileTag =  (obj.has("isRefNewFileId") && obj.getInt("isRefNewFileId")==1)?"":fileTag;//參照新fileId，fileTag為空字串
			
			int  isNewFileId =(obj.has("isNewFileId"))? Integer.valueOf(obj.get("isNewFileId").toString()):0;				
			
			int  isSave =(obj.has("isSave"))?Integer.valueOf(obj.get("isSave").toString()):1;
			
			int  isGetUrl =(obj.has("isGetUrl"))?Integer.valueOf(obj.get("isGetUrl").toString()):1;
			
			int  isSSL =(obj.has("isSSL"))?Integer.valueOf(obj.get("isSSL").toString()):0;
			
			double basis11Ratio = obj.optDouble("basis11Ratio", 0);
			
			long urlExpiredTime  = 0L ; //預設3分鐘 單位毫秒
			
			if(obj.has("urlExpiredTime")){
				urlExpiredTime = Long.valueOf(obj.get("urlExpiredTime").toString());
			}
			else {
				urlExpiredTime = new Date().getTime() + 30000L;
			}
			
			String response="";
			String newFileId="";			
			String status = "";
			String error = "";
			

			JSONObject o_response = new JSONObject();
			//TODO Johnson:先不處理Method的議題暫時Pass
			/*
			if(obj.getString("method").equals("videoSnap")){
				//System.out.println("DEBUG ImageProcess doAction() , ENTER method 'videoSnap' for calling video.getImg()  , " + DateUtil.getDateTimeForLog() + " , fildId=>" + fileId + ", obj=>" + obj);
				
				logger.info("videoSnap for fileid => " + fileId);
				
				//call video getImage
				JSONObject tmp = new JSONObject();
				JSONObject param = new JSONObject();
				
				if(obj.has("width")) tmp.put("width", obj.get("width").toString());
				if(obj.has("height")) tmp.put("height", obj.get("height").toString());				
				if(obj.has("sec")) tmp.put("sec", obj.get("sec").toString());
				tmp.put("tag",tag);				
				
				param.put("fileId", fileId);
				param.put("videoImageSize",new JSONArray().put(tmp));	
				VideoService video = new VideoService();
				String result = video.getImg(param.toString());
        		
				if(isEmpty(result)){
					logger.error("ERROR ImageProcess doAction() , method 'videoSnap', After video.getImg()  response is null or empty String ,  fildId=>" + fileId + ", obj=>" + obj);
				}
				
                logger.info("[videosnap response] " + result);
                o_response = new JSONObject(result);
                status = (o_response.has("status"))?o_response.getString("status"):"";
				error = (o_response.has("errmsg"))?o_response.getString("errmsg"):"";				
				
				logger.info(String.format("status => %s, error => ", status, error));
			}
			
			
			else if(obj.getString("method").equals("docSnap")){
				//System.out.println("DEBUG ImageProcess doAction() , ENTER method 'docSnap' for calling FileConvert.doc2Img  , " + DateUtil.getDateTimeForLog() + " , fildId=>" + fileId + ", obj=>" + obj);
				
				FileConvert fc = new FileConvert();
				response = fc.doc2Img(fileId, obj.getInt("page"), obj.getInt("height"), obj.getInt("width"), tag+"isCover", isSave, 1);
				o_response = new JSONObject(response);
				//todo判斷是否成功
				newFileId = (isNewFileId==1 && o_response.has("newFileId"))?o_response.getString("newFileId"):"";
				status = (o_response.has("status"))?o_response.getString("status"):"";
				error = (o_response.has("error"))?o_response.getString("error"):"";
				
				
				//System.out.println("DEBUG ImageProcess doAction() , Exit method 'docSnap' for calling FileConvert.doc2Img  , " + DateUtil.getDateTimeForLog() + " , fildId=>" + fileId + ", obj=>" + obj);
			}
			
			else if(obj.getString("method").equals("resize")){
				//System.out.println("DEBUG ImageProcess doAction() , ENTER method 'resize' for calling ImageProcess.resize  , " + DateUtil.getDateTimeForLog() + " , fildId=>" + fileId + ", obj=>" + obj);
				
				//modify by JasonHsiao on 2013-07-18 , set reduceOnly parameter to resize method in obj 
				int reduceOnly = 0;
				if(obj.has("reduceOnly")){
					reduceOnly = Integer.valueOf(obj.get("reduceOnly").toString());
					//System.out.println("ImageProcess doAction() , " + DateUtil.getDateTimeForLog() + " , obj has reduceOnly , value is=>" + reduceOnly);					
				}else{
					logger.info("ImageProcess doAction() , " + DateUtil.getDateTimeForLog() + " , obj doesn't have reduceOnly , set default value to 0");
				}
				//System.out.println("DEBUG ImageProcess doAction() , ENTER method 'resize' Before calling ImageProcess.resize  , " + DateUtil.getDateTimeForLog() + " , fildId=>" + fileId + ", width=>" + obj.get("width").toString() + "height=>" + obj.getString("height").toString() + ",basis=>" + obj.get("basis").toString() + ",tag=>" + tag + ",fileTag=>" + fileTag + ",isNewFileId=>" + isNewFileId + ",isSave=>" + isSave + ",reduceOnly=>" + reduceOnly);
				response = this.resize(fileId, Integer.valueOf(obj.get("width").toString()),Integer.valueOf( obj.getString("height").toString()),Integer.valueOf(obj.get("basis").toString()) ,tag, fileTag,isNewFileId , isSave, reduceOnly, toTypeObj, basis11Ratio);
				//System.out.println("DEBUG ImageProcess doAction() , ENTER method 'resize' After calling ImageProcess.resize  , " + DateUtil.getDateTimeForLog() + " , fildId=>" + fileId + ", width=>" + obj.get("width").toString() + "height=>" + obj.getString("height").toString() + ",basis=>" + obj.get("basis").toString() + ",tag=>" + tag + ",fileTag=>" + fileTag + ",isNewFileId=>" + isNewFileId + ",isSave=>" + isSave + ",reduceOnly=>" + reduceOnly);
				//System.out.println(response.toString());
				o_response = new JSONObject(response);
				//todo判斷是否成功
				newFileId = (isNewFileId==1 && o_response.has("newFileId"))?o_response.getString("newFileId"):"";
				status = (o_response.has("status"))?o_response.getString("status"):"";
				error = (o_response.has("error"))?o_response.getString("error"):"";
				
				//System.out.println("DEBUG ImageProcess doAction() , Exit method 'resize' for calling ImageProcess.resize  , " + DateUtil.getDateTimeForLog() + " , fildId=>" + fileId + ", obj=>" + obj);
			}
			
			else if(obj.getString("method").equals("picCrop")){
				//System.out.println("DEBUG ImageProcess doAction() , ENTER method 'picCrop' for calling ImageProcess.picCrop  , " + DateUtil.getDateTimeForLog() + " , fildId=>" + fileId + ", obj=>" + obj);
				
				if(!obj.getString("startPoint").equals("") && !obj.getString("endPoint").equals("")) {
					//System.out.println("DEBUG ImageProcess doAction() , method 'picCrop' Before calling ImageProcess.picCrop  , " + DateUtil.getDateTimeForLog() + " fileId=>" + fileId + ", startPoint=>" + "{\"startPoint\""+":"+obj.getString("startPoint")+"}" + ", endpoint=>" + "{\"endPoint\""+":"+obj.getString("endPoint")+"}" + ", shape=>" + obj.get("shape").toString() + " , isNewFileId=>" + isNewFileId + ", isSave=>" + isSave);
					response = this.picCrop(fileId, "{\"startPoint\""+":"+obj.getString("startPoint")+"}", "{\"endPoint\""+":"+obj.getString("endPoint")+"}",tag,fileTag ,Integer.valueOf(obj.get("shape").toString())  ,  isNewFileId , isSave, toTypeObj);
					//System.out.println("DEBUG ImageProcess doAction() , method 'picCrop' After calling ImageProcess.picCrop  , " + DateUtil.getDateTimeForLog() + " fileId=>" + fileId + ", startPoint=>" + "{\"startPoint\""+":"+obj.getString("startPoint")+"}" + ", endpoint=>" + "{\"endPoint\""+":"+obj.getString("endPoint")+"}" + ", shape=>" + obj.get("shape").toString() + " , isNewFileId=>" + isNewFileId + ", isSave=>" + isSave);
				}else{
					//System.out.println("DEBUG ImageProcess doAction() , method 'picCrop' Before calling ImageProcess.picCrop  , " + DateUtil.getDateTimeForLog() + " , fileId=>" + fileId + " ,  ,  , tag=>" + tag + ", fileTag=>" + fileTag + " , shape=>" + obj.get("shape").toString() + " , isNewFileId=>" + isNewFileId + " , isSave=>" + isSave);
					response = this.picCrop(fileId, "", "",tag,fileTag ,Integer.valueOf(obj.get("shape").toString())  ,  isNewFileId , isSave, toTypeObj);
					//System.out.println("DEBUG ImageProcess doAction() , method 'picCrop' After calling ImageProcess.picCrop  , " + DateUtil.getDateTimeForLog() + " , fileId=>" + fileId + " ,  ,  , tag=>" + tag + ", fileTag=>" + fileTag + " , shape=>" + obj.get("shape").toString() + " , isNewFileId=>" + isNewFileId + " , isSave=>" + isSave);
				}
				
				o_response = new JSONObject(response);
				//todo 判斷是否成功
				newFileId = (isNewFileId==1 && o_response.has("newFileId"))?o_response.getString("newFileId"):"";
				status = (o_response.has("status"))?o_response.getString("status"):"";
				error = (o_response.has("error"))?o_response.getString("error"):"";
				
				//System.out.println("DEBUG ImageProcess doAction() , Exit method 'picCrop' for calling ImageProcess.picCrop  , " + DateUtil.getDateTimeForLog() + " , fildId=>" + fileId + ", obj=>" + obj);
			}
			*/		
			
			
			//處理回傳結果
			if(isGetUrl == 1){//保留{"origin":{"fileId":"xxx",  "url":"xxx"}}
				rtn.put("origin", this.getActionUrl( fileId, tag, isGetUrl, urlExpiredTime, isSSL));	
			}
			if(isNewFileId==1){//產生新檔案 {"new":{"fileId":"xxx",  "url":"xxx"}}
				rtn.put("new", this.getActionUrl( newFileId, "", isGetUrl, urlExpiredTime, isSSL));									
			}
			
			rtn.put("status",status);
			rtn.put("error", error);

			logger.info("DEBUG ImageProcess doAction() , End of method  , " + DateUtil.getDateTimeForLog() + " ,rtn=>" + rtn.toString() + " , response=>" + response + ", status=>" + status + ", error=>" + error);
			
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("ERROR ImageProcess doAction() ,  " + DateUtil.getDateTimeForLog() + " " + e.toString());
		}
		logger.info("Exit ImageProcess doAction , " + DateUtil.getDateTimeForLog() + " fileId=>" + fileId + ", obj=>" + obj);
		return rtn;
	}
	
	private JSONObject getActionUrl(String fileId, String tag, int isGetUrl, long urlExpiredTime, int isSSL) throws JSONException, DocApplicationException{
		JSONObject rtn = new JSONObject();
		String timestamp = String.valueOf(new java.util.Date().getTime() + urlExpiredTime);
		rtn.put("fileId",fileId);//{"fileId":"xxxx"}
		if(isGetUrl == 1){
			DocAPIImpl fm = new DocAPIImpl();
			JSONArray params = new JSONArray();
			JSONObject t = new JSONObject();
			t.put("fileId", fileId);
			t.put("fileTag", tag);
			t.put("page", 0);//取單頁 不加頁次
			
			// add by louis  2014-04-17
			if(isSSL == 1)
				t.put("protocol", "https");				// 指定回傳 https url.
			
			params.put(t);
			//TODO Johnson記得在參數內需加入timestamp.method name 也要改回getFileUrl
			//JSONArray res = new JSONArray(fm.getFileUrl(params.toString(), timestamp));
			//JSONArray res = new JSONArray(fm.getFileUrlnoRedis(params.toString()));
			GetFileArr getFileArr = new GetFileArr();
			getFileArr.setFileId(fileId);
			getFileArr.setFileTag(tag);
			if (t.has("protocol"))
				getFileArr.setProtocol("https");
			
			List<GetFileArr> fileArr = new ArrayList<>();
			fileArr.add(getFileArr);
			
			GetFileUrl getFileUrl = new GetFileUrl();
			getFileUrl.setFileArr(fileArr);
			JSONArray res = new JSONArray(fm.getFileUrl(getFileUrl));
			rtn.put("url", res.getJSONObject(0).getJSONArray("url"));
		}
		return rtn;		
	}
	
}
