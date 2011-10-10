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
package org.alfresco.repo.imap;

import java.io.BufferedInputStream;
import java.io.FilterInputStream;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeMultipart;

import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.mail.util.BASE64DecoderStream;

public class RemoteLoadTester extends TestCase
{

    private Log logger = LogFactory.getLog(RemoteLoadTester.class);

    private static final String USER_NAME = "test_imap_user";
    private static final String USER_PASSWORD = "test_imap_user";
    private static final String TEST_FOLDER_NAME = "test_imap1000";
    
    private static final String ADMIN_USER_NAME = "admin";
    private static String REMOTE_HOST = "127.0.0.1";
    
    public static void main(String[] args)
    {
        if (args.length > 0)
            REMOTE_HOST = args[0];
        new RemoteLoadTester().testListSequence();
    }

    @Override
    public void setUp() throws Exception
    {
    }

    public void tearDown() throws Exception
    {
    }

    public void testListSequence()
    {
        System.out.println(String.format("Connecting to remote server '%s'", REMOTE_HOST));
        Properties props = System.getProperties();
        props.setProperty("mail.imap.partialfetch", "false");
        Session session = Session.getDefaultInstance(props, null);
        
        Store store = null;
        long startTime = 0;
        long endTime = 0;
        try
        {
            store = session.getStore("imap");
            store.connect(REMOTE_HOST, ADMIN_USER_NAME, ADMIN_USER_NAME);
            Folder[] folders = null;
            
            startTime = System.currentTimeMillis();
            folders = store.getDefaultFolder().list("");
            endTime = System.currentTimeMillis();
            System.out.println(String.format("LIST '', folders.length = %d, execTime = %d sec", folders.length, (endTime - startTime)/1000));
            
            startTime = System.currentTimeMillis();
            folders = store.getDefaultFolder().list("*");
            endTime = System.currentTimeMillis();
            System.out.println(String.format("LIST *, folders.length = %d, execTime = %d sec", folders.length, (endTime - startTime)/1000));
            
            startTime = System.currentTimeMillis();
            folders = store.getDefaultFolder().listSubscribed("*");
            endTime = System.currentTimeMillis();
            System.out.println(String.format("LSUB *, folders.length = %d, execTime = %d sec", folders.length, (endTime - startTime)/1000));
            
            startTime = System.currentTimeMillis();
            for (Folder folder : folders)
            {
                folder.getMessageCount();
                //Folder f = store.getFolder(folder.getFullName());
            }
            endTime = System.currentTimeMillis();
            System.out.println(String.format("Folders Loop, folders.length = %d, execTime = %d sec", folders.length, (endTime - startTime)/1000));
            
        }
        catch (NoSuchProviderException e)
        {
            e.printStackTrace();
        }
        catch (MessagingException e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                store.close();
            }
            catch (MessagingException e)
            {
                System.err.println(e.getMessage());
            }
        }
    }

    public void testMailbox()
    {
        logger.info("Getting folder...");
        long t = System.currentTimeMillis();
        
        // Create empty properties
        Properties props = new Properties();
        props.setProperty("mail.imap.partialfetch", "false");

        // Get session
        Session session = Session.getDefaultInstance(props, null);

        Store store = null;
        Folder folder = null;
        try
        {
            // Get the store
            store = session.getStore("imap");
            store.connect(REMOTE_HOST, USER_NAME, USER_PASSWORD);

            // Get folder
            folder = store.getFolder(TEST_FOLDER_NAME);
            folder.open(Folder.READ_ONLY);

            // Get directory
            Message message[] = folder.getMessages();

            for (int i = 0, n = message.length; i < n; i++)
            {
                message[i].getAllHeaders();
                
                Address[] from = message[i].getFrom();
                System.out.print(i + ": ");
                if (from != null)
                {
                    System.out.print(message[i].getFrom()[0] + "\t");
                }
                System.out.println(message[i].getSubject());

                Object content = message[i].getContent();
                if (content instanceof MimeMultipart)
                {
                    for (int j = 0, m = ((MimeMultipart)content).getCount(); j < m; j++)
                    {
                        BodyPart part = ((MimeMultipart)content).getBodyPart(j);
                        Object partContent = part.getContent();

                        if (partContent instanceof String)
                        {
                            String body = (String)partContent;
                        }
                        else if (partContent instanceof FilterInputStream)
                        {
                            FilterInputStream fis = (FilterInputStream)partContent;
                            BufferedInputStream bis = new BufferedInputStream(fis);

                           /* while (bis.available() > 0) 
                            {
                               bis.read();
                            }*/
                            byte[] bytes = new byte[524288];
                            while (bis.read(bytes) != -1)
                            {
                            }
                            bis.close();
                            fis.close();
                        }
                    }
                }
            
                int nn = 0;
            
            }

            
            
            t = System.currentTimeMillis() - t;
            logger.info("Time: " + t + " ms (" + t/1000 + " s)");
            logger.info("Length: " + message.length);

        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            fail(e.getMessage());
        }
        finally
        {
            // Close connection
            try
            {
                if (folder != null)
                {
                    folder.close(false);
                }
            }
            catch (MessagingException e)
            {
                logger.error(e.getMessage(), e);
                fail(e.getMessage());
            }
            try
            {
                if (store != null)
                {
                    store.close();
                }
            }
            catch (MessagingException e)
            {
                logger.error(e.getMessage(), e);
                fail(e.getMessage());
            }
        }

    }


}
