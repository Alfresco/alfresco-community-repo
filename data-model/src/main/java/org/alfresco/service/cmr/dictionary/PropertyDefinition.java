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

import java.util.List;
import java.util.Locale;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.repo.dictionary.Facetable;
import org.alfresco.repo.dictionary.IndexTokenisationMode;
import org.alfresco.service.cmr.i18n.MessageLookup;
import org.alfresco.service.namespace.QName;

/**
 * Read-only definition of a Property.
 * 
 * @author David Caruana
 */
@AlfrescoPublicApi
public interface PropertyDefinition extends ClassAttributeDefinition
{
    /**
     * @return defining model
     */
    public ModelDefinition getModel();

    /**
     * @return the qualified name of the property
     */
    public QName getName();

    /**
     * @deprecated The problem identified in MNT-413 will still exist
     */
    public String getTitle();

    /**
     * @deprecated The problem identified in MNT-413 will still exist
     */
    public String getDescription();

    /**
     * @return the human-readable class title
     */
    public String getTitle(MessageLookup messageLookup);

    /**
     * @return the human-readable class title in the specified Locale, if available.
     * @since 5.0
     */
    public String getTitle(MessageLookup messageLookup, Locale locale);

    /**
     * @return the human-readable class description
     */
    public String getDescription(MessageLookup messageLookup);

    /**
     * @return the human-readable class description in the specified Locale, if available.
     * @since 5.0
     */
    public String getDescription(MessageLookup messageLookup, Locale locale);

    /**
     * @return the default value
     */
    public String getDefaultValue();

    /**
     * @return the qualified name of the property type
     */
    public DataTypeDefinition getDataType();

    /**
     * @return Returns the owning class's defintion
     */
    public ClassDefinition getContainerClass();

    public boolean isOverride();

    /**
     * @return true => multi-valued, false => single-valued
     */
    public boolean isMultiValued();

    /**
     * @return true => mandatory, false => optional
     */
    public boolean isMandatory();

    /**
     * @return Returns true if the system enforces the presence of
     *         {@link #isMandatory() mandatory} properties, or false if the
     *         system just marks objects that don't have all mandatory
     *         properties present.
     */
    public boolean isMandatoryEnforced();

    /**
     * @return true => system maintained, false => client may maintain
     */
    public boolean isProtected();

    /**
     * @return true => indexed, false => not indexed
     */
    public boolean isIndexed();

    /**
     * @return true => stored in index
     */
    public boolean isStoredInIndex();

    /**
     * @return IndexTokenisationMode.TREU => tokenised when it is indexed (the
     *         stored value will not be tokenised)
     */
    public IndexTokenisationMode getIndexTokenisationMode();
    
    /**
     * @return if this field shoul be faceted
     */
    public Facetable getFacetable();

    /**
     * All non atomic properties will be indexed at the same time.
     * 
     * @return true => The attribute must be indexed in the commit of the
     *         transaction. false => the indexing will be done in the background
     *         and may be out of date.
     */
    public boolean isIndexedAtomically();

    /**
     * Get all constraints that apply to the property value
     * 
     * @return Returns a list of property constraint definitions
     */
    public List<ConstraintDefinition> getConstraints();
    
    /**
     * Get the name of the property bundle that defines analyser mappings for this class.
     * @return the resource or null if not set.
     */
    public String getAnalyserResourceBundleName();

    /**
     * @param locale Locale
     * @return String
     */
    public String resolveAnalyserClassName(Locale locale);
    
    /**
     * 
     * @return String
     */
    public String resolveAnalyserClassName();
}
