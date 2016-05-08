package br.inf.ibtech.redmine.mailintegration.redmine.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonRootName;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonRootName(value = "user")
public class User {

	private Integer id;
	private String login;
	private String password;
	private String firstname;
	private String lastname;
	private String mail;
	
	@Override
	public boolean equals(Object obj) {
		return (id == null && obj == null) || (id != null && obj != null && obj instanceof User && id.equals(((User)obj).id));
	}

}
