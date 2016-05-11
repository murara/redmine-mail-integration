package br.inf.ibtech.redmine.mailintegration.redmine;

import java.util.ArrayList;
import java.util.List;

import javax.mail.internet.MimeBodyPart;

public class MimeMessageModel {

	private MimeBodyPart textPart;
	
	private MimeBodyPart htmlPart;
	
	private List<MimeBodyPart> attachments = new ArrayList<MimeBodyPart>();

	public void addAttachments(MimeBodyPart part) {
		attachments.add(part);
	}

	public MimeBodyPart getTextPart() {
		return textPart;
	}

	public void setTextPart(MimeBodyPart textPart) {
		this.textPart = textPart;
	}

	public MimeBodyPart getHtmlPart() {
		return htmlPart;
	}

	public void setHtmlPart(MimeBodyPart htmlPart) {
		this.htmlPart = htmlPart;
	}

	public List<MimeBodyPart> getAttachments() {
		return attachments;
	}
	
}
