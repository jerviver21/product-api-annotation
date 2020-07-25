package com.webflux.model;

public class ProductEvent {
	
	private Long eventId; 
	private String eventType; 
	
	public ProductEvent(Long id, String type) {
		this.setEventId(id);
		this.setEventType(type);
	}

	public Long getEventId() {
		return eventId;
	}

	public void setEventId(Long eventId) {
		this.eventId = eventId;
	}

	public String getEventType() {
		return eventType;
	}

	public void setEventType(String eventType) {
		this.eventType = eventType;
	}
	

	
}
