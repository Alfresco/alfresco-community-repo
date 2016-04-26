package org.alfresco.repo.web.auth;

import org.alfresco.jlan.server.auth.spnego.NegTokenInit;
import org.alfresco.jlan.server.auth.spnego.NegTokenTarg;

/**
 * {@link WebCredentials} implementation for holding Kerberos credentials.
 */
public class KerberosCredentials implements WebCredentials
{
    private static final long serialVersionUID = 4625258932647351551L;

    private NegTokenInit negToken;
    private NegTokenTarg negTokenTarg;

    public KerberosCredentials(NegTokenInit negToken, NegTokenTarg negTokenTarg)
    {
        this.negToken = negToken;
        this.negTokenTarg = negTokenTarg;
    }

    public KerberosCredentials(NegTokenInit negToken)
    {
        this.negToken = negToken;
    }

}
