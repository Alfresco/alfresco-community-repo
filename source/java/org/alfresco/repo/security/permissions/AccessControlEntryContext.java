package org.alfresco.repo.security.permissions;

import java.io.Serializable;

public interface AccessControlEntryContext extends Serializable
{
    /**
     * Get the class context.
     * 
     * This is a space separated list of QNames 
     * with an optional + or minus 
     * 
     * +QName => Must be of this type or have the aspect
     * -Qname => Must not be of this type or have the aspect
     * +QName +QName +QName => Must have all of these types
     * -QName -Qname => Must not have any of these types
     * QName QName QName => Must have one of the types
     * QName => requires exact type match
     * QName~ => requires a match on the type or subtype
     * 
     * Supports () for grouping
     * 
     * @return String
     */
    public String getClassContext();
    
    /**
     * Get the property context
     * 
     * QName QName Qname => property types to which it applies
     * 
     * @return String
     */
    public String getPropertyContext(); 
    
    /**
     * Get the key value pair context
     * 
     * Serialized Map
     * 
     * @return String
     */
    public String getKVPContext();}
