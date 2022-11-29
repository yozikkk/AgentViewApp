package com.avaya.aes.agenttest.ui;


import com.avaya.aes.agenttest.callcontrol.AvayaCallCenterUserAgent;
import com.avaya.aes.agenttest.callcontrol.CallControlUpdate;
import com.avaya.aes.agenttest.callcontrol.CallManager;
import com.avaya.aes.agenttest.restapi.CallRestAPI;
import com.avaya.aes.agenttest.worker.ChatChecker;
import com.avaya.jtapi.tsapi.LucentAgent;
import com.avaya.jtapi.tsapi.TsapiInvalidArgumentException;
import com.avaya.jtapi.tsapi.TsapiInvalidStateException;
import com.avaya.jtapi.tsapi.TsapiPlatformException;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.text.DefaultCaret;
import javax.telephony.*;
import javax.telephony.callcenter.Agent;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Observable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * This class represent the Agent State UI. It allows the user to:
 * 1. Change/Set the Agent State with enablePendingFlag value.
 * 2. Answer/Disconnect the active Call.
 * 3. Logout the Agent.
 * 4. Display the current Agent State and other call related messages.
 */
public final class AgentStateUI extends JFrame implements
	AgentStateInterface, CallStateInterface {

	private static Logger logger = Logger.getLogger(AgentStateUI.class);
	
	//To avoid warning defined serialVersionUID
	final static long serialVersionUID = 1;
	
	// to store callID
	private long callId;

	// Answer button
	private JButton answerButton;
	
	// Log Out button
	private JButton logoutButton;
	
	// Change State button
	private JButton changeStateButton;
	
	// Drop button
	private JButton dropButton;
	
	/**
	 * This Button group is used to group
	 * the Ready, Not Ready radio buttons.
	 */
	private ButtonGroup agentStateButtonGroup;

	/**
	 * This Panel holds the components to change the agent state.
	 * It holds the radio buttons 'Ready', 'Not Ready', 
	 * the 'Enable Pending' check box, and 'Change State' button.
	 */
	private JPanel agentStatePanel;

	/**
	 * This Panel holds the components for display of agentId and
	 * Station extension agent state.
	 */
	private JPanel agentIDPanel;

	/**
	 * This Panel holds the label components to display the 
	 * 'Current Agent State'
	 */
	private JPanel currAgentStatePanel;

	/**
	 * This ScrollPane component is used to scroll the text area
	 * on the PhoneGUI form
	 */
	private JScrollPane mainTextAreaScrollPane;
	
	
	private JScrollPane chatTextAreaScrollPane;

	// Label to display 'Request State change to'
	private JLabel agentStateLabel;

	// 'Not Ready' radio button
	private JRadioButton notReadyRadioButton;

	// 'Ready' radio button
	private JRadioButton readyRadioButton;

	// 'Work Not Ready' radio button
	private JRadioButton workNotReadyRadioButton;

	// Text area to display the information
	private JTextArea infoTextArea;

	// Label to display 'Current Agent State'
	private JLabel currAgentStateLabel;

	// Label to display 'Agent ID'
	private JLabel agentIDLabel;

	// Label to display 'Station Extension'
	private JLabel stationExtnLabel;

	// Label to display 'Current Agent State'
	private JLabel agentIdValueLabel;

	// Label to display 'Current Agent State'
	private JLabel extensionIDLabel;

	// Label to display 'Enable Pending'
	private JLabel enablePendingLabel;
	
	// Check box to check whether 'Enable Pending' is true or false
	private JCheckBox enablePendingCheckBox;
	private AvayaCallCenterUserAgent userAgentObj;
	private CallManager callManagerObj;
	private int previousAgentState = LucentAgent.UNKNOWN;
	private String stationExtn;
	private String agentID;
	public static Long chatid;
	private static String agentIdNew;
	// To store agent mode
	private int agentMode ;
	private LoginUI loginUIObj;
	private ChatChecker chatChecker ;
	private ExecutorService service ;

	
	/**
	 * Class Constructor
	 * @param agtObjectParam
	 * @param callMngrObjParam
	 * @param stationExtension
	 * @param agentID
	 * @param agentWORKMode

	 * @throws HeadlessException
	 * @throws TsapiInvalidArgumentException
	 * @throws TsapiPlatformException
	 * @throws TsapiInvalidStateException
	 * @throws MethodNotSupportedException
	 * @throws ResourceUnavailableException
	 * @throws InvalidArgumentException
	 * @throws JtapiPeerUnavailableException
	 * @throws ProviderUnavailableException
	 */
	public AgentStateUI(AvayaCallCenterUserAgent agtObjectParam,
			CallManager callMngrObjParam, String stationExtension,
			String agentID, int agentWORKMode,
			LoginUI loginUIObject)
	throws HeadlessException, TsapiInvalidArgumentException,
	TsapiPlatformException, TsapiInvalidStateException,
	MethodNotSupportedException, ResourceUnavailableException,
	InvalidArgumentException, JtapiPeerUnavailableException,
	ProviderUnavailableException {

		super();
		this.agentID = agentID;
		this.stationExtn = stationExtension;
		this.userAgentObj = agtObjectParam;
		this.agentMode = agentWORKMode;
		this.callManagerObj = callMngrObjParam;
		this.loginUIObj = loginUIObject;
		
		setupDialog();
		attach();
		setAgentId();
		

		String msg = "Agent logged in Details:\n";
		msg += "Terminal Address : " + stationExtension + "\n";
		msg += "Agent ID: " + agentID + "\n";
		
		if (agentWORKMode == LucentAgent.MODE_AUTO_IN) {
			msg += "Agent Work Mode : AUTO IN";
		} else if (agentWORKMode == LucentAgent.MODE_MANUAL_IN) {
			msg += "Agent Work Mode : MANUAL IN";
		}
		
		updateTextArea(msg);
    }

	/**
	 * Register as a listener to the Call Manager and Agent User 
	 */
	private void attach() {
		userAgentObj.addObserver(this);
		callManagerObj.addObserver(this);
	}

	/**
	 * This method is called from within the constructor to initialize the
	 * fields in the GUI.
	 */
	private void setupDialog() {
		
		Color colorObj = new Color(0, 0, 255);
		Border lineBorder = new LineBorder(colorObj);
		
		// Panel for displaying Current Agent State
		currAgentStatePanel = new JPanel();
		
		// button group to group 'Ready' and 'Not Ready' buttons
		agentStateButtonGroup = new ButtonGroup();
		
		// Panel for holding 'Change State' component variables.
		agentStatePanel = new JPanel();
		
		// Panel for holding AgentId and StationExtension component variables.
		agentIDPanel = new JPanel();

		changeStateButton = new JButton();

		// text area to display the information
		infoTextArea = new JTextArea(50,5);

		// scrollPane to scroll the textArea
		mainTextAreaScrollPane = new JScrollPane(infoTextArea);
		
	//Hey my new update
		

		// Logout button
		logoutButton = new JButton();

		// Answer button
		answerButton = new JButton();

		// Drop button
		dropButton = new JButton();

		addWindowListener(win);
		setTitle("Agent State GUI");
		setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
		setMaximizedBounds(new Rectangle(0, 0, 410, 800));
		setResizable(false);
		setName("Agent State GUI");
		getContentPane().setLayout(null);

		agentStatePanel.setLayout(null);
		agentIDPanel.setLayout(null);
		currAgentStatePanel.setLayout(null);

		// Current Agent state display
		currAgentStateLabel = new JLabel();
		currAgentStateLabel.setHorizontalAlignment(0);
		currAgentStateLabel.setHorizontalTextPosition(0);
		currAgentStatePanel.add(currAgentStateLabel);
		currAgentStateLabel.setBounds(40, 10, 300, 25);

		// label 'Request State Change To'
		agentStateLabel = new JLabel();
		agentStatePanel.setBorder(new LineBorder(new Color(0, 0, 0)));
		agentStateLabel.setText("Request State change to :");
		agentStatePanel.add(agentStateLabel);
		agentStateLabel.setBounds(20, 10, 145, 30);

		// Label and check box for 'Enable Pending'
		enablePendingLabel = new JLabel("enablePending ");
		enablePendingCheckBox = new JCheckBox();
		agentStatePanel.add(enablePendingCheckBox);
		enablePendingCheckBox.setBounds(190, 70, 20, 30);
		agentStatePanel.add(enablePendingLabel);
		enablePendingLabel.setBounds(220, 60, 100, 50);

		// Ready radio button
		readyRadioButton = new JRadioButton();
		agentStateButtonGroup.add(readyRadioButton);
		readyRadioButton.setText("READY");
		readyRadioButton.setMnemonic('r');

		agentStatePanel.add(readyRadioButton);
		readyRadioButton.setBounds(190,10,75, 30);

		// Not ready radio button
		notReadyRadioButton = new JRadioButton();
		agentStateButtonGroup.add(notReadyRadioButton);
		notReadyRadioButton.setText("NOT READY");
		notReadyRadioButton.setMnemonic('n');

		agentStatePanel.add(notReadyRadioButton);
		notReadyRadioButton.setBounds(270,10,90,30);
	
		// Work Not Ready radio button
		workNotReadyRadioButton = new JRadioButton();
		agentStateButtonGroup.add(workNotReadyRadioButton);
		workNotReadyRadioButton.setText("WORK NOT READY");
		workNotReadyRadioButton.setMnemonic('w');

		agentStatePanel.add(workNotReadyRadioButton);
		workNotReadyRadioButton.setBounds(190,35, 150, 30);

		// Change state button
		changeStateButton.setText("Change State");
		changeStateButton.setMnemonic('c');
		agentStatePanel.add(changeStateButton);

		changeStateButton.setBounds(320, 75, 110, 23);
		changeStateButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent actionEventParam) {
				onBnClickedSetAgtState();
			}
		});

		agentIDLabel = new JLabel();
		agentIDLabel.setText("Agent ID :");
		agentIDPanel.setBorder(new LineBorder(new Color(0, 0, 0)));
		agentIDPanel.add(agentIDLabel);
		agentIDLabel.setBounds(20,10,75, 25);

		agentIdValueLabel = new JLabel();
		agentIdValueLabel.setText(agentID);
		agentIDPanel.add(agentIdValueLabel);
		agentIdValueLabel.setBounds(85,10,75, 25);

		stationExtnLabel = new JLabel();
		stationExtnLabel.setText("Station Extension :");
		agentIDPanel.add(stationExtnLabel);
		stationExtnLabel.setBounds(230,10,115, 25);

		extensionIDLabel = new JLabel();
		extensionIDLabel.setText(stationExtn);
		agentIDPanel.add(extensionIDLabel);
		extensionIDLabel.setBounds(350,10,75, 25);

		getContentPane().add(currAgentStatePanel);
		currAgentStatePanel.setBounds(25, 10, 445, 35);
		currAgentStatePanel.setBorder(lineBorder);

		getContentPane().add(agentStatePanel);
		agentStatePanel.setBounds(25, 55, 445, 115);
		agentStatePanel.setBorder(lineBorder);
		getContentPane().add(agentIDPanel);
		agentIDPanel.setBounds(25, 175, 445, 35);
		agentIDPanel.setBorder(lineBorder);

		// The information text area
		infoTextArea.setColumns(25);
		infoTextArea.setRows(5);
		infoTextArea.setEditable(false);
		DefaultCaret caret = (DefaultCaret)infoTextArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		mainTextAreaScrollPane.setAutoscrolls(true);
		getContentPane().add(mainTextAreaScrollPane);
		mainTextAreaScrollPane.setBounds(25,220, 445, 220);

		// Logout button
		logoutButton.setText("Logout");
		logoutButton.setMnemonic('l');
		getContentPane().add(logoutButton);
		logoutButton.setBounds(295, 475, 75, 23);
		logoutButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent actionEventParam) {
				onBnClickedLogout();
			}
		});

		// Answer button
		answerButton.setText("Answer");
		answerButton.setMnemonic('a');
		getContentPane().add(answerButton);
		answerButton.setBounds(125, 475, 80, 23);
		answerButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent actionEventParam) {
				onBnClickedAgtAnsCall();
			}
		});
		answerButton.setEnabled(false);

		// Drop button
		dropButton.setText("Drop");
		dropButton.setMnemonic('d');
		getContentPane().add(dropButton);
		dropButton.setBounds(215, 475, 70, 23);
		dropButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent actionEventParam) {
				onBnClickedDisconnectCall();
			}
		});
		dropButton.setEnabled(false);

		java.awt.Dimension screenSize = java.awt.Toolkit.
		getDefaultToolkit().getScreenSize();
		setBounds((screenSize.width - 417) / 2, 
				(screenSize.height - 437) / 2, 1070, 672);
		getContentPane().setForeground(colorObj);
		
		textChatArea = new JTextArea();
		textChatArea.setEditable(false);
		
		
		
		DefaultCaret caret1 = (DefaultCaret)textChatArea.getCaret();
		caret1.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		
		chatTextAreaScrollPane = new JScrollPane(textChatArea);
		chatTextAreaScrollPane.setAutoscrolls(true);
		getContentPane().add(chatTextAreaScrollPane);
		chatTextAreaScrollPane.setBounds(528, 55, 404, 318);
		
		//textChatArea.setBounds(528, 55, 404, 318);

		//JScrollPane sp = new JScrollPane(textChatArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		//getContentPane().add(sp);
		
		
		
		
		nextChat = new Button("Next chat");
		nextChat.setName("nextChat");
		nextChat.setForeground(Color.BLACK);
		nextChat.setFont(new Font("Tahoma", Font.PLAIN, 10));
		nextChat.setBackground(SystemColor.activeCaption);
		nextChat.setBounds(613, 475, 75, 23);
		getContentPane().add(nextChat);
		nextChat.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent actionEventParam) {
				try {
			 
					logger.debug("�������� getMessages..");
					resetChatIdandGetMessages();
				} catch (IOException | JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		
		
		
		sendText = new Button("Send");
		sendText.setBackground(SystemColor.activeCaption);
		sendText.setForeground(Color.BLACK);
		sendText.setBounds(771, 475, 75, 23);
		getContentPane().add(sendText);
		sendText.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent actionEventParam) {
				try {
					sendMessage();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		
		chatAgentTextInput = new JTextArea();
		chatAgentTextInput.setBounds(528, 386, 404, 54);
		getContentPane().add(chatAgentTextInput);
		
		JToggleButton tglbtnNewToggleButton = new JToggleButton("Chat on/off");
		tglbtnNewToggleButton.setBounds(817, 10, 115, 21);
		getContentPane().add(tglbtnNewToggleButton);		
		ItemListener itemListener = new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent itemEvent) {
				
				
				 //CallRestAPI api = new CallRestAPI();
				 int state = itemEvent.getStateChange();
				 if (state == ItemEvent.SELECTED) {
					 
					 readyForChat = true;
					 
					 /*
					 
					  String jsonInputString =  "{ "
					      		+ "\"agentid\":\""+agentID+"\","
					      		+ "\"ready\":\""+"true"+"\"}";
					    try {
							api.doPost(jsonInputString, "updateAgentState");
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					    
					    
	                    System.out.println("Start chatting");
	                    chatChecker =  new ChatChecker();
	                    service = Executors.newFixedThreadPool(1);
	            		service.submit(chatChecker);
	                    */
	                }
	                else {
	                	
	                	 readyForChat = false;
	                	/*
	                	  String jsonInputString =  "{ "
						      		+ "\"agentid\":\""+agentID+"\","
						      		+ "\"ready\":\""+"false"+"\"}";
						    try {
								api.doPost(jsonInputString, "updateAgentState");
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}	
	                	*/
	                System.out.println("Stop chatting");                
	               	chatChecker.stop();
	        		service.shutdown();
	        		
	        		try {
	        		    if (service.awaitTermination(5,TimeUnit.SECONDS)) {
	        		    	System.out.println("task completed");
	        		        
	        		    } 
	        		} catch (InterruptedException e) {
	        			 System.out.println("Forcing shutdown...");
	        			 service.shutdownNow();
	        		}
	        		
	        		
	             }
				
			}
			
		};
		
		tglbtnNewToggleButton.addItemListener(itemListener);
	
		
		
		
		
		
		
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	}

	/**
	 * Called whenever the ChangeState button on the UI is pressed
	 */
	protected void onBnClickedSetAgtState() {
		
		if (!notReadyRadioButton.isSelected() && !readyRadioButton.
				isSelected() && !workNotReadyRadioButton.isSelected()) {
			JOptionPane.showMessageDialog(getContentPane(), "Please select " +
				"the Agent state");
			return;
		}
		
		String str = "";
		if (agentMode == LucentAgent.MODE_NONE) {
			
			// if mode is not auto mode or manual mode
			updateTextArea("Agent mode is not valid.");
			
		} else if (previousAgentState == Agent.UNKNOWN) {
			
			updateTextArea("Agent state is not valid.");
		} else {
		
			try {
				if (readyRadioButton.isSelected()) {
					if (enablePendingCheckBox.isSelected()) {
						userAgentObj.setAgentState(Agent.READY, true);
						str = "READY";
						updateTextArea("Agent State change request to '" + 
								str + "' with pending flag enabled");
					} else {
						str = "READY";
						userAgentObj.setAgentState(Agent.READY, false);
						updateTextArea("Agent State change request to '" + 
								str + "'");
					}
				} else if (notReadyRadioButton.isSelected()) {
					if (enablePendingCheckBox.isSelected()) {
						str = "NOT READY";
						userAgentObj.setAgentState(Agent.NOT_READY, true);
						updateTextArea("Agent State change request to '" + 
								str + "' with pending flag enabled");
					} else {
						str = "NOT READY";
						userAgentObj.setAgentState(Agent.NOT_READY, false);
						updateTextArea("Agent State change request to '" + 
								str + "'");
					}
				} else if (workNotReadyRadioButton.isSelected()) {
					if (enablePendingCheckBox.isSelected()) {
						str = "WORK NOT READY";
						userAgentObj.setAgentState(Agent.WORK_NOT_READY, 
								true);
						updateTextArea("Agent State change request to '" +
							 str + 
							"' with pending flag enabled");
					} else {
						str = "WORK NOT READY";
						userAgentObj.setAgentState(Agent.WORK_NOT_READY, 
								false);
						updateTextArea("Agent State change request to '" + 
								str + "'");
					}
				}
			} catch (TsapiPlatformException e) {
				JOptionPane.showMessageDialog(getContentPane(), 
						"Cannot change the agent state to " + 
						str + " at this time",
						"Message:",JOptionPane.PLAIN_MESSAGE);
			}
		}
	}

	/**
	 * Called when the Drop button on the UI is pressed
	 */
	protected void onBnClickedDisconnectCall() {
		callManagerObj.agentDisconnectCall(this.callId);
	}
	
	
	protected void resetChatIdandGetMessages() throws IOException, JSONException {
		
		
		textChatArea.selectAll();
		textChatArea.replaceSelection("");
		
		
		String pattern = "HH:mm:ss";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		String date = simpleDateFormat.format(new Date());
		CallRestAPI call = new CallRestAPI();
		
		
		String jsonInputString =  "{ "
		      		+ "\"agentid\":\""+agentIdNew+"\"}";
		call.doPost(jsonInputString, "unAssignChatToAgent");
		
		
		
		JSONObject jsonObject = new JSONObject(call.doGet("getMessage",AgentStateUI.chatid));
		String fullMesage = jsonObject.getString("message");
		if(AgentStateUI.chatid.equals(jsonObject.getLong("chatid"))) {
			
			AgentStateUI.chatid =null;
			JSONObject jsonNextObj = new JSONObject(call.doGet("getMessage",null));
			String nextMessage = jsonNextObj.getString("message");
			Long nextChatid = jsonNextObj.getLong("chatid");
			AgentStateUI.chatid = nextChatid;
			for(String message :nextMessage .split(";") ) {
				System.out.println(message);
				textChatArea.append(date+"  "+message+"\n");		
			}
			
		
		}
		else {
			AgentStateUI.chatid = jsonObject.getLong("chatid");
			for(String message :fullMesage .split(";") ) {
				System.out.println(message);
				textChatArea.append(date+"  "+message+"\n");		
			}
			
		}
			
	}
	
	
	protected void sendMessage() throws IOException {
		
	
		 SimpleDateFormat simpleDateFormat;
		 String pattern = "yyyy-MM-dd'T'HH:mm:ss";
		 simpleDateFormat = new SimpleDateFormat(pattern);
		 String date = simpleDateFormat.format(new Date());
		 String channel = "telegram";
		
		  String jsonInputString =  "{ "
		      		+ "\"message\":\""+chatAgentTextInput.getText()+"\","
		      		+ "\"chatid\":\""+chatid+"\","
		      		+ "\"date\":\""+date+"\","
		      		+ "\"channel\":\""+channel+"\"}";
		
		
		CallRestAPI api = new CallRestAPI();
		api.doPost(jsonInputString,"sendMessage");
		
		
		
		
		String pattern1 = "HH:mm:ss";
		simpleDateFormat = new SimpleDateFormat(pattern1);
		String date1 = simpleDateFormat.format(new Date());
		 
		textChatArea.append(date1+"  " +chatAgentTextInput.getText()+"\n");
		chatAgentTextInput.selectAll();
		chatAgentTextInput.replaceSelection("");
		
	}
	

	/**
	 * Called when the Answer button on the UI is pressed
	 */
	protected void onBnClickedAgtAnsCall() {
		
	    callManagerObj.agentAnswerCall(this.callId);
	}

	/**
	 * Called when the Logout button on the UI is pressed
	 */
	protected void onBnClickedLogout() {
		
	  	String json =  "{ "
	      	
	      		+ "\"agentid\":\""+Long.parseLong(agentID)+"\"}";
	      
		
		try {
			onCancel();
			
			CallRestAPI api = new CallRestAPI();
			api.doDelete(json, "removeAgent");
			
		} catch (Exception e) {
			JOptionPane.showMessageDialog(getContentPane(),
					"Cannot logout agent: " + e.getMessage(),
					"Exception:",JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Logout the agent
	 */
	private void onCancel() {
		try {
			userAgentObj.agentLogout();
			CallRestAPI api = new CallRestAPI();
			
			System.out.println("Deleting agent from db:"+agentID);
			api.doDelete(agentID, "removeAgent");
			
			
		} catch (Exception e) {
			JOptionPane.showMessageDialog(getContentPane(),
					"Cannot logout agent: " + e.getMessage(),
					"Exception:",JOptionPane.ERROR_MESSAGE);
		}
	}
	
	/**
	 * The agent has logged out.  Stop monitoring the agent's terminal and state,
	 * close the window and remove the observers.
	 */
	private void agentLoggedOut(){
		userAgentObj.agentStopMonitor();
		detach();
		dispose();
		loginUIObj.agentLogOut();
	}

	/**
	 * detach the observer from the Agent and CallManager
	 */
	private void detach() {
		userAgentObj.deleteObserver(this);
		callManagerObj.deleteObserver(this);
	}

	/**
	 * Method to display message in TextArea
	 * @param msg
	 */
	private void updateTextArea(String msg) {
		
		// Append the string in the text area.
		if (msg != null && msg.length() > 0) {
			this.infoTextArea.append(msg + "\n");
			infoTextArea.setCaretPosition(infoTextArea.getText().length());
			if(logger.isDebugEnabled())
			    logger.debug(msg);
		}
	}


	/**
	 * A method of Observer Interface and is overridden by the 
	 * AgentStateUI Class. This method updates the UI with current
	 * activity information.
	 */
	@Override
	
	public void update(final Observable o, final Object arg) 
	{
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					// The following code is executed on the Swing 
					// Event Dispatch Thread.
					try {

						if(arg instanceof String)
						{
							showError((String) arg);
						}
						else if(o instanceof CallManager && 
								arg instanceof CallControlUpdate)
						{
							CallControlUpdate u = (CallControlUpdate)arg;
							cmUpdate(u.getConnectionState(), u.getCallState(), 
									u.getCallID(), u.getCalledDeviceID(), 
									u.getCallingDeviceID(), u.getThirdDeviceID());
						}
						else if(o instanceof AvayaCallCenterUserAgent && 
								arg instanceof AgentState)
						{
							AgentState u = (AgentState) arg;
							agentUpdate(u);
						}
					} catch(Exception e) {
						String str = "Exception while updating the Agent State: " + 
						e.getMessage();
						showError(str);
					}
				}
			});

		} catch(Exception e) {
			String str = "Exception while updating the Agent State: " + 
			             e.getMessage();
			showError(str);
		}
	}
	
	/**
	 * Display the error from the Call Control to the user
	 * @param error
	 */
	private void showError(String error)
	{
        JOptionPane.showMessageDialog(
        		null, error, "Exception", JOptionPane.ERROR_MESSAGE);
	}
	
	/**
	 * Handle the events that are sent by the Call Manager
	 * @param connectionState
	 * @param callState
	 * @param callID
	 * @param calledDeviceID
	 * @param callingDeviceID
	 * @param additionalDevID
	 */
	private void cmUpdate(ConnectionState connectionState, CallState callState,
			long callID, String calledDeviceID, String callingDeviceID, 
			String additionalDevID) {
			
		long prevCallId = this.callId;
		String msg = "";
		this.callId = callID;

		switch (connectionState) {
		
		case None:
			detach();
			break;

		case Null:
			if(callManagerObj.getNumberOfCalls() <= 1) {
				this.dropButton.setEnabled(false);
			} else
				this.callId = prevCallId;
			this.answerButton.setEnabled(false);
			msg += "Extension " + additionalDevID + 
				" Dropped from Call " + callID ;
			if(callState != null) {
				msg += "\nCall " + callID + 
					" is in " + callState + " state.";
			}
			updateTextArea(msg);
			break;
			
		case Initiate:
			msg += "Outgoing Call " + callID + " from " +
				callingDeviceID + " to " + calledDeviceID;
			msg += "\nCall " + callID + " is in " + callState + " state.";
			msg += "\nExtension "+ callingDeviceID + " " +
				"is Talking in Call " + callID;
			msg += "\nExtension " + calledDeviceID;
			msg += " is Ringing in call " + callID;
			this.answerButton.setEnabled(false);
			this.dropButton.setEnabled(true);
			updateTextArea(msg);
			break;

		case Alerting:
			if(additionalDevID.equals(calledDeviceID)) {
				msg = "Incoming Call " + callID + " from " +
				callingDeviceID + " to " + calledDeviceID;
			} else {
				msg = "Incoming Call " + callID + " from " +
				callingDeviceID + " to " + additionalDevID +
				" alerting at " +	calledDeviceID;
			}
			msg += "\nCall " + callID + " is in " + callState + " state.";
			msg += "\nExtension "+ callingDeviceID + " " +
				"is Talking in Call " + callID;
			msg += "\nExtension " + calledDeviceID;
			msg += " is Ringing in Call " + callID;
			this.answerButton.setEnabled(true);
			this.dropButton.setEnabled(false);
			updateTextArea(msg);
			break;

		case ConfAlerting:
			msg = "Conference Call " + callID + " from " +
				callingDeviceID + " to " + calledDeviceID;
			msg += "\nCall " + callID + " is in " + callState + " state.";
			msg += "\nExtension "+ callingDeviceID + " " +
				"is Talking in Call " + callID;
			msg += "\nExtension "+ additionalDevID + " " +
			"is Talking in Call " + callID;
			msg += "\nExtension " + calledDeviceID;
			msg += " is Ringing in Call " + callID;
			if(calledDeviceID.equals(stationExtn)) {
				this.answerButton.setEnabled(true);
				this.dropButton.setEnabled(false);
			}
			else
			{
				this.answerButton.setEnabled(false);
				this.dropButton.setEnabled(true);
			}
			updateTextArea(msg);
			break;
			
		case Connect:
			if(additionalDevID.equals(stationExtn) || callingDeviceID.equals(stationExtn)) {
				this.answerButton.setEnabled(false);
				this.dropButton.setEnabled(true);
			}
			if(additionalDevID != "") {
				msg = "Call " + callID + " is Answered and in " + callState + " state.";
				msg += "\nExtensions " + callingDeviceID + " " +
					"and " + additionalDevID + " are Talking in Call " + callID;
			}
			updateTextArea(msg);
			break;
			
		case Hold:
			if (callID == 0) 
				break;
			msg += "Extension " + calledDeviceID + "" +
					" put on hold in call " + callID;
			updateTextArea(msg);
			break;
			
		case Conferenced:
			msg += "Conference Call is Established";
			msg += "\nExtensions " + calledDeviceID + ", " +
				callingDeviceID + " and " + additionalDevID + " are " +
				"Conferencing in Call " + callID;
			this.answerButton.setEnabled(false);
			this.dropButton.setEnabled(true);
			updateTextArea(msg);
			break;

		case AddingNewParty:
			msg += "Attempting to conference Extension " + additionalDevID + " to Call " + callID;
			updateTextArea(msg);
			break;
			
		case Transfered:
			msg = "Call " + callID + " is Transferring From Extension " + additionalDevID;
			msg += " to Extension " + callingDeviceID;
			updateTextArea(msg);
			
			break;
			
		case UnHold:
			msg += "Call " + callID + " retrieved ";
			msg += "\nExtension " + calledDeviceID +" is " +
				"Talking in call " + callID;
			if (calledDeviceID.equals(stationExtn)) {
				dropButton.setEnabled(true);
			}
			updateTextArea(msg);
			break;
			
		case ThirdPartyDropped:
			this.callId = prevCallId;
			if (callState == CallState.INVALID) {
				this.answerButton.setEnabled(false);
			}
			msg += "Extension " + additionalDevID + " is " +
				"Dropped from Call " + callID ;
			updateTextArea(msg);
			break;
			
		default:
			// Received an event which is not to be processed.
		}
	}

	/**
	 * A method of Observer Interface and is overridden by the 
	 * AgentStateUI Class. This method update the UI with current
	 * activity information.
	 */
	public void agentUpdate(AgentState agentState) {

		if (agentState.getState() == previousAgentState)
			return;

		previousAgentState = agentState.getState();
		logoutButton.setEnabled(true);
		
		String msg = "AgentState: ";
		switch (previousAgentState) {

		case Agent.LOG_OUT:
			agentLoggedOut();
			break;
			
		case Agent.READY:
			currAgentStateLabel.setText("Current Agent State :    READY");
			msg += agentState;
			updateTextArea(msg);
			dropButton.setEnabled(false);
			this.logoutButton.setEnabled(true);
			break;
			
		case Agent.NOT_READY:
			currAgentStateLabel.setText("Current Agent State :    NOT READY");
			msg += "NOT READY";
			updateTextArea(msg);
			dropButton.setEnabled(false);
			this.logoutButton.setEnabled(true);
			break;

		case Agent.WORK_NOT_READY:
			currAgentStateLabel.setText("Current Agent State :    WORK NOT READY");
			msg += "WORK NOT READY";
			updateTextArea(msg);
			dropButton.setEnabled(false);
			this.logoutButton.setEnabled(true);
			break;
			
		case Agent.BUSY:
			currAgentStateLabel.setText("Current Agent State :    BUSY");
			msg += agentState;
			this.logoutButton.setEnabled(false);
			updateTextArea(msg);
			break;
			
		default:
			// Received an event which is not to be processed.
		}
	}

	/**
	 * WindowAdapter is added to the JFrame for handling window events 
	 */
	private WindowListener win = new WindowAdapter() {
		
		public void windowClosing(WindowEvent event) {
			try{
				if (logoutButton.isEnabled()) {
					onCancel();
				} else {
					JOptionPane.showMessageDialog(getContentPane(),
					 "Error: Cannot close the Agent State GUI." +
					 " Agent is handling a call.");
				}

			} catch (Exception e) {
				JOptionPane.showMessageDialog(getContentPane(),
				 "Exception: Cannot close the Agent State GUI."+
				 " Agent is handling a call.");
			}
		}
	};
	

	
	public void setAgentId() {
		agentIdNew = agentID;
	}
	
	public static String getAgentId() {
		return agentIdNew;
	}
	
	
	public static JTextArea textChatArea;
	private Button sendText;
	private Button nextChat;
	public static JTextArea chatAgentTextInput;
	public static boolean readyForChat;
}
