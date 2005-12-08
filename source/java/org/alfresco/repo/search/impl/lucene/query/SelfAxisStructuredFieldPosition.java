/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.search.impl.lucene.query;

import java.io.IOException;

public class SelfAxisStructuredFieldPosition extends AbstractStructuredFieldPosition
{

    public SelfAxisStructuredFieldPosition()
    {
        super(null, true, false);
    }

    public int matches(int start, int end, int offset) throws IOException
    {
        return offset;
    }

    public String getDescription()
    {
        return "Self Axis";
    }

    public boolean linkSelf()
    {
        return true;
    }

    public boolean isTerminal()
    {
        return false;
    }

   
    
    
}
