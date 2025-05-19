/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2025 Alfresco Software Limited
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
package org.alfresco.repo.security.authentication.identityservice.webscript;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.alfresco.repo.admin.SysAdminParams;

/**
 * Service to handle Web Script authentication-related cookies.
 */
public class WebScriptHomeAuthenticationCookiesService
{
    private final SysAdminParams sysAdminParams;
    private final int cookieLifetime;

    public WebScriptHomeAuthenticationCookiesService(SysAdminParams sysAdminParams, int cookieLifetime)
    {
        this.sysAdminParams = sysAdminParams;
        this.cookieLifetime = cookieLifetime;
    }

    /**
     * Get the cookie with the given name.
     *
     * @param name
     *            the name of the cookie
     * @param request
     *            the request that might contain the cookie
     * @return the cookie value, or null if the cookie cannot be found
     */
    public String getCookie(String name, HttpServletRequest request)
    {
        String result = null;
        Cookie[] cookies = request.getCookies();

        if (cookies != null)
        {
            for (Cookie cookie : cookies)
            {
                if (cookie.getName().equals(name))
                {
                    result = cookie.getValue();
                    break;
                }
            }
        }

        return result;
    }

    /**
     * Add a cookie to the response.
     *
     * @param name
     *            the name of the cookie
     * @param value
     *            the value of the cookie
     * @param servletResponse
     *            the response to add the cookie to
     */
    public void addCookie(String name, String value, HttpServletResponse servletResponse)
    {
        internalAddCookie(name, value, cookieLifetime, servletResponse);
    }

    /**
     * Issue a cookie reset within the given response.
     *
     * @param name
     *            the cookie to reset
     * @param servletResponse
     *            the response to issue the cookie reset
     */
    public void resetCookie(String name, HttpServletResponse servletResponse)
    {
        internalAddCookie(name, "", 0, servletResponse);
    }

    private void internalAddCookie(String name, String value, int maxAge, HttpServletResponse servletResponse)
    {
        Cookie authCookie = new Cookie(name, value);
        authCookie.setPath("/"); // Set the cookie's valid path
        authCookie.setMaxAge(maxAge); // Set expiration time (in seconds)

        // Ensure the cookie is only transmitted over secure connections (HTTPS)
        authCookie.setSecure(sysAdminParams.getAlfrescoProtocol().equalsIgnoreCase("https"));

        // Prevent JavaScript access to this cookie for security reasons (XSS protection)
        authCookie.setHttpOnly(true);

        // Add the cookie to the response
        servletResponse.addCookie(authCookie);
    }
}
