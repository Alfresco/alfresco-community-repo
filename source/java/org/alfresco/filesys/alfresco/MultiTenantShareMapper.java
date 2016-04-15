/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
package org.alfresco.filesys.alfresco;

import java.util.Enumeration;
import java.util.Hashtable;

import org.springframework.extensions.config.ConfigElement;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.filesys.AlfrescoConfigSection;
import org.alfresco.filesys.repo.ContentContext;
import org.alfresco.jlan.debug.Debug;
import org.alfresco.jlan.server.SrvSession;
import org.alfresco.jlan.server.auth.InvalidUserException;
import org.alfresco.jlan.server.config.ConfigId;
import org.alfresco.jlan.server.config.ConfigurationListener;
import org.alfresco.jlan.server.config.InvalidConfigurationException;
import org.alfresco.jlan.server.config.ServerConfiguration;
import org.alfresco.jlan.server.core.ShareMapper;
import org.alfresco.jlan.server.core.ShareType;
import org.alfresco.jlan.server.core.SharedDevice;
import org.alfresco.jlan.server.core.SharedDeviceList;
import org.alfresco.jlan.server.filesys.DiskInterface;
import org.alfresco.jlan.server.filesys.DiskSharedDevice;
import org.alfresco.jlan.server.filesys.FilesystemsConfigSection;
import org.alfresco.jlan.server.filesys.SrvDiskInfo;
import org.alfresco.jlan.server.filesys.quota.QuotaManager;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.util.PropertyCheck;
import org.springframework.beans.factory.InitializingBean;
import org.alfresco.filesys.config.ServerConfigurationBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Multi Tenant Share Mapper Class
 * 
 * @author gkspencer
 */
public class MultiTenantShareMapper implements ShareMapper, ConfigurationListener, InitializingBean {

	//	Server configuration and configuration sections
    private static final Log logger = LogFactory.getLog(MultiTenantShareMapper.class);
	
	private ServerConfiguration m_config;
	
	private FilesystemsConfigSection m_filesysConfig;
	private AlfrescoConfigSection m_alfrescoConfig;
	
	//  Share name for multi-tenant connections
	
	private String m_tenantShareName;
	
	//  Store name and root path 
	
	private String m_rootPath;
	private String m_storeName;
	
	//  Table of tenant share lists indexed by tenant domain
	
	private Hashtable<String, SharedDeviceList> m_tenantShareLists;
	
	//	Quota manager to use when creating multi-tenant shares
	
	private QuotaManager m_quotaManager;
	
	private DiskInterface repoDiskInterface;
		
	/**
	 * Default constructor
	 */
	public MultiTenantShareMapper() 
	{
	}
	
	public void init()
	{
	    PropertyCheck.mandatory(this, "ServerConfiguration", m_config);
	    PropertyCheck.mandatory(this, "Tenant share name", m_tenantShareName);
	    PropertyCheck.mandatory(this, "repoDiskInterface", getRepoDiskInterface());
	    PropertyCheck.mandatory(this, "Store name", m_storeName);
	    PropertyCheck.mandatory(this, "Root Path", m_rootPath);
	}
    
    /**
	 * Initialize the share mapper
	 * 
	 * @param config ServerConfiguration
	 * @param params ConfigElement
	 * @exception InvalidConfigurationException
	 */
	public void initializeMapper(ServerConfiguration config, ConfigElement params)
		throws InvalidConfigurationException 
	{
	    if(logger.isDebugEnabled())
	    {
	        logger.debug("initialiseMapper");
	    }
		
		//	Save the server configuration
		
	    setServerConfiguration(config);
		
		// Complete initialization
		afterPropertiesSet();
	}

	public void afterPropertiesSet()
    {
        //  Filesystem configuration will usually be initialized after the security configuration so we need to plug in
        //  a listener to initialize it later
    
        m_filesysConfig  = (FilesystemsConfigSection) m_config.getConfigSection( FilesystemsConfigSection.SectionName);

        //  Get the Alfresco configuration section
        
        m_alfrescoConfig = (AlfrescoConfigSection) m_config.getConfigSection( AlfrescoConfigSection.SectionName);

        if ( m_filesysConfig == null || m_alfrescoConfig == null)
            m_config.addListener( this);
        
        // Create the tenant share lists table
        
        m_tenantShareLists = new Hashtable<String, SharedDeviceList>();
    }
	
	/**
	 * Find a share using the name and type for the specified client.
	 * 
	 * @param host String
	 * @param name String
	 * @param typ int
	 * @param sess SrvSession
	 * @param create boolean
	 * @return SharedDevice or null if no matching device was found
	 * @exception InvalidUserException
	 */
	public SharedDevice findShare(String host, String name, int typ, SrvSession sess, boolean create)
		throws InvalidUserException {
		
		//  Check if this is a tenant user
		
		if ( m_alfrescoConfig.getTenantService().isEnabled() && 
		        m_alfrescoConfig.getTenantService().isTenantUser() &&
		        typ != ShareType.ADMINPIPE)
		{
			return findTenantShare(host, name, typ, sess, create);
		}
		
		//	Find the required share by name/type. Use a case sensitive search first, if that fails use a case
		//	insensitive search.
		
		SharedDevice share = m_filesysConfig.getShares().findShare(name, typ, false);
		
		if ( share == null) {
			
			//	Try a case insensitive search for the required share
			
			share = m_filesysConfig.getShares().findShare(name, typ, true);
		}
		
		//	Check if the share is available
		
		if ( share != null && share.getContext() != null && share.getContext().isAvailable() == false)
		    share = null;
		
		//	Return the shared device, or null if no matching device was found
		
		return share;
	}

	/**
	 * Delete temporary shares for the specified session
	 * 
	 * @param sess SrvSession
	 */
	public void deleteShares(SrvSession sess) {

		//	Check if the session has any dynamic shares
		
		if ( sess.hasDynamicShares() == false)
			return;
			
		//	Delete the dynamic shares
		
		SharedDeviceList shares = sess.getDynamicShareList();
		Enumeration<SharedDevice> enm = shares.enumerateShares();
		
		while ( enm.hasMoreElements()) {

			//	Get the current share from the list
			
			SharedDevice shr = enm.nextElement();
			
			//	Close the shared device
			
			shr.getContext().CloseContext();
		}
	}
	
	/**
	 * Return the list of available shares.
	 * 
	 * @param host String
	 * @param sess SrvSession
	 * @param allShares boolean
	 * @return SharedDeviceList
	 */
	public SharedDeviceList getShareList(String host, SrvSession sess, boolean allShares) {

		//  Check that the filesystems configuration is valid
    
		if ( m_filesysConfig == null)
			return null;
    
		//  Check if this is a tenant user
		
		if ( m_alfrescoConfig.getTenantService().isEnabled() && m_alfrescoConfig.getTenantService().isTenantUser())
		{
			return getTenantShareList(host, sess, allShares);
		}
		
		//	Make a copy of the global share list and add the per session dynamic shares
		
		SharedDeviceList shrList = new SharedDeviceList( m_filesysConfig.getShares());
		
		if ( sess != null && sess.hasDynamicShares()) 
		{	
			//	Add the per session dynamic shares	
			shrList.addShares(sess.getDynamicShareList());
		}
		  
		//	Remove unavailable shares from the list and return the list

		if ( allShares == false)
		{
		    shrList.removeUnavailableShares();
		}
		return shrList;
	}
	
	/**
	 * Close the share mapper, release any resources.
	 */
	public void closeMapper() {
		
		// Close all the tenant shares
		
		// TODO:
	}

	/**
	 * Configuration changed
	 * 
	 * @param id int
	 * @param config Serverconfiguration
	 * @param newVal Object
	 * @return int
	 * @throws InvalidConfigurationException
	 */
	public int configurationChanged(int id, ServerConfiguration config, Object newVal)
		throws InvalidConfigurationException {
	
		// Check if the filesystems configuration section has been added
			    
		if ( id == ConfigId.ConfigSection) {
	      
			// Check if the section added is the filesystems config

			if (newVal instanceof FilesystemsConfigSection)
				m_filesysConfig = (FilesystemsConfigSection) newVal;

			// Or the Alfresco config

			else if (newVal instanceof AlfrescoConfigSection)
				m_alfrescoConfig = (AlfrescoConfigSection) newVal;

			// Return a dummy status

			return ConfigurationListener.StsAccepted;
		}

		
		// Return a dummy status

		return ConfigurationListener.StsIgnored;
	}
  
	/**
	 * Find a share for a tenant
	 * 
	 * @param host String
	 * @param name String
	 * @param typ int
	 * @param sess SrvSession
	 * @param create boolean
	 * @return SharedDevice
	 * @exception InvalidUserException
	 */
	private final SharedDevice findTenantShare(String host, String name, int typ, SrvSession sess, boolean create)
	    throws InvalidUserException {

		// Get the share list for the tenant
		
		SharedDeviceList shareList = getTenantShareList(host, sess, true);
		if ( shareList == null)
		{    
			return null;
		}
		
		// Search for the required share in the list of shares for the tennant
		
		return shareList.findShare( name, typ, true);
	}
	
	/**
	 * Return the list of available shares for a particular tenant
	 * 
	 * @param host String
	 * @param sess SrvSession
	 * @param allShares boolean
	 * @return SharedDeviceList
	 */
	private final SharedDeviceList getTenantShareList(String host, SrvSession sess, boolean allShares) {
		
		// Get the tenant user domain
		
		String tenantDomain = m_alfrescoConfig.getTenantService().getCurrentUserDomain();

		// Get the share list for the current domain

		SharedDeviceList shareList = null;
		
		if(m_tenantShareLists.containsKey(tenantDomain))
		{
		    shareList = m_tenantShareLists.get( tenantDomain);
		}
		
		synchronized ( m_tenantShareLists)
		{
			// Get the tenant specific share list

			shareList = m_tenantShareLists.get( tenantDomain);
		
			if ( shareList == null)
			{
				// Create the tenant specific share list
				
				shareList = new SharedDeviceList();
				
				// Create a tenant specific share for this domain
				
				shareList.addShare( createTenantShare(tenantDomain));
				
				// Store the list for use by other members of this domain
				
				m_tenantShareLists.put( tenantDomain, shareList);
			}
		}
		
		// Return the tenant specific share list
		
		return shareList;
	}
	
	/**
	 * Create a tenant domain specific share
	 */
	private final DiskSharedDevice createTenantShare(String tenantDomain)
	{
	    logger.debug("create tenant share for domain " + tenantDomain);
        StoreRef storeRef = new StoreRef(getStoreName());
        NodeRef rootNodeRef = new NodeRef(storeRef.getProtocol(), storeRef.getIdentifier(), "dummy"); 
        
        // Root nodeRef is required for storeRef part
        
        rootNodeRef = m_alfrescoConfig.getTenantService().getRootNode(
                m_alfrescoConfig.getNodeService(), 
                m_alfrescoConfig.getSearchService(),
        		m_alfrescoConfig.getNamespaceService(), 
        		getRootPath(), 
        		rootNodeRef);

        //  Create the disk driver and context

        DiskInterface diskDrv = getRepoDiskInterface();
        ContentContext diskCtx = new ContentContext(m_tenantShareName, getStoreName(), getRootPath(), rootNodeRef);
        
        // Set a quota manager for the share, if enabled
        
        if ( m_quotaManager != null)
        {
        	diskCtx.setQuotaManager( m_quotaManager);
        }
        
        if(m_config instanceof ServerConfigurationBean)
        {
            ServerConfigurationBean config = (ServerConfigurationBean)m_config;
            
            config.initialiseRuntimeContext("cifs.tenant." + tenantDomain, diskCtx);
        }
        else
        {
            throw new AlfrescoRuntimeException("configuration error, unknown configuration bean");
        }
        
        //  Default the filesystem to look like an 80Gb sized disk with 90% free space

        diskCtx.setDiskInformation(new SrvDiskInfo(2560, 64, 512, 2304));

        //  Create a temporary shared device for the user to access the tenant company home directory

        return new DiskSharedDevice(m_tenantShareName, diskDrv, diskCtx);
	}
	
	public void setServerConfiguration(ServerConfiguration config)
	{
	    this.m_config = config;
	}
	    
	public void setTenantShareName(String shareName)
	{
	    m_tenantShareName = shareName;
	}

	/**
	 * Set the quota manager to be used by multi-tenant shares
	 * 
	 * @param quotaManager QuotaManager
	 */
	public void setQuotaManager( QuotaManager quotaManager) 
	{
	    m_quotaManager = quotaManager;
	}
	
    public void setRootPath(String m_rootPath)
    {
        this.m_rootPath = m_rootPath;
    }

    public String getRootPath()
    {
        return m_rootPath;
    }

    public void setStoreName(String m_storeName)
    {
        this.m_storeName = m_storeName;
    }

    public String getStoreName()
    {
        return m_storeName;
    }

    public void setRepoDiskInterface(DiskInterface repoDiskInterface)
    {
        this.repoDiskInterface = repoDiskInterface;
    }

    public DiskInterface getRepoDiskInterface()
    {
        return repoDiskInterface;
    }
}
