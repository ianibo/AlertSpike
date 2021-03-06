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
 "alert": {
 "sourceId": "iq-mowr-mosul-dam-en",
 "sourceName": "Iraq Ministry of Water Resources, Mosul Dam alerts in English",
 "guid": "urn:oid:2.49.0.0.368.1",
 "author": "tbd",
 "sourceIsOfficial": true,
 "sourceLanguage": "en",
 "capAlertFeed": "tbd", 
 "authorityCountry": "iq",
 "authorityAbbrev": "iq-mowr",
 "capXML": "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n<?xml-stylesheet type=\"text/xsl\" href=\"cap-style-mowr-en.xsl\" ?>\n<cap:alert xmlns:cap=\"urn:oasis:names:tc:emergency:cap:1.1\" >\n  <cap:identifier>urn:oid:2.49.0.1.368.1.2016.4.2.18.24.38</cap:identifier>\n  <cap:sender>Eliot.Christian@redcross.org</cap:sender>\n  <cap:sent>2016-04-02T18:24:38-00:00</cap:sent>\n  <cap:status>Test</cap:status>\n  <cap:msgType>Alert</cap:msgType>\n  <cap:scope>Public</cap:scope>\n  <cap:info>\n    <cap:language>en</cap:language>\n    <cap:category>Infra</cap:category>\n    <cap:event>dam failure</cap:event>\n    <cap:responseType>Evacuate</cap:responseType>\n    <cap:urgency>Immediate</cap:urgency>\n    <cap:severity>Extreme</cap:severity>\n    <cap:certainty>Observed</cap:certainty>\n    <cap:senderName>Iraq Ministry of Water Resources</cap:senderName>\n    <cap:headline>URGENT--FLOOD EVACUATION Warning for Tigris river. Head for higher ground immediately.</cap:headline>\n    <cap:description>Mosul dam has failed. Extreme Tigris river flooding will affect: Mosul to 25 meters within 4 hours; Tikrit to 15 meters within 22 hours; Baghdad to 4 meters within 45 hours.</cap:description>\n    <cap:instruction>Everyone near the Tigris below Mosul dam must move immediately to high ground and several kilometers away from the river. Help persons that need assistance to evacuate. Follow evacuation signs or ask locals. Do not take anything with you. Do not block roads or paths. Stay at the flood safe point as the flood will take several hours to recede. Local authorities will tell you when it is safe.</cap:instruction>\n    <cap:area>\n      <cap:areaDesc>Areas below Mosul on Tigris river, including the cities of Mosul, Shirqat, Baiji, Tikrit, Samarra, Balad, Dujail, and Baghdad</cap:areaDesc>\n      <cap:polygon>30.3,47.6 30.8,48 32.4,47.3 33.2,44.9 34,44.5 34.8,43.8 35.8,43.6 36.5,43.2 36.7,42.7 36.4,42.6 33,44.3 31.8,46.7 30.3,47.6</cap:polygon>\n    </cap:area>\n  </cap:info>\n</cap:alert>"
 }
};

alertsHandler.handler(evt1, context);
