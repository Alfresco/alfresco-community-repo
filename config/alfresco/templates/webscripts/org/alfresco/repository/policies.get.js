// Make things more java like
importPackage(java.lang);


// Pinch the service registry off the ScriptUtils bean
var f = utils.getClass().getDeclaredField("services");
f.setAccessible(true);
var serviceRegistry = f.get(utils);
//System.err.println(serviceRegistry);

// Fetch the bean factory from it
// (Getting random services straight from the registry doesn't always work)
var bf = serviceRegistry.getClass().getDeclaredField("beanFactory");
bf.setAccessible(true);
var beanFactory = bf.get(serviceRegistry);
//System.err.println(beanFactory);

// Grab the Policies object
importPackage(org.alfresco.service.namespace);
var policyComponent = beanFactory.getBean("policyComponent");
//System.err.println(policyComponent);


// Fetch the list of registered policies
var registeredPolicies = policyComponent.getRegisteredPolicies();
//System.err.println(registeredPolicies);

// Now get the list of things defined for those policies
var fc = policyComponent.getClass().getDeclaredField("classBehaviours");
fc.setAccessible(true);
var classBehaviours = fc.get(policyComponent);
//System.err.println(classBehaviours);


// Produce something FreeMarker stands a chance with for the list
//  of the registered policies
var regPolicies = new Array();
var rpi = registeredPolicies.iterator();
for(var rp = rpi.next(); rpi.hasNext(); rp = rpi.next() ) {
   var p = {};
   p.qname = rp.getName().toString();
   p.name = rp.getName().getLocalName();
   p.policy = rp.getPolicyInterface().toString();

   regPolicies.push( p );
}
model.registeredPolicies = regPolicies;


// Ditto for the things that are called by them
var behavioursByPolicy = {};
var behavioursByClass = {};
for(var i=0; i<classBehaviours.length; i++) {
   var cbi = classBehaviours[i];
   // Work around nasty object/ClassBehaviour issue
   var getAll = cbi.getClass().getMethod("getAll");
   getAll.setAccessible(true);
   var behaviours = getAll.invoke(cbi);

   for(var j=0; j<behaviours.size(); j++) {
       var bhd = behaviours.get(j);
       var behaviour = bhd.getBehaviour();
       var qn = bhd.getPolicy();

       var p = {}
       p.qname = qn.toString()
       p.name = qn.toString().split("}")[1];

       p.frequency = behaviour.getNotificationFrequency();

       var bobj = behaviour.getClass().getDeclaredField("instance");
       bobj.setAccessible(true);
       p.classname = bobj.get(behaviour).getClass().getName();

       var bmethod = behaviour.getClass().getDeclaredField("method");
       bmethod.setAccessible(true);
       p.method = bmethod.get(behaviour);

       // Index by the two different ways - class and policy
       if(behavioursByPolicy[p.qname] == null) {
          behavioursByPolicy[p.qname] = new Array();
       }
       behavioursByPolicy[p.qname].push(p);
       if(behavioursByClass[p.classname] == null) {
          behavioursByClass[p.classname] = new Array();
       }
       behavioursByClass[p.classname].push(p);
   }
}
model.behavioursByPolicy = behavioursByPolicy;
model.behavioursByClass = behavioursByClass;
