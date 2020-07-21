/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.encryption;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.crypto.Mac;

import org.alfresco.error.AlfrescoRuntimeException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Provides support for generating and checking MACs (Message Authentication Codes) using Alfresco's
 * secret keys.
 * 
 * @since 4.0
 *
 */
public class MACUtils
{
    private static Log logger = LogFactory.getLog(Encryptor.class);
    private static byte SEPARATOR = 0;

    private final ThreadLocal<Mac> threadMac;

    private KeyProvider keyProvider;
    private String macAlgorithm;

    /**
     * Default constructor for IOC
     */
    public MACUtils()
    {
        threadMac = new ThreadLocal<Mac>();
    }
    
    public void setKeyProvider(KeyProvider keyProvider)
    {
        this.keyProvider = keyProvider;
    }

    public void setMacAlgorithm(String macAlgorithm)
    {
        this.macAlgorithm = macAlgorithm;
    }
    
    protected Mac getMac(String keyAlias) throws Exception
    {
        Mac mac = threadMac.get();
        if(mac == null)
        {
            mac = Mac.getInstance(macAlgorithm);

            threadMac.set(mac);
        }
        Key key = keyProvider.getKey(keyAlias);
        if(key == null)
        {
            throw new AlfrescoRuntimeException("Unexpected null key for key alias " + keyAlias);
        }
        mac.init(key);
        return mac;
    }
    
    protected byte[] longToByteArray(long l) throws IOException
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();  
        DataOutputStream dos = new DataOutputStream(bos);  
        dos.writeLong(l);  
        dos.flush();  
        return bos.toByteArray();         
    }

    public byte[] generateMAC(String keyAlias, MACInput macInput)
    {
        try
        {
            InputStream fullMessage = macInput.getMACInput();

            if(logger.isDebugEnabled())
            {
                logger.debug("Generating MAC for " + macInput + "...");
            }

            Mac mac = getMac(keyAlias);

            byte[] buf = new byte[1024];
            int len;
            while((len = fullMessage.read(buf, 0, 1024)) != -1)
            {
                mac.update(buf, 0, len);
            }
            byte[] newMAC = mac.doFinal();

            if(logger.isDebugEnabled())
            {
                logger.debug("...done. MAC is " + Arrays.toString(newMAC));
            }

            return newMAC;
        }
        catch (Exception e)
        {
            throw new AlfrescoRuntimeException("Failed to generate MAC", e);
        }
    }

    /**
     * Compares the expectedMAC against the MAC generated from 
     * Assumes message has been decrypted
     * @param keyAlias String
     * @param expectedMAC byte[]
     * @param macInput MACInput
     * @return boolean
     */
    public boolean validateMAC(String keyAlias, byte[] expectedMAC, MACInput macInput)
    {
        try
        {
            byte[] mac = generateMAC(keyAlias, macInput);

            if(logger.isDebugEnabled())
            {
                logger.debug("Validating expected MAC " + Arrays.toString(expectedMAC) + " against mac " + Arrays.toString(mac) + " for MAC input " + macInput + "...");
            }

            boolean areEqual = Arrays.equals(expectedMAC, mac);
            
            if(logger.isDebugEnabled())
            {
                logger.debug(areEqual ? "...MAC validation succeeded." : "...MAC validation failed.");
            }

            return areEqual;
        }
        catch (Exception e)
        {
            throw new AlfrescoRuntimeException("Failed to validate MAC", e);
        }        
    }

    /**
     * Represents the information to be fed into the MAC generator
     * 
     * @since 4.0
     *
     */
    public static class MACInput
    {
        // The message, may be null
        private InputStream message;
        private long timestamp;
        private String ipAddress;
        
        public MACInput(byte[] message, long timestamp, String ipAddress)
        {
            this.message = (message != null ? new ByteArrayInputStream(message) : null);
            this.timestamp = timestamp;
            this.ipAddress = ipAddress;
        }

        public InputStream getMessage()
        {
            return message;
        }

        public long getTimestamp()
        {
            return timestamp;
        }

        public String getIpAddress()
        {
            return ipAddress;
        }

        public InputStream getMACInput() throws IOException
        {
            List<InputStream> inputStreams = new ArrayList<InputStream>();

            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(bytes);
            out.writeUTF(ipAddress);
            out.writeByte(SEPARATOR);
            out.writeLong(timestamp);
            inputStreams.add(new ByteArrayInputStream(bytes.toByteArray()));

            if(message != null)
            {
                inputStreams.add(message);
            }

            return new MessageInputStream(inputStreams);
        }
        
        public String toString()
        {
            StringBuilder sb = new StringBuilder("MACInput[");
            sb.append("timestamp: ").append(getTimestamp());
            sb.append("ipAddress: ").append(getIpAddress());
            return sb.toString();
        }
    }
    
    private static class MessageInputStream extends InputStream
    {
        private List<InputStream> input;
        private InputStream activeInputStream;
        private int currentStream = 0;

        public MessageInputStream(List<InputStream> input)
        {
            this.input = input;
            this.currentStream = 0;
            this.activeInputStream = input.get(currentStream);
        }

        @Override
        public void close() throws IOException
        {
            IOException firstIOException = null;

            for(InputStream in : input)
            {
                try
                {
                    in.close();
                }
                catch(IOException e)
                {
                    if(firstIOException == null)
                    {
                        firstIOException = e;
                    }
                }
            }

            if(firstIOException != null)
            {
                throw firstIOException;
            }

        }

        @Override
        public int read() throws IOException
        {
            int i = activeInputStream.read();
            if(i == -1)
            {
                currentStream++;
                if(currentStream >= input.size())
                {
                    return -1;
                }
                else
                {
                    activeInputStream = input.get(currentStream);
                    i = activeInputStream.read();
                }
            }

            return i;
        }
    }
}
