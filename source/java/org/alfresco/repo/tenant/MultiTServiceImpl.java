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
package org.alfresco.repo.tenant;

import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.surf.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/*
 * MT Service implementation
 *
 * Adapts names to be tenant specific or vice-versa.
 */
public class MultiTServiceImpl implements TenantService
{ 
    private static Log logger = LogFactory.getLog(MultiTServiceImpl.class);
    
    // clusterable cache of enabled/disabled tenants - managed via TenantAdmin Service
    private SimpleCache<String, Tenant> tenantsCache;  
    
    private MultiTAdminServiceImpl tenantAdminService = null; // registered (rather than injected) - to avoid circular dependency
    

    public void setTenantsCache(SimpleCache<String, Tenant> tenantsCache)
    {
        this.tenantsCache = tenantsCache;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.tenant.TenantService#getName(org.alfresco.service.cmr.repository.NodeRef)
     */
    public NodeRef getName(NodeRef nodeRef)
    {
        if (nodeRef == null) { return null; }

        return new NodeRef(nodeRef.getStoreRef().getProtocol(), getName(nodeRef.getStoreRef().getIdentifier()), nodeRef.getId());
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.tenant.TenantService#getName(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.repository.NodeRef)
     */
    public NodeRef getName(NodeRef inNodeRef, NodeRef nodeRef)
    {
        if (inNodeRef == null || nodeRef == null) { return null; }

        int idx = inNodeRef.getStoreRef().getIdentifier().lastIndexOf(SEPARATOR);
        if (idx != -1)
        {   
            String tenantDomain = inNodeRef.getStoreRef().getIdentifier().substring(1, idx);
            return new NodeRef(nodeRef.getStoreRef().getProtocol(), getName(nodeRef.getStoreRef().getIdentifier(), tenantDomain), nodeRef.getId());            
        }

        return nodeRef;       
    }    
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.tenant.TenantService#getName(org.alfresco.service.cmr.repository.StoreRef)
     */
    public StoreRef getName(StoreRef storeRef)
    {
        if (storeRef == null) { return null; }
        
        return new StoreRef(storeRef.getProtocol(), getName(storeRef.getIdentifier()));
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.tenant.TenantService#getName(org.alfresco.service.cmr.repository.ChildAssociationRef)
     */
    public ChildAssociationRef getName(ChildAssociationRef childAssocRef)
    {
        if (childAssocRef == null) { return null; }
        
        return new ChildAssociationRef(
                childAssocRef.getTypeQName(),
                getName(childAssocRef.getParentRef()),
                childAssocRef.getQName(),
                getName(childAssocRef.getChildRef()),
                childAssocRef.isPrimary(), 
                childAssocRef.getNthSibling());
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.tenant.TenantService#getName(org.alfresco.service.cmr.repository.AssociationRef)
     */
    public AssociationRef getName(AssociationRef assocRef)
    {
        if (assocRef == null) { return null; }
        
        return new AssociationRef(assocRef.getId(),
                getName(assocRef.getSourceRef()),
                assocRef.getTypeQName(),
                getName(assocRef.getTargetRef()));
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.tenant.TenantService#getName(java.lang.String, org.alfresco.service.cmr.repository.StoreRef)
     */
    public StoreRef getName(String username, StoreRef storeRef)
    {
        if (storeRef == null) { return null; }

        if (username != null) 
        {
            int idx = username.lastIndexOf(SEPARATOR);
            if ((idx > 0) && (idx < (username.length()-1)))
            {
                String tenantDomain = username.substring(idx+1);
                return new StoreRef(storeRef.getProtocol(), getName(storeRef.getIdentifier(), tenantDomain));
            }
        }

        return storeRef;
    }
  
    protected String getName(String name, String tenantDomain)
    {
        // Check that all the passed values are not null
        ParameterCheck.mandatory("name", name);
        ParameterCheck.mandatory("tenantDomain", tenantDomain);
                
        checkTenantEnabled(tenantDomain);
        
        int idx1 = name.indexOf(SEPARATOR);
        if (idx1 != 0)
        {
            // no domain, so add it as a prefix (between two domain separators)
            name = SEPARATOR + tenantDomain + SEPARATOR + name;
        }
        else
        {
            int idx2 = name.indexOf(SEPARATOR, 1);
            String nameDomain = name.substring(1, idx2);
            if (! tenantDomain.equals(nameDomain))
            {
                throw new AlfrescoRuntimeException("domain mismatch: expected = " + tenantDomain + ", actual = " + nameDomain);
            }
        }               

        return name;          
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.tenant.TenantService#getName(org.alfresco.service.namespace.QName)
     */
    public QName getName(QName name)
    {
        // Check that all the passed values are not null
        ParameterCheck.mandatory("Name", name);

        String tenantDomain = getCurrentUserDomain();
        
        if (! tenantDomain.equals(DEFAULT_DOMAIN))
        {
            checkTenantEnabled(tenantDomain);
            name = getName(name, tenantDomain);
        }
        
        return name;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.tenant.TenantService#getName(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
     */
    public QName getName(NodeRef inNodeRef, QName name)
    {
        // Check that all the passed values are not null
        ParameterCheck.mandatory("InNodeRef", inNodeRef);
        ParameterCheck.mandatory("Name", name);

        int idx = inNodeRef.getStoreRef().getIdentifier().lastIndexOf(SEPARATOR);
        if (idx != -1)
        {
            String tenantDomain = inNodeRef.getStoreRef().getIdentifier().substring(1, idx);
            checkTenantEnabled(tenantDomain);
            return getName(name, tenantDomain);
        }

        return name;       
        
    }

    private QName getName(QName name, String tenantDomain)
    {            
        String namespace = name.getNamespaceURI();
        int idx1 = namespace.indexOf(SEPARATOR);
        if (idx1 == -1)
        {
            // no domain, so add it as a prefix (between two domain separators)
            namespace = SEPARATOR + tenantDomain + SEPARATOR + namespace;
            name = QName.createQName(namespace, name.getLocalName());
        }
        else
        {
            int idx2 = namespace.indexOf(SEPARATOR, 1);
            String nameDomain = namespace.substring(1, idx2);
            if (! tenantDomain.equals(nameDomain))
            {
                throw new AlfrescoRuntimeException("domain mismatch: expected = " + tenantDomain + ", actual = " + nameDomain);
            }
        }

        return name;  
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.tenant.TenantService#getName(java.lang.String)
     */
    public String getName(String name)
    {
        // Check that all the passed values are not null
        ParameterCheck.mandatory("name", name);
        
        String tenantDomain = getCurrentUserDomain();
        
        if (! tenantDomain.equals(DEFAULT_DOMAIN))
        {
            int idx1 = name.indexOf(SEPARATOR);
            if (idx1 != 0)
            {
                // no tenant domain prefix, so add it
                name = SEPARATOR + tenantDomain + SEPARATOR + name;
            }
            else
            {
                int idx2 = name.indexOf(SEPARATOR, 1);
                String nameDomain = name.substring(1, idx2);
                if (! tenantDomain.equals(nameDomain))
                {
                    throw new AlfrescoRuntimeException("domain mismatch: expected = " + tenantDomain + ", actual = " + nameDomain);
                }
            }               
        }

        return name;          
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.tenant.TenantService#getBaseName(org.alfresco.service.namespace.QName, boolean)
     */
    public QName getBaseName(QName name, boolean forceForNonTenant)
    {  
        String baseNamespaceURI = getBaseName(name.getNamespaceURI(), forceForNonTenant);
        return QName.createQName(baseNamespaceURI, name.getLocalName());
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.tenant.TenantService#getBaseName(org.alfresco.service.cmr.repository.NodeRef)
     */
    public NodeRef getBaseName(NodeRef nodeRef)
    { 
        if (nodeRef == null) { return null; }

        return new NodeRef(nodeRef.getStoreRef().getProtocol(), getBaseName(nodeRef.getStoreRef().getIdentifier()), nodeRef.getId());
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.tenant.TenantService#getBaseName(org.alfresco.service.cmr.repository.StoreRef)
     */
    public StoreRef getBaseName(StoreRef storeRef)
    {         
        if (storeRef == null) { return null; }
        
        return new StoreRef(storeRef.getProtocol(), getBaseName(storeRef.getIdentifier()));
    } 
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.tenant.TenantService#getBaseName(org.alfresco.service.cmr.repository.ChildAssociationRef)
     */
    public ChildAssociationRef getBaseName(ChildAssociationRef childAssocRef)
    {
        if (childAssocRef == null) { return null; }
        
        return new ChildAssociationRef(
                childAssocRef.getTypeQName(),
                getBaseName(childAssocRef.getParentRef()),
                childAssocRef.getQName(),
                getBaseName(childAssocRef.getChildRef()),
                childAssocRef.isPrimary(), 
                childAssocRef.getNthSibling());
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.tenant.TenantService#getBaseName(org.alfresco.service.cmr.repository.AssociationRef)
     */
    public AssociationRef getBaseName(AssociationRef assocRef)
    {
        if (assocRef == null) { return null; }
        
        return new AssociationRef(assocRef.getId(),
                getBaseName(assocRef.getSourceRef()),
                assocRef.getTypeQName(),
                getBaseName(assocRef.getTargetRef()));
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.tenant.TenantService#getBaseName(java.lang.String)
     */
    public String getBaseName(String name)
    {  
        // get base name, but don't force for non-tenant user (e.g. super admin)
        return getBaseName(name, false);       
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.tenant.TenantService#getBaseName(java.lang.String, boolean)
     */
    public String getBaseName(String name, boolean forceForNonTenant)
    {   
        // Check that all the passed values are not null
        ParameterCheck.mandatory("name", name);

        String tenantDomain = getCurrentUserDomain();
               
        int idx1 = name.indexOf(SEPARATOR);
        if (idx1 == 0)
        {
            int idx2 = name.indexOf(SEPARATOR, 1);
            String nameDomain = name.substring(1, idx2);
            
            if ((! tenantDomain.equals(DEFAULT_DOMAIN)) && (! tenantDomain.equals(nameDomain)))
            {
                throw new AlfrescoRuntimeException("domain mismatch: expected = " + tenantDomain + ", actual = " + nameDomain);
            }
            
            if ((! tenantDomain.equals(DEFAULT_DOMAIN)) || (forceForNonTenant))
            {
                // remove tenant domain
                name = name.substring(idx2+1);
            }
        } 

        return name;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.tenant.TenantService#getBaseNameUser(java.lang.String)
     */
    public String getBaseNameUser(String name)
    {
        // can be null (e.g. for System user / during app ctx init)
        if (name != null) 
        {
            int idx = name.lastIndexOf(SEPARATOR);
            if (idx != -1)
            {
               return name.substring(0, idx);
            }
        }        
        return name;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.tenant.TenantService#checkDomainUser(java.lang.String)
     */
    public void checkDomainUser(String username)
    {   
        // Check that all the passed values are not null        
        ParameterCheck.mandatory("Username", username);
        
        String tenantDomain = getCurrentUserDomain();
          
         if (! tenantDomain.equals(DEFAULT_DOMAIN))
         {
            int idx2 = username.lastIndexOf(SEPARATOR);
            if ((idx2 > 0) && (idx2 < (username.length()-1)))
            {
                String tenantUserDomain = username.substring(idx2+1);
                
                if ((tenantUserDomain == null) || (! tenantDomain.equals(tenantUserDomain)))
                {
                    throw new TenantDomainMismatchException(tenantDomain, tenantUserDomain); 
                }
            }
            else
            {
                throw new TenantDomainMismatchException(tenantDomain, null); 
            }
        }
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.tenant.TenantService#checkDomain(java.lang.String)
     */
    public void checkDomain(String name)
    {       
        // Check that all the passed values are not null        
        ParameterCheck.mandatory("Name", name);
          
        String nameDomain = null;
        
        int idx1 = name.indexOf(SEPARATOR);
        if (idx1 == 0)
        {
            int idx2 = name.indexOf(SEPARATOR, 1);
            nameDomain = name.substring(1, idx2);
        }
                
        String tenantDomain = getCurrentUserDomain();
        
        if (((nameDomain == null) && (! tenantDomain.equals(DEFAULT_DOMAIN))) || 
            ((nameDomain != null) && (! nameDomain.equals(tenantDomain))))
        {
            throw new AlfrescoRuntimeException("domain mismatch: expected = " + tenantDomain + ", actual = " + nameDomain);
        }
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.tenant.TenantService#getRootNode(org.alfresco.service.cmr.repository.NodeService, org.alfresco.service.cmr.search.SearchService, org.alfresco.service.namespace.NamespaceService, java.lang.String, org.alfresco.service.cmr.repository.NodeRef)
     */
    public NodeRef getRootNode(NodeService nodeService, SearchService searchService, NamespaceService namespaceService, String rootPath, NodeRef rootNodeRef)
    {
        // Check that all the passed values are not null
        ParameterCheck.mandatory("NodeService", nodeService);
        ParameterCheck.mandatory("SearchService", searchService);  
        ParameterCheck.mandatory("NamespaceService", namespaceService);
        ParameterCheck.mandatory("RootPath", rootPath);  
        ParameterCheck.mandatory("RootNodeRef", rootNodeRef); 

        String username = AuthenticationUtil.getFullyAuthenticatedUser();
        StoreRef storeRef = getName(username, rootNodeRef.getStoreRef());
        
        AuthenticationUtil.RunAsWork<NodeRef> action = new GetRootNode(nodeService, searchService, namespaceService, rootPath, rootNodeRef, storeRef);
        return getBaseName(AuthenticationUtil.runAs(action, AuthenticationUtil.getSystemUserName()));
    }         
    
    private class GetRootNode implements AuthenticationUtil.RunAsWork<NodeRef>
    {
        NodeService nodeService;
        SearchService searchService;
        NamespaceService namespaceService;
        String rootPath;
        NodeRef rootNodeRef;
        StoreRef storeRef;

        GetRootNode(NodeService nodeService, SearchService searchService, NamespaceService namespaceService, String rootPath, NodeRef rootNodeRef, StoreRef storeRef)
        {
            this.nodeService = nodeService;
            this.searchService = searchService;
            this.namespaceService = namespaceService;
            this.rootPath = rootPath;
            this.rootNodeRef = rootNodeRef;
            this.storeRef = storeRef;
        }

        public NodeRef doWork() throws Exception
        {         
            // Get company home / root for the tenant domain
            // Do this as the System user in case the tenant user does not have permission

            // Connect to the repo and ensure that the store exists
            if (! nodeService.exists(storeRef))
            {
                throw new AlfrescoRuntimeException("Store not created prior to application startup: " + storeRef);
            }
            NodeRef storeRootNodeRef = nodeService.getRootNode(storeRef);

            // Find the root node for this device
            List<NodeRef> nodeRefs = searchService.selectNodes(storeRootNodeRef, rootPath, null, namespaceService, false);

            if (nodeRefs.size() > 1)
            {
                throw new AlfrescoRuntimeException("Multiple possible roots for device: \n" +
                        "   root path: " + rootPath + "\n" +
                        "   results: " + nodeRefs);
            }
            else if (nodeRefs.size() == 0)
            {
                // nothing found
                throw new AlfrescoRuntimeException("No root found for device: \n" +
                        "   root path: " + rootPath);
            }
            else
            {
                // we found a node
                rootNodeRef = nodeRefs.get(0);
            }   
            
            return rootNodeRef;
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.tenant.TenantService#isTenantUser()
     */
    public boolean isTenantUser()
    {
        return isTenantUser(AuthenticationUtil.getRunAsUser());
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.tenant.TenantService#isTenantUser(java.lang.String)
     */
    public boolean isTenantUser(String username)
    {    
        // can be null (e.g. for System user / during app ctx init)
        if (username != null) {
            int idx = username.lastIndexOf(SEPARATOR);
            if ((idx > 0) && (idx < (username.length()-1)))
            {
               return true;
            }
        }        
        return false;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.tenant.TenantService#isTenantName(java.lang.String)
     */
    public boolean isTenantName(String name)
    {
        // Check that all the passed values are not null
        ParameterCheck.mandatory("name", name);
        
        int idx1 = name.indexOf(SEPARATOR);
        if (idx1 == 0)
        {
            int idx2 = name.indexOf(SEPARATOR, 1);
            if (idx2 != -1)
            {
                return true;
            }
        }

        return false;  
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.tenant.TenantService#getUserDomain(java.lang.String)
     */
    public String getUserDomain(String username)
    {
    	// can be null (e.g. for System user / during app ctx init)
        if (username != null) 
        {
            int idx = username.lastIndexOf(SEPARATOR);
            if ((idx > 0) && (idx < (username.length()-1)))
            {
               String tenantDomain = getTenantDomain(username.substring(idx+1));
               checkTenantEnabled(tenantDomain);
               
               return tenantDomain;
            }
        }        
        
        return DEFAULT_DOMAIN; // default domain - non-tenant user
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.tenant.TenantUserService#getCurrentUserDomain()
     */
    public String getCurrentUserDomain()
    {
    	String user = AuthenticationUtil.getRunAsUser();
        return getUserDomain(user);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.tenant.TenantUserService#getDomain(java.lang.String)
     */
    public String getDomain(String name)
    {
        // Check that all the passed values are not null
        ParameterCheck.mandatory("name", name);

    	name = getTenantDomain(name);
        String tenantDomain = getCurrentUserDomain();
        
        String nameDomain = DEFAULT_DOMAIN;
        
        int idx1 = name.indexOf(SEPARATOR);
        if (idx1 == 0)
        {
            int idx2 = name.indexOf(SEPARATOR, 1);
            nameDomain = name.substring(1, idx2);
            
            if ((! tenantDomain.equals(DEFAULT_DOMAIN)) && (! tenantDomain.equals(nameDomain)))
            {
                throw new AlfrescoRuntimeException("domain mismatch: expected = " + tenantDomain + ", actual = " + nameDomain);
            }    
        } 

        return nameDomain;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.tenant.TenantUserService#getDomainUser(java.lang.String, java.lang.String)
     */
    public String getDomainUser(String baseUsername, String tenantDomain)
    {
        // Check that all the passed values are not null
        ParameterCheck.mandatory("baseUsername", baseUsername);
        
        if ((tenantDomain == null) || (tenantDomain.equals(DEFAULT_DOMAIN)))
        {
        	return baseUsername;
        }
        else
        {
            if (baseUsername.contains(SEPARATOR))
            {
                throw new AlfrescoRuntimeException("Invalid base username: " + baseUsername);
            }
            
            if (tenantDomain.contains(SEPARATOR))
            {
                throw new AlfrescoRuntimeException("Invalid tenant domain: " + tenantDomain);
            }
        
            tenantDomain = getTenantDomain(tenantDomain);
            return baseUsername + SEPARATOR + tenantDomain;
        }
    }
    
    protected void checkTenantEnabled(String tenantDomain)
    {
        // note: System user can access disabled tenants
        if (!AuthenticationUtil.isRunAsUserTheSystemUser() && !(getTenant(tenantDomain).isEnabled()))
        {
            throw new AlfrescoRuntimeException("Tenant is not enabled: " + tenantDomain);
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.tenant.TenantService#getTenant(java.lang.String)
     */
    public Tenant getTenant(String tenantDomain)
    {
    	tenantDomain = getTenantDomain(tenantDomain);
        Tenant tenant = tenantsCache.get(tenantDomain);
        if (tenant == null)
        {
            // backed by TenantAdminService - update this cache, e.g. could have been invalidated and/or expired
            if (tenantAdminService != null)
            {    
                tenant = tenantAdminService.getTenant(tenantDomain);
                if (tenant == null)
                {
                    throw new AlfrescoRuntimeException("No such tenant " + tenantDomain);
                }
                else
                {
                    putTenant(tenantDomain, tenant);
                }
            }
        }
        
        return tenant;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.tenant.TenantUserService#isEnabled()
     */
    public boolean isEnabled()
    {
        return true;
    }
    
    // should only be called by Tenant Admin Service
    protected void register(MultiTAdminServiceImpl tenantAdminService)
    {
        this.tenantAdminService = tenantAdminService;
    }
    
    // should only be called by Tenant Admin Service
    protected void putTenant(String tenantDomain, Tenant tenant)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("putTenant " + tenantDomain);
        }
        tenantsCache.put(tenantDomain, tenant);
    }
    
    // should only be called by Tenant Admin Service
    protected void removeTenant(String tenantDomain)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("removeTenant " + tenantDomain);
        }
        tenantsCache.remove(tenantDomain);
    }

    protected String getTenantDomain(String tenantDomain)
    {
        ParameterCheck.mandatory("tenantDomain", tenantDomain);
        return tenantDomain.toLowerCase(I18NUtil.getLocale());
    }

}
