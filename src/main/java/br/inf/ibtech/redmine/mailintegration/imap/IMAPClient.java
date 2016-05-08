package br.inf.ibtech.redmine.mailintegration.imap;

import java.util.Properties;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

import br.inf.ibtech.redmine.mailintegration.redmine.RequiredEnvVarsException;

import com.sun.mail.imap.IMAPFolder;

public class IMAPClient {

	private IMAPClientParams params;
	private Store store;
	private Folder inboxFolder;
	private Folder okFolder;
	private Folder failedFolder;
	private Session session;

	public IMAPClient() throws RequiredEnvVarsException {
		this.params = IMAPClientParamsFactory.create();
	}

	public void connect() throws MessagingException {
		Properties props = System.getProperties();
		props.setProperty("mail.store.protocol", "imaps");
		session = Session.getDefaultInstance(props, null);
		store = session.getStore("imaps");
		store.connect(params.getHostname(), params.getUsername(), params.getPassword());
		checkControlFolders();
	}
	
	public void checkControlFolders() throws MessagingException {
		okFolder = store.getDefaultFolder().getFolder(params.getFolderOK());
		if (okFolder.exists() == false) okFolder.create(Folder.HOLDS_MESSAGES);
		okFolder.setSubscribed(true);
		
		failedFolder = store.getDefaultFolder().getFolder(params.getFolderFAILED());
		if (failedFolder.exists() == false) failedFolder.create(Folder.HOLDS_MESSAGES);
		failedFolder.setSubscribed(true);
	}

	public Message[] getMessages() throws MessagingException {
		inboxFolder = (IMAPFolder) store.getFolder(params.getDefaultFolder());
		if (!inboxFolder.isOpen()) inboxFolder.open(Folder.READ_WRITE);
		return inboxFolder.getMessages();
	}
	
	public void closeFolder(Folder folder, boolean doExpunge) {
		if (folder != null && folder.isOpen()) {
			try {
				if (doExpunge) inboxFolder.expunge();
				folder.close(true);
			} catch (MessagingException e) {
				e.printStackTrace();
			}
		}
	}

	public void disconnect() {
		closeFolder(okFolder, false);
		closeFolder(failedFolder, false);
		closeFolder(inboxFolder, true);
		if (store != null && store.isConnected()) {
			try {
				store.close();
			} catch (MessagingException e) {
				e.printStackTrace();
			}
		}
	}

	public void moveToOKFolder(Message message) throws MessagingException {
		Message[] messages = new Message[] {message};
		inboxFolder.copyMessages(messages, okFolder);
		inboxFolder.setFlags(messages, new Flags(Flags.Flag.DELETED), true);
	}

	public void moveToFailedFolder(Message message, String reason) throws MessagingException {
		System.out.print(reason);
//		message.addHeader("X-Redmine-Error", reason);
		Message[] messages = new Message[] {message};
		inboxFolder.copyMessages(messages, failedFolder);
		inboxFolder.setFlags(messages, new Flags(Flags.Flag.DELETED), true);
	}

}
