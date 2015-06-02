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
 * A Spring class used to provide the {@link ClassificationSchemeService} to non-Spring classes.
 *
 * @author tpage
 */
public class ClassificationSchemeServiceProvider
{
    /** Logging utility for the class. */
    private static final Logger LOGGER = LoggerFactory.getLogger(ClassificationSchemeServiceProvider.class);
    /** The Spring application context. */
    private static final AtomicReference<ClassificationSchemeService> CLASSIFICATION_SCHEME_SERVICE_REF = new AtomicReference<>();

    /** Constructor that takes the classification scheme service and makes it available statically. */
    public ClassificationSchemeServiceProvider(ClassificationSchemeService classificationSchemeService)
    {
        ClassificationSchemeService oldClassificationSchemeService = CLASSIFICATION_SCHEME_SERVICE_REF.getAndSet(classificationSchemeService);
        if (oldClassificationSchemeService != null)
        {
            LOGGER.debug("Unexpected instantiation of ClassificationSchemeServiceProvider has updated reference to classification scheme service.");
        }
    }

    /**
     * Get the <code>ClassificationSchemeService</code> as defined in the Spring context.
     *
     * @return The service bean.
     */
    public static ClassificationSchemeService getClassificationSchemeService()
    {
        return CLASSIFICATION_SCHEME_SERVICE_REF.get();
    }
}
