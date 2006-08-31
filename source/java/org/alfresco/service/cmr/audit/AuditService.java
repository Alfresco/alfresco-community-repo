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

import org.alfresco.service.NotAuditable;
import org.alfresco.service.PublicService;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * The public API by which applications can create audit entries.
 * This does not affect auditing using method interceptors.
 * The information recorded can not be confused between the two.
 * 
 * This API could be used by an audit action.
 * 
 * @author Andy Hind
 */
@PublicService
public interface AuditService
{

    /**
     * Add an application audit entry.
     * 
     * @param source -
     *            a string that represents the application
     * @param description -
     *            the audit entry
     */
    @NotAuditable
    public void audit(String source, String description);

    /**
     * 
     * @param source -
     *            a string that represents the application
     * @param description -
     *            the audit entry
     * @param key -
     *            a node ref to use as the key for filtering etc
     */
    @NotAuditable
    public void audit(String source, String description, NodeRef key);

    /**
     * 
     * @param source -
     *            a string that represents the application
     * @param description -
     *            the audit entry
     * @param args -
     *            an arbitrary list of parameters
     */
    @NotAuditable
    public void audit(String source, String description, Object... args);

    /**
     * 
     * @param source -
     *            a string that represents the application
     * @param description -
     *            the audit entry *
     * @param key -
     *            a node ref to use as the key for filtering etc
     * @param args -
     *            an arbitrary list of parameters
     */
    @NotAuditable
    public void audit(String source, String description, NodeRef key, Object... args);

}
