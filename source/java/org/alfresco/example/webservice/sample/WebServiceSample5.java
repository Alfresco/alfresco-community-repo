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

import javax.xml.rpc.ServiceException;

import org.alfresco.example.webservice.authentication.AuthenticationResult;
import org.alfresco.example.webservice.authentication.AuthenticationServiceLocator;
import org.alfresco.example.webservice.authentication.AuthenticationServiceSoapBindingStub;
import org.alfresco.example.webservice.authoring.AuthoringServiceLocator;
import org.alfresco.example.webservice.authoring.AuthoringServiceSoapBindingStub;
import org.alfresco.example.webservice.authoring.CheckoutResult;
import org.alfresco.example.webservice.content.Content;
import org.alfresco.example.webservice.content.ContentServiceSoapBindingStub;
import org.alfresco.example.webservice.repository.RepositoryServiceSoapBindingStub;
import org.alfresco.example.webservice.types.CML;
import org.alfresco.example.webservice.types.CMLAddAspect;
import org.alfresco.example.webservice.types.ContentFormat;
import org.alfresco.example.webservice.types.NamedValue;
import org.alfresco.example.webservice.types.Predicate;
import org.alfresco.example.webservice.types.Reference;
import org.alfresco.example.webservice.types.Store;
import org.alfresco.example.webservice.types.StoreEnum;
import org.alfresco.example.webservice.types.Version;
import org.alfresco.example.webservice.types.VersionHistory;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.apache.axis.EngineConfiguration;
import org.apache.axis.configuration.FileProvider;

/**
 * Web service sample 5
 * <p>
 * This sample shows how to check documents out, check them back in and then view the 
 * version history.
 * 
 * @author Roy Wetherall
 */
public class WebServiceSample5
{
    private final static String INITIAL_CONTENT = "This is the content pror to checkout";
    private final static String UPDATED_CONTENT = "This is the updated content";
    
    private final static String VERSIONABLE_ASPECT = "{http://www.alfresco.org/model/content/1.0}versionable";
    
    /**
     * Main function
     */
    public static void main(String[] args)
        throws Exception
    {
        // Start the session
        AuthenticationServiceSoapBindingStub authenticationService = (AuthenticationServiceSoapBindingStub)new AuthenticationServiceLocator().getAuthenticationService();
        AuthenticationResult result = authenticationService.startSession(WebServiceSample1.USERNAME, WebServiceSample1.PASSWORD);
        WebServiceSample1.currentTicket = result.getTicket();
        
        // Get the content and authoring service
        RepositoryServiceSoapBindingStub repositoryService = WebServiceSample1.getRepositoryWebService();
        ContentServiceSoapBindingStub contentService = WebServiceSample3.getContentWebService();
        AuthoringServiceSoapBindingStub authoringService = WebServiceSample5.getAuthoringWebService();
        
        // Get a reference to a newly created content
        Reference contentReference = WebServiceSample3.createNewContent(contentService, "SampleFiveFileOne.txt", INITIAL_CONTENT);
        
        // Add the versionable aspect to the newly created content.  This will allows the content to be versioned
        makeVersionable(repositoryService, contentReference);
        
        // Checkout the newly created content, placing the working document in the same folder
        Predicate itemsToCheckOut = new Predicate(new Reference[]{contentReference}, null, null);
        CheckoutResult checkOutResult = authoringService.checkout(itemsToCheckOut, null);
        
        // Get a reference to the working copy
        Reference workingCopyReference = checkOutResult.getWorkingCopies()[0];
        
        // Update the content of the working copy
        ContentFormat format = new ContentFormat(MimetypeMap.MIMETYPE_TEXT_PLAIN, "UTF-8");
        contentService.write(workingCopyReference, ContentModel.PROP_CONTENT.toString(), UPDATED_CONTENT.getBytes(), format);
        
        // Now check the working copy in with a description of the change made that will be recorded in the version history
        Predicate predicate = new Predicate(new Reference[]{workingCopyReference}, null, null);
        NamedValue[] comments = new NamedValue[]{new NamedValue("description", "The content has been updated")};
        authoringService.checkin(predicate, comments, false);
        
        // Output the updated content
        Store store = new Store(StoreEnum.workspace, "SpacesStore");
        Content[] readResult = contentService.read(
                        new Predicate(new Reference[]{contentReference}, store, null), 
                        ContentModel.PROP_CONTENT.toString());
        Content content = readResult[0];
        System.out.println("This is the checked-in content:");
        System.out.println(WebServiceSample3.getContentAsString(WebServiceSample1.currentTicket, content.getUrl()));
        
        // Get the version history
        System.out.println("The version history:");
        VersionHistory versionHistory = authoringService.getVersionHistory(contentReference);
        for (Version version : versionHistory.getVersions())
        {
            // Output the version details
            outputVersion(version);
        }
        
        // End the session
        authenticationService.endSession();        
    }
    
    /**
     * Get a reference to the authoring web service
     * 
     * @return                      the authoring web service
     * @throws ServiceException
     */
    public static AuthoringServiceSoapBindingStub getAuthoringWebService() throws ServiceException
    {
        // Create the content service, adding the WS security header information
        EngineConfiguration config = new FileProvider(new ByteArrayInputStream(WebServiceSample1.WS_SECURITY_INFO.getBytes()));
        AuthoringServiceLocator authoringServiceLocator = new AuthoringServiceLocator(config);        
        AuthoringServiceSoapBindingStub authoringService = (AuthoringServiceSoapBindingStub)authoringServiceLocator.getAuthoringService();
        return authoringService;
    }
    
    /**
     * Helper method to make apply the versionable aspect to a given reference
     * <p>
     * See sample 4 for more CML examples
     * 
     * @param respositoryService    the respository service
     * @param reference             the reference
     * @throws Exception
     */
    public static void makeVersionable(RepositoryServiceSoapBindingStub respositoryService, Reference reference)
        throws Exception
    {
        // Create the add aspect query object
        Predicate predicate = new Predicate(new Reference[]{reference}, null, null);
        CMLAddAspect addAspect = new CMLAddAspect(VERSIONABLE_ASPECT, null, predicate, null); 
        
        // Create the content management language query
        CML cml = new CML();
        cml.setAddAspect(new CMLAddAspect[]{addAspect});
        
        // Execute the query, which will add the versionable aspect to the node is question
        respositoryService.update(cml);
    }
    
    /**
     * Helper to output the version details
     * 
     * @param version   the version
     */
    private static void outputVersion(Version version)
    {
        System.out.println("Version label = " + version.getLabel() + "; Version description = " + version.getCommentaries()[0].getValue());
    }
}
