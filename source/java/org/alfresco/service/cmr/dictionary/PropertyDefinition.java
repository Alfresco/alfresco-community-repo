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

package org.alfresco.service.cmr.dictionary;

import java.util.List;

import org.alfresco.repo.dictionary.IndexTokenisationMode;
import org.alfresco.service.namespace.QName;

/**
 * Read-only definition of a Property.
 * 
 * @author David Caruana
 */
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
     * @return the human-readable class title
     */
    public String getTitle();

    /**
     * @return the human-readable class description
     */
    public String getDescription();

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
}
