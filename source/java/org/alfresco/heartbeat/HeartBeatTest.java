/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
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
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.heartbeat;

import java.beans.XMLDecoder;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.alfresco.util.BaseSpringTest;
import org.alfresco.util.security.DecryptingInputStream;

/**
 * An integration test for the heartbeat service. Fakes an HTTP endpoint with a server socket in order to sure the
 * service is functioning correctly.
 * 
 * @author dward
 */
public class HeartBeatTest extends BaseSpringTest
{

    /**
     * Test heart beat.
     * 
     * @throws Exception
     *             the exception
     */
    @SuppressWarnings("unchecked")
    public void testHeartBeat() throws Exception
    {
        // Load the private key from the trial key store
        PrivateKey privateKey;
        {
            final KeyStore keyStore = KeyStore.getInstance("JKS");
            final InputStream in = getClass().getResourceAsStream(HeartBeat.PUBLIC_STORE);
            keyStore.load(in, HeartBeat.PUBLIC_STORE_PWD);
            in.close();
            privateKey = (PrivateKey) keyStore.getKey("test", HeartBeat.PUBLIC_STORE_PWD);
        }

        // Construct a heartbeat instance in test mode (beats every second using test public key)
        new HeartBeat(getApplicationContext(), true);
        ServerSocket serverSocket = new ServerSocket(9999);

        // Now attempt to parse 4 of the 'beats'
        for (int i = 0; i < 4; i++)
        {
            Socket clientSocket = serverSocket.accept();
            XMLDecoder decoder = null;
            InputStream in = null;
            OutputStream out = null;
            try
            {
                in = new GZIPInputStream(new DecryptingInputStream(new HttpChunkedInputStream(clientSocket
                        .getInputStream()), privateKey), 1024);
                out = clientSocket.getOutputStream();
                decoder = new XMLDecoder(in);
                Map<String, String> params = (Map<String, String>) decoder.readObject();
                out.write("HTTP/1.1 200 OK\r\n\r\n".getBytes("ASCII"));
                System.out.println(params);
            }
            finally
            {
                if (decoder != null)
                {
                    try
                    {
                        decoder.close();
                        in = null;
                    }
                    catch (final Exception e)
                    {
                    }
                }
                if (in != null)
                {
                    try
                    {
                        in.close();
                    }
                    catch (final Exception e)
                    {
                    }
                }
                if (out != null)
                {
                    try
                    {
                        out.close();
                    }
                    catch (final Exception e)
                    {
                    }
                }
                try
                {
                    clientSocket.close();
                }
                catch (Exception e)
                {
                }
            }

        }
        serverSocket.close();

    }

    /**
     * Wraps a raw byte stream in a chunked HTTP request to look like a regular input stream. Skips headers and parses
     * chunk sizes.
     */
    public static class HttpChunkedInputStream extends InputStream
    {
        /** The raw input stream. */
        private final InputStream socketIn;

        /** A buffer for parsing headers. */
        private StringBuilder headerBuff = new StringBuilder(100);

        /** The current chunk size. */
        private int chunkSize;

        /** The current position in the chunk. */
        private int chunkPosition;

        /** Have we got to the end of the last chunk? */
        private boolean isAtEnd;

        /**
         * Instantiates a new http chunked input stream.
         * 
         * @param socketIn
         *            raw input stream from an HTTP request
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        public HttpChunkedInputStream(InputStream socketIn) throws IOException
        {
            this.socketIn = socketIn;
            for (;;)
            {
                if (getNextHeader().length() == 0)
                {
                    break;
                }
            }
            setNextChunkSize();
        }

        /**
         * Gets the next header.
         * 
         * @return the next header
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        private String getNextHeader() throws IOException
        {
            int b;
            while ((b = socketIn.read()) != '\n')
            {
                if (b == -1)
                {
                    throw new EOFException();
                }
                headerBuff.append((char) b); // cast to char acceptable because this is ASCII
            }
            String header = headerBuff.toString().trim();
            headerBuff.setLength(0);
            return header;
        }

        /**
         * Sets the next chunk size by parsing a chunk header. May detect an end of file condition and set isAtEnd =
         * true.
         * 
         * @return the next chunk size
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        private int setNextChunkSize() throws IOException
        {
            String chunkHeader = getNextHeader();
            int sepIndex = chunkHeader.indexOf(';');
            if (sepIndex != -1)
            {
                chunkHeader = chunkHeader.substring(0, sepIndex).trim();
            }
            this.chunkSize = Integer.parseInt(chunkHeader, 16);
            this.chunkPosition = 0;
            if (this.chunkSize == 0)
            {
                this.isAtEnd = true;
            }
            return this.chunkSize;
        }

        /*
         * (non-Javadoc)
         * @see java.io.InputStream#close()
         */
        @Override
        public void close() throws IOException
        {
            // We intentionally avoid closing the socket input stream here, as that seems to close the entire socket,
            // and stops us from being able to write a response!
            // this.socketIn.close();
        }

        /*
         * (non-Javadoc)
         * @see java.io.InputStream#read()
         */
        @Override
        public int read() throws IOException
        {
            final byte[] buf = new byte[1];
            int bytesRead;
            while ((bytesRead = read(buf)) == 0)
            {
                ;
            }
            return bytesRead == -1 ? -1 : buf[0] & 0xFF;
        }

        /*
         * (non-Javadoc)
         * @see java.io.InputStream#read(byte[], int, int)
         */
        @Override
        public int read(byte[] b, int off, int len) throws IOException
        {
            if (len == 0)
            {
                return 0;
            }
            if (this.isAtEnd)
            {
                return -1;
            }
            int bytesToRead = len;
            while (bytesToRead > 0)
            {
                if (this.chunkPosition >= this.chunkSize)
                {
                    // Skip the \r\n after this chunk
                    String eol = getNextHeader();
                    if (eol.length() > 0)
                    {
                        throw new IOException("Bad chunk format");
                    }
                    // Read the new chunk header
                    setNextChunkSize();
                    if (this.isAtEnd)
                    {
                        // Skip past the trailers. We have to do this in case the same connection is recycled for the
                        // next request
                        for (;;)
                        {
                            if (getNextHeader().length() == 0)
                            {
                                break;
                            }
                        }
                        break;
                    }

                }
                int bytesRead = Math.min(bytesToRead, this.chunkSize - this.chunkPosition);
                bytesRead = this.socketIn.read(b, off, bytesRead);
                if (bytesRead == -1)
                {
                    break;
                }
                bytesToRead -= bytesRead;
                off += bytesRead;
                this.chunkPosition += bytesRead;
            }
            return bytesToRead == len ? -1 : len - bytesToRead;
        }
    }
}
