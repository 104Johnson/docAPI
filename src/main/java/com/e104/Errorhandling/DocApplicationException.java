package com.e104.Errorhandling;

public class DocApplicationException  extends Exception 
{
	private int code;
    private static final long serialVersionUID = 1L;
    public  DocApplicationException(Exception e ,int code) {
    	super(e);
    	this.code=code;
    }
    public DocApplicationException(String msg,int code)   {
        super(msg);
        this.code=code;
    }

    public int getCode(){
    	return code;
    }
    
    
}