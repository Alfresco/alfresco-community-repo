/*
 * #%L
 * Alfresco Enterprise Repository
 * %%
 * Copyright (C) 2024 Alfresco Software Limited
 * %%
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 * #L%
 */
package org.alfresco.repo.audit2;

import java.util.Map;

public record AuditRecord(String applicationId, long time, String username, Map<String, ?> auditData)
{}
