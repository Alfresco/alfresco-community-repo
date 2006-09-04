/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.service.cmr.audit;

import java.io.Serializable;
import java.net.InetAddress;
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
    public InetAddress getClientAddress();

    /**
     * The timestamp for the audit entry.
     * 
     * @return
     */
    public Date getDate();

    /**
     * Is this entry recording an error?
     * 
     * @return
     */
    public boolean isFail();

    /**
     * Was this audit entry subject to filtering (which must have been met if an entry is found). Filters are not applied in version 1.4.
     * 
     * @return
     */
    public boolean isFiltered();

    /**
     * Get the host address of the server machine.
     * 
     * @return
     */
    public InetAddress getHostAddress();

    /**
     * Get the ID of the key node.
     * 
     * @return - the id of the key node - this may be null if there is no key or the key is not a node ref.
     */
    public String getKeyGUID();

    /**
     * The serialized properties on the key node, if one exists, after the method invocation. Note these values are serialized before the method is called so they are unaffected by
     * the method invocation. In V1.4 these are not stored.
     * 
     * @return
     */
    public Map<QName, Serializable> getKeyPropertiesAfter();

    /**
     * The serialized properties on the key node, if one exists, before the method invocation. In V1.4 these are not stored.
     * 
     * @return
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
     * @return
     */
    public Serializable[] getMethodArguments();

    /**
     * Get the method arguments as strings.
     * 
     * @return
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
     * @return
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
     * @return
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
