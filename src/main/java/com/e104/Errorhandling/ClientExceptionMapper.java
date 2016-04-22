package com.e104.Errorhandling;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.json.JSONObject;


@Provider
public class ClientExceptionMapper implements ExceptionMapper<ClientErrorException>{
	
	@Context
	private MessageContext context;
	
	JSONObject errObject = new JSONObject();
	@Override
    public Response toResponse(ClientErrorException e)  {
        e.printStackTrace();
        errObject.put("message",e.getMessage());
    	errObject.put("code","");
    	errObject.put("trace_id",context);
        return Response.status(e.getResponse().getStatus()).header("Content-Type", "application/json").entity(new String(errObject.toString())).build();
    }
}