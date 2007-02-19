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
package org.alfresco.service.cmr.repository;

import java.io.Writer;

/**
 * Interface to be implemented by template engine wrapper classes. The developer is responsible
 * for interfacing to an appropriate template engine, using the supplied data model as input to
 * the template and directing the output to the Writer stream. 
 * 
 * @author Kevin Roast
 */
public interface TemplateProcessor
{
    /**
     * Process a template against the supplied data model and write to the out.
     * 
     * @param template       Template name/path
     * @param model          Object model to process template against
     * @param out            Writer object to send output too
     */
    public void process(String template, Object model, Writer out);
    
    /**
     * Process a string template against the supplied data model and write to the out.
     * 
     * @param template       Template string
     * @param model          Object model to process template against
     * @param out            Writer object to send output too
     */
    public void processString(String template, Object model, Writer out);
}
