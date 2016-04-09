package com.e104.restapi.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties
@XmlRootElement(name = "signature")
@XmlType(name = "", propOrder = {"fileId", "protocol", "fileTag"})
public class Signature {
//"apnum":"10400","pid":"10400","contenttype":"image/jpeg","Content_Disposition":"photo.jpg","isP":1,"extra": {"convert":"false","extraNo":"6ee88242-33cd-4b8f-8347-6d45650ca987","multiAction":[{"isSave":"1","height":"0","basis":"9","tag":"channelGridXL","width":"476","toTypeObj":{"toType":"png","fromType":"gif"},"isNewFileId":"0","method":"resize","isGetUrl":"1"}]},"title":"測試","description":"測試"}
	private String apnum;
	private String pid;
	private String contentDisposition;
	private String isP;
	private Extra extra;
	private String contenttype;
	private String title;
	private String description;
	
	
	@XmlElement(name = "apnum")
	public void setApnum(String apnum){
		this.apnum = apnum;
	}
	public String getApnum(){
		return this.apnum;
	}
	
	@XmlElement(name = "pid")
	public void setPid(String pid){
		this.pid = pid;
	}
	public String getPid(){
		return this.pid;
	}
	
	@XmlElement(name = "contentDisposition")
	public void setContentDisposition(String contentDisposition){
		this.contentDisposition = contentDisposition;
	}
	public String getContentDisposition(){
		return this.contentDisposition;
	}
	
	@XmlElement(name = "isP")
	public void setIsP(String isP){
		this.isP = isP;
	}
	public String getIsP(){
		return this.isP;
	}
	
	@XmlElement(name = "extra")
	public void setExtra(Extra extra){
		this.extra = extra;
	}
	public Extra getExtra(){
		return this.extra;
	}
	
	@XmlElement(name = "contenttype")
	public void setContenttype(String contenttype){
		this.contenttype = contenttype;
	}
	public String getContenttype(){
		return this.contenttype;
	}
	
	@XmlElement(name = "title")
	public void setTitle(String title){
		this.title = title;
	}
	public String getTitle(){
		return this.title;
	}
	
	@XmlElement(name = "description")
	public void setDescription(String description){
		this.description = description;
	}
	public String getDescription(){
		return this.description;
	}
	
	
}
