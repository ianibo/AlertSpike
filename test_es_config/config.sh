# Clear down
curl -XDELETE 'http://localhost:9200/alerts'
# Create an index called alerts
curl -XPUT 'http://localhost:9200/alerts'
# Create a type mapping called alert
curl -XPUT 'http://localhost:9200/alerts/alert/_mapping' -d ' 
{ 
   "alert":{ 
      "properties":{ 
         "id":{ 
            "include_in_all":"false", 
            "index":"not_analyzed", 
            "type":"string", 
            "store":"yes" 
         }, 
         "areas" : {
           "type": "nested",
           "properties":{
             "alertShape": {
                "type": "geo_shape",
                "tree": "quadtree",
                "precision": "100m"
             },
             "label":{
               "type":"string", 
               "store":"yes" 
             }
           }
         }
      }
   } 
}' 
curl -XDELETE 'http://localhost:9200/alertssubscriptions'
curl -XPUT 'http://localhost:9200/alertssubscriptions'
curl -XPUT 'http://localhost:9200/alertssubscriptions/subscription/_mapping' -d ' 
{ 
   "subscription":{ 
      "properties":{ 
         "id":{ 
            "include_in_all":"false", 
            "index":"not_analyzed", 
            "type":"string", 
            "store":"yes" 
         }, 
         "areas" : {
           "type": "nested",
           "properties":{
             "alertShape": {
                "type": "geo_shape",
                "tree": "quadtree",
                "precision": "100m"
             },
             "label":{
               "type":"string", 
               "store":"yes" 
             }
           }
         }
      }
   } 
}'

