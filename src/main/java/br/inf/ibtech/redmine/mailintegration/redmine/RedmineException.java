package br.inf.ibtech.redmine.mailintegration.redmine;

import java.io.InputStream;

import lombok.Getter;
import br.inf.ibtech.redmine.mailintegration.redmine.model.Error;

import com.fasterxml.jackson.databind.ObjectMapper;

@SuppressWarnings("serial")
@Getter
public class RedmineException extends Exception {

	private Error error;

	public RedmineException(InputStream errorStream) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			error = mapper.readValue(errorStream, Error.class);
		} catch (Exception e) {
			createError(e);
		}
	}

	public RedmineException(Exception e) {
		createError(e);
	}

	private void createError(Exception e) {
		error = new Error(e);
	}
	
	@Override
	public String toString() {
		return error.toString();
	}
	
}
