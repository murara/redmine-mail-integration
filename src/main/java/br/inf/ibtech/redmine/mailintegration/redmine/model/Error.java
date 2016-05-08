package br.inf.ibtech.redmine.mailintegration.redmine.model;

import java.util.LinkedList;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown=true)
public class Error {

	private String[] errors;

	public Error(Exception e) {
		List<String> messages = new LinkedList<String>();
		messages.add(e.getMessage());
		for (StackTraceElement ste : e.getStackTrace()) {
			messages.add(ste.toString());
		}
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		if (errors == null) return null;
		for (String error : errors) {
			sb.append(error);
			sb.append(" ");
		}
		return sb.toString();
	}
	
}
