/*
 * Copyright (C) 2005 Alfresco, Inc.
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
package org.alfresco.service.cmr.dictionary;

import java.util.Locale;

import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;


/**
 * Read-only definition of a Data Type
 * 
 * @author David Caruana
 */
public interface DataTypeDefinition
{
    //
    // Built-in Property Types
    //
    public QName ANY = QName.createQName(NamespaceService.DICTIONARY_MODEL_1_0_URI, "any");
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
    
    
    /**
     * @return defining model
     */
    public ModelDefinition getModel();
    
    /**
     * @return the qualified name of the data type
     */
    public QName getName();
    
    /**
     * @return the human-readable class title 
     */
    public String getTitle();
    
    /**
     * @return the human-readable class description 
     */
    public String getDescription();

    /**
     * @return the indexing analyser class
     */
    public String getAnalyserClassName();
    
    /**
     * @return the indexing analyser class for the specified locale
     */
    public String getAnalyserClassName(Locale locale);
    
    /**
     * @return the equivalent java class name (or null, if not mapped) 
     */
    public String getJavaClassName();
    
}
