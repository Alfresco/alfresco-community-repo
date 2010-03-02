/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.cmis.ws.example;

import java.io.IOException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.alfresco.repo.cmis.ws.CmisObjectInFolderListType;
import org.alfresco.repo.cmis.ws.CmisObjectInFolderType;
import org.alfresco.repo.cmis.ws.EnumIncludeRelationships;
import org.alfresco.repo.cmis.ws.NavigationServicePort;
import org.alfresco.repo.cmis.ws.RepositoryServicePort;
import org.apache.cxf.binding.soap.saaj.SAAJOutInterceptor;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSPasswordCallback;
import org.apache.ws.security.handler.WSHandlerConstants;

/**
 * This helper-class contain all necessary for <b>SimpleCmisWsTest</b> correct working service-methods
 * 
 * @author Dmitry Velichkevich
 */
public class CmisUtils
{
    private static final QName NAVIGATION_SERVICE_NAME = new QName("http://docs.oasis-open.org/ns/cmis/ws/200908/", "NavigationService");
    private static final QName REPOSITORY_SERVICE_NAME = new QName("http://docs.oasis-open.org/ns/cmis/ws/200908/", "RepositoryService");

    private static final String NAVIGATION_SERVER_URL_POSTFIX = "/alfresco/cmis/NavigationService?wsdl";
    private static final String REPOSITORY_SERVER_URL_POSTFIX = "/alfresco/cmis/RepositoryService?wsdl";

    private String username;
    private String password;
    private Service navigationServicesFactory;
    private Service repositoryServicesFactory;

    private RepositoryServicePort repositoryService;
    private NavigationServicePort navigationService;

    private String repositoryId;
    private String rootFolderId;

    /**
     * @param username - an existent authentication user name
     * @param password - appropriate password for specified user name
     * @param serverAddress - IP address (or domain name) and port for the server to connect
     * @throws Exception - an caught <b>MalformedURLException</b> in time of server connect <b>URL</b> creation
     */
    public CmisUtils(String username, String password, String serverAddress)
    {
        this.username = username;
        this.password = password;

        try
        {
            navigationServicesFactory = Service.create(new URL(serverAddress + NAVIGATION_SERVER_URL_POSTFIX), NAVIGATION_SERVICE_NAME);
            repositoryServicesFactory = Service.create(new URL(serverAddress + REPOSITORY_SERVER_URL_POSTFIX), REPOSITORY_SERVICE_NAME);
        }
        catch (MalformedURLException e)
        {
            throw new RuntimeException("Field to connect to specified URL. Exception Message: " + e.getMessage());
        }
    }

    /**
     * This method simplify receiving of Root Folder Id
     * 
     * @return <b>String</b> representation of <b>Object Identificator</b>
     * @throws RuntimeException This exception will be thrown when any <b>CMIS Services</b> operation fail
     */
    public String getRootFolderId()
    {
        if (null == rootFolderId)
        {
            try
            {
                rootFolderId = getRepositoryService().getRepositoryInfo(getRepositoryId(), null).getRootFolderId();
            }
            catch (Exception e)
            {
                throw new RuntimeException("Can't receive Root Folder Id. Cause error message: " + e.toString());
            }
        }
        return rootFolderId;
    }

    /**
     * This method simplify receiving children objects of specified folder
     * 
     * @param folderId - <b>String</b> value that represents parent folder id
     * @return <b>List&lt;CmisObjectType&gt;</b> - list of all children elements of specified folder
     * @throws RuntimeException This exception will be thrown when any <b>CMIS Services</b> operation fail
     */
    public List<CmisObjectInFolderType> receiveFolderEntry(String folderId)
    {
        CmisObjectInFolderListType result = null;
        try
        {
            result = getNavigationService().getChildren(getRepositoryId(), folderId, "*", "", false, EnumIncludeRelationships.NONE, "", false, BigInteger.ZERO, BigInteger.ZERO,
                    null);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Can't receive Children of specified folder with Id=" + folderId + ". Cause error message: " + e.toString());
        }
        if ((null != result) && (result.getObjects() != null))
        {
            return result.getObjects();
        }
        else
        {
            return null;
        }
    }

    private String getRepositoryId()
    {
        if (null == repositoryId)
        {
            try
            {
                return getRepositoryService().getRepositories(null).get(0).getRepositoryId();
            }
            catch (Exception e)
            {
                throw new RuntimeException("Can't receive Repository Id. Cause error message: " + e.toString());
            }
        }
        return repositoryId;
    }

    /**
     * This method simplify <b>RepositoryServicePort</b> instance creation
     * 
     * @return an instance of <b>RepositoryServicePort</b>
     */
    private RepositoryServicePort getRepositoryService()
    {
        if (null == repositoryService)
        {
            repositoryService = configureWss4jClient(repositoryServicesFactory.getPort(RepositoryServicePort.class));
        }

        return repositoryService;
    }

    /**
     * This method simplify <b>NavigationServicePort</b> instance creation
     * 
     * @return an instance of <b>NavigationServicePort</b>
     */
    private NavigationServicePort getNavigationService()
    {
        if (null == navigationService)
        {
            navigationService = configureWss4jClient(navigationServicesFactory.getPort(NavigationServicePort.class));
        }

        return navigationService;
    }

    private <ResultType> ResultType configureWss4jClient(ResultType servicePort)
    {
        Map<String, Object> outInterceptorProperties = new HashMap<String, Object>();
        outInterceptorProperties.put(WSHandlerConstants.ACTION, WSHandlerConstants.USERNAME_TOKEN + " " + WSHandlerConstants.TIMESTAMP);
        outInterceptorProperties.put(WSHandlerConstants.USER, username);
        outInterceptorProperties.put(WSHandlerConstants.PASSWORD_TYPE, WSConstants.PW_TEXT);

        outInterceptorProperties.put(WSHandlerConstants.PW_CALLBACK_REF, new CallbackHandler()
        {
            public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException
            {
                ((WSPasswordCallback) callbacks[0]).setPassword(password);
            }
        });

        WSS4JOutInterceptor outInterceptor = new WSS4JOutInterceptor(outInterceptorProperties);
        Client client = ClientProxy.getClient(servicePort);
        client.getEndpoint().getOutInterceptors().add(new SAAJOutInterceptor());
        client.getEndpoint().getOutInterceptors().add(outInterceptor);

        return servicePort;
    }
}
