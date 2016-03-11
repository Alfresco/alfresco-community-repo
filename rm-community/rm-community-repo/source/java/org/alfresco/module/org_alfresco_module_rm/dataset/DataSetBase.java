package org.alfresco.module.org_alfresco_module_rm.dataset;

import org.apache.commons.lang.StringUtils;
import org.springframework.extensions.surf.util.I18NUtil;

public class DataSetBase implements DataSet
{

    /** Data set service */
    private DataSetService dataSetService;

    /** Data set label */
    private String label;

    /** Data set id */
    private String id;

    /** Data set path */
    private String path;

    /**
     * Sets the data set service
     *
     * @param dataSetService the data set service
     */
    public void setDataSetService(DataSetService dataSetService)
    {
        this.dataSetService = dataSetService;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.dataset.DataSet#getLabel()
     */
    public String getLabel()
    {
        String label = this.label;
        if (StringUtils.isBlank(label))
        {
            String propertyKey = "dataset." + getId() + ".label";
            label = I18NUtil.getMessage(propertyKey);
            if (StringUtils.isBlank(label))
            {
                label = propertyKey;
            }
        }
        return label;
    }

    /**
     * Sets the label of the data set service
     *
     * @param label the label
     */
    public void setLabel(String label)
    {
        this.label = label;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.dataset.DataSet#getId()
     */
    public String getId()
    {
        return this.id;
    }

    /**
     * Sets the id of the data set service
     *
     * @param id the id
     */
    public void setId(String id)
    {
        this.id = id;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.dataset.DataSet#getPath()
     */
    public String getPath()
    {
        return this.path;
    }

    /**
     * Sets the path of the data set service
     *
     * @param path the path
     */
    public void setPath(String path)
    {
        this.path = path;
    }

    /**
     * Registers the data set implementation with the data set service.
     */
    public void register()
    {
        this.dataSetService.register(this);
    }

}
