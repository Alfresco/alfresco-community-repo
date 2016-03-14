/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * 
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

package org.alfresco.module.org_alfresco_module_rm.script.hold;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.test.util.BaseWebScriptUnitTest;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Base hold web script unit test.
 *
 * @author Roy Wetherall
 * @since 2.2
 */
public abstract class BaseHoldWebScriptUnitTest extends BaseWebScriptUnitTest
{
    /** test holds */
    protected NodeRef hold1NodeRef;
    protected NodeRef hold2NodeRef;
    protected List<NodeRef> holds;
    protected List<NodeRef> records;
    protected List<NodeRef> recordFolders;
    protected List<NodeRef> filePlanComponents;

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseUnitTest#before()
     */
    @Override
    public void before() throws Exception
    {
        super.before();

        // generate test holds
        hold1NodeRef = generateHoldNodeRef("hold1");
        hold2NodeRef = generateHoldNodeRef("hold2");

        // list of holds
        holds = new ArrayList<NodeRef>(2);
        Collections.addAll(holds, hold1NodeRef, hold2NodeRef);

        // list of records
        records = Collections.singletonList(record);

        // list of record folders
        recordFolders = Collections.singletonList(recordFolder);

        // list of file plan components
        filePlanComponents = Collections.singletonList(filePlanComponent);
    }
}
