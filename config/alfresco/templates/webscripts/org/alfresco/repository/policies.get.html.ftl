<html>
<body>
<h1>Registered Policies</h1>

<ul>
 <li><a href="#policies">Policies Registered</a></li>
 <li><a href="#bypolicy">Classes bound to Policies</a></li>
 <li><a href="#byclass">Policies of each Class</a></li>
</ul>

<a name="policies"></a>
<h3>Policies Registered</h3>
<ul>
<#list registeredPolicies as policy>
 <li title="${policy.qname}">${policy.name}<br />
   <i>${policy.policy}</i></li>
</#list>
</ul>

<a name="bypolicy"></a>
<h3>Classes bound to Policies</h3>

<#list behavioursByPolicy?keys as policy>
 <h5>${policy}</h5>
 <ul>
   <#list behavioursByPolicy[policy] as bh>
     <li title="${bh.qname}">${bh.name}<br />
       <i>${bh.classname}</i> - ${bh.method} - ${bh.frequency}</li>
   </#list>
 </ul>
</#list>

<a name="byclass"></a>
<h3>Policies of each Class</h3>
<#list behavioursByClass?keys as cls>
 <h5>${cls}</h5>
 <ul>
   <#list behavioursByClass[cls] as bh>
     <li title="${bh.qname}">${bh.name}<br />
       <i>${bh.classname}</i> - ${bh.method} - ${bh.frequency}</li>
   </#list>
 </ul>
</#list>

</body>
</html>
