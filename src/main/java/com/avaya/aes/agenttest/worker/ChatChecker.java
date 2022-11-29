package com.avaya.aes.agenttest.worker;

import com.avaya.aes.agenttest.jsonparser.JsonParse;
import com.avaya.aes.agenttest.restapi.CallRestAPI;
import com.avaya.aes.agenttest.ui.AgentStateUI;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatChecker implements Runnable{

	
	private volatile boolean isrunning = true;
	public void run() {
		
		
		while(isrunning) {
			
			try {
				Thread.sleep(3000);	
				CallRestAPI api = new CallRestAPI();
				JsonParse  parse = new JsonParse();				
				JSONObject jsonObject = parse.parse(api.doGet("getMessage",AgentStateUI.chatid));
		
				
				String pattern = "HH:mm:ss";
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
				String date = simpleDateFormat.format(new Date());
				if(jsonObject.isNull("message")) {
					System.out.println("активных чатов нет");
				}
				else {
					
				  	String json =  "{"
				      		+ "\"agentid\":\""+AgentStateUI.getAgentId()+"\"}";
				  	String jsonResp = api.doPost(json,"getAgentState");
				  	JSONObject jsonStateObject = parse.parse(jsonResp);	
					if(jsonStateObject.isNull("chatId")||jsonStateObject.getString("chatId").isEmpty()) {
						
						System.out.println("чат не назначен агенту");
						 String jsonInputString =  "{ "
						      		+ "\"agentid\":\""+AgentStateUI.getAgentId()+"\","
						      		+ "\"chatId\":\""+jsonObject.getLong("chatid")+"\"}";
						    try {
						    	System.out.println("Назначаем чат:"+jsonObject.getLong("chatid")+" агенту :"+AgentStateUI.getAgentId());
								api.doPost(jsonInputString, "assignChatToAgent");
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}	
						
				
					}
					else {
						
						System.out.println("чат уже назначен агенту");
						System.out.println(jsonStateObject.getString("chatId"));
					}
					
					
					
					
					String fullMesage = jsonObject.getString("message");
					if(jsonObject.getString("message").isEmpty()) {
						System.out.println("нет новых сообщений от чата с id "+ AgentStateUI.chatid);
					}
					else {
						
						//ChatPopUP pop = new ChatPopUP();
						//pop.showPopUp();
						
						AgentStateUI.chatid = jsonObject.getLong("chatid");
						for(String message :fullMesage .split(";") ) {
							AgentStateUI.textChatArea.append(date+"  "+message+"\n");

						}
					}
					
				}
				
			} catch (InterruptedException | IOException | JSONException e) {
				
				System.out.println("Interrupted, so exiting.");
				e.printStackTrace();
			}	
		}		
		
	}
	
	public void stop() {
		this.isrunning =false;
		
	}
	
  public  void getMessages() throws IOException, JSONException {
		
		String pattern = "HH:mm:ss";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		String date = simpleDateFormat.format(new Date());
		CallRestAPI call = new CallRestAPI();

		
		
		JSONObject jsonObject = new JSONObject(call.doGet("getMessage",AgentStateUI.chatid));
		String fullMesage = jsonObject.getString("message");
		AgentStateUI.chatid = jsonObject.getLong("chatid");
		
		
		for(String message :fullMesage .split(";") ) {
			System.out.println(message);
			AgentStateUI.textChatArea.append(date+"  "+message+"\n");
			
		}
	}

}
