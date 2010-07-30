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
package org.alfresco.repo.audit.model;

import java.util.Map;

import org.alfresco.util.PathMapper;

/**
 * Interface for component used to store audit model definitions.
 * 
 * @author Derek Hulley
 * @since 3.3
 */
public interface AuditModelRegistry
{
    /** The name of the global enablement property. */
    public static final String AUDIT_PROPERTY_AUDIT_ENABLED = "audit.enabled";
    public static final String AUDIT_SCHEMA_LOCATION = "classpath:alfresco/audit/alfresco-audit-3.2.xsd";
    
    public static final String AUDIT_RESERVED_KEY_USERNAME = "username";
    public static final String AUDIT_RESERVED_KEY_SYSTEMTIME = "systemTime";

    /**
     * Method to load audit models into memory.  This method is also responsible for persisting
     * the audit models for later retrieval.
     * <p/>
     * Note, the models are loaded in a new transaction, so this method can be called by any code
     * at any time.
     */
    public void loadAuditModels();

    /**
     * Determines whether audit is globally enabled or disabled.
     * 
     * @return                      <code>true</code> if audit is enabled
     */
    public boolean isAuditEnabled();
    
    /**
     * Get a map of all audit applications key by name
     * 
     * @return                      the audit applications
     * 
     * @since 3.4
     */
    public Map<String, AuditApplication> getAuditApplications();

    /**
     * Get the application model for the given root key (as defined on the application)
     * 
     * @param key                   the key defined on the application
     * @return                      the java model (<tt>null</tt> if not found)
     */
    public AuditApplication getAuditApplicationByKey(String key);

    /**
     * Get the application model for the given application name
     * 
     * @param applicationName       the name of the audited application
     * @return                      the java model (<tt>null</tt> if not found)
     */
    public AuditApplication getAuditApplicationByName(String applicationName);

    /**
     * Get the path mapper.
     * @return                      the path mapper
     */
    public PathMapper getAuditPathMapper();
}