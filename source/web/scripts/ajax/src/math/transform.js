dojo.provide("dojo.math.Matrix");
dojo.provide("dojo.math.Transform");
dojo.require("dojo.lang");

/* TODO: figure out a way of reconciling the matrix functionality of Cal's
	to the drawing needs.
*/

/*
	3 x 3 matrix for transformation purposes:
		| a  b  c |		| 1 0 0 |
		| d  e  f |		| 0 1 0 |
		| g  h  i |		| 0 0 1 |
*/
dojo.math.Matrix=function(){
	this.a=this.e=this.i=1;
	this.b=this.c=this.d=this.f=this.g=this.h=0;
};
dojo.lang.extend(dojo.math.Matrix,{
	reset:function(){
		this.a=this.e=this.i=1;
		this.b=this.c=this.d=this.f=this.g=this.h=0;
	},
	apply:function(point){
		return {
			x: this.a*point.x + this.b*point.y + this.c,
			y: this.d*point.x + this.e*point.y + this.f
		};
	}
});

dojo.math.Transform=function(matrix){
	this.transformations=[];
	this.transformations.push(matrix||new dojo.math.Matrix());
};
dojo.lang.extend(dojo.math.Transform,{
	reset:function(){
		this.transformations=[];
	},
	add:function(matrix){
		this.transformations.push(matrix);
	},
	flatten:function(){
		var matrix=this.get();
		this.transformations=[ matrix ];
	},
	get:function(){
		var idx=0;
		var matrix=this.transformations[idx++];
		while(this.transformations.length>idx){
			var operand=new dojo.math.Matrix();
			var current=this.transformations[idx++];
			operand.a=matrix.a*current.a + matrix.b*current.d + matrix.c*current.g;
			operand.b=matrix.a*current.b + matrix.b*current.e + matrix.c*current.h;
			operand.c=matrix.a*current.c + matrix.b*current.f + matrix.c*current.i;
			operand.d=matrix.d*current.a + matrix.e*current.d + matrix.f*current.g;
			operand.e=matrix.d*current.b + matrix.e*current.e + matrix.f*current.h;
			operand.f=matrix.d*current.c + matrix.e*current.f + matrix.f*current.i;
			operand.g=matrix.g*current.a + matrix.h*current.d + matrix.i*current.g;
			operand.h=matrix.g*current.b + matrix.h*current.e + matrix.i*current.h;
			operand.i=matrix.g*current.c + matrix.h*current.f + matrix.i*current.i;
			matrix=operand;
		}
		return matrix;
	},
	peek:function(){
		return this.transformations[0];
	},
	rotate:function(angle){
		angle=dojo.math.degToRad(angle);
		var matrix=new dojo.math.Matrix();
		matrix.a=matrix.e=Math.cos(angle);
		matrix.d=Math.sin(angle);
		matrix.b=-1*matrix.d;
		this.add(matrix);
	},
	rotateAt:function(angle, cx, cy){
		this.translate(cx, cy);
		this.rotate(angle);
		this.translate(-cx, -cy);
	},
	scale:function(sx, sy){
		var matrix=new dojo.math.Matrix();
		matrix.a=sx;
		matrix.e=sy;
		this.add(matrix);
	},
	skewX:function(angle){
		angle=dojo.math.degToRad(angle);
		var matrix=new dojo.math.Matrix();
		matrix.b=Math.tan(angle);
		this.add(matrix);
	},
	skewY:function(angle){
		angle=dojo.math.degToRad(angle);
		var matrix=new dojo.math.Matrix();
		matrix.d=Math.tan(angle);
		this.add(matrix);
	},
	translate:function(tx, ty){
		var matrix=new dojo.math.Matrix();
		matrix.c=tx;
		matrix.f=ty;
		this.add(matrix);
	}
});
