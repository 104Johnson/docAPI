package com.e104.restapi.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

public class Extra {
	//"extra": {"convert":"false","extraNo":"6ee88242-33cd-4b8f-8347-6d45650ca987","multiAction":[{"isSave":"1","height":"0","basis":"9","tag":"channelGridXL","width":"476","toTypeObj":{"toType":"png","fromType":"gif"},"isNewFileId":"0","method":"resize","isGetUrl":"1"}]},"title":"測試","description":"測試"}
		private String convert;
		private String extraNo;
		private List<MultiAction> multiAction = new ArrayList<>();
		private List<VideoImageSize> videoImageSize = new ArrayList<>();
		
		@XmlElement(name = "convert")
		public void setConvert(String convert){
			this.convert = convert;
		}
		public String getConvert(){
			return this.convert;
		}
		
		@XmlElement(name = "extraNo")
		public void setExtraNo(String extraNo){
			this.extraNo = extraNo;
		}
		public String getExtraNo(){
			return this.extraNo;
		}
		
		@XmlElement(name = "multiAction")
		public void setMultiAction(List<MultiAction> multiAction){
			this.multiAction = multiAction;
		}
		public List<MultiAction> getMultiAction(){
			return this.multiAction;
		}
		
		@XmlElement(name = "videoImageSize")
		public void setVideoImageSize(List<VideoImageSize> videoImageSize){
			this.videoImageSize = videoImageSize;
		}
		public List<VideoImageSize> getVideoImageSize(){
			return this.videoImageSize;
		}
		
}
