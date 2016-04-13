package com.e104.restapi.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

public class VideoImageSize {
	private int width;
	private int height;
	private String tag;
	private int sec;
	
	
	@XmlElement(name = "width")
	public void setWidth(int width){
		this.width = width;
	}
	public int getWidth(){
		return this.width;
	}
	
	@XmlElement(name = "height")
	public void setHeight(int height){
		this.height = height;
	}
	public int getHeight(){
		return this.height;
	}
	
	@XmlElement(name = "tag")
	public void setTagt(String tag){
		this.tag = tag;
	}
	public String getTag(){
		return this.tag;
	}
	
	@XmlElement(name = "sec")
	public void setSec(int sec){
		this.sec = sec;
	}
	public int getSec(){
		return this.sec;
	}
}
