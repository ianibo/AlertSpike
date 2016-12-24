@Grapes([
    @GrabResolver(name='mvnRepository', root='http://central.maven.org/maven2/'),
//    @Grab(group='org.elasticsearch', module='elasticsearch-groovy', version='2.1.2'),
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
import org.elasticsearch.common.transport.InetSocketTransportAddress


Settings settings = Settings.builder().put("cluster.name", "elasticsearch").build();

TransportClient esclient = new org.elasticsearch.transport.client.PreBuiltTransportClient(settings);

esclient.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300));


ctr = 0

println("Load data");
load(esclient)
println("Done")

// on shutdown
esclient.close();
System.exit(0);


def load(esclient) {
  def data = null;
  def data_file = null; 
  if ( args.length == 1 ) {
    println("Load subs from ${args[0]}");
    data_file = new File(args[0])
  }
  else {
    data_file = new File('./alert-hub-subscriptions.json');
  }

  if ( data_file.exists() ) {
    data = new JsonSlurper().parseText( data_file.text )
  }
  else {
    data = [:]
  }

  data.subscriptions.each { sub ->
    if ( sub.subscription.areaFilter?.polygonCoordinates ) {
      if ( sub.subscription.areaFilter?.polygonCoordinates.size() > 0 ) {
        // println("Got coords ${sub.subscription.areaFilter?.polygonCoordinates}");
        processEntry(sub.subscription, esclient)
      }
    }
  }
}

def processEntry(sub, esclient) {

  println("Process ${sub.subscriptionId} : ${sub.subscriptionName}");

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
                    areaFilterId: sub.areaFilterId,
                    loadSubsVersion: "1.1"
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
    println("Circle");
    es_record.subshape.type='circle'
    es_record.subshape.coordinates=sub.areaFilter.polygonCoordinates
  }

  def submit_start = System.currentTimeMillis();

  try {
    println("Prepare");
    def future = esclient.prepareIndex('alertssubscriptions','alertsubscription').setSource(es_record)
    println("Get");
    def r=future.get()
    println("Completed ${r}");
  }
  catch ( Exception e ) {
    e.printStackTrace()
    println("Error processing ${toJson(es_record)}\n\n");
    // System.exit(0);
  }

  println("ES update in ${System.currentTimeMillis()-submit_start}");
}
