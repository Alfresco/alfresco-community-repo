/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.rest.api.tests;

import java.util.Map;

import org.alfresco.opencmis.tck.tests.query.QueryForObjectCustom;
import org.alfresco.opencmis.tck.tests.query.QueryInFolderTestCustom;
import org.alfresco.opencmis.tck.tests.query.QueryLikeTestCustom;
import org.apache.chemistry.opencmis.tck.impl.AbstractSessionTestGroup;
import org.apache.chemistry.opencmis.tck.impl.JUnitHelper;
import org.apache.chemistry.opencmis.tck.tests.basics.BasicsTestGroup;
import org.apache.chemistry.opencmis.tck.tests.control.ControlTestGroup;
import org.apache.chemistry.opencmis.tck.tests.crud.BulkUpdatePropertiesTest;
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
import org.apache.chemistry.opencmis.tck.tests.query.ContentChangesSmokeTest;
import org.apache.chemistry.opencmis.tck.tests.query.QuerySmokeTest;
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
        CRUDTestGroup crudTestGroup = new CRUDTestGroup();
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
        OverrideQueryTestGroup queryTestGroup = new OverrideQueryTestGroup();
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
            addTest(new BulkUpdatePropertiesTest());
            addTest(new BulkUpdatePropertiesCustomTest());
            addTest(new SetAndDeleteContentTest());
            addTest(new ChangeTokenTest());
            addTest(new ContentRangesTest());
            addTest(new CopyTest());
            addTest(new MoveTest());
            addTest(new DeleteTreeTest());
            addTest(new OperationContextTest());
        }
    }

    public class OverrideQueryTestGroup extends AbstractSessionTestGroup
    {
        @Override
        public void init(Map<String, String> parameters) throws Exception
        {
            super.init(parameters);

            setName("Query Test Group");
            setDescription("Query and content changes tests.");

            addTest(new QuerySmokeTest());
            // The test fails on Lucene see MNT-11223
//            addTest(new QueryRootFolderTest());
            addTest(new QueryForObjectCustom());
            addTest(new QueryLikeTestCustom());
            addTest(new QueryInFolderTestCustom());
            addTest(new ContentChangesSmokeTest());
        }
    }
}
