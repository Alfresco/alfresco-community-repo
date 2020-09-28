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

import java.io.Serializable;

import org.alfresco.opencmis.dictionary.CMISDictionaryService;
import org.alfresco.opencmis.dictionary.TypeDefinitionWrapper;
import org.alfresco.repo.search.adaptor.lucene.AnalysisMode;
import org.alfresco.repo.search.adaptor.lucene.LuceneFunction;
import org.alfresco.repo.search.adaptor.lucene.LuceneQueryParserAdaptor;
import org.alfresco.repo.search.impl.querymodel.PredicateMode;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;


/**
 * Get the CMIS object type id property
 * 
 * @author andyh
 */
public class BaseTypeIdLuceneBuilder extends BaseLuceneBuilder
{
    private CMISDictionaryService dictionaryService;
    

    /**
     * Construct
     */
    public BaseTypeIdLuceneBuilder(CMISDictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    @Override
    public <Q, S, E extends Throwable> Q buildLuceneEquality(LuceneQueryParserAdaptor<Q, S, E> lqpa, Serializable value, PredicateMode mode, LuceneFunction luceneFunction) throws E
    {
        return lqpa.getFieldQuery("TYPE", getType(getValueAsString(value)), AnalysisMode.IDENTIFIER, luceneFunction);     
    }

    @Override
    public <Q, S, E extends Throwable> Q buildLuceneExists(LuceneQueryParserAdaptor<Q, S, E> lqpa, Boolean not) throws E
    {
        if (not)
        {
            return lqpa.getMatchNoneQuery();
        }
        else
        { 
            return lqpa.getMatchAllQuery();
        }
    }
    
    private String getType(String tableName)
    {
        TypeDefinitionWrapper typeDef = dictionaryService.findTypeByQueryName(tableName);
        if (typeDef == null)
        {
            throw new CmisInvalidArgumentException("Unknown type: " + tableName);
        }
        if(!typeDef.isBaseType())
        {
            throw new CmisInvalidArgumentException("Not a base type: " + tableName);
        }
        if(!typeDef.getTypeDefinition(false).isQueryable())
        {
            throw new CmisInvalidArgumentException("Type is not queryable: " + tableName);
        }
        return typeDef.getAlfrescoClass().toString();
    }
    
    private String getValueAsString(Serializable value)
    {
        String asString = DefaultTypeConverter.INSTANCE.convert(String.class, value);
        return asString;
    }
}
