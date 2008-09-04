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
package org.alfresco.cmis.property;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.cmis.CMISService;
import org.alfresco.cmis.dictionary.CMISMapping;
import org.alfresco.cmis.dictionary.CMISScope;
import org.alfresco.cmis.dictionary.ContentStreamAllowed;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.search.impl.lucene.LuceneQueryParser;
import org.alfresco.repo.search.impl.lucene.ParseException;
import org.alfresco.repo.search.impl.querymodel.PredicateMode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.lucene.search.Query;
import org.springframework.beans.factory.InitializingBean;

/**
 * Mapping between Alfresco and CMIS property types
 * 
 * @author andyh
 */
public class CMISPropertyServiceImpl implements CMISPropertyService, InitializingBean
{
    private HashMap<String, NamedPropertyAccessor> namedPropertyAccessors = new HashMap<String, NamedPropertyAccessor>();

    private AbstractGenericPropertyAccessor genericPropertyAccessor;

    private CMISService cmisService;

    private CMISMapping cmisMapping;

    private ServiceRegistry serviceRegistry;

    private boolean strict = false;

    /**
     * @param cmisService
     */
    public void setCMISService(CMISService cmisService)
    {
        this.cmisService = cmisService;
    }

    /**
     * @param cmisMapping
     */
    public void setCMISMapping(CMISMapping cmisMapping)
    {
        this.cmisMapping = cmisMapping;
    }

    /**
     * @return cmis mapping service
     */
    public CMISMapping getCMISMapping()
    {
        return cmisMapping;
    }

    /**
     * @param serviceRegistry
     */
    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
    }

    public boolean isStrict()
    {
        return strict;
    }

    public void setStrict(boolean strict)
    {
        this.strict = strict;
    }

    public Map<String, Serializable> getProperties(NodeRef nodeRef)
    {
        // Map
        QName typeQName = cmisMapping.getCmisType(serviceRegistry.getNodeService().getType(nodeRef));
        CMISScope scope;
        if (cmisMapping.isValidCmisDocument(typeQName))
        {
            scope = CMISScope.DOCUMENT;
        }
        else if (cmisMapping.isValidCmisFolder(typeQName))
        {
            scope = CMISScope.FOLDER;
        }
        else
        {
            scope = null;
        }

        if (scope == null)
        {
            return Collections.<String, Serializable> emptyMap();
        }

        HashMap<String, Serializable> mapped = new HashMap<String, Serializable>();
        if (!strict)
        {
            Map<QName, Serializable> unmapped = serviceRegistry.getNodeService().getProperties(nodeRef);
            for (QName propertyQName : unmapped.keySet())
            {
                String cmisPropertyName = cmisMapping.getCmisPropertyName(propertyQName);
                mapped.put(cmisPropertyName, unmapped.get(propertyQName));
            }
        }
        // Add core
        for (String cmisPropertyName : namedPropertyAccessors.keySet())
        {
            NamedPropertyAccessor accessor = namedPropertyAccessors.get(cmisPropertyName);
            if ((accessor.getScope() == CMISScope.OBJECT) || accessor.getScope().equals(scope))
            {
                mapped.put(cmisPropertyName, accessor.getProperty(nodeRef));
                // Could hide properties here ....
            }
        }
        return mapped;
    }

    public Serializable getProperty(NodeRef nodeRef, String propertyName)
    {

        QName typeQName = cmisMapping.getCmisType(serviceRegistry.getNodeService().getType(nodeRef));
        CMISScope scope;
        if (cmisMapping.isValidCmisDocument(typeQName))
        {
            scope = CMISScope.DOCUMENT;
        }
        else if (cmisMapping.isValidCmisFolder(typeQName))
        {
            scope = CMISScope.FOLDER;
        }
        else
        {
            scope = null;
        }

        if (scope == null)
        {
            return null;
        }

        NamedPropertyAccessor accessor = namedPropertyAccessors.get(propertyName);
        if (accessor != null)
        {
            if ((accessor.getScope() == CMISScope.OBJECT) || accessor.getScope().equals(scope))
            {
                return accessor.getProperty(nodeRef);
            }
            else
            {
                return null;
            }
        }
        else
        {
            if (strict)
            {
                return null;
            }
            else
            {
                return genericPropertyAccessor.getProperty(nodeRef, propertyName);
            }
        }

    }

    public void setProperties(NodeRef nodeRef, Map<String, Serializable> values)
    {
        throw new UnsupportedOperationException();
    }

    public void setProperty(NodeRef nodeRef, String propertyName, Serializable value)
    {
        throw new UnsupportedOperationException();
    }

    public void afterPropertiesSet() throws Exception
    {
        // Generic Alfresco mappings
        genericPropertyAccessor = new MappingPropertyAccessor();
        genericPropertyAccessor.setServiceRegistry(serviceRegistry);
        genericPropertyAccessor.setCMISMapping(cmisMapping);

        // CMIS Object
        addNamedPropertyAccessor(getObjectIdPropertyAccessor());
        addNamedPropertyAccessor(getFixedValuePropertyAccessor(CMISMapping.PROP_URI, null, CMISScope.OBJECT));
        addNamedPropertyAccessor(getObjectTypeIdPropertyAccessor());
        addNamedPropertyAccessor(getSimplePropertyAccessor(CMISMapping.PROP_CREATED_BY, ContentModel.PROP_CREATOR, CMISScope.OBJECT));
        addNamedPropertyAccessor(getSimplePropertyAccessor(CMISMapping.PROP_CREATION_DATE, ContentModel.PROP_CREATED, CMISScope.OBJECT));
        addNamedPropertyAccessor(getSimplePropertyAccessor(CMISMapping.PROP_LAST_MODIFIED_BY, ContentModel.PROP_MODIFIER, CMISScope.OBJECT));
        addNamedPropertyAccessor(getSimplePropertyAccessor(CMISMapping.PROP_LAST_MODIFICATION_DATE, ContentModel.PROP_MODIFIED, CMISScope.OBJECT));
        addNamedPropertyAccessor(getFixedValuePropertyAccessor(CMISMapping.PROP_CHANGE_TOKEN, null, CMISScope.OBJECT));

        // CMIS Document and Folder
        addNamedPropertyAccessor(getSimplePropertyAccessor(CMISMapping.PROP_NAME, ContentModel.PROP_NAME, CMISScope.OBJECT));

        // CMIS Document
        addNamedPropertyAccessor(getIsImmutablePropertyAccessor());
        addNamedPropertyAccessor(getIsLatestVersionPropertyAccessor());
        addNamedPropertyAccessor(getIsMajorVersionPropertyAccessor());
        addNamedPropertyAccessor(getIsLatestMajorVersionPropertyAccessor());
        addNamedPropertyAccessor(getSimplePropertyAccessor(CMISMapping.PROP_VERSION_LABEL, ContentModel.PROP_VERSION_LABEL, CMISScope.DOCUMENT));
        addNamedPropertyAccessor(getVersionSeriesIdPropertyAccessor());
        addNamedPropertyAccessor(getVersionSeriesIsCheckedOutPropertyAccessor());
        addNamedPropertyAccessor(getVersionSeriesCheckedOutByPropertyAccessor());
        addNamedPropertyAccessor(getVersionSeriesCheckedOutIdPropertyAccessor());
        addNamedPropertyAccessor(getCheckinCommentPropertyAccessor());
        addNamedPropertyAccessor(getFixedValuePropertyAccessor(CMISMapping.PROP_CONTENT_STREAM_ALLOWED, ContentStreamAllowed.ALLOWED.toString(), CMISScope.DOCUMENT));
        addNamedPropertyAccessor(getContentStreamLengthPropertyAccessor());
        addNamedPropertyAccessor(getContentStreamMimetypePropertyAccessor());
        addNamedPropertyAccessor(getSimplePropertyAccessor(CMISMapping.PROP_CONTENT_STREAM_FILENAME, ContentModel.PROP_NAME, CMISScope.DOCUMENT));
        addNamedPropertyAccessor(getFixedValuePropertyAccessor(CMISMapping.PROP_CONTENT_STREAM_URI, null, CMISScope.DOCUMENT));

        // CMIS Folder
        addNamedPropertyAccessor(getParentPropertyAccessor());
        addNamedPropertyAccessor(getFixedValuePropertyAccessor(CMISMapping.PROP_ALLOWED_CHILD_OBJECT_TYPES, null, CMISScope.FOLDER));
    }

    public void addNamedPropertyAccessor(NamedPropertyAccessor namedPropertyAccessor)
    {

        namedPropertyAccessors.put(namedPropertyAccessor.getPropertyName(), namedPropertyAccessor);
    }

    public NamedPropertyAccessor getSimplePropertyAccessor(String propertyName, QName to, CMISScope scope)
    {
        SimplePropertyAccessor accessor = new SimplePropertyAccessor();
        accessor.setServiceRegistry(serviceRegistry);
        accessor.setCMISMapping(cmisMapping);
        accessor.setPropertyName(propertyName);
        accessor.setMapping(to.toString());
        accessor.setScope(scope);
        try
        {
            accessor.afterPropertiesSet();
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return accessor;
    }

    public NamedPropertyAccessor getObjectIdPropertyAccessor()
    {
        ObjectIdPropertyAccessor accessor = new ObjectIdPropertyAccessor();
        accessor.setServiceRegistry(serviceRegistry);
        accessor.setCMISMapping(cmisMapping);
        return accessor;
    }

    public NamedPropertyAccessor getFixedValuePropertyAccessor(String propertyName, Serializable fixedValue, CMISScope scope)
    {
        FixedValuePropertyAccessor accessor = new FixedValuePropertyAccessor();
        accessor.setServiceRegistry(serviceRegistry);
        accessor.setCMISMapping(cmisMapping);
        accessor.setPropertyName(propertyName);
        accessor.setFixedValue(fixedValue);
        accessor.setScope(scope);
        return accessor;
    }

    public NamedPropertyAccessor getObjectTypeIdPropertyAccessor()
    {
        ObjectTypeIdPropertyAccessor accessor = new ObjectTypeIdPropertyAccessor();
        accessor.setServiceRegistry(serviceRegistry);
        accessor.setCMISMapping(cmisMapping);
        return accessor;
    }

    public NamedPropertyAccessor getIsImmutablePropertyAccessor()
    {
        IsImmutablePropertyAccessor accessor = new IsImmutablePropertyAccessor();
        accessor.setServiceRegistry(serviceRegistry);
        accessor.setCMISMapping(cmisMapping);
        return accessor;
    }

    public NamedPropertyAccessor getIsLatestVersionPropertyAccessor()
    {
        IsLatestVersionPropertyAccessor accessor = new IsLatestVersionPropertyAccessor();
        accessor.setServiceRegistry(serviceRegistry);
        accessor.setCMISMapping(cmisMapping);
        return accessor;
    }

    public NamedPropertyAccessor getIsMajorVersionPropertyAccessor()
    {
        IsMajorVersionPropertyAccessor accessor = new IsMajorVersionPropertyAccessor();
        accessor.setServiceRegistry(serviceRegistry);
        accessor.setCMISMapping(cmisMapping);
        return accessor;
    }

    public NamedPropertyAccessor getIsLatestMajorVersionPropertyAccessor()
    {
        IsLatestMajorVersionPropertyAccessor accessor = new IsLatestMajorVersionPropertyAccessor();
        accessor.setServiceRegistry(serviceRegistry);
        accessor.setCMISMapping(cmisMapping);
        return accessor;
    }

    public NamedPropertyAccessor getVersionSeriesIdPropertyAccessor()
    {
        VersionSeriesIdPropertyAccessor accessor = new VersionSeriesIdPropertyAccessor();
        accessor.setServiceRegistry(serviceRegistry);
        accessor.setCMISMapping(cmisMapping);
        return accessor;
    }

    public NamedPropertyAccessor getVersionSeriesIsCheckedOutPropertyAccessor()
    {
        VersionSeriesIsCheckedOutPropertyAccessor accessor = new VersionSeriesIsCheckedOutPropertyAccessor();
        accessor.setServiceRegistry(serviceRegistry);
        accessor.setCMISMapping(cmisMapping);
        return accessor;
    }

    public NamedPropertyAccessor getVersionSeriesCheckedOutByPropertyAccessor()
    {
        VersionSeriesCheckedOutByPropertyAccessor accessor = new VersionSeriesCheckedOutByPropertyAccessor();
        accessor.setServiceRegistry(serviceRegistry);
        accessor.setCMISMapping(cmisMapping);
        return accessor;
    }

    public NamedPropertyAccessor getVersionSeriesCheckedOutIdPropertyAccessor()
    {
        VersionSeriesCheckedOutIdPropertyAccessor accessor = new VersionSeriesCheckedOutIdPropertyAccessor();
        accessor.setServiceRegistry(serviceRegistry);
        accessor.setCMISMapping(cmisMapping);
        return accessor;
    }

    public NamedPropertyAccessor getCheckinCommentPropertyAccessor()
    {
        CheckinCommentPropertyAccessor accessor = new CheckinCommentPropertyAccessor();
        accessor.setServiceRegistry(serviceRegistry);
        accessor.setCMISMapping(cmisMapping);
        return accessor;
    }

    public NamedPropertyAccessor getContentStreamLengthPropertyAccessor()
    {
        ContentStreamLengthPropertyAccessor accessor = new ContentStreamLengthPropertyAccessor();
        accessor.setServiceRegistry(serviceRegistry);
        accessor.setCMISMapping(cmisMapping);
        return accessor;
    }

    public NamedPropertyAccessor getContentStreamMimetypePropertyAccessor()
    {
        ContentStreamMimetypePropertyAccessor accessor = new ContentStreamMimetypePropertyAccessor();
        accessor.setServiceRegistry(serviceRegistry);
        accessor.setCMISMapping(cmisMapping);
        return accessor;
    }

    public NamedPropertyAccessor getParentPropertyAccessor()
    {
        ParentPropertyAccessor accessor = new ParentPropertyAccessor();
        accessor.setServiceRegistry(serviceRegistry);
        accessor.setCMISMapping(cmisMapping);
        accessor.setCMISService(cmisService);
        return accessor;
    }

    /**
     * @param lqp
     * @param propertyName
     * @param value
     * @param mode 
     * @return
     * @throws ParseException 
     */
    public Query buildLuceneEquality(LuceneQueryParser lqp, String propertyName, Serializable value, PredicateMode mode) throws ParseException
    {
        NamedPropertyAccessor accessor = namedPropertyAccessors.get(propertyName);
        if (accessor != null)
        {
            return accessor.buildLuceneEquality(lqp, propertyName, value, mode);
        }
        else
        {
            return genericPropertyAccessor.buildLuceneEquality(lqp, propertyName, value, mode);
        }
    }

    /**
     * @param lqp
     * @param propertyName
     * @param not
     * @return
     */
    public Query buildLuceneExists(LuceneQueryParser lqp, String propertyName, Boolean not) throws ParseException
    {
        NamedPropertyAccessor accessor = namedPropertyAccessors.get(propertyName);
        if (accessor != null)
        {
            return accessor.buildLuceneExists(lqp, propertyName, not);
        }
        else
        {
            return genericPropertyAccessor.buildLuceneExists(lqp, propertyName, not);
        }
    }

    /**
     * @param lqp
     * @param propertyName
     * @param value
     * @param mode
     * @return
     * @throws ParseException 
     */
    public Query buildLuceneGreaterThan(LuceneQueryParser lqp, String propertyName, Serializable value, PredicateMode mode) throws ParseException
    {
        NamedPropertyAccessor accessor = namedPropertyAccessors.get(propertyName);
        if (accessor != null)
        {
            return accessor.buildLuceneGreaterThan(lqp, propertyName, value, mode);
        }
        else
        {
            return genericPropertyAccessor.buildLuceneGreaterThan(lqp, propertyName, value, mode);
        }
    }

    /**
     * @param lqp
     * @param propertyName
     * @param value
     * @param mode
     * @return
     * @throws ParseException 
     */
    public Query buildLuceneGreaterThanOrEquals(LuceneQueryParser lqp, String propertyName, Serializable value, PredicateMode mode) throws ParseException
    {
        NamedPropertyAccessor accessor = namedPropertyAccessors.get(propertyName);
        if (accessor != null)
        {
            return accessor.buildLuceneGreaterThanOrEquals(lqp, propertyName, value, mode);
        }
        else
        {
            return genericPropertyAccessor.buildLuceneGreaterThanOrEquals(lqp, propertyName, value, mode);
        }
    }

    /**
     * @param lqp
     * @param propertyName
     * @param values
     * @param not
     * @param mode
     * @return
     * @throws ParseException 
     */
    public Query buildLuceneIn(LuceneQueryParser lqp, String propertyName, Collection<Serializable> values, Boolean not, PredicateMode mode) throws ParseException
    {
        NamedPropertyAccessor accessor = namedPropertyAccessors.get(propertyName);
        if (accessor != null)
        {
            return accessor.buildLuceneIn(lqp, propertyName, values, not,  mode);
        }
        else
        {
            return genericPropertyAccessor.buildLuceneIn(lqp, propertyName, values, not, mode);
        }
    }

    /**
     * @param lqp
     * @param propertyName
     * @param value
     * @param mode
     * @return
     * @throws ParseException 
     */
    public Query buildLuceneInequality(LuceneQueryParser lqp, String propertyName, Serializable value, PredicateMode mode) throws ParseException
    {
        NamedPropertyAccessor accessor = namedPropertyAccessors.get(propertyName);
        if (accessor != null)
        {
            return accessor.buildLuceneInequality(lqp, propertyName, value, mode);
        }
        else
        {
            return genericPropertyAccessor.buildLuceneInequality(lqp, propertyName, value, mode);
        }
    }

    /**
     * @param lqp
     * @param propertyName
     * @param value
     * @param mode
     * @return
     * @throws ParseException 
     */
    public Query buildLuceneLessThan(LuceneQueryParser lqp, String propertyName, Serializable value, PredicateMode mode) throws ParseException
    {
        NamedPropertyAccessor accessor = namedPropertyAccessors.get(propertyName);
        if (accessor != null)
        {
            return accessor.buildLuceneLessThan(lqp, propertyName, value, mode);
        }
        else
        {
            return genericPropertyAccessor.buildLuceneLessThan(lqp, propertyName, value, mode);
        }
    }

    /**
     * @param lqp
     * @param propertyName
     * @param value
     * @param mode
     * @return
     * @throws ParseException 
     */
    public Query buildLuceneLessThanOrEquals(LuceneQueryParser lqp, String propertyName, Serializable value, PredicateMode mode) throws ParseException
    {
        NamedPropertyAccessor accessor = namedPropertyAccessors.get(propertyName);
        if (accessor != null)
        {
            return accessor.buildLuceneLessThanOrEquals(lqp, propertyName, value, mode);
        }
        else
        {
            return genericPropertyAccessor.buildLuceneLessThanOrEquals(lqp, propertyName, value, mode);
        }
    }

    /**
     * @param lqp
     * @param propertyName
     * @param value
     * @param not
     * @return
     * @throws ParseException 
     */
    public Query buildLuceneLike(LuceneQueryParser lqp, String propertyName, Serializable value, Boolean not) throws ParseException
    {
        NamedPropertyAccessor accessor = namedPropertyAccessors.get(propertyName);
        if (accessor != null)
        {
            return accessor.buildLuceneLike(lqp, propertyName, value, not);
        }
        else
        {
            return genericPropertyAccessor.buildLuceneLike(lqp, propertyName, value, not);
        }
    }

    /**
     * @param propertyName
     * @return
     */
    public String getLuceneSortField(String propertyName)
    {
        NamedPropertyAccessor accessor = namedPropertyAccessors.get(propertyName);
        if (accessor != null)
        {
            return accessor.getLuceneSortField(propertyName);
        }
        else
        {
            return genericPropertyAccessor.getLuceneSortField(propertyName);
        }
    }

}
