package org.alfresco.web.api;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.alfresco.i18n.I18NUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class TestAPIServer
{
    /**
     * The reader for interaction.
     */
    private BufferedReader fIn;

    private APIRegistry apiRegistry;
    
    /**
     * Last command issued
     */
    private String lastCommand = null;
    
    
    
    
    /**
     * Main entry point.
     */
    public static void main(String[] args)
    {
        try
        {
            TestAPIServer test = new TestAPIServer();
            test.rep();
        }
        catch(Throwable e)
        {
            StringWriter strWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(strWriter);
            e.printStackTrace(printWriter);
            System.out.println(strWriter.toString());
        }
        finally
        {
            System.exit(0);
        }
    }

    /**
     * Make up a new console.
     */
    public TestAPIServer()
    {
        fIn = new BufferedReader(new InputStreamReader(System.in));
        String[] CONFIG_LOCATIONS = new String[] { "classpath:alfresco/application-context.xml", "classpath:alfresco/web-api-application-context.xml" };
        ApplicationContext context = new ClassPathXmlApplicationContext(CONFIG_LOCATIONS);
        DeclarativeAPIRegistry testAPIRegistry = (DeclarativeAPIRegistry)context.getBean("web.api.test.Registry");
        testAPIRegistry.initServices();
        apiRegistry = testAPIRegistry;
    }
    
    /**
     * A Read-Eval-Print loop.
     */
    public void rep()
    {
        // accept commands
        while (true)
        {
            System.out.print("ok> ");
            try
            {
                // get command
                final String line = fIn.readLine();
                if (line.equals("exit") || line.equals("quit"))
                {
                    return;
                }
                
                // execute command in context of currently selected user
                long startms = System.currentTimeMillis();
                System.out.print(interpretCommand(line));
                System.out.println("" + (System.currentTimeMillis() - startms) + "ms");
            }
            catch (Exception e)
            {
                e.printStackTrace(System.err);
                System.out.println("");
            }
        }
    }
    
    /**
     * Execute a single command using the BufferedReader passed in for any data needed.
     * 
     * TODO: Use decent parser!
     * 
     * @param line The unparsed command
     * @return The textual output of the command.
     */
    private String interpretCommand(String line)
        throws IOException
    {
        String[] command = line.split(" ");
        if (command.length == 0)
        {
            command = new String[1];
            command[0] = line;
        }
        
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(bout);

        // repeat last command?
        if (command[0].equals("r"))
        {
            if (lastCommand == null)
            {
                return "No command entered yet.";
            }
            return "repeating command " + lastCommand + "\n\n" + interpretCommand(lastCommand);
        }
        
        // remember last command
        lastCommand = line;

        // execute command
        if (command[0].equals("help"))
        {
            // TODO:
            String helpFile = I18NUtil.getMessage("test_service.help");
            ClassPathResource helpResource = new ClassPathResource(helpFile);
            byte[] helpBytes = new byte[500];
            InputStream helpStream = helpResource.getInputStream();
            try
            {
                int read = helpStream.read(helpBytes);
                while (read != -1)
                {
                    bout.write(helpBytes, 0, read);
                    read = helpStream.read(helpBytes);
                }
            }
            finally
            {
                helpStream.close();
            }
        }
        
        else if (command[0].equals("req"))
        {
            if (command.length < 2)
            {
                return "Syntax Error.\n";
            }

            String uri = command[1];
            MockHttpServletRequest req = createRequest("get", uri);
            MockHttpServletResponse res = new MockHttpServletResponse();
            
            APIServiceMatch match = apiRegistry.findService(req.getMethod(), uri);
            if (match == null)
            {
                throw new APIException("No service bound to uri '" + uri + "'");
            }

            APIRequest apiReq = new APIRequest(req, match);
            APIResponse apiRes = new APIResponse(res);
            match.getService().execute(apiReq, apiRes);
            bout.write(res.getContentAsByteArray());
            out.println();
        }

        out.flush();
        String retVal = new String(bout.toByteArray());
        out.close();
        return retVal;
    }
        
    
    private MockHttpServletRequest createRequest(String method, String uri)
    {
        MockHttpServletRequest req = new MockHttpServletRequest("get", uri);

        // set parameters
        int iArgIndex = uri.indexOf('?');
        if (iArgIndex != -1 && iArgIndex != uri.length() -1)
        {
            String uriArgs = uri.substring(iArgIndex +1);
            String[] args = uriArgs.split("&");
            for (String arg : args)
            {
                String[] parts = arg.split("=");
                req.addParameter(parts[0], (parts.length == 2) ? parts[1] : null);
            }
        }
        
        // set path info
        req.setPathInfo(iArgIndex == -1 ? uri : uri.substring(0, iArgIndex));

        return req;
    }
    
}
