package com.e104.errorhandling;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.json.JSONObject;

@Provider
public class ClientExceptionMapper implements ExceptionMapper<ClientErrorException>{
	JSONObject errObject = new JSONObject();
	@Override
    public Response toResponse(ClientErrorException e)  {
        e.printStackTrace();
        errObject.put("message",e.getMessage());
    	errObject.put("code","");
    	errObject.put("trace_id","");
        return Response.status(e.getResponse().getStatus()).header("Content-Type", "application/json").entity(new String(errObject.toString())).build();
    }
}
