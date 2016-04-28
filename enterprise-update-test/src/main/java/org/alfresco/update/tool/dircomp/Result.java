/*
 * Copyright 2016 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.update.tool.dircomp;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple struct-style result data class.
 * 
 * @author Matt Ward
 */
public final class Result
{
    /**
     * The path within tree1 being compared (intended to be the updated Alfresco installation).
     * <p>
     * This field will be <code>null</code> if the file in question appears only in the tree2.
     */
    Path p1;
    /**
     * The path within tree2 being compared (intended to be the freshly installed equivalent of {@link #p1}).
     * <p>
     * This field will be <code>null</code> if the file in question appears only in the tree1.
     */
    Path p2;
    /**
     * Are the paths in {@link #p1} and {@link #p2} of identical content? If they refer to a directory, then
     * equal in this sense is to have the same directory name. If they refer to a plain file, then equal means
     * that they contain the exact same contents.
     */
    boolean equal;
    /**
     * The root path of sub-tree1 to which {@link #subResults} refers.
     * @see #subResults
     */
    Path subTree1;
    /**
     * The root path of sub-tree2 to which {@link #subResults} refers.
     * @see #subResults
     */
    Path subTree2;
    /**
     * If p1 and p2 refer to a special archive with differences, e.g. they refer to alfresco.war,
     * then a deep comparison of the archives will be performed and the results stored here.
     * <p>
     * The paths to the expanded archives will be stored in {@link #subTree1} and {@link #subTree2}
     * and all the paths in {@link #subResults} will be within those new roots.
     */
    List<Result> subResults = new ArrayList<>();
    
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (this.equal ? 1231 : 1237);
        result = prime * result + ((this.p1 == null) ? 0 : this.p1.hashCode());
        result = prime * result + ((this.p2 == null) ? 0 : this.p2.hashCode());
        return result;
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Result other = (Result) obj;
        if (this.equal != other.equal) return false;
        if (this.p1 == null)
        {
            if (other.p1 != null) return false;
        }
        else if (!this.p1.equals(other.p1)) return false;
        if (this.p2 == null)
        {
            if (other.p2 != null) return false;
        }
        else if (!this.p2.equals(other.p2)) return false;
        return true;
    }

    @Override
    public String toString()
    {
        return String.format("Result[p1=%s, p2=%s, equal=%b]", p1, p2, equal);
    }
}
