package com.e104.util;

import java.nio.ByteBuffer;

import org.json.JSONException;
import org.json.JSONObject;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.regions.RegionUtils;
import com.amazonaws.services.kinesisfirehose.AmazonKinesisFirehoseClient;
import com.amazonaws.services.kinesisfirehose.model.PutRecordRequest;
import com.amazonaws.services.kinesisfirehose.model.Record;
import com.e104.Errorhandling.DocApplicationException;


public class TraceLog {
	protected static AmazonKinesisFirehoseClient firehoseClient;
	
	//private static transient Logger Logger = LogManager.getLogger(TraceLog.class);

		 public void writeKinesisLog(String track_id,String caller,String src,String msg, JSONObject extraObj) throws DocApplicationException{
			 tools tools =new tools();
			 JSONObject rtn = new JSONObject();
			 try{
				
				 String ts = tools.getCurrentUTCTimestamp((byte)0);
				 
				 if (track_id==null || "".equals(track_id))
					 track_id =tools.generateTxid();
				 
				 if (extraObj.length()>0)
					 rtn.put("extra", extraObj);
				 
				 rtn.put("ts", ts);
				 rtn.put("track_id", track_id);
				 rtn.put("caller", caller);
				 rtn.put("src", src);
				 rtn.put("msg", msg);
				 putRecod(rtn.toString());
				 //System.out.println(rtn.toString());
				 //Logger.info(rtn);
			 }catch(JSONException e){
				 throw new DocApplicationException("Json格式轉換失敗", 1);
			 }

	    }
		 
	
	
		private void putRecod(String JsonObj){
			
			PutRecordRequest putRecordRequest = new PutRecordRequest();
			putRecordRequest.setDeliveryStreamName("e104-logs-general");

			String data = JsonObj + "\n";

			Record record = createRecord(data);
			putRecordRequest.setRecord(record);

			
			//AWSCredentials credentials = null;
			
			//credentials =  new InstanceProfileCredentialsProvider().getCredentials();
			// Firehose client
	        firehoseClient = new AmazonKinesisFirehoseClient(new DefaultAWSCredentialsProviderChain());
	        firehoseClient.setRegion(RegionUtils.getRegion("us-west-2"));
	       
	        // Put record into the DeliveryStream
			firehoseClient.putRecord(putRecordRequest);

		}
		 /**
	     * Method to create the record object for given data.
	     *
	     * @param data the content data
	     * @return the Record object
	     */
	    private static Record createRecord(String data) {
	        return new Record().withData(ByteBuffer.wrap(data.getBytes()));
	    }
}
