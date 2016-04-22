import static org.junit.Assert.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.junit.Assert.fail;
















import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;
import com.google.common.collect.Multiset.Entry;
import com.sun.xml.bind.v2.schemagen.xmlschema.List;


public class DocTest {
	 private RequestConfig requestConfig = RequestConfig.custom()  
	            .setSocketTimeout(15000)  
	            .setConnectTimeout(15000)  
	            .setConnectionRequestTimeout(15000)
	            .build(); 
	 private String baseURL = "http://docapi-1217519329.ap-northeast-1.elb.amazonaws.com/docapi/rest/services/";
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
	public void test() {
		//測試
		decryptParam();
	}
	
	private void decryptParam(){
		Map<String,String> target = new HashMap<String,String>();
		JSONObject response;
		try {
			//case 1
			target.put(baseURL+"decryptParam/"
					+ "eyJhcG51bSI6IjEwNDAwIiwidGl0bGUiOiLmuKzoqaYiLCJleHRyYSI6eyJleHRyYU5vIjoiMDliODdhOTUtNmVmNC00ZDIzLWFjYTYtNjYwNTIxYTM5NjhlIiwiY29udmVydCI6ImZhbHNlIn0sImRlc2NyaXB0aW9uIjoi5ris6KmmIiwiYWN0aW9uVGltZXN0YW1wIjoxNDYwNDMwMTg2OTI1LCJpc1AiOjEsInBpZCI6IjEwNDAwIiwiY29udGVudHR5cGUiOiJpbWFnZS9qcGVnIiwiY29udGVudERpc3Bvc2l0aW9uIjoiUGVuZ3VpbnMuanBnIn0", "{\"apnum\":\"10400\","
					+ "\"title\":\"測試\","
					+ "\"extra\":{\"extraNo\":\"09b87a95-6ef4-4d23-aca6-660521a3968e\","
					+ "\"convert\":\"false\"},"
					+ "\"description\":\"測試\","
					+ "\"actionTimestamp\":1460430186925,"
					+ "\"isP\":1,"
					+ "\"pid\":\"10400\","
					+ "\"contenttype\":\"image/jpeg\","
					+ "\"contentDisposition\":\"Penguins.jpg\"}");
			//case 2
			target.put(baseURL+"decryptParam/1", "");
			Iterator iter = target.entrySet().iterator(); 
			while (iter.hasNext()) { 
			    Map.Entry entry = (Map.Entry) iter.next(); 
			    Object key = entry.getKey(); 
			    Object val = entry.getValue(); 
				response = new JSONObject(sendHttp(key.toString(),"","GET"));
				
			    //assertEquals(val,response);
			    if (response.has("error"))
			    	fail();
			} 
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//assertEquals(targetDecryptParam,response);
	}
	
	
	
	 /** 
     * 发送 post请求 
     * @param httpUrl 地址 
     * @param params 参数(格式:key1=value1&key2=value2) 
     */  
    public String sendHttp(String httpUrl, String params ,String method) {  
    	 CloseableHttpClient httpClient = null;  
         CloseableHttpResponse response = null;  
         HttpEntity entity = null;  
         String responseContent = null;  
    	
        try {  
        	 // 创建默认的httpClient实例.  
            httpClient = HttpClients.createDefault();  
            //设置参数  
            StringEntity stringEntity = new StringEntity(params, "UTF-8");  
            stringEntity.setContentType("application/json");
            
            switch (method) {
			case "POST":
				HttpPost httpPost = new HttpPost(httpUrl);// 创建httpPost   
				httpPost.setConfig(requestConfig);  
				httpPost.setEntity(stringEntity); 
				// 执行请求  
                response = httpClient.execute(httpPost); 
				break;

			case "GET":
				HttpGet httpGet = new HttpGet(httpUrl);// 创建get请求  
				// 执行请求  
                response = httpClient.execute(httpGet);
				break;
			case "PUT":
				break;
			case "DELETE":
				break;
				
			}
            
            entity = response.getEntity();  
            responseContent = EntityUtils.toString(entity, "UTF-8");  
             
               
            // 关闭连接,释放资源  
            if (response != null) {  
                response.close();  
            }  
            if (httpClient != null) {  
                httpClient.close();  
            }  
              
            
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
        return responseContent;  
    }  
	
	

}
