/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.repo.security.authentication;

/**
 * <p>
 * SHA implementation of PasswordEncoder.
 * </p>
 * <p>
 * If a <code>null</code> password is presented, it will be treated as an empty
 * <code>String</code> ("") password.
 * </p>
 * <p>
 * As SHA is a one-way hash, the salt can contain any characters. The default
 * strength for the SHA encoding is SHA-1. If you wish to use higher strengths
 * use the argumented constructor. {@link #ShaPasswordEncoder(int strength)}
 * </p>
 * <p>
 * The applicationContext example...
 * 
 * <pre>
 * &lt;bean id="passwordEncoder" class="org.springframework.security.authentication.encoding.ShaPasswordEncoder"&gt;
 *     &lt;constructor-arg value="256"/>
 * &lt;/bean&gt;
 * </pre>
 */
public class ShaPasswordEncoderImpl extends MessageDigestPasswordEncoder
{

    /**
     * Initializes the ShaPasswordEncoder for SHA-1 strength
     */
    public ShaPasswordEncoderImpl()
    {
        this(1);
    }

    /**
     * Initialize the ShaPasswordEncoder with a given SHA stength as supported
     * by the JVM EX:
     * <code>ShaPasswordEncoder encoder = new ShaPasswordEncoder(256);</code>
     * initializes with SHA-256
     * 
     * @param strength
     *            EX: 1, 256, 384, 512
     */
    public ShaPasswordEncoderImpl(int strength)
    {
        super("SHA-" + strength);
    }
}