/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2017 Alfresco Software Limited
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
import org.alfresco.rest.framework.core.exceptions.NotFoundException;
import org.alfresco.rest.framework.resource.actions.interfaces.BinaryResourceAction;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceBinaryAction;
import org.alfresco.rest.framework.resource.actions.interfaces.ResourceAction;
import org.alfresco.rest.framework.tools.ApiAssistant;
import org.apache.commons.lang3.StringUtils;
import org.springframework.extensions.webscripts.*;
import org.springframework.extensions.webscripts.Description.FormatStyle;
import org.springframework.extensions.webscripts.Description.RequiredAuthentication;
import org.springframework.extensions.webscripts.Description.RequiredTransaction;
import org.springframework.extensions.webscripts.Description.TransactionCapability;
import org.springframework.http.HttpMethod;

/**
 *
 * @author steveglover
 * @author janv
 * @author Jamal Kaabi-Mofrad
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
        Match match;

        HttpMethod httpMethod = HttpMethod.valueOf(method);
        if (HttpMethod.GET.equals(httpMethod))
        {
            if (uri.equals(PublicApiTenantWebScriptServletRequest.NETWORKS_PATH))
            {
                Map<String, String> templateVars = new HashMap<>();
                templateVars.put("apiScope", "public");
                templateVars.put("apiVersion", "1");
                templateVars.put("apiName", "networks");
                match = new Match("", templateVars, "", getNetworksWebScript);
            }
            else if (uri.equals(PublicApiTenantWebScriptServletRequest.NETWORK_PATH))
            {
                Map<String, String> templateVars = new HashMap<>();
                templateVars.put("apiScope", "public");
                templateVars.put("apiVersion", "1");
                templateVars.put("apiName", "network");
                match = new Match("", templateVars, "", getNetworkWebScript);
            }
            else
            {
                match = super.findWebScript(method, uri);
                if (match == null)
                {
                    return null;
                }
                Map<String, String> templateVars = match.getTemplateVars();
                ResourceWithMetadata rwm = getResourceWithMetadataOrNull(templateVars, httpMethod);
                if (rwm != null)
                {
                    Class<? extends ResourceAction> resAction = null;

                    String entityId = templateVars.get(ResourceLocator.ENTITY_ID);
                    String relationshipId = templateVars.get(ResourceLocator.RELATIONSHIP_ID);

                    switch (rwm.getMetaData().getType())
                    {
                        case ENTITY:
                            if (StringUtils.isNotBlank(entityId))
                            {
                                if (EntityResourceAction.ReadById.class.isAssignableFrom(rwm.getResource().getClass()))
                                {
                                    resAction = EntityResourceAction.ReadById.class;
                                }
                            }
                            else
                            {
                                if (EntityResourceAction.Read.class.isAssignableFrom(rwm.getResource().getClass()))
                                {
                                    resAction = EntityResourceAction.Read.class;
                                }
                            }
                            break;
                        case PROPERTY:
                            if (StringUtils.isNotBlank(entityId))
                            {
                                if (BinaryResourceAction.Read.class.isAssignableFrom(rwm.getResource().getClass()))
                                {
                                    resAction = BinaryResourceAction.Read.class;
                                }
                                else if (RelationshipResourceBinaryAction.Read.class.isAssignableFrom(rwm.getResource().getClass()))
                                {
                                    resAction = RelationshipResourceBinaryAction.Read.class;
                                }
                            }
                            break;
                        case RELATIONSHIP:
                            if (StringUtils.isNotBlank(relationshipId))
                            {
                                if (RelationshipResourceAction.ReadById.class.isAssignableFrom(rwm.getResource().getClass()))
                                {
                                    resAction = RelationshipResourceAction.ReadById.class;
                                }
                            }
                            else
                            {
                                if (RelationshipResourceAction.Read.class.isAssignableFrom(rwm.getResource().getClass()))
                                {
                                    resAction = RelationshipResourceAction.Read.class;
                                }
                            }
                            break;
                        default:
                            break;
                    }

                    final boolean noAuth = (resAction != null && rwm.getMetaData().isNoAuth(resAction));
                    if (noAuth)
                    {
                        // override match with noAuth
                        match = overrideMatch(match);
                    }
                }
            }
        }
        else if (HttpMethod.POST.equals(httpMethod))
        {
            match = super.findWebScript(method, uri);
            if (match != null)
            {
                ResourceWithMetadata rwm = getResourceWithMetadataOrNull(match.getTemplateVars(), httpMethod);
                if (rwm != null)
                {
                    Class<? extends ResourceAction> resAction = null;
                    Boolean noAuth = null;
                    switch (rwm.getMetaData().getType())
                    {
                        case ENTITY:
                            if (EntityResourceAction.Create.class.isAssignableFrom(rwm.getResource().getClass()))
                            {
                                resAction = EntityResourceAction.Create.class;
                            }
                            else if (EntityResourceAction.CreateWithResponse.class.isAssignableFrom(rwm.getResource().getClass()))
                            {
                                resAction = EntityResourceAction.CreateWithResponse.class;
                            }
                            break;
                        case RELATIONSHIP:
                            if (RelationshipResourceAction.Create.class.isAssignableFrom(rwm.getResource().getClass()))
                            {
                                resAction = RelationshipResourceAction.Create.class;
                            }
                            else if (RelationshipResourceAction.CreateWithResponse.class.isAssignableFrom(rwm.getResource().getClass()))
                            {
                                resAction = RelationshipResourceAction.CreateWithResponse.class;
                            }
                            break;
                        case OPERATION:
                            noAuth = rwm.getMetaData().isNoAuth(null);
                            break;
                        default:
                            break;
                    }

                    if (noAuth == null)
                    {
                        noAuth = (resAction != null && rwm.getMetaData().isNoAuth(resAction));
                    }
                    if (noAuth)
                    {
                        // override match with noAuth
                        match = overrideMatch(match);
                    }
                }
            }
        }
        else
        {
            match = super.findWebScript(method, uri);
        }

        if (match == null)
        {
            throw new NotFoundException(NotFoundException.DEFAULT_MESSAGE_ID, new String[] {uri});
        }
        return match;
    }

    private ResourceWithMetadata getResourceWithMetadataOrNull(Map<String, String> templateVars, HttpMethod method)
    {
        if (templateVars.get("apiName") != null)
        {
            // NOTE: noAuth currently only exposed for GET or POST
            Api api = ApiAssistant.determineApi(templateVars);

            // TODO can we avoid locating resource more than once (or at least provide a common code to determine the GET resourceAction) ?
            return locator.locateResource(api, templateVars, method);
        }
        return null;
    }

    private Match overrideMatch(final Match match)
    {
        // TODO is there a better way (to dynamically override "requiredAuthentication") or handle noAuth check earlier ?
        WebScript noAuthWebScriptWrapper = new WebScript()
        {
            @Override
            public void init(Container container, Description description)
            {
                match.getWebScript().init(container, description);
            }

            @Override
            public Description getDescription()
            {
                final Description d = match.getWebScript().getDescription();
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
                return match.getWebScript().getResources();
            }

            @Override
            public void execute(WebScriptRequest webScriptRequest, WebScriptResponse webScriptResponse) throws IOException
            {
                match.getWebScript().execute(webScriptRequest, webScriptResponse);
            }

            @Override
            public void setURLModelFactory(URLModelFactory urlModelFactory)
            {
                match.getWebScript().setURLModelFactory(urlModelFactory);
            }
        };

        // override match with noAuth
        return new Match(match.getTemplate(), match.getTemplateVars(), match.getPath(), noAuthWebScriptWrapper);
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

