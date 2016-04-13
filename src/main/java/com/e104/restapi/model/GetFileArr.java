package com.e104.restapi.model;

import javax.xml.bind.annotation.XmlElement;

public class GetFileArr  {
	private String fileId;
	private String protocol;
	private String fileTag;
	
	@XmlElement(name = "fileId")
	public void setFileId(String fileId){
		this.fileId = fileId;
	}
	public String getFileId(){
		return this.fileId;
	}
	
	@XmlElement(name = "protocol")
	public void setProtocol(String protocol){
		this.protocol = protocol;
	}
	public String getProtocol(){
		return this.protocol;
	}
	
	@XmlElement(name = "fileTag")
	public void setFileTag(String fileTag){
		this.fileTag = fileTag;
	}
	public String getFileTag(){
		return this.fileTag;
	}
}
