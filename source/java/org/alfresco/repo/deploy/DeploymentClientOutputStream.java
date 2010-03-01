/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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

package org.alfresco.repo.deploy;

import java.io.IOException;
import java.io.OutputStream;

import org.alfresco.deployment.DeploymentReceiverTransport;

/**
 * OutputStream used by client side to talk to 
 * the deployment receiver.
 * @author britt
 */
public class DeploymentClientOutputStream extends OutputStream
{
    private DeploymentReceiverTransport fTransport;
    
    private String fTicket;
    
    private String fOutputToken;
    
    private boolean open = true;
    
    /**
     * Make one up.
     * @param transport
     * @param ticket
     * @param outputToken
     */
    public DeploymentClientOutputStream(DeploymentReceiverTransport transport,
                                        String ticket,
                                        String outputToken)
    {
        fTransport = transport;
        fTicket = ticket;
        fOutputToken = outputToken;
    }
    
    /* (non-Javadoc)
     * @see java.io.OutputStream#write(int)
     */
    @Override
    public void write(int b) throws IOException
    {
        byte[] buff = new byte[1];
        buff[0] = (byte)b;
        write(buff);
    }

    /* (non-Javadoc)
     * @see java.io.OutputStream#close()
     */
    @Override
    public void close() throws IOException
    {
    	if(open)
    	{
    		fTransport.finishSend(fTicket, fOutputToken);
    	}
        open = false;
    }

    /* (non-Javadoc)
     * @see java.io.OutputStream#flush()
     */
    @Override
    public void flush() throws IOException
    {
        // NO OP
    }

    /* (non-Javadoc)
     * @see java.io.OutputStream#write(byte[], int, int)
     */
    @Override
    public void write(byte[] b, int off, int len) throws IOException
    {
        fTransport.write(fTicket, fOutputToken, b, off, len);
    }

    /* (non-Javadoc)
     * @see java.io.OutputStream#write(byte[])
     */
    @Override
    public void write(byte[] b) throws IOException
    {
        write(b, 0, b.length);
    }
    
    /**
     * Get the deployment ticket.
     * @return
     */
    public String getTicket()
    {
        return fTicket;
    }
    
    /**
     * Get the output token.
     * @return
     */
    public String getOutputToken()
    {
        return fOutputToken;
    }
}
