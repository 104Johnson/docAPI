package com.e104.util;


import org.json.JSONException;
import org.json.JSONObject;

import scala.annotation.StaticAnnotation;


/**
 * @className Config
 * @purpose 設定檔
 * @author jason.hsiao
 * @createTime 2013-07-16
 */

public abstract class Config {	
	
	// 編譯前請先指定環境
//	public static final Environment ACTIVE_ENV = Environment.PROD;
	
	/************************* ENDPOINT *********************/
	public static final String MONGOSERVICEWSDL = System.getProperty("MongoService.service") + "?wsdl";
	public static final String MQ_WSDL = System.getProperty("MqService.service");
	public static final String MC_ENDPOINT = System.getProperty("McService.service");
	public static final String MONGO_ENDPOINT = System.getProperty("MongoService.service");
//	public static final String VIDEO_ENDPOINT = System.getProperty("Doc.VideoService.service");	
	public static final String FILE_ACCESS_URL = "://s3-ap-northeast-1.amazonaws.com/e104-doc-api-file-store/";	
	public static final String FILE_ACCESS_SSL_URL = System.getProperty("Doc.FileAccessSslURL");	
	public static final String DFRTMPT_URL = System.getProperty("Doc.FileRtmptURL");
	
//	public static final String MONGOSERVICEWSDL = "http://intesb.cloud.s104.com.tw/services/MongoService.0.0?wsdl";
//	public static final String MQ_WSDL = "http://intesb.cloud.s104.com.tw/services/MqService.0.0";
//	public static final String MC_ENDPOINT = "http://intesb.cloud.s104.com.tw/services/McService.0.0";
//	public static final String MONGO_ENDPOINT =   "http://intesb.cloud.s104.com.tw/services/MongoService.0.0";
//	public static final String VIDEO_ENDPOINT = "http://esb.cloud.s104.com.tw/services/Video.0.0";
//	public static final String FILE_ACCESS_URL = "http://172.19.7.75/DocumentManagementTomcatAccess/imgs";
//	public static final String FILE_ACCESS_SSL_URL = "https://172.19.7.75/DocumentManagementTomcatAccess/imgs";
//	public static final String DFRTMPT_URL = "rtmpt://172.19.7.97/vod";
	
	public static final String buildDate = "2015-10-13 12:00";
	public static String VERSION = "";
	public static String bucketName = "docapi-dev-origin";
	public static String convertBucketName = "docapi-dev-variant"; 
//	public static final String VERSION = "{\"time\":\"2015-05-22 16:10 \",\"Desc\":\"for all environment audio return http OR rtmpt \",\"MQ_WSDL\":\""+MQ_WSDL+"\",\"MC_ENDPOINT\":\""+MC_ENDPOINT+"\",\"MONGO_ENDPOINT\":\""+MONGO_ENDPOINT+"\",\"VIDEO_ENDPOINT\":\""+VIDEO_ENDPOINT+"\",\"FILE_ACCESS_URL\":\""+FILE_ACCESS_URL+"\",\"DFRTMPT_URL\":\""+DFRTMPT_URL+"\"}";

	
	/************************* OTHERS *********************/
	//資料庫名稱
//	public static final String DB_NAME = "documentmanagement";
	public static final String DB_NAME = "mdb0c00001";
	
	// modify by jj on 2013-12-04 add for html save image 
	public static final String TABLENAMETXIDFILEIDMAP = "fileidUuidaaMap";
	
	//設定imagemagick convert指令的位置
	public static final String CONVERT_COMMAND = "/usr/bin/convert";
	//public static final String CONVERT_COMMAND = "C:\\Program Files\\ImageMagick-6.8.8-Q8\\convert.exe";
	
	//設定104plus rootPath
	public static final String ROOT_PATH = "filetemp";
	
	//設定root位置(吐資料時只需要/104plus/以後
	public static final String ROOT = "/mnt/mfs/stream";	
	
	//ffmpeg 執行檔路徑.
	public static final String FFMPEG_COMMAND= "/opt/ffmpeg/bin/ffmpeg";
	
	//mediainfo 執行檔路徑
	public static final String MEDIAINFO_COMMAND= "/usr/bin/mediainfo";
	
	//設定md5加密key
	public static final String MD5_PWD = "Show me money";
	
	//isp1 for cache
	public static final String MD5_PWD_ISP1 = "give me the money";
	
	public static final String VIDEO_VERSION = "_v1_";
	
	public static final String CHECK = "alive";
	
	//2013-11-26 16:00
	//xor運算 , tools xorEncrypt , xorDecrypt 使用
	public static final int RADIX = 32;	
	public static final String SEED = "687943213546843213546351321";
	
	//modify by jj on 2013-12-04 add for html save image 
	public static final String TARGETNAMESPACEMONGO = "http://Mongo.e104.com";
	public static final String METHOD_INSERT = "Insert";
	public static final String METHOD_SELECT = "Select";
	public static final String METHOD_UPDATE = "Update";
	
	// queue name
	public static String QName_DocToPdf = "docConvertToPdf";
	public static String QName_MA = "maConvert";
	public static String QName_Media = "MediaConvert";			// this is video queue name
	public static String QName_Audio = "AudioConvert";

	static{

		// switch to OL queue name if current running environment is OL (test by mongo api domain name).
		if(MONGOSERVICEWSDL.contains("olin.api.104.com.tw")){
			QName_DocToPdf = "docConvertToPdf-OL";
			QName_MA = "maConvert-OL";
			QName_Media = "MediaConvert-OL";
			QName_Audio = "AudioConvert-OL";
		}
		
		JSONObject versionObj = new JSONObject();
		try {
			versionObj.put("buildDate", buildDate)
			.put("MONGOSERVICEWSDL", MONGOSERVICEWSDL)
			.put("MQ_WSDL", MQ_WSDL)
			.put("MC_ENDPOINT", MC_ENDPOINT)
			.put("MONGO_ENDPOINT", MONGO_ENDPOINT)
			.put("FILE_ACCESS_URL", FILE_ACCESS_URL)
			.put("FILE_ACCESS_SSL_URL", FILE_ACCESS_SSL_URL)
			.put("DFRTMPT_URL", DFRTMPT_URL)
			.put("QName_DocToPdf", QName_DocToPdf)
			.put("QName_MA", QName_MA)
			.put("QName_Media", QName_Media)
			.put("QName_Audio", QName_Audio);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		VERSION = versionObj.toString();
		
		
		/*
		switch(ACTIVE_ENV){
			case DEV:
				MONGOSERVICEWSDL = "http://172.19.7.90:9763/services/MongoService?wsdl";					
				MQ_WSDL = "http://172.19.7.90:9763/services/MqService.MqServiceHttpSoap11Endpoint";	
				MC_ENDPOINT = "http://172.19.7.90:9763/services/McService.McServiceHttpSoap11Endpoint";	
				MONGO_ENDPOINT = "http://172.19.7.90:9763/services/MongoService.MongoServiceHttpSoap11Endpoint";	
				VIDEO_ENDPOINT = "http://172.19.7.90:9763/services/Video.VideoHttpSoap11Endpoint";				
				FILE_ACCESS_URL = "http://172.19.7.90:8080/DocumentManagementTomcatAccess/imgs";
				FILE_ACCESS_SSL_URL = "https://172.19.7.90:8443/DocumentManagementTomcatAccess/imgs";
				DFRTMPT_URL = "rtmpt://172.19.7.141:8080/vod";				
				VERSION = "{\"time\":\"2014-06-18 15:40 \",\"Desc\":\"for all DEV if putFile pic convert success , videoConvert key convert to pending \",\"MQ_WSDL\":\""+MQ_WSDL+"\",\"MC_ENDPOINT\":\""+MC_ENDPOINT+"\",\"MONGO_ENDPOINT\":\""+MONGO_ENDPOINT+"\",\"VIDEO_ENDPOINT\":\""+VIDEO_ENDPOINT+"\",\"FILE_ACCESS_URL\":\""+FILE_ACCESS_URL+"\",\"DFRTMPT_URL\":\""+DFRTMPT_URL+"\"}";	
				break;
				
			case LAB:
				MONGOSERVICEWSDL = "http://intesb.cloud.s104.com.tw/services/MongoService.0.0?wsdl";				
				MQ_WSDL = "http://intesb.cloud.s104.com.tw/services/MqService.0.0.MqService.0.0HttpSoap11Endpoint";				
				MC_ENDPOINT = "http://intesb.cloud.s104.com.tw/services/McService.0.0.McService.0.0HttpSoap11Endpoint";			
				MONGO_ENDPOINT = "http://intesb.cloud.s104.com.tw/services/MongoService.0.0.MongoService.0.0HttpSoap11Endpoint";			 	
				VIDEO_ENDPOINT = "http://esb.cloud.s104.com.tw/services/Video.0.0.Video.0.0HttpSoap11Endpoint";				
				FILE_ACCESS_URL = "http://172.19.7.75/DocumentManagementTomcatAccess/imgs";	
				FILE_ACCESS_SSL_URL = "https://172.19.7.75/DocumentManagementTomcatAccess/imgs";
				DFRTMPT_URL = "rtmpt://172.19.7.97/vod";	
				VERSION = "{\"time\":\"2014-06-20 10:40 \",\"Desc\":\"for all SIT audio return http OR rtmpt \",\"MQ_WSDL\":\""+MQ_WSDL+"\",\"MC_ENDPOINT\":\""+MC_ENDPOINT+"\",\"MONGO_ENDPOINT\":\""+MONGO_ENDPOINT+"\",\"VIDEO_ENDPOINT\":\""+VIDEO_ENDPOINT+"\",\"FILE_ACCESS_URL\":\""+FILE_ACCESS_URL+"\",\"DFRTMPT_URL\":\""+DFRTMPT_URL+"\"}";
				break;
				
			case PROD:				
				MONGOSERVICEWSDL = "http://in.api.104.com.tw/services/MongoService.0.0?wsdl";				
				MQ_WSDL = "http://in.api.104.com.tw/services/MqService.0.0.MqService.0.0HttpSoap11Endpoint";				
				MC_ENDPOINT = "http://in.api.104.com.tw/services/McService.0.0.McService.0.0HttpSoap11Endpoint";			
				MONGO_ENDPOINT = "http://in.api.104.com.tw/services/MongoService.0.0.MongoService.0.0HttpSoap11Endpoint";
				VIDEO_ENDPOINT = "http://api.104.com.tw/services/Video.0.0.Video.0.0HttpSoap11Endpoint";				
				FILE_ACCESS_URL = "http://file.104.com.tw/DocumentManagementTomcatAccess/imgs";
				FILE_ACCESS_SSL_URL = "https://file.104.com.tw/DocumentManagementTomcatAccess/imgs";
				DFRTMPT_URL = "rtmpt://stream.104.com.tw/vod";				
				VERSION = "{\"time\":\"2014-06-20 10:45 \",\"Desc\":\"for all PRD audio return http OR rtmpt \",\"MQ_WSDL\":\""+MQ_WSDL+"\",\"MC_ENDPOINT\":\""+MC_ENDPOINT+"\",\"MONGO_ENDPOINT\":\""+MONGO_ENDPOINT+"\",\"VIDEO_ENDPOINT\":\""+VIDEO_ENDPOINT+"\",\"FILE_ACCESS_URL\":\""+FILE_ACCESS_URL+"\",\"DFRTMPT_URL\":\""+DFRTMPT_URL+"\"}";				
				break;
				
			default:
				throw new RuntimeException("Environment setting is not valid.");
			
		}
		*/
	}
}
