/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.search.impl.lucene.query;

public abstract class AbstractStructuredFieldPosition implements StructuredFieldPosition
{
    private String termText;

    private boolean isTerminal;

    private boolean isAbsolute;

    private CachingTermPositions tps;

    public AbstractStructuredFieldPosition(String termText, boolean isTerminal, boolean isAbsolute)
    {
        super();
        this.termText = termText;
        this.isTerminal = isTerminal;
        this.isAbsolute = isAbsolute;
    }

    public boolean isTerminal()
    {
        return isTerminal;
    }

    protected void setTerminal(boolean isTerminal)
    {
        this.isTerminal = isTerminal;
    }

    public boolean isAbsolute()
    {
        return isAbsolute;
    }

    public boolean isRelative()
    {
        return !isAbsolute;
    }

    public String getTermText()
    {
        return termText;
    }

    public int getPosition()
    {
        return -1;
    }

    public void setCachingTermPositions(CachingTermPositions tps)
    {
        this.tps = tps;
    }

    public CachingTermPositions getCachingTermPositions()
    {
        return this.tps;
    }

    
    
    public boolean allowsLinkingBySelf()
    {
       return false;
    }

    public boolean allowslinkingByParent()
    {
        return true;
    }

    public boolean linkParent()
    {
        return true;
    }

    public boolean linkSelf()
    {
       return false;
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer(256);
        buffer.append(getDescription());
        buffer.append("<"+getTermText()+"> at "+getPosition());
        buffer.append(" Terminal = "+isTerminal());
        buffer.append(" Absolute = "+isAbsolute());
        return buffer.toString();
    }
    
    public abstract String getDescription();

    public boolean isDescendant()
    {
        return false;
    }
    
    public boolean matchesAll()
    {
        return getCachingTermPositions() == null;
    }

 

    
}
