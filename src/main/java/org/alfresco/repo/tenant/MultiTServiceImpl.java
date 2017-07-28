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
package org.alfresco.repo.tenant;

import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.domain.tenant.TenantAdminDAO;
import org.alfresco.repo.domain.tenant.TenantEntity;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;
import org.springframework.extensions.surf.util.I18NUtil;

/*
 * MT Service implementation
 *
 * Adapts names to be tenant specific or vice-versa, if MT is enabled (otherwise NOOP).
 *
 * author janv
 * since 3.0
 */
public class MultiTServiceImpl implements TenantService
{
    private TenantAdminDAO tenantAdminDAO;

    public void setTenantAdminDAO(TenantAdminDAO tenantAdminDAO)
    {
        this.tenantAdminDAO = tenantAdminDAO;
    }

    @Override
    public NodeRef getName(NodeRef nodeRef)
    {
        if (nodeRef == null)
        {
            return null;
        }

        return new NodeRef(nodeRef.getStoreRef().getProtocol(), getName(nodeRef.getStoreRef().getIdentifier()), nodeRef.getId());
    }

    @Override
    public NodeRef getName(NodeRef inNodeRef, NodeRef nodeRef)
    {
        if (inNodeRef == null || nodeRef == null)
        {
            return null;
        }

        int idx = inNodeRef.getStoreRef().getIdentifier().lastIndexOf(SEPARATOR);
        if (idx != -1)
        {
            String tenantDomain = inNodeRef.getStoreRef().getIdentifier().substring(1, idx);
            return new NodeRef(nodeRef.getStoreRef().getProtocol(), getName(nodeRef.getStoreRef().getIdentifier(), tenantDomain), nodeRef.getId());
        }

        return nodeRef;
    }

    @Override
    public StoreRef getName(StoreRef storeRef)
    {
        if (storeRef == null)
        {
            return null;
        }

        return new StoreRef(storeRef.getProtocol(), getName(storeRef.getIdentifier()));
    }

    @Override
    public ChildAssociationRef getName(ChildAssociationRef childAssocRef)
    {
        if (childAssocRef == null)
        {
            return null;
        }

        return new ChildAssociationRef(
                childAssocRef.getTypeQName(),
                getName(childAssocRef.getParentRef()),
                childAssocRef.getQName(),
                getName(childAssocRef.getChildRef()),
                childAssocRef.isPrimary(),
                childAssocRef.getNthSibling());
    }

    @Override
    public AssociationRef getName(AssociationRef assocRef)
    {
        if (assocRef == null)
        {
            return null;
        }

        return new AssociationRef(assocRef.getId(),
                getName(assocRef.getSourceRef()),
                assocRef.getTypeQName(),
                getName(assocRef.getTargetRef()));
    }

    @Override
    public StoreRef getName(String username, StoreRef storeRef)
    {
        if (storeRef == null)
        {
            return null;
        }

        if ((username != null) && (AuthenticationUtil.isMtEnabled()))
        {
            int idx = username.lastIndexOf(SEPARATOR);
            if ((idx > 0) && (idx < (username.length() - 1)))
            {
                String tenantDomain = username.substring(idx + 1);
                return new StoreRef(storeRef.getProtocol(), getName(storeRef.getIdentifier(), tenantDomain));
            }
        }

        return storeRef;
    }

    protected StoreRef getName(StoreRef storeRef, String tenantDomain, boolean checkTenantEnabled)
    {
        if (storeRef == null)
        {
            return null;
        }
        if (tenantDomain != null)
        {
            storeRef = new StoreRef(storeRef.getProtocol(), getName(storeRef.getIdentifier(), tenantDomain, checkTenantEnabled));
        }

        return storeRef;
    }

    protected String getName(String name, String tenantDomain)
    {
        return getName(name, tenantDomain, true);
    }

    protected String getName(String name, String tenantDomain, boolean checkTenantEnabled)
    {
        ParameterCheck.mandatory("tenantDomain", tenantDomain);

        if (name == null)
        {
            return null;
        }

        if (checkTenantEnabled)
        {
            checkTenantEnabled(tenantDomain);
        }

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
            if (!tenantDomain.equalsIgnoreCase(nameDomain))
            {
                throw new AlfrescoRuntimeException("domain mismatch: expected = " + tenantDomain + ", actual = " + nameDomain);
            }
        }

        return name;
    }

    @Override
    public QName getName(QName name)
    {
        String tenantDomain = getCurrentUserDomain();

        if (!tenantDomain.equals(DEFAULT_DOMAIN))
        {
            checkTenantEnabled(tenantDomain);
            name = getName(name, tenantDomain);
        }

        return name;
    }

    @Override
    public QName getName(NodeRef inNodeRef, QName name)
    {
        ParameterCheck.mandatory("InNodeRef", inNodeRef);

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
        if (name == null)
        {
            return null;
        }

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
            if (!tenantDomain.equalsIgnoreCase(nameDomain))
            {
                throw new AlfrescoRuntimeException("domain mismatch: expected = " + tenantDomain + ", actual = " + nameDomain);
            }
        }

        return name;
    }

    @Override
    public String getName(String name)
    {
        if (name == null)
        {
            return null;
        }

        String tenantDomain = getCurrentUserDomain();

        if (!tenantDomain.equals(DEFAULT_DOMAIN))
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
                if (!tenantDomain.equalsIgnoreCase(nameDomain))
                {
                    throw new AlfrescoRuntimeException("domain mismatch: expected = " + tenantDomain + ", actual = " + nameDomain);
                }
            }
        }

        return name;
    }

    @Override
    public QName getBaseName(QName name, boolean forceForNonTenant)
    {
        String baseNamespaceURI = getBaseName(name.getNamespaceURI(), forceForNonTenant);
        return QName.createQName(baseNamespaceURI, name.getLocalName());
    }

    @Override
    public NodeRef getBaseName(NodeRef nodeRef)
    {
        return getBaseName(nodeRef, false);
    }

    @Override
    public NodeRef getBaseName(NodeRef nodeRef, boolean forceForNonTenant)
    {
        if (nodeRef == null)
        {
            return null;
        }
        return new NodeRef(nodeRef.getStoreRef().getProtocol(), getBaseName(nodeRef.getStoreRef().getIdentifier(), forceForNonTenant), nodeRef.getId());
    }

    @Override
    public StoreRef getBaseName(StoreRef storeRef)
    {
        if (storeRef == null)
        {
            return null;
        }

        return new StoreRef(storeRef.getProtocol(), getBaseName(storeRef.getIdentifier()));
    }

    @Override
    public ChildAssociationRef getBaseName(ChildAssociationRef childAssocRef)
    {
        return getBaseName(childAssocRef, false);
    }

    @Override
    public ChildAssociationRef getBaseName(ChildAssociationRef childAssocRef, boolean forceForNonTenant)
    {
        if (childAssocRef == null)
        {
            return null;
        }

        return new ChildAssociationRef(
                childAssocRef.getTypeQName(),
                getBaseName(childAssocRef.getParentRef(), forceForNonTenant),
                childAssocRef.getQName(),
                getBaseName(childAssocRef.getChildRef(), forceForNonTenant),
                childAssocRef.isPrimary(),
                childAssocRef.getNthSibling());
    }

    @Override
    public AssociationRef getBaseName(AssociationRef assocRef)
    {
        if (assocRef == null)
        {
            return null;
        }

        return new AssociationRef(assocRef.getId(),
                getBaseName(assocRef.getSourceRef()),
                assocRef.getTypeQName(),
                getBaseName(assocRef.getTargetRef()));
    }

    @Override
    public String getBaseName(String name)
    {
        // get base name, but don't force for non-tenant user (e.g. super admin)
        return getBaseName(name, false);
    }

    @Override
    public String getBaseName(String name, boolean forceForNonTenant)
    {
        if (name == null)
        {
            return null;
        }

        String tenantDomain = getCurrentUserDomain();

        int idx1 = name.indexOf(SEPARATOR);
        if (idx1 == 0)
        {
            int idx2 = name.indexOf(SEPARATOR, 1);
            String nameDomain = name.substring(1, idx2);

            if ((!tenantDomain.equals(DEFAULT_DOMAIN)) && (!tenantDomain.equals(nameDomain)))
            {
                throw new AlfrescoRuntimeException("domain mismatch: expected = " + tenantDomain + ", actual = " + nameDomain);
            }

            if ((!tenantDomain.equals(DEFAULT_DOMAIN)) || (forceForNonTenant))
            {
                // remove tenant domain
                name = name.substring(idx2 + 1);
            }
        }

        return name;
    }

    public String getBaseNameUser(String name)
    {
        if (name == null || !isEnabled())
        {
            // Can be null (e.g. for System user / during app ctx init)
            // The name is the name and we don't care about the domain part
            return name;
        }
        // We only bother with MT username@domain format if MT is enabled
        int idx = name.lastIndexOf(SEPARATOR);
        if (idx != -1)
        {
           return name.substring(0, idx);
        }
        else
        {
            return name;
        }
    }

    @Override
    public void checkDomainUser(String username)
    {
        ParameterCheck.mandatory("Username", username);

        String tenantDomain = getCurrentUserDomain();

        if (!tenantDomain.equals(DEFAULT_DOMAIN))
        {
            int idx2 = username.lastIndexOf(SEPARATOR);
            if ((idx2 > 0) && (idx2 < (username.length() - 1)))
            {
                String tenantUserDomain = username.substring(idx2 + 1);

                if ((tenantUserDomain == null) || (!tenantDomain.equalsIgnoreCase(tenantUserDomain)))
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

    @Override
    public void checkDomain(String name)
    {
        if (name == null)
        {
            return;
        }

        String nameDomain = null;

        int idx1 = name.indexOf(SEPARATOR);
        if (idx1 == 0)
        {
            int idx2 = name.indexOf(SEPARATOR, 1);
            nameDomain = name.substring(1, idx2);
        }

        String tenantDomain = getCurrentUserDomain();

        if (((nameDomain == null) && (!tenantDomain.equals(DEFAULT_DOMAIN))) || ((nameDomain != null) && (!nameDomain.equals(tenantDomain))))
        {
            throw new AlfrescoRuntimeException("domain mismatch: expected = " + tenantDomain + ", actual = " + nameDomain);
        }
    }

    @Override
    public NodeRef getRootNode(NodeService nodeService, SearchService searchService, NamespaceService namespaceService, String rootPath, NodeRef rootNodeRef)
    {
        ParameterCheck.mandatory("NodeService", nodeService);
        ParameterCheck.mandatory("SearchService", searchService);
        ParameterCheck.mandatory("NamespaceService", namespaceService);
        ParameterCheck.mandatory("RootPath", rootPath);
        ParameterCheck.mandatory("RootNodeRef", rootNodeRef);

        // String username = AuthenticationUtil.getFullyAuthenticatedUser();
        StoreRef storeRef = rootNodeRef.getStoreRef();

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
            if (!nodeService.exists(storeRef))
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

    // TODO review usages (re: cloud external user => more than one domain)
    @Override
    public boolean isTenantUser()
    {
        // return isTenantUser(AuthenticationUtil.getRunAsUser());
        return (!getCurrentUserDomain().equals(TenantService.DEFAULT_DOMAIN));
    }

    // TODO review usages (re: cloud external user => more than one domain)
    @Override
    public boolean isTenantUser(String username)
    {
        // can be null (e.g. for System user / during app ctx init)
        if (username != null && AuthenticationUtil.isMtEnabled())
        {
            int idx = username.lastIndexOf(SEPARATOR);
            if ((idx > 0) && (idx < (username.length() - 1)))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isTenantName(String name)
    {
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

    // TODO review usages (re: cloud external user => more than one domain)
    @Override
    public String getUserDomain(String username)
    {
        // can be null (e.g. for System user / during app ctx init)
        if ((username != null) && AuthenticationUtil.isMtEnabled())
        {
            int idx = username.lastIndexOf(SEPARATOR);
            if ((idx > 0) && (idx < (username.length() - 1)))
            {
                String tenantDomain = getTenantDomain(username.substring(idx + 1));
                checkTenantEnabled(tenantDomain);

                return tenantDomain;
            }
        }

        return DEFAULT_DOMAIN; // default domain - non-tenant user
    }

    /**
     * Get the primary domain for the given user, if a tenant for that domain exists.
     * 
     * For user names of the form "user@tenantdomain", the tenant domain the part of the string 
     * after the @ symbol. A check is then made to see if tenant with that domain name exists.  
     * If it does, then the identified domain is returned. If no tenant exists then null is 
     * returned.
     * 
     * If the username does not end with a domain, as described above, then the default domain is 
     * returned. 
     */
    @Override
    public String getPrimaryDomain(String username)
    {
        String result = null;
        // can be null (e.g. for System user / during app ctx init)
        if (username != null && AuthenticationUtil.isMtEnabled())
        {
            int idx = username.lastIndexOf(SEPARATOR);
            if ((idx > 0) && (idx < (username.length() - 1)))
            {
                String tenantDomain = getTenantDomain(username.substring(idx + 1));

                if (getTenant(tenantDomain) != null)
                {
                    result = tenantDomain;
                }
            }
            else
            {
                result = DEFAULT_DOMAIN;
            }
        }

        return result; // default domain - non-tenant user
    }

    public String getCurrentUserDomain()
    {
        String tenantDomain = TenantUtil.getCurrentDomain();
        // if (! tenantDomain.equals(TenantService.DEFAULT_DOMAIN))
        // {
        // checkTenantEnabled(tenantDomain);
        // }
        return tenantDomain;
    }

    @Override
    public String getDomain(String name)
    {
        return getDomain(name, false);
    }

    @Override
    public String getDomain(String name, boolean checkCurrentDomain)
    {
        ParameterCheck.mandatory("name", name);

        String nameDomain = DEFAULT_DOMAIN;

        int idx1 = name.indexOf(SEPARATOR);
        if (idx1 == 0)
        {
            int idx2 = name.indexOf(SEPARATOR, 1);

            nameDomain = getTenantDomain(name.substring(1, idx2));

            if (checkCurrentDomain)
            {
                String tenantDomain = getCurrentUserDomain();

                if ((!tenantDomain.equals(DEFAULT_DOMAIN)) && (!tenantDomain.equals(nameDomain)))
                {
                    throw new AlfrescoRuntimeException("domain mismatch: expected = " + tenantDomain + ", actual = " + nameDomain);
                }
            }
        }

        return nameDomain;
    }

    /**
     * @return String
     */
    public static String getMultiTenantDomainName(String name)
    {
        // Check that all the passed values are not null
        ParameterCheck.mandatory("name", name);

        int idx1 = name.indexOf(SEPARATOR);
        if (idx1 == 0)
        {
            int idx2 = name.indexOf(SEPARATOR, 1);
            if (idx2 != -1)
            {
                return name.substring(1, idx2);
            }
        }
        return DEFAULT_DOMAIN;
    }

    public String getDomainUser(String baseUsername, String tenantDomain)
    {
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
        Tenant tenant = getTenant(tenantDomain);
        // note: System user can access disabled tenants
        if (tenant == null || !AuthenticationUtil.isRunAsUserTheSystemUser() && !tenant.isEnabled())
        {
            throw new TenantDisabledException(tenantDomain);
        }
    }

    @Override
    public Tenant getTenant(String tenantDomain)
    {
        TenantEntity tenantEntity = tenantAdminDAO.getTenant(tenantDomain);
        Tenant tenant = null;
        if (tenantEntity != null)
        {
            tenant = new Tenant(tenantEntity.getTenantDomain(), tenantEntity.getEnabled(), tenantEntity.getContentRoot(), null);
        }
        return tenant;
    }

    public boolean isEnabled()
    {
        return AuthenticationUtil.isMtEnabled();
    }

    private String getTenantDomain(String tenantDomain)
    {
        return tenantDomain.toLowerCase(I18NUtil.getLocale());
    }
}
