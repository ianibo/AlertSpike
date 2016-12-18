@Grapes([
    @GrabResolver(name='mvnRepository', root='http://central.maven.org/maven2/'),
    @Grab(group='org.elasticsearch', module='elasticsearch-groovy', version='2.1.2'),
    @Grab(group = 'org.elasticsearch', module = 'elasticsearch', version = '5.1.1'),
    @Grab(group = 'org.elasticsearch.client', module = 'transport', version = '5.1.1'),
    @Grab(group = 'org.apache.logging.log4j', module = 'log4j-api', version = '2.7'),
    @Grab(group = 'org.apache.logging.log4j', module = 'log4j-core', version = '2.7'),
    @GrabExclude('org.codehaus.groovy:groovy-all')   
])

import static groovy.json.JsonOutput.*
import groovy.json.JsonSlurper
import java.text.SimpleDateFormat
import java.net.InetAddress;

import org.elasticsearch.client.Client
import org.elasticsearch.node.Node
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.groovy.*
import org.elasticsearch.common.transport.InetSocketTransportAddress

println("Get ES");

// Settings settings = Settings.settingsBuilder()
//                        .put("client.transport.sniff", true)
//                        .put("cluster.name", "elasticsearch")
//                        .build();
// esclient = TransportClient.builder().settings(settings).build();

esclient = new org.elasticsearch.transport.client.PreBuiltTransportClient(Settings.EMPTY)
esclient.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300))

// add transport addresses
// esclient.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300 as int))

ctr = 0

println("Load data");
load()
println("Done")

System.exit(0);


def load() {
  def data = null;
  def data_file = new File('./alert-hub-subscriptions.json');
  if ( data_file.exists() ) {
    data = new JsonSlurper().parseText( data_file.text )
  }
  else {
    data = [:]
  }

  data.subscriptions.each { sub ->
    // println("Sub ${sub}");
    if ( sub.subscription.areaFilter?.polygonCoordinates ) {
      if ( sub.subscription.areaFilter?.polygonCoordinates.size() > 0 ) {
        // println("Got coords ${sub.subscription.areaFilter?.polygonCoordinates}");
        processEntry(sub.subscription)
      }
    }
  }
}

def processEntry(sub) {

  println("Process ${sub}");

  def es_record = [
                    recid:sub.subscriptionId,
                    name:sub.subscriptionName,
                    shortcode:sub.subscriptionId,
                    subshape:[:],
                    subscriptionUrl:sub.subscriptionUrl,
                    languageOnly:sub.languageOnly,
                    highPriorityOnly: sub.highPriorityOnly,
                    officialOnly: sub.officialOnly,
                    xPathFilterId: sub.xPathFilterId,
                    xPathFilter: sub.xPathFilter,
                    areaFilterId: sub.areaFilterId
                  ]

  println("Value of sub.areaFilter.circleCenterRadius :: \"${sub.areaFilter.circleCenterRadius}\" ");
  if ( ( sub.areaFilter.circleCenterRadius=="none") || 
       ( sub.areaFilter.circleCenterRadius=="") || 
       ( sub.areaFilter.circleCenterRadius==null) ) {
    // treat as polygon
    println("Polygon");
    es_record.subshape.type='polygon'
    es_record.subshape.coordinates=[sub.areaFilter.polygonCoordinates]
  }
  else {
    es_record.subshape.type='circle'
    es_record.subshape.coordinates=sub.areaFilter.polygonCoordinates
  }

  def submit_start = System.currentTimeMillis();

  try {
    def future = esclient.index {
      index "alertssubscriptions"
      type "alertsubscription"
      id sub.subscriptionId
      source es_record
    }

    def r=future.actionGet()

    println("Index completed ${r}");
  }
  catch ( Exception e ) {
    e.printStackTrace()
    println("Error processing ${toJson(es_record)}\n\n");
    // System.exit(0);
  }

  println("ES update in ${System.currentTimeMillis()-submit_start}");
}
