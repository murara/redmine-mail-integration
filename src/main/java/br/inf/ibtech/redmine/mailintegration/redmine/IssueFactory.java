package br.inf.ibtech.redmine.mailintegration.redmine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.MimetypesFileTypeMap;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import net.freeutils.tnef.Attachment;
import net.freeutils.tnef.CompressedRTFInputStream;
import net.freeutils.tnef.MAPIProp;
import net.freeutils.tnef.RawInputStream;
import net.freeutils.tnef.TNEFInputStream;
import net.freeutils.tnef.mime.RawDataSource;

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
	private List<String> wasteImages;

	private IssueFactory(Message message) throws IOException, MessagingException {
		this.message = message;
		this.uploads = new HashMap<String,Upload>();
		this.wasteImages = new ArrayList<String>();
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
				MimeMessageModel mimeMessageModel = splitBodyParts((MimeMultipart)contentObject, new MimeMessageModel());
				
				if(mimeMessageModel.getTextPart()!=null) {
					description = (String) mimeMessageModel.getTextPart().getContent();
				}
				
				if(mimeMessageModel.getRtfPart()!=null) {
					RawInputStream ris = (RawInputStream) mimeMessageModel.getRtfPart().getContent();
					description = new String(CompressedRTFInputStream.decompressRTF(ris.toByteArray()));
				}

				if (mimeMessageModel.getHtmlPart()!=null) {
					try {
						description = convertHTMLToTextile((String) mimeMessageModel.getHtmlPart().getContent());
					} catch (Exception e) {
						if (description == null) description = (String) mimeMessageModel.getHtmlPart().getContent();
					}
				}
				
				for (MimeBodyPart attachment : mimeMessageModel.getAttachments()) {
					addUpload(attachment);
				}
			}else{
				description = (String) contentObject;
			}
		}

		adjustAllImages();
	}
	
	private MimeMessageModel splitBodyParts(MimeMultipart contentObject, MimeMessageModel mimeMessageModel) throws MessagingException, IOException {
		MimeMultipart content = (MimeMultipart)contentObject;
		int count = content.getCount();
		for(int i=0; i<count; i++)
		{
			MimeBodyPart part =  (MimeBodyPart) content.getBodyPart(i);
			if(part.isMimeType("text/plain")) {
				mimeMessageModel.setTextPart(part);
			} else if(part.isMimeType("text/html")){
				mimeMessageModel.setHtmlPart(part);
			} else if(part.isMimeType("application/ms-tnef")) {
				net.freeutils.tnef.Message tnefMessage = new net.freeutils.tnef.Message(new TNEFInputStream(part.getInputStream()));
				mimeMessageModel = splitTNEFBodyParts(tnefMessage, mimeMessageModel);
			} else if(part.isMimeType("multipart/alternative") || part.isMimeType("multipart/related")){
				splitBodyParts((MimeMultipart)part.getContent(), mimeMessageModel);
			} else {
				mimeMessageModel.addAttachments(part);
			}
		}
		return mimeMessageModel;
	}

	private MimeMessageModel splitTNEFBodyParts(net.freeutils.tnef.Message tnefMessage, MimeMessageModel mimeMessageModel) throws MessagingException, IOException {
		List<Attachment> attachments = tnefMessage.getAttachments();
		MimeBodyPart part = new MimeBodyPart();
		for (Attachment attachment : attachments) {
            String filename = attachment.getFilename();
            if (filename != null)
                part.setFileName(filename);
            String mimeType = null;
            if (attachment.getMAPIProps() != null)
                mimeType = (String)attachment.getMAPIProps().getPropValue(MAPIProp.PR_ATTACH_MIME_TAG);
            if (mimeType == null && filename != null)
                mimeType = MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(filename);
            if (mimeType == null)
                mimeType = "application/octet-stream";
            DataSource ds = new RawDataSource(attachment.getRawData(), mimeType, filename);
            part.setDataHandler(new DataHandler(ds));
            mimeMessageModel.addAttachments(part);
		}
		return mimeMessageModel;
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
		String cid = (part.getContentID() == null) ? part.getFileName() : part.getContentID().replaceAll("[<>]", "");
		String filename = (part.getFileName() != null) ? part.getFileName() : cid;
		if (filename.contains("@")) {
			String extension = "." + (contentType.contains("/") ? contentType.split("[/]")[1] : contentType);
			filename = filename.split("[@]")[0] + extension;
		}
		
		for (String wasteImage : wasteImages) {
			if (wasteImage.toLowerCase().indexOf(cid.toLowerCase()) >= 0) {
//				System.out.println("Waste: "+cid);
				return;
			}
		}
		
//		System.out.println("Good: "+cid);
		
		filename = removerAcentos(filename).replace(" ", "_").toLowerCase();
		
		uploads.put(cid,
				Upload.builder()
				.contentType(contentType)
				.filename(filename)
				.data(data)
				.build());
	}

	private String convertHTMLToTextile(String html) throws IOException, MessagingException {
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
	
	private String prepareHTML(String html) {
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

		el = doc.select("a:has(img)");
		for (Element e : el) {
			Elements imgElements = e.select("img");
			for (Element imgElement : imgElements) {
				String src = imgElement.attr("src");
				if (src == null) continue;
				src = src.replace("cid:","");
				wasteImages.add(src);
			}
			e.remove();
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
	
	public static String removerAcentos(String str) {
	    return Normalizer.normalize(str, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
	}

}