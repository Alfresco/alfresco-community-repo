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
package org.alfresco.repo.audit;

import java.util.List;

import org.alfresco.service.cmr.audit.AuditInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.aopalliance.intercept.MethodInvocation;

/**
 * The audit component.
 * 
 * Used by the AuditService and AuditMethodInterceptor to insert audit entries.
 * 
 * @author Andy Hind
 */
public interface AuditComponent
{
    /**
     * Audit entry point for method interceptors.
     * 
     * @param methodInvocation
     */
    public Object audit(MethodInvocation methodInvocation) throws Throwable;

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
    public void audit(String source, String description, NodeRef key, Object... args);
    
    /**
     * Get the audit trail for a node.
     * 
     * @param nodeRef
     * @return
     */
    public List<AuditInfo> getAuditTrail(NodeRef nodeRef);


}
