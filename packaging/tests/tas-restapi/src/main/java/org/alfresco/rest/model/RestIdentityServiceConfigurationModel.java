/*-
 * #%L
 * alfresco-tas-restapi
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
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
