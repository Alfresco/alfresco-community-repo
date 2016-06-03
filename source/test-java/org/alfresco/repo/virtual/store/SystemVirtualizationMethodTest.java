
package org.alfresco.repo.virtual.store;

import java.io.InputStream;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.virtual.VirtualizationIntegrationTest;
import org.alfresco.repo.virtual.VirtualContentModel;
import org.alfresco.repo.virtual.VirtualizationException;
import org.alfresco.repo.virtual.ref.Reference;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

public class SystemVirtualizationMethodTest extends VirtualizationIntegrationTest
{

    private static Log logger = LogFactory.getLog(VirtualStoreImplTest.class);

    private SystemVirtualizationMethod systemVirtualizationMethod;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        systemVirtualizationMethod = ctx.getBean("systemVirtualizationMethod",
                                                 SystemVirtualizationMethod.class);

    }

    @Test
    public void testDefaultSyspath() throws Exception
    {
        NodeRef vf = createVirtualizedFolder(testRootFolder.getNodeRef(),
                                             "SystemVirtualizationMethodTest_testDefaultSyspath",
                                             null);
        assertFalse(systemVirtualizationMethod.canVirtualize(environment,
                                                             vf));
        try
        {
            systemVirtualizationMethod.virtualize(environment,
                                                  vf);
            fail("Should not be able to virtualize non-virtualizable nodes.");
        }
        catch (VirtualizationException e)
        {
            logger.info(e);
        }
    }

    @Test
    public void testNonExistentClassPathResource() throws Exception
    {
        NodeRef vf = createVirtualizedFolder(testRootFolder.getNodeRef(),
                                             "SystemVirtualizationMethodTest_testNonExistentClassPathResource",
                                             "C/org/alfresco/repo/virtual/template/nonExistetJsonTemplate.json");
        assertFalse(systemVirtualizationMethod.canVirtualize(environment,
                                                             vf));
    }

    @Test
    public void testNonExistentRepoResource() throws Exception
    {
        NodeRef vf = createVirtualizedFolder(testRootFolder.getNodeRef(),
                                             "SystemVirtualizationMethodTest_testNonExistentRepoResource",
                                             "Nworkspace://SpacesStore/nonExistentRepoNode");
        assertFalse(systemVirtualizationMethod.canVirtualize(environment,
                                                             vf));
    }

    @Test
    public void testTemplateFromRepository() throws Exception
    {
        InputStream openContentStream = environment.openContentStream(TEST_TEMPLATE_1_JSON_CLASSPATH);
        NodeRef repositoryTemplate = fileAndFolderService
                    .create(testRootFolder.getNodeRef(),
                            "test1.json",
                            VirtualContentModel.TYPE_VIRTUAL_FOLDER_TEMPLATE)
                        .getNodeRef();
        ContentWriter writer = contentService.getWriter(repositoryTemplate,
                                                        ContentModel.PROP_CONTENT,
                                                        true);
        writer.setMimetype("application/json");
        writer.putContent(openContentStream);

        NodeRef vf = createVirtualizedFolder(testRootFolder.getNodeRef(),
                                             "repositoryVirtualizedFolder",
                                             "N" + repositoryTemplate.toString());
        assertTrue(systemVirtualizationMethod.canVirtualize(environment,
                                                            vf));

        Reference ref = systemVirtualizationMethod.virtualize(environment,
                                                              vf);
        assertNotNull(ref);

        assertNotNull(nodeService.getChildByName(ref.toNodeRef(),
                                                 ContentModel.ASSOC_CONTAINS,
                                                 "Node1"));
        assertNotNull(nodeService.getChildByName(ref.toNodeRef(),
                                                 ContentModel.ASSOC_CONTAINS,
                                                 "Node2"));
    }

    @Test
    public void testTemplateFromClassPath() throws Exception
    {
        NodeRef vf = createVirtualizedFolder(testRootFolder.getNodeRef(),
                                             "classpathVirtualizedFolder",
                                             TEST_TEMPLATE_1_JSON_SYS_PATH);
        assertTrue(systemVirtualizationMethod.canVirtualize(environment,
                                                            vf));

        Reference ref = systemVirtualizationMethod.virtualize(environment,
                                                              vf);
        assertNotNull(ref);

        assertNotNull(nodeService.getChildByName(ref.toNodeRef(),
                                                 ContentModel.ASSOC_CONTAINS,
                                                 "Node1"));
        assertNotNull(nodeService.getChildByName(ref.toNodeRef(),
                                                 ContentModel.ASSOC_CONTAINS,
                                                 "Node2"));

    }
}
