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

import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.ig.IgTest;
import org.alfresco.rest.model.FileplanComponentTypes;
import org.alfresco.rest.model.RestFilePlanComponentModel;
import org.alfresco.rest.requests.RestFilePlanComponentApi;
import org.alfresco.utility.data.DataUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;
import static org.springframework.http.HttpStatus.CREATED;

/**
 *
 * @author Kristijan Conkas
 * @since
 */
public class RecordCategoryComponentsTest extends IgTest
{
    @Autowired
    private RestFilePlanComponentApi filePlanComponentApi;

    @Autowired
    private DataUser dataUser;
    
    @Test
    public void createCategory() throws Exception
    {
        RestWrapper restWrapper = filePlanComponentApi.usingRestWrapper();
        restWrapper.authenticateUser(dataUser.getAdminUser());
        RestFilePlanComponentModel filePlanComponent = 
            filePlanComponentApi.createFilePlanComponent("-filePlan-", "category 123", FileplanComponentTypes.CATEGORY, null);
        
        restWrapper.assertStatusCodeIs(CREATED);
        assertTrue(filePlanComponent.isIsCategory());
        // TODO: add more verification here
    }
}
