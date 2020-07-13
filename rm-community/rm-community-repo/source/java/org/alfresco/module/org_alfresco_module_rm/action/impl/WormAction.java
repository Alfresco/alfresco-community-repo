/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2020 Alfresco Software Limited
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

package org.alfresco.module.org_alfresco_module_rm.action.impl;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.action.RMActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collections;

/**
 * Worm lock Action
 */
public class WormAction extends RMActionExecuterAbstractBase {

    private static Log logger = LogFactory.getLog(WormAction.class);

    /**
     * Action name
     */
    public static final String NAME = "worm";

    /**
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef) {

        // TODO also check if the node is already worm locked
        if (getNodeService().exists(actionedUponNodeRef) && getRecordService().isRecord(actionedUponNodeRef)) {

            getNodeService().addAspect(actionedUponNodeRef, ContentModel.ASPECT_STORE_SELECTOR, Collections.emptyMap());
            // TODO: set PROP_STORE_NAME to the desired worm enabled bucket, hardcoded for now
            //getNodeService().setProperty(actionedUponNodeRef, ContentModel.PROP_STORE_NAME, "cm:storeName");
        } else {
            // we cannot lock a document which is not a record
            if (logger.isErrorEnabled()) {
                logger.error("Cannot worm lock the document, because '" + actionedUponNodeRef.toString() + "' is not a record.");
            }
        }
    }
}
