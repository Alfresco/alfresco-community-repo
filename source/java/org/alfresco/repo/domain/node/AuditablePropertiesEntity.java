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
package org.alfresco.repo.domain.node;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.namespace.QName;

/**
 * Class holding properties associated with the <b>cm:auditable</b> aspect.
 * This aspect is common enough to warrant direct inclusion on the <b>Node</b> entity.
 * 
 * @author Derek Hulley
 * @since 3.4
 */
public class AuditablePropertiesEntity
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
     * @return          Returns the <tt>QName</tt>s of the <b>cm:auditable</b> properties
     */
    public static Set<QName> getAuditablePropertyQNames()
    {
        return auditablePropertyQNames;
    }
    
    /**
     * @return          Returns <tt>true</tt> if the property belongs to the <b>cm:auditable</b> aspect
     */
    public static boolean isAuditableProperty(QName qname)
    {
        return auditablePropertyQNames.contains(qname);
    }
    
    /**
     * @param typeQName             a node type
     * @return                      <tt>true</tt> if the type given has the <b>cm:auditable</b> aspect by default
     */
    public static boolean hasAuditableAspect(QName typeQName, DictionaryService dictionaryService)
    {
        TypeDefinition typeDef = dictionaryService.getType(typeQName);
        if (typeDef == null)
        {
            return false;
        }
        return typeDef.getDefaultAspectNames().contains(ContentModel.ASPECT_AUDITABLE);
    }
    
    private String auditCreator;
    private String auditCreated;
    private String auditModifier;
    private String auditModified;
    private String auditAccessed;
    
    // Cached value for faster comparisons when updating modification time
    private long auditModifiedTime = -1L;
    
    /**
     * Default constructor with all <tt>null</tt> values.
     */
    public AuditablePropertiesEntity()
    {
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(512);
        sb.append("AuditablePropertiesEntity")
          .append("[ auditCreator=").append(auditCreator)
          .append(", auditCreated=").append(auditCreated)
          .append(", auditModifier=").append(auditModifier)
          .append(", auditModified=").append(auditModified)
          .append("]");
        return sb.toString();
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
     * @param user      the username; <tt>null</tt> to use the
     *                  {@link AuthenticationUtil#getFullyAuthenticatedUser() fully-authenticated user}
     * @param date      the creation or modification date; <tt>null</tt> to use the current system time
     * @param force     <tt>true</tt> to force the values to overwrite any pre-existing values
     * @param modifiedDateToleranceMs   the number of milliseconds' to tolerate before updating the
     *                  modification date.
     *                  Setting this to 1000L (say) will mean that the modification time will not be
     *                  changed if the existing value is withing 1000 ms of the new time.
     * @return          Returns <tt>true</tt> if there were any changes made, otherwise <tt>false</tt>
     */
    public boolean setAuditValues(String user, Date date, boolean force, long modifiedDateToleranceMs)
    {
        // Get a user if we need
        if (user == null)
        {
            user = AuthenticationUtil.getFullyAuthenticatedUser();
            if (user == null)
            {
                user = "unknown";
            }
        }
        // Get a date if we need
        if (date == null)
        {
            date = new Date();
        }
        
        String dateStr = DefaultTypeConverter.INSTANCE.convert(String.class, date);
        long dateTime = date.getTime();

        // Need to know if anything changed
        boolean changed = false;
        
        // Always set cm:creator and cm:created
        if (force || auditCreator == null)
        {
            auditCreator = user;
            changed = true;
        }
        if (force || auditCreated == null)
        {
            auditCreated = dateStr;
            changed = true;
        }
        if (auditModifier == null || !auditModifier.equals(user))
        {
            auditModifier = user;
            changed = true;
        }
        long lastModTime = getAuditModifiedTime();
        if (lastModTime < 0 || (lastModTime + modifiedDateToleranceMs) < dateTime)
        {
            // The time has moved on enough
            auditModifiedTime = dateTime;
            auditModified = dateStr;
            changed = true;
        }
        // Done
        return changed;
    }

    /**
     * Set all <b>cm:auditable</b> parameters as required, giving precedence to the supplied
     * property map.
     * 
     * @param user          the username
     * @param date          the creation or modification date
     * @param properties    the properties to override the user and date
     * @return              Returns <tt>true</tt> if there were any changes made, otherwise <tt>false</tt>
     */
    public boolean setAuditValues(String user, Date date, Map<QName, Serializable> properties)
    {
        // Need to know if anything changed
        boolean changed = false;
        
        if (properties.containsKey(ContentModel.PROP_CREATOR))
        {
            auditCreator = DefaultTypeConverter.INSTANCE.convert(
                    String.class,
                    properties.get(ContentModel.PROP_CREATOR));
            changed = true;
        }
        if (properties.containsKey(ContentModel.PROP_MODIFIER))
        {
            auditModifier = DefaultTypeConverter.INSTANCE.convert(
                    String.class,
                    properties.get(ContentModel.PROP_MODIFIER));
            changed = true;
        }
        if (properties.containsKey(ContentModel.PROP_CREATED))
        {
            auditCreated = DefaultTypeConverter.INSTANCE.convert(
                    String.class,
                    properties.get(ContentModel.PROP_CREATED));
            changed = true;
        }
        if (properties.containsKey(ContentModel.PROP_MODIFIED))
        {
            Date auditModifiedDate = DefaultTypeConverter.INSTANCE.convert(
                    Date.class,
                    properties.get(ContentModel.PROP_MODIFIED));
            auditModifiedTime = auditModifiedDate.getTime();
            auditModified = DefaultTypeConverter.INSTANCE.convert(
                    String.class,
                    auditModifiedDate);
            changed = true;
        }
        if (properties.containsKey(ContentModel.PROP_ACCESSED))
        {
            auditAccessed = DefaultTypeConverter.INSTANCE.convert(
                    String.class,
                    properties.get(ContentModel.PROP_ACCESSED));
            changed = true;
        }

        if (changed)
        {
            // Make sure populate any missing values
            // Get a user if we need
            if (user == null)
            {
                user = AuthenticationUtil.getFullyAuthenticatedUser();
                if (user == null)
                {
                    user = "unknown";
                }
            }
            // Get a date if we need
            if (date == null)
            {
                date = new Date();
            }
            
            String dateStr = DefaultTypeConverter.INSTANCE.convert(String.class, date);
            long dateTime = date.getTime();
    
            if (auditCreator == null)
            {
                auditCreator = user;
            }
            if (auditModifier == null)
            {
                auditModifier = user;
            }
            if (auditCreated == null)
            {
                auditCreated = dateStr;
            }
            if (auditModified == null)
            {
                auditModifiedTime = dateTime;
                auditModified = dateStr;
            }
        }
        // Done
        return changed;
    }
    
    /**
     * For persistance use
     */
    public String getAuditCreator()
    {
        return auditCreator;
    }

    /**
     * For persistance use
     */
    public void setAuditCreator(String auditCreator)
    {
        this.auditCreator = auditCreator;
    }

    /**
     * For persistance use
     */
    public String getAuditCreated()
    {
        return auditCreated;
    }

    /**
     * For persistance use
     */
    public void setAuditCreated(String auditCreated)
    {
        this.auditCreated = auditCreated;
    }

    /**
     * For persistance use
     */
    public String getAuditModifier()
    {
        return auditModifier;
    }

    /**
     * For persistance use
     */
    public void setAuditModifier(String auditModifier)
    {
        this.auditModifier = auditModifier;
    }

    /**
     * For persistance use
     */
    public String getAuditModified()
    {
        return auditModified;
    }

    /**
     * For internal use.  Provides access to the time (<tt>long</tt>) for the
     * {@link #getAuditModified() auditModified} property.
     */
    private long getAuditModifiedTime()
    {
        if (auditModifiedTime < 0 && auditModified != null)
        {
            auditModifiedTime = DefaultTypeConverter.INSTANCE.convert(Date.class, auditModified).getTime();
        }
        return auditModifiedTime;
    }

    /**
     * For persistance use
     */
    public void setAuditModified(String auditModified)
    {
        this.auditModified = auditModified;
    }

    /**
     * For persistance use
     */
    public String getAuditAccessed()
    {
        return auditAccessed;
    }

    /**
     * For persistance use
     */
    public void setAuditAccessed(String auditAccessed)
    {
        this.auditAccessed = auditAccessed;
    }
}
