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
package org.alfresco.service.cmr.dictionary;

import java.util.Locale;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.service.cmr.i18n.MessageLookup;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;


/**
 * Read-only definition of a Data Type
 * 
 * @author David Caruana
 */
@AlfrescoPublicApi
public interface DataTypeDefinition
{
    //
    // Built-in Property Types
    //
    public QName ANY = QName.createQName(NamespaceService.DICTIONARY_MODEL_1_0_URI, "any");
    public QName ENCRYPTED = QName.createQName(NamespaceService.DICTIONARY_MODEL_1_0_URI, "encrypted");
    public QName TEXT = QName.createQName(NamespaceService.DICTIONARY_MODEL_1_0_URI, "text");
    public QName MLTEXT = QName.createQName(NamespaceService.DICTIONARY_MODEL_1_0_URI, "mltext");
    public QName CONTENT = QName.createQName(NamespaceService.DICTIONARY_MODEL_1_0_URI, "content");
    public QName INT = QName.createQName(NamespaceService.DICTIONARY_MODEL_1_0_URI, "int");
    public QName LONG = QName.createQName(NamespaceService.DICTIONARY_MODEL_1_0_URI, "long");
    public QName FLOAT = QName.createQName(NamespaceService.DICTIONARY_MODEL_1_0_URI, "float");
    public QName DOUBLE = QName.createQName(NamespaceService.DICTIONARY_MODEL_1_0_URI, "double");
    public QName DATE = QName.createQName(NamespaceService.DICTIONARY_MODEL_1_0_URI, "date");
    public QName DATETIME = QName.createQName(NamespaceService.DICTIONARY_MODEL_1_0_URI, "datetime");
    public QName BOOLEAN = QName.createQName(NamespaceService.DICTIONARY_MODEL_1_0_URI, "boolean");
    public QName QNAME = QName.createQName(NamespaceService.DICTIONARY_MODEL_1_0_URI, "qname");
    public QName CATEGORY = QName.createQName(NamespaceService.DICTIONARY_MODEL_1_0_URI, "category");
    public QName NODE_REF = QName.createQName(NamespaceService.DICTIONARY_MODEL_1_0_URI, "noderef");
    public QName CHILD_ASSOC_REF = QName.createQName(NamespaceService.DICTIONARY_MODEL_1_0_URI, "childassocref");
    public QName ASSOC_REF = QName.createQName(NamespaceService.DICTIONARY_MODEL_1_0_URI, "assocref");
    public QName PATH = QName.createQName(NamespaceService.DICTIONARY_MODEL_1_0_URI, "path");
    public QName LOCALE = QName.createQName(NamespaceService.DICTIONARY_MODEL_1_0_URI, "locale");
    public QName PERIOD = QName.createQName(NamespaceService.DICTIONARY_MODEL_1_0_URI, "period");
    
    
    /**
     * @return defining model
     */
    public ModelDefinition getModel();
    
    /**
     * @return the qualified name of the data type
     */
    public QName getName();
    
    /**
     * @deprecated The problem identified in MNT-413 will still exist
     * @see org.alfresco.service.cmr.dictionary.DataTypeDefinition#getTitle(org.alfresco.service.cmr.i18n.MessageLookup)
     */
    public String getTitle();

    /**
     * @deprecated The problem identified in MNT-413 will still exist
     * @see org.alfresco.service.cmr.dictionary.DataTypeDefinition#getDescription(org.alfresco.service.cmr.i18n.MessageLookup)
     */
    public String getDescription();
    
    /**
     * @return the human-readable class title 
     */
    public String getTitle(MessageLookup messageLookup);
    
    /**
     * @return the human-readable class description 
     */
    public String getDescription(MessageLookup messageLookup);
    
    /**
     * Get the name of the property bundle that defines analyser mappings for this data type (keyed by the type of the property) 
     * @return the resource or null if not set.
     */
    public String getAnalyserResourceBundleName();

    /**
     * @return the equivalent java class name (or null, if not mapped) 
     */
    public String getJavaClassName();
    
    /**
     * Get the default analyser class - used when no resource bundles can be found and no repository default is set.
     * @return String
     */
    public String getDefaultAnalyserClassName();

    /**
     * @param locale
     * @return String
     */
    public String resolveAnalyserClassName(Locale locale);
    
    /**
     * 
     * @return String
     */
    public String resolveAnalyserClassName();
    
}
