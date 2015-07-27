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
package org.alfresco.module.org_alfresco_module_rm.script.classification;

import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationAspectProperties;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Implementation for Java backed webscript to edit a classified content.
 *
 * @author Tuna Aksoy
 * @since 3.0.a
 */
public class ClassifyContentPut extends ClassifyContentBase
{
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.script.classification.ClassifyContentBase#doClassifyAction(java.lang.String, java.lang.String, java.lang.String, java.util.Set, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void doClassifyAction(ClassificationAspectProperties classificationAspectProperties, NodeRef document)
    {
        getContentClassificationService().editClassifiedContent(classificationAspectProperties, document);
    }
}