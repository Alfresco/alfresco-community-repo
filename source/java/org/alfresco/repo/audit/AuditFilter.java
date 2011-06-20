/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.repo.audit;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.repo.audit.model.AuditApplication;
import org.alfresco.repo.audit.model._3.AuditPath;

/**
 * Filter of audit map values before an audit record is written.
 * 
 * @author Alan Davis
 */
public interface AuditFilter
{
    /**
     * Returns {@code true} if the audit map values have not been discarded by audit filters.
     * @param rootPath String a base path of {@link AuditPath} key entries concatenated with the
     *        path separator '/' ({@link AuditApplication#AUDIT_PATH_SEPARATOR})
     * @param auditMap Map of values to audit, mapped by {@link AuditPath} key relative to root path.
     * @return {@code true} if the audit map values should be recorded.
     */
    boolean accept(String rootPath, Map<String, Serializable> auditMap);
}