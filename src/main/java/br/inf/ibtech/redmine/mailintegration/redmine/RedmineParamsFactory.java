package br.inf.ibtech.redmine.mailintegration.redmine;

public class RedmineParamsFactory {

	private static final String REDMINE_HOSTNAME = "REDMINE_HOSTNAME";
	private static final String REDMINE_APIKEY = "REDMINE_APIKEY";
	private static final String REDMINE_PROJECT_ID = "REDMINE_PROJECT_ID";
	private static final String REDMINE_TRACKER_ID = "REDMINE_TRACKER_ID";
	
	public static RedmineParams create() throws RequiredEnvVarsException {
		String hostname = System.getenv(REDMINE_HOSTNAME);
		String apiKey = System.getenv(REDMINE_APIKEY);
		String project = System.getenv(REDMINE_PROJECT_ID);
		String tracker = System.getenv(REDMINE_TRACKER_ID);

		if (hostname == null || REDMINE_TRACKER_ID == null || project == null || tracker == null) {
			throw new RequiredEnvVarsException("REQUIRED ENVIRONMENT VARIABLES:\n"
					+ "- "+REDMINE_HOSTNAME+"\n"
					+ "- "+REDMINE_APIKEY+"\n"
					+ "- "+REDMINE_PROJECT_ID+"\n"
					+ "- "+REDMINE_TRACKER_ID+"\n");
		}

		if (project.endsWith("/")) {
			project = project.substring(0, project.length()-1);
		}
		
		return RedmineParams.builder()
				.hostname(hostname)
				.apiKey(apiKey)
				.project(Integer.valueOf(project))
				.tracker(Integer.valueOf(tracker))
				.build();
	}

}
