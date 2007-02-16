/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.security.authentication;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;

import net.sf.acegisecurity.providers.encoding.BaseDigestPasswordEncoder;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

import cryptix.jce.provider.CryptixCrypto;

/**
 * <p>
 * MD4 implementation of PasswordEncoder.
 * </p>
 * 
 * <p>
 * If a <code>null</code> password is presented, it will be treated as an
 * empty <code>String</code> ("") password.
 * </p>
 * 
 * <P>
 * As MD4 is a one-way hash, the salt can contain any characters.
 * </p>
 */
public class MD4PasswordEncoderImpl extends BaseDigestPasswordEncoder implements MD4PasswordEncoder
{

    static
    {
        try
        {
            MessageDigest.getInstance("MD4");
        }
        catch (NoSuchAlgorithmException e)
        {
            Security.addProvider(new CryptixCrypto());
        }
    }

    
    public MD4PasswordEncoderImpl()
    {
        super();
        // TODO Auto-generated constructor stub
    }

    // ~ Methods
    // ================================================================

    public boolean isPasswordValid(String encPass, String rawPass, Object salt)
    {
        String pass1 = "" + encPass;
        String pass2 = encodeInternal(mergePasswordAndSalt(rawPass, salt, false));

        return pass1.equals(pass2);
    }

    public String encodePassword(String rawPass, Object salt)
    {
        return encodeInternal(mergePasswordAndSalt(rawPass, salt, false));
    }

    private String encodeInternal(String input)
    {
        if (!getEncodeHashAsBase64())
        {
            return new String(Hex.encodeHex(md4(input)));
        }

        byte[] encoded = Base64.encodeBase64(md4(input));

        try
        {
            return new String(encoded, "UTF8");
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException("UTF8 not supported!", e);
        }
    }

    private byte[] md4(String input)
    {
        try
        {
            MessageDigest digester = MessageDigest.getInstance("MD4");
            return digester.digest(input.getBytes("UnicodeLittleUnmarked"));
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException(e.getMessage(), e);
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public byte[] decodeHash(String encodedHash)
    {
        if (!getEncodeHashAsBase64())
        {
            try
            {
                return Hex.decodeHex(encodedHash.toCharArray());
            }
            catch (DecoderException e)
            {
               throw new RuntimeException("Unable to decode password hash");
            }
        }
        else
        {
            return Base64.decodeBase64(encodedHash.getBytes());
        }
    }

}
