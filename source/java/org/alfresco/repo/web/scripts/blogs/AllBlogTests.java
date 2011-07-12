/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.repo.web.scripts.blogs;

import org.alfresco.repo.blog.BlogServiceImplTest;
import org.alfresco.service.cmr.blog.BlogService;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This class is a holder for the various test classes associated with the {@link BlogService}.
 * It is not (at the time of writing) intended to be incorporated into the automatic build
 * which will find the various test classes and run them individually.
 * 
 * @author Neil Mc Erlean
 * @since 4.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    BlogServiceImplTest.class,
    BlogServiceTest.class
})
public class AllBlogTests
{
    // Intentionally empty
}
