// Our Lambda function fle is required 
var alertsHandler = require('./FindSubsForAlert.js');

// The Lambda context "done" function is called when complete with/without error
var context = {
    done: function (err, result) {
        console.log('------------');
        console.log('Context done');
        console.log('   error:', err);
        console.log('   result:', result);
    },

    fail: function(err,result) {
        console.log('------------');
        console.log('Context fail');
        console.log('   error:', err);
        console.log('   result:', result);
    },

    succeed: function(body) {
        console.log('------------');
        console.log('Context succeed');
        console.log('   body:', body);
    }
};

var evt1 = {
  shape : {
    "type": "polygon",
    "coordinates" : [ [-109.5297,40.4554], [-109.5298,40.4556], [-109.5299,40.4556], [-109.5299,40.4554], [-109.5297,40.4554] ]
  }
};

var evt2 = {
  shape : { 
    "type": "circle",
    "coordinates" : [-109.5288,40.4555],
    "radius" : "1000m"

  }
};


// Call the Lambda function
alertsHandler.handler(evt1, context);
alertsHandler.handler(evt2, context);
