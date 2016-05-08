package br.inf.ibtech.redmine.mailintegration.redmine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

import br.inf.ibtech.redmine.mailintegration.redmine.model.Issue;
import br.inf.ibtech.redmine.mailintegration.redmine.model.Upload;
import br.inf.ibtech.redmine.mailintegration.redmine.model.User;

public class IssueFactory {

	private Message message;
	private String description = "";
	private Map<String,Upload> uploads;

	private IssueFactory(Message message) throws IOException, MessagingException {
		this.message = message;
		this.uploads = new HashMap<String,Upload>();
		extractContent();
	}

	public static Issue createIssue(int projectId, int trackerId, Message message) throws MessagingException, IOException {
		IssueFactory issueFactory = new IssueFactory(message);

		return Issue.builder().
				projectId(projectId).
				trackerId(trackerId).
				statusId(1).
				priorityId(issueFactory.getPriority()).
				subject(message.getSubject()).
				description(issueFactory.description).
				assignedToId(issueFactory.getAuthor()).
				watcherUserIds(issueFactory.getWatcher()).
				isPrivate(false).
				uploads(issueFactory.uploads.values()).
				build();
	}

	private int getPriority()
			throws MessagingException {
		int priority = 2;
		String[] messagePriorities = message.getHeader("X-Priority");
		if (messagePriorities != null && messagePriorities.length > 0) {
			try {
				Integer messagePriority = Integer.valueOf(messagePriorities[0]);
				if (messagePriority > 3) priority = 1;
				if (messagePriority < 3) priority = 4;
			}catch(NumberFormatException e){}
		}
		return priority;
	}

	private void extractContent() throws IOException, MessagingException {
		Object contentObject = message.getContent();
		if(message instanceof MimeMessage) {
			MimeMessage m = (MimeMessage)message;
			contentObject = m.getContent();
			if(contentObject instanceof MimeMultipart)
			{
				MimeBodyPart clearTextPart = null;
				MimeBodyPart htmlTextPart = null;
				MimeMultipart content = (MimeMultipart)contentObject;
				int count = content.getCount();
				for(int i=0; i<count; i++)
				{
					MimeBodyPart part =  (MimeBodyPart) content.getBodyPart(i);
					if(part.isMimeType("text/plain")) {
						clearTextPart = part;
					} else if(part.isMimeType("text/html")){
						htmlTextPart = part;
					} else if(part.getContentType().trim().startsWith("multipart/alternative")){
						MimeMultipart mimeMultipart = (MimeMultipart)part.getContent();
						for (int j = 0; j < mimeMultipart.getCount(); j++) {
							MimeBodyPart partAlternative = (MimeBodyPart) mimeMultipart.getBodyPart(j);
							if (partAlternative.isMimeType("text/plain")) {
								clearTextPart = partAlternative;
							} else if(partAlternative.isMimeType("text/html")){
								htmlTextPart = partAlternative;
							}
						}
					} else {
						addUpload(part);
					}
				}

				if(clearTextPart!=null) {
					description = (String) clearTextPart.getContent();
				}

				if (htmlTextPart!=null) {
					try {
						description = convertHTMLToTextile((String) htmlTextPart.getContent());
					} catch (Exception e) {
						if (description == null) description = (String) htmlTextPart.getContent();
					}
				}

			}else{
				description = (String) contentObject;
			}
		}

		adjustAllImages();
	}

	private void adjustAllImages() {
		uploads.forEach((k,v) -> description = adjustImage(description, k, v));
	}

	private static String adjustImage(String description, String cid, Upload upload) {
		description = description.replaceAll("\"?\\!(.*)cid:"+cid+"!\"?", "!"+upload.getFilename()+"! ");
		return description;
	}
	
	private void addUpload(MimeBodyPart part) throws IOException, MessagingException {
		byte[] data = IOUtils.toByteArray(part.getInputStream());
		String contentType = part.getContentType();
		if (contentType.contains(";")) contentType = contentType.split("[;]")[0];
		String cid = part.getFileName();
		if (part.getContentID() != null) {
			cid = part.getContentID().replaceAll("[<>]", "");
		}
		uploads.put(cid,
				Upload.builder()
				.contentType(contentType)
				.filename(part.getFileName())
				.data(data)
				.build());
	}

	private static String convertHTMLToTextile(String html) throws IOException, MessagingException {
		html = prepareHTML(html);
		BufferedReader br = null;
		InputStreamReader isr = null;
		BufferedReader brError = null;
		InputStreamReader isrError = null;

		OutputStream procIn = null;
		InputStream procOut = null;
		InputStream procError = null;
		try{
			String pandocPath = System.getenv("PANDOC_PATH");
			if (pandocPath == null) pandocPath = "";
			else if (pandocPath.endsWith("/") == false) pandocPath = pandocPath + "/";
			Process process = Runtime.getRuntime().exec(pandocPath+"pandoc -f html -t textile");
			procIn = process.getOutputStream();
			procOut = process.getInputStream();
			procError = process.getErrorStream();

			procIn.write(html.getBytes());
			procIn.flush();
			procIn.close();
			process.waitFor();

			isr = new InputStreamReader(procOut);
			br = new BufferedReader(isr);
			String line = null;
			StringBuffer textile = new StringBuffer();
			while ((line = br.readLine()) != null) {
				textile.append(line).append("\n");
			}

			isrError = new InputStreamReader(procError);
			brError = new BufferedReader(isrError);
			StringBuffer errorMessage = new StringBuffer();
			while ((line = brError.readLine()) != null) {
				errorMessage.append(line).append("\n");
			}
			if (errorMessage.length() > 0) throw new IOException(errorMessage.toString());
			
			String text = removeMissedHtmlTags(textile.toString());

			return text;
		}catch(Exception e) {
			e.printStackTrace();
			return null;
		}finally{
			if (br != null) br.close();
			if (isr != null) isr.close();

			if (brError != null) brError.close();
			if (isrError != null) isrError.close();

			if (procIn != null) procIn.close();
			if (procOut != null) procOut.close();
			if (procError != null) procError.close();
		}

	}

	private static String removeMissedHtmlTags(String textile) {
		Document doc = Jsoup.parse(textile.toString());
		doc.outputSettings(new Document.OutputSettings().prettyPrint(false));
		String text = Jsoup.clean(doc.html(), "", Whitelist.none(),new Document.OutputSettings().prettyPrint(false));
		return text;
	}
	
	private static String prepareHTML(String html) {
		Document doc = Jsoup.parseBodyFragment(html);
		Elements el = doc.getAllElements();
		for (Element e : el) {
			if (e.tagName().trim().equals("img")) {
			    Attributes at = e.attributes();
			    for (Attribute a : at) {
			    	if (a.getKey().trim().equals("alt")) {
			    		e.removeAttr(a.getKey());
			    	}
			    }
			}
		}
		return doc.html();
	}

	private Integer getAuthor() {
		try {
			if (message.getFrom().length == 0) return null;
			InternetAddress email = (InternetAddress)message.getFrom()[0];
			UserResource.getInstance();
			User user = UserResource.getByEmail(email);
			if (user == null) return null;
			return user.getId();
		}catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private Integer[] getWatcher() {
		try {
			List<Integer> users = new ArrayList<Integer>();
			for (Address recipient: message.getAllRecipients()){
				User user = UserResource.getByEmail((InternetAddress) recipient);
				if (user != null) users.add(user.getId());
			}
			if (users.size() == 0) return null;

			return users.toArray(new Integer[users.size()]);
		}catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}