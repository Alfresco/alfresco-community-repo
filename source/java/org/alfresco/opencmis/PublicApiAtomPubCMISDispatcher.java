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
package org.alfresco.opencmis;

import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Dispatches OpenCMIS requests to the OpenCMIS AtomPub servlet.
 * 
 * @author steveglover
 *
 */
public class PublicApiAtomPubCMISDispatcher extends AtomPubCMISDispatcher
{
	@Override
	protected CMISHttpServletRequest getHttpRequest(WebScriptRequest req)
	{
		String serviceName = getServiceName();
		CMISHttpServletRequest httpReqWrapper = new PublicApiCMISHttpServletRequest(req, serviceName, baseUrlGenerator, getBinding(), getCurrentDescriptor());
    	return httpReqWrapper;
	}
}