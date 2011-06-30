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
package org.alfresco.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorker;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorkerAdaptor;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.person.PersonServiceImpl;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.NoSuchPersonException;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.VmShutdownListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Rename user tool. This tool provides minimal support for renaming users.
 * See {@link displayHelp} message for restrictions.
 * <pre>
 * Usage: renameUser -user username [options] oldUsername newUsername");
 *        renameUser -user username [options] -file filename");
 * </pre>
 * The csv file has a simple comma separated list, with
 * a pair of usernames on each line. Comments and blank
 * lines may also be included. For example:
 * <pre>
 * # List of usernames to change
 * 
 * # oldUsername,newUsername
 * johnp,ceo # President and CEO
 * johnn,cto # CTO and Chairman
 * </pre>
 * 
 * @author Alan Davis
 */
public class RenameUser extends Tool
{
    private static Log logger = LogFactory.getLog(RenameUser.class);
    
    /** User Rename Tool Context */
    protected RenameUserToolContext context;
    private boolean login = true;
    
    PersonService personService;
    NodeService nodeService;

    private PersonService getPersonService()
    {
        if (personService == null)
        {
            personService = getServiceRegistry().getPersonService();
        }
        return personService;
    }

    private NodeService getNodeService()
    {
        if (nodeService == null)
        {
            nodeService = getServiceRegistry().getNodeService();
        }
        return nodeService;
    }
    
    public void setLogin(boolean login)
    {
        this.login = login;
    }

    /**
     * Entry Point
     * 
     * @param args
     */
    public static void main(String[] args)
    {
        Tool tool = new RenameUser();
        tool.start(args);
    }
    
    /*
     * (non-Javadoc)
     * @see org.alfresco.tools.Tool#processArgs(java.lang.String[])
     */
    @Override
    protected ToolContext processArgs(String[] args)
        throws ToolArgumentException
    {
        context = new RenameUserToolContext();
        context.setLogin(login);

        int i = 0;
        while (i < args.length)
        {
            if (args[i].equals("-h") || args[i].equals("-help"))
            {
                context.setHelp(true);
                break;
            }
            else if (args[i].equals("-user"))
            {
                i++;
                if (i == args.length || args[i].length() == 0)
                {
                    throw new ToolArgumentException("The value <user> for the option -user must be specified");
                }
                context.setUsername(args[i]);
            }
            else if (args[i].equals("-pwd"))
            {
                i++;
                if (i == args.length || args[i].length() == 0)
                {
                    throw new ToolArgumentException("The value <password> for the option -pwd must be specified");
                }
                context.setPassword(args[i]);
            }
            else if (args[i].equals("-encoding"))
            {
                i++;
                if (i == args.length || args[i].length() == 0)
                {
                    throw new ToolArgumentException("The value <encoding> for the option -encoding must be specified");
                }
                try
                {
                    context.encoding = Charset.forName(args[i]);
                }
                catch (IllegalCharsetNameException e)
                { 
                    throw new ToolArgumentException("The value <encoding> is not recognised");
                }
                catch (UnsupportedCharsetException e)
                {
                    throw new ToolArgumentException("The value <encoding> is unsupported");
                }
            }
            else if (args[i].equals("-quiet"))
            {
                context.setQuiet(true);
            }
            else if (args[i].equals("-verbose"))
            {
                context.setVerbose(true);
            }
            else if (args[i].equals("-f") || args[i].equals("-file"))
            {
                i++;
                if (i == args.length || args[i].length() == 0)
                {
                    throw new ToolArgumentException("The value <filename> for the option -file must be specified");
                }
                context.setFilename(args[i]);
            }
            else if (!args[i].startsWith("-"))
            {
                i++;
                if (i == args.length || args[i-1].trim().length() == 0 || args[i].trim().length() == 0)
                {
                    throw new ToolArgumentException("Both <oldUsername> <newUsername> must be specified");
                }
                if (context.userCount() > 0)
                {
                    throw new ToolArgumentException("Only one <oldUsername> <newUsername> pair may be " +
                    		"specified on the command line. See the -file option");
                }
                String oldUsername = args[i-1].trim();
                String newUsername = args[i].trim();
                String error = context.add(-1, null, oldUsername, newUsername);
                if (error != null)
                {
                    throw new ToolArgumentException(error);
                }
            }
            else
            {
                throw new ToolArgumentException("Unknown option " + args[i]);
            }

            // next argument
            i++;
        }

        return context;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.tools.Tool#displayHelp()
     */
    protected @Override
    /*package*/ void displayHelp()
    {
        logError("This tool provides minimal support for renaming users. It fixes");
        logError("authorities, group memberships and current zone (older versions");
        logError("still require a property change).");
        logError("");
        logError("WARNING: It does NOT change properties that store the username such");
        logError("         as (creator, modifier, lock owner or owner). Of these owner");
        logError("         and lock affect user rights. The username is also used");
        logError("         directly in workflow, for RM caveats, for Share invites and");
        logError("         auditing");
        logError("");
        logError("Usage: renameUser -user username [options] oldUsername newUsername");
        logError("       renameUser -user username [options] -file filename");
        logError("");
        logError("   username: username for login");
        logError("oldUsername: current username ");
        logError("newUsername: replacement username ");
        logError("");
        logError("Options:");
        logError(" -h[elp] display this help");
        logError(" -pwd password for login");
        logError(" -f[ile] csv file of old and new usernames");
        logError(" -encoding for source file (default: " + Charset.defaultCharset() + ")");
        logError(" -quiet do not display any messages during rename");
        logError(" -verbose report rename progress");
    }
    
    /*
     * (non-Javadoc)
     * @see org.alfresco.tools.Tool#getToolName()
     */
    @Override
    protected String getToolName()
    {
        return "Alfresco Rename User";
    }
    
    /*
     * (non-Javadoc)
     * @see org.alfresco.tools.Tool#execute()
     */
    @Override
    protected int execute() throws ToolException
    {
        // Used for ability to be final and have a set
        final AtomicInteger status = new AtomicInteger(0);

        BatchProcessWorker<User> worker = new BatchProcessWorkerAdaptor<User>()
        {
            public void process(final User user) throws Throwable
            {
                RunAsWork<Void> runAsWork = new RunAsWork<Void>()
                {
                    @Override
                    public Void doWork() throws Exception
                    {
                        try
                        {
                            renameUser(user.getOldUsername(), user.getNewUsername());
                        }
                        catch (Throwable t)
                        {
                            status.set(handleError(t));
                        }
                        return null;
                    }
                };
                AuthenticationUtil.runAs(runAsWork, context.getUsername());
            }
        };
        
        // Use 2 threads, 20 User objects per transaction. Log every 100 entries.
        BatchProcessor<User> processor = new BatchProcessor<User>(
                "HomeFolderProviderSynchronizer",
                getServiceRegistry().getTransactionService().getRetryingTransactionHelper(),
                new WorkProvider(context),
                2, 20,
                null,
                logger, 100);
        processor.process(worker, true);

        return status.get();
    }
    
    private void renameUser(String oldUsername, String newUsername)
    {
        logInfo("\""+oldUsername+"\" --> \""+newUsername+"\""); 
        try
        {
            NodeRef person = getPersonService().getPerson(oldUsername, false);
            
            // Allow us to update the username just like the LDAP process
            AlfrescoTransactionSupport.bindResource(PersonServiceImpl.KEY_ALLOW_UID_UPDATE, Boolean.TRUE);

            // Update the username property which will result in a PersonServiceImpl.onUpdateProperties call
            // on commit.
            getNodeService().setProperty(person, ContentModel.PROP_USERNAME, newUsername);
        }
        catch (NoSuchPersonException e)
        {
            logError("User does not exist: "+oldUsername);
        }
    }
    
    public class User
    {
        private final String oldUsername;
        private final String newUsername;
        
        public User(String oldUsername, String newUsername)
        {
            this.oldUsername = oldUsername;
            this.newUsername = newUsername;
        }

        public String getOldUsername()
        {
            return oldUsername;
        }

        public String getNewUsername()
        {
            return newUsername;
        }
    }
    
    public class RenameUserToolContext extends ToolContext
    {
        /**
         * Old and new usernames to change.
         */
        private List<User> usernames = new ArrayList<User>();
        
        // Internal - used check the name has not been used before.
        private Set<String> uniqueNames = new HashSet<String>();
        
        /**
         * Source filename of usernames.
         */
        private String filename;
        
        /**
         * Encoding of filename of usernames.
         */
        private Charset encoding = Charset.defaultCharset();
        
        public void setFilename(String filename)
        {
            this.filename = filename;
        }
        
        public String add(int lineNumber, String line, String oldUsername, String newUsername)
        {
            String error = null;
            if (oldUsername.equals(newUsername))
            {
                error = "Old and new usernames are the same";
                if (line != null)
                    error = "Error on line " + lineNumber + " ("+error+"): " + line;
            }
            else if (uniqueNames.contains(oldUsername))
            {
                error = "Old username already specified";
                if (line != null)
                    error = "Error on line " + lineNumber + " ("+error+"): " + line;
            }
            else if (uniqueNames.contains(newUsername))
            {
                error = "New username already specified";
                if (line != null)
                    error = "Error on line " + lineNumber + " ("+error+"): " + line;
            }
            else
            {
                add(new User(oldUsername, newUsername));
            }
            return error;
        }

        private void add(User user)
        {
            usernames.add(user);
            uniqueNames.add(user.getOldUsername());
            uniqueNames.add(user.getNewUsername());
        }
        
        public int userCount()
        {
            return usernames.size();
        }
        
        public Iterator<User> iterator()
        {
            return usernames.iterator();
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.tools.ToolContext#validate()
         */
        @Override
        /*package*/ void validate()
        {
            super.validate();
            
            if (filename != null)
            {
                if (userCount() > 0)
                {
                    throw new ToolArgumentException("<filename> should not have been specified if " +
                            "<oldUsername> <newUsername> has been specified on the command line.");
                }
                File file = new File(filename);
                if (!file.exists())
                {
                    throw new ToolArgumentException("File " + filename + " does not exist.");
                }
                if (!readFile(file))
                {
                    throw new ToolArgumentException("File " + filename + " contained errors.");
                }
            }
            
            if (userCount() == 0)
            {
                throw new ToolArgumentException("No old and new usernames have been specified.");
            }
        }

        /**
         * Read the user names out of the file.
         * @param file to be read
         * @return {@code true} if there were no problems found with the file contents.
         */
        private boolean readFile(File file)
        {
            BufferedReader in = null;
            boolean noErrors = true;
            try
            {
                in  = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding.name()));
                int lineNumber = 1;
                for (String line = in.readLine(); line != null; line = in.readLine(), lineNumber++)
                {
                    int i = line.indexOf('#');
                    if (i != -1)
                    {
                        line = line.substring(0, i);
                    }
                    if (line.trim().length() != 0)
                    {
                        String[] names = line.split(",");
                        String oldUsername = names[0].trim();
                        String newUsername = names[1].trim();
                        if (names.length != 2 || oldUsername.length() == 0 || newUsername.length() == 0)
                        {
                            RenameUser.this.logError("Error on line " + lineNumber + ": " + line);
                            noErrors = false;
                        }
                        else
                        {
                            String error = context.add(lineNumber, line, oldUsername, newUsername);
                            if (error != null)
                            {
                                RenameUser.this.logError(error);
                                noErrors = false;
                            }
                        }
                    }
                }
            }
            catch (IOException e)
            {
                throw new ToolArgumentException("Failed to read <filename>.", e);
            }
            finally
            {
                if (in != null)
                {
                    try
                    {
                        in.close();
                    } catch (IOException e)
                    {
                        // ignore
                    }
                }
            }
            return noErrors;
        }
    }

    // BatchProcessWorkProvider returns batches of 100 User objects.
    private class WorkProvider implements BatchProcessWorkProvider<User>
    {
        private static final int BATCH_SIZE = 100;
        
        private final VmShutdownListener vmShutdownLister = new VmShutdownListener("getRenameUserWorkProvider");
        private final Iterator<User> iterator;
        private final int size;
        
        public WorkProvider(RenameUserToolContext context)
        {
            iterator = context.iterator();
            size = context.userCount();
        }

        @Override
        public synchronized int getTotalEstimatedWorkSize()
        {
            return size;
        }

        @Override
        public synchronized Collection<User> getNextWork()
        {
            if (vmShutdownLister.isVmShuttingDown())
            {
                return Collections.emptyList();
            }
            
            Collection<User> results = new ArrayList<User>(BATCH_SIZE);
            while (results.size() < BATCH_SIZE && iterator.hasNext())
            {
                results.add(iterator.next());
            }
            return results;
        }
    }
}
