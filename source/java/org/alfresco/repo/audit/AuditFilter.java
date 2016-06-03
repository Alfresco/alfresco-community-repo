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