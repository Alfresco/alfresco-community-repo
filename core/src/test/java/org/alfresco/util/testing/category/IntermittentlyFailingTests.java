/*
 * Copyright (C) 2005-2020 Alfresco Software Limited.
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
 * Category marker for tests that intermittently fail, but not that frequently. Still included in the build. If the
 * instability increases or we have time to investigate, an issue should be raised to investigate and possibly
 * be switch to {@link FrequentlyFailingTests}. Use by developers to aid in working out if the build job should just be
 * retried.
 */
public interface IntermittentlyFailingTests
{
}
