package org.alfresco.repo.activities;

public class SiteActivityTestCaseSensitivity extends AbstractSiteActivityTest
{
    static
    {
        System.setProperty("user.name.caseSensitive", "true");
    }
}
