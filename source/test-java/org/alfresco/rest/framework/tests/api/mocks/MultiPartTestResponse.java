
package org.alfresco.rest.framework.tests.api.mocks;

/**
 * Simple mock pojo for MultiPart response.
 *
 * @author Jamal Kaabi-Mofrad
 */
public class MultiPartTestResponse
{

    private String fileName;

    public MultiPartTestResponse(String fileName)
    {
        this.fileName = fileName;
    }

    public String getFileName()
    {
        return this.fileName;
    }
}
