package fr.openwide.core.wicket.gmap.api.directions;

import java.io.Serializable;

public enum GDirectionsStatus implements Serializable {

	OK("OK"),
	NOT_FOUND("NOT_FOUND"),
	ZERO_RESULTS("ZERO_RESULTS"),
	MAX_WAYPOINTS_EXCEEDED("MAX_WAYPOINTS_EXCEEDED"),
	INVALID_REQUEST("INVALID_REQUEST"),
	OVER_QUERY_LIMIT("OVER_QUERY_LIMIT"),
	REQUEST_DENIED("REQUEST_DENIED"),
	UNKNOWN_ERROR("UNKNOWN_ERROR");
	
	private String value;
	
	private GDirectionsStatus(){
	}
	
	private GDirectionsStatus(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}

}