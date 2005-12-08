// NOTE: if you change the package location of this class you will need to update the WS_SEURITY_INFO XML as for this example this class
//       doubles as the passwordCAllbackClass
package org.alfresco.example.webservice.sample;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.example.webservice.authentication.AuthenticationResult;
import org.alfresco.example.webservice.authentication.AuthenticationServiceLocator;
import org.alfresco.example.webservice.authentication.AuthenticationServiceSoapBindingStub;
import org.alfresco.example.webservice.repository.QueryResult;
import org.alfresco.example.webservice.repository.RepositoryServiceLocator;
import org.alfresco.example.webservice.repository.RepositoryServiceSoapBindingStub;
import org.alfresco.example.webservice.types.NamedValue;
import org.alfresco.example.webservice.types.Node;
import org.alfresco.example.webservice.types.Predicate;
import org.alfresco.example.webservice.types.Query;
import org.alfresco.example.webservice.types.QueryLanguageEnum;
import org.alfresco.example.webservice.types.Reference;
import org.alfresco.example.webservice.types.ResultSet;
import org.alfresco.example.webservice.types.ResultSetRow;
import org.alfresco.example.webservice.types.Store;
import org.alfresco.example.webservice.types.StoreEnum;
import org.alfresco.model.ContentModel;
import org.apache.axis.EngineConfiguration;
import org.apache.axis.configuration.FileProvider;
import org.apache.ws.security.WSPasswordCallback;

public class WebServiceSampleGetRankedContent implements CallbackHandler
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
         "               <parameter name='passwordCallbackClass' value='org.alfresco.example.webservice.sample.WebServiceSampleGetRankedContent'/>" +
         "               <parameter name='passwordType' value='PasswordText'/>" +
         "           </handler>" +
         "       </requestFlow >" +
         "   </globalConfiguration>" +
         "</deployment>";
    
    /**
     * Main method
     * 
     * @param args
     */
    public static void main(String[] args)
    {
        WebServiceSampleGetRankedContent sample = new WebServiceSampleGetRankedContent();
        List<ContentResult> results = sample.getRankedContent("Alfresco Tutorial", "alfresco*");
        
        // Output the results for visual inspection
        int iCount = 1;
        for (ContentResult result : results)
        {
            System.out.println("Result " + iCount + ": " + result.toString());
            iCount ++;
        }
    }
    
    /**
     * Get a list of ordered results of documents in the space specified matching the search 
     * text provided.
     * 
     * @param spaceName     the name of the space (immediatly beneth the company home space) to search
     * @param searchValue   the FTS search value
     * @return              list of results
     */
    public List<ContentResult> getRankedContent(String spaceName, String searchValue)
    {
        List<ContentResult> results = new ArrayList<ContentResult>();
        
        try
        {
            // Get the authentication service
            AuthenticationServiceSoapBindingStub authenticationService = (AuthenticationServiceSoapBindingStub)new AuthenticationServiceLocator().getAuthenticationService();
            
            // Start the session
            AuthenticationResult authenticationResult = authenticationService.startSession(WebServiceSample1.USERNAME, WebServiceSample1.PASSWORD);
            WebServiceSampleGetRankedContent.currentTicket = authenticationResult.getTicket();
            
            // Create the respository service, adding the WS security header information
            EngineConfiguration config = new FileProvider(new ByteArrayInputStream(WebServiceSampleGetRankedContent.WS_SECURITY_INFO.getBytes()));
            RepositoryServiceLocator repositoryServiceLocator = new RepositoryServiceLocator(config);        
            RepositoryServiceSoapBindingStub repositoryService = (RepositoryServiceSoapBindingStub)repositoryServiceLocator.getRepositoryService();       
            
            // Create a store object referencing the main spaced store
            Store store = new Store(StoreEnum.workspace, "SpacesStore");
            
            // Get a reference to the space we have named
            Reference reference = new Reference(store, null, "/app:company_home/*[@cm:name=\"" + spaceName + "\"]");
            Predicate predicate = new Predicate(new Reference[]{reference}, null, null);        
            Node[] nodes = repositoryService.get(predicate);
            
            // Create a query object, looking for all items with alfresco in the name of text
            Query query = new Query(
                    QueryLanguageEnum.lucene, 
                    "+PARENT:\"workspace://SpacesStore/"+ nodes[0].getReference().getUuid() + "\" +TEXT:\"" + searchValue + "\"");
            
            // Execute the query
            QueryResult queryResult = repositoryService.query(store, query, false);
            
            // Display the results
            ResultSet resultSet = queryResult.getResultSet();
            ResultSetRow[] rows = resultSet.getRows();
            
            if (rows != null)
            {
                // Get the infomation from the result set
                for(ResultSetRow row : rows)
                {
                    String nodeId = row.getNode().getId();
                    ContentResult contentResult = new ContentResult(nodeId);
                    
                    for (NamedValue namedValue : row.getColumns())
                    {
                        if (namedValue.getName().endsWith(ContentModel.PROP_CREATED.toString()) == true)
                        {
                            contentResult.setCreateDate(namedValue.getValue());
                        }
                        else if (namedValue.getName().endsWith(ContentModel.PROP_NAME.toString()) == true)
                        {
                            contentResult.setName(namedValue.getValue());
                        }
                        else if (namedValue.getName().endsWith(ContentModel.PROP_DESCRIPTION.toString()) == true)
                        {
                            contentResult.setDescription(namedValue.getValue());   
                        }
                        else if (namedValue.getName().endsWith(ContentModel.PROP_CONTENT.toString()) == true)
                        {
                            // We could go to the content service and ask for the content to get the URL but to save time we 
                            // might as well dig the content URL out of the results.                        
                            String contentString = namedValue.getValue();
                            String[] values = contentString.split("[|=]");
                            contentResult.setUrl(values[1]);
                        }
                    }
                    
                    results.add(contentResult);
                }
            }
            
            // End the session
            authenticationService.endSession();
        }
        catch (Exception serviceException)
        {
            throw new AlfrescoRuntimeException("Unable to perform search.", serviceException);
        }
        
        return results;
    }

    /**
     * Security callback handler
     */
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException
    {
        for (int i = 0; i < callbacks.length; i++) 
        {
           if (callbacks[i] instanceof WSPasswordCallback) 
           {
              WSPasswordCallback pc = (WSPasswordCallback)callbacks[i];
              pc.setPassword(WebServiceSampleGetRankedContent.currentTicket);
           }
           else 
           {
              throw new UnsupportedCallbackException(callbacks[i], "Unrecognized Callback");
           }
        }
    }
    
    /**
     * Class to contain the information about the result from the query
     */
    public class ContentResult
    {
        private String id;
        private String name;
        private String description;
        private String url;       
        private String createDate;
        
        public ContentResult(String id)
        {
            this.id = id;
        }
        
        public String getCreateDate()
        {
            return createDate;
        }
        
        public void setCreateDate(String createDate)
        {
            this.createDate = createDate;
        }
        
        public String getDescription()
        {
            return description;
        }
        
        public void setDescription(String description)        
        {
            this.description = description;
        }
        
        public String getId()
        {
            return id;
        }

        public String getName()
        {
            return name;
        }
        
        public void setName(String name)
        {
            this.name = name;
        }
        
        public String getUrl()
        {
            return url;
        }
        
        public void setUrl(String url)
        {
            this.url = url;
        }      

        @Override
        public String toString()
        {
            return "id=" + this.id + 
                   "; name=" + this.name + 
                   "; description=" + this.description + 
                   "; created=" + this.createDate + 
                   "; url=" + this.url;
        }
    }
}
