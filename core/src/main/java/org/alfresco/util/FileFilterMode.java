package org.alfresco.util;

public class FileFilterMode
{
    /**
     *  Clients for which specific hiding/visibility behaviour may be requested.
     *  Do not remove or change the order of
     */
    public static enum Client
    {
        cifs, imap, webdav, nfs, script, webclient, ftp, cmis, admin;
        
        /**
         * @deprecated Use {@link Client#valueOf(String)}
         */
        @Deprecated
        public static Client getClient(String clientStr)
        {
            if(clientStr.equals("cifs"))
            {
                return cifs;
            }
            else if(clientStr.equals("imap"))
            {
                return imap;
            }
            else if(clientStr.equals("webdav"))
            {
                return webdav;
            }
            else if(clientStr.equals("nfs"))
            {
                return nfs;
            }
            else if(clientStr.equals("ftp"))
            {
                return ftp;
            }
            else if(clientStr.equals("script"))
            {
                return script;
            }
            else if(clientStr.equals("webclient"))
            {
                return webclient;
            }
            else if(clientStr.equals("cmis"))
            {
                return cmis;
            }
            else if(clientStr.equals("admin"))
            {
                return admin;
            }
            else
            {
                throw new IllegalArgumentException();
            }
        }
    };
    
    public static enum Mode
    {
        BASIC, ENHANCED;
    };

    private static ThreadLocal<Client> client = new ThreadLocal<Client>()
    {
        protected Client initialValue() {
            return null;
        }
    };
    
    public static void clearClient()
    {
        client.set(null);
    }
    
    public static Client setClient(Client newClient)
    {
        Client oldClient = client.get();
        client.set(newClient);

        return oldClient;
    }

    public static Mode getMode()
    {
        Client client = getClient();
        if(client == null)
        {
            return Mode.BASIC;
        }
        else
        {
            switch(client)
            {
            case cifs :
            case nfs :
            case ftp :
            case webdav :
            case cmis :
            case admin :
                return Mode.ENHANCED;
            default:
                return Mode.BASIC;
            }
        }
    }
    
    public static Client getClient()
    {
        return client.get();
    }
}