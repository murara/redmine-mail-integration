package br.inf.ibtech.redmine.mailintegration.imap;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class IMAPClientParams {
	private String hostname;
	private int port;
	private String username;
	private String password;
	private String defaultFolder;
	private String folderOK;
	private String folderFAILED;
}