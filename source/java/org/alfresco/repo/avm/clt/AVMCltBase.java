/**
 * 
 */
package org.alfresco.repo.avm.clt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.avm.AVMRemote;
import org.alfresco.service.cmr.avmsync.AVMSyncService;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * This is the base class for AVM clts.
 * @author britt
 */
public abstract class AVMCltBase 
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
     * The ApplicationContext.
     */
    protected ConfigurableApplicationContext fContext;
    
    /**
     * Construct a new one. This takes care of instantiating
     * the application context and grabs references to the
     * services. 
     * @param args The program arguments.
     */
    protected AVMCltBase()
    {
        fContext = new ClassPathXmlApplicationContext("alfresco/avm-clt-context.xml");
        fAVMRemote = (AVMRemote)fContext.getBean("avmRemote");
        fAVMSyncService = (AVMSyncService)fContext.getBean("avmSyncService");
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
                    usage(usageMessage);
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
            usage(usageMessage);
        }
        // Do the work.
        run(flagValues, actualArgs);
        // Cleanup.
        fContext.close();
    }
    
    /**
     * Handle syntax error by exiting.
     * @param usageMessage The message to print.
     */
    protected void usage(String usageMessage)
    {
        System.err.println(usageMessage);
        fContext.close();
        System.exit(1);
    }
    
    protected abstract void run(Map<String, List<String>> flags, List<String> args);
}
