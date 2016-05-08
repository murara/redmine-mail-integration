package br.inf.ibtech.redmine.mailintegration.redmine;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class RedmineAPI {
	
	private RedmineParams params;

	public RedmineAPI() throws RequiredEnvVarsException {
		params = RedmineParamsFactory.create();
	}
	
	public HttpURLConnection getConnection(String url) throws MalformedURLException, IOException {
		HttpURLConnection con = (HttpURLConnection) new URL(params.getHostname() + url).openConnection();
		con.setRequestProperty("X-Redmine-API-Key", params.getApiKey());
		con.setConnectTimeout(600000);
		return con;
	}
	
	public RedmineParams getParams() {
		return params;
	}
	
}
