# Create our first polygon - a very poor approximation of a circle as it's square.
curl -XPUT 'http://localhost:9200/alerts/alert/test_00001' -d ' 
{ 
    "longId": 1, 
    "id": "00001", 
    "alertShape" : {
        "type" : "polygon",
        "coordinates" : [
            [ [-109.5288, 40.4555], 
              [-109.5288, 40.4565], 
              [-109.5298, 40.4565], 
              [-109.5298, 40.4555], 
              [-109.5288, 40.4555] ]
        ]
    }
}' 

# Elasticsearcg supports circles, even tho GeoJSON officially does not. Hurrah! - default units for radus == meters
# https://www.elastic.co/guide/en/elasticsearch/reference/current/geo-shape.html
curl -XPUT 'http://localhost:9200/alerts/alert/test_00002' -d ' 
{ 
    "longId": 2, 
    "id": "00002", 
    "alertShape" : {
        "type" : "circle",
        "coordinates" : [-109.5288,40.4555],
        "radius" : "1000m"
    }
}'
