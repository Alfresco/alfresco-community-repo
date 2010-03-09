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
package org.alfresco.repo.transfer;

import org.alfresco.service.cmr.transfer.TransferService;
import org.alfresco.service.cmr.transfer.TransferTarget;

public class TransferTestUtil
{
    private final static String TEST_TARGET_NAME = "TestTarget";

    private TransferTestUtil()
    {
        // Static methods only
    }

    public static TransferTarget getTestTarget(TransferService transferService)
    {
        TransferTarget target;
        if (!transferService.targetExists(TEST_TARGET_NAME))
        {
            target = transferService.createAndSaveTransferTarget(TEST_TARGET_NAME, "Test Target", "Test Target", "http",
                    "localhost", 8090, "/alfresco/service/api/transfer", "admin", "admin".toCharArray());
        }
        else
        {
            target = transferService.getTransferTarget(TEST_TARGET_NAME);
        }
        return target;
    }
}
