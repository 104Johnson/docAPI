import static org.junit.Assert.*;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
















import scala.reflect.internal.Trees.New;

import com.amazonaws.services.support.model.CaseCreationLimitExceededException;
import com.e104.Errorhandling.DocApplicationException;
import com.e104.restapi.model.Extra;
import com.e104.restapi.model.GetFileArr;
import com.e104.restapi.model.GetFileUrl;
import com.e104.restapi.model.MultiAction;
import com.e104.restapi.model.Signature;
import com.e104.restapi.model.VideoImageSize;
import com.e104.restapi.service.DocAPIImpl;
import com.e104.util.Config;
import com.e104.util.S3Service;



public class DocTest {
	private String response;
	DocAPIImpl docAPIImpl = new DocAPIImpl();
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() throws DocApplicationException {
		/*healthCheck();
		decryptParam();
		encryptParam();
		addKey();
		removeKey();
		discardFile();
		signature();
		getFileUrl();
		generateFileId();*/
		
	}
	private void healthCheck(){
		//case 1
		response = docAPIImpl.healthCheck();
		assertEquals("ok",response );
	}
	private void addKey(){
		JSONObject responseObject;
		try {
			//Case 1
			response = docAPIImpl.addKey("6dde8a907a3549c1afca3003b9e61a1111", "disabled", "1");
			responseObject = new JSONObject(response);
			assertEquals("Success",responseObject.getString("status"));
		
			//Case 2 FileId is not exist
			response = docAPIImpl.addKey("6dde8a907a3549c1afca3003b9e61a1111", "disabled", "1");
			
			//Case 3 Field value is null
			response = docAPIImpl.addKey("6dde8a907a3549c1afca3003b9e61a1111", "", "1");
			
			//Case 4Field value is null
			response = docAPIImpl.addKey("6dde8a907a3549c1afca3003b9e61a1111", "disabled", "");
			
		}
		  catch (DocApplicationException e) {
			if (e.getMessage()!="FileId is not exist" && e.getMessage()!="Field value is null")
				fail("[Error]"+e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			fail("[Error]"+e.getMessage());
		} 
		
	}
	private void removeKey(){
		JSONObject responseObject;
		try {
			//Case 1
			response = docAPIImpl.removeKey("6dde8a907a3549c1afca3003b9e61a1111", "disabled");
			responseObject = new JSONObject(response);
			assertEquals("Success",responseObject.getString("status"));
			
			//Case 2
			response = docAPIImpl.removeKey("6dde8a907a3549c1afca3003b9e61a1111", "disabled");
			
		}
		  catch (DocApplicationException e) {
			if (e.getMessage()!="FileId or key is not exist")
				fail("[Error]"+e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			fail("[Error]"+e.getMessage());
		} 
	}
	
	private void encryptParam(){
		try {
			//case 1
			response = docAPIImpl.encryptParam("{\"apnum\":\"10400\","
					+ "\"title\":\"測試\","
					+ "\"extra\":{\"extraNo\":\"09b87a95-6ef4-4d23-aca6-660521a3968e\","
					+ "\"convert\":\"false\"},"
					+ "\"description\":\"測試\","
					+ "\"actionTimestamp\":1460430186925,"
					+ "\"isP\":1,"
					+ "\"pid\":\"10400\","
					+ "\"contenttype\":\"image/jpeg\","
					+ "\"contentDisposition\":\"Penguins.jpg\"}");
			assertNotNull(response);	
			
			//case 2
			response = docAPIImpl.encryptParam("{}");
			assertNotNull(response);
			
			
		} catch (DocApplicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail("[Error]"+e.getMessage());
		}
	}
	
	
	private void decryptParam() {
		JSONObject responseObject;
		try {
			//case 1
			response = docAPIImpl.decryptParam("eyJhcG51bSI6IjEwNDAwIiwidGl0bGUiOiLmuKzoqaYiLCJleHRyYSI6eyJleHRyYU5vIjoiMDliODdhOTUtNmVmNC00ZDIzLWFjYTYtNjYwNTIxYTM5NjhlIiwiY29udmVydCI6ImZhbHNlIn0sImRlc2NyaXB0aW9uIjoi5ris6KmmIiwiYWN0aW9uVGltZXN0YW1wIjoxNDYwNDMwMTg2OTI1LCJpc1AiOjEsInBpZCI6IjEwNDAwIiwiY29udGVudHR5cGUiOiJpbWFnZS9qcGVnIiwiY29udGVudERpc3Bvc2l0aW9uIjoiUGVuZ3VpbnMuanBnIn0");
			responseObject = new JSONObject(response);
			assertEquals("1460430186925",String.valueOf(responseObject.getLong("actionTimestamp")));
			
			//Case2 Data Decrypt Fail
			response = docAPIImpl.decryptParam("1");
			
			
		} catch (DocApplicationException e) {
			// TODO Auto-generated catch block
			if (e.getMessage()!="Data Decrypt Fail")
				fail("[Error]"+e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			fail("[Error]"+e.getMessage());
		}
	}
	private void discardFile(){
		JSONObject responseObject;
		try {
			//Case 1
			response = docAPIImpl.discardFile("6dde8a907a3549c1afca3003b9e61a1111");
			responseObject = new JSONObject(response);
			assertEquals("Success",responseObject.getString("status"));
		
			S3Service s3Service = new S3Service();
			response = s3Service.uploadFile(Config.bucketName, "e8e/1dd/5e9/6dde8a907a3549c1afca3003b9e61a1111.jpg");
			assertEquals("Upload Success",response);
			docAPIImpl.removeKey("6dde8a907a3549c1afca3003b9e61a1111", "disabled");
			docAPIImpl.removeKey("6dde8a907a3549c1afca3003b9e61a1111", "discardDate");
			
			//Case 2 FileId is not exist
			response = docAPIImpl.discardFile("1");
			
		}
		  catch (DocApplicationException e) {
			  e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
			fail("[Error]"+e.getMessage());
		} 
		
	}
	
	private void signature(){
		JSONObject responseObject;
		
		Signature signature = new Signature();
		Extra extra = new Extra();
		List<MultiAction> multiAction = new ArrayList<MultiAction>();
		List<VideoImageSize> videoImageSize = new ArrayList<VideoImageSize>();
		
		signature.setContentDisposition("Penguins.jpg");
		signature.setApnum("10400");
		signature.setContenttype("image/jpeg");
		signature.setDescription("測試");
		
		extra.setConvert("false");
		extra.setExtraNo("09b87a95-6ef4-4d23-aca6-660521a3968e");
		extra.setMultiAction(multiAction);
		extra.setVideoImageSize(videoImageSize);
		
		signature.setExtra(extra);
		signature.setIsP("1");
		signature.setPid("10400");
		signature.setTitle("測試");
		
		
		try {
			//case 1 
			response = docAPIImpl.signature(signature);
			responseObject = new JSONObject(response);
			assertFalse(responseObject.has("policyDocument")&& 
					"".equals(responseObject.getString("policyDocument")));
			assertFalse(responseObject.has("signature")&& 
					"".equals(responseObject.getString("signature")));
			assertFalse(responseObject.has("objectKey")&& 
					"".equals(responseObject.getString("objectKey")));
			assertFalse(responseObject.has("bucketName") && 
					"".equals(responseObject.getString("bucketName")));
			assertFalse(responseObject.has("contentDisposition") && 
					"".equals(responseObject.getString("contentDisposition")));
			
			assertEquals("Penguins.jpg",responseObject.getString("contentDisposition"));
			
			//case 2
			signature.setContentDisposition(null);
			response = docAPIImpl.signature(signature);
			responseObject = new JSONObject(response);
			
			
		} catch (DocApplicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			if (e.getMessage() =="Service Error")
				fail("[Error]"+e.getMessage());
		}
	}
	
	private void getFileUrl(){
		JSONArray responseArray;
		GetFileUrl getFileUrl = new GetFileUrl();
		GetFileArr getFileArr = new GetFileArr();
		List<GetFileArr> getFileArrs = new ArrayList<>();
		
		getFileArr.setFileId("6dde8a907a3549c1afca3003b9e61a1111");
		getFileArr.setFileTag("");
		getFileArr.setProtocol("common");
		getFileArrs.add(getFileArr);
		
		
		Long timestamp = System.currentTimeMillis(); 
		getFileUrl.setFileArr(getFileArrs);
		getFileUrl.setTimestamp(String.valueOf((timestamp+10000)/1000));

		
		try {
			//Case 1
			response = docAPIImpl.getFileUrl(getFileUrl);
			responseArray = new JSONArray(response);
			assertEquals("6dde8a907a3549c1afca3003b9e61a1111",responseArray.getJSONObject(0).getString("fileId"));
			assertNotNull(responseArray.getJSONObject(0).getJSONArray("url"));

		}catch (DocApplicationException e) {
			e.printStackTrace();
		}catch (Exception e) {
			e.printStackTrace();
			fail("[Error]"+e.getMessage());
		} 
		
		try{
			//Case 2
			getFileUrl.setTimestamp(null);
			response = docAPIImpl.getFileUrl(getFileUrl);
		}catch(DocApplicationException e){
			if (e.getMessage()!="Empty parameter")
				fail("[Error]"+e.getMessage());
		}catch (Exception e) {
			e.printStackTrace();
			fail("[Error]"+e.getMessage());
		} 
		
		try{
			//Case 3
			getFileUrl.setTimestamp(String.valueOf((timestamp+10000)/1000));
			getFileUrl.clearFileArr();
			response = docAPIImpl.getFileUrl(getFileUrl);
		}catch(DocApplicationException e){
			if (e.getMessage()!="Empty parameter")
				fail("[Error]"+e.getMessage());
			//e.printStackTrace();
		}catch (Exception e) {
			e.printStackTrace();
			fail("[Error]"+e.getMessage());
		} 
		
		try{
			//Case 4
			getFileArr.setFileId("1");
			getFileArr.setFileTag("");
			getFileArr.setProtocol("common");
			getFileArrs.add(getFileArr);
			getFileUrl.setFileArr(getFileArrs);
			
			response = docAPIImpl.getFileUrl(getFileUrl);
		}catch(DocApplicationException e){
			if (e.getMessage()!="FileId is not exist")
				fail("[Error]"+e.getMessage());
		}catch (Exception e) {
			e.printStackTrace();
			fail("[Error]"+e.getMessage());
		} 
	}
	
	private void generateFileId(){
		JSONObject responseObject;
		try {
			//Case 1
			response = docAPIImpl.generateFileId("", "1", "1");
			responseObject = new JSONObject(response);
			
			//Case 2 FileId is not exist
			response = docAPIImpl.discardFile("1");
			
		}
		  catch (DocApplicationException e) {
			  e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
			fail("[Error]"+e.getMessage());
		} 
	}

}
