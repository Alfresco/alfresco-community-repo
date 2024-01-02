/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2024 Alfresco Software Limited
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
package org.alfresco.repo.security.authentication.identityservice.admin;

import static java.util.Arrays.asList;
import static java.util.Collections.enumeration;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.alfresco.util.PropertyCheck;

public class AdminConsoleHttpServletRequestWrapper extends HttpServletRequestWrapper
{
    private final Map<String, String> additionalHeaders;
    private final HttpServletRequest wrappedRequest;

    /**
     * Constructs a request object wrapping the given request.
     *
     * @param request the request to wrap
     * @throws IllegalArgumentException if the request is null
     */
    public AdminConsoleHttpServletRequestWrapper(Map<String, String> additionalHeaders, HttpServletRequest request)
    {
        super(request);
        PropertyCheck.mandatory(this, "additionalHeaders", additionalHeaders);
        this.additionalHeaders = additionalHeaders;
        this.wrappedRequest = request;
    }

    @Override
    public Enumeration<String> getHeaderNames()
    {
        List<String> result = new ArrayList<>();
        Enumeration<String> originalHeaders = wrappedRequest.getHeaderNames();
        if (originalHeaders != null)
        {
            while (originalHeaders.hasMoreElements())
            {
                String header = originalHeaders.nextElement();
                if (!additionalHeaders.containsKey(header))
                {
                    result.add(header);
                }
            }
        }

        result.addAll(additionalHeaders.keySet());
        return enumeration(result);
    }

    @Override
    public String getHeader(String name)
    {
        return additionalHeaders.getOrDefault(name, super.getHeader(name));
    }

    @Override
    public Enumeration<String> getHeaders(String name)
    {
        return enumeration(asList(additionalHeaders.getOrDefault(name, super.getHeader(name))));
    }
}
