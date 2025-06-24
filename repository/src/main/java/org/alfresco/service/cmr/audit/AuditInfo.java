/*
 * #%L
 * Alfresco Repository
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
package org.alfresco.service.cmr.audit;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;

/**
 * A single entry in an audit trail
 * 
 * @author Andy Hind
 */
public interface AuditInfo
{
    /**
     * The identifier for the application that performed the audit. Method interceptors around public services will use the string 'SystemMethodInterceptor'.
     * 
     * @return - the application (may not be null).
     */
    public String getAuditApplication();

    /**
     * The name of the method executed on a public service.
     * 
     * @return - the method name - this may be null for external audit entries.
     */
    public String getAuditMethod();

    /**
     * The public service on which a method was invoked.
     * 
     * @return - the service name - this may be null for external audit entries.
     */
    public String getAuditService();

    /**
     * The address for the client. (This will be null in version 1.4)
     * 
     * @return - the client address - may be null.
     */
    public String getClientAddress();

    /**
     * The timestamp for the audit entry.
     * 
     * @return Date
     */
    public Date getDate();

    /**
     * Is this entry recording an error?
     * 
     * @return boolean
     */
    public boolean isFail();

    /**
     * Was this audit entry subject to filtering (which must have been met if an entry is found). Filters are not applied in version 1.4.
     * 
     * @return boolean
     */
    public boolean isFiltered();

    /**
     * Get the host address of the server machine.
     * 
     * @return String
     */
    public String getHostAddress();

    /**
     * Get the ID of the key node.
     * 
     * @return - the id of the key node - this may be null if there is no key or the key is not a node ref.
     */
    public String getKeyGUID();

    /**
     * The serialized properties on the key node, if one exists, after the method invocation. Note these values are serialized before the method is called so they are unaffected by the method invocation. In V1.4 these are not stored.
     * 
     * @return Map
     */
    public Map<QName, Serializable> getKeyPropertiesAfter();

    /**
     * The serialized properties on the key node, if one exists, before the method invocation. In V1.4 these are not stored.
     * 
     * @return Map
     */
    public Map<QName, Serializable> getKeyPropertiesBefore();

    /**
     * The store ref for the key.
     * 
     * @return - the store ref - this may be null if there is no key.
     */
    public StoreRef getKeyStore();

    /**
     * The message entered for application audit entries.
     * 
     * @return - the audit message. This may be null, and will be null for audit entries generated from method invocations.
     */
    public String getMessage();

    /**
     * Get the serailized mehod arguments.
     * 
     * These are not stored in V1.4.
     * 
     * @return Serializable[]
     */
    public Serializable[] getMethodArguments();

    /**
     * Get the method arguments as strings.
     * 
     * @return String[]
     */
    public String[] getMethodArgumentsAsStrings();

    /**
     * Get the path to the key node, if one exists.
     * 
     * @return - the path or null.
     */
    public String getPath();

    /**
     * The serialized value of the return object.
     * 
     * This is not available in V1.4.
     * 
     * @return Serializable
     */
    public Serializable getReturnObject();

    /**
     * Get the return object string value.
     * 
     * @return - the string value of the return object. May be null if the method is of type void or returns null.
     */
    public String getReturnObjectAsString();

    /**
     * Get the session id.
     * 
     * This is not stored in V1.4.
     * 
     * @return String
     */
    public String getSessionId();

    /**
     * Get the deserialized error, if one occurred.
     * 
     * @return the throwable or null.
     */
    public Throwable getThrowable();

    /**
     * In 1.4, get the error message (no stack trace).
     * 
     * @return - the error message
     */
    public String getThrowableAsString();

    /**
     * Get the transaction id which caused the audit.
     * 
     * @return the Tx id (not null).
     */
    public String getTxId();

    /**
     * Get the name of the user who caused the audit entry.
     * 
     * @return - the user name / user authority (not null)
     */
    public String getUserIdentifier();

}
