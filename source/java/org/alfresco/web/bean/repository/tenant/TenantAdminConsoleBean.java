package org.alfresco.web.bean.repository.tenant;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;

import javax.faces.context.FacesContext;

import org.alfresco.repo.tenant.TenantInterpreter;
import org.alfresco.web.app.servlet.FacesHelper;


/**
 * Backing bean to support the Tenant Admin Console
 */
public class TenantAdminConsoleBean implements Serializable
{
    private static final long serialVersionUID = -9116623180660597894L;
    
   // command
    private String command = "";
    private String submittedCommand = "none";
    private long duration = 0L;
    private String result = null;

    // supporting repository services
    transient private TenantInterpreter tenantInterpreter;


    /**
     * @param tenantInterpreter  tenant admin interpreter
     */
    public void setTenantInterpreter(TenantInterpreter tenantInterpreter)
    {
        this.tenantInterpreter = tenantInterpreter;
    }
    
    /**
     * @return tenantInterpreter
     */
    private TenantInterpreter geTenantInterpreter()
    {
     //check for null for cluster environment
       if (tenantInterpreter == null)
       {
          tenantInterpreter = (TenantInterpreter) FacesHelper.getManagedBean(FacesContext.getCurrentInstance(), "tenantInterpreter");
       }
       return tenantInterpreter;
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
    	return (geTenantInterpreter() != null) ? geTenantInterpreter().getCurrentUserName() : null;
    }

    /**
     * Interpret tenant admin console command
     *
     * @param command  command
     */
    private void interpretCommand(String command)
    {
        try
        {
            long startms = System.currentTimeMillis();
            String result = (geTenantInterpreter() != null) ? geTenantInterpreter().interpretCommand(command) : "Tenant AdminConsole is not available - check that multi-tenancy is enabled !";
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
