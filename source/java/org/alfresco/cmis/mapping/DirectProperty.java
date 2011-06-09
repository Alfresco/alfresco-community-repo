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
package org.alfresco.cmis.mapping;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import org.alfresco.repo.dictionary.IndexTokenisationMode;
import org.alfresco.repo.search.MLAnalysisMode;
import org.alfresco.repo.search.SearcherException;
import org.alfresco.repo.search.impl.lucene.AbstractLuceneQueryParser;
import org.alfresco.repo.search.impl.lucene.analysis.DateTimeAnalyser;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.namespace.QName;
import org.apache.lucene.index.IndexReader.FieldOption;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * A simple 1-1 property mapping from a CMIS property name to an alfresco property
 * 
 * @author andyh
 */
public class DirectProperty extends AbstractSimpleProperty
{
    /* (non-Javadoc)
     * @see org.alfresco.cmis.mapping.AbstractSimpleProperty#getRangeMax()
     */
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

    /* (non-Javadoc)
     * @see org.alfresco.cmis.mapping.AbstractSimpleProperty#getRangeMin()
     */
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

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.mapping.AbstractSimpleProperty#getLuceneSortField()
     */
    @Override
    public String getLuceneSortField(AbstractLuceneQueryParser lqp)
    {

        String field = getLuceneFieldName();
        // need to find the real field to use
        Locale sortLocale = null;

        PropertyDefinition propertyDef = getServiceRegistry().getDictionaryService().getProperty(QName.createQName(field.substring(1)));

        if (propertyDef.getDataType().getName().equals(DataTypeDefinition.CONTENT))
        {
            throw new SearcherException("Order on content properties is not curently supported");
        }
        else if(propertyDef.getDataType().getName().equals(DataTypeDefinition.TEXT))
        {
            if(propertyDef.getIndexTokenisationMode() == IndexTokenisationMode.FALSE)
            {
                return field;
            }
            
            String noLocalField = field+".no_locale";
            for (Object current : lqp.getIndexReader().getFieldNames(FieldOption.INDEXED))
            {
                String currentString = (String) current;
                if (currentString.equals(noLocalField))
                {
                    return noLocalField;
                }
            }
            
            return findSortField(lqp, field);
        }
        else if (propertyDef.getDataType().getName().equals(DataTypeDefinition.MLTEXT))
        {
            field = findSortField(lqp, field);
        }
        else if (propertyDef.getDataType().getName().equals(DataTypeDefinition.DATETIME))
        {
            DataTypeDefinition dataType = propertyDef.getDataType();
            String analyserClassName = dataType.getAnalyserClassName();
            if (analyserClassName.equals(DateTimeAnalyser.class.getCanonicalName()))
            {
                field = field + ".sort";
            }
        }

        return field;
    }

    /**
     * @param lqp
     * @param field
     * @return
     */
    private String findSortField(AbstractLuceneQueryParser lqp, String field)
    {
        Locale sortLocale;
        List<Locale> locales = lqp.getSearchParameters().getLocales();
        if (((locales == null) || (locales.size() == 0)))
        {
            locales = Collections.singletonList(I18NUtil.getLocale());
        }

        if (locales.size() > 1)
        {
            throw new SearcherException("Order on text/mltext properties with more than one locale is not curently supported");
        }

        sortLocale = locales.get(0);
        // find best field match

        HashSet<String> allowableLocales = new HashSet<String>();
        MLAnalysisMode analysisMode = lqp.getDefaultSearchMLAnalysisMode();
        for (Locale l : MLAnalysisMode.getLocales(analysisMode, sortLocale, false))
        {
            allowableLocales.add(l.toString());
        }

        String sortField = field;

        for (Object current : lqp.getIndexReader().getFieldNames(FieldOption.INDEXED))
        {
            String currentString = (String) current;
            if (currentString.startsWith(field) && currentString.endsWith(".sort"))
            {
                String fieldLocale = currentString.substring(field.length() + 1, currentString.length() - 5);
                if (allowableLocales.contains(fieldLocale))
                {
                    if (fieldLocale.equals(sortLocale.toString()))
                    {
                        sortField = currentString;
                        break;
                    }
                    else if (sortLocale.toString().startsWith(fieldLocale))
                    {
                        if (sortField.equals(field) || (currentString.length() < sortField.length()))
                        {
                            sortField = currentString;
                        }
                    }
                    else if (fieldLocale.startsWith(sortLocale.toString()))
                    {
                        if (sortField.equals(field) || (currentString.length() < sortField.length()))
                        {
                            sortField = currentString;
                        }
                    }
                }
            }
        }

        field = sortField;
        return field;
    }

    private QName alfrescoName;

    /**
     * Construct
     * 
     * @param serviceRegistry
     * @param propertyName
     * @param alfrescoName
     */
    public DirectProperty(ServiceRegistry serviceRegistry, String propertyName, QName alfrescoName)
    {
        super(serviceRegistry, propertyName);
        this.alfrescoName = alfrescoName;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.property.AbstractPropertyAccessor#getMappedProperty()
     */
    public QName getMappedProperty()
    {
        return alfrescoName;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.property.PropertyAccessor#getValue(org.alfresco.service.cmr.repository.NodeRef)
     */
    public Serializable getValue(NodeRef nodeRef)
    {
        return getServiceRegistry().getNodeService().getProperty(nodeRef, alfrescoName);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.mapping.AbstractProperty#getValue(org.alfresco.service.cmr.repository.AssociationRef)
     */
    public Serializable getValue(AssociationRef assocRef)
    {
        return null;
    }

    public String getLuceneFieldName()
    {
        StringBuilder field = new StringBuilder(64);
        field.append("@");
        field.append(alfrescoName);
        return field.toString();
    }

    protected String getValueAsString(Serializable value)
    {
        PropertyDefinition pd = getServiceRegistry().getDictionaryService().getProperty(alfrescoName);
        Object converted = DefaultTypeConverter.INSTANCE.convert(pd.getDataType(), value);
        String asString = DefaultTypeConverter.INSTANCE.convert(String.class, converted);
        return asString;
    }

    protected QName getQNameForExists()
    {
        return alfrescoName;
    }

    protected DataTypeDefinition getInDataType()
    {
        PropertyDefinition pd = getServiceRegistry().getDictionaryService().getProperty(alfrescoName);
        return pd.getDataType();
    }

}
