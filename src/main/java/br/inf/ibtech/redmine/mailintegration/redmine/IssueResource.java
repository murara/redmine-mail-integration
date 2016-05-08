package br.inf.ibtech.redmine.mailintegration.redmine;

import java.io.IOException;
import java.net.HttpURLConnection;

import javax.mail.Message;

import org.apache.commons.io.IOUtils;

import br.inf.ibtech.redmine.mailintegration.redmine.model.Issue;
import br.inf.ibtech.redmine.mailintegration.redmine.model.Upload;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class IssueResource {

	private RedmineAPI api;

	public IssueResource() throws RequiredEnvVarsException {
		api = new RedmineAPI();
	}

	public void processMessage(Message message) throws RedmineException {
		try {
			Issue issue = IssueFactory.createIssue(api.getParams().getProject(), api.getParams().getTracker(), message);
			addAllAttachments(issue);
			createIssue(issue);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RedmineException(e);
		}
	}

	private void createIssue(Issue issue) throws IOException, RedmineException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
		mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
		mapper.setSerializationInclusion(Include.NON_NULL);
		
		HttpURLConnection con = api.getConnection("/issues.json");
		con.setDoOutput(true);
		con.addRequestProperty("Content-Type","application/json");
		con.setRequestMethod("POST");
		mapper.writer().writeValue(con.getOutputStream(), issue);
		con.getOutputStream().flush();
		int responseCode = con.getResponseCode();
		if (responseCode >= 400) throw new RedmineException(con.getErrorStream());
	}

	private void addAllAttachments(Issue issue) {
		if (issue.getUploads() != null) {
			issue.getUploads().forEach(u -> addAttachment(u));
		}
	}

	private boolean addAttachment(Upload u) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
			
			HttpURLConnection con = api.getConnection("/uploads.json");
			con.setDoOutput(true);
			con.setRequestProperty("Content-Type", "application/octet-stream");
			con.setRequestMethod("POST");
			con.getOutputStream().write(u.getData());
			con.getOutputStream().flush();
			int responseCode = con.getResponseCode();
			if (responseCode >= 400) throw new RedmineException(con.getErrorStream());
			
			String response = IOUtils.toString(con.getInputStream(), con.getContentEncoding());
			Upload result = mapper.readValue(response, Upload.class);
			if (result != null) {
				u.setToken(result.getToken());
				u.setData(null);
				return true;
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		return false;
	}

}
