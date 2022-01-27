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

package org.alfresco.module.org_alfresco_module_rm.jscript.app.evaluator;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.jscript.app.BaseEvaluator;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Split EMail action evaluator
 *
 * @author Roy Wetherall
 */
public class SplitEmailActionEvaluator extends BaseEvaluator
{
    @Override
    protected boolean evaluateImpl(NodeRef nodeRef)
    {
        boolean result = false;
        if (!recordService.isDeclared(nodeRef))
        {
            ContentData contentData = (ContentData)nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT);
            if (contentData != null)
            {
                String mimetype = contentData.getMimetype();
                if (mimetype != null &&
                    (MimetypeMap.MIMETYPE_RFC822.equals(mimetype) ||
                     MimetypeMap.MIMETYPE_OUTLOOK_MSG.equals(mimetype)))
                {
                    result = true;
                }
            }
        }
        return result;
    }
}
