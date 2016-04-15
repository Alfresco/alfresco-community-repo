
package org.alfresco.repo.virtual.ref;

import org.alfresco.util.Pair;

public interface PathHasher
{
    
    Pair<String,String> hash(String path);

    String lookup(Pair<String,String> hash);
}
