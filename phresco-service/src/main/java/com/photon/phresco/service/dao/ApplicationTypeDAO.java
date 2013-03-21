package com.photon.phresco.service.dao;

import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ApplicationTypeDAO extends CustomerBaseDAO {
	
	private static final long serialVersionUID = 1L;

	private List<String> techGroupIds;
	
	public ApplicationTypeDAO() {
    }
	
	public List<String> getTechGroupIds() {
		return techGroupIds;
	}

	public void setTechGroupIds(List<String> techGroupIds) {
		this.techGroupIds = techGroupIds;
	}
}
