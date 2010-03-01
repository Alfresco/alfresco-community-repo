/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.namespace.QName;

/**
 * Class holding properties associated with the <b>cm:auditable</b> aspect.
 * This aspect is common enough to warrant direct inclusion on the <b>Node</b> entity.
 * 
 * @author Derek Hulley
 * @since 2.2 SP2
 */
public class AuditableProperties
{
    private static Set<QName> auditablePropertyQNames;
    static
    {
        auditablePropertyQNames = new HashSet<QName>(13);
        auditablePropertyQNames.add(ContentModel.PROP_CREATOR);
        auditablePropertyQNames.add(ContentModel.PROP_CREATED);
        auditablePropertyQNames.add(ContentModel.PROP_MODIFIER);
        auditablePropertyQNames.add(ContentModel.PROP_MODIFIED);
        auditablePropertyQNames.add(ContentModel.PROP_ACCESSED);
    }
    
    /**
     * @return          Returns <tt>true</tt> if the property belongs to the <b>cm:auditable</b> aspect
     */
    public static boolean isAuditableProperty(QName qname)
    {
        return auditablePropertyQNames.contains(qname);
    }
    
    private String auditCreator;
    private String auditCreated;
    private String auditModifier;
    private String auditModified;
    private String auditAccessed;
    
    /**
     * Default constructor with all <tt>null</tt> values.
     */
    public AuditableProperties()
    {
    }
    
    /**
     * @param qname         the property name
     * @return              Returns the value of the <b>cm:auditable</b> property or <tt>null</tt>
     */
    public Serializable getAuditableProperty(QName qname)
    {
        if (qname.equals(ContentModel.PROP_CREATOR))
        {
            return auditCreator;
        }
        else if (qname.equals(ContentModel.PROP_CREATED))
        {
            return DefaultTypeConverter.INSTANCE.convert(Date.class, auditCreated);
        }
        else if (qname.equals(ContentModel.PROP_MODIFIER))
        {
            return auditModifier == null ? auditCreator : auditModifier;
        }
        else if (qname.equals(ContentModel.PROP_MODIFIED))
        {
            String dateStr = auditModified == null ? auditCreated : auditModified;
            return DefaultTypeConverter.INSTANCE.convert(Date.class, dateStr);
        }
        else if (qname.equals(ContentModel.PROP_ACCESSED))
        {
            return DefaultTypeConverter.INSTANCE.convert(Date.class, auditAccessed);
        }
        else
        {
            return null;
        }
    }
    
    /**
     * @param qname         the property name
     * @param value         the property value
     * @return              Returns <tt>true</tt> if the property was used
     * @deprecated          Deprecated from the start, but possibly useful code
     */
    @SuppressWarnings("unused")
    private boolean setAuditableProperty(QName qname, Serializable value)
    {
        if (qname.equals(ContentModel.PROP_CREATOR))
        {
            auditCreator = DefaultTypeConverter.INSTANCE.convert(String.class, value);
            return true;
        }
        if (qname.equals(ContentModel.PROP_MODIFIER))
        {
            auditModifier = DefaultTypeConverter.INSTANCE.convert(String.class, value);
            return true;
        }
        if (qname.equals(ContentModel.PROP_CREATED))
        {
            auditCreated = DefaultTypeConverter.INSTANCE.convert(String.class, value);
            return true;
        }
        if (qname.equals(ContentModel.PROP_MODIFIED))
        {
            auditModified = DefaultTypeConverter.INSTANCE.convert(String.class, value);
            return true;
        }
        if (qname.equals(ContentModel.PROP_ACCESSED))
        {
            auditAccessed = DefaultTypeConverter.INSTANCE.convert(String.class, value);
            return true;
        }
        else
        {
            return false;
        }
    }
    
    /**
     * @return          Returns a <tt>Map</tt> of auditable properties
     */
    public Map<QName, Serializable> getAuditableProperties()
    {
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>(7);
        properties.put(ContentModel.PROP_CREATOR, auditCreator);
        properties.put(ContentModel.PROP_CREATED, DefaultTypeConverter.INSTANCE.convert(Date.class, auditCreated));
        // cm:modifier - use cm:creator if not set
        if (auditModifier != null)
        {
            properties.put(ContentModel.PROP_MODIFIER, auditModifier);
        }
        else
        {
            properties.put(ContentModel.PROP_MODIFIER, auditCreator);
        }
        // cm:modified - use cm:created if not set
        if (auditModified != null)
        {
            properties.put(ContentModel.PROP_MODIFIED, DefaultTypeConverter.INSTANCE.convert(Date.class, auditModified));
        }
        else
        {
            properties.put(ContentModel.PROP_MODIFIED, DefaultTypeConverter.INSTANCE.convert(Date.class, auditCreated));
        }
        // Usually null
        if (auditAccessed != null)
        {
            properties.put(ContentModel.PROP_ACCESSED, DefaultTypeConverter.INSTANCE.convert(Date.class, auditAccessed));
        }
        return properties;
    }
    
    /**
     * Set all <b>cm:auditable</b> parameters as required.  Where possible, the creation and modification data
     * will be shared so as to reduce data duplication.
     * 
     * @param user      the username
     * @param date      the creation or modification date
     * @param force     <tt>true</tt> to force the values to overwrite any pre-existing values
     */
    public void setAuditValues(String user, Date date, boolean force)
    {
        String dateStr = DefaultTypeConverter.INSTANCE.convert(String.class, date);
        
        // Always set cm:creator and cm:created
        if (force || auditCreator == null)
        {
            auditCreator = user;
        }
        if (force || auditCreated == null)
        {
            auditCreated = dateStr;
        }
        auditModifier = user;
        auditModified = dateStr;
    }

    /**
     * Set all <b>cm:auditable</b> parameters as required, giving precedence to the supplied
     * property map.
     * 
     * @param user          the username
     * @param date          the creation or modification date
     * @param properties    the properties to override the user and date
     */
    public void setAuditValues(String user, Date date, Map<QName, Serializable> properties)
    {
        String dateStr = DefaultTypeConverter.INSTANCE.convert(String.class, date);
        if (properties.containsKey(ContentModel.PROP_CREATOR))
        {
            auditCreator = DefaultTypeConverter.INSTANCE.convert(
                    String.class,
                    properties.get(ContentModel.PROP_CREATOR));
        }
        else if (auditCreator == null)
        {
            auditCreator = user;
        }
        if (properties.containsKey(ContentModel.PROP_MODIFIER))
        {
            auditModifier = DefaultTypeConverter.INSTANCE.convert(
                    String.class,
                    properties.get(ContentModel.PROP_MODIFIER));
        }
        else if (auditModifier == null)
        {
            auditModifier = user;
        }
        if (properties.containsKey(ContentModel.PROP_CREATED))
        {
            auditCreated = DefaultTypeConverter.INSTANCE.convert(
                    String.class,
                    properties.get(ContentModel.PROP_CREATED));
        }
        else if (auditCreated == null)
        {
            auditCreated = dateStr;
        }
        if (properties.containsKey(ContentModel.PROP_MODIFIED))
        {
            auditModified = DefaultTypeConverter.INSTANCE.convert(
                    String.class,
                    properties.get(ContentModel.PROP_MODIFIED));
        }
        else if (auditModified == null)
        {
            auditModified = dateStr;
        }
        if (properties.containsKey(ContentModel.PROP_ACCESSED))
        {
            auditAccessed = DefaultTypeConverter.INSTANCE.convert(
                    String.class,
                    properties.get(ContentModel.PROP_ACCESSED));
        }
    }
    
    /**
     * For persistance use
     */
    @SuppressWarnings("unused")
    private String getAuditCreator()
    {
        return auditCreator;
    }

    /**
     * For persistance use
     */
    @SuppressWarnings("unused")
    private void setAuditCreator(String auditCreator)
    {
        this.auditCreator = auditCreator;
    }

    /**
     * For persistance use
     */
    @SuppressWarnings("unused")
    private String getAuditCreated()
    {
        return auditCreated;
    }

    /**
     * For persistance use
     */
    @SuppressWarnings("unused")
    private void setAuditCreated(String auditCreated)
    {
        this.auditCreated = auditCreated;
    }

    /**
     * For persistance use
     */
    @SuppressWarnings("unused")
    private String getAuditModifier()
    {
        return auditModifier;
    }

    /**
     * For persistance use
     */
    @SuppressWarnings("unused")
    private void setAuditModifier(String auditModifier)
    {
        this.auditModifier = auditModifier;
    }

    /**
     * For persistance use
     */
    @SuppressWarnings("unused")
    private String getAuditModified()
    {
        return auditModified;
    }

    /**
     * For persistance use
     */
    @SuppressWarnings("unused")
    private void setAuditModified(String auditModified)
    {
        this.auditModified = auditModified;
    }

    /**
     * For persistance use
     */
    @SuppressWarnings("unused")
    private String getAuditAccessed()
    {
        return auditAccessed;
    }

    /**
     * For persistance use
     */
    @SuppressWarnings("unused")
    private void setAuditAccessed(String auditAccessed)
    {
        this.auditAccessed = auditAccessed;
    }
}
