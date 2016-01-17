'use strict';

define(
  function () {
    console.log ("Finance Module");
    
    // Constructor code, and private values go here.
    var taxCoeficient = 0.2;
    
    // Return the object that represents the public API.
    return {
      calcTax : function (val) {
        return val * taxCoeficient;
      }
    };
  }
);