package com.e104.restapi.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;



import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;



import org.json.JSONObject;



import org.json.JSONArray;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties
@XmlRootElement(name = "getFileUrl")
@XmlType(name = "", propOrder = {"fileId", "protocol", "fileTag"})
public class GetFileUrl {
	private List<GetFileArr> getfileArr = new ArrayList<>();
	private JSONObject rtn = new JSONObject();
	private String timestamp;
	
	@XmlElementWrapper(name = "getFileArr")
	@XmlElement(name = "getFileArr")
	public void setFileArr(List<GetFileArr> getFileArr){

		//JSONArray jsonArr =new JSONArray();
	    Collection<GetFileArr> collection = getFileArr;
	    this.getfileArr.addAll(collection);

	    
		this.rtn.put("getFileArr",this.getfileArr);
		
	}
	public List<GetFileArr> getGetFileArr(){
		
		return getfileArr;
	}
	public void clearFileArr(){
		this.getfileArr.clear();
	}
	
	
	
	@XmlElement(name = "timestamp")
	public void setTimestamp(String timestamp){
		this.timestamp = timestamp;
		this.rtn.put("timestamp", this.timestamp);
	}
	public String getTimestamp(){
		return this.timestamp;
	}
	
	

}
