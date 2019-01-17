package com.backend;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;
import org.eclipse.jetty.http.HttpStatus;

import com.backend.Application;
import com.backend.response.StatusResponse;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import spark.Spark;

public abstract class AbstractTest {
	
	protected void startServer() {
		String[] args = {};
		Application.main(args);
	}

	protected void stopServer() {
		Spark.stop();
	}
	
	protected StatusResponse getStatusResponse(HttpResponse httpResponse){
		try {
			return new Gson().fromJson(EntityUtils.toString(httpResponse.getEntity()), StatusResponse.class);
		} catch (JsonSyntaxException | ParseException | IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	protected StatusResponse createErrorStatusResponse(){
		return  new StatusResponse(HttpStatus.BAD_REQUEST_400, "An error occurred!");
	}
}
