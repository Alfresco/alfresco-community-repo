/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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

package org.alfresco.repo.web.scripts.facet;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.QName;

/**
 * This interface defines a simple POJO/DTO for use in the FTL model and rendering in the JSON API.
 * @param T a type to ensure that the comparator is implemented in a typesafe way.
 * @since 5.0
 */
public abstract class FacetablePropertyFTL<T> implements Comparable<T>
{
    /** The localised title for this property. */
    protected final String localisedTitle;
    
    public FacetablePropertyFTL(String localisedTitle) { this.localisedTitle = localisedTitle; }
    
    // We use "*Qname*" (small 'n') in these accessors to make the FTL less ambiguous.
    public abstract String getShortQname();
    public abstract QName  getQname();
    public abstract String getDisplayName();
    public abstract QName  getContainerClassType();
    public abstract QName  getDataType();
    public abstract QName  getModelQname();
    
    public String getTitle() { return localisedTitle; }
    
    /** This class represents a normal Alfresco property which is facetable. */
    public static class StandardFacetablePropertyFTL extends FacetablePropertyFTL<StandardFacetablePropertyFTL>
    {
        /** The Alfresco property definition which declares this facetable property. */
        protected final PropertyDefinition propDef;
        
        /** A display name for this property. */
        protected final String displayName;
        
        /**
         * @param propDef The {@link PropertyDefinition}.
         * @param localisedTitle The localised title for this property e.g. "Titre".
         */
        public StandardFacetablePropertyFTL(PropertyDefinition propDef, String localisedTitle)
        {
            super(localisedTitle);
            
            this.propDef        = propDef;
            this.displayName    = getShortQname() + (localisedTitle == null ? "" : " (" + localisedTitle + ")");
        }
        
        @Override public String getShortQname()         { return propDef.getName().getPrefixString(); }
        
        @Override public QName  getQname()              { return propDef.getName(); }
        
        @Override public String getDisplayName()        { return displayName; }
        
        @Override public QName  getContainerClassType() { return propDef.getContainerClass().getName(); }
        
        @Override public QName  getDataType()           { return propDef.getDataType().getName(); }
        
        @Override public QName  getModelQname()         { return propDef.getModel().getName(); }
        
        @Override public boolean equals(Object obj)
        {
            if (this == obj)                  { return true; }
            if (obj == null)                  { return false; }
            if (getClass() != obj.getClass()) { return false; }
            
            StandardFacetablePropertyFTL other = (StandardFacetablePropertyFTL) obj;
            if (displayName == null)
            {
                if (other.displayName != null) { return false; }
            } else if (!displayName.equals(other.displayName)) { return false; }
            if (localisedTitle == null)
            {
                if (other.localisedTitle != null)
                    return false;
            } else if (!localisedTitle.equals(other.localisedTitle))
                return false;
            if (propDef == null)
            {
                if (other.propDef != null)
                    return false;
            } else if (!propDef.equals(other.propDef))
                return false;
            return true;
        }
        
        @Override public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((displayName == null) ? 0 : displayName.hashCode());
            result = prime * result + ((localisedTitle == null) ? 0 : localisedTitle.hashCode());
            result = prime * result + ((propDef == null) ? 0 : propDef.hashCode());
            return result;
        }
        
        @Override public int compareTo(StandardFacetablePropertyFTL that)
        {
            final int modelComparison = this.propDef.getModel().getName().compareTo(that.propDef.getModel().getName());
            final int classComparison = this.propDef.getContainerClass().getName().compareTo(that.propDef.getContainerClass().getName());
            final int propComparison  = this.propDef.getName().compareTo(that.propDef.getName());
            
            final int result;
            if      (modelComparison != 0) { result = modelComparison; }
            else if (classComparison != 0) { result = classComparison; }
            else                           { result = propComparison; }
            
            return result;
        }
    }
    
    /**
     * This class represents a facetable property, which is not actually an Alfresco
     * content property, but is closely associated with one.
     * Examples are the {@code size} and {@code MIME type} fields within the {@code cm:content} property type.
     */
    public static class SyntheticFacetablePropertyFTL extends FacetablePropertyFTL<SyntheticFacetablePropertyFTL>
    {
        /** The PropertyDefinition which 'contains' this synthetic property. */
        private final PropertyDefinition containingPropDef;
        
        /** This is the name of the synthetic property e.g. "size". Not localised. */
        private final String syntheticPropertyName;
        
        /** The type of this synthetic data property. */
        private final QName datatype;
        
        private final String displayName;
        
        /**
         * @param containingPropDef     The {@link PropertyDefinition}.
         * @param localisedTitle        The localised title of this synthetic property e.g. "taille".
         * @param syntheticPropertyName The synthetic property name e.g. "size".
         * @param datatype              The datatype of the synthetic property.
         */
        public SyntheticFacetablePropertyFTL(PropertyDefinition containingPropDef,
                                             String localisedTitle,
                                             String syntheticPropertyName,
                                             QName  datatype)
        {
            super(localisedTitle);
            this.containingPropDef     = containingPropDef;
            this.syntheticPropertyName = syntheticPropertyName;
            this.datatype              = datatype;
            this.displayName           = getShortQname() + (localisedTitle == null ? "" : " (" + localisedTitle + ")");
        }
        
        @Override public String getShortQname()
        {
            return containingPropDef.getName().getPrefixString() + "." + this.syntheticPropertyName;
        }
        
        @Override public QName getQname()
        {
            final QName containingPropQName = containingPropDef.getName();
            return QName.createQName(containingPropQName.getNamespaceURI(),
                                     containingPropQName.getLocalName() + "." + this.syntheticPropertyName);
        }
        
        @Override public QName getDataType()           { return datatype; }
        
        @Override public QName getContainerClassType() { return containingPropDef.getContainerClass().getName(); };
        
        @Override public QName getModelQname()         { return containingPropDef.getModel().getName(); }
        
        @Override public String getDisplayName()       { return displayName; }
        
        @Override public int compareTo(SyntheticFacetablePropertyFTL that)
        {
            final int modelComparison = this.containingPropDef.getModel().getName().compareTo(that.containingPropDef.getModel().getName());
            final int classComparison = this.containingPropDef.getContainerClass().getName().compareTo(that.containingPropDef.getContainerClass().getName());
            final int propComparison  = this.containingPropDef.getName().compareTo(that.containingPropDef.getName());
            final int displayNameComparison = this.displayName.compareTo(that.displayName);
            
            final int result;
            if      (modelComparison != 0) { result = modelComparison; }
            else if (classComparison != 0) { result = classComparison; }
            else if (propComparison != 0)  { result = propComparison; }
            else                           { result = displayNameComparison; }
            
            return result;
        }
        
        @Override public int hashCode()
        {
            final int prime = 31;
            int result = super.hashCode();
            result = prime
                    * result
                    + ((containingPropDef == null) ? 0 : containingPropDef
                            .hashCode());
            result = prime * result
                    + ((datatype == null) ? 0 : datatype.hashCode());
            result = prime * result
                    + ((displayName == null) ? 0 : displayName.hashCode());
            result = prime
                    * result
                    + ((syntheticPropertyName == null) ? 0
                            : syntheticPropertyName.hashCode());
            return result;
        }
        
        @Override public boolean equals(Object obj)
        {
            if (this == obj)
                return true;
            if (!super.equals(obj))
                return false;
            if (getClass() != obj.getClass())
                return false;
            SyntheticFacetablePropertyFTL other = (SyntheticFacetablePropertyFTL) obj;
            if (containingPropDef == null)
            {
                if (other.containingPropDef != null)
                    return false;
            } else if (!containingPropDef.equals(other.containingPropDef))
                return false;
            if (datatype == null)
            {
                if (other.datatype != null)
                    return false;
            } else if (!datatype.equals(other.datatype))
                return false;
            if (displayName == null)
            {
                if (other.displayName != null)
                    return false;
            } else if (!displayName.equals(other.displayName))
                return false;
            if (syntheticPropertyName == null)
            {
                if (other.syntheticPropertyName != null)
                    return false;
            } else if (!syntheticPropertyName
                    .equals(other.syntheticPropertyName))
                return false;
            return true;
        }
    }
    
    /**
     * This class represents a hard-coded facetable pseudo-property. It is not an Alfresco property
     * and yet it is something that Alfresco and SOLR can use for facetting.
     * Examples are the {@code TAG} and {@code SITE} facets.
     */
    public static class SpecialFacetablePropertyFTL extends FacetablePropertyFTL<SpecialFacetablePropertyFTL>
    {
        /** This is the name of the property e.g. "SITE". Not localised. */
        private final String name;
        
        private final String displayName;
        
        /**
         * @param localisedTitle        The localised title of this synthetic property e.g. "taille".
         */
        public SpecialFacetablePropertyFTL(String name, String localisedTitle)
        {
            super(localisedTitle);
            this.name = name;
            this.displayName = localisedTitle;
        }
        
        @Override public String getShortQname()        { return name; }
        
        @Override public QName getQname()              { return null; }
        
        @Override public QName getDataType()           { return null; }
        
        @Override public QName getContainerClassType() { return null; }
        
        @Override public QName getModelQname()         { return null; }
        
        @Override public String getDisplayName()       { return displayName; }
        
        @Override public int compareTo(SpecialFacetablePropertyFTL that)
        {
            return this.name.compareTo(that.name);
        }
        
        @Override public int hashCode()
        {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result
                    + ((displayName == null) ? 0 : displayName.hashCode());
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            return result;
        }
        
        @Override public boolean equals(Object obj)
        {
            if (this == obj)
                return true;
            if (!super.equals(obj))
                return false;
            if (getClass() != obj.getClass())
                return false;
            SpecialFacetablePropertyFTL other = (SpecialFacetablePropertyFTL) obj;
            if (displayName == null)
            {
                if (other.displayName != null)
                    return false;
            } else if (!displayName.equals(other.displayName))
                return false;
            if (name == null)
            {
                if (other.name != null)
                    return false;
            } else if (!name.equals(other.name))
                return false;
            return true;
        }
    }
    
    /**
     * In order to give deterministic responses when getting facetable properties,
     * all {@link FacetablePropertyFTL} instances are sorted. This comparator provides
     * the sorting implementation.
     */
    public static class FacetablePropertyFTLComparator implements Comparator<FacetablePropertyFTL<?>>
    {
        /** Used when sorting two objects of different types. */
        private final List<Class<?>> typeOrder = Arrays.asList(new Class<?>[] { SpecialFacetablePropertyFTL.class,
                                                                                SyntheticFacetablePropertyFTL.class,
                                                                                StandardFacetablePropertyFTL.class} );
        
        @SuppressWarnings({ "rawtypes", "unchecked" })
        @Override public int compare(FacetablePropertyFTL left, FacetablePropertyFTL right)
        {
            // First of all sort by the class according to the list above.
            if ( !left.getClass().equals(right.getClass())) { return typeOrder.indexOf(left.getClass()) -
                                                                     typeOrder.indexOf(right.getClass()); }
            else
            {
                // Otherwise we have two facetable properties of the same class.
                return left.compareTo(right);
            }
        }
    }
}