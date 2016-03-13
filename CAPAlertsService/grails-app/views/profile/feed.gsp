<html>
<head>
  <meta name="layout" content="main"/>
  <link rel="stylesheet" href="http://cdn.leafletjs.com/leaflet/v0.7.7/leaflet.css" />
  <script src="http://cdn.leafletjs.com/leaflet/v0.7.7/leaflet.js"></script>
  <style>
#map {
    height:800px;
    bottom:0;
    top:0;
    left:0;
    right:0;
}
  </style>
</head>
<body>
  <div class="container-fluid main">
    <div class="row">
      <div class="col-md-6">
         <div id="map"></div>
      </div>
      <div class="col-md-6" style="height:800px; overflow:scroll;">
        <g:link controller="profile" action="feed" id="${params.id}" params="${[format:'atom']}">ATOM</g:link>
        <g:link controller="profile" action="feed" id="${params.id}" params="${[format:'json']}">JSON</g:link>
        <table class="table table-striped table-bordered">
          <thead>
            <tr>
              <th>Event</th>
            </tr>
          </thead>
          <tbody>
            <g:each in="${hits}" var="h" status="i">
              <tr id="feature${i}" class="featureRow">
                <td>
                  <button class="btn pull-right btn-info" onClick="showAlert(${i})">Show</button>
                  <a href="${h.web}">${h.headline}</a> ${h.sourcets}<br/>
                  ${h.description}<br/>
                  [${h.category}]
                </td>
              </tr>
            </g:each>
          </tbody>
        </table>
      </div>
    </div>
  </div>
  <script language="JavaScript">

    var alerts = [
      <g:each in="${hits}" var="g" status="i">
        {
          pageid:${i},
          headline:"${g.headline}", 
          category:"${g.category}", 
          description:"${g.description}", 
          instruction:"${g.instruction}", 
          web:"${g.headline}", 
          event:"${g.event}", 
          agentts:"${g.agentts}", 
          sourcets:"${g.sourcets}", 
          urgency:"${g.urgency}", 
          severity:"${g.severity}",
          areas:[
            <g:each in="${g.areas}" var="a">
              {
                label:"${a.label}",
                shapeType:"${a.alertShape.type}",
                coordinates:${a.alertShape.coordinates},
                radius:"${a.alertShape.radius}"
              },
            </g:each>
          ]
        },
      </g:each>
    ];

    var map = null;
    var autoShow = 0;
    var autoShowTimer = null;

    $(document).ready(function() {
      map = L.map('map').setView([51.505, -0.09], 1);
      L.tileLayer('http://{s}.tile.osm.org/{z}/{x}/{y}.png', {
          attribution: '&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
      }).addTo(map);

      var lg = L.featureGroup([]);

      var l = alerts.length;
      for (var i = 0; i < l; i++) {
        // console.log("Adding geometry %o",alerts[i].areas[0]);

        if ( alerts[i].areas[0].shapeType === 'polygon' ) {
          var geojsonFeature = {
            "type": "Feature",
            "properties": {
                "name": alerts[i].headline,
            },
            "geometry": {
            }
          }
          geojsonFeature.geometry.type='Polygon';
          geojsonFeature.geometry.coordinates=alerts[i].areas[0].coordinates;
          var feature = L.geoJson(geojsonFeature);
          feature.addTo(map);
          feature.addLayer(lg);
          alerts[i].feature = feature;
        }
        else {

          var geojsonFeature = {
              "type": "Feature",
              "properties": {
                  "name": alerts[i].headline,
              },
              "geometry": {
                  "type": "Point",
                  "coordinates": alerts[i].areas[0].coordinates
              }
          };

          var geojsonMarkerOptions = {
              radius: 8,
              fillColor: "#ff7800",
              color: "#005",
              weight: 1,
              opacity: 1,
              fillOpacity: 0.4
          };

          var latlng = L.latLng( alerts[i].areas[0].coordinates[1], alerts[i].areas[0].coordinates[0]);

          var feature = L.geoJson(geojsonFeature, {
              pointToLayer: function (feature, latlng) {
                  // return L.circleMarker(latlng, geojsonMarkerOptions);
                  return L.circle(latlng, alerts[i].areas[0].radius*1000, geojsonMarkerOptions);
              }
          });
          feature.addTo(map);
          feature.addLayer(lg);
          alerts[i].feature = feature;
        }
      }
      // map.fitBounds(lg.getBounds()); 

      autoShowTimer = setInterval(autoAdvance,5000);
    });

    function autoAdvance() {
      autoShow++;
      if ( autoShow == alerts.length ) {
        autoShow = 0;
      }
      console.log("autoAdvance %d",autoShow);
      showAlert(autoShow);
      return true;
    }

    function showAlert(id) {
      map.fitBounds(alerts[id].feature.getBounds());
      var active_feature_div = document.getElementById('feature'+id);
       $('.featureRow').css({
         background:"#ffffff"
       })
      active_feature_div.scrollIntoView(true);
      active_feature_div.style.background = '#F0F0F3';
      active_feature_div.scrollIntoView(true);
    }
  </script>
</body>
</html>

