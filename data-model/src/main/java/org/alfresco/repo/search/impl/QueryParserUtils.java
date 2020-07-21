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
package org.alfresco.repo.search.impl;

import java.util.HashSet;

import org.alfresco.repo.search.adaptor.lucene.QueryConstants;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryException;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;

/**
 * @author Andy
 *
 */
public class QueryParserUtils implements QueryConstants
{
    public static String expandQName(String defaultNameSpaceUri, NamespacePrefixResolver namespacePrefixResolver, String qnameString)
    {
        String fieldName = qnameString;
        // Check for any prefixes and expand to the full uri
        if (qnameString.charAt(0) != '{')
        {
            int colonPosition = qnameString.indexOf(':');
            if (colonPosition == -1)
            {
                // use the default namespace
                fieldName = "{" + defaultNameSpaceUri + "}" + qnameString;
            }
            else
            {
                String prefix = qnameString.substring(0, colonPosition);
                String uri = matchURI(namespacePrefixResolver, prefix);
                if (uri == null)
                {
                    fieldName = "{" + defaultNameSpaceUri + "}" + qnameString;
                }
                else
                {
                    fieldName = "{" + uri + "}" + qnameString.substring(colonPosition + 1);
                }

            }
        }
        return fieldName;
    }
    
    public static String matchURI(NamespacePrefixResolver namespacePrefixResolver, String prefix)
    {
        HashSet<String> prefixes = new HashSet<String>(namespacePrefixResolver.getPrefixes());
        if (prefixes.contains(prefix))
        {
            return namespacePrefixResolver.getNamespaceURI(prefix);
        }
        String match = null;
        for (String candidate : prefixes)
        {
            if (candidate.equalsIgnoreCase(prefix))
            {
                if (match == null)
                {
                    match = candidate;
                }
                else
                {

                    throw new NamespaceException("Ambiguous namespace prefix " + prefix);

                }
            }
        }
        if (match == null)
        {
            return null;
        }
        else
        {
            return namespacePrefixResolver.getNamespaceURI(match);
        }
    }

    public static ClassDefinition matchClassDefinition(String defaultNameSpaceUri, NamespacePrefixResolver namespacePrefixResolver, DictionaryService dictionaryService, String string)
    {
        QName search = QName.createQName(expandQName(defaultNameSpaceUri, namespacePrefixResolver, string));
        ClassDefinition classDefinition = dictionaryService.getClass(QName.createQName(expandQName(defaultNameSpaceUri, namespacePrefixResolver, string)));
        QName match = null;
        if (classDefinition == null)
        {
            for (QName definition : dictionaryService.getAllTypes())
            {
                if (definition.getNamespaceURI().equalsIgnoreCase(search.getNamespaceURI()))
                {
                    if (definition.getLocalName().equalsIgnoreCase(search.getLocalName()))
                    {
                        if (match == null)
                        {
                            match = definition;
                        }
                        else
                        {
                            throw new DictionaryException("Ambiguous data datype " + string);
                        }
                    }
                }
            }
            for (QName definition : dictionaryService.getAllAspects())
            {
                if (definition.getNamespaceURI().equalsIgnoreCase(search.getNamespaceURI()))
                {
                    if (definition.getLocalName().equalsIgnoreCase(search.getLocalName()))
                    {
                        if (match == null)
                        {
                            match = definition;
                        }
                        else
                        {
                            throw new DictionaryException("Ambiguous data datype " + string);
                        }
                    }
                }
            }
        }
        else
        {
            return classDefinition;
        }
        if (match == null)
        {
            return null;
        }
        else
        {
            return dictionaryService.getClass(match);
        }
    }
    
    public static AspectDefinition matchAspectDefinition(String defaultNameSpaceUri, NamespacePrefixResolver namespacePrefixResolver, DictionaryService dictionaryService, String string)
    {
        QName search = QName.createQName(expandQName(defaultNameSpaceUri, namespacePrefixResolver, string));
        AspectDefinition aspectDefinition = dictionaryService.getAspect(QName.createQName(expandQName(defaultNameSpaceUri, namespacePrefixResolver, string)));
        QName match = null;
        if (aspectDefinition == null)
        {
            for (QName definition : dictionaryService.getAllAspects())
            {
                if (definition.getNamespaceURI().equalsIgnoreCase(search.getNamespaceURI()))
                {
                    if (definition.getLocalName().equalsIgnoreCase(search.getLocalName()))
                    {
                        if (match == null)
                        {
                            match = definition;
                        }
                        else
                        {
                            throw new DictionaryException("Ambiguous data datype " + string);
                        }
                    }
                }
            }
        }
        else
        {
            return aspectDefinition;
        }
        if (match == null)
        {
            return null;
        }
        else
        {
            return dictionaryService.getAspect(match);
        }
    }
    
    public static TypeDefinition matchTypeDefinition(String defaultNameSpaceUri, NamespacePrefixResolver namespacePrefixResolver, DictionaryService dictionaryService, String string) 
    {
        QName search = QName.createQName(expandQName(defaultNameSpaceUri, namespacePrefixResolver, string));
        TypeDefinition typeDefinition = dictionaryService.getType(QName.createQName(expandQName(defaultNameSpaceUri, namespacePrefixResolver, string)));
        QName match = null;
        if (typeDefinition == null)
        {
            for (QName definition : dictionaryService.getAllTypes())
            {
                if (definition.getNamespaceURI().equalsIgnoreCase(search.getNamespaceURI()))
                {
                    if (definition.getLocalName().equalsIgnoreCase(search.getLocalName()))
                    {
                        if (match == null)
                        {
                            match = definition;
                        }
                        else
                        {
                            throw new DictionaryException("Ambiguous data datype " + string);
                        }
                    }
                }
            }
        }
        else
        {
            return typeDefinition;
        }
        if (match == null)
        {
            return null;
        }
        else
        {
            return dictionaryService.getType(match);
        }
    }
    
    public static DataTypeDefinition matchDataTypeDefinition(String defaultNameSpaceUri, NamespacePrefixResolver namespacePrefixResolver, DictionaryService dictionaryService, String string) 
    {
        QName search = QName.createQName(QueryParserUtils.expandQName(defaultNameSpaceUri, namespacePrefixResolver, string));
        DataTypeDefinition dataTypeDefinition = dictionaryService.getDataType(QName.createQName(QueryParserUtils.expandQName(defaultNameSpaceUri, namespacePrefixResolver, string)));
        QName match = null;
        if (dataTypeDefinition == null)
        {
            for (QName definition : dictionaryService.getAllDataTypes())
            {
                if (definition.getNamespaceURI().equalsIgnoreCase(search.getNamespaceURI()))
                {
                    if (definition.getLocalName().equalsIgnoreCase(search.getLocalName()))
                    {
                        if (match == null)
                        {
                            match = definition;
                        }
                        else
                        {
                            throw new DictionaryException("Ambiguous data datype " + string);
                        }
                    }
                }

            }
        }
        else
        {
            return dataTypeDefinition;
        }
        if (match == null)
        {
            return null;
        }
        else
        {
            return dictionaryService.getDataType(match);
        }
    }

    public static PropertyDefinition matchPropertyDefinition(String defaultNameSpaceUri, NamespacePrefixResolver namespacePrefixResolver, DictionaryService dictionaryService, String string)
    {
        QName search = QName.createQName(QueryParserUtils.expandQName(defaultNameSpaceUri, namespacePrefixResolver, string));
        PropertyDefinition propertyDefinition = dictionaryService.getProperty(QName.createQName(QueryParserUtils.expandQName(defaultNameSpaceUri, namespacePrefixResolver, string)));
        QName match = null;
        if (propertyDefinition == null)
        {
            for (QName definition : dictionaryService.getAllProperties(null))
            {
                if (definition.getNamespaceURI().equalsIgnoreCase(search.getNamespaceURI()))
                {
                    if (definition.getLocalName().equalsIgnoreCase(search.getLocalName()))
                    {
                        if (match == null)
                        {
                            match = definition;
                        }
                        else
                        {
                            throw new DictionaryException("Ambiguous data datype " + string);
                        }
                    }
                }

            }
        }
        else
        {
            return propertyDefinition;
        }
        if (match == null)
        {
            return null;
        }
        else
        {
            return dictionaryService.getProperty(match);
        }
    }

    public static Pair<String, String> extractFieldNameAndEnding(String field)
    {
        String propertyFieldName = null;
        String ending = "";
        if (field.endsWith(FIELD_MIMETYPE_SUFFIX))
        {
            propertyFieldName = field.substring(field.startsWith("@") ? 1 : 0, field.length() - FIELD_MIMETYPE_SUFFIX.length());
            ending = FIELD_MIMETYPE_SUFFIX;
        }
        else if (field.endsWith(FIELD_SIZE_SUFFIX))
        {
            propertyFieldName = field.substring(field.startsWith("@") ? 1 : 0, field.length() - FIELD_SIZE_SUFFIX.length());
            ending = FIELD_SIZE_SUFFIX;
        }
        else if (field.endsWith(FIELD_LOCALE_SUFFIX))
        {
            propertyFieldName = field.substring(field.startsWith("@") ? 1 : 0, field.length() - FIELD_LOCALE_SUFFIX.length());
            ending = FIELD_LOCALE_SUFFIX;
        }
        else if (field.endsWith(FIELD_ENCODING_SUFFIX))
        {
            propertyFieldName = field.substring(field.startsWith("@") ? 1 : 0, field.length() - FIELD_ENCODING_SUFFIX.length());
            ending = FIELD_ENCODING_SUFFIX;
        }
        else if (field.endsWith(FIELD_CONTENT_DOC_ID_SUFFIX))
        {
            propertyFieldName = field.substring(field.startsWith("@") ? 1 : 0, field.length() - FIELD_CONTENT_DOC_ID_SUFFIX.length());
            ending = FIELD_CONTENT_DOC_ID_SUFFIX;
        }
        else if (field.endsWith(FIELD_TRANSFORMATION_EXCEPTION_SUFFIX))
        {
            propertyFieldName = field.substring(field.startsWith("@") ? 1 : 0, field.length() - FIELD_TRANSFORMATION_EXCEPTION_SUFFIX.length());
            ending = FIELD_TRANSFORMATION_EXCEPTION_SUFFIX;
        }
        else if (field.endsWith(FIELD_TRANSFORMATION_TIME_SUFFIX))
        {
            propertyFieldName = field.substring(field.startsWith("@") ? 1 : 0, field.length() - FIELD_TRANSFORMATION_TIME_SUFFIX.length());
            ending = FIELD_TRANSFORMATION_TIME_SUFFIX;
        }
        else if (field.endsWith(FIELD_TRANSFORMATION_STATUS_SUFFIX))
        {
            propertyFieldName = field.substring(field.startsWith("@") ? 1 : 0, field.length() - FIELD_TRANSFORMATION_STATUS_SUFFIX.length());
            ending = FIELD_TRANSFORMATION_STATUS_SUFFIX;
        }
        // ordering matters .__.u before .u 
        else if (field.endsWith(FIELD_SOLR_NOLOCALE_UNTOKENISED_SUFFIX))
        {
            propertyFieldName = field.substring(field.startsWith("@") ? 1 : 0, field.length() - FIELD_SOLR_NOLOCALE_UNTOKENISED_SUFFIX.length());
            ending = FIELD_SOLR_NOLOCALE_UNTOKENISED_SUFFIX;
        }
        else if (field.endsWith(FIELD_SOLR_LOCALISED_UNTOKENISED_SUFFIX))
        {
            propertyFieldName = field.substring(field.startsWith("@") ? 1 : 0, field.length() - FIELD_SOLR_LOCALISED_UNTOKENISED_SUFFIX.length());
            ending = FIELD_SOLR_LOCALISED_UNTOKENISED_SUFFIX;
        }
        else if (field.endsWith(FIELD_SOLR_NOLOCALE_TOKENISED_SUFFIX))
        {
            propertyFieldName = field.substring(field.startsWith("@") ? 1 : 0, field.length() - FIELD_SOLR_NOLOCALE_TOKENISED_SUFFIX.length());
            ending = FIELD_SOLR_NOLOCALE_TOKENISED_SUFFIX;
        }
        else
        {
            propertyFieldName = field.substring(field.startsWith("@") ? 1 : 0);
        }
        return new Pair<>(propertyFieldName, ending);
    }
 
}
