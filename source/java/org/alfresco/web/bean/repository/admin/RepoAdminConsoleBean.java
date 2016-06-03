package org.alfresco.web.bean.repository.admin;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;

import javax.faces.context.FacesContext;

import org.alfresco.repo.admin.RepoAdminInterpreter;
import org.alfresco.web.app.servlet.FacesHelper;


/**
 * Backing bean to support the Repository Admin Console
 */
public class RepoAdminConsoleBean implements Serializable
{
    private static final long serialVersionUID = -8131607149189675180L;
    
   // command
    private String command = "";
    private String submittedCommand = "none";
    private long duration = 0L;
    private String result = null;

    // supporting repository services
    transient private RepoAdminInterpreter repoAdminInterpreter;


    /**
     * @param repoAdminInterpreter  repo admin interpreter
     */
    public void setRepoAdminInterpreter(RepoAdminInterpreter repoAdminInterpreter)
    {
        this.repoAdminInterpreter = repoAdminInterpreter;
    }

    /**
     *@return repoAdminInterpreter
     */
    private RepoAdminInterpreter getRepoAdminInterpreter()
    {
     //check for null for cluster environment
       if (repoAdminInterpreter == null)
       {
          repoAdminInterpreter = (RepoAdminInterpreter) FacesHelper.getManagedBean(FacesContext.getCurrentInstance(), "repoAdminInterpreter");
       }
       return repoAdminInterpreter;
    }
    
    /**
     * Gets the command result
     *
     * @return  result
     */
    public String getResult()
    {
        if (result == null)
        {
            interpretCommand("help");
        }
        return result;
    }

    /**
     * Sets the command result
     *
     * @param result String
     */
    public void setResult(String result)
    {
        this.result = result;
    }

    /**
     * Gets the current query
     *
     * @return  query statement
     */
    public String getCommand()
    {
        return command;
    }

    /**
     * Set the current command
     *
     * @param command   command
     */
    public void setCommand(String command)
    {
        this.command = command;
    }

    /**
     * Gets the submitted command
     *
     * @return  submitted command
     */
    public String getSubmittedCommand()
    {
        return submittedCommand;
    }

    /**
     * Set the submitted command
     *
     * @param submittedCommand The submitted command
     */
    public void setSubmittedCommand(String submittedCommand)
    {
        this.submittedCommand = submittedCommand;
    }

    /**
     * Gets the last command duration
     *
     * @return  command duration
     */
    public long getDuration()
    {
        return duration;
    }

    /**
     * Set the duration
     *
     * @param duration   The duration
     */
    public void setDuration(long duration)
    {
        this.duration = duration;
    }

    /**
     * Action to submit command
     *
     * @return  next action
     */
    public String submitCommand()
    {
        interpretCommand(command);
        return "success";
    }

    /**
     * Gets the current user name
     *
     * @return  user name
     */
    public String getCurrentUserName()
    {
        return getRepoAdminInterpreter().getCurrentUserName();
    }

    /**
     * Interpret repo admin console command
     *
     * @param command  command
     */
    private void interpretCommand(String command)
    {
        try
        {
            long startms = System.currentTimeMillis();
            String result = getRepoAdminInterpreter().interpretCommand(command);
            setDuration(System.currentTimeMillis() - startms);
            setResult(result);
            setCommand("");
            setSubmittedCommand(command);
        }
        catch (Exception e)
        {
            StringWriter stackTrace = new StringWriter();
            e.printStackTrace(new PrintWriter(stackTrace));
            setResult(stackTrace.toString());
        }
    }

}
