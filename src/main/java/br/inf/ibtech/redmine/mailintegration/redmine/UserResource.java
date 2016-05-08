package br.inf.ibtech.redmine.mailintegration.redmine;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.mail.internet.InternetAddress;

import org.apache.commons.io.IOUtils;

import br.inf.ibtech.redmine.mailintegration.redmine.model.User;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UserResource {
	
	private RedmineAPI api;
	
	private static UserResource resource;
	private List<User> users;
	
	private UserResource() throws MalformedURLException, IOException, RedmineException, RequiredEnvVarsException {
		api = new RedmineAPI();
		populateUsers();
	}

	public static UserResource getInstance() throws MalformedURLException, IOException, RedmineException, RequiredEnvVarsException {
		if (resource == null) resource = new UserResource();
		return resource;
	}
	
	private void populateUsers() throws MalformedURLException, IOException, RedmineException {
		ObjectMapper mapper = new ObjectMapper();
		
		HttpURLConnection con = api.getConnection("/users.json?limit=10000");
		con.setRequestMethod("GET");
		int responseCode = con.getResponseCode();
		if (responseCode >= 400) throw new RedmineException(con.getErrorStream());
		String json = IOUtils.toString(con.getInputStream(), con.getContentEncoding());
		JsonNode tree = mapper.readTree(json);
		JsonNode usersNode = tree.get("users");
		users = Arrays.asList(mapper.treeToValue(usersNode, User[].class));
	}

	public static User getByEmail(InternetAddress address) throws MalformedURLException, IOException, RedmineException, RequiredEnvVarsException {
		List<User> users = getInstance().users;
		if (users == null) return null;
		
		Optional<User> user = users.stream()
			.filter(u -> u.getMail().equals(address.getAddress())).findFirst();
		return user.orElse(null);
	}

}
