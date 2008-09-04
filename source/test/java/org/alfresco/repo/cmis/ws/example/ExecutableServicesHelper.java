/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * and Open Source Software ("FLOSS") applications as described in Alfresco's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.cmis.ws.example;

import java.io.IOException;
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

import org.alfresco.repo.cmis.ws.FolderTreeType;
import org.alfresco.repo.cmis.ws.GetChildren;
import org.alfresco.repo.cmis.ws.GetChildrenResponse;
import org.alfresco.repo.cmis.ws.NavigationServicePort;
import org.alfresco.repo.cmis.ws.RepositoryInfoType;
import org.alfresco.repo.cmis.ws.RepositoryServicePort;
import org.alfresco.repo.cmis.ws.RepositoryType;
import org.alfresco.repo.cmis.ws.TypesOfFileableObjectsEnum;
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
public class ExecutableServicesHelper
{
    private static final QName NAVIGATION_SERVICE_NAME = new QName("http://www.cmis.org/ns/1.0", "NavigationService");
    private static final QName REPOSITORY_SERVICE_NAME = new QName("http://www.cmis.org/ns/1.0", "RepositoryService");

    private static final String NAVIGATION_SERVER_URL_POSTFIX = "/alfresco/cmis/NavigationService?wsdl";
    private static final String REPOSITORY_SERVER_URL_POSTFIX = "/alfresco/cmis/RepositoryService?wsdl";

    private String username;
    private String password;
    private Service navigationServicesFactory;
    private Service repositoryServicesFactory;

    /**
     * @param username - an existent authentication user name
     * @param password - appropriate password for specified user name
     * @param serverAddress - IP address (or domain name) and port for the server to connect
     * @throws Exception - an caught <b>MalformedURLException</b> in time of server connect <b>URL</b> creation
     */
    public ExecutableServicesHelper(String username, String password, String serverAddress) throws Exception
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
            throw new Exception("Field to connect to specified URL. Exception Message: " + e.getMessage());
        }
    }

    /**
     * This method simplify <b>RepositoryServicePort</b> instance creation
     *
     * @return an instance of <b>RepositoryServicePort</b>
     */
    public RepositoryServicePort receiveAuthorizedRepositoryServicePort()
    {
        RepositoryServicePort result = repositoryServicesFactory.getPort(RepositoryServicePort.class);

        createAuthorizationClient(result);

        return result;
    }

    /**
     * This method simplify <b>NavigationServicePort</b> instance creation
     *
     * @return an instance of <b>NavigationServicePort</b>
     */
    public NavigationServicePort receiveAuthorizedNavigationServicePort()
    {
        NavigationServicePort result = navigationServicesFactory.getPort(NavigationServicePort.class);

        createAuthorizationClient(result);

        return result;
    }

    /**
     * This method simplify configuring of <b>GetChildren CMIS Service</b> query with "ANY" filter, <b>Company Home Object Identificator</b> and <b>FOLDERS_AND_DOCUMENTS</b>
     * entity types.
     *
     * @param servicesPort - <b>NavigationServicePort</b> configured with <b>WSS4J Client</b> instance
     * @return <b>List<FolderTreeType></b> - list of all children elements of <b>Company Home</b> folder
     * @throws Exception This exception throws when any <b>CMIS Services</b> operations was failed
     */
    public List<FolderTreeType> receiveSpaceContent(NavigationServicePort servicesPort) throws Exception
    {
        GetChildrenResponse response;

        try
        {
            response = servicesPort.getChildren(configureGetChildrenServiceQuery());

            if ((response != null) && (response.getChildren().getChild() != null))
            {
                return response.getChildren().getChild();
            }
            else
            {
                return null;
            }
        }
        catch (Exception e)
        {
            throw new Exception("Can't receive content of Company Home caused: " + e.getMessage());
        }
    }

    /**
     * This method simplify receiving of Object Identificator for Company Home Root Folder
     *
     * @param servicesPort - <b>RepositoryServicePort</b> instance that configured with WSS4J Client
     * @return <b>String</b> representation of <b>Object Identificator</b>
     * @throws Exception This exception throws when any <b>CMIS Services</b> operations was failed
     */
    public String receiveCompanyHomeObjectId(RepositoryServicePort servicesPort) throws Exception
    {
        try
        {
            List<RepositoryType> repositories = servicesPort.getRepositories();
            if (repositories.isEmpty())
            {
                throw new RuntimeException("List of repositories is empty");
            }
            else
            {
                RepositoryInfoType repositoryInfo = servicesPort.getRepositoryInfo(repositories.get(0).getRepositoryID());
                return repositoryInfo.getRootFolderId();
            }
        }
        catch (Exception e)
        {
            throw new Exception("Can't receive Repository info caused: " + e.getMessage());
        }
    }

    /**
     * This method simplify creation of authorized Client instance with specified user name and appropriate password
     *
     * @return - an instance of authorized <b>CMIS Client</b>
     */
    protected Client createAuthorizationClient(Object servicePortInstance)
    {
        Map<String, Object> outInterceptorProperties = configureWss4jProperties();

        WSS4JOutInterceptor outInterceptor = new WSS4JOutInterceptor(outInterceptorProperties);

        return createAndConfigureClientInstance(servicePortInstance, outInterceptor);
    }

    private GetChildren configureGetChildrenServiceQuery() throws Exception
    {
        GetChildren requestParameters = new GetChildren();
        requestParameters.setFilter("*");
        requestParameters.setFolderId(receiveCompanyHomeObjectId(receiveAuthorizedRepositoryServicePort()));
        requestParameters.setType(TypesOfFileableObjectsEnum.ANY);

        return requestParameters;
    }

    private Client createAndConfigureClientInstance(Object servicePortInstance, WSS4JOutInterceptor outInterceptor)
    {
        Client client = ClientProxy.getClient(servicePortInstance);
        client.getEndpoint().getOutInterceptors().add(new SAAJOutInterceptor());
        client.getEndpoint().getOutInterceptors().add(outInterceptor);

        return client;
    }

    private Map<String, Object> configureWss4jProperties()
    {
        Map<String, Object> outInterceptorProperties = new HashMap<String, Object>();
        outInterceptorProperties.put(WSHandlerConstants.ACTION, WSHandlerConstants.USERNAME_TOKEN + " " + WSHandlerConstants.TIMESTAMP);
        outInterceptorProperties.put(WSHandlerConstants.USER, username);
        outInterceptorProperties.put(WSHandlerConstants.PASSWORD_TYPE, WSConstants.PW_DIGEST);

        outInterceptorProperties.put(WSHandlerConstants.PW_CALLBACK_REF, new CallbackHandler()
        {
            public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException
            {
                ((WSPasswordCallback) callbacks[0]).setPassword(password);
            }
        });

        return outInterceptorProperties;
    }
}
