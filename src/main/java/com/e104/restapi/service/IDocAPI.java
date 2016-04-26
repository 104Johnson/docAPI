package com.e104.restapi.service;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.e104.Errorhandling.DocApplicationException;
import com.e104.restapi.model.GetFileUrl;
import com.e104.restapi.model.Signature;
import com.e104.restapi.model.UpdateFile;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ApiResponse;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Api(value = "/rest/services")
@Path("/rest/services")
public interface IDocAPI {
	
	   @PUT
	   @Path("/addKey")
	   @ApiOperation(value = "Update user table key & valu", notes = "updateFile", httpMethod = "PUT")
	   @ApiResponses(value =  @ApiResponse(code = 200, message = "Successful response"))
	   @ApiImplicitParam(name = "body", value = "JSONObject", required = true, dataType = "string", paramType = "string") 
	   public String addKey(@ApiParam(value = "fileId",required=true) @QueryParam("fileid") String fileid,
			   @ApiParam(value = "key",required=true) @QueryParam("key") String key,
			   @ApiParam(value = "value",required=true) @QueryParam("value") String value) throws DocApplicationException;


	   
	   @POST
	   @Path("/checkFileSpec")
	   @ApiOperation(value = "Check upload spec", httpMethod = "POST")
	   @ApiResponses(value = { @ApiResponse(code = 200, message = "http/1.1 200 OK{\"error\":\"\",\"data\":\"\",\"success\":\"true\"}")})
	   /**request String 
	    * {"filePath":"https://s3-ap-northeast-1.amazonaws.com/awssyslogs03/docUpload.jpg",
	    * 	"extraNo":"2cdds-asdsad-asdas-adssad",
	    * 	"specObj":"{
						"maxwidth" : "9999",
						"description" : "NonSns",
						"source" : "104pro",
						"title" : "NonSns",
						"contenttype" : "4",
						"extensions" : "wav,mp3,wma,m4a",
						"maxheight" : "9999",
						"minwidth" : "0",
						"extra" : {
						"convert" : "true"
						},
						"maxsize" : "500",
						"minheight" : "0"
						}"}
	    * 
	    * 
	    * 
	    * respone jsonobject string {"status":"Success"}
	    * */
	   public String checkFileSpec(@ApiParam(value = "check upload spec", required = true)@PathParam("specObj") String specObj);
	   
	   
	   @DELETE
	   @Path("/clearFileCache/{cacheSize}")
	   @ApiOperation(value = "clear redis cache", httpMethod = "DELETE")
	   @ApiResponses(value = { @ApiResponse(code = 200, message = "http/1.1 200 OK{\"error\":\"\",\"data\":\"\",\"success\":\"true\"}")})
	   public String clearFileCache(@ApiParam(value = "clear redis Size", required = true) @PathParam("cacheSize") String cacheSize);
	   
	   
	   @GET
	   @Path("/confirmUpload/{fileid}")
	   @ApiOperation(value = "clear redis cache", httpMethod = "GET")
	   @ApiResponses(value = { @ApiResponse(code = 200, message = "http/1.1 200 OK{\"error\":\"\",\"data\":\"\",\"success\":\"true\"}")})
	   public String confirmUpload(@ApiParam(value = "check Upload file is exist,if exist remove expireTimestamp", required = true) @PathParam("fileid") String fileid);
	  
	   
	   @POST
	   @Path("/copyFile")
	   @ApiOperation(value = "copy file by user config & use putfile method", httpMethod = "POST")
	   @ApiResponses(value = { @ApiResponse(code = 200, message = "http/1.1 200 OK{\"error\":\"\",\"data\":\"\",\"success\":\"true\"}")})
	   public String copyFile(@ApiParam(value = "{fileId:123,apNum:10400,pid:104,jsonObj:{},title:hello,description:hello word,filename:hello.jpg}", required = true) @FormParam("fileObj") String fileObj);
	   
	   @POST
	   @Path("/copyFileForMM")
	   @ApiOperation(value = "copy file by inputstream", httpMethod = "POST")
	   @ApiResponses(value = { @ApiResponse(code = 200, message = "http/1.1 200 OK{\"error\":\"\",\"data\":\"\",\"success\":\"true\"}")})
	   public String copyFileForMM(@ApiParam(value = "{fileid:123442,cnt:1,contenttype:1}", required = true) @FormParam("fileObj") String fileObj);
	   
	   @GET
	   @Path("/decryptParam/{param}")
	   @ApiOperation(value = "Data encrypt", notes = "EncryptParam", httpMethod = "GET")
	   @ApiResponses(value =  @ApiResponse(code = 200, message = "Successful response"))
	   @ApiImplicitParam(name = "param", value = "EncryptCode", required = true, dataType = "string", paramType = "string") 	  
	   public String decryptParam(@ApiParam(value = "decrypt data", required = true) @PathParam("param") String param)throws DocApplicationException;
	   
	   @POST
	   @Path("/encryptParam")
	   @ApiOperation(value = "Data encrypt", notes = "EncryptParam", httpMethod = "POST")
	   @ApiResponses(value =  @ApiResponse(code = 200, message = "Successful response"))
	   @ApiImplicitParam(name = "body", value = "JSONObject", required = true, dataType = "string", paramType = "body") 	  
	   public String encryptParam(@ApiParam(value = "encrypt data", required = true)  String param) throws DocApplicationException;
	   
	   
	   @DELETE
	   @Path("/deleteFile")
	   @ApiOperation(value = "delete file", notes = "updateFile", httpMethod = "DELETE")
	   @ApiResponses(value =  @ApiResponse(code = 200, message = "Successful response"))
	   @ApiImplicitParam(name = "body", value = "JSONObject", required = true, dataType = "string", paramType = "string") 
	   public String deleteFile(@ApiParam(value = "fileId",required=true) @QueryParam("fileId") String fileId,
			   @ApiParam(value = "fileTag",required=true) @QueryParam("fileTag") String fileTag,
			   @ApiParam(value = "delExtend",required=true) @QueryParam("delExtend") String delExtend) throws DocApplicationException;
	   
	   @DELETE
	   @Path("/discardFile/{fileId}")
	   @ApiOperation(value = "delete files by fileId", httpMethod = "DELETE")
	   @ApiResponses(value =  @ApiResponse(code = 200, message = "Successful response"))
	   @ApiImplicitParam(name = "fileid", value = "22883ab0899047c28da1969df4dabab211", required = true, dataType = "string", paramType = "string") 
	   public String discardFile(@ApiParam(value = "fileid", required = true) @PathParam("fileId") String fileId) throws DocApplicationException;
	   
	   @GET
	   @Path("/generateFileId")
	   @ApiOperation(value = "delete files by fileId", httpMethod = "GET")
	   @ApiResponses(value =  @ApiResponse(code = 200, message = "Successful response"))
	   @ApiImplicitParam(name = "fileid", value = "String test", required = true, dataType = "string", paramType = "string") 
	   public String generateFileId(@ApiParam(value = "extraNo", required = true)@QueryParam("extraNo") String extraNo,
			   @ApiParam(value = "contenttypeP", required = true)@QueryParam("contenttype") String contenttype,
			   @ApiParam(value = "isP", required = true)@QueryParam("isP") String isP
			   ) throws DocApplicationException;
	   
	   @GET
	   @Path("/getCheck")
	   @ApiOperation(value = "Get a value", httpMethod = "GET")
	   @ApiResponses(value = { @ApiResponse(code = 200, message = "http/1.1 200 OK{\"error\":\"\",\"data\":\"\",\"success\":\"true\"}")})
	   public String getCheck();
	   
	   @GET
	   @Path("/getFileCache/(Param)")
	   @ApiOperation(value = "Get redis cache url", httpMethod = "GET")
	   @ApiResponses(value = { @ApiResponse(code = 200, message = "http/1.1 200 OK{\"error\":\"\",\"data\":\"\",\"success\":\"true\"}")})
	   public String getFile(@ApiParam(value = "Param is decode,need pattern & limit", required = true) @PathParam("Param") String Param);
	   
	   @GET
	   @Path("/getFileDetail")
	   @ApiOperation(value = "Get file meta by fileId", httpMethod = "GET")
	   @ApiResponses(value =  @ApiResponse(code = 200, message = "Successful response"))
	   @ApiImplicitParam(name = "body", value = "JSONObject", required = true, dataType = "string", paramType = "string") 
	   public String getFileDetail(@ApiParam(value = "fileId",required=true) @QueryParam("fileId") String fileId,
			   @ApiParam(value = "tag",required=true) @QueryParam("tag") String tag) throws DocApplicationException;
	   
	   @GET
	   @Path("/getFileList/(Param)")
	   @ApiOperation(value = "Get file List by Pid", httpMethod = "GET")
	   @ApiResponses(value = { @ApiResponse(code = 200, message = "http/1.1 200 OK{\"error\":\"\",\"data\":\"\",\"success\":\"true\"}")})
	   public String getFileList(@ApiParam(value = "Param is decode,need pid & contenttype & apnum", required = true) @PathParam("Param") String Param);
	   
	   @POST
	   @Path("/getFileUrl")
	   @ApiOperation(value = "", notes = "GetFileUrl", tags={  })
	   @ApiResponses(value =  @ApiResponse(code = 200, message = "Successful response"))
	   @ApiImplicitParam(name = "body", value = "JSONObject", required = true, dataType = "string", paramType = "body") 	  
	   public String getFileUrl(@ApiParam(value = "JSONObject",required=true)  GetFileUrl jsonData) throws DocApplicationException;
	   
	   @POST
	   @Path("/test")
	   @ApiOperation(value = "", notes = "產生上傳檔案前呼叫，產生檔案名稱", tags={  })
	   @ApiResponses(value =  @ApiResponse(code = 200, message = "Successful response"))
	   @ApiImplicitParam(name = "jsonData", value = "JSONObject", required = true, dataType = "String", paramType = "body") 	  
	   public String test(@ApiParam(value = "JsonObject",required=true) Signature  jsonData) throws DocApplicationException;
	   
	   
	   
	   @GET
	   @Path("/getQueueLength")
	   @ApiOperation(value = "Get Quere Length", httpMethod = "GET")
	   @ApiResponses(value = { @ApiResponse(code = 200, message = "http/1.1 200 OK{\"error\":\"\",\"data\":\"\",\"success\":\"true\"}")})
	   public String getQueueLength();
	   
	   @GET
	   @Path("/getVersion")
	   @ApiOperation(value = "Get Version", httpMethod = "GET")
	   @ApiResponses(value = { @ApiResponse(code = 200, message = "http/1.1 200 OK{\"error\":\"\",\"data\":\"\",\"success\":\"true\"}")})
	   public String getVersion();
	   
	   @POST
	   @Path("/putfile")
	   @ApiOperation(value = "", notes = "產生上傳檔案前呼叫，產生檔案名稱", response = DocAPIImpl.class)
	   @ApiResponses(value =  @ApiResponse(code = 200, message = "Successful response"))
	   @ApiImplicitParam(name = "body", value = "JSONObject", required = true, dataType = "string", paramType = "body") 
	   public String putfile(Signature jsonData) throws DocApplicationException;
	   
	   /*
	   @POST
	   @Path("/putfile12")
	   @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	   @ApiOperation(value = "", notes = "產生上傳檔案前呼叫，產生檔案名稱", response = docAPIImp.class)
	   @ApiResponses(value =  @ApiResponse(code = 200, message = "Successful response"))
	   @ApiImplicitParams(
		   @ApiImplicitParam(name = "body", value = "JSONObject", required = true, dataType = "com.e104.restapi.docAPI.", paramType = "body") 
		  )
	   public String putfile12( @ApiParam(value = "Hash of the user", required = true) @QueryParam("jsonData") com.e104.restapi.model.jsonData jsonData) throws DocApplicationException;
	   */
	   
	   @POST
	   @Path("/putfile12")
	   @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	   @ApiOperation(value = "", notes = "產生上傳檔案前呼叫，產生檔案名稱", response = DocAPIImpl.class)
	   @ApiResponses(value =  @ApiResponse(code = 200, message = "Successful response"))
	   @ApiImplicitParams(
		   @ApiImplicitParam(name = "body", value = "JSONObject", required = true, dataType = "com.e104.restapi.docAPI.", paramType = "body") 
		  )
	   public String putfile12( @ApiParam(value = "Hash of the user", required = true) com.e104.restapi.model.jsonData jsonData) throws DocApplicationException;
	   
	   
	   @DELETE
	   @Path("/removeKey")
	   @ApiOperation(value = "remove user collection Key ", httpMethod = "DELETE")
	   @ApiResponses(value = { @ApiResponse(code = 200, message = "http/1.1 200 OK{\"error\":\"\",\"data\":\"\",\"success\":\"true\"}")})
	   public String removeKey(@ApiParam(value = "fileId",required=true) @QueryParam("fileId") String fileId,
			   @ApiParam(value = "key",required=true) @QueryParam("key") String key)throws DocApplicationException;
	   
	   @PUT
	   @Path("/setExpireTimestamp")
	   @ApiOperation(value = "Data encrypt", notes = "set user collection ExpireTimestamp", httpMethod = "PUT")
	   @ApiResponses(value =  @ApiResponse(code = 200, message = "Successful response"))
	   @ApiImplicitParam(name = "body", value = "JSONObject", required = true, dataType = "string", paramType = "body") 	  
	   public String setExpireTimestamp(@ApiParam(value = "fileId",required=true) @QueryParam("fileId") String fileId,
			   @ApiParam(value = "timestamp", required = true) @QueryParam("timestamp") String timestamp);
	   
	   @PUT
	   @Path("/updateFile")
	   @ApiOperation(value = "Update title & description to user table ", notes = "updateFile", httpMethod = "PUT")
	   @ApiResponses(value =  @ApiResponse(code = 200, message = "Successful response"))
	   @ApiImplicitParam(name = "body", value = "JSONObject", required = true, dataType = "string", paramType = "body") 
	   public String updateFile(@ApiParam(value = "JSONObject",required=true)  UpdateFile Param) throws DocApplicationException;
	   
	   
	   @POST
	   @Path("/doc2img")
	   @ApiOperation(value = "doc2img", httpMethod = "POST")
	   @ApiResponses(value = { @ApiResponse(code = 200, message = "http/1.1 200 OK{\"error\":\"\",\"data\":\"\",\"success\":\"true\"}")})
	   public String doc2img(@ApiParam(value = "{fileId,page,height,width,tag,isSave,isGetURL}", required = true) @PathParam("Param") String Param);
	   
	   @GET
	   @Path("/getStatus/{fileId}")
	   @ApiOperation(value = "getStatus", notes = "GetStatus", tags={  })
	   @ApiResponses(value =  @ApiResponse(code = 200, message = "Successful response"))
	   @ApiImplicitParam(name = "fileId", value = "String", required = true, dataType = "String", paramType = "String") 
	   public String getStatus(@ApiParam(value = "fileId", required = true) @PathParam("fileId") String fileId);
	   
	   @PUT
	   @Path("/setConvertStatus")
	   @ApiOperation(value = "Set convert status", httpMethod = "PUT")
	   @ApiResponses(value = { @ApiResponse(code = 200, message = "http/1.1 200 OK{\"error\":\"\",\"data\":\"\",\"success\":\"true\"}")})
	   public String setConvertStatus(@ApiParam(value = "{fileId,progress}", required = true) @PathParam("Param") String Param);
	   
	   @PUT
	   @Path("/updateData")
	   @ApiOperation(value = "add user collection key & value ", httpMethod = "PUT")
	   @ApiResponses(value = { @ApiResponse(code = 200, message = "http/1.1 200 OK{\"error\":\"\",\"data\":\"\",\"success\":\"true\"}")})
	   public String updateData(@ApiParam(value = "{fileId,jsonobject}", required = true) @PathParam("Param") String Param);
	   
	   @POST
	   @Path("/videoConvert")
	   @ApiOperation(value = "video task send to quere", httpMethod = "POST")
	   @ApiResponses(value = { @ApiResponse(code = 200, message = "http/1.1 200 OK{\"error\":\"\",\"data\":\"\",\"success\":\"true\"}")})
	   public String videoConvert(@ApiParam(value = "{fileId}", required = true) @PathParam("Param") String Param);
	   
	   @POST
	   @Path("/audioConvert")
	   @ApiOperation(value = "audio task send to quere", httpMethod = "POST")
	   @ApiResponses(value = { @ApiResponse(code = 200, message = "http/1.1 200 OK{\"error\":\"\",\"data\":\"\",\"success\":\"true\"}")})
	   public String audioConvert(@ApiParam(value = "{fileId}", required = true) @PathParam("Param") String Param);
	   
	   
	   @POST
	   @Path("/signature")
	   @ApiOperation(value = "Get S3 signature", notes = "GetFileUrl", httpMethod = "POST")
	   @ApiResponses(value =  @ApiResponse(code = 200, message = "Successful response"))
	   @ApiImplicitParam(name = "body", value = "JSONObject", required = true, dataType = "string", paramType = "body") 	  
	   public String signature(@ApiParam(value = "JSONObject",required=true)  Signature jsonData) throws DocApplicationException; 
	   
	   @GET
	   @Path("/healthCheck")
	   @ApiOperation(value = "healthCheck", notes = "healthCheck", httpMethod = "GET")
	   @ApiResponses(value =  @ApiResponse(code = 200, message = "Successful response"))
	   @ApiImplicitParam(name = "param", value = "healthCheck", required = true, dataType = "string", paramType = "string") 	  
	   public String healthCheck();
	   
	   @GET
	   @Path("/action")
	   public String action();
	   
	   /*
	   @GET
	   @Path("/wbGetStatus")
	   @ApiOperation(value = "Get Process Status", notes = "wbGetStatus", httpMethod = "GET")
	   @ApiResponses(value =  @ApiResponse(code = 200, message = "Successful response"))
	   @ApiImplicitParam(name = "ProcessId", value = "String", required = true, dataType = "String", paramType = "String") 	  
	   public String wbGetStatus(@ApiParam(value = "ProcessId",required=true)  Signature processId) throws DocApplicationException; 
	   */
	   
	 //doing##########################################################
	   
	   @POST
	   @Path("/getFileUrlnoRedis")
	   @ApiOperation(value = "", notes = "產生上傳檔案前呼叫，產生檔案名稱", tags={  })
	   @ApiResponses(value = { 
		        @ApiResponse(code = 200, message = "Successful response"),
		        @ApiResponse(code = 400, message = "Error response") })
	   public String getFileUrlnoRedis(@ApiParam(value = "JSONObject",required=true)  String jsonData)throws DocApplicationException;
	   
	   
}
