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

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * A configurable password encoding that delegates the encoding to a Map of
 * configured encoders.
 *
 * @author Gethin James
 */
public class CompositePasswordEncoder
{
    private static Log logger = LogFactory.getLog(CompositePasswordEncoder.class);
    private Map<String,Object> encoders;
    private String preferredEncoding;

    public static final String MD4_KEY = "md4";
    public static final List<String> SHA256 = Arrays.asList("sha256");
    public static final List<String> MD4 = Arrays.asList(MD4_KEY);

    public String getPreferredEncoding()
    {
        return preferredEncoding;
    }

    public void setPreferredEncoding(String preferredEncoding)
    {
        this.preferredEncoding = preferredEncoding;
    }

    public void setEncoders(Map<String, Object> encoders)
    {
        this.encoders = encoders;
    }

    /**
     * Is the preferred encoding the last encoding to be used.
     * @param hashIndicator representing the encoding
     * @return true if is correct
     */
    public boolean lastEncodingIsPreferred(List<String> hashIndicator)
    {
        if (hashIndicator!= null && hashIndicator.size() > 0 && preferredEncoding.equals(hashIndicator.get(hashIndicator.size()-1)))
        {
            return true;
        }
        return false;
    }

    /**
     * Determines if its safe to encode the encoding chain.  This applies particularly to double-hashing.
     * BCRYPT uses its own internal salt so its NOT safe to use it more than once in an encoding chain
     * (because the result will be different each time.)  BCRYPT CAN BE USED successfully as the last element in
     * an encoding chain.
     *
     * Anything that implements springframework PasswordEncoder is considered "unsafe"
     * (because the method takes no salt param).
     *
     * @param encodingChain mandatory encoding chain
     * @return true if it is okay to encode this chain.
     */
    public boolean isSafeToEncodeChain(List<String> encodingChain)
    {
        if (encodingChain!= null && encodingChain.size() > 0 )
        {
            List<String> unsafeEncoders = new ArrayList<>();
            for (String encoderKey : encodingChain)
            {
                Object encoder = encoders.get(encoderKey);
                if (encoder == null) throw new AlfrescoRuntimeException("Invalid encoder specified: "+encoderKey);
                if (encoder instanceof org.springframework.security.crypto.password.PasswordEncoder)
                {
                    //BCRYPT uses its own internal salt so its NOT safe to use it more than once in an encoding chain.
                    //the Spring PasswordEncoder class doesn't require a salt and BCRYPTEncoder implements this, so
                    //we will count the instances of Spring PasswordEncoder
                    unsafeEncoders.add(encoderKey);
                }
            }

            if (unsafeEncoders.isEmpty()) return true;
            if (unsafeEncoders.size() == 1 && unsafeEncoders.get(0).equals(encodingChain.get(encodingChain.size()-1)))
            {
                //The unsafe encoder is used at the end so that's ok.
                return true;
            }
            //, because there is already an unupgradable encoder at the end of the chain.
            if (logger.isDebugEnabled()) {
                logger.debug("Non-upgradable encoders in the encoding chain: "+Arrays.toString(unsafeEncoders.toArray())
                        +". Only 1 non-upgradable encoder is allowed at the end of the chain: "+Arrays.toString(encodingChain.toArray()));
            }

        }
        return false;
    }

    /**
     * Basic init method for checking mandatory properties
     */
    public void init()
    {
        PropertyCheck.mandatory(this, "encoders", encoders);
        PropertyCheck.mandatory(this, "preferredEncoding", preferredEncoding);
        if (logger.isDebugEnabled()) {
            logger.debug("Preferred password encoding set to "+preferredEncoding);
        }
        if (!encoders.containsKey(preferredEncoding)) throw new AlfrescoRuntimeException("Invalid preferredEncoding specified: "
                +preferredEncoding+ ". Permissible encoders are "+encoders.keySet());
    }

    /**
     * Encode a password
     * @param rawPassword mandatory password
     * @param salt optional salt
     * @param encodingChain mandatory encoding chain
     * @return the encoded password
     */
    public String encodePassword(String rawPassword, Object salt, List<String> encodingChain) {

        ParameterCheck.mandatoryString("rawPassword", rawPassword);
        ParameterCheck.mandatoryCollection("encodingChain", encodingChain);
        String encoded = new String(rawPassword);
        for (String encoderKey : encodingChain)
        {
            encoded = encode(encoderKey, encoded, salt);

        }
        if (encoded == rawPassword) throw new AlfrescoRuntimeException("No password encoding specified. "+encodingChain);
        return encoded;
    }

    /**
     * Encodes a password in the preferred encoding.
     * @param rawPassword  mandatory password
     * @param salt optional salt
     * @return Encoded password
     */
    public String encodePreferred(String rawPassword, Object salt)
    {
        return encode(getPreferredEncoding(), rawPassword, salt);
    }

    /**
     *  Encode a password using the specified encoderKey
     * @param encoderKey the encoder to use
     * @param rawPassword  mandatory password
     * @param salt optional salt
     * @return the encoded password
     */
    protected String encode(String encoderKey, String rawPassword, Object salt)
    {
       ParameterCheck.mandatoryString("rawPassword", rawPassword);
       ParameterCheck.mandatoryString("encoderKey", encoderKey);
       Object encoder = encoders.get(encoderKey);
       if (encoder == null) throw new AlfrescoRuntimeException("Invalid encoder specified: "+encoderKey);
       if (encoder instanceof net.sf.acegisecurity.providers.encoding.PasswordEncoder)
       {
           net.sf.acegisecurity.providers.encoding.PasswordEncoder pEncoder = (net.sf.acegisecurity.providers.encoding.PasswordEncoder) encoder;
           if (MD4_KEY.equals(encoderKey))
           {
               //In the past MD4 password encoding didn't use a SALT
               salt = null;
           }
           if (logger.isDebugEnabled()) {
               logger.debug("Encoding using acegis PasswordEncoder: "+encoderKey);
           }
           return pEncoder.encodePassword(rawPassword, salt);
       }
       if (encoder instanceof org.springframework.security.crypto.password.PasswordEncoder)
       {
           org.springframework.security.crypto.password.PasswordEncoder passEncoder = (org.springframework.security.crypto.password.PasswordEncoder) encoder;
           if (logger.isDebugEnabled()) {
               logger.debug("Encoding using spring PasswordEncoder: "+encoderKey);
           }
           return passEncoder.encode(rawPassword);
       }

       throw new AlfrescoRuntimeException("Unsupported encoder specified: "+encoderKey);
    }

    /**
     * Does the password match?
     * @param rawPassword  mandatory password
     * @param encodedPassword mandatory hashed version
     * @param salt optional salt
     * @param encodingChain mandatory encoding chain
     * @return true if they match
     */
    public boolean matchesPassword(String rawPassword, String encodedPassword, Object salt, List<String> encodingChain)
    {
        ParameterCheck.mandatoryString("rawPassword", rawPassword);
        ParameterCheck.mandatoryString("encodedPassword", encodedPassword);
        ParameterCheck.mandatoryCollection("encodingChain", encodingChain);
        if (encodingChain.size() > 1)
        {
            String lastEncoder = encodingChain.get(encodingChain.size() - 1);
            String encoded = encodePassword(rawPassword,salt, encodingChain.subList(0,encodingChain.size()-1));
            return matches(lastEncoder,encoded,encodedPassword,salt);
        }

        if (encodingChain.size() == 1)
        {
            return matches(encodingChain.get(0), rawPassword, encodedPassword, salt);
        }
        return false;
    }

    /**
     * Does the password match?
     * @param encoderKey the encoder to use
     * @param rawPassword  mandatory password
     * @param encodedPassword mandatory hashed version
     * @param salt optional salt
     * @return true if they match
     */
    protected boolean matches(String encoderKey, String rawPassword, String encodedPassword, Object salt)
    {
        ParameterCheck.mandatoryString("rawPassword", rawPassword);
        ParameterCheck.mandatoryString("encodedPassword", encodedPassword);
        ParameterCheck.mandatoryString("encoderKey", encoderKey);
        Object encoder = encoders.get(encoderKey);
        if (encoder == null) throw new AlfrescoRuntimeException("Invalid matches encoder specified: "+encoderKey);
        if (encoder instanceof net.sf.acegisecurity.providers.encoding.PasswordEncoder)
        {
            net.sf.acegisecurity.providers.encoding.PasswordEncoder pEncoder = (net.sf.acegisecurity.providers.encoding.PasswordEncoder) encoder;
            if (MD4_KEY.equals(encoderKey))
            {
                //In the past MD4 password encoding didn't use a SALT
                salt = null;
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Matching using acegis PasswordEncoder: "+encoderKey);
            }
            return pEncoder.isPasswordValid(encodedPassword, rawPassword, salt);
        }
        if (encoder instanceof org.springframework.security.crypto.password.PasswordEncoder)
        {
            org.springframework.security.crypto.password.PasswordEncoder passEncoder = (org.springframework.security.crypto.password.PasswordEncoder) encoder;
            if (logger.isDebugEnabled()) {
                logger.debug("Matching using spring PasswordEncoder: "+encoderKey);
            }
            return passEncoder.matches(rawPassword, encodedPassword);
        }
        throw new AlfrescoRuntimeException("Unsupported encoder for matching: "+encoderKey);
    }
}
