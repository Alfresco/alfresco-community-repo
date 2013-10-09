/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.rest.api;

import java.util.HashMap;
import java.util.Map;

import org.springframework.extensions.webscripts.Container;
import org.springframework.extensions.webscripts.DeclarativeRegistry;
import org.springframework.extensions.webscripts.Description.FormatStyle;
import org.springframework.extensions.webscripts.Description.RequiredAuthentication;
import org.springframework.extensions.webscripts.Description.RequiredTransaction;
import org.springframework.extensions.webscripts.Description.TransactionCapability;
import org.springframework.extensions.webscripts.DescriptionImpl;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.TransactionParameters;
import org.springframework.extensions.webscripts.WebScript;

public class PublicApiDeclarativeRegistry extends DeclarativeRegistry
{
	private WebScript getNetworksWebScript;
	private WebScript getNetworkWebScript;
	private Container container;

    public void setGetNetworksWebScript(WebScript getNetworksWebScript)
    {
		this.getNetworksWebScript = getNetworksWebScript;
	}

	public void setGetNetworkWebScript(WebScript getNetworkWebScript)
	{
		this.getNetworkWebScript = getNetworkWebScript;
	}

	public void setContainer(Container container)
    {
    	super.setContainer(container);
        this.container = container;
    }
    
	/* (non-Javadoc)
     * @see org.alfresco.web.scripts.Registry#findWebScript(java.lang.String, java.lang.String)
     */
    public Match findWebScript(String method, String uri)
    {
    	if(method.equalsIgnoreCase("get") && uri.equals(PublicApiTenantWebScriptServletRequest.NETWORKS_PATH))
    	{
    		Map<String, String> templateVars = new HashMap<String, String>();
    		templateVars.put("apiScope", "public");
    		templateVars.put("apiVersion", "1");
    		templateVars.put("apiName", "networks");
    		Match match = new Match("", templateVars, "", getNetworksWebScript);
    		return match;
    	}
    	else if(method.equalsIgnoreCase("get") && uri.equals(PublicApiTenantWebScriptServletRequest.NETWORK_PATH))
    	{
    	    Map<String, String> templateVars = new HashMap<String, String>();
            templateVars.put("apiScope", "public");
            templateVars.put("apiVersion", "1");
            templateVars.put("apiName", "network");
    		Match match = new Match("", templateVars, "", getNetworkWebScript);
    		return match;
    	} 
    	else
    	{
    		return super.findWebScript(method, uri);
    	}
    }
    
    private void initWebScript(WebScript webScript, String name)
    {
    	DescriptionImpl serviceDesc = new DescriptionImpl(name, name, name, name);
    	serviceDesc.setRequiredAuthentication(RequiredAuthentication.user);
    	TransactionParameters transactionParameters = new TransactionParameters();
    	transactionParameters.setRequired(RequiredTransaction.required);
    	transactionParameters.setCapability(TransactionCapability.readonly);
    	serviceDesc.setRequiredTransactionParameters(transactionParameters);
    	serviceDesc.setFormatStyle(FormatStyle.argument);
    	serviceDesc.setDefaultFormat("json");
    	serviceDesc.setUris(new String[] { name });
        webScript.init(container, serviceDesc);
    }

    public void reset()
    {
    	super.reset();
    	initWebScript(getNetworksWebScript, "networks");
    	initWebScript(getNetworkWebScript, "network");
    }
}
