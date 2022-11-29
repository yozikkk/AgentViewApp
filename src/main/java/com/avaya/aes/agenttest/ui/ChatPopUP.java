package com.avaya.aes.agenttest.ui;

import javax.swing.*;

public class ChatPopUP {	 
	 
	 public JFrame showPopUp() {
		 
		    JFrame jFrame = new JFrame();
		    jFrame.setAlwaysOnTop(true);
		    jFrame.setVisible(true);
		    jFrame.setLocation(700,100);
		    
	        int result = JOptionPane.showConfirmDialog(jFrame, "Принять чат?");

	        if (result == 0) {
	        	
	        	 System.out.println("You pressed Yes");
	        	 jFrame.hide();
	        }
	        	
	  
	        
	           
	        else if (result == 1) {
	        	  System.out.println("You pressed NO");
	        }
	          
	        else {
	        	System.out.println("You pressed Cancel");
	        }
	           
			return jFrame;
	 }

	}


