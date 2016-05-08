package br.inf.ibtech.redmine.mailintegration.redmine;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class RedmineParams {
	private String hostname;
	private String apiKey;
	private Integer project;
	private Integer tracker;
}