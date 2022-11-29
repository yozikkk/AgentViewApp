/**
 * The application worker thread.  The duty of this thread is to wait for an 
 * Event Transaction to be available in the application event queue. Once a
 * Transaction has been put in the queue by the JTAPI Client thread 
 * (see: CallControlTerminalConnectionProxy), the WorkerThread receives a 
 * notification, removes the Transaction from the queue and processes it. 
 */
package com.avaya.aes.agenttest.callcontrol;

import java.util.Queue;

public class WorkerThread extends Thread{

	private CallManager callManager;
	private Queue<EventTransaction> transq;
	
	public WorkerThread(CallManager callManager, Queue<EventTransaction> transq)
	{
		super("Worker");
		this.callManager = callManager;
		this.transq = transq;
	}
	public void run() {
		EventTransaction transaction = null;
		while(true) {
			// Wait until able to get a lock on the transaction queue
			synchronized(transq) {

				// This thread gets transactions, one at a time, from the  
				// queue.  If the queue is empty, sleep until a transaction
				// is added (see: CallManager.queueTransaction()).
				while((transaction = transq.poll()) == null) {
					try {
						transq.wait();
					} catch(InterruptedException e) {
						// In the case of this exception the application 
						// needs to continue to monitor the event queue. 
					}
				}
			}

			// At this point the application has at least one transaction to process. 
			while(transaction.hasNext())
			{
				// Process each event in the transaction, one at a time, in the
				// order they were queued.
				handleCallEvent(transaction.next());
			}
		}
	}

	/**
	 * Execute the Event handler. 
	 * @param event a call event to process
	 */
	private void handleCallEvent(final EventHandler event) {
		try {
			event.execute(callManager);
		} catch(Exception e) {
			String str = "Exception while " +
			"handling the Call Events :" + e.getMessage();
			callManager.displayMessage(str);
		}
	}
}
