/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.security.authority;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.permissions.impl.AclDaoComponent;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.NoSuchPersonException;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.EqualsHelper;
import org.alfresco.util.SearchLanguageConversion;

public class AuthorityDAOImpl implements AuthorityDAO, NodeServicePolicies.BeforeDeleteNodePolicy, NodeServicePolicies.OnUpdatePropertiesPolicy
{
    private StoreRef storeRef;

    private NodeService nodeService;

    private NamespacePrefixResolver namespacePrefixResolver;

    private QName qnameAssocSystem;

    private QName qnameAssocAuthorities;

    private QName qnameAssocZones;

    private DictionaryService dictionaryService;

    private PersonService personService;
    
    private TenantService tenantService;
    
    private SimpleCache<CacheKey, Set<String>> authorityLookupCache;
    
    /** System Container ref cache (Tennant aware) */
    private Map<String, NodeRef> systemContainerRefs = new ConcurrentHashMap<String, NodeRef>(4);
    
    private AclDaoComponent aclDao;

    private PolicyComponent policyComponent;


    public AuthorityDAOImpl()
    {
        super();
    }

    public void setStoreUrl(String storeUrl)
    {
        this.storeRef = new StoreRef(storeUrl);
    }

    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    public void setNamespacePrefixResolver(NamespacePrefixResolver namespacePrefixResolver)
    {
        this.namespacePrefixResolver = namespacePrefixResolver;
        qnameAssocSystem = QName.createQName("sys", "system", namespacePrefixResolver);
        qnameAssocAuthorities = QName.createQName("sys", "authorities", namespacePrefixResolver);
        qnameAssocZones = QName.createQName("sys", "zones", namespacePrefixResolver);
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setUserToAuthorityCache(SimpleCache<CacheKey, Set<String>> userToAuthorityCache)
    {
        this.authorityLookupCache = userToAuthorityCache;
    }

    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }
    
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }
    
    public void setAclDao(AclDaoComponent aclDao)
    {
        this.aclDao = aclDao;
    }

    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }

    public boolean authorityExists(String name)
    {
        NodeRef ref = getAuthorityOrNull(name);
        return ref != null;
    }

    public void addAuthority(Collection<String> parentNames, String childName)
    {
        Set<NodeRef> parentRefs = new HashSet<NodeRef>(parentNames.size() * 2);
        AuthorityType authorityType = AuthorityType.getAuthorityType(childName);
        boolean notUserOrGroup = !authorityType.equals(AuthorityType.USER) && !authorityType.equals(AuthorityType.GROUP);
        for (String parentName : parentNames)
        {
            NodeRef parentRef = getAuthorityOrNull(parentName);
            if (parentRef == null)
            {
                throw new UnknownAuthorityException("An authority was not found for " + parentName);
            }
            if (notUserOrGroup
                    && !(authorityType.equals(AuthorityType.ROLE) && AuthorityType.getAuthorityType(parentName).equals(
                            AuthorityType.ROLE)))
            {
                throw new AlfrescoRuntimeException("Authorities of the type " + authorityType
                        + " may not be added to other authorities");
            }
            parentRefs.add(parentRef);
        }
        NodeRef childRef = getAuthorityOrNull(childName);
        if (childRef == null)
        {
            throw new UnknownAuthorityException("An authority was not found for " + childName);
        }
        nodeService.addChild(parentRefs, childRef, ContentModel.ASSOC_MEMBER, QName.createQName("cm", childName,
                namespacePrefixResolver));
        authorityLookupCache.clear();
    }

    public void createAuthority(String name, String authorityDisplayName, Set<String> authorityZones)
    {
        HashMap<QName, Serializable> props = new HashMap<QName, Serializable>();
        props.put(ContentModel.PROP_AUTHORITY_NAME, name);
        props.put(ContentModel.PROP_AUTHORITY_DISPLAY_NAME, authorityDisplayName);
        NodeRef childRef;
        NodeRef authorityContainerRef = getAuthorityContainer();
        childRef = nodeService.createNode(authorityContainerRef, ContentModel.ASSOC_CHILDREN, QName.createQName("cm", name, namespacePrefixResolver),
                ContentModel.TYPE_AUTHORITY_CONTAINER, props).getChildRef();
        if (authorityZones != null)
        {
            Set<NodeRef> zoneRefs = new HashSet<NodeRef>(authorityZones.size() * 2);
            for (String authorityZone : authorityZones)
            {
                zoneRefs.add(getOrCreateZone(authorityZone));
            }
            nodeService.addChild(zoneRefs, childRef, ContentModel.ASSOC_IN_ZONE, QName.createQName("cm", name, namespacePrefixResolver));
        }
        authorityLookupCache.clear();
    }

    public void deleteAuthority(String name)
    {
        NodeRef nodeRef = getAuthorityOrNull(name);
        if (nodeRef == null)
        {
            throw new UnknownAuthorityException("An authority was not found for " + name);
        }
        nodeService.deleteNode(nodeRef);
        authorityLookupCache.clear();
    }

    public Set<String> getAllRootAuthorities(AuthorityType type)
    {
        return getAllRootAuthoritiesUnderContainer(getAuthorityContainer(), type);
    }

    public Set<String> getAllAuthorities(AuthorityType type)
    {
        return findAuthorities(type, null, null);
    }

    public Set<String> findAuthorities(AuthorityType type, String namePattern, Set<String> zones)
    {
        Pattern pattern = null;
        if (namePattern != null)
        {
            String regExpString = SearchLanguageConversion.convert(SearchLanguageConversion.DEF_LUCENE, SearchLanguageConversion.DEF_REGEX, namePattern);
            pattern = Pattern.compile(regExpString, Pattern.CASE_INSENSITIVE);
        }
        HashSet<String> authorities = new HashSet<String>();

        // If users are included, we use the person service to determine the complete set of names
        if (type == null || type.equals(AuthorityType.USER))
        {
            for (NodeRef nodeRef : personService.getAllPeople())
            {
                addAuthorityNameIfMatches(authorities, DefaultTypeConverter.INSTANCE.convert(String.class, nodeService.getProperty(nodeRef, ContentModel.PROP_USERNAME)), type,
                        pattern);
            }
        }

        // For other types, we just look directly under the authority container
        if (type == null || !type.equals(AuthorityType.USER))
        {
            if (zones == null)
            {
                NodeRef container = getAuthorityContainer();
                if (container != null)
                {
                    for (ChildAssociationRef childRef : nodeService.getChildAssocs(container,
                            ContentModel.ASSOC_CHILDREN, RegexQNamePattern.MATCH_ALL))
                    {
                        addAuthorityNameIfMatches(authorities, childRef.getQName().getLocalName(), type, pattern);
                    }
                }
            }
            else
            {
                for (String zone : zones)
                {
                    NodeRef container = getZone(zone);
                    if (container != null)
                    {
                        if (container != null)
                        {
                            for (ChildAssociationRef childRef : nodeService.getChildAssocs(container,
                                    ContentModel.ASSOC_IN_ZONE, RegexQNamePattern.MATCH_ALL))
                            {
                                addAuthorityNameIfMatches(authorities, childRef.getQName().getLocalName(), type,
                                        pattern);
                            }
                        }
                    }
                }
            }
        }
        return authorities;
    }

    public Set<String> getContainedAuthorities(AuthorityType type, String name, boolean immediate)
    {
        if (AuthorityType.getAuthorityType(name).equals(AuthorityType.USER))
        {
            return Collections.<String> emptySet();
        }
        else
        {
            NodeRef nodeRef = getAuthorityOrNull(name);
            if (nodeRef == null)
            {
                throw new UnknownAuthorityException("An authority was not found for " + name);
            }

            CacheKey key = new CacheKey(type, name, tenantService.getCurrentUserDomain(), false, !immediate);

            Set<String> authorities = authorityLookupCache.get(key);
            if (authorities == null)
            {
                authorities = new HashSet<String>(64);
                findAuthorities(type, null, nodeRef, authorities, false, !immediate, false);
                authorities = (Set<String>)Collections.unmodifiableSet(authorities);
                authorityLookupCache.put(key, authorities);
            }
            return authorities;
        }
    }

    public void removeAuthority(String parentName, String childName)
    {
        NodeRef parentRef = getAuthorityOrNull(parentName);
        if (parentRef == null)
        {
            throw new UnknownAuthorityException("An authority was not found for " + parentName);
        }
        NodeRef childRef = getAuthorityOrNull(childName);
        if (childRef == null)
        {
            throw new UnknownAuthorityException("An authority was not found for " + childName);
        }
        nodeService.removeChild(parentRef, childRef);
        authorityLookupCache.clear();
    }

    public Set<String> getContainingAuthorities(AuthorityType type, String name, boolean immediate)
    {
        CacheKey key = new CacheKey(type, name, tenantService.getCurrentUserDomain(), true, !immediate);

        Set<String> authorities = authorityLookupCache.get(key);
        if (authorities == null)
        {
            authorities = new HashSet<String>(64);
            findAuthorities(type, name, authorities, true, !immediate);
            authorities = (Set<String>)Collections.unmodifiableSet(authorities);
            authorityLookupCache.put(key, authorities);
        }
        return authorities;
    }

    private void addAuthorityNameIfMatches(Set<String> authorities, String authorityName, AuthorityType type, Pattern pattern)
    {
        if (type == null || AuthorityType.getAuthorityType(authorityName).equals(type))
        {
            if (pattern == null)
            {
                authorities.add(authorityName);
            }
            else
            {
                Matcher m = pattern.matcher(authorityName);
                if (m.matches())
                {
                    authorities.add(authorityName);
                }
            }
        }
    }

    private void findAuthorities(AuthorityType type, String name, Set<String> authorities, boolean parents, boolean recursive)
    {
        AuthorityType localType = AuthorityType.getAuthorityType(name);
        if (localType.equals(AuthorityType.GUEST))
        {
            // Nothing to do
        }
        else
        {
            NodeRef ref = getAuthorityOrNull(name);

            if (ref != null)
            {
                findAuthorities(type, null, ref, authorities, parents, recursive, false);
            }
            else if (!localType.equals(AuthorityType.USER))
            {
                // Don't worry about missing person objects. It might be the system user or a user yet to be
                // auto-created
                throw new UnknownAuthorityException("An authority was not found for " + name);
            }
        }
    }

    private void findAuthorities(AuthorityType type, Pattern pattern, NodeRef nodeRef, Set<String> authorities, boolean parents, boolean recursive, boolean includeNode)
    {
        QName currentType = nodeService.getType(nodeRef);
        boolean isAuthority = dictionaryService.isSubClass(currentType, ContentModel.TYPE_AUTHORITY);

        if (includeNode && isAuthority)
        {
            String authorityName = DefaultTypeConverter.INSTANCE.convert(String.class, nodeService.getProperty(nodeRef, dictionaryService.isSubClass(currentType,
                    ContentModel.TYPE_AUTHORITY_CONTAINER) ? ContentModel.PROP_AUTHORITY_NAME : ContentModel.PROP_USERNAME));
            addAuthorityNameIfMatches(authorities, authorityName, type, pattern);
        }

        // Loop over children if we want immediate children or are in recursive mode
        if (!includeNode || (recursive && isAuthority))
        {
            List<ChildAssociationRef> cars = parents ? nodeService.getParentAssocs(nodeRef, ContentModel.ASSOC_MEMBER, RegexQNamePattern.MATCH_ALL) : nodeService
                    .getChildAssocs(nodeRef);

            for (ChildAssociationRef car : cars)
            {
                NodeRef current = parents ? car.getParentRef() : car.getChildRef();
                findAuthorities(type, pattern, current, authorities, parents, recursive, true);
            }
        }
    }

    private NodeRef getAuthorityOrNull(String name)
    {
        try
        {
            if (AuthorityType.getAuthorityType(name).equals(AuthorityType.USER))
            {
                return personService.getPerson(name, false);
            }
            else if (AuthorityType.getAuthorityType(name).equals(AuthorityType.GUEST))
            {
                return personService.getPerson(name, false);
            }
            else if (AuthorityType.getAuthorityType(name).equals(AuthorityType.ADMIN))
            {
                return personService.getPerson(name, false);
            }
            else
            {
                List<ChildAssociationRef> results = nodeService.getChildAssocs(getAuthorityContainer(),
                        ContentModel.ASSOC_CHILDREN, QName.createQName("cm", name, namespacePrefixResolver));
                return results.isEmpty() ? null : results.get(0).getChildRef();
            }
        }
        catch (NoSuchPersonException e)
        {
            return null;
        }
    }

    /**
     * @return Returns the authority container, <b>which must exist</b>
     */
    private NodeRef getAuthorityContainer()
    {
        return getSystemContainer(qnameAssocAuthorities);
    }

    /**
     * @return Returns the zone container, <b>which must exist</b>
     */
    private NodeRef getZoneContainer()
    {
        return getSystemContainer(qnameAssocZones);
    }

    /**
     * Return the system container for the specified assoc name.
     * The containers are cached in a thread safe Tenant aware cache.
     *
     * @param assocQName
     *
     * @return System container, <b>which must exist</b>
     */
    private NodeRef getSystemContainer(QName assocQName)
    {
        final String cacheKey = tenantService.getCurrentUserDomain() + assocQName.toString();
        NodeRef systemContainerRef = systemContainerRefs.get(cacheKey);
        if (systemContainerRef == null)
        {
            NodeRef rootNodeRef = nodeService.getRootNode(this.storeRef);
            List<ChildAssociationRef> results = nodeService.getChildAssocs(rootNodeRef, RegexQNamePattern.MATCH_ALL, qnameAssocSystem);
            if (results.size() == 0)
            {
                throw new AlfrescoRuntimeException("Required system path not found: " + qnameAssocSystem);
            }
            NodeRef sysNodeRef = results.get(0).getChildRef();
            results = nodeService.getChildAssocs(sysNodeRef, RegexQNamePattern.MATCH_ALL, assocQName);
            if (results.size() == 0)
            {
                throw new AlfrescoRuntimeException("Required path not found: " + assocQName);
            }
            systemContainerRef = results.get(0).getChildRef();
            systemContainerRefs.put(cacheKey, systemContainerRef);
        }
        return systemContainerRef;
    }

    public NodeRef getAuthorityNodeRefOrNull(String name)
    {
        return getAuthorityOrNull(name);
    }

    public String getAuthorityName(NodeRef authorityRef)
    {
        String name = null;
        if (nodeService.exists(authorityRef))
        {
            QName type = nodeService.getType(authorityRef);
            if (type.equals(ContentModel.TYPE_AUTHORITY_CONTAINER))
            {
                name = (String) nodeService.getProperty(authorityRef, ContentModel.PROP_AUTHORITY_NAME);
            }
            else if (type.equals(ContentModel.TYPE_PERSON))
            {
                name = (String) nodeService.getProperty(authorityRef, ContentModel.PROP_USERNAME);
            }
        }
        return name;
    }

    public String getAuthorityDisplayName(String authorityName)
    {
        NodeRef ref = getAuthorityOrNull(authorityName);
        if (ref == null)
        {
            return null;
        }
        Serializable value = nodeService.getProperty(ref, ContentModel.PROP_AUTHORITY_DISPLAY_NAME);
        if (value == null)
        {
            return null;
        }
        return DefaultTypeConverter.INSTANCE.convert(String.class, value);
    }

    public void setAuthorityDisplayName(String authorityName, String authorityDisplayName)
    {
        NodeRef ref = getAuthorityOrNull(authorityName);
        if (ref == null)
        {
            return;
        }
        nodeService.setProperty(ref, ContentModel.PROP_AUTHORITY_DISPLAY_NAME, authorityDisplayName);

    }

    public NodeRef getOrCreateZone(String zoneName)
    {
        return getOrCreateZone(zoneName, true);
    }

    private NodeRef getOrCreateZone(String zoneName, boolean create)
    {
        NodeRef zoneContainerRef = getZoneContainer();
        QName zoneQName = QName.createQName("cm", zoneName, namespacePrefixResolver);
        List<ChildAssociationRef> results = nodeService.getChildAssocs(zoneContainerRef, ContentModel.ASSOC_CHILDREN, zoneQName);
        if (results.isEmpty())
        {
            if (create)
            {
                HashMap<QName, Serializable> props = new HashMap<QName, Serializable>();
                props.put(ContentModel.PROP_NAME, zoneName);
                return nodeService.createNode(zoneContainerRef, ContentModel.ASSOC_CHILDREN, zoneQName, ContentModel.TYPE_ZONE, props).getChildRef();
            }
            else
            {
                return null;
            }
        }
        else
        {
            return results.get(0).getChildRef();
        }
    }

    public NodeRef getZone(String zoneName)
    {
        return getOrCreateZone(zoneName, false);
    }

    public Set<String> getAuthorityZones(String name)
    {
        HashSet<String> zones = new HashSet<String>();
        NodeRef childRef = getAuthorityOrNull(name);
        if (childRef == null)
        {
            return null;
        }
        List<ChildAssociationRef> results = nodeService.getParentAssocs(childRef, ContentModel.ASSOC_IN_ZONE, RegexQNamePattern.MATCH_ALL);
        if (results.isEmpty())
        {
            return zones;
        }

        for (ChildAssociationRef current : results)
        {
            NodeRef zoneRef = current.getParentRef();
            Serializable value = nodeService.getProperty(zoneRef, ContentModel.PROP_NAME);
            if (value == null)
            {
                continue;
            }
            else
            {
                String zone = DefaultTypeConverter.INSTANCE.convert(String.class, value);
                zones.add(zone);
            }
        }
        return zones;
    }

    public Set<String> getAllAuthoritiesInZone(String zoneName, AuthorityType type)
    {
        HashSet<String> authorities = new HashSet<String>();
        NodeRef zoneRef = getZone(zoneName);
        if (zoneRef != null)
        {
            for (ChildAssociationRef childRef : nodeService.getChildAssocs(zoneRef, ContentModel.ASSOC_IN_ZONE, RegexQNamePattern.MATCH_ALL))
            {
                addAuthorityNameIfMatches(authorities, childRef.getQName().getLocalName(), type, null);
            }
        }
        return authorities;
    }

    public void addAuthorityToZones(String authorityName, Set<String> zones)
    {
        if ((zones != null) && (zones.size() > 0))
        {
            Set<NodeRef> zoneRefs = new HashSet<NodeRef>(zones.size() * 2);
            for (String authorityZone : zones)
            {
                zoneRefs.add(getOrCreateZone(authorityZone));
            }
            NodeRef authRef = getAuthorityOrNull(authorityName);
            if (authRef != null)
            {
                nodeService.addChild(zoneRefs, authRef, ContentModel.ASSOC_IN_ZONE, QName.createQName("cm", authorityName, namespacePrefixResolver));
            }
        }
    }

    public void removeAuthorityFromZones(String authorityName, Set<String> zones)
    {
        if ((zones != null) && (zones.size() > 0))
        {
            NodeRef authRef = getAuthorityOrNull(authorityName);
            List<ChildAssociationRef> results = nodeService.getParentAssocs(authRef, ContentModel.ASSOC_IN_ZONE, RegexQNamePattern.MATCH_ALL);
            for (ChildAssociationRef current : results)
            {
                NodeRef zoneRef = current.getParentRef();
                Serializable value = nodeService.getProperty(zoneRef, ContentModel.PROP_NAME);
                if (value == null)
                {
                    continue;
                }
                else
                {
                    String testZone = DefaultTypeConverter.INSTANCE.convert(String.class, value);
                    if (zones.contains(testZone))
                    {
                        nodeService.removeChildAssociation(current);
                    }
                }
            }
        }
    }
    
    public Set<String> getAllRootAuthoritiesInZone(String zoneName, AuthorityType type)
    {
        NodeRef zone = getZone(zoneName);
        return zone == null ? Collections.<String> emptySet() : getAllRootAuthoritiesUnderContainer(zone, type);
    }
    
    private Set<String> getAllRootAuthoritiesUnderContainer(NodeRef container, AuthorityType type)
    {
        if (type != null && type.equals(AuthorityType.USER))
        {
            return Collections.<String> emptySet();
        }        
        Collection<ChildAssociationRef> childRefs = nodeService.getChildAssocsWithoutParentAssocsOfType(container, ContentModel.ASSOC_MEMBER);
        Set<String> authorities = new HashSet<String>(childRefs.size() << 1);
        for (ChildAssociationRef childRef : childRefs)
        {
            addAuthorityNameIfMatches(authorities, childRef.getQName().getLocalName(), type, null);
        }
        return authorities;        
    }

    
    /**
     * CacheKey class for getContainedAuthorities() parent Group cache.
     */
    private final static class CacheKey implements Serializable
    {
        private static final long serialVersionUID = -3787608436067567757L;

        AuthorityType type;

        String name;

        String tenantDomain;

        boolean parents;

        boolean recursive;

        CacheKey(AuthorityType type, String name, String tenantDomain, boolean parents, boolean recursive)
        {
            this.type = type;
            this.name = name;
            this.tenantDomain = (tenantDomain == null ? TenantService.DEFAULT_DOMAIN : tenantDomain);
            this.parents = parents;
            this.recursive = recursive;
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            result = prime * result + ((tenantDomain == null) ? 0 : tenantDomain.hashCode());
            result = prime * result + (parents ? 1231 : 1237);
            result = prime * result + (recursive ? 1231 : 1237);
            result = prime * result + ((type == null) ? 0 : type.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            final CacheKey other = (CacheKey) obj;
            if (name == null)
            {
                if (other.name != null)
                    return false;
            }
            else if (!name.equals(other.name))
                return false;
            else if (!tenantDomain.equals(other.tenantDomain))
                return false;
            if (parents != other.parents)
                return false;
            if (recursive != other.recursive)
                return false;
            if (type == null)
            {
                if (other.type != null)
                    return false;
            }
            else if (!type.equals(other.type))
                return false;
            return true;
        }
    }

    public void beforeDeleteNode(NodeRef nodeRef)
    {
        authorityLookupCache.clear();
    }

    public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after)
    {
        String authBefore = DefaultTypeConverter.INSTANCE.convert(String.class, before.get(ContentModel.PROP_AUTHORITY_NAME));
        String authAfter = DefaultTypeConverter.INSTANCE.convert(String.class, after.get(ContentModel.PROP_AUTHORITY_NAME));
        if (!EqualsHelper.nullSafeEquals(authBefore, authAfter))
        {
            if ((authBefore == null) || authBefore.equalsIgnoreCase(authAfter))
            {
                // Fix any ACLs
                aclDao.updateAuthority(authBefore, authAfter);
                // Fix primary association local name
                // This will need to lower case in 3.2+
                QName newAssocQName = QName.createQName("cm", authAfter, namespacePrefixResolver);
                ChildAssociationRef assoc = nodeService.getPrimaryParent(nodeRef);
                nodeService.moveNode(nodeRef, assoc.getParentRef(), assoc.getTypeQName(), newAssocQName);
                // Cache is out of date
                authorityLookupCache.clear();
            }
            else
            {
                throw new UnsupportedOperationException("The name of an authority can not be changed");
            }
        }
    }

    public void init()
    {
        this.policyComponent.bindClassBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "beforeDeleteNode"), ContentModel.TYPE_AUTHORITY_CONTAINER, new JavaBehaviour(
                this, "beforeDeleteNode"));
        this.policyComponent.bindClassBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "onUpdateProperties"), ContentModel.TYPE_AUTHORITY_CONTAINER, new JavaBehaviour(
                this, "onUpdateProperties"));
    }
}
