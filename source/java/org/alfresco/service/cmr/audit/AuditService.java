/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.service.cmr.audit;

import java.util.List;

import org.alfresco.repo.audit.AuditState;
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
    
    
    /**
     * Get the audit trail for a node ref.
     * 
     * @param nodeRef - the node ref for which to get the audit trail.
     * @return - tha audit trail 
     */
    @NotAuditable
    public List<AuditInfo> getAuditTrail(NodeRef nodeRef);
}
