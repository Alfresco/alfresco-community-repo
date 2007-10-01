/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.security.authority;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.search.impl.lucene.QueryParser;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.ISO9075;

public class AuthorityDAOImpl implements AuthorityDAO
{
    public static final StoreRef STOREREF_USERS = new StoreRef("user", "alfrescoUserStore");

    private NodeService nodeService;

    private NamespacePrefixResolver namespacePrefixResolver;

    private QName qnameAssocSystem;

    private QName qnameAssocAuthorities;

    private SearchService searchService;

    private DictionaryService dictionaryService;

    private SimpleCache<String, HashSet<String>> userToAuthorityCache;

    public AuthorityDAOImpl()
    {
        super();
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
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    public void setUserToAuthorityCache(SimpleCache<String, HashSet<String>> userToAuthorityCache)
    {
        this.userToAuthorityCache = userToAuthorityCache;
    }

    public boolean authorityExists(String name)
    {
        NodeRef ref = getAuthorityOrNull(name);
        return ref != null;
    }

    public void addAuthority(String parentName, String childName)
    {
        NodeRef parentRef = getAuthorityOrNull(parentName);
        if (parentRef == null)
        {
            throw new UnknownAuthorityException("An authority was not found for " + parentName);
        }
        if (AuthorityType.getAuthorityType(childName).equals(AuthorityType.USER))
        {
            Collection<String> memberCollection = DefaultTypeConverter.INSTANCE.getCollection(String.class, nodeService
                    .getProperty(parentRef, ContentModel.PROP_MEMBERS));
            HashSet<String> members = new HashSet<String>();
            members.addAll(memberCollection);
            members.add(childName);
            nodeService.setProperty(parentRef, ContentModel.PROP_MEMBERS, members);
            userToAuthorityCache.remove(childName);
        }
        else if (AuthorityType.getAuthorityType(childName).equals(AuthorityType.GROUP))
        {
            NodeRef childRef = getAuthorityOrNull(childName);
            if (childRef == null)
            {
                throw new UnknownAuthorityException("An authority was not found for " + childName);
            }
            nodeService.addChild(parentRef, childRef, ContentModel.ASSOC_MEMBER, QName.createQName("usr", childName,
                    namespacePrefixResolver));
            userToAuthorityCache.clear();
        }
        else
        {
            throw new AlfrescoRuntimeException("Authorities of the type "
                    + AuthorityType.getAuthorityType(childName) + " may not be added to other authorities");
        }
    }

    public void createAuthority(String parentName, String name)
    {
        HashMap<QName, Serializable> props = new HashMap<QName, Serializable>();
        props.put(ContentModel.PROP_AUTHORITY_NAME, name);
        if (parentName != null)
        {
            NodeRef parentRef = getAuthorityOrNull(parentName);
            if (parentRef == null)
            {
                throw new UnknownAuthorityException("An authority was not found for " + parentName);
            }
            nodeService.createNode(parentRef, ContentModel.ASSOC_MEMBER, QName.createQName("usr", name,
                    namespacePrefixResolver), ContentModel.TYPE_AUTHORITY_CONTAINER, props);
        }
        else
        {
            NodeRef authorityContainerRef = getAuthorityContainer();
            nodeService.createNode(authorityContainerRef, ContentModel.ASSOC_CHILDREN, QName.createQName("usr", name,
                    namespacePrefixResolver), ContentModel.TYPE_AUTHORITY_CONTAINER, props);
        }
    }

    public void deleteAuthority(String name)
    {
        NodeRef nodeRef = getAuthorityOrNull(name);
        if (nodeRef == null)
        {
            throw new UnknownAuthorityException("An authority was not found for " + name);
        }
        nodeService.deleteNode(nodeRef);
        userToAuthorityCache.clear();
    }

    public Set<String> getAllRootAuthorities(AuthorityType type)
    {
        HashSet<String> authorities = new HashSet<String>();
        NodeRef container = getAuthorityContainer();
        if (container != null)
        {
            findAuthorities(type, container, authorities, false, false, false);
        }
        return authorities;
    }

    public Set<String> getAllAuthorities(AuthorityType type)
    {
        HashSet<String> authorities = new HashSet<String>();
        NodeRef container = getAuthorityContainer();
        if (container != null)
        {
            findAuthorities(type, container, authorities, false, true, false);
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
            HashSet<String> authorities = new HashSet<String>();
            findAuthorities(type, nodeRef, authorities, false, !immediate, false);
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
        if (AuthorityType.getAuthorityType(childName).equals(AuthorityType.USER))
        {
            Collection<String> memberCollection = DefaultTypeConverter.INSTANCE.getCollection(String.class, nodeService
                    .getProperty(parentRef, ContentModel.PROP_MEMBERS));
            HashSet<String> members = new HashSet<String>();
            members.addAll(memberCollection);
            members.remove(childName);
            nodeService.setProperty(parentRef, ContentModel.PROP_MEMBERS, members);
            userToAuthorityCache.remove(childName);
        }
        else
        {
            NodeRef childRef = getAuthorityOrNull(childName);
            if (childRef == null)
            {
                throw new UnknownAuthorityException("An authority was not found for " + childName);
            }
            nodeService.removeChild(parentRef, childRef);
            userToAuthorityCache.clear();
        }
    }

    public Set<String> getContainingAuthorities(AuthorityType type, String name, boolean immediate)
    {
        if (AuthorityType.getAuthorityType(name).equals(AuthorityType.USER) && !immediate && (type == null))
        {
            // Cache user to authority look ups
            HashSet<String> authorities = userToAuthorityCache.get(name);
            if (authorities == null)
            {
                authorities = new HashSet<String>();
                findAuthorities(type, name, authorities, true, !immediate);
                userToAuthorityCache.put(name, authorities);
            }
            return authorities;
        }
        else
        {
            HashSet<String> authorities = new HashSet<String>();
            findAuthorities(type, name, authorities, true, !immediate);
            return authorities;
        }
    }

    private void findAuthorities(AuthorityType type, String name, Set<String> authorities, boolean parents,
            boolean recursive)
    {
        if (AuthorityType.getAuthorityType(name).equals(AuthorityType.GUEST))
        {
            // Nothing to do
        }
        else if (AuthorityType.getAuthorityType(name).equals(AuthorityType.USER))
        {
            if (parents)
            {
                for (NodeRef ref : getUserContainers(name))
                {
                    if (recursive)
                    {
                        findAuthorities(type, ref, authorities, parents, recursive, true);
                    }
                    else
                    {
                        String authorityName = DefaultTypeConverter.INSTANCE.convert(String.class, nodeService
                                .getProperty(ref, ContentModel.PROP_AUTHORITY_NAME));
                        if (type == null)
                        {
                            authorities.add(authorityName);
                        }
                        else
                        {
                            AuthorityType authorityType = AuthorityType.getAuthorityType(authorityName);
                            if (authorityType.equals(type))
                            {
                                authorities.add(authorityName);
                            }
                        }
                    }
                }
            }
        }

        else
        {
            NodeRef ref = getAuthorityOrNull(name);

            if (ref == null)
            {
                throw new UnknownAuthorityException("An authority was not found for " + name);
            }

            findAuthorities(type, ref, authorities, parents, recursive, false);

        }
    }

    private ArrayList<NodeRef> getUserContainers(String name)
    {
        ArrayList<NodeRef> containers = findUserContainers(name);
        return containers;
    }

    private ArrayList<NodeRef> findUserContainers(String name)
    {
        SearchParameters sp = new SearchParameters();
        sp.addStore(STOREREF_USERS);
        sp.setLanguage("lucene");
        sp.setQuery("+TYPE:\""
                + ContentModel.TYPE_AUTHORITY_CONTAINER
                + "\""
                + " +@"
                + QueryParser.escape("{"
                        + ContentModel.PROP_MEMBERS.getNamespaceURI() + "}"
                        + ISO9075.encode(ContentModel.PROP_MEMBERS.getLocalName())) + ":\"" + name + "\"");
        ResultSet rs = null;
        try
        {
            rs = searchService.query(sp);
            ArrayList<NodeRef> answer = new ArrayList<NodeRef>(rs.length());
            for (ResultSetRow row : rs)
            {
                answer.add(row.getNodeRef());
            }
            return answer;
        }
        finally
        {
            if (rs != null)
            {
                rs.close();
            }
        }

    }

    private void findAuthorities(AuthorityType type, NodeRef nodeRef, Set<String> authorities, boolean parents,
            boolean recursive, boolean includeNode)
    {
        List<ChildAssociationRef> cars = parents ? nodeService.getParentAssocs(nodeRef) : nodeService
                .getChildAssocs(nodeRef);

        if (includeNode)
        {
            String authorityName = DefaultTypeConverter.INSTANCE.convert(String.class, nodeService.getProperty(nodeRef,
                    ContentModel.PROP_AUTHORITY_NAME));
            if (type == null)
            {
                authorities.add(authorityName);
            }
            else
            {
                AuthorityType authorityType = AuthorityType.getAuthorityType(authorityName);
                if (authorityType.equals(type))
                {
                    authorities.add(authorityName);
                }
            }
        }

        // Loop over children
        for (ChildAssociationRef car : cars)
        {
            NodeRef current = parents ? car.getParentRef() : car.getChildRef();
            QName currentType = nodeService.getType(current);
            if (dictionaryService.isSubClass(currentType, ContentModel.TYPE_AUTHORITY))
            {

                String authorityName = DefaultTypeConverter.INSTANCE.convert(String.class, nodeService.getProperty(
                        current, ContentModel.PROP_AUTHORITY_NAME));

                if (type == null)
                {
                    authorities.add(authorityName);
                    if (recursive)
                    {
                        findAuthorities(type, current, authorities, parents, recursive, false);
                    }
                }
                else
                {
                    AuthorityType authorityType = AuthorityType.getAuthorityType(authorityName);
                    if (authorityType.equals(type))
                    {
                        authorities.add(authorityName);
                    }
                    if (recursive)
                    {
                        findAuthorities(type, current, authorities, parents, recursive, false);
                    }
                }
            }
        }
        // loop over properties
        if (!parents)
        {
            Collection<String> members = DefaultTypeConverter.INSTANCE.getCollection(String.class, nodeService
                    .getProperty(nodeRef, ContentModel.PROP_MEMBERS));
            if (members != null)
            {
                for (String user : members)
                {
                    if (user != null)
                    {
                        if (type == null)
                        {
                            authorities.add(user);
                        }
                        else
                        {
                            AuthorityType authorityType = AuthorityType.getAuthorityType(user);
                            if (authorityType.equals(type))
                            {
                                authorities.add(user);
                            }
                        }
                    }
                }
            }
        }
    }

    private NodeRef getAuthorityOrNull(String name)
    {
        SearchParameters sp = new SearchParameters();
        sp.addStore(STOREREF_USERS);
        sp.setLanguage("lucene");
        sp.setQuery("+TYPE:\""
                + ContentModel.TYPE_AUTHORITY_CONTAINER
                + "\""
                + " +@"
                + QueryParser.escape("{"
                        + ContentModel.PROP_AUTHORITY_NAME.getNamespaceURI() + "}"
                        + ISO9075.encode(ContentModel.PROP_AUTHORITY_NAME.getLocalName())) + ":\"" + name + "\"");
        ResultSet rs = null;
        try
        {
            rs = searchService.query(sp);
            if (rs.length() == 0)
            {
                return null;
            }
            else
            {
                for (ResultSetRow row : rs)
                {
                    String test = DefaultTypeConverter.INSTANCE.convert(String.class, nodeService.getProperty(row
                            .getNodeRef(), ContentModel.PROP_AUTHORITY_NAME));
                    if (test.equals(name))
                    {
                        return row.getNodeRef();
                    }
                }
            }
            return null;
        }
        finally
        {
            if (rs != null)
            {
                rs.close();
            }
        }

    }

    /**
     * @return Returns the authority container, <b>which must exist</b>
     */
    private NodeRef getAuthorityContainer()
    {
        NodeRef rootNodeRef = nodeService.getRootNode(STOREREF_USERS);
        List<ChildAssociationRef> results = nodeService.getChildAssocs(rootNodeRef, RegexQNamePattern.MATCH_ALL,
                qnameAssocSystem);
        NodeRef sysNodeRef = null;
        if (results.size() == 0)
        {
            throw new AlfrescoRuntimeException("Required authority system path not found: " + qnameAssocSystem);
        }
        else
        {
            sysNodeRef = results.get(0).getChildRef();
        }
        results = nodeService.getChildAssocs(sysNodeRef, RegexQNamePattern.MATCH_ALL, qnameAssocAuthorities);
        NodeRef authNodeRef = null;
        if (results.size() == 0)
        {
            throw new AlfrescoRuntimeException("Required authority path not found: " + qnameAssocAuthorities);
        }
        else
        {
            authNodeRef = results.get(0).getChildRef();
        }
        return authNodeRef;
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
            else if (type.equals(ContentModel.TYPE_AUTHORITY))
            {
                name = (String) nodeService.getProperty(authorityRef, ContentModel.PROP_USER_USERNAME);
            }
        }
        return name;
    }

}
