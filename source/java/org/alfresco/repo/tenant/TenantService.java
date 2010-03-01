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

import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;


/**
 * Tenant Service interface.
 * <p>
 * This interface provides methods to support either ST or MT implementations.
 *
 */
public interface TenantService extends TenantUserService
{
    public static final String SEPARATOR = "@";
    
    public static final String DEFAULT_DOMAIN = "";
    
    /**
     * @return          the reference <b>with</b> the tenant-specific ID attached
     */
    public NodeRef getName(NodeRef nodeRef);

    /**
     * @return          the reference <b>with</b> the tenant-specific ID attached
     */
    public NodeRef getName(NodeRef inNodeRef, NodeRef nodeRef);
    
    /**
     * @return          the reference <b>with</b> the tenant-specific ID attached
     */
    public StoreRef getName(StoreRef storeRef);
    
    /**
     * @return          the reference <b>with</b> the tenant-specific ID attached
     */
    public ChildAssociationRef getName(ChildAssociationRef childAssocRef);
    
    /**
     * @return          the reference <b>with</b> the tenant-specific ID attached
     */
    public AssociationRef getName(AssociationRef assocRef);
    
    /**
     * @return          the reference <b>with</b> the tenant-specific ID attached
     */
    public StoreRef getName(String username, StoreRef storeRef);
    
    /**
     * @return          the reference <b>with</b> the tenant-specific ID attached
     */
    public QName getName(QName name);
    
    /**
     * @return          the reference <b>with</b> the tenant-specific ID attached
     */
    public QName getName(NodeRef inNodeRef, QName name);
    
    /**
     * @return          the reference <b>with</b> the tenant-specific ID attached
     */
    public String getName(String name);

    /**
     * @return          the reference <b>without</b> the tenant-specific ID attached
     */
    public QName getBaseName(QName name, boolean forceIfNonTenant);
    
    /**
     * @return          the reference <b>without</b> the tenant-specific ID attached
     */
    public NodeRef getBaseName(NodeRef nodeRef);
    
    /**
     * @return          the reference <b>without</b> the tenant-specific ID attached
     */
    public StoreRef getBaseName(StoreRef storeRef);
    
    /**
     * @return          the reference <b>without</b> the tenant-specific ID attached
     */
    public ChildAssociationRef getBaseName(ChildAssociationRef childAssocRef);
    
    /**
     * @return          the reference <b>without</b> the tenant-specific ID attached
     */
    public AssociationRef getBaseName(AssociationRef assocRef);
    
    /**
     * @return          the reference <b>without</b> the tenant-specific ID attached
     */
    public String getBaseName(String name);
    
    /**
     * @return          the reference <b>without</b> the tenant-specific ID attached
     */
    public String getBaseName(String name, boolean forceIfNonTenant);
    
    public void checkDomainUser(String username);
    
    public void checkDomain(String name);  

    public NodeRef getRootNode(NodeService nodeService, SearchService searchService, NamespaceService namespaceService, String rootPath, NodeRef rootNodeRef);

    public boolean isTenantUser();
    
    public boolean isTenantUser(String username);
    
    public boolean isTenantName(String name);

    public String getUserDomain(String username);

    public Tenant getTenant(String tenantDomain);
}
