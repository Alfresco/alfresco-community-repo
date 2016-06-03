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
