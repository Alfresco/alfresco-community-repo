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

package org.alfresco.module.org_alfresco_module_rm.classification;

import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A Spring class used to provide the {@link ClassificationService} to non-Spring classes.
 * 
 * @author tpage
 */
public class ClassificationServiceProvider
{
    /** Logging utility for the class. */
    private static final Logger LOGGER = LoggerFactory.getLogger(ClassificationServiceProvider.class);
    /** The Spring application context. */
    private static final AtomicReference<ClassificationService> CLASSIFICATION_SERVICE_REF = new AtomicReference<>();

    /** Constructor that takes the classification service and makes it available statically. */
    public ClassificationServiceProvider(ClassificationService classificationService)
    {
        ClassificationService oldClassificationService = CLASSIFICATION_SERVICE_REF.getAndSet(classificationService);
        if (oldClassificationService != null)
        {
            LOGGER.debug("Unexpected instantiation of ClassificationServiceProvider has updated reference to classification service.");
        }
    }

    /**
     * Get the <code>ClassificationService</code> as defined in the Spring context.
     * 
     * @return The service bean.
     */
    public static ClassificationService getClassificationService()
    {
        return CLASSIFICATION_SERVICE_REF.get();
    }
}
