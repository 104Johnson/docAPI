package com.e104.Errorhandling;

import javax.ws.rs.ServerErrorException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.json.JSONObject;

@Provider
public class ServerExceptionMapper implements ExceptionMapper<ServerErrorException> {
	JSONObject errObject = new JSONObject();
    @Override
    public Response toResponse(ServerErrorException e) {
        e.printStackTrace();
        errObject.put("message",e.getMessage());
    	errObject.put("code",e.getResponse().getStatus());
    	errObject.put("trace_id","");
    	return Response.status(Response.Status.INTERNAL_SERVER_ERROR).header("Content-Type", "application/json").entity(errObject.toString()).build();
    }
   
}