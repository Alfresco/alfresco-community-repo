/*-----------------------------------------------------------------------------
*  Copyright 2007 Alfresco Inc.
*  
*  This program is free software; you can redistribute it and/or modify
*  it under the terms of the GNU General Public License as published by
*  the Free Software Foundation; either version 2 of the License, or
*  (at your option) any later version.
*  
*  This program is distributed in the hope that it will be useful, but
*  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
*  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
*  for more details.
*  
*  You should have received a copy of the GNU General Public License along
*  with this program; if not, write to the Free Software Foundation, Inc.,
*  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.  As a special
*  exception to the terms and conditions of version 2.0 of the GPL, you may
*  redistribute this Program in connection with Free/Libre and Open Source
*  Software ("FLOSS") applications as described in Alfresco's FLOSS exception.
*  You should have received a copy of the text describing the FLOSS exception,
*  and it is also available here:   http://www.alfresco.com/legal/licensing
*  
*  
*  Author  Jon Cox  <jcox@alfresco.com>
*  File    HrefValidationProgress.java
*----------------------------------------------------------------------------*/

package org.alfresco.linkvalidation;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.logging.Log;                                                                                                                                         
import org.apache.commons.logging.LogFactory;    

/**
*  This class allows the progress of synchronous calls to
*  updateHrefInfo to be be monitored asynchronously
*  in another thread via polling.
*
*  Whenever the status monitoring thread wishes to determine 
*  how many of the webaps, directories, files, or URLs have
*  been updated sucessfully so far, it can query  
*  getWebappUpdateCount(), getDirUpdateCount(),  
*  getFileUpdateCount(), or getUrlUpdateCount().
*  The monitoring thread can determine when the
*  call to updateHrefInfo() has completed by examining
*  the value returned by isDone().
*  <p>
*  Note:  It is safest to instantiate a fresh HrefValidationProgress 
*  object for every invocation of updateHrefInfo().
*/
public class HrefValidationProgress implements Serializable
{
    private static final long serialVersionUID = 3031274879702889688L;
    private static Log   log = LogFactory.getLog(HrefValidationProgress.class );    

    AtomicInteger webapp_update_count_;
    AtomicInteger dir_update_count_;
    AtomicInteger file_update_count_;
    AtomicInteger url_update_count_;
    AtomicBoolean is_done_;
    AtomicBoolean is_aborted_;

    public HrefValidationProgress() 
    {
        webapp_update_count_ = new AtomicInteger();
        dir_update_count_    = new AtomicInteger();
        file_update_count_   = new AtomicInteger();
        url_update_count_    = new AtomicInteger();
        is_done_             = new AtomicBoolean( false );
        is_aborted_          = new AtomicBoolean( false );
    }

    /**
    *  Returns the number of webapps that have been completely 
    *  URL-revalidated thus far by a call to updateHrefInfo().
    *  Note that it is possible to revalidate every webapp
    *  in a store via  updateHrefInfo(), so this value can
    *  be greater than 1.
    */
    public int getWebappUpdateCount() { return webapp_update_count_.intValue();}

    /**
    *  Returns the number of directories that have been completely 
    *  URL-revalidated thus far by a call to updateHrefInfo().
    */
    public int getDirUpdateCount()    { return dir_update_count_.intValue(); }

    /**
    *  Tell the thread that is validating hrefs to abort its current 
    *  operation, and cause that operation to declare it "done" and throw 
    *  LinkValidationAbortedException.
    *  
    *  Thus, if you have an observer polling progress & checking isDone(), 
    *  they'll also see that things are "finished", and can check to see 
    *  whether it's because isAborted() is true.
    */
    public void abort()        
    { 
        if(log.isDebugEnabled())
        {
            log.debug( "Validation request aborted via: " + 
                       "HrefValidationProgress.abort()");
        }

        is_aborted_.set( true ); 
        setDone( true );
    }

    /**
    *  Indicates whether or not the validation operation was halted by abort().
    */
    public boolean isAborted() { return is_aborted_.get(); }

    /**
    *  Returns the number of files that have been completely 
    *  URL-revalidated thus far by a call to updateHrefInfo().
    */
    public int getFileUpdateCount()   { return file_update_count_.intValue();}

    /**
    *  Returns the number of distinct URLs that have been 
    *  URL-revalidated thus far by a call to updateHrefInfo().
    */
    public int getUrlUpdateCount()    { return url_update_count_.intValue(); }

    /**
    *  Returns true if and only if the call to updateHrefInfo() has returned
    *  (whether by a normal return or via an exception).
    */
    public boolean isDone() { return is_done_.get(); }


    void init()
    {
        setDone( false );

        // some defensive measures against datastructure recycling
        //
        webapp_update_count_.set(0);
        dir_update_count_.set(0);   
        file_update_count_.set(0);  
        url_update_count_.set(0);   
    }


    int incrementWebappUpdateCount() 
    {
        return webapp_update_count_.incrementAndGet();
    }
    int incrementDirUpdateCount() {return dir_update_count_.incrementAndGet(); }
    int incrementFileUpdateCount(){return file_update_count_.incrementAndGet();}
    int incrementUrlUpdateCount() {return url_update_count_.incrementAndGet(); }

    void setDone(Boolean tf) { is_done_.set( tf ); }
}
