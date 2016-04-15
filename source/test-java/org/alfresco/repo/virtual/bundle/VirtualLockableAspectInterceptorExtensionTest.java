
package org.alfresco.repo.virtual.bundle;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.lock.mem.Lifetime;
import org.alfresco.repo.virtual.VirtualizationIntegrationTest;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Test;

public class VirtualLockableAspectInterceptorExtensionTest extends VirtualizationIntegrationTest
{
    private LockService lockService;

    private NodeRef originalContentNodeRef;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        lockService = ctx.getBean("lockService",
                                  LockService.class);
        NodeRef node = nodeService.getChildByName(virtualFolder1NodeRef,
                                                  ContentModel.ASSOC_CONTAINS,
                                                  "Node1");
        originalContentNodeRef = createContent(node,
                                                       "originalContentName",
                                                       "0",
                                                       MimetypeMap.MIMETYPE_TEXT_PLAIN,
                                                       "UTF-8").getChildRef();
    }

    @Test
    public void testHasLockableAspect() throws Exception
    {
        assertFalse("Node should not be reported as lockable",
                    nodeService.hasAspect(originalContentNodeRef,
                                          ContentModel.ASPECT_LOCKABLE));

        lockService.lock(originalContentNodeRef,
                         LockType.WRITE_LOCK,
                         10,
                         Lifetime.EPHEMERAL,
                         null);

        assertTrue("Node should be reported as lockable",
                   nodeService.hasAspect(originalContentNodeRef,
                                         ContentModel.ASPECT_LOCKABLE));
    }

    @Test
    public void testGetAspectsHasLockableAspect() throws Exception
    {
        assertFalse("Node should not contain lockable aspect",
                    nodeService.getAspects(originalContentNodeRef).contains(ContentModel.ASPECT_LOCKABLE));

        lockService.lock(originalContentNodeRef,
                         LockType.WRITE_LOCK,
                         10,
                         Lifetime.EPHEMERAL,
                         null);

        assertTrue("Node should contain lockable aspect",
                   nodeService.getAspects(originalContentNodeRef).contains(ContentModel.ASPECT_LOCKABLE));
    }
}
