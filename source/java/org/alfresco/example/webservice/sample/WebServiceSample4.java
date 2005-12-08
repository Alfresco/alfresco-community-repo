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

import org.alfresco.example.webservice.authentication.AuthenticationResult;
import org.alfresco.example.webservice.authentication.AuthenticationServiceLocator;
import org.alfresco.example.webservice.authentication.AuthenticationServiceSoapBindingStub;
import org.alfresco.example.webservice.repository.RepositoryServiceSoapBindingStub;
import org.alfresco.example.webservice.repository.UpdateResult;
import org.alfresco.example.webservice.types.CML;
import org.alfresco.example.webservice.types.CMLAddAspect;
import org.alfresco.example.webservice.types.CMLCreate;
import org.alfresco.example.webservice.types.Node;
import org.alfresco.example.webservice.types.ParentReference;
import org.alfresco.example.webservice.types.Predicate;
import org.alfresco.example.webservice.types.Reference;
import org.alfresco.example.webservice.types.Store;
import org.alfresco.example.webservice.types.StoreEnum;
import org.alfresco.model.ContentModel;

/**
 * Web service sample 4
 * <p>
 * This sample shows how to construct and execute CML queries using the respository web service.
 * 
 * @author Roy Wetherall
 */
public class WebServiceSample4
{
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
        
        // Create a store object referencing the main spaced store
        Store store = new Store(StoreEnum.workspace, "SpacesStore");
        
        // Get the repository 
        RepositoryServiceSoapBindingStub repositoryService = WebServiceSample1.getRepositoryWebService();        
        Reference folder = getTutorialFolder(store, repositoryService);
        
        // Create the CML structure
        // When executed this cml update query will create a new content node beneth the tutorial folder and the add the
        // versionable aspect to the newly created node
        ParentReference parentReference = new ParentReference(ContentModel.ASSOC_CONTAINS.toString(), ContentModel.ASSOC_CONTAINS.toString());
        parentReference.setStore(store);
        parentReference.setUuid(folder.getUuid());
        CMLCreate create = new CMLCreate("id1", parentReference, ContentModel.TYPE_CONTENT.toString(), null);        
        CMLAddAspect addAspect = new CMLAddAspect(ContentModel.ASPECT_VERSIONABLE.toString(), null, null, "id1");
        CML cml = new CML();
        cml.setCreate(new CMLCreate[]{create});
        cml.setAddAspect(new CMLAddAspect[]{addAspect});
        
        // Execute the update
        UpdateResult[] updateResults = repositoryService.update(cml);
               
        for (UpdateResult updateResult : updateResults)
        {
            String sourceId = "none";
            Reference source = updateResult.getSource();
            if (source != null)
            {
                sourceId = source.getUuid();
            }
            
            String destinationId = "none";
            Reference destination = updateResult.getDestination();
            if (destination != null)
            {
                destinationId = destination.getUuid();
            }
            
            System.out.println(
                    "Command = " + updateResult.getStatement() + 
                    "; Source = " + sourceId +
                    "; Destination = " + destinationId);
        }
        
        // End the session
        authenticationService.endSession();      
    }
    
    /**
     * Get the space immediatly beneth company home call "Alfresco Tutorial"
     * 
     * @param store
     * @param repositoryService
     * @return
     * @throws Exception
     */
    public static Reference getTutorialFolder(Store store, RepositoryServiceSoapBindingStub repositoryService)
        throws Exception
    {
        Reference reference = new Reference(store, null, "/app:company_home/*[@cm:name=\"Alfresco Tutorial\"]");
        Predicate predicate = new Predicate(new Reference[]{reference}, null, null);        
        Node[] nodes = repositoryService.get(predicate);
        return nodes[0].getReference();
    }
    
}
