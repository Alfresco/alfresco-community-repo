/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2020 Alfresco Software Limited
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
package org.alfresco.repo.event2;

import java.io.Serializable;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.event.v1.model.ContentInfo;
import org.alfresco.repo.event.v1.model.NodeResource;
import org.alfresco.repo.event.v1.model.UserInfo;
import org.alfresco.repo.event2.filter.EventFilterRegistry;
import org.alfresco.repo.event2.filter.NodeAspectFilter;
import org.alfresco.repo.event2.filter.NodePropertyFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.security.NoSuchPersonException;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PathUtil;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * Helper for {@link NodeResource} objects.
 *
 * @author Jamal Kaabi-Mofrad
 */
public class NodeResourceHelper implements InitializingBean
{
    private static final Log LOGGER = LogFactory.getLog(NodeResourceHelper.class);

    protected NodeService         nodeService;
    protected DictionaryService   dictionaryService;
    protected PersonService       personService;
    protected EventFilterRegistry eventFilterRegistry;
    protected NamespaceService    namespaceService;

    private NodeAspectFilter   nodeAspectFilter;
    private NodePropertyFilter nodePropertyFilter;

    @Override
    public void afterPropertiesSet() throws Exception
    {
        PropertyCheck.mandatory(this, "nodeService", nodeService);
        PropertyCheck.mandatory(this, "dictionaryService", dictionaryService);
        PropertyCheck.mandatory(this, "personService", personService);
        PropertyCheck.mandatory(this, "eventFilterRegistry", eventFilterRegistry);
        PropertyCheck.mandatory(this, "namespaceService", namespaceService);

        this.nodeAspectFilter = eventFilterRegistry.getNodeAspectFilter();
        this.nodePropertyFilter = eventFilterRegistry.getNodePropertyFilter();
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }

    // To make IntelliJ stop complaining about unused method!
    @SuppressWarnings("unused")
    public void setEventFilterRegistry(EventFilterRegistry eventFilterRegistry)
    {
        this.eventFilterRegistry = eventFilterRegistry;
    }

    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    public NodeResource.Builder createNodeResourceBuilder(NodeRef nodeRef)
    {
        final QName type = nodeService.getType(nodeRef);
        final Path path = nodeService.getPath(nodeRef);

        final Map<QName, Serializable> properties = getProperties(nodeRef);

        // minor: save one lookup if creator & modifier are the same
        Map<String, UserInfo> mapUserCache = new HashMap<>(2);

        return NodeResource.builder().setId(nodeRef.getId())
                           .setName((String) properties.get(ContentModel.PROP_NAME))
                           .setNodeType(getQNamePrefixString(type))
                           .setIsFile(isSubClass(type, ContentModel.TYPE_CONTENT))
                           .setIsFolder(isSubClass(type, ContentModel.TYPE_FOLDER))
                           .setCreatedByUser(getUserInfo((String) properties.get(ContentModel.PROP_CREATOR), mapUserCache))
                           .setCreatedAt(getZonedDateTime((Date)properties.get(ContentModel.PROP_CREATED)))
                           .setModifiedByUser(getUserInfo((String) properties.get(ContentModel.PROP_MODIFIER), mapUserCache))
                           .setModifiedAt(getZonedDateTime((Date)properties.get(ContentModel.PROP_MODIFIED)))
                           .setContent(getContentInfo(properties))
                           .setPrimaryHierarchy(PathUtil.getNodeIdsInReverse(path, false))
                           .setProperties(mapToNodeProperties(properties))
                           .setAspectNames(getMappedAspects(nodeRef));
    }

    private boolean isSubClass(QName className, QName ofClassQName)
    {
        return dictionaryService.isSubClass(className, ofClassQName);
    }

    private UserInfo getUserInfo(String userName, Map<String, UserInfo> mapUserCache)
    {
        UserInfo userInfo = mapUserCache.get(userName);
        if (userInfo == null)
        {
            userInfo = getUserInfo(userName);
            mapUserCache.put(userName, userInfo);
        }
        return userInfo;
    }

    public boolean nodeExists(NodeRef nodeRef)
    {
        return nodeService.exists(nodeRef);
    }

    public Map<String, Serializable> mapToNodeProperties(Map<QName, Serializable> props)
    {
        Map<String, Serializable> filteredProps = new HashMap<>(props.size());

        props.forEach((k, v) -> {
            if (!nodePropertyFilter.isExcluded(k) && v != null)
            {
                if (v instanceof MLText)
                {
                    //TODO - should we send all of the values if multiple locales exist?
                    v = ((MLText) v).getDefaultValue();
                }

                if (isNotEmptyString(v))
                {
                    filteredProps.put(getQNamePrefixString(k), v);
                }
            }
        });

        return filteredProps;
    }

    public ContentInfo getContentInfo(Map<QName, Serializable> props)
    {
        final Serializable content = props.get(ContentModel.PROP_CONTENT);
        ContentInfo contentInfo = null;
        if ((content instanceof ContentData))
        {
            ContentData cd = (ContentData) content;
            contentInfo = new ContentInfo(cd.getMimetype(), cd.getSize(), cd.getEncoding());
        }
        return contentInfo;
    }

    public UserInfo getUserInfo(String userName)
    {
        UserInfo userInfo = null;
        if (userName != null)
        {
            String sysUserName = AuthenticationUtil.getSystemUserName();
            if (userName.equals(sysUserName) || (AuthenticationUtil.isMtEnabled()
                        && userName.startsWith(sysUserName + "@")))
            {
                userInfo = new UserInfo(userName, userName, "");
            }
            else
            {
                PersonService.PersonInfo pInfo = null;
                try
                {
                    NodeRef pNodeRef = personService.getPersonOrNull(userName);
                    if (pNodeRef != null)
                    {
                        pInfo = personService.getPerson(pNodeRef);
                    }
                }
                catch (NoSuchPersonException | AccessDeniedException ex)
                {
                    // ignore
                }

                if (pInfo != null)
                {
                    userInfo = new UserInfo(userName, pInfo.getFirstName(), pInfo.getLastName());
                }
                else
                {
                    if (LOGGER.isDebugEnabled())
                    {
                        LOGGER.debug("Unknown person: " + userName);
                    }
                    userInfo = new UserInfo(userName, userName, "");
                }
            }
        }
        return userInfo;
    }

    public ZonedDateTime getZonedDateTime(Date date)
    {
        if (date == null)
        {
            return null;
        }
        return ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    /**
     * Returns the QName in the format prefix:local, but in the exceptional case where there is no registered prefix
     * returns it in the form {uri}local.
     *
     * @param   k QName
     * @return  a String representing the QName in the format prefix:local or {uri}local.
     */
    public String getQNamePrefixString(QName k)
    {
        String key;
        try
        {
            key = k.toPrefixString(namespaceService);
        }
        catch (NamespaceException e)
        {
            key = k.toString();
        }
        return key;
    }

    public Set<String> mapToNodeAspects(Collection<QName> aspects)
    {
        Set<String> filteredAspects = new HashSet<>(aspects.size());

        aspects.forEach(q -> {
            if (!nodeAspectFilter.isExcluded(q))
            {
                filteredAspects.add(getQNamePrefixString(q));
            }
        });

        return filteredAspects;
    }

    private boolean isNotEmptyString(Serializable ser)
    {
        return !(ser instanceof String) || !((String) ser).isEmpty();
    }

    public QName getNodeType(NodeRef nodeRef)
    {
       return nodeService.getType(nodeRef);
    }

    public Serializable getProperty(NodeRef nodeRef, QName qName)
    {
        return nodeService.getProperty(nodeRef, qName);
    }

    public Map<QName, Serializable> getProperties(NodeRef nodeRef)
    {
        return nodeService.getProperties(nodeRef);
    }

    public Set<String> getMappedAspects(NodeRef nodeRef)
    {
        return mapToNodeAspects(nodeService.getAspects(nodeRef));
    }
    
    public List<String> getPrimaryHierarchy(NodeRef nodeRef, boolean showLeaf)
    {
        final Path path = nodeService.getPath(nodeRef);
        return PathUtil.getNodeIdsInReverse(path, showLeaf);
    }
}
