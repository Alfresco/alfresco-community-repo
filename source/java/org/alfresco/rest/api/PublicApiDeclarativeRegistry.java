/*
 * Copyright (C) 2005-2016 Alfresco Software Limited.
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

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import org.alfresco.rest.framework.Api;
import org.alfresco.rest.framework.core.ResourceLocator;
import org.alfresco.rest.framework.core.ResourceWithMetadata;
import org.alfresco.rest.framework.core.exceptions.DeletedResourceException;
import org.alfresco.rest.framework.core.exceptions.UnsupportedResourceOperationException;
import org.alfresco.rest.framework.resource.actions.interfaces.BinaryResourceAction;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceBinaryAction;
import org.alfresco.rest.framework.resource.actions.interfaces.ResourceAction;
import org.alfresco.rest.framework.resource.content.BinaryResource;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.apache.commons.lang.StringUtils;
import org.springframework.extensions.webscripts.ArgumentTypeDescription;
import org.springframework.extensions.webscripts.Container;
import org.springframework.extensions.webscripts.DeclarativeRegistry;
import org.springframework.extensions.webscripts.Description;
import org.springframework.extensions.webscripts.Description.FormatStyle;
import org.springframework.extensions.webscripts.Description.RequiredAuthentication;
import org.springframework.extensions.webscripts.Description.RequiredTransaction;
import org.springframework.extensions.webscripts.Description.TransactionCapability;
import org.springframework.extensions.webscripts.DescriptionImpl;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.NegotiatedFormat;
import org.springframework.extensions.webscripts.Path;
import org.springframework.extensions.webscripts.TransactionParameters;
import org.springframework.extensions.webscripts.TypeDescription;
import org.springframework.extensions.webscripts.URLModelFactory;
import org.springframework.extensions.webscripts.WebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.http.HttpMethod;

/**
 *
 * @author steveglover
 * @author janv
 * @since PublicApi1.0
 */
public class PublicApiDeclarativeRegistry extends DeclarativeRegistry
{
	private WebScript getNetworksWebScript;
	private WebScript getNetworkWebScript;
	private Container container;

    private ResourceLocator locator;

    public void setLocator(ResourceLocator locator)
    {
        this.locator = locator;
    }

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
        Match match = null;

        HttpMethod httpMethod = HttpMethod.valueOf(method);
        if (httpMethod.equals(HttpMethod.GET))
        {
            if (uri.equals(PublicApiTenantWebScriptServletRequest.NETWORKS_PATH))
            {
                Map<String, String> templateVars = new HashMap<String, String>();
                templateVars.put("apiScope", "public");
                templateVars.put("apiVersion", "1");
                templateVars.put("apiName", "networks");
                match = new Match("", templateVars, "", getNetworksWebScript);
            }
            else if (uri.equals(PublicApiTenantWebScriptServletRequest.NETWORK_PATH))
            {
                Map<String, String> templateVars = new HashMap<String, String>();
                templateVars.put("apiScope", "public");
                templateVars.put("apiVersion", "1");
                templateVars.put("apiName", "network");
                match = new Match("", templateVars, "", getNetworkWebScript);
            }
            else
            {
                // TODO - review (experimental)
                match = super.findWebScript(method, uri);

                Map<String, String> templateVars = match.getTemplateVars();

                if (templateVars.get("apiName") != null)
                {
                    // NOTE: noAuth currently only exposed for GET
                    Api api = determineApi(templateVars);

                    // TODO can we avoid locating resource more than once ?
                    ResourceWithMetadata rwm = locator.locateResource(api, templateVars, HttpMethod.valueOf(method));

                    Class resAction = null;

                    switch (rwm.getMetaData().getType())
                    {
                        case ENTITY:
                            // TODO check params for entity id (for now - assume there is)
                            if (EntityResourceAction.ReadById.class.isAssignableFrom(rwm.getResource().getClass()))
                            {
                                resAction = EntityResourceAction.ReadById.class;
                            }
                            break;
                        case PROPERTY:
                            // TODO check params for entity id (for now - assume there is)
                            if (BinaryResourceAction.Read.class.isAssignableFrom(rwm.getResource().getClass()))
                            {
                                resAction = BinaryResourceAction.Read.class;
                            }
                            else if (RelationshipResourceBinaryAction.Read.class.isAssignableFrom(rwm.getResource().getClass()))
                            {
                                resAction = RelationshipResourceBinaryAction.Read.class;
                            }
                            break;
                        default:
                            break;
                    }

                    final boolean noAuth = (resAction != null && rwm.getMetaData().isNoAuth(resAction));

                    if (noAuth)
                    {
                        final WebScript webScript = match.getWebScript();

                        // TODO is there a better way (to dynamically override "requiredAuthentication") or handle noAuth check earlier ?
                        WebScript noAuthWebScriptWrapper = new WebScript()
                        {
                            @Override
                            public void init(Container container, Description description)
                            {
                                webScript.init(container, description);
                            }

                            @Override
                            public Description getDescription()
                            {
                                final Description d = webScript.getDescription();
                                return new Description()
                                {
                                    @Override
                                    public String getStorePath()
                                    {
                                        return d.getStorePath();
                                    }

                                    @Override
                                    public String getScriptPath()
                                    {
                                        return d.getScriptPath();
                                    }

                                    @Override
                                    public Path getPackage()
                                    {
                                        return d.getPackage();
                                    }

                                    @Override
                                    public String getDescPath()
                                    {
                                        return d.getDescPath();
                                    }

                                    @Override
                                    public InputStream getDescDocument() throws IOException
                                    {
                                        return d.getDescDocument();
                                    }

                                    @Override
                                    public String getKind()
                                    {
                                        return d.getKind();
                                    }

                                    @Override
                                    public Set<String> getFamilys()
                                    {
                                        return d.getFamilys();
                                    }

                                    @Override
                                    public RequiredAuthentication getRequiredAuthentication()
                                    {
                                        return RequiredAuthentication.none;
                                    }

                                    @Override
                                    public String getRunAs()
                                    {
                                        return d.getRunAs();
                                    }

                                    @Override
                                    public RequiredTransaction getRequiredTransaction()
                                    {
                                        return d.getRequiredTransaction();
                                    }

                                    @Override
                                    public RequiredTransactionParameters getRequiredTransactionParameters()
                                    {
                                        return d.getRequiredTransactionParameters();
                                    }

                                    @Override
                                    public RequiredCache getRequiredCache()
                                    {
                                        return d.getRequiredCache();
                                    }

                                    @Override
                                    public String getMethod()
                                    {
                                        return d.getMethod();
                                    }

                                    @Override
                                    public String[] getURIs()
                                    {
                                        return d.getURIs();
                                    }

                                    @Override
                                    public FormatStyle getFormatStyle()
                                    {
                                        return d.getFormatStyle();
                                    }

                                    @Override
                                    public String getDefaultFormat()
                                    {
                                        return d.getDefaultFormat();
                                    }

                                    @Override
                                    public NegotiatedFormat[] getNegotiatedFormats()
                                    {
                                        return d.getNegotiatedFormats();
                                    }

                                    @Override
                                    public Map<String, Serializable> getExtensions()
                                    {
                                        return d.getExtensions();
                                    }

                                    @Override
                                    public Lifecycle getLifecycle()
                                    {
                                        return d.getLifecycle();
                                    }

                                    @Override
                                    public boolean getMultipartProcessing()
                                    {
                                        return d.getMultipartProcessing();
                                    }

                                    @Override
                                    public void setMultipartProcessing(boolean b)
                                    {
                                        d.setMultipartProcessing(b);
                                    }

                                    @Override
                                    public ArgumentTypeDescription[] getArguments()
                                    {
                                        return d.getArguments();
                                    }

                                    @Override
                                    public TypeDescription[] getRequestTypes()
                                    {
                                        return d.getRequestTypes();
                                    }

                                    @Override
                                    public TypeDescription[] getResponseTypes()
                                    {
                                        return d.getResponseTypes();
                                    }

                                    @Override
                                    public String getId()
                                    {
                                        return d.getId();
                                    }

                                    @Override
                                    public String getShortName()
                                    {
                                        return d.getShortName();
                                    }

                                    @Override
                                    public String getDescription()
                                    {
                                        return d.getDescription();
                                    }
                                };
                            }

                            @Override
                            public ResourceBundle getResources()
                            {
                                return webScript.getResources();
                            }

                            @Override
                            public void execute(WebScriptRequest webScriptRequest, WebScriptResponse webScriptResponse) throws IOException
                            {
                                webScript.execute(webScriptRequest, webScriptResponse);
                            }

                            @Override
                            public void setURLModelFactory(URLModelFactory urlModelFactory)
                            {
                                webScript.setURLModelFactory(urlModelFactory);
                            }
                        };

                        // override match with noAuth
                        match = new Match(match.getTemplate(), match.getTemplateVars(), match.getPath(), noAuthWebScriptWrapper);
                    }
                }
            }
        }
        else
        {
            match = super.findWebScript(method, uri);
        }

        return match;
    }

    // note: same as ApiWebscript (apiName must not be null)
    private Api determineApi(Map<String, String> templateVars)
    {
        String apiScope = templateVars.get("apiScope");
        String apiVersion = templateVars.get("apiVersion");
        String apiName = templateVars.get("apiName");
        return Api.valueOf(apiName,apiScope,apiVersion);
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
