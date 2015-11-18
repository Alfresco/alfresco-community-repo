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
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Base class for classification enforcement tests for the search action
 *
 * @author Tuna Aksoy
 * @since 2.4.a
 */
public abstract class SearchClassificationEnforcementTestBase extends BaseRMTestCase
{
    protected String testUser;
    protected static final String TOP_SECRET_ID = "TS";
    protected static final String SECRET_ID = "S";
    protected static final String REASON = "Test Reason 1";
    /** Classified properties for top secret. */
    protected ClassificationAspectProperties propertiesDTO1;
    /** Classified properties for secret. */
    protected ClassificationAspectProperties propertiesDTO2;

    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        propertiesDTO1 = new ClassificationAspectProperties();
        propertiesDTO1.setClassificationLevelId(TOP_SECRET_ID);
        propertiesDTO1.setClassifiedBy(generate());
        propertiesDTO1.setClassificationAgency(generate());
        propertiesDTO1.setClassificationReasonIds(Collections.singleton(REASON));
        propertiesDTO2 = new ClassificationAspectProperties();
        propertiesDTO2.setClassificationLevelId(SECRET_ID);
        propertiesDTO2.setClassifiedBy(generate());
        propertiesDTO2.setClassificationAgency(generate());
        propertiesDTO2.setClassificationReasonIds(Collections.singleton(REASON));
    }

    protected abstract List<NodeRef> search(String searchQuery);

    private List<NodeRef> search(String searchQuery, String userName)
    {
        return doTestInTransaction(new Test<List<NodeRef>>()
        {
            @Override
            public List<NodeRef> run()
            {
                return search(searchQuery);
            }
        }, userName);
    }

    protected List<NodeRef> searchAsAdmin(String searchQuery)
    {
        return search(searchQuery, getAdminUserName());
    }

    protected List<NodeRef> searchAsTestUser(String searchQuery)
    {
        return search(searchQuery, testUser);
    }
}
