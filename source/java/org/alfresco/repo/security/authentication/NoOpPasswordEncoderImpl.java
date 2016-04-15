/*
 * #%L
 * Alfresco Repository
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
package org.alfresco.repo.security.authentication;

import net.sf.acegisecurity.providers.encoding.BaseDigestPasswordEncoder;


/**
 * <p>
 * NoOp implementation of PasswordEncoder.
 * </p>
 * The No Op Password Encoder produces a blank hash.  And will not match any value of hash.  
 * Used to replace an obsolete encoder like the MD4.
 * </p>
 */
public class NoOpPasswordEncoderImpl extends BaseDigestPasswordEncoder implements MD4PasswordEncoder
{
    
    public NoOpPasswordEncoderImpl()
    {
        super();
        // TODO Auto-generated constructor stub
    }

    // ~ Methods
    // ================================================================

    public boolean isPasswordValid(String encPass, String rawPass, Object salt)
    {
       return false;
    }

    public String encodePassword(String rawPass, Object salt)
    {
        return "";
    }

    public byte[] decodeHash(String encodedHash)
    {
        return new byte[0];
    }

}
