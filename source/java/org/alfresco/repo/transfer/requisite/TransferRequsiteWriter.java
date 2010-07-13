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

import java.io.Writer;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.xml.sax.SAXException;

/**
 * Transfer Requsite Writer
 * 
 * This class formats the transfer requsite and prints it to the specified writer
 * 
 * It is a statefull object and writes one requsite at a time.
 *  
 */
public interface TransferRequsiteWriter
{
    
    /**
     * 
     * @param writer
     */
    void startTransferRequsite() ;
       
    /**
     * 
     */
    void endTransferRequsite() ;
    
    /**
     * 
     */
    void missingContent(NodeRef nodeRef, QName qName, String name);
}
