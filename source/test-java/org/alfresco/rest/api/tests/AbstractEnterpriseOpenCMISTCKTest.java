/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.rest.api.tests;

import org.alfresco.opencmis.OpenCMISClientContext;

/**
 * Base class for Chemistry OpenCMIS TCK tests.
 * 
 * @author steveglover
 *
 */
public abstract class AbstractEnterpriseOpenCMISTCKTest extends EnterpriseTestApi
{
	protected static OpenCMISClientContext clientContext;
	
   @Override
    protected TestFixture getTestFixture() throws Exception
    {
        return TCKEnterpriseTestFixture.getInstance();
    }
}
