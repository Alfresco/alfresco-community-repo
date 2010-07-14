/*
 * Copyright (C) 2009-2010 Alfresco Software Limited.
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
package org.alfresco.repo.transfer.requisite;

import org.alfresco.repo.transfer.DeltaList;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.xml.sax.SAXException;

/**
 * A processor of the XML Transfer Requsite file to populate a DeltaList object
 * 
 * The requsite is parsed once and the delta list is available from getDeltaList at the end.
 * 
 * @author mrogers
 *
 */
public class DeltaListRequsiteProcessor implements TransferRequsiteProcessor
{

    DeltaList deltaList = null;
    
    public void missingContent(NodeRef node, QName qname, String name)
    {
        deltaList.getRequiredParts().add(name);
    }
    
    public void startTransferRequsite()
    {
        deltaList = new DeltaList();
    }
    
    public void endTransferRequsite()
    {
        // No op
    }
    
    /**
     * Get the delta list
     * @return the delta list or null if the XML provided does not contain the data.
     */
    public DeltaList getDeltaList()
    {
        return deltaList;
    }
    
}
