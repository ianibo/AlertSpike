# Clear down
curl -XDELETE 'http://localhost:9200/alerts'
# Create an index called alerts
curl -XPUT 'http://localhost:9200/alerts'
# Create a type mapping called alert
curl -XPUT 'http://localhost:9200/alerts/alert/_mapping' -d ' 
{ 
   "alert":{ 
      "properties":{ 
         "longId":{ 
            "type":"long", 
            "store":"yes" 
         }, 
         "id":{ 
            "include_in_all":"false", 
            "index":"not_analyzed", 
            "type":"string", 
            "store":"yes" 
         }, 
         "areas" : {
           "properties":{
             "alertShape": {
                "type": "geo_shape",
                "tree": "quadtree",
                "precision": "1m"
             },
             "label":{
               "index":"not_analyzed", 
               "type":"string", 
               "store":"yes" 
             }
           }
         }
      }
   } 
}' 
