package br.inf.ibtech.redmine.mailintegration;

import javax.mail.Message;
import javax.mail.MessagingException;

import br.inf.ibtech.redmine.mailintegration.imap.IMAPClient;
import br.inf.ibtech.redmine.mailintegration.redmine.IssueResource;
import br.inf.ibtech.redmine.mailintegration.redmine.RedmineException;
import br.inf.ibtech.redmine.mailintegration.redmine.RequiredEnvVarsException;

public class Main {
	
	private static IMAPClient imapClient;
	private static IssueResource redmineClient;

	public static void main(String[] args){
		try {
			init();
		} catch (RequiredEnvVarsException e) {
			System.out.println(e.getMessage());
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}

	private static void init() throws RequiredEnvVarsException, MessagingException {
		imapClient = new IMAPClient();
		redmineClient = new IssueResource();
		try	{
			imapClient.connect();
			Message[] messages = imapClient.getMessages();
			for (Message message : messages) {
				try {
					redmineClient.processMessage(message);
					imapClient.moveToOKFolder(message);
				} catch (RedmineException e) {
					e.printStackTrace();
					imapClient.moveToFailedFolder(message, e.getMessage());
				}
			}
		}finally{
			imapClient.disconnect();
		}

	}

}
