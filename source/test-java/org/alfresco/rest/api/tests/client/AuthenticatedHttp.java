package org.alfresco.rest.api.tests.client;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.json.simple.JSONObject;

public class AuthenticatedHttp extends AbstractHttp
{
    private static final String JSON_TICKET_KEY = "ticket";
    private static final String TICKET_CREDENTIAL_PLACEHOLDER = "ROLE_TICKET";
    
    /**
     * URL for obtaining an alfresco-ticket
     */
    private String LOGIN_URL = "/alfresco/service/api/login";
    
    private String LOGIN_JSON_USERNAME = "username";
    private String LOGIN_JSON_PASSWORD = "password";
    
    public static final String MIME_TYPE_JSON = "application/json";
    public static final String HEADER_ACCEPT = "Accept";

    private HttpClientProvider httpProvider;
    private AuthenticationDetailsProvider authDetailProvider;
    private boolean ticketBasedAuthentication;
    
    /**
     * @param httpProvider provider class for http-client
     * @param authDetailProvider provider for authentication details
     */
    public AuthenticatedHttp(HttpClientProvider httpProvider, AuthenticationDetailsProvider authenticationDetailsProvider)
    {
        this.httpProvider = httpProvider;
        this.authDetailProvider = authenticationDetailsProvider;
        this.ticketBasedAuthentication = false;
    }
    
    /**
     * Enable ticket-based authentication. If set to false, BASIC Authentication will
     * be used instead. Defaults to false.
     * 
     * @param ticketBasedAuthentication whether or not to use ticket for authentication
     */
    public void setTicketBasedAuthentication(boolean ticketBasedAuthentication)
    {
        this.ticketBasedAuthentication = ticketBasedAuthentication;
    }
    
    /**
     * @return the {@link HttpClientProvider} used by this class.
     */
    public HttpClientProvider getHttpProvider()
    {
        return this.httpProvider;
    }
    
    /**
     * @return the {@link AuthenticationDetailsProvider} used by this class.
     */
    public AuthenticationDetailsProvider getAuthDetailProvider()
    {
        return this.authDetailProvider;
    }
    
    /**
     * Execute the given method, authenticated as the given user. Automatically closes the 
     * response-stream to release the connection. If response should be extracted, this should be done
     * in the {@link HttpRequestCallback}.
     * 
     * @param method method to execute
     * @param userName name of user to authenticate
     * @param callback called after http-call is executed. When callback returns, the 
     *  response stream is closed, so all respose-related operations should be done in the callback. Can be null.
     * @return result returned by the callback or status-code if no callback is given.
     */
    public <T extends Object> T executeHttpMethodAuthenticated(HttpMethod method, String userName, HttpRequestCallback<T> callback)
    {
        if(ticketBasedAuthentication)
        {
            return executeWithTicketAuthentication(method, userName, authDetailProvider.getPasswordForUser(userName), callback);
        }
        else
        {
            return executeWithBasicAuthentication(method, userName, authDetailProvider.getPasswordForUser(userName), callback);
        }
    }
    
    public <T extends Object> T executeHttpMethodAuthenticated(HttpMethod method, String userName, String password, HttpRequestCallback<T> callback)
    {
        if(ticketBasedAuthentication)
        {
            return executeWithTicketAuthentication(method, userName, password, callback);
        }
        else
        {
            return executeWithBasicAuthentication(method, userName, password, callback);
        }
    }
    
    /**
     * Execute the given method, authenticated as the Alfresco Administrator.
     * 
     * @param method method to execute
     * @param callback called after http-call is executed. When callback returns, the 
     *  response stream is closed, so all respose-related operations should be done in the callback. Can be null.
     * @return result returned by the callback or null if no callback is given.
     */
    public <T extends Object> T executeHttpMethodAsAdmin(HttpMethod method, HttpRequestCallback<T> callback)
    {
        if(ticketBasedAuthentication)
        {
            return executeWithTicketAuthentication(method, authDetailProvider.getAdminUserName(), authDetailProvider.getAdminPassword(), callback);
        }
        else
        {
            return executeWithBasicAuthentication(method, authDetailProvider.getAdminUserName(), authDetailProvider.getAdminPassword(), callback);
        }
    }
    
    /**
     * Execute the given method, authenticated as the given user using Basic Authentication.
     * @param method method to execute
     * @param userName name of user to authenticate
     * @param callback called after http-call is executed. When callback returns, the 
     *  response stream is closed, so all respose-related operations should be done in the callback. Can be null.
     * @return result returned by the callback or null if no callback is given.
     */
    private <T extends Object> T executeWithBasicAuthentication(HttpMethod method, String userName, String password, HttpRequestCallback<T> callback)
    {
        try
        {
            HttpState state = new HttpState();
            state.setCredentials(
                        new AuthScope(null, AuthScope.ANY_PORT),
                        new UsernamePasswordCredentials(userName, password));
            
            httpProvider.getHttpClient().executeMethod(null, method, state);
            
            if(callback != null)
            {
                return callback.onCallSuccess(method);
            }
            
            // No callback used, return null
            return null;
        }
        catch(Throwable t)
        {
            boolean handled = false;
            
            // Delegate to callback to handle error. If not available, throw exception
            if(callback != null)
            {
                handled = callback.onError(method, t);
            }
            
            if(!handled)
            {
                throw new RuntimeException("Error while executing HTTP-call (" + method.getPath() +")", t);
            }
            
            return null;
        }
        finally
        {
            method.releaseConnection();
        }
    }
    
    /**
     * Execute the given method, authenticated as the given user using ticket-based authentication.
     * @param method method to execute
     * @param userName name of user to authenticate
     * @return status-code resulting from the request
     */
    private <T extends Object> T executeWithTicketAuthentication(HttpMethod method, String userName, String password, HttpRequestCallback<T> callback)
    {
        String ticket = authDetailProvider.getTicketForUser(userName);
        if(ticket == null)
        {
           ticket = fetchLoginTicket(userName, password);
           authDetailProvider.updateTicketForUser(userName, ticket);
        }
        
        
        
        try
        {
            HttpState state = applyTicketToMethod(method, ticket);
            
           // Try executing the method
            int result = httpProvider.getHttpClient().executeMethod(null, method, state);
            
            if(result == HttpStatus.SC_UNAUTHORIZED || result == HttpStatus.SC_FORBIDDEN)
            {
                method.releaseConnection();
                if(!method.validate())
                {
                    throw new RuntimeException("Ticket re-authentication failed for user " + userName + " (HTTPMethod not reusable)");
                }
                // Fetch new ticket, store and apply to HttpMethod
                ticket = fetchLoginTicket(userName, userName);
                authDetailProvider.updateTicketForUser(userName, ticket);
                
                state = applyTicketToMethod(method, ticket);
                
                // Run method agian with new ticket
                result = httpProvider.getHttpClient().executeMethod(null, method, state);
            }
           
            if(callback != null)
            {
                return callback.onCallSuccess(method);
            }
            
            return null;
        }
        catch(Throwable t)
        {
            boolean handled = false;
            // Delegate to callback to handle error. If not available, throw exception
            if(callback != null)
            {
                handled = callback.onError(method, t);
            }
            
            if(!handled)
            {
                throw new RuntimeException("Error while executing HTTP-call (" + method.getPath() +")", t);
            }
            return null;
            
        }
        finally
        {
            method.releaseConnection();
        }
        
    }
    
    /**
     * Add the ticket to the method. In case of {@link EntityEnclosingMethod}s (which don't 
     * support Query-parameters), the ticket is added as Username in BASIC Authentication, 
     * this is a supported way of passing in ticket into Alfresco.
     * 
     * @param method method to apply
     * @param ticket ticket to apply
     * 
     * @return a {@link HttpState} object to use. Null, if no specific state should be used.
     */
    private HttpState applyTicketToMethod(HttpMethod method, String ticket) throws URIException
    {
        // POST and PUT methods don't support Query-params, use Basic Authentication to pass
        // in the ticket (ROLE_TICKET) for all methods.
        HttpState state = new HttpState();
        state.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(TICKET_CREDENTIAL_PLACEHOLDER, ticket));
        return state;
        
    }

    /**
     * Adds the JSON as request-body the the method and sets the correct
     * content-type.
     * @param method
     * @param object
     */
    private void populateRequestBody(EntityEnclosingMethod method, JSONObject object)
    {
        try
        {
            method.setRequestEntity(new StringRequestEntity(object.toJSONString(), MIME_TYPE_JSON, "UTF-8"));
        }
        catch (UnsupportedEncodingException error)
        {
            // This will never happen!
            throw new RuntimeException("All hell broke loose, a JVM that doesn't have UTF-8 encoding...");
        }
    }

    /**
     * Perform the login-call to obtain a ticket.
     * 
     * @param userName user to log in
     * @return ticket to use for authentication.
     * @throws RuntimeException when no ticket can be obtained for the user.
     */
    @SuppressWarnings("unchecked")
    private String fetchLoginTicket(String userName, String password)
    {
        String url = httpProvider.getFullAlfrescoUrlForPath(LOGIN_URL);
        PostMethod loginMethod = null;
        try
        {
            loginMethod = new PostMethod(url);
            loginMethod.setRequestHeader(HEADER_ACCEPT, MIME_TYPE_JSON);

            // Populate resuest body
            JSONObject requestBody = new JSONObject();
            requestBody.put(LOGIN_JSON_USERNAME, userName);
            requestBody.put(LOGIN_JSON_PASSWORD, password);
            
            populateRequestBody(loginMethod, requestBody);
            
            HttpClient client = httpProvider.getHttpClient();
            
            // Since no authentication info is available yet, no need to use a
            // custom HostConfiguration for the login-call
            client.executeMethod(loginMethod);
            
            if(loginMethod.getStatusCode() == HttpStatus.SC_OK)
            {
                // Extract the ticket
                JSONObject data = getDataFromResponse(loginMethod);
                if(data == null)
                {
                    throw new RuntimeException("Failed to login to Alfresco with user " + userName +
                    		" (No JSON-data found in response)");
                }
                
                // Extract the actual ticket
                String ticket = getString(data, JSON_TICKET_KEY, null);
                if(ticket == null)
                {
                    throw new RuntimeException("Failed to login to Alfresco with user " + userName +
                                " (No ticket found in JSON-response)");
                }
                return ticket;
            }
            else
            {
                // Unable to login
                throw new RuntimeException("Failed to login to Alfresco with user " + userName +
                            " (" + loginMethod.getStatusCode() + loginMethod.getStatusLine().getReasonPhrase() + ")");
            }
        }
        catch (IOException ioe)
        {
            // Something went wrong when sending request
            throw new RuntimeException("Failed to login to Alfresco with user " + userName, ioe);
        }
        finally
        {
            if(loginMethod != null)
            {
                try
                {
                    loginMethod.releaseConnection();
                }
                catch(Throwable t)
                {
                    // Ignore this to prevent swallowing potential original exception
                }
            }
        }
    }
    
    /**
     * Callback used when executing HTTP-request. After this has been called,
     * the response-stream is closed automatically.
     * 
     * @author Frederik Heremans
     */
    public interface HttpRequestCallback<T extends Object>
    {
        /**
         * Called when call was successful.
         * @param method the method executed which can be used to extract response from.
         * @return any result extracted from the response body.
         */
        T onCallSuccess(HttpMethod method) throws Exception;
        /**
         * Called when an error occurs when sending the request.
         * @param method the method executed
         * @param t optional exception that caused the error
         * @return true, if error is handled. False, if an exception should be thrown.
         */
        boolean onError(HttpMethod method, Throwable t);
    }
}
