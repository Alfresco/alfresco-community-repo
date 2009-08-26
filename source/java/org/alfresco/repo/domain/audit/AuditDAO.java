/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.domain.audit;

import java.io.Serializable;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.audit.AuditState;
import org.alfresco.service.cmr.audit.AuditInfo;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.Pair;

/**
 * DAO services for <b>alf_audit_XXX</b> tables.
 * <p>
 * The older methods are supported by a different implementation and will eventually
 * be deprecated and phased out.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public interface AuditDAO
{
    /**
     * Create an audit entry.
     * 
     * @param auditInfo
     * @since 2.1
     */
    public void audit(AuditState auditInfo);

    /**
     * Get the audit trail for a node.
     * 
     * @since 2.1
     */
    public List<AuditInfo> getAuditTrail(NodeRef nodeRef);
    
    /*
     * V3.2 methods after here only, please
     */

    /**
     * Creates a new audit model entry or finds an existing one
     * 
     * @param               the URL of the configuration
     * @return              Returns the ID of the config matching the input stream and the
     *                      content storage details
     */
    Pair<Long, ContentData> getOrCreateAuditModel(URL url);
    
    /**
     * Creates a new audit session entry - there is no session re-use.
     * 
     * @param modelId       an existing audit model ID
     * @param application   the name of the application
     * @return              Returns the unique session ID
     */
    Long createAuditSession(Long modelId, String application);
    
    /**
     * Create a new audit entry with the given map of values.
     * 
     * @param sessionId     an existing audit session ID
     * @param time          the time (ms since epoch) to log the entry against
     * @param username      the authenticated user (<tt>null</tt> if not present)
     * @param values        the values to record
     * @return              Returns the unique entry ID
     */
    Long createAuditEntry(Long sessionId, long time, String username, Map<String, Serializable> values);
}
