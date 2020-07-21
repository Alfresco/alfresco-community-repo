/*
 * #%L
 * Alfresco Data model classes
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
package org.alfresco.opencmis.mapping;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.opencmis.dictionary.CMISDictionaryService;
import org.alfresco.opencmis.dictionary.CMISPropertyLuceneBuilder;
import org.alfresco.opencmis.dictionary.PropertyLuceneBuilderMapping;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.QName;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.springframework.beans.factory.InitializingBean;


/**
 * Lucene Builder mappings for built-in CMIS properties
 *  
 * @author davidc
 */
public class RuntimePropertyLuceneBuilderMapping implements PropertyLuceneBuilderMapping, InitializingBean
{
    private CMISDictionaryService cmisDictionaryService;
    private DictionaryService dictionaryService;

    private Map<String, CMISPropertyLuceneBuilder> luceneBuilders = new HashMap<String, CMISPropertyLuceneBuilder>();

    /**
     * @param cmisDictionaryService cmisDictionaryService
     */
    public void setCmisDictionaryService(CMISDictionaryService cmisDictionaryService)
    {
        this.cmisDictionaryService = cmisDictionaryService;
    }

    /**
     * @param dictionaryService dictionaryService
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }
    
    @Override
    public void afterPropertiesSet()
    {
        registerPropertyLuceneBuilder(PropertyIds.OBJECT_ID, new ObjectIdLuceneBuilder(dictionaryService));
        registerPropertyLuceneBuilder(PropertyIds.OBJECT_TYPE_ID, new ObjectTypeIdLuceneBuilder(cmisDictionaryService));
        registerPropertyLuceneBuilder(PropertyIds.BASE_TYPE_ID, new BaseTypeIdLuceneBuilder(cmisDictionaryService));
        registerPropertyLuceneBuilder(PropertyIds.CREATED_BY, new DirectLuceneBuilder(dictionaryService, ContentModel.PROP_CREATOR));
        registerPropertyLuceneBuilder(PropertyIds.CREATION_DATE, new DirectLuceneBuilder(dictionaryService, ContentModel.PROP_CREATED));
        registerPropertyLuceneBuilder(PropertyIds.LAST_MODIFIED_BY, new DirectLuceneBuilder(dictionaryService, ContentModel.PROP_MODIFIER));
        registerPropertyLuceneBuilder(PropertyIds.LAST_MODIFICATION_DATE, new DirectLuceneBuilder(dictionaryService, ContentModel.PROP_MODIFIED));
        registerPropertyLuceneBuilder(PropertyIds.CHANGE_TOKEN, new FixedValueLuceneBuilder(null));
        registerPropertyLuceneBuilder(PropertyIds.NAME, new DirectLuceneBuilder(dictionaryService, ContentModel.PROP_NAME));
        registerPropertyLuceneBuilder(PropertyIds.DESCRIPTION, new DirectLuceneBuilder(dictionaryService, ContentModel.PROP_DESCRIPTION));
        registerPropertyLuceneBuilder(PropertyIds.IS_IMMUTABLE, new NotSupportedLuceneBuilder());
        registerPropertyLuceneBuilder(PropertyIds.IS_LATEST_VERSION, new NotSupportedLuceneBuilder());
        registerPropertyLuceneBuilder(PropertyIds.IS_MAJOR_VERSION, new NotSupportedLuceneBuilder());
        registerPropertyLuceneBuilder(PropertyIds.IS_LATEST_MAJOR_VERSION, new NotSupportedLuceneBuilder());
        registerPropertyLuceneBuilder(PropertyIds.VERSION_LABEL, new NotSupportedLuceneBuilder());
        registerPropertyLuceneBuilder(PropertyIds.VERSION_SERIES_ID, new NotSupportedLuceneBuilder());
        registerPropertyLuceneBuilder(PropertyIds.IS_VERSION_SERIES_CHECKED_OUT, new NotSupportedLuceneBuilder());
        registerPropertyLuceneBuilder(PropertyIds.VERSION_SERIES_CHECKED_OUT_BY, new NotSupportedLuceneBuilder());
        registerPropertyLuceneBuilder(PropertyIds.VERSION_SERIES_CHECKED_OUT_ID, new NotSupportedLuceneBuilder());
        registerPropertyLuceneBuilder(PropertyIds.CHECKIN_COMMENT, new NotSupportedLuceneBuilder());
        registerPropertyLuceneBuilder(PropertyIds.CONTENT_STREAM_LENGTH, new ContentStreamLengthLuceneBuilder(dictionaryService));
        registerPropertyLuceneBuilder(PropertyIds.CONTENT_STREAM_MIME_TYPE, new ContentStreamMimetypeLuceneBuilder(dictionaryService));
        registerPropertyLuceneBuilder(PropertyIds.CONTENT_STREAM_ID, new NotSupportedLuceneBuilder());
        registerPropertyLuceneBuilder(PropertyIds.CONTENT_STREAM_FILE_NAME, new DirectLuceneBuilder(dictionaryService, ContentModel.PROP_NAME));
        registerPropertyLuceneBuilder(PropertyIds.PARENT_ID, new ParentLuceneBuilder(dictionaryService));
        registerPropertyLuceneBuilder(PropertyIds.PATH, new NotSupportedLuceneBuilder());
        registerPropertyLuceneBuilder(PropertyIds.ALLOWED_CHILD_OBJECT_TYPE_IDS, new NotSupportedLuceneBuilder());
        registerPropertyLuceneBuilder(PropertyIds.SOURCE_ID, new NotSupportedLuceneBuilder());
        registerPropertyLuceneBuilder(PropertyIds.TARGET_ID, new NotSupportedLuceneBuilder());
        
        registerPropertyLuceneBuilder("alfcmis:nodeRef", new NotSupportedLuceneBuilder());
    }

    @Override
    public CMISPropertyLuceneBuilder getPropertyLuceneBuilder(String propertyId)
    {
        return luceneBuilders.get(propertyId);
    }
    
    @Override
    public CMISPropertyLuceneBuilder createDirectPropertyLuceneBuilder(QName propertyName)
    {
        return new DirectLuceneBuilder(dictionaryService, propertyName);
    }
    
    /**
     * Register pre-defined Property Accessor
     * 
     * @param name String
     * @param luceneBuilder CMISPropertyLuceneBuilder
     */
    private void registerPropertyLuceneBuilder(String name, CMISPropertyLuceneBuilder luceneBuilder)
    {
        luceneBuilders.put(name, luceneBuilder);
    }
}
