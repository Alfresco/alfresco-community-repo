/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.domain;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.alfresco.repo.domain.audit.AuditDAOTest;
import org.alfresco.repo.domain.contentdata.ContentDataDAOTest;
import org.alfresco.repo.domain.encoding.EncodingDAOTest;
import org.alfresco.repo.domain.locale.LocaleDAOTest;
import org.alfresco.repo.domain.locks.LockDAOTest;
import org.alfresco.repo.domain.mimetype.MimetypeDAOTest;
import org.alfresco.repo.domain.node.NodeDAOTest;
import org.alfresco.repo.domain.patch.AppliedPatchDAOTest;
import org.alfresco.repo.domain.permissions.AclCrudDAOTest;
import org.alfresco.repo.domain.propval.PropertyValueDAOTest;
import org.alfresco.repo.domain.qname.QNameDAOTest;
import org.alfresco.repo.domain.query.CannedQueryDAOTest;
import org.alfresco.repo.domain.usage.UsageDAOTest;

/**
 * Suite for domain-related tests.
 * 
 * @author Derek Hulley
 */
public class DomainTestSuite extends TestSuite
{
    public static Test suite() 
    {
        TestSuite suite = new TestSuite();
        
        suite.addTestSuite(NodeDAOTest.class);
        suite.addTestSuite(ContentDataDAOTest.class);
        suite.addTestSuite(EncodingDAOTest.class);
        suite.addTestSuite(LockDAOTest.class);
        suite.addTestSuite(MimetypeDAOTest.class);
        suite.addTestSuite(LocaleDAOTest.class);
        suite.addTestSuite(PropertyValueTest.class);
        suite.addTestSuite(QNameDAOTest.class);
        suite.addTestSuite(PropertyValueDAOTest.class);
        suite.addTestSuite(AuditDAOTest.class);
        suite.addTestSuite(AppliedPatchDAOTest.class);
        suite.addTestSuite(AclCrudDAOTest.class);
        suite.addTestSuite(UsageDAOTest.class);
        suite.addTestSuite(CannedQueryDAOTest.class);
        
        return suite;
    }
}
