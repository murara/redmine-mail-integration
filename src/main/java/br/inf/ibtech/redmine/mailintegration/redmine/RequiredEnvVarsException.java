package br.inf.ibtech.redmine.mailintegration.redmine;

@SuppressWarnings("serial")
public class RequiredEnvVarsException extends Exception {

	public RequiredEnvVarsException(String message) {
		super(message);
	}

}
