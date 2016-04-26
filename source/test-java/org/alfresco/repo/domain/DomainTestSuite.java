package org.alfresco.repo.domain;

import org.alfresco.repo.domain.audit.AuditDAOTest;
import org.alfresco.repo.domain.contentdata.ContentDataDAOTest;
import org.alfresco.repo.domain.encoding.EncodingDAOTest;
import org.alfresco.repo.domain.locale.LocaleDAOTest;
import org.alfresco.repo.domain.locks.LockDAOTest;
import org.alfresco.repo.domain.mimetype.MimetypeDAOTest;
import org.alfresco.repo.domain.node.NodeDAOTest;
import org.alfresco.repo.domain.patch.AppliedPatchDAOTest;
import org.alfresco.repo.domain.permissions.AclCrudDAOTest;
import org.alfresco.repo.domain.propval.PropertyValueCleanupTest;
import org.alfresco.repo.domain.propval.PropertyValueDAOTest;
import org.alfresco.repo.domain.qname.QNameDAOTest;
import org.alfresco.repo.domain.query.CannedQueryDAOTest;
import org.alfresco.repo.domain.solr.SOLRDAOTest;
import org.alfresco.repo.domain.tenant.TenantAdminDAOTest;
import org.alfresco.repo.domain.usage.UsageDAOTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Suite for domain-related tests.
 * 
 * @author Derek Hulley
 */
@RunWith(Suite.class)
@Suite.SuiteClasses(
{
    NodeDAOTest.class,
    ContentDataDAOTest.class,
    EncodingDAOTest.class,
    LockDAOTest.class,
    MimetypeDAOTest.class,
    LocaleDAOTest.class,
    QNameDAOTest.class,
    PropertyValueDAOTest.class,
    PropertyValueCleanupTest.class,
    AuditDAOTest.class,
    AppliedPatchDAOTest.class,
    AclCrudDAOTest.class,
    UsageDAOTest.class,
    SOLRDAOTest.class,
    TenantAdminDAOTest.class,
    CannedQueryDAOTest.class
})
public class DomainTestSuite
{
    // Intentionally empty
}
