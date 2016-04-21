package com.e104.Errorhandling;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.json.JSONObject;

@Provider
public class DocErrorhandling implements ExceptionMapper<DocApplicationException> 
{
    @Override
    public Response toResponse(DocApplicationException exception) 
    {
    	JSONObject errObject = new JSONObject();
    	
    	if( exception instanceof DocApplicationException ){
    		exception.printStackTrace();
    		errObject.put("message",exception.getMessage());
        	errObject.put("code",((DocApplicationException)exception).getCode());
        	errObject.put("trace_id","");
        	String returnError =  new JSONObject().put("error", errObject).toString();
        	
    		switch (((DocApplicationException)exception).getCode()) {
			
    		case 1:
    			return Response.status(Status.BAD_REQUEST).entity(returnError).build();
			case 2:
				return Response.status(Status.BAD_REQUEST).entity(returnError).build();
			case 3:
				return Response.status(Status.BAD_REQUEST).entity(returnError).build();
			case 12:
				return Response.status(Status.BAD_REQUEST).entity(returnError).build();
			default:
				return Response.serverError().build();
			}
    		
    	}else{
    		
        	return Response.serverError().build();
    	}

       
    }
    
   
}
