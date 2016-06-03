package org.alfresco.repo.tagging.script;

/**
 * Stores  total tags count together with tags to be sent to UI
 * 
 * @author Viachaslau Tsikhanovich
 *
 */
public class PagedTagsWrapper
{

    private String[] tagNames;
    
    private String total;

    public PagedTagsWrapper(String[] tagNames, int total)
    {
        super();
        this.setTagNames(tagNames);
        this.setTotal(total);
    }

    public String[] getTagNames()
    {
        return tagNames;
    }

    public void setTagNames(String[] tagNames)
    {
        this.tagNames = tagNames;
    }

    public String getTotal()
    {
        return total;
    }

    public void setTotal(int total)
    {
        this.total = String.valueOf(total);
    }

}
