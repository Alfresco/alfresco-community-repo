<h1>        6.1.N
</h1>
<h2>
  New Features
</h2>
<ul>
  <li>
    ActiveMQ:
  </li>
</ul>
<h2>
  Deprecations
</h2>
<ul>
  <li>
    TransformService and RenditionService: All Java APIs related to TransformService and RenditionService have been deprecated; the ability to perform arbitrary transformations will be phased out as the new DBP Transform Service takes effect.  Renditions can be triggered using the existing repository REST API but will be processed asynchronously using the new services.<br/>
  </li>
</ul>
<h2>
  Known issues
</h2>
<ul>
  <li>
    Due to the changes to the RenditionService the Media Management AMP is not supported yet.<br/>
  </li>
</ul>

<h1>        Release Notes - Alfresco - Version Community Edition 201808 EA
</h1>                                                                                                                                                                                                                                                            
<h2>        Bug
</h2>
<ul>
<li>[<a href='https://issues.alfresco.com/jira/browse/ALF-21992'>ALF-21992</a>] -         BehaviourFilterImpl.isEnabled(NodeRef, QName) is checking wrong QName in case of subClass
</li>
<li>[<a href='https://issues.alfresco.com/jira/browse/ALF-22006'>ALF-22006</a>] -         VersionServicePolicies cannot be disabled on a specific node
</li>
<li>[<a href='https://issues.alfresco.com/jira/browse/ALF-22007'>ALF-22007</a>] -         TransactionListeners are executed in unpredictable order
</li>
</ul>
                
<h2>        Improvement
</h2>
<ul>
<li>[<a href='https://issues.alfresco.com/jira/browse/ALF-22011'>ALF-22011</a>] -         Upgrade to XMLBeans 3.0.0
</li>
</ul>


<h1>        Release Notes - Alfresco - Version Community Edition 201806 GA
</h1>                                                                                                                                                                                                                                                            
<h2>        Bug
</h2>
<ul>
<li>[<a href='https://issues.alfresco.com/jira/browse/ALF-22000'>ALF-22000</a>] -         Docker: HTML link 693ce565f4c4:8080/share but name 
</li>
<li>[<a href='https://issues.alfresco.com/jira/browse/ALF-22008'>ALF-22008</a>] -         It is not possible to upload a document without versions using CMIS
</li>
</ul>
                                                                                
