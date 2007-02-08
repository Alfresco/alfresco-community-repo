/*
 * Copyright (C) 2005 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.jcr.repository;

import java.util.HashMap;
import java.util.Map;

import javax.jcr.Credentials;
import javax.jcr.LoginException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.jcr.dictionary.NamespaceRegistryImpl;
import org.alfresco.jcr.session.SessionImpl;
import org.alfresco.repo.importer.ImporterComponent;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.descriptor.Descriptor;
import org.alfresco.service.descriptor.DescriptorService;


/**
 * Alfresco implementation of a JCR Repository
 * 
 * @author David Caruana
 */
public class RepositoryImpl implements Repository
{
    /** Empty Password, if not supplied */
    private final static char[] EMPTY_PASSWORD = "".toCharArray();
    
    /** Repository Descriptors */
    private static final Map<String, String> descriptors = new HashMap<String, String>();

    /** Thread Local Session */
    // Note: For now, we're only allowing one active (i.e. logged in) Session per-thread
    private static ThreadLocal<SessionImpl> sessions = new ThreadLocal<SessionImpl>();
    
    // Service dependencies
    private ServiceRegistry serviceRegistry;
    private ImporterComponent importerComponent;
    private String defaultWorkspace = null;

    // Services
    private NamespaceRegistryImpl namespaceRegistry = null;
    

    //
    // Dependency Injection
    //
    
    /**
     * Set the service registry
     * 
     * @param serviceRegistry
     */
    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
    }
    
    /**
     * Set the Importer Component
     * 
     * @param importerComponent
     */
    public void setImporterComponent(ImporterComponent importerComponent)
    {
        this.importerComponent = importerComponent;
    }
    
    /**
     * Sets the Default Workspace
     * 
     * @param defaultWorkspace  default workspace 
     */
    public void setDefaultWorkspace(String defaultWorkspace)
    {
        this.defaultWorkspace = defaultWorkspace;
    }

    /**
     * Initialisation
     */
    public void init()
    {
        if (serviceRegistry == null)
        {
            throw new IllegalStateException("Service Registry has not been specified.");
        }
        
        // initialise namespace registry
        namespaceRegistry = new NamespaceRegistryImpl(false, serviceRegistry.getNamespaceService());

        // initialise descriptors
        DescriptorService descriptorService = serviceRegistry.getDescriptorService();
        Descriptor descriptor = descriptorService.getServerDescriptor();

        String repNameDesc = "Alfresco Content Repository";
        String edition = descriptor.getEdition();
        if (edition != null && edition.length() > 0)
        {
            repNameDesc += " (" + edition + ")";
        }
        String repVersion = descriptor.getVersion();
        
        descriptors.put(Repository.REP_NAME_DESC, repNameDesc);
        descriptors.put(Repository.REP_VENDOR_DESC, "Alfresco");
        descriptors.put(Repository.REP_VENDOR_URL_DESC, "http://www.alfresco.org");
        descriptors.put(Repository.REP_VERSION_DESC, repVersion);
        descriptors.put(Repository.SPEC_NAME_DESC, "Content Repository API for Java(TM) Technology Specification");
        descriptors.put(Repository.SPEC_VERSION_DESC, "1.0");
        descriptors.put(Repository.LEVEL_1_SUPPORTED, "true");
        descriptors.put(Repository.LEVEL_2_SUPPORTED, "true");
        descriptors.put(Repository.OPTION_TRANSACTIONS_SUPPORTED, "true");
        descriptors.put(Repository.QUERY_XPATH_DOC_ORDER, "true");
        descriptors.put(Repository.QUERY_XPATH_POS_INDEX, "true");
    }
    
    /**
     * Get the service registry
     * 
     * @return  the service registry
     */
    public ServiceRegistry getServiceRegistry()
    {
        return serviceRegistry;
    }

    /**
     * Get the importer component
     * 
     * @return  the importer component
     */
    public ImporterComponent getImporterComponent()
    {
        return importerComponent;
    }
    
    /**
     * Get the Namespace Registry
     */
    public NamespaceRegistryImpl getNamespaceRegistry()
    {
        return namespaceRegistry;
    }
    
    /* (non-Javadoc)
     * @see javax.jcr.Repository#getDescriptorKeys()
     */
    public String[] getDescriptorKeys()
    {
        String[] keys = (String[]) descriptors.keySet().toArray(new String[descriptors.keySet().size()]);
        return keys;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Repository#getDescriptor(java.lang.String)
     */
    public String getDescriptor(String key)
    {
        return descriptors.get(key); 
    }

    /* (non-Javadoc)
     * @see javax.jcr.Repository#login(javax.jcr.Credentials, java.lang.String)
     */
    public Session login(Credentials credentials, String workspaceName)
        throws LoginException, NoSuchWorkspaceException, RepositoryException
    {
        // extract username and password
        // TODO: determine support for general Credentials
        String username = null;
        char[] password = EMPTY_PASSWORD;
        if (credentials != null && credentials instanceof SimpleCredentials)
        {
            username = ((SimpleCredentials)credentials).getUserID();
            password = ((SimpleCredentials)credentials).getPassword();
        }

        try
        {
            // construct the session
            SessionImpl sessionImpl = new SessionImpl(this);
            
            // authenticate user
            AuthenticationService authenticationService = getServiceRegistry().getAuthenticationService();
            try
            {
                authenticationService.authenticate(username, password);
            }
            catch(AuthenticationException e)
            {
                throw new LoginException("Alfresco Repository failed to authenticate credentials", e);
            }
            
            // initialise the session
            String ticket = authenticationService.getCurrentTicket();
            String sessionWorkspace = (workspaceName == null) ? defaultWorkspace : workspaceName;
            sessionImpl.init(ticket, sessionWorkspace, getAttributes(credentials));

            // session is now ready
            Session session = sessionImpl.getProxy();
            registerSession(sessionImpl);
            return session;
        }
        catch(AlfrescoRuntimeException e)
        {
            throw new RepositoryException(e);
        }
    }

    /* (non-Javadoc)
     * @see javax.jcr.Repository#login(javax.jcr.Credentials)
     */
    public Session login(Credentials credentials)
        throws LoginException, RepositoryException
    {
        return login(credentials, null);
    }

    /* (non-Javadoc)
     * @see javax.jcr.Repository#login(java.lang.String)
     */
    public Session login(String workspaceName)
        throws LoginException, NoSuchWorkspaceException, RepositoryException
    {
        return login(null, workspaceName);
    }

    /* (non-Javadoc)
     * @see javax.jcr.Repository#login()
     */
    public Session login()
        throws LoginException, RepositoryException
    {
        return login(null, null);
    }

    /**
     * Get attributes from passed Credentials
     * 
     * @param credentials  the credentials to extract attribute from
     * @return  the attributes
     */
    private Map<String, Object> getAttributes(Credentials credentials)
    {
        Map<String, Object> attributes = null;
        if (credentials != null && credentials instanceof SimpleCredentials)
        {
            SimpleCredentials simpleCredentials = (SimpleCredentials)credentials;
            String[] names = simpleCredentials.getAttributeNames();
            attributes = new HashMap<String, Object>(names.length);
            for (String name : names)
            {
                attributes.put(name, simpleCredentials.getAttribute(name));
            }
        }
        return attributes;
    }
    
    /**
     * Register active session
     * 
     * @param session
     */
    private void registerSession(SessionImpl session)
        throws RepositoryException
    {
        // only allow one active session
        if (sessions.get() != null)
        {
            throw new RepositoryException("Only one active session is allowed per thread.");
        }
        
        // record session in current thread
        sessions.set(session);
    }

    /**
     * De-register current active session
     */
    public void deregisterSession()
    {
        // remove session from current thread
        sessions.set(null);
    }
    
}
