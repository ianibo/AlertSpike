console.log('Loading CAP event notifications');

var http = require('http');
var querystring = require('querystring');
var aws = require('aws-sdk');
var s3 = new aws.S3({ apiVersion: '2006-03-01' });

/**
 * See https://nodejs.org/api/http.html#http_http_request_options_callback
 *
 * This handler accepts an event containing a GeoJson shape definition. Normally [tho not constrained so] this will
 * be a polygon or a circle. The handler will return a list of all subscriptions intersecting that alert profile.
 * Example events include
 *
 *  "polygonCoordinates": [
 *    [-109.5297,40.4554], [-109.5298,40.4556], [-109.5299,40.4556], [-109.5299,40.4554], [-109.5297,40.4554]
 *  ]
 *
 *
 *  "circleCenterRadius": [ -109.5288, 40.4555, 1000]
 *
 */
exports.handler = function(event, context) {

    var shape = null;
    var send_sns = 1;

    if ( event.polygonCoordinates ) {
      shape = {
        "type": "polygon",
        "coordinates" : [ event.polygonCoordinates ]
      }
    }
    else if ( event.circleCenterRadius ) {
       shape = { 
        "type": "circle",
        "coordinates" : [event.circleCenterRadius[0],event.circleCenterRadius[1]],
        "radius" : ""+event.circleCenterRadius[2]+"m"
      }
    }
    else {
    }

    if ( shape ) {

      var postData = JSON.stringify({
                       "from":0,
                       "size":1000,
                       "query":{
                         "bool": {
                           "must": {
                             "match_all": {}
                           },
                           "filter": {
                               "geo_shape": {
                                 "subshape": {
                                   "shape": shape,
                                   "relation":'intersects'
                                 }
                               }
                             }
                           }
                         },
                         "sort":{
                           "recid":{order:'asc'}
                         }

      });

      console.log("Send query %s",postData);

      var options = {
        hostname: 'ce.semweb.co',
        port: 80,
        json: true,
        body: postData,
        path: '/es/alertssubscriptions/_search',
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Content-Length': postData.length
        }
      };


      var req = http.request(options, function(res) {
          var body = '';
          var lambda_response = 'OK';
          // console.log('Status:', res.statusCode);
          // console.log('Headers:', JSON.stringify(res.headers));
          res.setEncoding('utf8');
          res.on('data', function(chunk) {
              body += chunk;
          });

          res.on('end', function() {
              // console.log('Successfully processed HTTP response');
              // If we know it's JSON, parse it
              if (res.headers['content-type'].lastIndexOf('application/json',0) === 0 ) {
                body = JSON.parse(body);
                var sns = send_sns ? new aws.SNS() : null;
                var num_profiles = body.hits.hits.length;

                // console.log("Processing %d profiles",num_profiles);
                for (var i = 0; i < num_profiles; i++) {
                  var profile_entry = body.hits.hits[i]
                  console.log("Processing hit %d %s %s %s %s",i,profile_entry._source.name,profile_entry._source.recid,profile_entry._source.shortcode);
                  // Shape is in profile_entry._source.subshape

                  if ( send_sns ) {
                    // Send sns for each matching sub
                    var pubResult = sns.publish({
                        Message: 'CAP Alert Profile Notification '+profile_entry._source.recid,
                        TopicArn: 'arn:aws:sns:eu-west-1:603029492791:CAPProfileNotification'
                        // TopicArn: 'arn:aws:sns:us-east-1:381798314226:alert-hub-area-match'
                    }, function(err, data) {
                        if (err) {
                            console.log(err.stack);
                            return;
                        }
                        // console.log('push sent');
                        // console.log(data);
                    });
                  }
                }
              }
              else {
                console.log("Unable to process response type %s",res.headers['content-type']);
              }

              context.succeed(lambda_response);
          });
      });

      req.on('error', context.fail);

      req.write(postData);
      req.end();
    }
    else {
      // No shape to search against
    }
};
