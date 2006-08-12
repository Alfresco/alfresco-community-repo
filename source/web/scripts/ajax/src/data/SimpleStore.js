dojo.provide("dojo.data.SimpleStore");
dojo.require("dojo.lang");

dojo.require("dojo.experimental");
dojo.experimental("dojo.data.SimpleStore");

/*	SimpleStore
 *	Designed to be a simple store of data with access methods...
 *	specifically to be mixed into other objects (such as widgets).
 * 
 *	This *might* be better in collections, we'll see.
 */
dojo.data.SimpleStore = function(/* array? */json){
	//	summary
	//	Data Store with accessor methods.
	var data = [];
	this.keyField = "Id";

	this.get = function(){
		//	summary
		//	Get the internal data array, should not be used.
		return data;	//	array
	};
	this.getByKey = function(/* string */key){
		//	summary
		//	Find the internal data object by key.
		for(var i=0; i<data.length; i++){
			if(data[i].key==key){
				return data[i];	// object
			}
		}
		return null;	// null
	};
	this.getByIndex = function(/*number*/idx){ 
		//	summary
		//	Get the internal data object by index.
		return data[idx]; 	// object
	};
	
	this.getData = function(){
		//	summary
		//	Get an array of source objects.
		var arr = [];
		for(var i=0; i<data.length; i++){
			arr.push(data[i]);
		}
		return arr;	//	array
	};
	this.getDataByKey = function(/*string*/key){
		//	summary
		//	Get the source object by key.
		for(var i=0; i<data.length; i++){
			if(data[i].key==key){
				return data[i].src; //	object
			}
		}
		return null;	//	null
	};
	this.getDataByIndex = function(/*number*/idx){ 
		//	summary
		//	Get the source object at index idx.
		return data[idx].src; 	//	object
	};

	this.forEach = function(/* function */fn){
		//	summary
		//	Functional iteration directly on the internal data array.
		if(Array.forEach){
			Array.forEach(data, fn, this);
		}else{
			for(var i=0; i<data.length; i++){
				fn.call(this, data[i]);
			}
		}
	};
	this.forEachData = function(/* function */fn){
		//	summary
		//	Functional iteration on source objects in internal data array.
		if(Array.forEach){
			Array.forEach(this.getData(), fn, this);
		}else{
			var a=this.getData();
			for(var i=0; i<a.length; i++){
				fn.call(this, a[i]);
			}
		}
	};

	this.setData = function(/*array*/arr){
		//	summary
		//	Set up the internal data.
		data = []; 	//	don't fire onClearData
		for(var i=0; i<arr.length; i++){
			data.push({ 
				key:arr[i][this.keyField], 
				src:arr[i]
			});
		}
		this.onSetData();
	};
	
	this.clearData = function(){
		//	summary
		//	Clears the internal data array.
		data = [];
		this.onClearData();
	};

	this.addData = function(/*obj*/obj,/*string?*/key){ 
		//	summary
		//	Add an object with optional key to the internal data array.
		var o={ 
			key:key||obj[this.keyField], 
			src:obj 
		};
		data.push(o);
		this.onAddData(o);
	};
	this.addDataRange = function(/*array*/arr){
		//	summary
		//	Add a range of objects to the internal data array.
		var objects=[];
		for(var i=0; i<arr.length; i++){
			var o = { 
				key:arr[i][this.keyField], 
				src:arr[i] 
			};
			data.push(o);
			objects.push(o);
		}
		this.onAddDataRange(objects);
	};
	
	this.removeData = function(/*obj*/obj){
		//	summary
		//	remove the passed object from the internal data array.
		var idx=-1;
		var o=null;
		for(var i=0; i<data.length; i++){
			if(data[i].src==obj){
				idx=i;
				o=data[i];
				break;
			}
		}
		if(idx>-1){
			data.splice(idx,1);
		}
		this.onRemoveData(o);
	};
	this.removeDataByKey = function(/*string*/key){
		//	summary
		//	remove the object at key from the internal data array.
		this.removeData(this.getDataByKey(key));
	};
	this.removeDataByIndex = function(/*number*/idx){
		//	summary
		//	remove the object at idx from the internal data array.
		this.removeData(this.getDataByIndex(idx));
	};

	if(json && json.length && json[0]){
		this.setData(json);
	}
};

dojo.lang.extend(dojo.data.SimpleStore, {
	getField:function(/*object*/obj, /*string*/field){
		//	helper to get the nested value if needed.
		var parts=field.split("."), i=0, o=obj;
		do{ 
			if(parts[i].indexOf("()")>-1){
				var temp=parts[i++].split("()")[0];
				if(!o[temp]){
					dojo.raise("dojo.data.SimpleStore.getField(obj, '" + field + "'): '" + field + "' is not a property of the passed object.");
				} else {
					o = o[temp]();
				}
			} else {
				o = o[parts[i++]];
			}
		} while (i<parts.length && o);
		
		if(i < parts.length || !o){
			dojo.raise("dojo.data.SimpleStore.getField(obj, '" + field + "'): '" + field + "' is not a property of the passed object.");
		}
		return o; // object
	},
	onSetData:function(){ },
	onClearData:function(){ },
	onAddData:function(obj){ },
	onAddDataRange:function(arr){ },
	onRemoveData:function(obj){ }
});
