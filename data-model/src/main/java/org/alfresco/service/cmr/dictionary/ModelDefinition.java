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

import java.io.OutputStream;
import java.util.Collection;
import java.util.Date;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.service.cmr.i18n.MessageLookup;
import org.alfresco.service.namespace.QName;


/**
 * Read-only definition of a Model.
 * 
 * @author David Caruana
 */
@AlfrescoPublicApi
public interface ModelDefinition
{
    public static enum XMLBindingType
    {
    	DEFAULT
    	{
    		public String toString()
    		{
    			return "default";
    		}
    	}
    };

    /**
     * @return the model name
     */
    public QName getName();
    
    /**
     * @return the model description
     */
    public String getDescription(MessageLookup messageLookup);
    
    /**
     * @return the model author
     */
    public String getAuthor();
    
    /**
     * @return the date when the model was published
     */
    public Date getPublishedDate();
    
    /**
     * @return the model version
     */
    public String getVersion();
    
    /**
     * @return  the namespaces defined by this model
     */
    public Collection<NamespaceDefinition> getNamespaces();

    /**
     * @param uri  namespace uri
     * @return  true => model defines the uri
     */
    public boolean isNamespaceDefined(String uri);
    
    /**
     * @return  the namespaces imported by this model
     */
    public Collection<NamespaceDefinition> getImportedNamespaces();
    
    /**
     * @param uri  namespace uri
     * @return  true => model imports the uri
     */
    public boolean isNamespaceImported(String uri);

    public void toXML(XMLBindingType bindingType, OutputStream xml);

    public long getChecksum(XMLBindingType bindingType);
    
    /**
     * Get the name of the property bundle that defines analyser mappings for this model (keyed by the type of the property) 
     * @return the resource or null if not set.
     */
    public String getAnalyserResourceBundleName();

    /**
     * @return DictionaryDAO
     */
    public DictionaryDAO getDictionaryDAO();
}
