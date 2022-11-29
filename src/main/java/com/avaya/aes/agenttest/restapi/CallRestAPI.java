package com.avaya.aes.agenttest.restapi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class CallRestAPI {
	
	
	public String doPost(String jsonInputString,String action) throws IOException{
		

	      StringBuilder response = new StringBuilder();
	      URL url = new URL ("http://localhost:8080/"+action);
	      HttpURLConnection con = (HttpURLConnection)url.openConnection();
	      con.setRequestMethod("POST");
	      con.setRequestProperty("Content-Type", "application/json; utf-8");
	      con.setRequestProperty("Accept", "application/json");
	      con.setDoOutput(true);
	      System.out.println(jsonInputString);
	      try(OutputStream os = con.getOutputStream()) {
	    	    byte[] input = jsonInputString.getBytes("utf-8");
	    	    os.write(input, 0, input.length);			
	    	}
	      try(BufferedReader br = new BufferedReader(
	    		  new InputStreamReader(con.getInputStream(), "utf-8"))) {
	    		    
	    		    String responseLine = null;
	    		    while ((responseLine = br.readLine()) != null) {
	    		        response.append(responseLine.trim());
	    		    }  
	    		} 
	      con.disconnect();
		return response.toString();
	      
	
	}
	
	
	public String doDelete(String jsonInputString,String action) throws IOException{
		
	      StringBuilder response = new StringBuilder();
	      URL url = new URL ("http://localhost:8080/"+action);
	      HttpURLConnection con = (HttpURLConnection)url.openConnection();
	      con.setRequestMethod("DELETE");
	      con.setRequestProperty("Content-Type", "application/json; utf-8");
	      con.setRequestProperty("Accept", "application/json");
	      con.setDoOutput(true);
	      System.out.println(jsonInputString);
	      try(OutputStream os = con.getOutputStream()) {
	    	    byte[] input = jsonInputString.getBytes("utf-8");
	    	    os.write(input, 0, input.length);			
	    	}      
	      try(BufferedReader br = new BufferedReader(
	    		  new InputStreamReader(con.getInputStream(), "utf-8"))) {  		    
	     		    String responseLine = null;
	    		    while ((responseLine = br.readLine()) != null) {
	    		        response.append(responseLine.trim());
	    		    }	    
	    		}
	      con.disconnect();
		return response.toString();
	      
	
	}
	
public String doGet(String action,Long chatid) throws IOException{
		
	    
	      URL url;
	      StringBuilder result = new StringBuilder();      
	      if(chatid == null) {	  
	    	  url = new URL("http://localhost:8080/"+action);  
	      }
	      else {
	    	  url = new URL("http://localhost:8080/"+action+"?chatid="+chatid);   	  
	      }
	      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	      conn.setRequestMethod("GET");
	      try (BufferedReader reader = new BufferedReader(
	                  new InputStreamReader(conn.getInputStream()))) {
	          for (String line; (line = reader.readLine()) != null; ) {
	              result.append(line);
	          }
	      }
		
	  return result.toString();
	}
	
	
	
	




	
	

}
