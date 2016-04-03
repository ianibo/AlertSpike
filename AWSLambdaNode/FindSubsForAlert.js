console.log('Loading CAP event notifications');

var http = require('http');
var aws = require('aws-sdk');
// var s3 = new aws.S3({ apiVersion: '2006-03-01' });

console.log("Get xml2js");
var parseString = require('xml2js').parseString;

console.log("OK GO");
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

    // console.log("Event %o",event);

    var shape = null;
    var send_sns = 0;
    var alerts = [];
    var num_alerts = 1;
    var lambda_response = 'OK';
    var ctr = 0;

    if ( event.Records ) {
      // console.log("Handle sns");
      num_alerts = event.Records.length;

      for (var i = 0; i < num_alerts; i++) {
        // console.log("Pushing %o",event.Records[i].Sns.Message);
        var json_payload = JSON.parse(event.Records[i].Sns.Message);
        // console.log("Parsed json payload %o",json_payload);
        parseString(json_payload.alert.capXML, function(err, result) {
          alerts.push(result);
        });
      }
      send_sns = 0;
    }
    else {
      // console.log("Handle direct");
      // Direct event from http interface or test
      parseString(event.alert.capXML, function(err, result) {
        alerts.push(result);
      });
    }

    var sns = send_sns ? new aws.SNS() : null;

    for (var i = 0; i < num_alerts; i++) {

      var alert = alerts[i];
      console.log("Processing %o",alert);

      var info_elements = alert['cap:alert']['cap:info'];
      // console.log("Processing info %o",info_elements);

      for ( var j=0; j<info_elements.length; j++ ) {
        // console.log("Process info element %o",info_elements[j]);

        var cap_area_elements = info_elements[j]['cap:area'];
        for ( var k=0; k<info_elements.length; k++ ) {
          console.log("Process cap area %o %o",cap_area_elements[k]['cap:areaDesc'],cap_area_elements[k]['cap:polygon']);
          if ( cap_area_elements[k]['cap:polygon'] ) {

            var polygon_str = cap_area_elements[k]['cap:polygon'][0];
            var polygon_arr = []

            // Parse cap:polygon into array of points consisting of lng lat comma separated
            var points_arr = polygon_str.split(' ');
            for ( var l = 0; l<points_arr.length; l++ ) {
              var lonlat = points_arr[l].split(',');
              polygon_arr.push([lonlat[0],lonlat[1]]);
            }

            shape = {
              "type": "polygon",
              "coordinates" : [ polygon_arr ]
            }
            console.log("Setting up polygon shape : %o",shape);
          }
          else {
            console.log("No polygon found");
          }
        }

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
  
        // console.log("Send query %s",postData);
  
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
  
        console.log("Create request");

        var req = http.request(options, function(res) {

            var body = '';
            console.log('Status:', res.statusCode);
            console.log('Headers:', JSON.stringify(res.headers));
            res.setEncoding('utf8');
            res.on('data', function(chunk) {
                body += chunk;
            });
  
            res.on('end', function() {
                console.log("Processing search response");

                // console.log('Successfully processed HTTP response');
                // If we know it's JSON, parse it
                if (res.headers['content-type'].lastIndexOf('application/json',0) === 0 ) {
                  body = JSON.parse(body);
                  var num_profiles = body.hits.hits.length;
  
                  // console.log("Processing %d profiles",num_profiles);
                  for (var i = 0; i < num_profiles; i++) {
                    var profile_entry = body.hits.hits[i]
                    console.log("Processing hit %d %s %s %s %s",i,profile_entry._source.name,profile_entry._source.recid,profile_entry._source.shortcode);
                    // Shape is in profile_entry._source.subshape
  
                    if ( send_sns ) {
                      console.log("Publish profile alert message");
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
  
                // Don't call this until all requests have completed
                ctr--;
                console.log("ctr: %o",ctr);
                if ( ctr == 0 ) {
                  context.succeed(lambda_response);
                }
            });
        });
  
        console.log("Sending http query %s",postData);

        req.on('error', function(e) {
          console.log("error %o",e);
          context.fail();
        });

        ctr++;
        req.write(postData);
        console.log("req.end");
        req.end();
        console.log("Call completed");
      }
      else {
        // No shape to search against
        console.log("No shape to search against");
      }
    }


    console.log("Complete");

};
