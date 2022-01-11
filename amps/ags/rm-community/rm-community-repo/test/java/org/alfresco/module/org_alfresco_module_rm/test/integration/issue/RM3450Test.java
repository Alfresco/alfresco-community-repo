/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.module.org_alfresco_module_rm.test.integration.issue;

import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.module.org_alfresco_module_rm.test.util.TestModel;
import org.alfresco.repo.node.integrity.IntegrityException;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.util.GUID;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Integration test for RM-3450
 *
 * @author Roxana Lucanu
 * @since 2.4.1
 */
public class RM3450Test extends BaseRMTestCase
{

    private static final String MSG_CANNOT_CAST_TO_RM_TYPE = "rm.action.cast-to-rm-type";

    public void testRM3450() throws Exception
    {
        doTestInTransaction(new FailureTest
                (
                        I18NUtil.getMessage(MSG_CANNOT_CAST_TO_RM_TYPE),
                        IntegrityException.class
                )
        {
            @Override
            public void run() throws Exception
            {
                transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Object>()
                {
                    public Object execute() throws Exception
                    {
                        fileFolderService.create(unfiledContainer, GUID.generate(), TestModel.NOT_RM_FOLDER_TYPE).getNodeRef();
                        return null;
                    }
                }, false, true);
            }
        }, ADMIN_USER);
    }

}
