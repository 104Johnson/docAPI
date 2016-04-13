package com.e104.restapi.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.json.JSONObject;
@XmlRootElement(name = "putFile")
public class MultiAction {
	private String isSave;
	private String height;
	private String basis;
	private String tag;
	private String width;
	private String isNewFileId;
	private String method;
	private String isGetUrl;
	private JSONObject toType;
	
	@XmlElement(name = "isSave")
	public void setIsSave(String isSave){
		this.isSave = isSave;
	}
	public String getIsSave(){
		return this.isSave;
	}
	
	@XmlElement(name = "height")
	public void setHeight(String height){
		this.height = height;
	}
	public String getHeight(){
		return this.height;
	}
	
	@XmlElement(name = "basis")
	public void setBasis(String basis){
		this.basis = basis;
	}
	public String getBasis(){
		return this.basis;
	}
	
	@XmlElement(name = "tag")
	public void setTag(String tag){
		this.tag = tag;
	}
	public String getTag(){
		return this.tag;
	}
	
	@XmlElement(name = "width")
	public void setWidth(String width){
		this.width = width;
	}
	public String getWidth(){
		return this.width;
	}
	
	@XmlElement(name = "isNewFileId")
	public void setIsNewFileId(String isNewFileId){
		this.isNewFileId = isNewFileId;
	}
	public String getIsNewFileId(){
		return this.isNewFileId;
	}
	
	@XmlElement(name = "isGetUrl")
	public void setIsGetUrl(String isGetUrl){
		this.isGetUrl = isGetUrl;
	}
	public String getIsGetUrl(){
		return this.isGetUrl;
	}
	
	@XmlElement(name = "toType")
	public void setToType(JSONObject toType){
		this.toType = toType;
	}
	public JSONObject getToType(){
		return this.toType;
	}
	//{"isSave":"1","height":"0","basis":"9","tag":"channelGridXL","width":"476","toTypeObj":{"toType":"png","fromType":"gif"},"isNewFileId":"0","method":"resize","isGetUrl":"1"}
}
