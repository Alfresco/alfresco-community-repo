/*
 * Copyright 2015-2016 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

public class EndToEndIntegrationTest
{
    private final static String JAR_FILE_NAME = "alfresco-update-tool.jar";
    
    File targetDir;
    
    private String getBasePath()
    {
        String basePath = System.getProperty("base.alfresco.instance");
        if (basePath == null)
        {
            basePath = "./test-data/base-alf-installation"; 
        }
        File base = new File(targetDir, basePath);
        assertTrue("base instance does not exist :" + base, base.exists());
        
        return base.getAbsolutePath();
    }
    
    private String getThisPath()
    {
        String basePath = System.getProperty("this.alfresco.instance");
        if (basePath == null)
        {
            basePath = "./test-data/this-alf-installation"; 
        }
        File base = new File(targetDir, basePath);
        assertTrue("this instance (the one to update) does not exist :" + base, base.exists());
        
        return base.getAbsolutePath();
    }
    
    private String getUpdatePath()
    {
        String basePath = System.getProperty("unpacked.update.package");
        if (basePath == null)
        {
            basePath = "./test-data/alfresco-enterprise-update-package"; 
        }
        File base = new File(targetDir, basePath);
        assertTrue(base.isDirectory());
        assertTrue("the update package does not exist :" + base, base.exists());
        String[] dirs = base.list();
        assertTrue(dirs.length == 1);
        
        return new File(base, dirs[0]).getAbsolutePath();
    }
    
    private void initTargetDir()
    {
        String targetDir = System.getProperty("alfresco.target.dir");
        if (targetDir == null)
        {
            targetDir = "./target";    // test needs to be run in target dir.
        }
        this.targetDir = new File(targetDir);
        assertTrue("target dir does not exist :" + targetDir, this.targetDir.exists());
    }
    
    @Before
    public void setUp() throws Exception
    {
        initTargetDir();    
    }
    
    
    @Test
    public void testEndToEndUpdate() throws Exception
    {
       File updateThisOne = new File(getBasePath());
       
       File referenceInstance = new File(getThisPath());
       
       File updatePackage = new File(getUpdatePath());
        
       // Run the update
       runUpdateTool(updateThisOne, updatePackage); 
       
       // Run the diff tool
       compare(referenceInstance, updateThisOne);

    }  
    
    /**
     * Run the update tool
     */
    public void runUpdateTool(File instanceToUpdate, File updatePackage) throws Exception
    {
        // expect to find jar at "lib/alfresco-update-tool.jar"
        File jar = new File(updatePackage, "lib/" + JAR_FILE_NAME);
        assertTrue("lib/" + JAR_FILE_NAME, jar.exists());
        
        // expect to find update resources
        
        String options = " --assumeyes -u " + updatePackage;
        String cmd = "java -jar " +  jar.getAbsolutePath() + options + " " + instanceToUpdate.getPath();
        
        boolean found = runCommand(
                    targetDir,
                    cmd,
                    null,
                    0,
                    "The update was successful"
                    );
        
        assertTrue("The update was successful", found);
        
    }
    
    /**
     * Run the diff tool
     * 
     * @param src
     * @param dest
     */
    public void compare(File src, File dest)
    {
        //assertTrue("test not implemented", false);
    }
    
    /* 
    * Method to execute a command 
    * 
    * This variant of runCommand takes multiple expected messages.   
    * If a message is repeated twice in <i>expectedMessage</i> then the message must appear in 
    * the command line output at least twice to return true.
    * 
    * @param targetLocation location for executing the command
    * @param cmd the command to be executed
    * @param input - input for command line prompts, may be null if not required
    * @param expectedMessage... messages to be verified 
    * @param expectedReturnCode 0 for success
    * @return true the messages are all found
    * @throws IOException
    */ 
    public boolean runCommand(File targetLocation, String cmd, String[] input, int expectedReturnCode, String... expectedMessage) throws IOException, InterruptedException
    {
        Runtime rt = Runtime.getRuntime();    
        String line = null;
        Process pr = rt.exec(cmd, null, targetLocation);
        if(input != null && input.length > 0)
        {
            Input inputThread = new Input(pr.getOutputStream(), input);
            inputThread.start();
        }
        
        ArrayList<String> toFind = new ArrayList<String>();
        for(int i = 0; i < expectedMessage.length; i++)
        {
            toFind.add(expectedMessage[i]);
        }
        
        int found;
        try (BufferedReader out = new BufferedReader(new InputStreamReader(pr.getInputStream())))
        {
            while ((line = out.readLine()) != null)
            {
                found = -1;
                for(int i = 0; i < toFind.size() ; i++)
                {
                    if (line.contains(toFind.get(i)))
                    {
                        found = i;
                    }
                }
                System.out.println(line);
                
                if(found >= 0)
                {
                    toFind.remove(found);
                }
            }
        }
        
        int retCode = pr.waitFor();
        
        if(retCode != expectedReturnCode)
        {
            System.out.println("Not expected return code expected:" + expectedReturnCode + " actual: " + retCode);
            return false;
        }
        
        if(toFind.size() == 0)
        {
            return true;
        }
        
        System.out.println("Did not find expected message: " + toFind);
        
        return false;
    }
    
    /**
     * 
     */
    protected class Input extends Thread
    {
        OutputStream is;
        String[] input;

        Input(OutputStream is, String[] input)
        {
            this.is = is;
            this.input = input;
        }

        public void run()
        {
            try (BufferedWriter in = new BufferedWriter(new OutputStreamWriter(is));)
            {
                for (String line : input)
                {
                    in.write(line);
                    in.newLine();
                    System.out.println("wrote : " + line);
                }
            }
            catch (IOException e)
            {

            }
        }
    }
    
     
}
