package org.alfresco.repo.web.util.paging;


/**
 * A Page within a Cursor.
 * 
 * @author davidc
 */
public class Page
{
    Paging.PageType pageType;
    boolean zeroBasedIdx;
    int startIdx;
    int pageSize;
    
    /**
     * Construct
     * 
     * @param pageType  Page or Window
     * @param zeroBasedIdx  true => start index from 0
     * @param startIdx  start index
     * @param pageSize  page size
     */
    /*package*/ Page(Paging.PageType pageType, boolean zeroBasedIdx, int startIdx, int pageSize)
    {
        this.pageType = pageType;
        this.zeroBasedIdx = zeroBasedIdx;
        this.startIdx = startIdx;
        this.pageSize = pageSize;
    }

    /**
     * Gets the Page Type
     * 
     * @return  page type
     */
    /*package*/ Paging.PageType getType()
    {
        return pageType;
    }
    
    /**
     * Gets the page number
     * 
     * @return  page number
     */
    public int getNumber()
    {
        return startIdx;
    }
    
    /**
     * Gets the page size
     * 
     * @return  page size
     */
    public int getSize()
    {
        return pageSize;
    }

    /**
     * Is zero based page index
     * 
     * @return  true => page number starts from zero
     */
    public boolean isZeroBasedIdx()
    {
        return zeroBasedIdx;
    }
    
}
