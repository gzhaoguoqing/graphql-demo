package com.bes.graphql.bo;

public class Ext {
	
	private String userId;
	
	private String address;
	
	public Ext(String userId, String address) {
		this.userId = userId;
		this.address = address;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}
	
}
