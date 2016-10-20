/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 * #L%
 */
package org.alfresco.rest.fileplancomponents;

import static org.springframework.http.HttpStatus.OK;
import static org.testng.Assert.assertEquals;

import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.ig.IgRestTest;
import org.alfresco.rest.model.RestFilePlanComponentModel;
import org.alfresco.rest.requests.RestFilePlanComponentApi;
import org.alfresco.utility.data.DataUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

/**
 * FIXME: Document me :)
 *
 * @author Tuna Aksoy
 * @since 1.0
 */
public class FilePlanComponentsTest extends IgRestTest
{
    @Autowired
    private RestFilePlanComponentApi filePlanComponentApi;

    @Autowired
    private DataUser dataUser;

    @Test
    public void testfilePlanComponentsGet() throws Exception
    {
        RestWrapper restWrapper = filePlanComponentApi.usingRestWrapper();
        restWrapper.authenticateUser(dataUser.getAdminUser());
        RestFilePlanComponentModel filePlanComponent = filePlanComponentApi.getFilePlanComponent("-filePlan-");
        restWrapper.assertStatusCodeIs(OK);
        assertEquals(filePlanComponent.getNodeType(), "rma:filePlan");
    }
}
