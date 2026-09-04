/*
 * #%L
 * Alfresco Repository
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
package org.alfresco.repo.domain.permissions;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Test;

import org.alfresco.service.transaction.TransactionService;

public class UnusedAclCleanerTest
{
    private final AclCrudDAO aclCrudDAO = mock(AclCrudDAO.class);

    @Test
    public void cleanupBatchDeletesAtMostConfiguredCandidates()
    {
        UnusedAclCleaner cleaner = new UnusedAclCleaner();
        cleaner.setAclCrudDAO(aclCrudDAO);
        cleaner.setBatchSize(2);
        when(aclCrudDAO.getUnusedAclIds(0, 2)).thenReturn(Arrays.asList(11L, 12L));
        when(aclCrudDAO.deleteUnusedAcl(11L)).thenReturn(true);
        when(aclCrudDAO.deleteUnusedAcl(12L)).thenReturn(false);

        assertEquals(1, cleaner.cleanupBatch());
        verify(aclCrudDAO).deleteUnusedAcl(11L);
        verify(aclCrudDAO).deleteUnusedAcl(12L);
    }

    @Test
    public void disabledCleanerDoesNothing()
    {
        UnusedAclCleaner cleaner = new UnusedAclCleaner();
        cleaner.setAclCrudDAO(aclCrudDAO);
        cleaner.setTransactionService(mock(TransactionService.class));
        cleaner.setEnabled(false);

        assertEquals(0, cleaner.execute());
        verifyNoInteractions(aclCrudDAO);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsInvalidBatchSize()
    {
        new UnusedAclCleaner().setBatchSize(0);
    }
}