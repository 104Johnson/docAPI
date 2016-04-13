package com.e104.restapi.model;

import javax.xml.bind.annotation.XmlElement;

public class UpdateFile {
//fileId, String title, String description, String fileTag
	private String fileId;
	private String title;
	private String description;
	private String fileTag;
	
	@XmlElement(name = "fileId")
	public void setFileId(String fileId){
		this.fileId = fileId;
	}
	public String getFileId(){
		return this.fileId;
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
	
	@XmlElement(name = "fileTag")
	public void setFileTag(String fileTag){
		this.fileTag = fileTag;
	}
	public String getFileTag(){
		return this.fileTag;
	}
}
