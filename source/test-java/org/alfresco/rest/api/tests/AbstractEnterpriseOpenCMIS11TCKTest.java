package org.alfresco.rest.api.tests;

import java.util.Map;

import org.apache.chemistry.opencmis.tck.impl.AbstractSessionTestGroup;
import org.apache.chemistry.opencmis.tck.impl.JUnitHelper;
import org.apache.chemistry.opencmis.tck.tests.basics.BasicsTestGroup;
import org.apache.chemistry.opencmis.tck.tests.control.ControlTestGroup;
import org.apache.chemistry.opencmis.tck.tests.crud.ChangeTokenTest;
import org.apache.chemistry.opencmis.tck.tests.crud.ContentRangesTest;
import org.apache.chemistry.opencmis.tck.tests.crud.CopyTest;
import org.apache.chemistry.opencmis.tck.tests.crud.CreateAndDeleteDocumentTest;
import org.apache.chemistry.opencmis.tck.tests.crud.CreateAndDeleteFolderTest;
import org.apache.chemistry.opencmis.tck.tests.crud.CreateAndDeleteItemTest;
import org.apache.chemistry.opencmis.tck.tests.crud.CreateAndDeleteRelationshipTest;
import org.apache.chemistry.opencmis.tck.tests.crud.CreateBigDocument;
import org.apache.chemistry.opencmis.tck.tests.crud.CreateDocumentWithoutContent;
import org.apache.chemistry.opencmis.tck.tests.crud.DeleteTreeTest;
import org.apache.chemistry.opencmis.tck.tests.crud.MoveTest;
import org.apache.chemistry.opencmis.tck.tests.crud.NameCharsetTest;
import org.apache.chemistry.opencmis.tck.tests.crud.OperationContextTest;
import org.apache.chemistry.opencmis.tck.tests.crud.SetAndDeleteContentTest;
import org.apache.chemistry.opencmis.tck.tests.crud.UpdateSmokeTest;
import org.apache.chemistry.opencmis.tck.tests.filing.FilingTestGroup;
import org.apache.chemistry.opencmis.tck.tests.query.QueryTestGroup;
import org.apache.chemistry.opencmis.tck.tests.versioning.VersionDeleteTest;
import org.apache.chemistry.opencmis.tck.tests.versioning.VersioningSmokeTest;
import org.apache.chemistry.opencmis.tck.tests.versioning.VersioningStateCreateTest;
import org.junit.Test;

public abstract class AbstractEnterpriseOpenCMIS11TCKTest extends AbstractEnterpriseOpenCMISTCKTest
{
    @Test
    public void testCMISTCKBasics() throws Exception
    {
        BasicsTestGroup basicsTestGroup = new BasicsTestGroup();
        JUnitHelper.run(basicsTestGroup);
    }

    @Test
    public void testCMISTCKCRUD() throws Exception
    {
        OverrideCRUDTestGroup crudTestGroup = new OverrideCRUDTestGroup();
        JUnitHelper.run(crudTestGroup);
    }

    @Test
    public void testCMISTCKVersioning() throws Exception
    {
        OverrideVersioningTestGroup versioningTestGroup = new OverrideVersioningTestGroup();
        JUnitHelper.run(versioningTestGroup);
    }

    @Test
    public void testCMISTCKFiling() throws Exception
    {
        FilingTestGroup filingTestGroup = new FilingTestGroup();
        JUnitHelper.run(filingTestGroup);
    }

    @Test
    public void testCMISTCKControl() throws Exception
    {
        ControlTestGroup controlTestGroup = new ControlTestGroup();
        JUnitHelper.run(controlTestGroup);
    }

    @Test
    public void testCMISTCKQuery() throws Exception
    {
        QueryTestGroup queryTestGroup = new QueryTestGroup();
        JUnitHelper.run(queryTestGroup);
    }

    class OverrideVersioningTestGroup extends AbstractSessionTestGroup
    {
        @Override
        public void init(Map<String, String> parameters) throws Exception
        {
            super.init(parameters);

            setName("Versioning Test Group");
            setDescription("Versioning tests.");

            addTest(new VersioningSmokeTest());
            addTest(new VersionDeleteTest());
            addTest(new VersioningStateCreateTest());
            // relies on Solr being available
//            addTest(new CheckedOutTest());
        }
    }
        
    class OverrideCRUDTestGroup extends AbstractSessionTestGroup
    {
        @Override
        public void init(Map<String, String> parameters) throws Exception
        {
            super.init(parameters);

            setName("CRUD Test Group");
            setDescription("Create, Read, Update, and Delete tests.");

            addTest(new CreateAndDeleteFolderTest());
            addTest(new CreateAndDeleteDocumentTest());
            addTest(new CreateBigDocument());
            addTest(new CreateDocumentWithoutContent());
            addTest(new NameCharsetTest());
            addTest(new CreateAndDeleteRelationshipTest());
            addTest(new CreateAndDeleteItemTest());
            addTest(new UpdateSmokeTest());
            // TCK fails because we support only 1 object in bulk update at present
            // See ACE-34
            //addTest(new BulkUpdatePropertiesTest());
            addTest(new SetAndDeleteContentTest());
            addTest(new ChangeTokenTest());
            addTest(new ContentRangesTest());
            addTest(new CopyTest());
            addTest(new MoveTest());
            addTest(new DeleteTreeTest());
            addTest(new OperationContextTest());
        }
    }
}
