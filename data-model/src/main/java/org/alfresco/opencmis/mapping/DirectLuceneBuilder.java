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

import org.alfresco.repo.search.adaptor.lucene.LuceneQueryParserAdaptor;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.namespace.QName;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;

/**
 * A simple 1-1 property lucene builder mapping from a CMIS property name to an alfresco property
 * 
 * @author andyh
 */
public class DirectLuceneBuilder extends AbstractSimpleLuceneBuilder
{
    private DictionaryService dictionaryService;
    private QName alfrescoName;
    
    public DirectLuceneBuilder(DictionaryService dictionaryService, QName alfrescoName)
    {
        this.dictionaryService = dictionaryService;
        this.alfrescoName = alfrescoName;
    }
    
    @Override
    public <Q, S, E extends Throwable> String getLuceneSortField(LuceneQueryParserAdaptor<Q, S, E> lqpa) throws E
    {
        String field = getLuceneFieldName();
        // need to find the real field to use
        PropertyDefinition propertyDef = dictionaryService.getProperty(QName.createQName(field.substring(1)));

        if (propertyDef.getDataType().getName().equals(DataTypeDefinition.CONTENT))
        {
            throw new CmisInvalidArgumentException("Order on content properties is not curently supported");
        }
        else if ((propertyDef.getDataType().getName().equals(DataTypeDefinition.MLTEXT)) || (propertyDef.getDataType().getName().equals(DataTypeDefinition.TEXT)))
        {
            field = lqpa.getSortField(field);
        }
        else if (propertyDef.getDataType().getName().equals(DataTypeDefinition.DATETIME))
        {
            field = lqpa.getDatetimeSortField(field, propertyDef);
        }        

        return field;
    }

    @Override
    public String getLuceneFieldName()
    {
        StringBuilder field = new StringBuilder(64);
        field.append("@");
        field.append(alfrescoName);
        return field.toString();
    }

    @Override
    protected String getValueAsString(Serializable value)
    {
        PropertyDefinition pd = dictionaryService.getProperty(alfrescoName);
        Object converted = DefaultTypeConverter.INSTANCE.convert(pd.getDataType(), value);
        String asString = DefaultTypeConverter.INSTANCE.convert(String.class, converted);
        return asString;
    }

    @Override
    protected QName getQNameForExists()
    {
        return alfrescoName;
    }

    @Override
    protected DataTypeDefinition getInDataType()
    {
        PropertyDefinition pd = dictionaryService.getProperty(alfrescoName);
        return pd.getDataType();
    }

    @Override
    protected String getRangeMax()
    {
        if(getInDataType().getName().equals(DataTypeDefinition.DATE))
        {
            return "MAX";
        }
        else if(getInDataType().getName().equals(DataTypeDefinition.DATETIME))
        {
            return "MAX";
        }
        else
        {
            return super.getRangeMax();
        }
    }
    
    
    @Override
    protected String getRangeMin()
    {
        if(getInDataType().getName().equals(DataTypeDefinition.DATE))
        {
            return "MIN";
        }
        else if(getInDataType().getName().equals(DataTypeDefinition.DATETIME))
        {
            return "MIN";
        }
        else
        {
            return super.getRangeMin();
        }
    }
    
}
