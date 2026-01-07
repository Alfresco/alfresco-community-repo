/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
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

import static java.util.Optional.ofNullable;

import java.io.Serializable;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.google.common.collect.Sets;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.event.v1.model.ContentInfo;
import org.alfresco.repo.event.v1.model.NodeResource;
import org.alfresco.repo.event.v1.model.UserInfo;
import org.alfresco.repo.event2.filter.EventFilterRegistry;
import org.alfresco.repo.event2.filter.NodeAspectFilter;
import org.alfresco.repo.event2.filter.NodePropertyFilter;
import org.alfresco.repo.event2.mapper.PropertyMapper;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.security.NoSuchPersonException;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PathUtil;
import org.alfresco.util.PropertyCheck;

/**
 * Helper for {@link NodeResource} objects.
 *
 * @author Jamal Kaabi-Mofrad
 */
public class NodeResourceHelper implements InitializingBean
{
    private static final Log LOGGER = LogFactory.getLog(NodeResourceHelper.class);

    protected NodeService nodeService;
    protected DictionaryService dictionaryService;
    protected PersonService personService;
    protected EventFilterRegistry eventFilterRegistry;
    protected NamespaceService namespaceService;
    protected PermissionService permissionService;
    protected PropertyMapper propertyMapper;

    private NodeAspectFilter nodeAspectFilter;
    private NodePropertyFilter nodePropertyFilter;

    @Override
    public void afterPropertiesSet() throws Exception
    {
        PropertyCheck.mandatory(this, "nodeService", nodeService);
        PropertyCheck.mandatory(this, "dictionaryService", dictionaryService);
        PropertyCheck.mandatory(this, "personService", personService);
        PropertyCheck.mandatory(this, "eventFilterRegistry", eventFilterRegistry);
        PropertyCheck.mandatory(this, "namespaceService", namespaceService);
        PropertyCheck.mandatory(this, "permissionService", permissionService);
        PropertyCheck.mandatory(this, "propertyMapper", propertyMapper);

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

    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
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

    public void setPropertyMapper(PropertyMapper propertyMapper)
    {
        this.propertyMapper = propertyMapper;
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
                .setCreatedAt(getZonedDateTime((Date) properties.get(ContentModel.PROP_CREATED)))
                .setModifiedByUser(getUserInfo((String) properties.get(ContentModel.PROP_MODIFIER), mapUserCache))
                .setModifiedAt(getZonedDateTime((Date) properties.get(ContentModel.PROP_MODIFIED)))
                .setContent(getContentInfo(properties))
                .setPrimaryAssocQName(getPrimaryAssocQName(nodeRef))
                .setPrimaryHierarchy(PathUtil.getNodeIdsInReverse(path, false))
                .setProperties(mapToNodeProperties(properties))
                .setLocalizedProperties(mapToNodeLocalizedProperties(properties))
                .setAspectNames(getMappedAspects(nodeRef));
    }

    private boolean isSubClass(QName className, QName ofClassQName)
    {
        return dictionaryService.isSubClass(className, ofClassQName);
    }

    private String getPrimaryAssocQName(NodeRef nodeRef)
    {
        String result = null;
        try
        {
            ChildAssociationRef primaryParent = nodeService.getPrimaryParent(nodeRef);
            if (primaryParent != null && primaryParent.getQName() != null)
            {
                result = primaryParent.getQName().getPrefixedQName(namespaceService).getPrefixString();
            }
        }
        catch (NamespaceException namespaceException)
        {
            LOGGER.error("Cannot return a valid primary association QName: " + namespaceException.getMessage());
        }
        return result;
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
            if (!nodePropertyFilter.isExcluded(k))
            {
                if (v instanceof MLText)
                {
                    v = ((MLText) v).getDefaultValue();
                }
                Serializable mappedValue = propertyMapper.map(k, v);
                filteredProps.put(getQNamePrefixString(k), mappedValue);
            }
        });

        return filteredProps;
    }

    public Map<String, Map<String, String>> mapToNodeLocalizedProperties(Map<QName, Serializable> props)
    {
        Map<String, Map<String, String>> filteredProps = new HashMap<>(props.size());

        props.forEach((k, v) -> {
            if (!nodePropertyFilter.isExcluded(k) && v instanceof MLText)
            {
                final MLText mlTextValue = (MLText) v;
                final HashMap<String, String> localizedValues = new HashMap<>(mlTextValue.size());
                mlTextValue.forEach((locale, text) -> {
                    Serializable mappedValue = propertyMapper.map(k, text);
                    localizedValues.put(locale.toString(), (String) mappedValue);
                });
                filteredProps.put(getQNamePrefixString(k), localizedValues);
            }
        });

        return filteredProps.isEmpty() ? null : filteredProps;
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
     * Returns the QName in the format prefix:local, but in the exceptional case where there is no registered prefix returns it in the form {uri}local.
     *
     * @param k
     *            QName
     * @return a String representing the QName in the format prefix:local or {uri}local.
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
        // We need to have full MLText properties here. This is why we are marking the current thread as MLAware
        final boolean toRestore = MLPropertyInterceptor.isMLAware();
        MLPropertyInterceptor.setMLAware(true);
        try
        {
            return nodeService.getProperties(nodeRef);
        }
        finally
        {
            MLPropertyInterceptor.setMLAware(toRestore);
        }
    }

    public Map<String, Map<String, String>> getLocalizedPropertiesBefore(Map<QName, Serializable> propsBefore, NodeResource nodeAfter)
    {
        final Map<String, Map<String, String>> locPropsBefore = ofNullable(propsBefore)
                .map(this::mapToNodeLocalizedProperties)
                .orElseGet(Map::of);
        final Map<String, Map<String, String>> locPropsAfter = ofNullable(nodeAfter)
                .map(NodeResource::getLocalizedProperties)
                .orElseGet(Map::of);

        return getLocalizedPropertiesBefore(locPropsBefore, locPropsAfter);
    }

    static Map<String, Map<String, String>> getLocalizedPropertiesBefore(Map<String, Map<String, String>> locPropsBefore,
            Map<String, Map<String, String>> locPropsAfter)
    {
        final Map<String, Map<String, String>> result = new HashMap<>(locPropsBefore.size());

        Sets.union(locPropsBefore.keySet(), locPropsAfter.keySet()).forEach(propertyName -> {
            final Map<String, String> valuesBefore = ofNullable(locPropsBefore.get(propertyName)).orElseGet(Map::of);
            final Map<String, String> valuesAfter = ofNullable(locPropsAfter.get(propertyName)).orElseGet(Map::of);

            if (!valuesAfter.isEmpty() || !valuesBefore.isEmpty())
            {
                final Map<String, String> diff = new HashMap<>(valuesBefore.size());
                Sets.union(valuesBefore.keySet(), valuesAfter.keySet()).forEach(lang -> {
                    final String valueBefore = valuesBefore.get(lang);
                    final String valueAfter = valuesAfter.get(lang);
                    if (!Objects.equals(valueBefore, valueAfter))
                    {
                        diff.put(lang, valueBefore);
                    }
                });
                if (!diff.isEmpty())
                {
                    result.put(propertyName, diff);
                }
            }
        });

        return result;
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

    public PermissionService getPermissionService()
    {
        return permissionService;
    }
}
