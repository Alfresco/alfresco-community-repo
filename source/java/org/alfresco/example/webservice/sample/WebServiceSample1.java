/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.example.webservice.sample;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.xml.rpc.ServiceException;

import org.alfresco.example.webservice.authentication.AuthenticationResult;
import org.alfresco.example.webservice.authentication.AuthenticationServiceLocator;
import org.alfresco.example.webservice.authentication.AuthenticationServiceSoapBindingStub;
import org.alfresco.example.webservice.repository.RepositoryServiceLocator;
import org.alfresco.example.webservice.repository.RepositoryServiceSoapBindingStub;
import org.alfresco.example.webservice.types.Store;
import org.apache.axis.EngineConfiguration;
import org.apache.axis.configuration.FileProvider;
import org.apache.ws.security.WSPasswordCallback;

/**
 * Web service sample 1.
 * <p>
 * Connect to the reposity and get a list of all the stores available in the repository.
 * 
 * @author Roy Wetherall
 */
public class WebServiceSample1 implements CallbackHandler
{
    /** Admin user name and password used to connect to the repository */
    public static final String USERNAME = "admin";
    public static final String PASSWORD = "admin";
    
    /** The current ticket */
    public static String currentTicket;
    
    /** WS security information */
    public static final String WS_SECURITY_INFO = 
         "<deployment xmlns='http://xml.apache.org/axis/wsdd/' xmlns:java='http://xml.apache.org/axis/wsdd/providers/java'>" +
         "   <transport name='http' pivot='java:org.apache.axis.transport.http.HTTPSender'/>" +
         "   <globalConfiguration >" +
         "      <requestFlow >" +
         "       <handler type='java:org.apache.ws.axis.security.WSDoAllSender' >" +
         "               <parameter name='action' value='UsernameToken'/>" +
         "               <parameter name='user' value='ticket'/>" +
         "               <parameter name='passwordCallbackClass' value='org.alfresco.example.webservice.sample.WebServiceSample1'/>" +
         "               <parameter name='passwordType' value='PasswordText'/>" +
         "           </handler>" +
         "       </requestFlow >" +
         "   </globalConfiguration>" +
         "</deployment>";
    
    /**
     * Connect to the respository and print out the names of the available 
     * 
     * @param args
     */
    public static void main(String[] args) 
        throws Exception
    {
        AuthenticationServiceSoapBindingStub authenticationService = (AuthenticationServiceSoapBindingStub)new AuthenticationServiceLocator().getAuthenticationService();
        
        // Start the session
        AuthenticationResult result = authenticationService.startSession(WebServiceSample1.USERNAME, WebServiceSample1.PASSWORD);
        WebServiceSample1.currentTicket = result.getTicket();
        
        // Get a reference to the respository web service
        RepositoryServiceSoapBindingStub repositoryService = getRepositoryWebService();        
        
        // Get array of stores available in the repository
        Store[] stores = repositoryService.getStores();
        if (stores == null)
        {
            // NOTE: empty array are returned as a null object, this is a issue with the generated web service code.
            System.out.println("There are no stores avilable in the repository.");
        }
        else
        {
            // Output the names of all the stores available in the repository
            System.out.println("The following stores are available in the repository:");
            for (Store store : stores)
            {
                System.out.println(store.getAddress());
            }
        }
        
        // End the session
        authenticationService.endSession();
    }   
    
    /**
     * Get the respository web service.
     * 
     * @return                      the respository web service
     * @throws ServiceException     Service Exception
     */
    public static RepositoryServiceSoapBindingStub getRepositoryWebService() throws ServiceException
    {
        // Create the respository service, adding the WS security header information
        EngineConfiguration config = new FileProvider(new ByteArrayInputStream(WebServiceSample1.WS_SECURITY_INFO.getBytes()));
        RepositoryServiceLocator repositoryServiceLocator = new RepositoryServiceLocator(config);        
        RepositoryServiceSoapBindingStub repositoryService = (RepositoryServiceSoapBindingStub)repositoryServiceLocator.getRepositoryService();
        return repositoryService;
    }
    
    /**
     * The implementation of the passwrod call back used by the WS Security
     * 
     * @see javax.security.auth.callback.CallbackHandler#handle(javax.security.auth.callback.Callback[])
     */
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException
    {
       for (int i = 0; i < callbacks.length; i++) 
       {
          if (callbacks[i] instanceof WSPasswordCallback) 
          {
             WSPasswordCallback pc = (WSPasswordCallback)callbacks[i];
             pc.setPassword(currentTicket);
          }
          else 
          {
             throw new UnsupportedCallbackException(callbacks[i], "Unrecognized Callback");
          }
       }
    }    
}
