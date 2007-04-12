/**
 * 
 */
package org.alfresco.repo.clt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.remote.ClientTicketHolder;
import org.alfresco.repo.remote.ClientTicketHolderGlobal;
import org.alfresco.service.cmr.avmsync.AVMSyncService;
import org.alfresco.service.cmr.remote.AVMRemote;
import org.alfresco.service.cmr.remote.RepoRemote;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.util.Pair;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * This is the base class for AVM clts.
 * @author britt
 */
public abstract class CltBase 
{
    /**
     * The instance of the remote interface.
     */
    protected AVMRemote fAVMRemote;
    
    /**
     * The instance of the remote sync service interface.
     */
    protected AVMSyncService fAVMSyncService;

    /**
     * The instance of the remote repo interface.
     */
    protected RepoRemote fRepoRemote;
    
    /**
     * The ApplicationContext.
     */
    protected ConfigurableApplicationContext fContext;
    
    /**
     * The Authentication Service.
     */
    protected AuthenticationService fAuthenticationService;
    
    /**
     * The usage string.
     */
    private String fUsage;
    
    /**
     * Construct a new one. This takes care of instantiating
     * the application context and grabs references to the
     * services. 
     * @param args The program arguments.
     */
    protected CltBase()
    {
        fContext = new ClassPathXmlApplicationContext("alfresco/clt-context.xml");
        fAVMRemote = (AVMRemote)fContext.getBean("avmRemote");
        fAVMSyncService = (AVMSyncService)fContext.getBean("avmSyncService");
        fRepoRemote = (RepoRemote)fContext.getBean("repoRemote");
        fAuthenticationService = (AuthenticationService)fContext.getBean("authenticationService");
        fAuthenticationService.authenticate(System.getenv("ALFUSER"), System.getenv("ALFPASSWORD").toCharArray());
        String ticket = fAuthenticationService.getCurrentTicket();
        ((ClientTicketHolder)fContext.getBean("clientTicketHolder")).setTicket(ticket);
    }
    
    /**
     * All clts go through this call. This parses the arguments, exits if 
     * there are any errors and then passes the broken flags and arguments
     * to the run method of the derived clt.
     * @param args The raw command line arguments.
     * @param flagDefs The definition of what flags to accept and their
     * arities.
     * @param minArgs The minimum number of actual arguments expected.
     * @param usageMessage The message that should be printed if there is a 
     * syntax error.
     */
    public void exec(String [] args, 
                     Object [] flagDefs,
                     int minArgs,
                     String usageMessage)
    {
        fUsage = usageMessage;
        Map<String, Integer> flagArgs = new HashMap<String, Integer>();
        Map<String, List<String>> flagValues = new HashMap<String, List<String>>();
        List<String> actualArgs = new ArrayList<String>();
        // Convert the flag definitions into a convenient form.
        for (int i = 0; i < flagDefs.length / 2; i++)
        {
            flagArgs.put((String)flagDefs[i * 2], (Integer)flagDefs[i * 2 + 1]);
        }
        // Walk through the raw command line arguments.
        int pos = 0;
        while (pos < args.length)
        {
            if (args[pos].equals("-h"))
            {
                usage();
            }
            // If the argument is one of the accepted flags then it's
            // a flag.
            if (flagArgs.containsKey(args[pos]))
            {
                String flag = args[pos];
                pos++;
                int count = flagArgs.get(flag);
                // Check for too few arguments
                if (args.length - pos < count)
                {
                    usage();
                }
                // Stuff the parsed flag away.
                List<String> flArgs = new ArrayList<String>();
                for (int i = 0; i < count; i++)
                {
                    flArgs.add(args[pos + i]);
                }
                flagValues.put(flag, flArgs);
                pos += count;
                continue;
            }
            // Otherwise its just a plain old arg.
            actualArgs.add(args[pos]);
            pos++;
        }
        // Check for too few arguments.
        if (actualArgs.size() < minArgs)
        {
            usage();
        }
        // Do the work.
        run(flagValues, actualArgs);
        // Cleanup.
        fContext.close();
    }
    
    /**
     * Handle syntax error by exiting.
     */
    protected void usage()
    {
        System.err.println(fUsage);
        fContext.close();
        System.exit(1);
    }
    
    /**
     * Utility to split an AVM path into a parent path and a
     * base name.
     * @param path The path to split.
     * @return An array of 1 or 2 Strings representing the parent path
     * and the base name, or just the path if the path given is a root path.
     */
    protected String[] splitPath(String path)
    {
        if (path.endsWith(":/"))
        {
            String [] ret = { path };
            return ret;
        }
        int lastSlash = path.lastIndexOf("/");
        if (lastSlash == -1)
        {
            System.err.println("Malformed path: " + path);
            fContext.close();
            System.exit(1);
        }
        String name = path.substring(lastSlash + 1);
        String parent = path.substring(0, lastSlash);
        if (parent.endsWith(":"))
        {
            parent = parent + "/";
        }
        while (parent.endsWith("/") && !parent.endsWith(":/"))
        {
            parent = parent.substring(0, parent.length() - 1);
        }
        String [] ret = { parent, name };
        return ret;
    }
    
    protected void copyStream(InputStream in, OutputStream out)
    {
        try
        {
            byte [] buff = new byte[8192];
            int read = 0;
            while ((read = in.read(buff)) != -1)
            {
                out.write(buff, 0, read);
            }
            in.close();
            out.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            fContext.close();
            System.exit(1);
        }
    }
    
    protected Pair<String, Integer> splitPathVersion(String pathVersion)
    {
        int index = pathVersion.lastIndexOf('@');
        if (index == -1)
        {
            usage();
        }
        String path = pathVersion.substring(0, index);
        int version = Integer.parseInt(pathVersion.substring(index + 1));
        return new Pair<String, Integer>(path, version);
    }
    
    protected abstract void run(Map<String, List<String>> flags, List<String> args);
}
