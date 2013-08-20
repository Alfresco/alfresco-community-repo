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
package org.alfresco.rest.api.tests.client;

import org.apache.commons.httpclient.HttpClient;

/**
 * Provides {@link HttpClient} instance to be used to perform HTTP-calls.
 *
 * @author Frederik Heremans
 */
public interface HttpClientProvider
{
    /**
     * @return the {@link HttpClient} instance to use for the next HTTP-call.
     */
    HttpClient getHttpClient();
    
    /**
     * @param path relative path of the URL from alfresco host.
     * @return full URL including hostname and port for the given path.
     */
    String getFullAlfrescoUrlForPath(String path);
}
