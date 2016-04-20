package org.alfresco.repo.importer.system;

import java.util.Date;

/**
 * Data holder of patch information that's to be exported and imported
 *
 * @author davidc
 */
public class PatchInfo
{
    public String id = null;
    public String description = null;
    public Integer fixesFromSchema = null;
    public Integer fixesToSchema = null;
    public Integer targetSchema = null;
    public Integer appliedToSchema = null;
    public String appliedToServer = null;
    public Date appliedOnDate = null;
    public Boolean wasExecuted = null;
    public Boolean succeeded = null;
    public String report = null;
}
