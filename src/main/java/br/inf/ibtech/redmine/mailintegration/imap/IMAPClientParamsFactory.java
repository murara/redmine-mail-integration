package br.inf.ibtech.redmine.mailintegration.imap;

import br.inf.ibtech.redmine.mailintegration.redmine.RequiredEnvVarsException;

public class IMAPClientParamsFactory {

	private static final int DEFAULT_IMAP_PORT = 143;
	
	private static final String DEFAULTFOLDER = "INBOX";
	private static final String OK = "OK";
	private static final String FAILED = "FAILED";

	private static final String IMAP_HOSTNAME = "IMAP_HOSTNAME";
	private static final String IMAP_PORT = "IMAP_PORT";
	private static final String IMAP_USERNAME = "IMAP_USERNAME";
	private static final String IMAP_PASSWORD = "IMAP_PASSWORD";
	private static final String IMAP_DEFAULTFOLDER = "IMAP_DEFAULTFOLDER";

	public static IMAPClientParams create() throws RequiredEnvVarsException {
		String hostname = System.getenv(IMAP_HOSTNAME);
		String port = System.getenv(IMAP_PORT);
		String username = System.getenv(IMAP_USERNAME);
		String password = System.getenv(IMAP_PASSWORD);
		String folder = System.getenv(IMAP_DEFAULTFOLDER);
		folder = folder == null ? DEFAULTFOLDER : folder; 

		if (hostname == null || username == null || password == null) {
			throw new RequiredEnvVarsException("REQUIRED ENVIRONMENT VARIABLES:\n"
					+ "- "+IMAP_HOSTNAME+"\n"
					+ "- "+IMAP_USERNAME+"\n"
					+ "- "+IMAP_PASSWORD+"\n");
		}

		return IMAPClientParams.builder()
				.hostname(hostname)
				.port(port != null ? Integer.valueOf(port) : DEFAULT_IMAP_PORT)
				.username(username)
				.password(password)
				.defaultFolder(folder)
				.folderOK(folder+"."+OK)
				.folderFAILED(folder+"."+FAILED)
				.build();
	}

}
