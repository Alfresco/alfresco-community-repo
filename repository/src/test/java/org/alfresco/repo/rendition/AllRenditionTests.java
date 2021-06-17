/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
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
package org.alfresco.repo.rendition;

import org.alfresco.repo.thumbnail.ThumbnailServiceImplParameterTest;
import org.alfresco.repo.thumbnail.ThumbnailServiceImplTest;
import org.alfresco.repo.thumbnail.conditions.NodeEligibleForRethumbnailingEvaluatorTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This class is a holder for the various test classes associated with the Rendition Service.
 * It is not (at the time of writing) intended to be incorporated into the automatic build
 * which will find the various test classes and run them individually.
 * 
 * @author Neil McErlean
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        RenditionServiceImplTest.class,
        ThumbnailServiceImplParameterTest.class,
        ThumbnailServiceImplTest.class,
        NodeEligibleForRethumbnailingEvaluatorTest.class,
        StandardRenditionLocationResolverTest.class,
        RenditionServiceIntegrationTest.class,
        RenditionServicePermissionsTest.class,
        RenditionNodeManagerTest.class,
        MultiUserRenditionTest.class
})
public class AllRenditionTests
{
    // Intentionally empty
}
