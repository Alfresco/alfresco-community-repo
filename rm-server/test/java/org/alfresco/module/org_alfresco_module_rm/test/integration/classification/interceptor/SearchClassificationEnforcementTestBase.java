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

import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Base class for classification enforcement tests for the search action
 *
 * @author Tuna Aksoy
 * @since 3.0.a
 */
public abstract class SearchClassificationEnforcementTestBase extends BaseRMTestCase
{
    protected String testUser;
    protected static final String LEVEL1 = "level1";
    protected static final String LEVEL2 = "level2";
    protected static final String REASON = "Test Reason 1";

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
