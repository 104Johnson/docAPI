import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;





import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.amazonaws.services.support.model.CaseCreationLimitExceededException;
import com.e104.Errorhandling.DocApplicationException;
import com.e104.restapi.service.DocAPIImpl;



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
		removeKey();*/
		
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
			
			
		}
		  catch (DocApplicationException e) {
			if (e.getMessage()!="FileId is not exist" && e.getMessage()!="Field value is null")
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
	
	

}
