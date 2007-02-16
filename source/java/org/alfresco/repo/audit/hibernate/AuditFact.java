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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.audit.hibernate;

import java.util.Date;

public interface AuditFact
{

    public abstract String getArg1();

    public abstract String getArg2();

    public abstract String getArg3();

    public abstract String getArg4();

    public abstract String getArg5();

    public abstract AuditConfig getAuditConfig();

    public abstract AuditDate getAuditDate();

    public abstract AuditSource getAuditSource();

    public abstract String getClientInetAddress();

    public abstract Date getDate();

    public abstract String getException();

    public abstract boolean isFail();

    public abstract boolean isFiltered();

    public abstract String getHostInetAddress();

    public abstract long getId();

    public abstract String getMessage();

    public abstract String getNodeUUID();

    public abstract String getPath();

    public abstract String getReturnValue();

    public abstract String getSerialisedURL();

    public abstract String getSessionId();

    public abstract String getStoreId();

    public abstract String getStoreProtocol();

    public abstract String getTransactionId();

    public abstract String getUserId();

}