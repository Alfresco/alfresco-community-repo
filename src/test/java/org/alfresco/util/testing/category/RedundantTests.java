/*
 * Copyright (C) 2005-2017 Alfresco Software Limited.
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
package org.alfresco.util.testing.category;

/**
 * Category marker for tests that are duplicate (test the same functionality as other tests), or tests that test
 * deprecated functionality.<p>
 *
 * Use the {@link NeverRunsTests} interface if the reason a test is not run is unknown. Also see {@link DebugTests},
 * {@link PerformanceTests} if the reason is known.
 */
public interface RedundantTests extends NonBuildTests
{
}
