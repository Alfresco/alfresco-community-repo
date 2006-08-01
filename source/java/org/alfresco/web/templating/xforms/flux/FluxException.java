package org.alfresco.web.templating.xforms.flux;

/**
 * Used for signalling problems with Flux execution
 *
 * @author Joern Turner
 * @version $Id: FluxException.java,v 1.1 2005/11/08 17:34:07 joernt Exp $
 */
public class FluxException extends Exception{

    public FluxException() {
    }

    public FluxException(String string) {
        super(string);
    }

    public FluxException(Throwable throwable) {
        super(throwable);
    }
}


