/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
package org.alfresco.rest.framework;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;

/**
 * Standard API settings for the Alfresco Rest API
 * 
 * SCOPE - PUBLIC_API - Reserved for public API - PRIVATE_API - Reserved for private API
 *
 * VERSION - A positive integer starting at 1.
 * 
 * @author Gethin James
 */
public class Api implements Comparable<Api>
{
    private static Log logger = LogFactory.getLog(Api.class);

    public enum SCOPE
    {
        PRIVATE, PUBLIC
    };

    private final String name;
    private final SCOPE scope;
    private final int version;

    // Minor optimization to reuse this object as it will probably be used a lot
    public static final Api ALFRESCO_PUBLIC = new Api("alfresco", SCOPE.PUBLIC, 1);

    /**
     * Constructor used to create an api
     * 
     * @param name
     *            String
     * @param scope
     *            SCOPE
     * @param version
     *            int
     */
    private Api(String name, SCOPE scope, int version)
    {
        super();
        this.name = name;
        this.scope = scope;
        this.version = version;
    }

    public String getName()
    {
        return this.name;
    }

    public SCOPE getScope()
    {
        return this.scope;
    }

    public int getVersion()
    {
        return this.version;
    }

    /* @see java.lang.Object#hashCode() */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
        result = prime * result + ((this.scope == null) ? 0 : this.scope.hashCode());
        result = prime * result + this.version;
        return result;
    }

    /* @see java.lang.Object#equals(java.lang.Object) */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Api other = (Api) obj;
        if (this.name == null)
        {
            if (other.name != null)
                return false;
        }
        else if (!this.name.equals(other.name))
            return false;
        if (this.scope != other.scope)
            return false;
        if (this.version != other.version)
            return false;
        return true;
    }

    /* @see java.lang.Object#toString() */
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("Api [name=");
        builder.append(this.name);
        builder.append(", scope=");
        builder.append(this.scope);
        builder.append(", version=");
        builder.append(this.version);
        builder.append("]");
        return builder.toString();
    }

    /**
     * Creates an valid instance of the Api object
     * 
     * @param apiName
     *            a String in lowercase
     * @param apiScope
     *            SCOPE
     * @param apiVersion
     *            postive integer
     * @return Api
     */
    public static Api valueOf(String apiName, String apiScope, String apiVersion) throws InvalidArgumentException
    {
        SCOPE scope = null;
        int version = 1;

        try
        {
            if (!StringUtils.isAllLowerCase(apiName))
                throw new InvalidArgumentException("Api name must be lowercase");
            scope = SCOPE.valueOf(apiScope.toUpperCase());
            version = Integer.parseInt(apiVersion);
            if (version < 1)
                throw new InvalidArgumentException("Version must be a positive integer.");
        }
        catch (Exception error)
        {
            if (error instanceof InvalidArgumentException)
                throw (InvalidArgumentException) error; // Just throw it on.
            logger.debug("Invalid API definition: " + apiName + " " + apiScope + " " + apiVersion);
            throw new InvalidArgumentException("Invalid API definition:" + error.getMessage());
        }

        Api anApi = new Api(apiName, scope, version);
        return ALFRESCO_PUBLIC.equals(anApi) ? ALFRESCO_PUBLIC : anApi;

    }

    @Override
    public int compareTo(Api other)
    {
        int compare = this.scope.compareTo(other.scope);
        if (compare != 0)
            return compare;

        compare = this.name.compareTo(other.name);
        if (compare != 0)
            return compare;

        if (this.version < other.version)
            return -1;
        if (this.version > other.version)
            return 1;
        return 0; // All fields are equal
    }
}
