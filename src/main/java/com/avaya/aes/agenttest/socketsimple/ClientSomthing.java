package com.avaya.aes.agenttest.socketsimple;

import com.avaya.aes.agenttest.restapi.CallRestAPI;
import com.avaya.aes.agenttest.ui.AgentStateUI;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ClientSomthing {


	    private Socket socket;
	    private BufferedReader in; // поток чтения из сокета
	    private BufferedWriter out; // поток чтения в сокет
	    private BufferedReader inputUser; // поток чтения с консоли
	    private String addr; // ip адрес клиента
	    private int port; // порт соединения
	    private String nickname; // имя клиента
	    private Date time;
	    private String dtime;
	    private SimpleDateFormat dt1;
	    private String text;
	    /**
	     * для создания необходимо принять адрес и номер порта
	     *
	     * @param addr
	     * @param port
	     */
	    
	    
	    
	    
	    public void getConnected(String addr, int port) {
	        this.addr = addr;
	        this.port = port;
	        try {
	            this.socket = new Socket(addr, port);
	            System.out.println("socket on client:"+socket);
	        } catch (IOException e) {
	            System.err.println("Socket failed");
	        }
	        try {
	            // потоки чтения из сокета / записи в сокет, и чтения с консоли
	            inputUser = new BufferedReader(new InputStreamReader(System.in));
	            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
	            //this.pressNickname(); // перед началом необходимо спросит имя
	            new ReadMsg().start(); // нить читающая сообщения из сокета в бесконечном цикле
	            new WriteMsg().start(); // нить пишущая сообщения в сокет приходящие с консоли в бесконечном цикле
	        } catch (IOException e) {
	            // Сокет должен быть закрыт при любой
	            // ошибке, кроме ошибки конструктора сокета:
	            ClientSomthing.this.downService();
	        }
	        // В противном случае сокет будет закрыт
	        // в методе run() нити.
	    }
	    
	    public String getText() {
			return text;
		}

		public void setText(String text) {
			this.text = text;
		}

		/**
	     * просьба ввести имя,
	     * и отсылка эхо с приветсвием на сервер
	     */
	    
	   public void processMessage(String msg) {
	        try {
	            out.write(msg); //send text to server
	            //jtfMsg.setText(""); //clear out text input field
	        }
	        catch (IOException ioe) {
	            System.err.println(ioe);
	        }
	    }
	    
	    private void pressNickname() {
	        System.out.print("Press your nick: ");
	        try {
	            nickname = inputUser.readLine();
	            out.write("Hello " + nickname + "\n");
	            out.flush();
	        } catch (IOException ignored) {
	        }
	        
	    }
	    
	    /**
	     * закрытие сокета
	     */
	    private void downService() {
	        try {
	            if (!socket.isClosed()) {
	                socket.close();
	                in.close();
	                out.close();
	            }
	        } catch (IOException ignored) {}
	    }
	    
	    // нить чтения сообщений с сервера
	    private class ReadMsg extends Thread {
	        @Override
	        public void run() {
	        	CallRestAPI api = new CallRestAPI();
	            String str;
	            try {
	                while (true) {
	                    str = in.readLine(); // ждем сообщения с сервера
	                    if (str.equals("stop")) {
	                        ClientSomthing.this.downService(); // харакири
	                        break; // выходим из цикла если пришло "stop"
	                    }
	                    //System.out.println(str); // пишем сообщение с сервера на консоль
	                    time = new Date(); // текущая дата
	                    dt1 = new SimpleDateFormat("HH:mm:ss"); // берем только время до секунд
	                    dtime = dt1.format(time); // время
	                    
	                    JSONObject jsonObject = new JSONObject(str);
	            		String fullMesage = jsonObject.getString("message");
	            		
	            		if(AgentStateUI.readyForChat) {
	            			if(AgentStateUI.chatid !=null) {
		            			if(AgentStateUI.chatid.equals(jsonObject.getLong("chatid")))
		            				AgentStateUI.textChatArea.append(dtime+"  "+fullMesage+"\n");
		            					            			
		            			
		            		}
		            		else {
		            			
		            			
		            			 String jsonChatState =  "{ "
			  					      		+ "\"chatId\":\""+jsonObject.getLong("chatid")+"\"}";      			 
		            			 if(api.doPost(jsonChatState,"getChatState").isEmpty()) {
		            				 String jsonInputString =  "{ "
				  					      		+ "\"agentid\":\""+AgentStateUI.getAgentId()+"\","
				  					      		+ "\"chatId\":\""+jsonObject.getLong("chatid")+"\"}";
				  					    try {
				  							api.doPost(jsonInputString, "assignChatToAgent");
				  							AgentStateUI.chatid = jsonObject.getLong("chatid"); 
					            			AgentStateUI.textChatArea.append(dtime+"  "+fullMesage+"\n");
				  						} catch (IOException e) {
				  							// TODO Auto-generated catch block
				  							e.printStackTrace();
				  						}
				            		
		            			 }
		       
		            		}
	            		}
	            		
	            	
	                }
	            } catch (IOException | JSONException e) {
	                ClientSomthing.this.downService();
	            }
	        }
	    }
	    
	    // нить отправляющая сообщения приходящие с консоли на сервер
	    public class WriteMsg extends Thread {
	        
	        @Override
	        public void run() {
	        	
	        	
	        	
	            while (true) {
	                String userWord;
	                try {
	                    time = new Date(); // текущая дата
	                    dt1 = new SimpleDateFormat("HH:mm:ss"); // берем только время до секунд
	                    dtime = dt1.format(time); // время
	                    userWord = getText(); // сообщения с консоли
	                    if (userWord.equals("stop")) {
	                        out.write("stop" + "\n");
	                        ClientSomthing.this.downService(); // харакири
	                        break; // выходим из цикла если пришло "stop"
	                    } else {
	                        out.write("(" + dtime + ") " + nickname + ": " + userWord + "\n"); // отправляем на сервер
	                    }
	                    out.flush(); // чистим
	                } catch (IOException e) {
	                    ClientSomthing.this.downService(); // в случае исключения тоже харакири
	                    
	                }
	                
	            }
	        }
	    }
	}