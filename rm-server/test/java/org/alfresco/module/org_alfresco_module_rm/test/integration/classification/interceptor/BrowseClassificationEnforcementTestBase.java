/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.test.integration.classification.interceptor;

import static org.alfresco.repo.security.authentication.AuthenticationUtil.getAdminUserName;
import static org.alfresco.util.GUID.generate;

import java.util.Collections;
import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationAspectProperties;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Base class for classification enforcement tests for the browse action
 *
 * @author Tuna Aksoy
 * @since 3.0.a
 */
public abstract class BrowseClassificationEnforcementTestBase extends BaseRMTestCase
{
    protected String testUser;
    protected static final String LEVEL1 = "level1";
    protected static final String LEVEL2 = "level2";
    protected static final String REASON = "Test Reason 1";
    /** Classified properties for classification level 1. */
    protected ClassificationAspectProperties propertiesDTO1;
    /** Classified properties for classification level 2. */
    protected ClassificationAspectProperties propertiesDTO2;

    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        propertiesDTO1 = new ClassificationAspectProperties();
        propertiesDTO1.setClassificationLevelId(LEVEL1);
        propertiesDTO1.setClassifiedBy(generate());
        propertiesDTO1.setClassificationAgency(generate());
        propertiesDTO1.setClassificationReasonIds(Collections.singleton(REASON));
        propertiesDTO2 = new ClassificationAspectProperties();
        propertiesDTO2.setClassificationLevelId(LEVEL2);
        propertiesDTO2.setClassifiedBy(generate());
        propertiesDTO2.setClassificationAgency(generate());
        propertiesDTO2.setClassificationReasonIds(Collections.singleton(REASON));
    }

    protected List<ChildAssociationRef> browse(NodeRef folder, String userName)
    {
        return doTestInTransaction(new Test<List<ChildAssociationRef>>()
        {
            @Override
            public List<ChildAssociationRef> run()
            {
                return nodeService.getChildAssocs(folder);
            }
        }, userName);
    }

    protected List<ChildAssociationRef> browseAsAdmin(NodeRef folder)
    {
        return browse(folder, getAdminUserName());
    }

    protected List<ChildAssociationRef> browseAsTestUser(NodeRef folder)
    {
        return browse(folder, testUser);
    }
}
