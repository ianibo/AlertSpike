@Grapes([
    // @GrabResolver(name='central', root='http://central.maven.org/maven2/'),
    @GrabResolver(name='mvnRepository', root='http://central.maven.org/maven2/'),
    // @Grab(group='org.slf4j', module='slf4j-api', version='1.7.6'),
    // @Grab(group='org.slf4j', module='jcl-over-slf4j', version='1.7.6'),
    @Grab(group='net.sourceforge.nekohtml', module='nekohtml', version='1.9.14'),
    @Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.5.1'),
    @Grab(group='xerces', module='xercesImpl', version='2.9.1'),
    @Grab(group='org.apache.httpcomponents', module='httpclient', version='4.5.1'),
    @GrabExclude('org.codehaus.groovy:groovy-all')   
])

// http://www.sheffieldairmap.org/view_map.html
// http://uk-air.defra.gov.uk/networks/site-info?site_id=SHE2

import groovyx.net.http.*
import static groovyx.net.http.ContentType.URLENC
import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*
import groovyx.net.http.*
import org.apache.http.entity.mime.*
import org.apache.http.entity.mime.content.*
import java.nio.charset.Charset
import static groovy.json.JsonOutput.*
import groovy.json.JsonSlurper
import java.text.SimpleDateFormat
import java.net.InetAddress;


println("Run as groovy -Dgroovy.grape.autoDownload=false  ./ial.groovy\nTo avoid startup lag");

def result = searchForSubscriptions();
println result;


def searchForSubscriptions() {
  def result = [:]
  def es = new RESTClient("http://ce.semweb.co")
  // def es = new RESTClient("http://localhost:9200")
  // result.r = es.get(path:"/alertssubscriptions/_search",
  //                   query:[
  //                     q:'*'
  //                   ])
  def res = es.post(path:"/es/alertssubscriptions/_search",
                   requestContentType: JSON,
                   body:[
                     "query":[
                       "bool": [
                         "must": [
                           "match_all": [:]
                         ],
                         "filter": [
                             "geo_shape": [
                               "subshape": [
                                 "shape": [
                                   "type": "circle",
                                   "coordinates" : [-109.5288,40.4555],
                                   "radius" : "1000m"
                                 ],
                                 relation:'intersects'
                               ]
                             ]
                           ]
                         ]
                       ]
                   ])
    
  res.data
}
