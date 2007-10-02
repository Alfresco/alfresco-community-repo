/*
Script: Core.js
	Mootools - My Object Oriented javascript.

License:
	MIT-style license.

MooTools Copyright:
	copyright (c) 2007 Valerio Proietti, <http://mad4milk.net>

MooTools Credits:
	- Class is slightly based on Base.js <http://dean.edwards.name/weblog/2006/03/base/> (c) 2006 Dean Edwards, License <http://creativecommons.org/licenses/LGPL/2.1/>
	- Some functions are inspired by those found in prototype.js <http://prototype.conio.net/> (c) 2005 Sam Stephenson sam [at] conio [dot] net, MIT-style license
	- Documentation by Aaron Newton (aaron.newton [at] cnet [dot] com) and Valerio Proietti.
*/

var MooTools = {
	version: '1.11'
};

/* Section: Core Functions */

/*
Function: $defined
	Returns true if the passed in value/object is defined, that means is not null or undefined.

Arguments:
	obj - object to inspect
*/

function $defined(obj){
	return (obj != undefined);
};

/*
Function: $type
	Returns the type of object that matches the element passed in.

Arguments:
	obj - the object to inspect.

Example:
	>var myString = 'hello';
	>$type(myString); //returns "string"

Returns:
	'element' - if obj is a DOM element node
	'textnode' - if obj is a DOM text node
	'whitespace' - if obj is a DOM whitespace node
	'arguments' - if obj is an arguments object
	'object' - if obj is an object
	'string' - if obj is a string
	'number' - if obj is a number
	'boolean' - if obj is a boolean
	'function' - if obj is a function
	'regexp' - if obj is a regular expression
	'class' - if obj is a Class. (created with new Class, or the extend of another class).
	'collection' - if obj is a native htmlelements collection, such as childNodes, getElementsByTagName .. etc.
	false - (boolean) if the object is not defined or none of the above.
*/

function $type(obj){
	if (!$defined(obj)) return false;
	if (obj.htmlElement) return 'element';
	var type = typeof obj;
	if (type == 'object' && obj.nodeName){
		switch(obj.nodeType){
			case 1: return 'element';
			case 3: return (/\S/).test(obj.nodeValue) ? 'textnode' : 'whitespace';
		}
	}
	if (type == 'object' || type == 'function'){
		switch(obj.constructor){
			case Array: return 'array';
			case RegExp: return 'regexp';
			case Class: return 'class';
		}
		if (typeof obj.length == 'number'){
			if (obj.item) return 'collection';
			if (obj.callee) return 'arguments';
		}
	}
	return type;
};

/*
Function: $merge
	merges a number of objects recursively without referencing them or their sub-objects.

Arguments:
	any number of objects.

Example:
	>var mergedObj = $merge(obj1, obj2, obj3);
	>//obj1, obj2, and obj3 are unaltered
*/

function $merge(){
	var mix = {};
	for (var i = 0; i < arguments.length; i++){
		for (var property in arguments[i]){
			var ap = arguments[i][property];
			var mp = mix[property];
			if (mp && $type(ap) == 'object' && $type(mp) == 'object') mix[property] = $merge(mp, ap);
			else mix[property] = ap;
		}
	}
	return mix;
};

/*
Function: $extend
	Copies all the properties from the second passed object to the first passed Object.
	If you do myWhatever.extend = $extend the first parameter will become myWhatever, and your extend function will only need one parameter.

Example:
	(start code)
	var firstOb = {
		'name': 'John',
		'lastName': 'Doe'
	};
	var secondOb = {
		'age': '20',
		'sex': 'male',
		'lastName': 'Dorian'
	};
	$extend(firstOb, secondOb);
	//firstOb will become:
	{
		'name': 'John',
		'lastName': 'Dorian',
		'age': '20',
		'sex': 'male'
	};
	(end)

Returns:
	The first object, extended.
*/

var $extend = function(){
	var args = arguments;
	if (!args[1]) args = [this, args[0]];
	for (var property in args[1]) args[0][property] = args[1][property];
	return args[0];
};

/*
Function: $native
	Will add a .extend method to the objects passed as a parameter, but the property passed in will be copied to the object's prototype only if non previously existent.
	Its handy if you dont want the .extend method of an object to overwrite existing methods.
	Used automatically in MooTools to implement Array/String/Function/Number methods to browser that dont support them whitout manual checking.

Arguments:
	a number of classes/native javascript objects

*/

var $native = function(){
	for (var i = 0, l = arguments.length; i < l; i++){
		arguments[i].extend = function(props){
			for (var prop in props){
				if (!this.prototype[prop]) this.prototype[prop] = props[prop];
				if (!this[prop]) this[prop] = $native.generic(prop);
			}
		};
	}
};

$native.generic = function(prop){
	return function(bind){
		return this.prototype[prop].apply(bind, Array.prototype.slice.call(arguments, 1));
	};
};

$native(Function, Array, String, Number);

/*
Function: $chk
	Returns true if the passed in value/object exists or is 0, otherwise returns false.
	Useful to accept zeroes.

Arguments:
	obj - object to inspect
*/

function $chk(obj){
	return !!(obj || obj === 0);
};

/*
Function: $pick
	Returns the first object if defined, otherwise returns the second.

Arguments:
	obj - object to test
	picked - the default to return

Example:
	(start code)
		function say(msg){
			alert($pick(msg, 'no meessage supplied'));
		}
	(end)
*/

function $pick(obj, picked){
	return $defined(obj) ? obj : picked;
};

/*
Function: $random
	Returns a random integer number between the two passed in values.

Arguments:
	min - integer, the minimum value (inclusive).
	max - integer, the maximum value (inclusive).

Returns:
	a random integer between min and max.
*/

function $random(min, max){
	return Math.floor(Math.random() * (max - min + 1) + min);
};

/*
Function: $time
	Returns the current timestamp

Returns:
	a timestamp integer.
*/

function $time(){
	return new Date().getTime();
};

/*
Function: $clear
	clears a timeout or an Interval.

Returns:
	null

Arguments:
	timer - the setInterval or setTimeout to clear.

Example:
	>var myTimer = myFunction.delay(5000); //wait 5 seconds and execute my function.
	>myTimer = $clear(myTimer); //nevermind

See also:
	<Function.delay>, <Function.periodical>
*/

function $clear(timer){
	clearTimeout(timer);
	clearInterval(timer);
	return null;
};

/*
Class: Abstract
	Abstract class, to be used as singleton. Will add .extend to any object

Arguments:
	an object

Returns:
	the object with an .extend property, equivalent to <$extend>.
*/

var Abstract = function(obj){
	obj = obj || {};
	obj.extend = $extend;
	return obj;
};

//window, document

var Window = new Abstract(window);
var Document = new Abstract(document);
document.head = document.getElementsByTagName('head')[0];

/*
Class: window
	Some properties are attached to the window object by the browser detection.
	
Note:
	browser detection is entirely object-based. We dont sniff.

Properties:
	window.ie - will be set to true if the current browser is internet explorer (any).
	window.ie6 - will be set to true if the current browser is internet explorer 6.
	window.ie7 - will be set to true if the current browser is internet explorer 7.
	window.gecko - will be set to true if the current browser is Mozilla/Gecko.
	window.webkit - will be set to true if the current browser is Safari/Konqueror.
	window.webkit419 - will be set to true if the current browser is Safari2 / webkit till version 419.
	window.webkit420 - will be set to true if the current browser is Safari3 (Webkit SVN Build) / webkit over version 419.
	window.opera - is set to true by opera itself.
*/

window.xpath = !!(document.evaluate);
if (window.ActiveXObject) window.ie = window[window.XMLHttpRequest ? 'ie7' : 'ie6'] = true;
else if (document.childNodes && !document.all && !navigator.taintEnabled) window.webkit = window[window.xpath ? 'webkit420' : 'webkit419'] = true;
else if (document.getBoxObjectFor != null) window.gecko = true;

/*compatibility*/

window.khtml = window.webkit;

Object.extend = $extend;

/*end compatibility*/

//htmlelement

if (typeof HTMLElement == 'undefined'){
	var HTMLElement = function(){};
	if (window.webkit) document.createElement("iframe"); //fixes safari
	HTMLElement.prototype = (window.webkit) ? window["[[DOMElement.prototype]]"] : {};
}
HTMLElement.prototype.htmlElement = function(){};

//enables background image cache for internet explorer 6

if (window.ie6) try {document.execCommand("BackgroundImageCache", false, true);} catch(e){};

/*
Script: Class.js
	Contains the Class Function, aims to ease the creation of reusable Classes.

License:
	MIT-style license.
*/

/*
Class: Class
	The base class object of the <http://mootools.net> framework.
	Creates a new class, its initialize method will fire upon class instantiation.
	Initialize wont fire on instantiation when you pass *null*.

Arguments:
	properties - the collection of properties that apply to the class.

Example:
	(start code)
	var Cat = new Class({
		initialize: function(name){
			this.name = name;
		}
	});
	var myCat = new Cat('Micia');
	alert(myCat.name); //alerts 'Micia'
	(end)
*/

var Class = function(properties){
	var klass = function(){
		return (arguments[0] !== null && this.initialize && $type(this.initialize) == 'function') ? this.initialize.apply(this, arguments) : this;
	};
	$extend(klass, this);
	klass.prototype = properties;
	klass.constructor = Class;
	return klass;
};

/*
Property: empty
	Returns an empty function
*/

Class.empty = function(){};

Class.prototype = {

	/*
	Property: extend
		Returns the copy of the Class extended with the passed in properties.

	Arguments:
		properties - the properties to add to the base class in this new Class.

	Example:
		(start code)
		var Animal = new Class({
			initialize: function(age){
				this.age = age;
			}
		});
		var Cat = Animal.extend({
			initialize: function(name, age){
				this.parent(age); //will call the previous initialize;
				this.name = name;
			}
		});
		var myCat = new Cat('Micia', 20);
		alert(myCat.name); //alerts 'Micia'
		alert(myCat.age); //alerts 20
		(end)
	*/

	extend: function(properties){
		var proto = new this(null);
		for (var property in properties){
			var pp = proto[property];
			proto[property] = Class.Merge(pp, properties[property]);
		}
		return new Class(proto);
	},

	/*
	Property: implement
		Implements the passed in properties to the base Class prototypes, altering the base class, unlike <Class.extend>.

	Arguments:
		properties - the properties to add to the base class.

	Example:
		(start code)
		var Animal = new Class({
			initialize: function(age){
				this.age = age;
			}
		});
		Animal.implement({
			setName: function(name){
				this.name = name
			}
		});
		var myAnimal = new Animal(20);
		myAnimal.setName('Micia');
		alert(myAnimal.name); //alerts 'Micia'
		(end)
	*/

	implement: function(){
		for (var i = 0, l = arguments.length; i < l; i++) $extend(this.prototype, arguments[i]);
	}

};

//internal

Class.Merge = function(previous, current){
	if (previous && previous != current){
		var type = $type(current);
		if (type != $type(previous)) return current;
		switch(type){
			case 'function':
				var merged = function(){
					this.parent = arguments.callee.parent;
					return current.apply(this, arguments);
				};
				merged.parent = previous;
				return merged;
			case 'object': return $merge(previous, current);
		}
	}
	return current;
};

/*
Script: Class.Extras.js
	Contains common implementations for custom classes. In Mootools is implemented in <Ajax>, <XHR> and <Fx.Base> and many more.

License:
	MIT-style license.
*/

/*
Class: Chain
	An "Utility" Class. Its methods can be implemented with <Class.implement> into any <Class>.
	Currently implemented in <Fx.Base>, <XHR> and <Ajax>. In <Fx.Base> for example, is used to execute a list of function, one after another, once the effect is completed.
	The functions will not be fired all togheter, but one every completion, to create custom complex animations.

Example:
	(start code)
	var myFx = new Fx.Style('element', 'opacity');

	myFx.start(1,0).chain(function(){
		myFx.start(0,1);
	}).chain(function(){
		myFx.start(1,0);
	}).chain(function(){
		myFx.start(0,1);
	});
	//the element will appear and disappear three times
	(end)
*/

var Chain = new Class({

	/*
	Property: chain
		adds a function to the Chain instance stack.

	Arguments:
		fn - the function to append.
	*/

	chain: function(fn){
		this.chains = this.chains || [];
		this.chains.push(fn);
		return this;
	},

	/*
	Property: callChain
		Executes the first function of the Chain instance stack, then removes it. The first function will then become the second.
	*/

	callChain: function(){
		if (this.chains && this.chains.length) this.chains.shift().delay(10, this);
	},

	/*
	Property: clearChain
		Clears the stack of a Chain instance.
	*/

	clearChain: function(){
		this.chains = [];
	}

});

/*
Class: Events
	An "Utility" Class. Its methods can be implemented with <Class.implement> into any <Class>.
	In <Fx.Base> Class, for example, is used to give the possibility add any number of functions to the Effects events, like onComplete, onStart, onCancel.
	Events in a Class that implements <Events> can be either added as an option, or with addEvent. Never with .options.onEventName.

Example:
	(start code)
	var myFx = new Fx.Style('element', 'opacity').addEvent('onComplete', function(){
		alert('the effect is completed');
	}).addEvent('onComplete', function(){
		alert('I told you the effect is completed');
	});

	myFx.start(0,1);
	//upon completion it will display the 2 alerts, in order.
	(end)

Implementing:
	This class can be implemented into other classes to add the functionality to them.
	Goes well with the <Options> class.

Example:
	(start code)
	var Widget = new Class({
		initialize: function(){},
		finish: function(){
			this.fireEvent('onComplete');
		}
	});
	Widget.implement(new Events);
	//later...
	var myWidget = new Widget();
	myWidget.addEvent('onComplete', myfunction);
	(end)
*/

var Events = new Class({

	/*
	Property: addEvent
		adds an event to the stack of events of the Class instance.

	Arguments:
		type - string; the event name (e.g. 'onComplete')
		fn - function to execute
	*/

	addEvent: function(type, fn){
		if (fn != Class.empty){
			this.$events = this.$events || {};
			this.$events[type] = this.$events[type] || [];
			this.$events[type].include(fn);
		}
		return this;
	},

	/*
	Property: fireEvent
		fires all events of the specified type in the Class instance.

	Arguments:
		type - string; the event name (e.g. 'onComplete')
		args - array or single object; arguments to pass to the function; if more than one argument, must be an array
		delay - (integer) delay (in ms) to wait to execute the event

	Example:
	(start code)
	var Widget = new Class({
		initialize: function(arg1, arg2){
			...
			this.fireEvent("onInitialize", [arg1, arg2], 50);
		}
	});
	Widget.implement(new Events);
	(end)
	*/

	fireEvent: function(type, args, delay){
		if (this.$events && this.$events[type]){
			this.$events[type].each(function(fn){
				fn.create({'bind': this, 'delay': delay, 'arguments': args})();
			}, this);
		}
		return this;
	},

	/*
	Property: removeEvent
		removes an event from the stack of events of the Class instance.

	Arguments:
		type - string; the event name (e.g. 'onComplete')
		fn - function that was added
	*/

	removeEvent: function(type, fn){
		if (this.$events && this.$events[type]) this.$events[type].remove(fn);
		return this;
	}

});

/*
Class: Options
	An "Utility" Class. Its methods can be implemented with <Class.implement> into any <Class>.
	Used to automate the options settings, also adding Class <Events> when the option begins with on.

	Example:
		(start code)
		var Widget = new Class({
			options: {
				color: '#fff',
				size: {
					width: 100
					height: 100
				}
			},
			initialize: function(options){
				this.setOptions(options);
			}
		});
		Widget.implement(new Options);
		//later...
		var myWidget = new Widget({
			color: '#f00',
			size: {
				width: 200
			}
		});
		//myWidget.options = {color: #f00, size: {width: 200, height: 100}}
		(end)
*/

var Options = new Class({

	/*
	Property: setOptions
		sets this.options

	Arguments:
		defaults - object; the default set of options
		options - object; the user entered options. can be empty too.

	Note:
		if your Class has <Events> implemented, every option beginning with on, followed by a capital letter (onComplete) becomes an Class instance event.
	*/

	setOptions: function(){
		this.options = $merge.apply(null, [this.options].extend(arguments));
		if (this.addEvent){
			for (var option in this.options){
				if ($type(this.options[option] == 'function') && (/^on[A-Z]/).test(option)) this.addEvent(option, this.options[option]);
			}
		}
		return this;
	}

});

/*
Script: Array.js
	Contains Array prototypes, <$A>, <$each>

License:
	MIT-style license.
*/

/*
Class: Array
	A collection of The Array Object prototype methods.
*/

//custom methods

Array.extend({

	/*
	Property: forEach
		Iterates through an array; This method is only available for browsers without native *forEach* support.
		For more info see <http://developer.mozilla.org/en/docs/Core_JavaScript_1.5_Reference:Global_Objects:Array:forEach>

		*forEach* executes the provided function (callback) once for each element present in the array. callback is invoked only for indexes of the array which have assigned values; it is not invoked for indexes which have been deleted or which have never been assigned values.

	Arguments:
		fn - function to execute with each item in the array; passed the item and the index of that item in the array
		bind - the object to bind "this" to (see <Function.bind>)

	Example:
		>['apple','banana','lemon'].each(function(item, index){
		>	alert(index + " = " + item); //alerts "0 = apple" etc.
		>}, bindObj); //optional second arg for binding, not used here
	*/

	forEach: function(fn, bind){
		for (var i = 0, j = this.length; i < j; i++) fn.call(bind, this[i], i, this);
	},

	/*
	Property: filter
		This method is provided only for browsers without native *filter* support.
		For more info see <http://developer.mozilla.org/en/docs/Core_JavaScript_1.5_Reference:Objects:Array:filter>

		*filter* calls a provided callback function once for each element in an array, and constructs a new array of all the values for which callback returns a true value. callback is invoked only for indexes of the array which have assigned values; it is not invoked for indexes which have been deleted or which have never been assigned values. Array elements which do not pass the callback test are simply skipped, and are not included in the new array.

	Arguments:
		fn - function to execute with each item in the array; passed the item and the index of that item in the array
		bind - the object to bind "this" to (see <Function.bind>)

	Example:
		>var biggerThanTwenty = [10,3,25,100].filter(function(item, index){
		> return item > 20;
		>});
		>//biggerThanTwenty = [25,100]
	*/

	filter: function(fn, bind){
		var results = [];
		for (var i = 0, j = this.length; i < j; i++){
			if (fn.call(bind, this[i], i, this)) results.push(this[i]);
		}
		return results;
	},

	/*
	Property: map
		This method is provided only for browsers without native *map* support.
		For more info see <http://developer.mozilla.org/en/docs/Core_JavaScript_1.5_Reference:Global_Objects:Array:map>

		*map* calls a provided callback function once for each element in an array, in order, and constructs a new array from the results. callback is invoked only for indexes of the array which have assigned values; it is not invoked for indexes which have been deleted or which have never been assigned values.

	Arguments:
		fn - function to execute with each item in the array; passed the item and the index of that item in the array
		bind - the object to bind "this" to (see <Function.bind>)

	Example:
		>var timesTwo = [1,2,3].map(function(item, index){
		> return item*2;
		>});
		>//timesTwo = [2,4,6];
	*/

	map: function(fn, bind){
		var results = [];
		for (var i = 0, j = this.length; i < j; i++) results[i] = fn.call(bind, this[i], i, this);
		return results;
	},

	/*
	Property: every
		This method is provided only for browsers without native *every* support.
		For more info see <http://developer.mozilla.org/en/docs/Core_JavaScript_1.5_Reference:Global_Objects:Array:every>

		*every* executes the provided callback function once for each element present in the array until it finds one where callback returns a false value. If such an element is found, the every method immediately returns false. Otherwise, if callback returned a true value for all elements, every will return true. callback is invoked only for indexes of the array which have assigned values; it is not invoked for indexes which have been deleted or which have never been assigned values.

	Arguments:
		fn - function to execute with each item in the array; passed the item and the index of that item in the array
		bind - the object to bind "this" to (see <Function.bind>)

	Example:
		>var areAllBigEnough = [10,4,25,100].every(function(item, index){
		> return item > 20;
		>});
		>//areAllBigEnough = false
	*/

	every: function(fn, bind){
		for (var i = 0, j = this.length; i < j; i++){
			if (!fn.call(bind, this[i], i, this)) return false;
		}
		return true;
	},

	/*
	Property: some
		This method is provided only for browsers without native *some* support.
		For more info see <http://developer.mozilla.org/en/docs/Core_JavaScript_1.5_Reference:Global_Objects:Array:some>

		*some* executes the callback function once for each element present in the array until it finds one where callback returns a true value. If such an element is found, some immediately returns true. Otherwise, some returns false. callback is invoked only for indexes of the array which have assigned values; it is not invoked for indexes which have been deleted or which have never been assigned values.

	Arguments:
		fn - function to execute with each item in the array; passed the item and the index of that item in the array
		bind - the object to bind "this" to (see <Function.bind>)

	Example:
		>var isAnyBigEnough = [10,4,25,100].some(function(item, index){
		> return item > 20;
		>});
		>//isAnyBigEnough = true
	*/

	some: function(fn, bind){
		for (var i = 0, j = this.length; i < j; i++){
			if (fn.call(bind, this[i], i, this)) return true;
		}
		return false;
	},

	/*
	Property: indexOf
		This method is provided only for browsers without native *indexOf* support.
		For more info see <http://developer.mozilla.org/en/docs/Core_JavaScript_1.5_Reference:Global_Objects:Array:indexOf>

		*indexOf* compares a search element to elements of the Array using strict equality (the same method used by the ===, or triple-equals, operator).

	Arguments:
		item - any type of object; element to locate in the array
		from - integer; optional; the index of the array at which to begin the search (defaults to 0)

	Example:
		>['apple','lemon','banana'].indexOf('lemon'); //returns 1
		>['apple','lemon'].indexOf('banana'); //returns -1
	*/

	indexOf: function(item, from){
		var len = this.length;
		for (var i = (from < 0) ? Math.max(0, len + from) : from || 0; i < len; i++){
			if (this[i] === item) return i;
		}
		return -1;
	},

	/*
	Property: each
		Same as <Array.forEach>.

	Arguments:
		fn - function to execute with each item in the array; passed the item and the index of that item in the array
		bind - optional, the object that the "this" of the function will refer to.

	Example:
		>var Animals = ['Cat', 'Dog', 'Coala'];
		>Animals.each(function(animal){
		>	document.write(animal)
		>});
	*/

	/*
	Property: copy
		returns a copy of the array.

	Returns:
		a new array which is a copy of the current one.

	Arguments:
		start - integer; optional; the index where to start the copy, default is 0. If negative, it is taken as the offset from the end of the array.
		length - integer; optional; the number of elements to copy. By default, copies all elements from start to the end of the array.

	Example:
		>var letters = ["a","b","c"];
		>var copy = letters.copy();		// ["a","b","c"] (new instance)
	*/

	copy: function(start, length){
		start = start || 0;
		if (start < 0) start = this.length + start;
		length = length || (this.length - start);
		var newArray = [];
		for (var i = 0; i < length; i++) newArray[i] = this[start++];
		return newArray;
	},

	/*
	Property: remove
		Removes all occurrences of an item from the array.

	Arguments:
		item - the item to remove

	Returns:
		the Array with all occurrences of the item removed.

	Example:
		>["1","2","3","2"].remove("2") // ["1","3"];
	*/

	remove: function(item){
		var i = 0;
		var len = this.length;
		while (i < len){
			if (this[i] === item){
				this.splice(i, 1);
				len--;
			} else {
				i++;
			}
		}
		return this;
	},

	/*
	Property: contains
		Tests an array for the presence of an item.

	Arguments:
		item - the item to search for in the array.
		from - integer; optional; the index at which to begin the search, default is 0. If negative, it is taken as the offset from the end of the array.

	Returns:
		true - the item was found
		false - it wasn't

	Example:
		>["a","b","c"].contains("a"); // true
		>["a","b","c"].contains("d"); // false
	*/

	contains: function(item, from){
		return this.indexOf(item, from) != -1;
	},

	/*
	Property: associate
		Creates an object with key-value pairs based on the array of keywords passed in
		and the current content of the array.

	Arguments:
		keys - the array of keywords.

	Example:
		(start code)
		var Animals = ['Cat', 'Dog', 'Coala', 'Lizard'];
		var Speech = ['Miao', 'Bau', 'Fruuu', 'Mute'];
		var Speeches = Animals.associate(Speech);
		//Speeches['Miao'] is now Cat.
		//Speeches['Bau'] is now Dog.
		//...
		(end)
	*/

	associate: function(keys){
		var obj = {}, length = Math.min(this.length, keys.length);
		for (var i = 0; i < length; i++) obj[keys[i]] = this[i];
		return obj;
	},

	/*
	Property: extend
		Extends an array with another one.

	Arguments:
		array - the array to extend ours with

	Example:
		>var Animals = ['Cat', 'Dog', 'Coala'];
		>Animals.extend(['Lizard']);
		>//Animals is now: ['Cat', 'Dog', 'Coala', 'Lizard'];
	*/

	extend: function(array){
		for (var i = 0, j = array.length; i < j; i++) this.push(array[i]);
		return this;
	},

	/*
	Property: merge
		merges an array in another array, without duplicates. (case- and type-sensitive)

	Arguments:
		array - the array to merge from.

	Example:
		>['Cat','Dog'].merge(['Dog','Coala']); //returns ['Cat','Dog','Coala']
	*/

	merge: function(array){
		for (var i = 0, l = array.length; i < l; i++) this.include(array[i]);
		return this;
	},

	/*
	Property: include
		includes the passed in element in the array, only if its not already present. (case- and type-sensitive)

	Arguments:
		item - item to add to the array (if not present)

	Example:
		>['Cat','Dog'].include('Dog'); //returns ['Cat','Dog']
		>['Cat','Dog'].include('Coala'); //returns ['Cat','Dog','Coala']
	*/

	include: function(item){
		if (!this.contains(item)) this.push(item);
		return this;
	},

	/*
	Property: getRandom
		returns a random item in the Array
	*/

	getRandom: function(){
		return this[$random(0, this.length - 1)] || null;
	},

	/*
	Property: getLast
		returns the last item in the Array
	*/

	getLast: function(){
		return this[this.length - 1] || null;
	}

});

//copies

Array.prototype.each = Array.prototype.forEach;
Array.each = Array.forEach;

/* Section: Utility Functions */

/*
Function: $A()
	Same as <Array.copy>, but as function.
	Useful to apply Array prototypes to iterable objects, as a collection of DOM elements or the arguments object.

Example:
	(start code)
	function myFunction(){
		$A(arguments).each(argument, function(){
			alert(argument);
		});
	};
	//the above will alert all the arguments passed to the function myFunction.
	(end)
*/

function $A(array){
	return Array.copy(array);
};

/*
Function: $each
	Use to iterate through iterables that are not regular arrays, such as builtin getElementsByTagName calls, arguments of a function, or an object.

Arguments:
	iterable - an iterable element or an objct.
	function - function to apply to the iterable.
	bind - optional, the 'this' of the function will refer to this object.

Function argument:
	The function argument will be passed the following arguments.

	item - the current item in the iterator being procesed
	index - integer; the index of the item, or key in case of an object.

Examples:
	(start code)
	$each(['Sun','Mon','Tue'], function(day, index){
		alert('name:' + day + ', index: ' + index);
	});
	//alerts "name: Sun, index: 0", "name: Mon, index: 1", etc.
	//over an object
	$each({first: "Sunday", second: "Monday", third: "Tuesday"}, function(value, key){
		alert("the " + key + " day of the week is " + value);
	});
	//alerts "the first day of the week is Sunday",
	//"the second day of the week is Monday", etc.
	(end)
*/

function $each(iterable, fn, bind){
	if (iterable && typeof iterable.length == 'number' && $type(iterable) != 'object'){
		Array.forEach(iterable, fn, bind);
	} else {
		 for (var name in iterable) fn.call(bind || iterable, iterable[name], name);
	}
};

/*compatibility*/

Array.prototype.test = Array.prototype.contains;

/*end compatibility*/

/*
Script: String.js
	Contains String prototypes.

License:
	MIT-style license.
*/

/*
Class: String
	A collection of The String Object prototype methods.
*/

String.extend({

	/*
	Property: test
		Tests a string with a regular expression.

	Arguments:
		regex - a string or regular expression object, the regular expression you want to match the string with
		params - optional, if first parameter is a string, any parameters you want to pass to the regex ('g' has no effect)

	Returns:
		true if a match for the regular expression is found in the string, false if not.
		See <http://developer.mozilla.org/en/docs/Core_JavaScript_1.5_Reference:Objects:RegExp:test>

	Example:
		>"I like cookies".test("cookie"); // returns true
		>"I like cookies".test("COOKIE", "i") // ignore case, returns true
		>"I like cookies".test("cake"); // returns false
	*/

	test: function(regex, params){
		return (($type(regex) == 'string') ? new RegExp(regex, params) : regex).test(this);
	},

	/*
	Property: toInt
		parses a string to an integer.

	Returns:
		either an int or "NaN" if the string is not a number.

	Example:
		>var value = "10px".toInt(); // value is 10
	*/

	toInt: function(){
		return parseInt(this, 10);
	},

	/*
	Property: toFloat
		parses a string to an float.

	Returns:
		either a float or "NaN" if the string is not a number.

	Example:
		>var value = "10.848".toFloat(); // value is 10.848
	*/

	toFloat: function(){
		return parseFloat(this);
	},

	/*
	Property: camelCase
		Converts a hiphenated string to a camelcase string.

	Example:
		>"I-like-cookies".camelCase(); //"ILikeCookies"

	Returns:
		the camel cased string
	*/

	camelCase: function(){
		return this.replace(/-\D/g, function(match){
			return match.charAt(1).toUpperCase();
		});
	},

	/*
	Property: hyphenate
		Converts a camelCased string to a hyphen-ated string.

	Example:
		>"ILikeCookies".hyphenate(); //"I-like-cookies"
	*/

	hyphenate: function(){
		return this.replace(/\w[A-Z]/g, function(match){
			return (match.charAt(0) + '-' + match.charAt(1).toLowerCase());
		});
	},

	/*
	Property: capitalize
		Converts the first letter in each word of a string to Uppercase.

	Example:
		>"i like cookies".capitalize(); //"I Like Cookies"

	Returns:
		the capitalized string
	*/

	capitalize: function(){
		return this.replace(/\b[a-z]/g, function(match){
			return match.toUpperCase();
		});
	},

	/*
	Property: trim
		Trims the leading and trailing spaces off a string.

	Example:
		>"    i like cookies     ".trim() //"i like cookies"

	Returns:
		the trimmed string
	*/

	trim: function(){
		return this.replace(/^\s+|\s+$/g, '');
	},

	/*
	Property: clean
		trims (<String.trim>) a string AND removes all the double spaces in a string.

	Returns:
		the cleaned string

	Example:
		>" i      like     cookies      \n\n".clean() //"i like cookies"
	*/

	clean: function(){
		return this.replace(/\s{2,}/g, ' ').trim();
	},

	/*
	Property: rgbToHex
		Converts an RGB value to hexidecimal. The string must be in the format of "rgb(255,255,255)" or "rgba(255,255,255,1)";

	Arguments:
		array - boolean value, defaults to false. Use true if you want the array ['FF','33','00'] as output instead of "#FF3300"

	Returns:
		hex string or array. returns "transparent" if the output is set as string and the fourth value of rgba in input string is 0.

	Example:
		>"rgb(17,34,51)".rgbToHex(); //"#112233"
		>"rgba(17,34,51,0)".rgbToHex(); //"transparent"
		>"rgb(17,34,51)".rgbToHex(true); //['11','22','33']
	*/

	rgbToHex: function(array){
		var rgb = this.match(/\d{1,3}/g);
		return (rgb) ? rgb.rgbToHex(array) : false;
	},

	/*
	Property: hexToRgb
		Converts a hexidecimal color value to RGB. Input string must be the hex color value (with or without the hash). Also accepts triplets ('333');

	Arguments:
		array - boolean value, defaults to false. Use true if you want the array [255,255,255] as output instead of "rgb(255,255,255)";

	Returns:
		rgb string or array.

	Example:
		>"#112233".hexToRgb(); //"rgb(17,34,51)"
		>"#112233".hexToRgb(true); //[17,34,51]
	*/

	hexToRgb: function(array){
		var hex = this.match(/^#?(\w{1,2})(\w{1,2})(\w{1,2})$/);
		return (hex) ? hex.slice(1).hexToRgb(array) : false;
	},

	/*
	Property: contains
		checks if the passed in string is contained in the String. also accepts an optional second parameter, to check if the string is contained in a list of separated values.

	Example:
		>'a b c'.contains('c', ' '); //true
		>'a bc'.contains('bc'); //true
		>'a bc'.contains('b', ' '); //false
	*/

	contains: function(string, s){
		return (s) ? (s + this + s).indexOf(s + string + s) > -1 : this.indexOf(string) > -1;
	},

	/*
	Property: escapeRegExp
		Returns string with escaped regular expression characters

	Example:
		>var search = 'animals.sheeps[1]'.escapeRegExp(); // search is now 'animals\.sheeps\[1\]'

	Returns:
		Escaped string
	*/

	escapeRegExp: function(){
		return this.replace(/([.*+?^${}()|[\]\/\\])/g, '\\$1');
	}

});

Array.extend({

	/*
	Property: rgbToHex
		see <String.rgbToHex>, but as an array method.
	*/

	rgbToHex: function(array){
		if (this.length < 3) return false;
		if (this.length == 4 && this[3] == 0 && !array) return 'transparent';
		var hex = [];
		for (var i = 0; i < 3; i++){
			var bit = (this[i] - 0).toString(16);
			hex.push((bit.length == 1) ? '0' + bit : bit);
		}
		return array ? hex : '#' + hex.join('');
	},

	/*
	Property: hexToRgb
		same as <String.hexToRgb>, but as an array method.
	*/

	hexToRgb: function(array){
		if (this.length != 3) return false;
		var rgb = [];
		for (var i = 0; i < 3; i++){
			rgb.push(parseInt((this[i].length == 1) ? this[i] + this[i] : this[i], 16));
		}
		return array ? rgb : 'rgb(' + rgb.join(',') + ')';
	}

});

/* 
Script: Function.js
	Contains Function prototypes and utility functions .

License:
	MIT-style license.

Credits:
	- Some functions are inspired by those found in prototype.js <http://prototype.conio.net/> (c) 2005 Sam Stephenson sam [at] conio [dot] net, MIT-style license
*/

/*
Class: Function
	A collection of The Function Object prototype methods.
*/

Function.extend({

	/*
	Property: create
		Main function to create closures.

	Returns:
		a function.

	Arguments:
		options - An Options object.

	Options:
		bind - The object that the "this" of the function will refer to. Default is the current function.
		event - If set to true, the function will act as an event listener and receive an event as first argument.
				If set to a class name, the function will receive a new instance of this class (with the event passed as argument's constructor) as first argument.
				Default is false.
		arguments - A single argument or array of arguments that will be passed to the function when called.
		
					If both the event and arguments options are set, the event is passed as first argument and the arguments array will follow.
					
					Default is no custom arguments, the function will receive the standard arguments when called.
					
		delay - Numeric value: if set, the returned function will delay the actual execution by this amount of milliseconds and return a timer handle when called.
				Default is no delay.
		periodical - Numeric value: if set, the returned function will periodically perform the actual execution with this specified interval and return a timer handle when called.
				Default is no periodical execution.
		attempt - If set to true, the returned function will try to execute and return either the results or false on error. Default is false.
	*/

	create: function(options){
		var fn = this;
		options = $merge({
			'bind': fn,
			'event': false,
			'arguments': null,
			'delay': false,
			'periodical': false,
			'attempt': false
		}, options);
		if ($chk(options.arguments) && $type(options.arguments) != 'array') options.arguments = [options.arguments];
		return function(event){
			var args;
			if (options.event){
				event = event || window.event;
				args = [(options.event === true) ? event : new options.event(event)];
				if (options.arguments) args.extend(options.arguments);
			}
			else args = options.arguments || arguments;
			var returns = function(){
				return fn.apply($pick(options.bind, fn), args);
			};
			if (options.delay) return setTimeout(returns, options.delay);
			if (options.periodical) return setInterval(returns, options.periodical);
			if (options.attempt) try {return returns();} catch(err){return false;};
			return returns();
		};
	},

	/*
	Property: pass
		Shortcut to create closures with arguments and bind.

	Returns:
		a function.

	Arguments:
		args - the arguments passed. must be an array if arguments > 1
		bind - optional, the object that the "this" of the function will refer to.

	Example:
		>myFunction.pass([arg1, arg2], myElement);
	*/

	pass: function(args, bind){
		return this.create({'arguments': args, 'bind': bind});
	},

	/*
	Property: attempt
		Tries to execute the function, returns either the result of the function or false on error.

	Arguments:
		args - the arguments passed. must be an array if arguments > 1
		bind - optional, the object that the "this" of the function will refer to.

	Example:
		>myFunction.attempt([arg1, arg2], myElement);
	*/

	attempt: function(args, bind){
		return this.create({'arguments': args, 'bind': bind, 'attempt': true})();
	},

	/*
	Property: bind
		method to easily create closures with "this" altered.

	Arguments:
		bind - optional, the object that the "this" of the function will refer to.
		args - optional, the arguments passed. must be an array if arguments > 1

	Returns:
		a function.

	Example:
		>function myFunction(){
		>	this.setStyle('color', 'red');
		>	// note that 'this' here refers to myFunction, not an element
		>	// we'll need to bind this function to the element we want to alter
		>};
		>var myBoundFunction = myFunction.bind(myElement);
		>myBoundFunction(); // this will make the element myElement red.
	*/

	bind: function(bind, args){
		return this.create({'bind': bind, 'arguments': args});
	},

	/*
	Property: bindAsEventListener
		cross browser method to pass event firer

	Arguments:
		bind - optional, the object that the "this" of the function will refer to.
		args - optional, the arguments passed. must be an array if arguments > 1

	Returns:
		a function with the parameter bind as its "this" and as a pre-passed argument event or window.event, depending on the browser.

	Example:
		>function myFunction(event){
		>	alert(event.clientx) //returns the coordinates of the mouse..
		>};
		>myElement.onclick = myFunction.bindAsEventListener(myElement);
	*/

	bindAsEventListener: function(bind, args){
		return this.create({'bind': bind, 'event': true, 'arguments': args});
	},

	/*
	Property: delay
		Delays the execution of a function by a specified duration.

	Arguments:
		delay - the duration to wait in milliseconds.
		bind - optional, the object that the "this" of the function will refer to.
		args - optional, the arguments passed. must be an array if arguments > 1

	Example:
		>myFunction.delay(50, myElement) //wait 50 milliseconds, then call myFunction and bind myElement to it
		>(function(){alert('one second later...')}).delay(1000); //wait a second and alert
	*/

	delay: function(delay, bind, args){
		return this.create({'delay': delay, 'bind': bind, 'arguments': args})();
	},

	/*
	Property: periodical
		Executes a function in the specified intervals of time

	Arguments:
		interval - the duration of the intervals between executions.
		bind - optional, the object that the "this" of the function will refer to.
		args - optional, the arguments passed. must be an array if arguments > 1
	*/

	periodical: function(interval, bind, args){
		return this.create({'periodical': interval, 'bind': bind, 'arguments': args})();
	}

});

/*
Script: Number.js
	Contains the Number prototypes.

License:
	MIT-style license.
*/

/*
Class: Number
	A collection of The Number Object prototype methods.
*/

Number.extend({

	/*
	Property: toInt
		Returns this number; useful because toInt must work on both Strings and Numbers.
	*/

	toInt: function(){
		return parseInt(this);
	},

	/*
	Property: toFloat
		Returns this number as a float; useful because toFloat must work on both Strings and Numbers.
	*/

	toFloat: function(){
		return parseFloat(this);
	},

	/*
	Property: limit
		Limits the number.

	Arguments:
		min - number, minimum value
		max - number, maximum value

	Returns:
		the number in the given limits.

	Example:
		>(12).limit(2, 6.5)  // returns 6.5
		>(-4).limit(2, 6.5)  // returns 2
		>(4.3).limit(2, 6.5) // returns 4.3
	*/

	limit: function(min, max){
		return Math.min(max, Math.max(min, this));
	},

	/*
	Property: round
		Returns the number rounded to specified precision.

	Arguments:
		precision - integer, number of digits after the decimal point. Can also be negative or zero (default).

	Example:
		>12.45.round() // returns 12
		>12.45.round(1) // returns 12.5
		>12.45.round(-1) // returns 10

	Returns:
		The rounded number.
	*/

	round: function(precision){
		precision = Math.pow(10, precision || 0);
		return Math.round(this * precision) / precision;
	},

	/*
	Property: times
		Executes a passed in function the specified number of times

	Arguments:
		function - the function to be executed on each iteration of the loop

	Example:
		>(4).times(alert);
	*/

	times: function(fn){
		for (var i = 0; i < this; i++) fn(i);
	}

});

/*
Script: Element.js
	Contains useful Element prototypes, to be used with the dollar function <$>.

License:
	MIT-style license.

Credits:
	- Some functions are inspired by those found in prototype.js <http://prototype.conio.net/> (c) 2005 Sam Stephenson sam [at] conio [dot] net, MIT-style license
*/

/*
Class: Element
	Custom class to allow all of its methods to be used with any DOM element via the dollar function <$>.
*/

var Element = new Class({

	/*
	Property: initialize
		Creates a new element of the type passed in.

	Arguments:
		el - string; the tag name for the element you wish to create. you can also pass in an element reference, in which case it will be extended.
		props - object; the properties you want to add to your element.
		Accepts the same keys as <Element.setProperties>, but also allows events and styles

	Props:
		the key styles will be used as setStyles, the key events will be used as addEvents. any other key is used as setProperty.

	Example:
		(start code)
		new Element('a', {
			'styles': {
				'display': 'block',
				'border': '1px solid black'
			},
			'events': {
				'click': function(){
					//aaa
				},
				'mousedown': function(){
					//aaa
				}
			},
			'class': 'myClassSuperClass',
			'href': 'http://mad4milk.net'
		});

		(end)
	*/

	initialize: function(el, props){
		if ($type(el) == 'string'){
			if (window.ie && props && (props.name || props.type)){
				var name = (props.name) ? ' name="' + props.name + '"' : '';
				var type = (props.type) ? ' type="' + props.type + '"' : '';
				delete props.name;
				delete props.type;
				el = '<' + el + name + type + '>';
			}
			el = document.createElement(el);
		}
		el = $(el);
		return (!props || !el) ? el : el.set(props);
	}

});

/*
Class: Elements
	- Every dom function such as <$$>, or in general every function that returns a collection of nodes in mootools, returns them as an Elements class.
	- The purpose of the Elements class is to allow <Element> methods to work also on <Elements> array.
	- Elements is also an Array, so it accepts all the <Array> methods.
	- Every node of the Elements instance is already extended with <$>.

Example:
	>$$('myselector').each(function(el){
	> //...
	>});

	some iterations here, $$('myselector') is also an array.

	>$$('myselector').setStyle('color', 'red');
	every element returned by $$('myselector') also accepts <Element> methods, in this example every element will be made red.
*/

var Elements = new Class({

	initialize: function(elements){
		return (elements) ? $extend(elements, this) : this;
	}

});

Elements.extend = function(props){
	for (var prop in props){
		this.prototype[prop] = props[prop];
		this[prop] = $native.generic(prop);
	}
};

/*
Section: Utility Functions

Function: $
	returns the element passed in with all the Element prototypes applied.

Arguments:
	el - a reference to an actual element or a string representing the id of an element

Example:
	>$('myElement') // gets a DOM element by id with all the Element prototypes applied.
	>var div = document.getElementById('myElement');
	>$(div) //returns an Element also with all the mootools extentions applied.

	You'll use this when you aren't sure if a variable is an actual element or an id, as
	well as just shorthand for document.getElementById().

Returns:
	a DOM element or false (if no id was found).

Note:
	you need to call $ on an element only once to get all the prototypes.
	But its no harm to call it multiple times, as it will detect if it has been already extended.
*/

function $(el){
	if (!el) return null;
	if (el.htmlElement) return Garbage.collect(el);
	if ([window, document].contains(el)) return el;
	var type = $type(el);
	if (type == 'string'){
		el = document.getElementById(el);
		type = (el) ? 'element' : false;
	}
	if (type != 'element') return null;
	if (el.htmlElement) return Garbage.collect(el);
	if (['object', 'embed'].contains(el.tagName.toLowerCase())) return el;
	$extend(el, Element.prototype);
	el.htmlElement = function(){};
	return Garbage.collect(el);
};

/*
Function: $$
	Selects, and extends DOM elements. Elements arrays returned with $$ will also accept all the <Element> methods.
	The return type of element methods run through $$ is always an array. If the return array is only made by elements,
	$$ will be applied automatically.

Arguments:
	HTML Collections, arrays of elements, arrays of strings as element ids, elements, strings as selectors.
	Any number of the above as arguments are accepted.

Note:
	if you load <Element.Selectors.js>, $$ will also accept CSS Selectors, otherwise the only selectors supported are tag names.

Example:
	>$$('a') //an array of all anchor tags on the page
	>$$('a', 'b') //an array of all anchor and bold tags on the page
	>$$('#myElement') //array containing only the element with id = myElement. (only with <Element.Selectors.js>)
	>$$('#myElement a.myClass') //an array of all anchor tags with the class "myClass"
	>//within the DOM element with id "myElement" (only with <Element.Selectors.js>)
	>$$(myelement, myelement2, 'a', ['myid', myid2, 'myid3'], document.getElementsByTagName('div')) //an array containing:
	>// the element referenced as myelement if existing,
	>// the element referenced as myelement2 if existing,
	>// all the elements with a as tag in the page,
	>// the element with id = myid if existing
	>// the element with id = myid2 if existing
	>// the element with id = myid3 if existing
	>// all the elements with div as tag in the page

Returns:
	array - array of all the dom elements matched, extended with <$>.  Returns as <Elements>.
*/

document.getElementsBySelector = document.getElementsByTagName;

function $$(){
	var elements = [];
	for (var i = 0, j = arguments.length; i < j; i++){
		var selector = arguments[i];
		switch($type(selector)){
			case 'element': elements.push(selector);
			case 'boolean': break;
			case false: break;
			case 'string': selector = document.getElementsBySelector(selector, true);
			default: elements.extend(selector);
		}
	}
	return $$.unique(elements);
};

$$.unique = function(array){
	var elements = [];
	for (var i = 0, l = array.length; i < l; i++){
		if (array[i].$included) continue;
		var element = $(array[i]);
		if (element && !element.$included){
			element.$included = true;
			elements.push(element);
		}
	}
	for (var n = 0, d = elements.length; n < d; n++) elements[n].$included = null;
	return new Elements(elements);
};

Elements.Multi = function(property){
	return function(){
		var args = arguments;
		var items = [];
		var elements = true;
		for (var i = 0, j = this.length, returns; i < j; i++){
			returns = this[i][property].apply(this[i], args);
			if ($type(returns) != 'element') elements = false;
			items.push(returns);
		};
		return (elements) ? $$.unique(items) : items;
	};
};

Element.extend = function(properties){
	for (var property in properties){
		HTMLElement.prototype[property] = properties[property];
		Element.prototype[property] = properties[property];
		Element[property] = $native.generic(property);
		var elementsProperty = (Array.prototype[property]) ? property + 'Elements' : property;
		Elements.prototype[elementsProperty] = Elements.Multi(property);
	}
};

/*
Class: Element
	Custom class to allow all of its methods to be used with any DOM element via the dollar function <$>.
*/

Element.extend({

	/*
	Property: set
		you can set events, styles and properties with this shortcut. same as calling new Element.
	*/

	set: function(props){
		for (var prop in props){
			var val = props[prop];
			switch(prop){
				case 'styles': this.setStyles(val); break;
				case 'events': if (this.addEvents) this.addEvents(val); break;
				case 'properties': this.setProperties(val); break;
				default: this.setProperty(prop, val);
			}
		}
		return this;
	},

	inject: function(el, where){
		el = $(el);
		switch(where){
			case 'before': el.parentNode.insertBefore(this, el); break;
			case 'after':
				var next = el.getNext();
				if (!next) el.parentNode.appendChild(this);
				else el.parentNode.insertBefore(this, next);
				break;
			case 'top':
				var first = el.firstChild;
				if (first){
					el.insertBefore(this, first);
					break;
				}
			default: el.appendChild(this);
		}
		return this;
	},

	/*
	Property: injectBefore
		Inserts the Element before the passed element.

	Arguments:
		el - an element reference or the id of the element to be injected in.

	Example:
		>html:
		><div id="myElement"></div>
		><div id="mySecondElement"></div>
		>js:
		>$('mySecondElement').injectBefore('myElement');
		>resulting html:
		><div id="mySecondElement"></div>
		><div id="myElement"></div>
	*/

	injectBefore: function(el){
		return this.inject(el, 'before');
	},

	/*
	Property: injectAfter
		Same as <Element.injectBefore>, but inserts the element after.
	*/

	injectAfter: function(el){
		return this.inject(el, 'after');
	},

	/*
	Property: injectInside
		Same as <Element.injectBefore>, but inserts the element inside.
	*/

	injectInside: function(el){
		return this.inject(el, 'bottom');
	},

	/*
	Property: injectTop
		Same as <Element.injectInside>, but inserts the element inside, at the top.
	*/

	injectTop: function(el){
		return this.inject(el, 'top');
	},

	/*
	Property: adopt
		Inserts the passed elements inside the Element.

	Arguments:
		accepts elements references, element ids as string, selectors ($$('stuff')) / array of elements, array of ids as strings and collections.
	*/

	adopt: function(){
		var elements = [];
		$each(arguments, function(argument){
			elements = elements.concat(argument);
		});
		$$(elements).inject(this);
		return this;
	},

	/*
	Property: remove
		Removes the Element from the DOM.

	Example:
		>$('myElement').remove() //bye bye
	*/

	remove: function(){
		return this.parentNode.removeChild(this);
	},

	/*
	Property: clone
		Clones the Element and returns the cloned one.

	Arguments:
		contents - boolean, when true the Element is cloned with childNodes, default true

	Returns:
		the cloned element

	Example:
		>var clone = $('myElement').clone().injectAfter('myElement');
		>//clones the Element and append the clone after the Element.
	*/

	clone: function(contents){
		var el = $(this.cloneNode(contents !== false));
		if (!el.$events) return el;
		el.$events = {};
		for (var type in this.$events) el.$events[type] = {
			'keys': $A(this.$events[type].keys),
			'values': $A(this.$events[type].values)
		};
		return el.removeEvents();
	},

	/*
	Property: replaceWith
		Replaces the Element with an element passed.

	Arguments:
		el - a string representing the element to be injected in (myElementId, or div), or an element reference.
		If you pass div or another tag, the element will be created.

	Returns:
		the passed in element

	Example:
		>$('myOldElement').replaceWith($('myNewElement')); //$('myOldElement') is gone, and $('myNewElement') is in its place.
	*/

	replaceWith: function(el){
		el = $(el);
		this.parentNode.replaceChild(el, this);
		return el;
	},

	/*
	Property: appendText
		Appends text node to a DOM element.

	Arguments:
		text - the text to append.

	Example:
		><div id="myElement">hey</div>
		>$('myElement').appendText(' howdy'); //myElement innerHTML is now "hey howdy"
	*/

	appendText: function(text){
		this.appendChild(document.createTextNode(text));
		return this;
	},

	/*
	Property: hasClass
		Tests the Element to see if it has the passed in className.

	Returns:
		true - the Element has the class
		false - it doesn't

	Arguments:
		className - string; the class name to test.

	Example:
		><div id="myElement" class="testClass"></div>
		>$('myElement').hasClass('testClass'); //returns true
	*/

	hasClass: function(className){
		return this.className.contains(className, ' ');
	},

	/*
	Property: addClass
		Adds the passed in class to the Element, if the element doesnt already have it.

	Arguments:
		className - string; the class name to add

	Example:
		><div id="myElement" class="testClass"></div>
		>$('myElement').addClass('newClass'); //<div id="myElement" class="testClass newClass"></div>
	*/

	addClass: function(className){
		if (!this.hasClass(className)) this.className = (this.className + ' ' + className).clean();
		return this;
	},

	/*
	Property: removeClass
		Works like <Element.addClass>, but removes the class from the element.
	*/

	removeClass: function(className){
		this.className = this.className.replace(new RegExp('(^|\\s)' + className + '(?:\\s|$)'), '$1').clean();
		return this;
	},

	/*
	Property: toggleClass
		Adds or removes the passed in class name to the element, depending on if it's present or not.

	Arguments:
		className - the class to add or remove

	Example:
		><div id="myElement" class="myClass"></div>
		>$('myElement').toggleClass('myClass');
		><div id="myElement" class=""></div>
		>$('myElement').toggleClass('myClass');
		><div id="myElement" class="myClass"></div>
	*/

	toggleClass: function(className){
		return this.hasClass(className) ? this.removeClass(className) : this.addClass(className);
	},

	/*
	Property: setStyle
		Sets a css property to the Element.

		Arguments:
			property - the property to set
			value - the value to which to set it; for numeric values that require "px" you can pass an integer

		Example:
			>$('myElement').setStyle('width', '300px'); //the width is now 300px
			>$('myElement').setStyle('width', 300); //the width is now 300px
	*/

	setStyle: function(property, value){
		switch(property){
			case 'opacity': return this.setOpacity(parseFloat(value));
			case 'float': property = (window.ie) ? 'styleFloat' : 'cssFloat';
		}
		property = property.camelCase();
		switch($type(value)){
			case 'number': if (!['zIndex', 'zoom'].contains(property)) value += 'px'; break;
			case 'array': value = 'rgb(' + value.join(',') + ')';
		}
		this.style[property] = value;
		return this;
	},

	/*
	Property: setStyles
		Applies a collection of styles to the Element.

	Arguments:
		source - an object or string containing all the styles to apply. When its a string it overrides old style.

	Examples:
		>$('myElement').setStyles({
		>	border: '1px solid #000',
		>	width: 300,
		>	height: 400
		>});

		OR

		>$('myElement').setStyles('border: 1px solid #000; width: 300px; height: 400px;');
	*/

	setStyles: function(source){
		switch($type(source)){
			case 'object': Element.setMany(this, 'setStyle', source); break;
			case 'string': this.style.cssText = source;
		}
		return this;
	},

	/*
	Property: setOpacity
		Sets the opacity of the Element, and sets also visibility == "hidden" if opacity == 0, and visibility = "visible" if opacity > 0.

	Arguments:
		opacity - float; Accepts values from 0 to 1.

	Example:
		>$('myElement').setOpacity(0.5) //make it 50% transparent
	*/

	setOpacity: function(opacity){
		if (opacity == 0){
			if (this.style.visibility != "hidden") this.style.visibility = "hidden";
		} else {
			if (this.style.visibility != "visible") this.style.visibility = "visible";
		}
		if (!this.currentStyle || !this.currentStyle.hasLayout) this.style.zoom = 1;
		if (window.ie) this.style.filter = (opacity == 1) ? '' : "alpha(opacity=" + opacity * 100 + ")";
		this.style.opacity = this.$tmp.opacity = opacity;
		return this;
	},

	/*
	Property: getStyle
		Returns the style of the Element given the property passed in.

	Arguments:
		property - the css style property you want to retrieve

	Example:
		>$('myElement').getStyle('width'); //returns "400px"
		>//but you can also use
		>$('myElement').getStyle('width').toInt(); //returns 400

	Returns:
		the style as a string
	*/

	getStyle: function(property){
		property = property.camelCase();
		var result = this.style[property];
		if (!$chk(result)){
			if (property == 'opacity') return this.$tmp.opacity;
			result = [];
			for (var style in Element.Styles){
				if (property == style){
					Element.Styles[style].each(function(s){
						var style = this.getStyle(s);
						result.push(parseInt(style) ? style : '0px');
					}, this);
					if (property == 'border'){
						var every = result.every(function(bit){
							return (bit == result[0]);
						});
						return (every) ? result[0] : false;
					}
					return result.join(' ');
				}
			}
			if (property.contains('border')){
				if (Element.Styles.border.contains(property)){
					return ['Width', 'Style', 'Color'].map(function(p){
						return this.getStyle(property + p);
					}, this).join(' ');
				} else if (Element.borderShort.contains(property)){
					return ['Top', 'Right', 'Bottom', 'Left'].map(function(p){
						return this.getStyle('border' + p + property.replace('border', ''));
					}, this).join(' ');
				}
			}
			if (document.defaultView) result = document.defaultView.getComputedStyle(this, null).getPropertyValue(property.hyphenate());
			else if (this.currentStyle) result = this.currentStyle[property];
		}
		if (window.ie) result = Element.fixStyle(property, result, this);
		if (result && property.test(/color/i) && result.contains('rgb')){
			return result.split('rgb').splice(1,4).map(function(color){
				return color.rgbToHex();
			}).join(' ');
		}
		return result;
	},

	/*
	Property: getStyles
		Returns an object of styles of the Element for each argument passed in.
		Arguments:
		properties - strings; any number of style properties
	Example:
		>$('myElement').getStyles('width','height','padding');
		>//returns an object like:
		>{width: "10px", height: "10px", padding: "10px 0px 10px 0px"}
	*/

	getStyles: function(){
		return Element.getMany(this, 'getStyle', arguments);
	},

	walk: function(brother, start){
		brother += 'Sibling';
		var el = (start) ? this[start] : this[brother];
		while (el && $type(el) != 'element') el = el[brother];
		return $(el);
	},

	/*
	Property: getPrevious
		Returns the previousSibling of the Element, excluding text nodes.

	Example:
		>$('myElement').getPrevious(); //get the previous DOM element from myElement

	Returns:
		the sibling element or undefined if none found.
	*/

	getPrevious: function(){
		return this.walk('previous');
	},

	/*
	Property: getNext
		Works as Element.getPrevious, but tries to find the nextSibling.
	*/

	getNext: function(){
		return this.walk('next');
	},

	/*
	Property: getFirst
		Works as <Element.getPrevious>, but tries to find the firstChild.
	*/

	getFirst: function(){
		return this.walk('next', 'firstChild');
	},

	/*
	Property: getLast
		Works as <Element.getPrevious>, but tries to find the lastChild.
	*/

	getLast: function(){
		return this.walk('previous', 'lastChild');
	},

	/*
	Property: getParent
		returns the $(element.parentNode)
	*/

	getParent: function(){
		return $(this.parentNode);
	},

	/*
	Property: getChildren
		returns all the $(element.childNodes), excluding text nodes. Returns as <Elements>.
	*/

	getChildren: function(){
		return $$(this.childNodes);
	},

	/*
	Property: hasChild
		returns true if the passed in element is a child of the $(element).
	*/

	hasChild: function(el){
		return !!$A(this.getElementsByTagName('*')).contains(el);
	},

	/*
	Property: getProperty
		Gets the an attribute of the Element.

	Arguments:
		property - string; the attribute to retrieve

	Example:
		>$('myImage').getProperty('src') // returns whatever.gif

	Returns:
		the value, or an empty string
	*/

	getProperty: function(property){
		var index = Element.Properties[property];
		if (index) return this[index];
		var flag = Element.PropertiesIFlag[property] || 0;
		if (!window.ie || flag) return this.getAttribute(property, flag);
		var node = this.attributes[property];
		return (node) ? node.nodeValue : null;
	},

	/*
	Property: removeProperty
		Removes an attribute from the Element

	Arguments:
		property - string; the attribute to remove
	*/

	removeProperty: function(property){
		var index = Element.Properties[property];
		if (index) this[index] = '';
		else this.removeAttribute(property);
		return this;
	},

	/*
	Property: getProperties
		same as <Element.getStyles>, but for properties
	*/

	getProperties: function(){
		return Element.getMany(this, 'getProperty', arguments);
	},

	/*
	Property: setProperty
		Sets an attribute for the Element.

	Arguments:
		property - string; the property to assign the value passed in
		value - the value to assign to the property passed in

	Example:
		>$('myImage').setProperty('src', 'whatever.gif'); //myImage now points to whatever.gif for its source
	*/

	setProperty: function(property, value){
		var index = Element.Properties[property];
		if (index) this[index] = value;
		else this.setAttribute(property, value);
		return this;
	},

	/*
	Property: setProperties
		Sets numerous attributes for the Element.

	Arguments:
		source - an object with key/value pairs.

	Example:
		(start code)
		$('myElement').setProperties({
			src: 'whatever.gif',
			alt: 'whatever dude'
		});
		<img src="whatever.gif" alt="whatever dude">
		(end)
	*/

	setProperties: function(source){
		return Element.setMany(this, 'setProperty', source);
	},

	/*
	Property: setHTML
		Sets the innerHTML of the Element.

	Arguments:
		html - string; the new innerHTML for the element.

	Example:
		>$('myElement').setHTML(newHTML) //the innerHTML of myElement is now = newHTML
	*/

	setHTML: function(){
		this.innerHTML = $A(arguments).join('');
		return this;
	},

	/*
	Property: setText
		Sets the inner text of the Element.

	Arguments:
		text - string; the new text content for the element.

	Example:
		>$('myElement').setText('some text') //the text of myElement is now = 'some text'
	*/

	setText: function(text){
		var tag = this.getTag();
		if (['style', 'script'].contains(tag)){
			if (window.ie){
				if (tag == 'style') this.styleSheet.cssText = text;
				else if (tag ==  'script') this.setProperty('text', text);
				return this;
			} else {
				this.removeChild(this.firstChild);
				return this.appendText(text);
			}
		}
		this[$defined(this.innerText) ? 'innerText' : 'textContent'] = text;
		return this;
	},

	/*
	Property: getText
		Gets the inner text of the Element.
	*/

	getText: function(){
		var tag = this.getTag();
		if (['style', 'script'].contains(tag)){
			if (window.ie){
				if (tag == 'style') return this.styleSheet.cssText;
				else if (tag ==  'script') return this.getProperty('text');
			} else {
				return this.innerHTML;
			}
		}
		return ($pick(this.innerText, this.textContent));
	},

	/*
	Property: getTag
		Returns the tagName of the element in lower case.

	Example:
		>$('myImage').getTag() // returns 'img'

	Returns:
		The tag name in lower case
	*/

	getTag: function(){
		return this.tagName.toLowerCase();
	},

	/*
	Property: empty
		Empties an element of all its children.

	Example:
		>$('myDiv').empty() // empties the Div and returns it

	Returns:
		The element.
	*/

	empty: function(){
		Garbage.trash(this.getElementsByTagName('*'));
		return this.setHTML('');
	}

});

Element.fixStyle = function(property, result, element){
	if ($chk(parseInt(result))) return result;
	if (['height', 'width'].contains(property)){
		var values = (property == 'width') ? ['left', 'right'] : ['top', 'bottom'];
		var size = 0;
		values.each(function(value){
			size += element.getStyle('border-' + value + '-width').toInt() + element.getStyle('padding-' + value).toInt();
		});
		return element['offset' + property.capitalize()] - size + 'px';
	} else if (property.test(/border(.+)Width|margin|padding/)){
		return '0px';
	}
	return result;
};

Element.Styles = {'border': [], 'padding': [], 'margin': []};
['Top', 'Right', 'Bottom', 'Left'].each(function(direction){
	for (var style in Element.Styles) Element.Styles[style].push(style + direction);
});

Element.borderShort = ['borderWidth', 'borderStyle', 'borderColor'];

Element.getMany = function(el, method, keys){
	var result = {};
	$each(keys, function(key){
		result[key] = el[method](key);
	});
	return result;
};

Element.setMany = function(el, method, pairs){
	for (var key in pairs) el[method](key, pairs[key]);
	return el;
};

Element.Properties = new Abstract({
	'class': 'className', 'for': 'htmlFor', 'colspan': 'colSpan', 'rowspan': 'rowSpan',
	'accesskey': 'accessKey', 'tabindex': 'tabIndex', 'maxlength': 'maxLength',
	'readonly': 'readOnly', 'frameborder': 'frameBorder', 'value': 'value',
	'disabled': 'disabled', 'checked': 'checked', 'multiple': 'multiple', 'selected': 'selected'
});
Element.PropertiesIFlag = {
	'href': 2, 'src': 2
};

Element.Methods = {
	Listeners: {
		addListener: function(type, fn){
			if (this.addEventListener) this.addEventListener(type, fn, false);
			else this.attachEvent('on' + type, fn);
			return this;
		},

		removeListener: function(type, fn){
			if (this.removeEventListener) this.removeEventListener(type, fn, false);
			else this.detachEvent('on' + type, fn);
			return this;
		}
	}
};

window.extend(Element.Methods.Listeners);
document.extend(Element.Methods.Listeners);
Element.extend(Element.Methods.Listeners);

var Garbage = {

	elements: [],

	collect: function(el){
		if (!el.$tmp){
			Garbage.elements.push(el);
			el.$tmp = {'opacity': 1};
		}
		return el;
	},

	trash: function(elements){
		for (var i = 0, j = elements.length, el; i < j; i++){
			if (!(el = elements[i]) || !el.$tmp) continue;
			if (el.$events) el.fireEvent('trash').removeEvents();
			for (var p in el.$tmp) el.$tmp[p] = null;
			for (var d in Element.prototype) el[d] = null;
			Garbage.elements[Garbage.elements.indexOf(el)] = null;
			el.htmlElement = el.$tmp = el = null;
		}
		Garbage.elements.remove(null);
	},

	empty: function(){
		Garbage.collect(window);
		Garbage.collect(document);
		Garbage.trash(Garbage.elements);
	}

};

window.addListener('beforeunload', function(){
	window.addListener('unload', Garbage.empty);
	if (window.ie) window.addListener('unload', CollectGarbage);
});

/*
Script: Element.Event.js
	Contains the Event Class, Element methods to deal with Element events, custom Events, and the Function prototype bindWithEvent.

License:
	MIT-style license.
*/

/*
Class: Event
	Cross browser methods to manage events.

Arguments:
	event - the event

Properties:
	shift - true if the user pressed the shift
	control - true if the user pressed the control
	alt - true if the user pressed the alt
	meta - true if the user pressed the meta key
	wheel - the amount of third button scrolling
	code - the keycode of the key pressed
	page.x - the x position of the mouse, relative to the full window
	page.y - the y position of the mouse, relative to the full window
	client.x - the x position of the mouse, relative to the viewport
	client.y - the y position of the mouse, relative to the viewport
	key - the key pressed as a lowercase string. key also returns 'enter', 'up', 'down', 'left', 'right', 'space', 'backspace', 'delete', 'esc'. Handy for these special keys.
	target - the event target
	relatedTarget - the event related target

Example:
	(start code)
	$('myLink').onkeydown = function(event){
		var event = new Event(event);
		//event is now the Event class.
		alert(event.key); //returns the lowercase letter pressed
		alert(event.shift); //returns true if the key pressed is shift
		if (event.key == 's' && event.control) alert('document saved');
	};
	(end)
*/

var Event = new Class({

	initialize: function(event){
		if (event && event.$extended) return event;
		this.$extended = true;
		event = event || window.event;
		this.event = event;
		this.type = event.type;
		this.target = event.target || event.srcElement;
		if (this.target.nodeType == 3) this.target = this.target.parentNode;
		this.shift = event.shiftKey;
		this.control = event.ctrlKey;
		this.alt = event.altKey;
		this.meta = event.metaKey;
		if (['DOMMouseScroll', 'mousewheel'].contains(this.type)){
			this.wheel = (event.wheelDelta) ? event.wheelDelta / 120 : -(event.detail || 0) / 3;
		} else if (this.type.contains('key')){
			this.code = event.which || event.keyCode;
			for (var name in Event.keys){
				if (Event.keys[name] == this.code){
					this.key = name;
					break;
				}
			}
			if (this.type == 'keydown'){
				var fKey = this.code - 111;
				if (fKey > 0 && fKey < 13) this.key = 'f' + fKey;
			}
			this.key = this.key || String.fromCharCode(this.code).toLowerCase();
		} else if (this.type.test(/(click|mouse|menu)/)){
			this.page = {
				'x': event.pageX || event.clientX + document.documentElement.scrollLeft,
				'y': event.pageY || event.clientY + document.documentElement.scrollTop
			};
			this.client = {
				'x': event.pageX ? event.pageX - window.pageXOffset : event.clientX,
				'y': event.pageY ? event.pageY - window.pageYOffset : event.clientY
			};
			this.rightClick = (event.which == 3) || (event.button == 2);
			switch(this.type){
				case 'mouseover': this.relatedTarget = event.relatedTarget || event.fromElement; break;
				case 'mouseout': this.relatedTarget = event.relatedTarget || event.toElement;
			}
			this.fixRelatedTarget();
		}
		return this;
	},

	/*
	Property: stop
		cross browser method to stop an event
	*/

	stop: function(){
		return this.stopPropagation().preventDefault();
	},

	/*
	Property: stopPropagation
		cross browser method to stop the propagation of an event
	*/

	stopPropagation: function(){
		if (this.event.stopPropagation) this.event.stopPropagation();
		else this.event.cancelBubble = true;
		return this;
	},

	/*
	Property: preventDefault
		cross browser method to prevent the default action of the event
	*/

	preventDefault: function(){
		if (this.event.preventDefault) this.event.preventDefault();
		else this.event.returnValue = false;
		return this;
	}

});

Event.fix = {

	relatedTarget: function(){
		if (this.relatedTarget && this.relatedTarget.nodeType == 3) this.relatedTarget = this.relatedTarget.parentNode;
	},

	relatedTargetGecko: function(){
		try {Event.fix.relatedTarget.call(this);} catch(e){this.relatedTarget = this.target;}
	}

};

Event.prototype.fixRelatedTarget = (window.gecko) ? Event.fix.relatedTargetGecko : Event.fix.relatedTarget;

/*
Property: keys
	you can add additional Event keys codes this way:

Example:
	(start code)
	Event.keys.whatever = 80;
	$(myelement).addEvent(keydown, function(event){
		event = new Event(event);
		if (event.key == 'whatever') console.log(whatever key clicked).
	});
	(end)
*/

Event.keys = new Abstract({
	'enter': 13,
	'up': 38,
	'down': 40,
	'left': 37,
	'right': 39,
	'esc': 27,
	'space': 32,
	'backspace': 8,
	'tab': 9,
	'delete': 46
});

/*
Class: Element
	Custom class to allow all of its methods to be used with any DOM element via the dollar function <$>.
*/

Element.Methods.Events = {

	/*
	Property: addEvent
		Attaches an event listener to a DOM element.

	Arguments:
		type - the event to monitor ('click', 'load', etc) without the prefix 'on'.
		fn - the function to execute

	Example:
		>$('myElement').addEvent('click', function(){alert('clicked!')});
	*/

	addEvent: function(type, fn){
		this.$events = this.$events || {};
		this.$events[type] = this.$events[type] || {'keys': [], 'values': []};
		if (this.$events[type].keys.contains(fn)) return this;
		this.$events[type].keys.push(fn);
		var realType = type;
		var custom = Element.Events[type];
		if (custom){
			if (custom.add) custom.add.call(this, fn);
			if (custom.map) fn = custom.map;
			if (custom.type) realType = custom.type;
		}
		if (!this.addEventListener) fn = fn.create({'bind': this, 'event': true});
		this.$events[type].values.push(fn);
		return (Element.NativeEvents.contains(realType)) ? this.addListener(realType, fn) : this;
	},

	/*
	Property: removeEvent
		Works as Element.addEvent, but instead removes the previously added event listener.
	*/

	removeEvent: function(type, fn){
		if (!this.$events || !this.$events[type]) return this;
		var pos = this.$events[type].keys.indexOf(fn);
		if (pos == -1) return this;
		var key = this.$events[type].keys.splice(pos,1)[0];
		var value = this.$events[type].values.splice(pos,1)[0];
		var custom = Element.Events[type];
		if (custom){
			if (custom.remove) custom.remove.call(this, fn);
			if (custom.type) type = custom.type;
		}
		return (Element.NativeEvents.contains(type)) ? this.removeListener(type, value) : this;
	},

	/*
	Property: addEvents
		As <addEvent>, but accepts an object and add multiple events at once.
	*/

	addEvents: function(source){
		return Element.setMany(this, 'addEvent', source);
	},

	/*
	Property: removeEvents
		removes all events of a certain type from an element. if no argument is passed in, removes all events.

	Arguments:
		type - string; the event name (e.g. 'click')
	*/

	removeEvents: function(type){
		if (!this.$events) return this;
		if (!type){
			for (var evType in this.$events) this.removeEvents(evType);
			this.$events = null;
		} else if (this.$events[type]){
			this.$events[type].keys.each(function(fn){
				this.removeEvent(type, fn);
			}, this);
			this.$events[type] = null;
		}
		return this;
	},

	/*
	Property: fireEvent
		executes all events of the specified type present in the element.

	Arguments:
		type - string; the event name (e.g. 'click')
		args - array or single object; arguments to pass to the function; if more than one argument, must be an array
		delay - (integer) delay (in ms) to wait to execute the event
	*/

	fireEvent: function(type, args, delay){
		if (this.$events && this.$events[type]){
			this.$events[type].keys.each(function(fn){
				fn.create({'bind': this, 'delay': delay, 'arguments': args})();
			}, this);
		}
		return this;
	},

	/*
	Property: cloneEvents
		Clones all events from an element to this element.

	Arguments:
		from - element, copy all events from this element
		type - optional, copies only events of this type
	*/

	cloneEvents: function(from, type){
		if (!from.$events) return this;
		if (!type){
			for (var evType in from.$events) this.cloneEvents(from, evType);
		} else if (from.$events[type]){
			from.$events[type].keys.each(function(fn){
				this.addEvent(type, fn);
			}, this);
		}
		return this;
	}

};

window.extend(Element.Methods.Events);
document.extend(Element.Methods.Events);
Element.extend(Element.Methods.Events);

/* Section: Custom Events */

Element.Events = new Abstract({

	/*
	Event: mouseenter
		In addition to the standard javascript events (load, mouseover, mouseout, click, etc.) <Event.js> contains two custom events
		this event fires when the mouse enters the area of the dom element; will not be fired again if the mouse crosses over children of the element (unlike mouseover)


	Example:
		>$(myElement).addEvent('mouseenter', myFunction);
	*/

	'mouseenter': {
		type: 'mouseover',
		map: function(event){
			event = new Event(event);
			if (event.relatedTarget != this && !this.hasChild(event.relatedTarget)) this.fireEvent('mouseenter', event);
		}
	},

	/*
	Event: mouseleave
		this event fires when the mouse exits the area of the dom element; will not be fired again if the mouse crosses over children of the element (unlike mouseout)


	Example:
		>$(myElement).addEvent('mouseleave', myFunction);
	*/

	'mouseleave': {
		type: 'mouseout',
		map: function(event){
			event = new Event(event);
			if (event.relatedTarget != this && !this.hasChild(event.relatedTarget)) this.fireEvent('mouseleave', event);
		}
	},

	'mousewheel': {
		type: (window.gecko) ? 'DOMMouseScroll' : 'mousewheel'
	}

});

Element.NativeEvents = [
	'click', 'dblclick', 'mouseup', 'mousedown', //mouse buttons
	'mousewheel', 'DOMMouseScroll', //mouse wheel
	'mouseover', 'mouseout', 'mousemove', //mouse movement
	'keydown', 'keypress', 'keyup', //keys
	'load', 'unload', 'beforeunload', 'resize', 'move', //window
	'focus', 'blur', 'change', 'submit', 'reset', 'select', //forms elements
	'error', 'abort', 'contextmenu', 'scroll' //misc
];

/*
Class: Function
	A collection of The Function Object prototype methods.
*/

Function.extend({

	/*
	Property: bindWithEvent
		automatically passes MooTools Event Class.

	Arguments:
		bind - optional, the object that the "this" of the function will refer to.
		args - optional, an argument to pass to the function; if more than one argument, it must be an array of arguments.

	Returns:
		a function with the parameter bind as its "this" and as a pre-passed argument event or window.event, depending on the browser.

	Example:
		>function myFunction(event){
		>	alert(event.client.x) //returns the coordinates of the mouse..
		>};
		>myElement.addEvent('click', myFunction.bindWithEvent(myElement));
	*/

	bindWithEvent: function(bind, args){
		return this.create({'bind': bind, 'arguments': args, 'event': Event});
	}

});


/*
Script: Element.Filters.js
	add Filters capability to <Elements>.

License:
	MIT-style license.
*/

/*
Class: Elements
	A collection of methods to be used with <$$> elements collections.
*/

Elements.extend({
	
	/*
	Property: filterByTag
		Filters the collection by a specified tag name.
		Returns a new Elements collection, while the original remains untouched.
	*/
	
	filterByTag: function(tag){
		return new Elements(this.filter(function(el){
			return (Element.getTag(el) == tag);
		}));
	},
	
	/*
	Property: filterByClass
		Filters the collection by a specified class name.
		Returns a new Elements collection, while the original remains untouched.
	*/
	
	filterByClass: function(className, nocash){
		var elements = this.filter(function(el){
			return (el.className && el.className.contains(className, ' '));
		});
		return (nocash) ? elements : new Elements(elements);
	},
	
	/*
	Property: filterById
		Filters the collection by a specified ID.
		Returns a new Elements collection, while the original remains untouched.
	*/
	
	filterById: function(id, nocash){
		var elements = this.filter(function(el){
			return (el.id == id);
		});
		return (nocash) ? elements : new Elements(elements);
	},
	
	/*
	Property: filterByAttribute
		Filters the collection by a specified attribute.
		Returns a new Elements collection, while the original remains untouched.
		
	Arguments:
		name - the attribute name.
		operator - optional, the attribute operator.
		value - optional, the attribute value, only valid if the operator is specified.
	*/
	
	filterByAttribute: function(name, operator, value, nocash){
		var elements = this.filter(function(el){
			var current = Element.getProperty(el, name);
			if (!current) return false;
			if (!operator) return true;
			switch(operator){
				case '=': return (current == value);
				case '*=': return (current.contains(value));
				case '^=': return (current.substr(0, value.length) == value);
				case '$=': return (current.substr(current.length - value.length) == value);
				case '!=': return (current != value);
				case '~=': return current.contains(value, ' ');
			}
			return false;
		});
		return (nocash) ? elements : new Elements(elements);
	}

});

/*
Script: Element.Selectors.js
	Css Query related functions and <Element> extensions

License:
	MIT-style license.
*/

/* Section: Utility Functions */

/*
Function: $E
	Selects a single (i.e. the first found) Element based on the selector passed in and an optional filter element.
	Returns as <Element>.

Arguments:
	selector - string; the css selector to match
	filter - optional; a DOM element to limit the scope of the selector match; defaults to document.

Example:
	>$E('a', 'myElement') //find the first anchor tag inside the DOM element with id 'myElement'

Returns:
	a DOM element - the first element that matches the selector
*/

function $E(selector, filter){
	return ($(filter) || document).getElement(selector);
};

/*
Function: $ES
	Returns a collection of Elements that match the selector passed in limited to the scope of the optional filter.
	See Also: <Element.getElements> for an alternate syntax.
	Returns as <Elements>.

Returns:
	an array of dom elements that match the selector within the filter

Arguments:
	selector - string; css selector to match
	filter - optional; a DOM element to limit the scope of the selector match; defaults to document.

Examples:
	>$ES("a") //gets all the anchor tags; synonymous with $$("a")
	>$ES('a','myElement') //get all the anchor tags within $('myElement')
*/

function $ES(selector, filter){
	return ($(filter) || document).getElementsBySelector(selector);
};

$$.shared = {

	'regexp': /^(\w*|\*)(?:#([\w-]+)|\.([\w-]+))?(?:\[(\w+)(?:([!*^$]?=)["']?([^"'\]]*)["']?)?])?$/,
	
	'xpath': {

		getParam: function(items, context, param, i){
			var temp = [context.namespaceURI ? 'xhtml:' : '', param[1]];
			if (param[2]) temp.push('[@id="', param[2], '"]');
			if (param[3]) temp.push('[contains(concat(" ", @class, " "), " ', param[3], ' ")]');
			if (param[4]){
				if (param[5] && param[6]){
					switch(param[5]){
						case '*=': temp.push('[contains(@', param[4], ', "', param[6], '")]'); break;
						case '^=': temp.push('[starts-with(@', param[4], ', "', param[6], '")]'); break;
						case '$=': temp.push('[substring(@', param[4], ', string-length(@', param[4], ') - ', param[6].length, ' + 1) = "', param[6], '"]'); break;
						case '=': temp.push('[@', param[4], '="', param[6], '"]'); break;
						case '!=': temp.push('[@', param[4], '!="', param[6], '"]');
					}
				} else {
					temp.push('[@', param[4], ']');
				}
			}
			items.push(temp.join(''));
			return items;
		},
		
		getItems: function(items, context, nocash){
			var elements = [];
			var xpath = document.evaluate('.//' + items.join('//'), context, $$.shared.resolver, XPathResult.UNORDERED_NODE_SNAPSHOT_TYPE, null);
			for (var i = 0, j = xpath.snapshotLength; i < j; i++) elements.push(xpath.snapshotItem(i));
			return (nocash) ? elements : new Elements(elements.map($));
		}

	},
	
	'normal': {
		
		getParam: function(items, context, param, i){
			if (i == 0){
				if (param[2]){
					var el = context.getElementById(param[2]);
					if (!el || ((param[1] != '*') && (Element.getTag(el) != param[1]))) return false;
					items = [el];
				} else {
					items = $A(context.getElementsByTagName(param[1]));
				}
			} else {
				items = $$.shared.getElementsByTagName(items, param[1]);
				if (param[2]) items = Elements.filterById(items, param[2], true);
			}
			if (param[3]) items = Elements.filterByClass(items, param[3], true);
			if (param[4]) items = Elements.filterByAttribute(items, param[4], param[5], param[6], true);
			return items;
		},

		getItems: function(items, context, nocash){
			return (nocash) ? items : $$.unique(items);
		}

	},

	resolver: function(prefix){
		return (prefix == 'xhtml') ? 'http://www.w3.org/1999/xhtml' : false;
	},

	getElementsByTagName: function(context, tagName){
		var found = [];
		for (var i = 0, j = context.length; i < j; i++) found.extend(context[i].getElementsByTagName(tagName));
		return found;
	}

};

$$.shared.method = (window.xpath) ? 'xpath' : 'normal';

/*
Class: Element
	Custom class to allow all of its methods to be used with any DOM element via the dollar function <$>.
*/

Element.Methods.Dom = {

	/*
	Property: getElements
		Gets all the elements within an element that match the given (single) selector.
		Returns as <Elements>.

	Arguments:
		selector - string; the css selector to match

	Examples:
		>$('myElement').getElements('a'); // get all anchors within myElement
		>$('myElement').getElements('input[name=dialog]') //get all input tags with name 'dialog'
		>$('myElement').getElements('input[name$=log]') //get all input tags with names ending with 'log'

	Notes:
		Supports these operators in attribute selectors:

		- = : is equal to
		- ^= : starts-with
		- $= : ends-with
		- != : is not equal to

		Xpath is used automatically for compliant browsers.
	*/

	getElements: function(selector, nocash){
		var items = [];
		selector = selector.trim().split(' ');
		for (var i = 0, j = selector.length; i < j; i++){
			var sel = selector[i];
			var param = sel.match($$.shared.regexp);
			if (!param) break;
			param[1] = param[1] || '*';
			var temp = $$.shared[$$.shared.method].getParam(items, this, param, i);
			if (!temp) break;
			items = temp;
		}
		return $$.shared[$$.shared.method].getItems(items, this, nocash);
	},

	/*
	Property: getElement
		Same as <Element.getElements>, but returns only the first. Alternate syntax for <$E>, where filter is the Element.
		Returns as <Element>.

	Arguments:
		selector - string; css selector
	*/

	getElement: function(selector){
		return $(this.getElements(selector, true)[0] || false);
	},

	/*
	Property: getElementsBySelector
		Same as <Element.getElements>, but allows for comma separated selectors, as in css. Alternate syntax for <$$>, where filter is the Element.
		Returns as <Elements>.

	Arguments:
		selector - string; css selector
	*/

	getElementsBySelector: function(selector, nocash){
		var elements = [];
		selector = selector.split(',');
		for (var i = 0, j = selector.length; i < j; i++) elements = elements.concat(this.getElements(selector[i], true));
		return (nocash) ? elements : $$.unique(elements);
	}

};

Element.extend({

	/*
	Property: getElementById
		Targets an element with the specified id found inside the Element. Does not overwrite document.getElementById.

	Arguments:
		id - string; the id of the element to find.
	*/

	getElementById: function(id){
		var el = document.getElementById(id);
		if (!el) return false;
		for (var parent = el.parentNode; parent != this; parent = parent.parentNode){
			if (!parent) return false;
		}
		return el;
	}/*compatibility*/,
	
	getElementsByClassName: function(className){ 
		return this.getElements('.' + className); 
	}
	
	/*end compatibility*/

});

document.extend(Element.Methods.Dom);
Element.extend(Element.Methods.Dom);

/*
Script: Window.DomReady.js
	Contains the custom event domready, for window.

License:
	MIT-style license.
*/

/* Section: Custom Events */

/*
Event: domready
	executes a function when the dom tree is loaded, without waiting for images. Only works when called from window.

Credits:
	(c) Dean Edwards/Matthias Miller/John Resig, remastered for MooTools.

Arguments:
	fn - the function to execute when the DOM is ready

Example:
	> window.addEvent('domready', function(){
	>	alert('the dom is ready');
	> });
*/

Element.Events.domready = {

	add: function(fn){
		if (window.loaded){
			fn.call(this);
			return;
		}
		var domReady = function(){
			if (window.loaded) return;
			window.loaded = true;
			window.timer = $clear(window.timer);
			this.fireEvent('domready');
		}.bind(this);
		if (document.readyState && window.webkit){
			window.timer = function(){
				if (['loaded','complete'].contains(document.readyState)) domReady();
			}.periodical(50);
		} else if (document.readyState && window.ie){
			if (!$('ie_ready')){
				var src = (window.location.protocol == 'https:') ? '://0' : 'javascript:void(0)';
				document.write('<script id="ie_ready" defer src="' + src + '"><\/script>');
				$('ie_ready').onreadystatechange = function(){
					if (this.readyState == 'complete') domReady();
				};
			}
		} else {
			window.addListener("load", domReady);
			document.addListener("DOMContentLoaded", domReady);
		}
	}

};

/*compatibility*/

window.onDomReady = function(fn){ 
	return this.addEvent('domready', fn); 
};

/*end compatibility*/

/*
Script: Fx.Base.js
	Contains <Fx.Base>, the foundamentals of the MooTools Effects.

License:
	MIT-style license.
*/

var Fx = {};

/*
Class: Fx.Base
	Base class for the Effects.

Options:
	transition - the equation to use for the effect see <Fx.Transitions>; default is <Fx.Transitions.Sine.easeInOut>
	duration - the duration of the effect in ms; 500 is the default.
	unit - the unit is 'px' by default (other values include things like 'em' for fonts or '%').
	wait - boolean: to wait or not to wait for a current transition to end before running another of the same instance. defaults to true.
	fps - the frames per second for the transition; default is 50
	
Events:
	onStart - the function to execute as the effect begins; nothing (<Class.empty>) by default.
	onComplete - the function to execute after the effect has processed; nothing (<Class.empty>) by default.
	onCancel - the function to execute when you manually stop the effect.
*/

Fx.Base = new Class({

	options: {
		onStart: Class.empty,
		onComplete: Class.empty,
		onCancel: Class.empty,
		transition: function(p){
			return -(Math.cos(Math.PI * p) - 1) / 2;
		},
		duration: 500,
		unit: 'px',
		wait: true,
		fps: 50
	},

	initialize: function(options){
		this.element = this.element || null;
		this.setOptions(options);
		if (this.options.initialize) this.options.initialize.call(this);
	},

	step: function(){
		var time = $time();
		if (time < this.time + this.options.duration){
			this.delta = this.options.transition((time - this.time) / this.options.duration);
			this.setNow();
			this.increase();
		} else {
			this.stop(true);
			this.set(this.to);
			this.fireEvent('onComplete', this.element, 10);
			this.callChain();
		}
	},

	/*
	Property: set
		Immediately sets the value with no transition.

	Arguments:
		to - the point to jump to

	Example:
		>var myFx = new Fx.Style('myElement', 'opacity').set(0); //will make it immediately transparent
	*/

	set: function(to){
		this.now = to;
		this.increase();
		return this;
	},

	setNow: function(){
		this.now = this.compute(this.from, this.to);
	},

	compute: function(from, to){
		return (to - from) * this.delta + from;
	},

	/*
	Property: start
		Executes an effect from one position to the other.

	Arguments:
		from - integer: staring value
		to - integer: the ending value

	Examples:
		>var myFx = new Fx.Style('myElement', 'opacity').start(0,1); //display a transition from transparent to opaque.
	*/

	start: function(from, to){
		if (!this.options.wait) this.stop();
		else if (this.timer) return this;
		this.from = from;
		this.to = to;
		this.change = this.to - this.from;
		this.time = $time();
		this.timer = this.step.periodical(Math.round(1000 / this.options.fps), this);
		this.fireEvent('onStart', this.element);
		return this;
	},

	/*
	Property: stop
		Stops the transition.
	*/

	stop: function(end){
		if (!this.timer) return this;
		this.timer = $clear(this.timer);
		if (!end) this.fireEvent('onCancel', this.element);
		return this;
	}/*compatibility*/,
	
	custom: function(from, to){
		return this.start(from, to);
	},

	clearTimer: function(end){
		return this.stop(end);
	}

	/*end compatibility*/

});

Fx.Base.implement(new Chain, new Events, new Options);

/*
Script: Fx.CSS.js
	Css parsing class for effects. Required by <Fx.Style>, <Fx.Styles>, <Fx.Elements>. No documentation needed, as its used internally.

License:
	MIT-style license.
*/

Fx.CSS = {

	select: function(property, to){
		if (property.test(/color/i)) return this.Color;
		var type = $type(to);
		if ((type == 'array') || (type == 'string' && to.contains(' '))) return this.Multi;
		return this.Single;
	},

	parse: function(el, property, fromTo){
		if (!fromTo.push) fromTo = [fromTo];
		var from = fromTo[0], to = fromTo[1];
		if (!$chk(to)){
			to = from;
			from = el.getStyle(property);
		}
		var css = this.select(property, to);
		return {'from': css.parse(from), 'to': css.parse(to), 'css': css};
	}

};

Fx.CSS.Single = {

	parse: function(value){
		return parseFloat(value);
	},

	getNow: function(from, to, fx){
		return fx.compute(from, to);
	},

	getValue: function(value, unit, property){
		if (unit == 'px' && property != 'opacity') value = Math.round(value);
		return value + unit;
	}

};

Fx.CSS.Multi = {

	parse: function(value){
		return value.push ? value : value.split(' ').map(function(v){
			return parseFloat(v);
		});
	},

	getNow: function(from, to, fx){
		var now = [];
		for (var i = 0; i < from.length; i++) now[i] = fx.compute(from[i], to[i]);
		return now;
	},

	getValue: function(value, unit, property){
		if (unit == 'px' && property != 'opacity') value = value.map(Math.round);
		return value.join(unit + ' ') + unit;
	}

};

Fx.CSS.Color = {

	parse: function(value){
		return value.push ? value : value.hexToRgb(true);
	},

	getNow: function(from, to, fx){
		var now = [];
		for (var i = 0; i < from.length; i++) now[i] = Math.round(fx.compute(from[i], to[i]));
		return now;
	},

	getValue: function(value){
		return 'rgb(' + value.join(',') + ')';
	}

};

/*
Script: Fx.Style.js
	Contains <Fx.Style>

License:
	MIT-style license.
*/

/*
Class: Fx.Style
	The Style effect, used to transition any css property from one value to another. Includes colors.
	Colors must be in hex format.
	Inherits methods, properties, options and events from <Fx.Base>.

Arguments:
	el - the $(element) to apply the style transition to
	property - the property to transition
	options - the Fx.Base options (see: <Fx.Base>)

Example:
	>var marginChange = new Fx.Style('myElement', 'margin-top', {duration:500});
	>marginChange.start(10, 100);
*/

Fx.Style = Fx.Base.extend({

	initialize: function(el, property, options){
		this.element = $(el);
		this.property = property;
		this.parent(options);
	},

	/*
	Property: hide
		Same as <Fx.Base.set> (0); hides the element immediately without transition.
	*/

	hide: function(){
		return this.set(0);
	},

	setNow: function(){
		this.now = this.css.getNow(this.from, this.to, this);
	},

	/*
	Property: set
		Sets the element's css property (specified at instantiation) to the specified value immediately.

	Example:
		(start code)
		var marginChange = new Fx.Style('myElement', 'margin-top', {duration:500});
		marginChange.set(10); //margin-top is set to 10px immediately
		(end)
	*/

	set: function(to){
		this.css = Fx.CSS.select(this.property, to);
		return this.parent(this.css.parse(to));
	},

	/*
	Property: start
		Displays the transition to the value/values passed in

	Arguments:
		from - (integer; optional) the starting position for the transition
		to - (integer) the ending position for the transition

	Note:
		If you provide only one argument, the transition will use the current css value for its starting value.

	Example:
		(start code)
		var marginChange = new Fx.Style('myElement', 'margin-top', {duration:500});
		marginChange.start(10); //tries to read current margin top value and goes from current to 10
		(end)
	*/

	start: function(from, to){
		if (this.timer && this.options.wait) return this;
		var parsed = Fx.CSS.parse(this.element, this.property, [from, to]);
		this.css = parsed.css;
		return this.parent(parsed.from, parsed.to);
	},

	increase: function(){
		this.element.setStyle(this.property, this.css.getValue(this.now, this.options.unit, this.property));
	}

});

/*
Class: Element
	Custom class to allow all of its methods to be used with any DOM element via the dollar function <$>.
*/

Element.extend({

	/*
	Property: effect
		Applies an <Fx.Style> to the Element; This a shortcut for <Fx.Style>.

	Arguments:
		property - (string) the css property to alter
		options - (object; optional) key/value set of options (see <Fx.Style>)

	Example:
		>var myEffect = $('myElement').effect('height', {duration: 1000, transition: Fx.Transitions.linear});
		>myEffect.start(10, 100);
		>//OR
		>$('myElement').effect('height', {duration: 1000, transition: Fx.Transitions.linear}).start(10,100);
	*/

	effect: function(property, options){
		return new Fx.Style(this, property, options);
	}

});

/*
Script: Fx.Elements.js
	Contains <Fx.Elements>

License:
	MIT-style license.
*/

/*
Class: Fx.Elements
	Fx.Elements allows you to apply any number of styles transitions to a selection of elements. Includes colors (must be in hex format).
	Inherits methods, properties, options and events from <Fx.Base>.

Arguments:
	elements - a collection of elements the effects will be applied to.
	options - same as <Fx.Base> options.
*/

Fx.Elements = Fx.Base.extend({

	initialize: function(elements, options){
		this.elements = $$(elements);
		this.parent(options);
	},

	setNow: function(){
		for (var i in this.from){
			var iFrom = this.from[i], iTo = this.to[i], iCss = this.css[i], iNow = this.now[i] = {};
			for (var p in iFrom) iNow[p] = iCss[p].getNow(iFrom[p], iTo[p], this);
		}
	},

	set: function(to){
		var parsed = {};
		this.css = {};
		for (var i in to){
			var iTo = to[i], iCss = this.css[i] = {}, iParsed = parsed[i] = {};
			for (var p in iTo){
				iCss[p] = Fx.CSS.select(p, iTo[p]);
				iParsed[p] = iCss[p].parse(iTo[p]);
			}
		}
		return this.parent(parsed);
	},

	/*
	Property: start
		Applies the passed in style transitions to each object named (see example). Each item in the collection is refered to as a numerical string ("1" for instance). The first item is "0", the second "1", etc.

	Example:
		(start code)
		var myElementsEffects = new Fx.Elements($$('a'));
		myElementsEffects.start({
			'0': { //let's change the first element's opacity and width
				'opacity': [0,1],
				'width': [100,200]
			},
			'4': { //and the fifth one's opacity
				'opacity': [0.2, 0.5]
			}
		});
		(end)
	*/

	start: function(obj){
		if (this.timer && this.options.wait) return this;
		this.now = {};
		this.css = {};
		var from = {}, to = {};
		for (var i in obj){
			var iProps = obj[i], iFrom = from[i] = {}, iTo = to[i] = {}, iCss = this.css[i] = {};
			for (var p in iProps){
				var parsed = Fx.CSS.parse(this.elements[i], p, iProps[p]);
				iFrom[p] = parsed.from;
				iTo[p] = parsed.to;
				iCss[p] = parsed.css;
			}
		}
		return this.parent(from, to);
	},

	increase: function(){
		for (var i in this.now){
			var iNow = this.now[i], iCss = this.css[i];
			for (var p in iNow) this.elements[i].setStyle(p, iCss[p].getValue(iNow[p], this.options.unit, p));
		}
	}

});

/*
Script: Fx.Slide.js
	Contains <Fx.Slide>

License:
	MIT-style license.
*/

/*
Class: Fx.Slide
	The slide effect; slides an element in horizontally or vertically, the contents will fold inside.
	Inherits methods, properties, options and events from <Fx.Base>.
	
Note:
	Fx.Slide requires an XHTML doctype.

Options:
	mode - set it to vertical or horizontal. Defaults to vertical.
	options - all the <Fx.Base> options

Example:
	(start code)
	var mySlider = new Fx.Slide('myElement', {duration: 500});
	mySlider.toggle() //toggle the slider up and down.
	(end)
*/

Fx.Slide = Fx.Base.extend({

	options: {
		mode: 'vertical'
	},

	initialize: function(el, options){
		this.element = $(el);
		this.wrapper = new Element('div', {'styles': $extend(this.element.getStyles('margin'), {'overflow': 'hidden'})}).injectAfter(this.element).adopt(this.element);
		this.element.setStyle('margin', 0);
		this.setOptions(options);
		this.now = [];
		this.parent(this.options);
		this.open = true;
		this.addEvent('onComplete', function(){
			this.open = (this.now[0] === 0);
		});
		if (window.webkit419) this.addEvent('onComplete', function(){
			if (this.open) this.element.remove().inject(this.wrapper);
		});
	},

	setNow: function(){
		for (var i = 0; i < 2; i++) this.now[i] = this.compute(this.from[i], this.to[i]);
	},

	vertical: function(){
		this.margin = 'margin-top';
		this.layout = 'height';
		this.offset = this.element.offsetHeight;
	},

	horizontal: function(){
		this.margin = 'margin-left';
		this.layout = 'width';
		this.offset = this.element.offsetWidth;
	},

	/*
	Property: slideIn
		Slides the elements in view horizontally or vertically.

	Arguments:
		mode - (optional, string) 'horizontal' or 'vertical'; defaults to options.mode.
	*/

	slideIn: function(mode){
		this[mode || this.options.mode]();
		return this.start([this.element.getStyle(this.margin).toInt(), this.wrapper.getStyle(this.layout).toInt()], [0, this.offset]);
	},

	/*
	Property: slideOut
		Sides the elements out of view horizontally or vertically.

	Arguments:
		mode - (optional, string) 'horizontal' or 'vertical'; defaults to options.mode.
	*/

	slideOut: function(mode){
		this[mode || this.options.mode]();
		return this.start([this.element.getStyle(this.margin).toInt(), this.wrapper.getStyle(this.layout).toInt()], [-this.offset, 0]);
	},

	/*
	Property: hide
		Hides the element without a transition.

	Arguments:
		mode - (optional, string) 'horizontal' or 'vertical'; defaults to options.mode.
	*/

	hide: function(mode){
		this[mode || this.options.mode]();
		this.open = false;
		return this.set([-this.offset, 0]);
	},

	/*
	Property: show
		Shows the element without a transition.

	Arguments:
		mode - (optional, string) 'horizontal' or 'vertical'; defaults to options.mode.
	*/

	show: function(mode){
		this[mode || this.options.mode]();
		this.open = true;
		return this.set([0, this.offset]);
	},

	/*
	Property: toggle
		Slides in or Out the element, depending on its state

	Arguments:
		mode - (optional, string) 'horizontal' or 'vertical'; defaults to options.mode.

	*/

	toggle: function(mode){
		if (this.wrapper.offsetHeight == 0 || this.wrapper.offsetWidth == 0) return this.slideIn(mode);
		return this.slideOut(mode);
	},

	increase: function(){
		this.element.setStyle(this.margin, this.now[0] + this.options.unit);
		this.wrapper.setStyle(this.layout, this.now[1] + this.options.unit);
	}

});

/*
Script: Fx.Transitions.js
	Effects transitions, to be used with all the effects.

License:
	MIT-style license.

Credits:
	Easing Equations by Robert Penner, <http://www.robertpenner.com/easing/>, modified & optimized to be used with mootools.
*/

/*
Class: Fx.Transitions
	A collection of tweening transitions for use with the <Fx.Base> classes.

Example:
	>//Elastic.easeOut with default values:
	>new Fx.Style('margin', {transition: Fx.Transitions.Elastic.easeOut});
	>//Elastic.easeOut with user-defined value for elasticity.
	> var myTransition = new Fx.Transition(Fx.Transitions.Elastic, 3);
	>new Fx.Style('margin', {transition: myTransition.easeOut});

See also:
	http://www.robertpenner.com/easing/
*/

Fx.Transition = function(transition, params){
	params = params || [];
	if ($type(params) != 'array') params = [params];
	return $extend(transition, {
		easeIn: function(pos){
			return transition(pos, params);
		},
		easeOut: function(pos){
			return 1 - transition(1 - pos, params);
		},
		easeInOut: function(pos){
			return (pos <= 0.5) ? transition(2 * pos, params) / 2 : (2 - transition(2 * (1 - pos), params)) / 2;
		}
	});
};

Fx.Transitions = new Abstract({

	/*
	Property: linear
		displays a linear transition.

	Graph:
		(see Linear.png)
	*/

	linear: function(p){
		return p;
	}

});

Fx.Transitions.extend = function(transitions){
	for (var transition in transitions){
		Fx.Transitions[transition] = new Fx.Transition(transitions[transition]);
		/*compatibility*/
		Fx.Transitions.compat(transition);
		/*end compatibility*/
	}
};

/*compatibility*/

Fx.Transitions.compat = function(transition){
	['In', 'Out', 'InOut'].each(function(easeType){
		Fx.Transitions[transition.toLowerCase() + easeType] = Fx.Transitions[transition]['ease' + easeType];
	});
};

/*end compatibility*/

Fx.Transitions.extend({

	/*
	Property: Quad
		displays a quadratic transition. Must be used as Quad.easeIn or Quad.easeOut or Quad.easeInOut

	Graph:
		(see Quad.png)
	*/

	//auto generated

	/*
	Property: Cubic
		displays a cubicular transition. Must be used as Cubic.easeIn or Cubic.easeOut or Cubic.easeInOut

	Graph:
		(see Cubic.png)
	*/

	//auto generated

	/*
	Property: Quart
		displays a quartetic transition. Must be used as Quart.easeIn or Quart.easeOut or Quart.easeInOut

	Graph:
		(see Quart.png)
	*/

	//auto generated

	/*
	Property: Quint
		displays a quintic transition. Must be used as Quint.easeIn or Quint.easeOut or Quint.easeInOut

	Graph:
		(see Quint.png)
	*/

	//auto generated

	/*
	Property: Pow
		Used to generate Quad, Cubic, Quart and Quint.
		By default is p^6.

	Graph:
		(see Pow.png)
	*/

	Pow: function(p, x){
		return Math.pow(p, x[0] || 6);
	},

	/*
	Property: Expo
		displays a exponential transition. Must be used as Expo.easeIn or Expo.easeOut or Expo.easeInOut

	Graph:
		(see Expo.png)
	*/

	Expo: function(p){
		return Math.pow(2, 8 * (p - 1));
	},

	/*
	Property: Circ
		displays a circular transition. Must be used as Circ.easeIn or Circ.easeOut or Circ.easeInOut

	Graph:
		(see Circ.png)
	*/

	Circ: function(p){
		return 1 - Math.sin(Math.acos(p));
	},


	/*
	Property: Sine
		displays a sineousidal transition. Must be used as Sine.easeIn or Sine.easeOut or Sine.easeInOut

	Graph:
		(see Sine.png)
	*/

	Sine: function(p){
		return 1 - Math.sin((1 - p) * Math.PI / 2);
	},

	/*
	Property: Back
		makes the transition go back, then all forth. Must be used as Back.easeIn or Back.easeOut or Back.easeInOut

	Graph:
		(see Back.png)
	*/

	Back: function(p, x){
		x = x[0] || 1.618;
		return Math.pow(p, 2) * ((x + 1) * p - x);
	},

	/*
	Property: Bounce
		makes the transition bouncy. Must be used as Bounce.easeIn or Bounce.easeOut or Bounce.easeInOut

	Graph:
		(see Bounce.png)
	*/

	Bounce: function(p){
		var value;
		for (var a = 0, b = 1; 1; a += b, b /= 2){
			if (p >= (7 - 4 * a) / 11){
				value = - Math.pow((11 - 6 * a - 11 * p) / 4, 2) + b * b;
				break;
			}
		}
		return value;
	},

	/*
	Property: Elastic
		Elastic curve. Must be used as Elastic.easeIn or Elastic.easeOut or Elastic.easeInOut

	Graph:
		(see Elastic.png)
	*/

	Elastic: function(p, x){
		return Math.pow(2, 10 * --p) * Math.cos(20 * p * Math.PI * (x[0] || 1) / 3);
	}

});

['Quad', 'Cubic', 'Quart', 'Quint'].each(function(transition, i){
	Fx.Transitions[transition] = new Fx.Transition(function(p){
		return Math.pow(p, [i + 2]);
	});
	
	/*compatibility*/
	Fx.Transitions.compat(transition);
	/*end compatibility*/
});

/*
Script: XHR.js
	Contains the basic XMLHttpRequest Class Wrapper.

License:
	MIT-style license.
*/

/*
Class: XHR
	Basic XMLHttpRequest Wrapper.

Arguments:
	options - an object with options names as keys. See options below.

Options:
	method - 'post' or 'get' - the protocol for the request; optional, defaults to 'post'.
	async - boolean: asynchronous option; true uses asynchronous requests. Defaults to true.
	encoding - the encoding, defaults to utf-8.
	autoCancel - cancels the already running request if another one is sent. defaults to false.
	headers - accepts an object, that will be set to request headers.
	
Events:
	onRequest - function to execute when the XHR request is fired.
	onSuccess - function to execute when the XHR request completes.
	onStateChange - function to execute when the state of the XMLHttpRequest changes.
	onFailure - function to execute when the state of the XMLHttpRequest changes.

Properties:
	running - true if the request is running.
	response - object, text and xml as keys. You can access this property in the onSuccess event.

Example:
	>var myXHR = new XHR({method: 'get'}).send('http://site.com/requestHandler.php', 'name=john&lastname=dorian');
*/

var XHR = new Class({

	options: {
		method: 'post',
		async: true,
		onRequest: Class.empty,
		onSuccess: Class.empty,
		onFailure: Class.empty,
		urlEncoded: true,
		encoding: 'utf-8',
		autoCancel: false,
		headers: {}
	},

	setTransport: function(){
		this.transport = (window.XMLHttpRequest) ? new XMLHttpRequest() : (window.ie ? new ActiveXObject('Microsoft.XMLHTTP') : false);
		return this;
	},

	initialize: function(options){
		this.setTransport().setOptions(options);
		this.options.isSuccess = this.options.isSuccess || this.isSuccess;
		this.headers = {};
		if (this.options.urlEncoded && this.options.method == 'post'){
			var encoding = (this.options.encoding) ? '; charset=' + this.options.encoding : '';
			this.setHeader('Content-type', 'application/x-www-form-urlencoded' + encoding);
		}
		if (this.options.initialize) this.options.initialize.call(this);
	},

	onStateChange: function(){
		if (this.transport.readyState != 4 || !this.running) return;
		this.running = false;
		var status = 0;
		try {status = this.transport.status;} catch(e){};
		if (this.options.isSuccess.call(this, status)) this.onSuccess();
		else this.onFailure();
		this.transport.onreadystatechange = Class.empty;
	},

	isSuccess: function(status){
		return ((status >= 200) && (status < 300));
	},

	onSuccess: function(){
		this.response = {
			'text': this.transport.responseText,
			'xml': this.transport.responseXML
		};
		this.fireEvent('onSuccess', [this.response.text, this.response.xml]);
		this.callChain();
	},

	onFailure: function(){
		this.fireEvent('onFailure', this.transport);
	},

	/*
	Property: setHeader
		Add/modify an header for the request. It will not override headers from the options.

	Example:
		>var myXhr = new XHR(url, {method: 'get', headers: {'X-Request': 'JSON'}});
		>myXhr.setHeader('Last-Modified','Sat, 1 Jan 2005 05:00:00 GMT');
	*/

	setHeader: function(name, value){
		this.headers[name] = value;
		return this;
	},

	/*
	Property: send
		Opens the XHR connection and sends the data. Data has to be null or a string.

	Example:
		>var myXhr = new XHR({method: 'post'});
		>myXhr.send(url, querystring);
		>
		>var syncXhr = new XHR({async: false, method: 'post'});
		>syncXhr.send(url, null);
		>
	*/

	send: function(url, data){
		if (this.options.autoCancel) this.cancel();
		else if (this.running) return this;
		this.running = true;
		if (data && this.options.method == 'get'){
			url = url + (url.contains('?') ? '&' : '?') + data;
			data = null;
		}
		this.transport.open(this.options.method.toUpperCase(), url, this.options.async);
		this.transport.onreadystatechange = this.onStateChange.bind(this);
		if ((this.options.method == 'post') && this.transport.overrideMimeType) this.setHeader('Connection', 'close');
		$extend(this.headers, this.options.headers);
		for (var type in this.headers) try {this.transport.setRequestHeader(type, this.headers[type]);} catch(e){};
		this.fireEvent('onRequest');
		this.transport.send($pick(data, null));
		return this;
	},

	/*
	Property: cancel
		Cancels the running request. No effect if the request is not running.

	Example:
		>var myXhr = new XHR({method: 'get'}).send(url);
		>myXhr.cancel();
	*/

	cancel: function(){
		if (!this.running) return this;
		this.running = false;
		this.transport.abort();
		this.transport.onreadystatechange = Class.empty;
		this.setTransport();
		this.fireEvent('onCancel');
		return this;
	}

});

XHR.implement(new Chain, new Events, new Options);