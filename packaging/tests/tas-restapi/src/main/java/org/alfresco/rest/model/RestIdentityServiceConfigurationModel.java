/*
 * Copyright 2020 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */

package org.alfresco.rest.model;

import org.alfresco.utility.model.TestModel;

public class RestIdentityServiceConfigurationModel extends TestModel{

	public RestIdentityServiceConfigurationModel() {
		
	}
	
	private String authenticationChain;
	
	private String authenticationEnabled;
	
	private String enableBasicAuth;
	
	private String authServerUrl;
	
	private String realm;
	
	private String resource;
	
	private String publicClient;
	
	private String sslRequired;
	
	private String enablePkce;
	
	private String credentialsSecret;
	
	private String credentialsProvider;

	public String getAuthenticationChain() {
		return authenticationChain;
	}

	public void setAuthenticationChain(String authenticationChain) {
		this.authenticationChain = authenticationChain;
	}

	public String getAuthenticationEnabled() {
		return authenticationEnabled;
	}

	public void setAuthenticationEnabled(String authenticationEnabled) {
		this.authenticationEnabled = authenticationEnabled;
	}

	public String getEnableBasicAuth() {
		return enableBasicAuth;
	}

	public void setEnableBasicAuth(String enableBasicAuth) {
		this.enableBasicAuth = enableBasicAuth;
	}

	public String getAuthServerUrl() {
		return authServerUrl;
	}

	public void setAuthServerUrl(String authServerUrl) {
		this.authServerUrl = authServerUrl;
	}

	public String getRealm() {
		return realm;
	}

	public void setRealm(String realm) {
		this.realm = realm;
	}

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	public String getPublicClient() {
		return publicClient;
	}

	public void setPublicClient(String publicClient) {
		this.publicClient = publicClient;
	}

	public String getSslRequired() {
		return sslRequired;
	}

	public void setSslRequired(String sslRequired) {
		this.sslRequired = sslRequired;
	}

	public String getEnablePkce() {
		return enablePkce;
	}

	public void setEnablePkce(String enablePkce) {
		this.enablePkce = enablePkce;
	}

	public String getCredentialsSecret() {
		return credentialsSecret;
	}

	public void setCredentialsSecret(String credentialsSecret) {
		this.credentialsSecret = credentialsSecret;
	}

	public String getCredentialsProvider() {
		return credentialsProvider;
	}

	public void setCredentialsProvider(String credentialsProvider) {
		this.credentialsProvider = credentialsProvider;
	}
}