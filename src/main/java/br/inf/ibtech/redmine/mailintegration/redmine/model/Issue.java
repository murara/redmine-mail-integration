package br.inf.ibtech.redmine.mailintegration.redmine.model;

import java.util.Collection;

import lombok.Builder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

@Builder
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonRootName(value = "issue")
public class Issue {
	
	@JsonIgnore
	private Integer projectId;
	
	@JsonIgnore
	private Integer trackerId;
	
	@JsonIgnore
	private Integer statusId;
	
	@JsonIgnore
	private Integer priorityId;
	
	private String subject;
	
	private String description;
		
	@JsonIgnore
	private Integer assignedToId;
	
	@JsonIgnore
	private Integer[] watcherUserIds;
	
	@JsonIgnore
	private boolean isPrivate = false;
	
	private Collection<Upload> uploads;

	@JsonProperty("project_id")
	public Integer getProjectId() {
		return projectId;
	}

	@JsonProperty("project_id")
	public void setProjectId(Integer projectId) {
		this.projectId = projectId;
	}

	@JsonProperty("tracker_id")
	public Integer getTrackerId() {
		return trackerId;
	}

	@JsonProperty("tracker_id")
	public void setTrackerId(Integer trackerId) {
		this.trackerId = trackerId;
	}

	@JsonProperty("status_id")
	public Integer getStatusId() {
		return statusId;
	}
	
	@JsonProperty("status_id")
	public void setStatusId(Integer statusId) {
		this.statusId = statusId;
	}

	@JsonProperty("priority_id")
	public Integer getPriorityId() {
		return priorityId;
	}

	@JsonProperty("priority_id")
	public void setPriorityId(Integer priorityId) {
		this.priorityId = priorityId;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@JsonProperty("assigned_to_id")
	public Integer getAssignedToId() {
		return assignedToId;
	}

	@JsonProperty("assigned_to_id")
	public void setAssignedToId(Integer assignedToId) {
		this.assignedToId = assignedToId;
	}

	@JsonProperty("watcher_user_ids")
	public Integer[] getWatcherUserIds() {
		return watcherUserIds;
	}

	@JsonProperty("watcher_user_ids")
	public void setWatcherUserIds(Integer[] watcherUserIds) {
		this.watcherUserIds = watcherUserIds;
	}

	@JsonProperty("is_private")
	public boolean isPrivate() {
		return isPrivate;
	}

	@JsonProperty("is_private")
	public void setPrivate(boolean isPrivate) {
		this.isPrivate = isPrivate;
	}

	public Collection<Upload> getUploads() {
		return uploads;
	}

	public void setUploads(Collection<Upload> uploads) {
		this.uploads = uploads;
	}

}
