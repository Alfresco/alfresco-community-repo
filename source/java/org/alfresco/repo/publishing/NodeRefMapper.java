/*
 * Copyright (C) 2009-2011 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.publishing;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * @author Brian
 * @since 4.0
 */
public class NodeRefMapper
{
    public NodeRef mapSourceNodeRef(NodeRef node)
    {
        StringBuilder nodeId = new StringBuilder(node.getId());
        if (nodeId.length() == 36)
        {
            for (int index = 35; index >= 0; --index)
            {
                int srcChar = nodeId.charAt(index);
                if (srcChar == '-')
                {
                    continue;
                }
                int destChar = srcCharToDestChar(srcChar);
                if (destChar != srcChar)
                {
                    nodeId.setCharAt(index, (char)destChar);
                    break;
                }
            }
        }
        else
        {
            nodeId.append('f');
        }
        return new NodeRef(node.getStoreRef(), nodeId.toString());
    }

    public NodeRef mapDestinationNodeRef(NodeRef node)
    {
        StringBuilder nodeId = new StringBuilder(node.getId());
        if (node.getId().endsWith("f"))
        {
            nodeId.deleteCharAt(nodeId.length() - 1);
        }
        else
        {
            int lastDestCharIndex = nodeId.length();
            int lastDestChar = 0;
            for (int index = nodeId.length() - 1; index >= 0; --index)
            {
                int destChar = nodeId.charAt(index);
                if (destChar == '-')
                {
                    continue;
                }
                if (isDestChar(destChar))
                {
                    lastDestCharIndex = index;
                    lastDestChar = destChar;
                }
                else
                {
                    break;
                }
            }
            if (lastDestCharIndex < nodeId.length())
            {
                int srcChar = destCharToSrcChar(lastDestChar);
                nodeId.setCharAt(lastDestCharIndex, (char)srcChar);
            }
        }
        return new NodeRef(node.getStoreRef(), nodeId.toString());
    }

    private int srcCharToDestChar(int ch)
    {
        int result = ch;
        if (ch >= '0' && ch <= '9')
        {
            result = (ch - '0') + 'A';
        }
        else if (ch >= 'a' && ch <= 'f')
        {
            result = (ch - 'a') + 10 + 'A';
        }
        return result;
    }

    private int destCharToSrcChar(int ch)
    {
        int result = ch;
        if (ch >= 'A' && ch <= 'J')   //'J' is the 10th uppercase character 
        {
            result = ch - 'A' + '0';
        }
        else if (ch >= 'K' && ch <= 'P')  //'P' is the 16th uppercase character
        {
            result = ch - 'K' + 'a';
        }
        return result;
    }
    
    private boolean isDestChar(int ch)
    {
        return (ch >= 'A' && ch <= 'P');
    }
}
