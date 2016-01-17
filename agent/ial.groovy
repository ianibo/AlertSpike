@Grapes([
    // @GrabResolver(name='central', root='http://central.maven.org/maven2/'),
    @GrabResolver(name='mvnRepository', root='http://central.maven.org/maven2/'),
    @Grab(group='org.slf4j', module='slf4j-api', version='1.7.6'),
    @Grab(group='org.slf4j', module='jcl-over-slf4j', version='1.7.6'),
    @Grab(group='net.sourceforge.nekohtml', module='nekohtml', version='1.9.14'),
    @Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.5.1'),
    @Grab(group='xerces', module='xercesImpl', version='2.9.1'),
    @Grab(group='org.elasticsearch', module='elasticsearch-groovy', version='2.0.0'),
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

import org.elasticsearch.client.Client
import org.elasticsearch.node.Node
import static org.elasticsearch.node.NodeBuilder.nodeBuilder
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.groovy.*
import org.elasticsearch.common.transport.InetSocketTransportAddress
import static org.elasticsearch.node.NodeBuilder.nodeBuilder

import java.security.MessageDigest

Settings settings = Settings.settingsBuilder()
                       .put("client.transport.sniff", true)
                       .put("cluster.name", "elasticsearch")
                       .build();
esclient = TransportClient.builder().settings(settings).build();
// add transport addresses
esclient.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300 as int))

ctr = 0


// Here we list all the fields from the cap object that we want to map into a JSON document for
// ES to index. the addField method will look at the variant and if the DEFAULT variant is the same,
// will add that field (If the value is different). Not sure this is the best way to go -- mixing
// locales in IR is always a tricky thing - will probably need to be reviewed and improved many times.
infoFields = [
    language:[element:'cap:language',    langstring:false,    json_element:'language'],
    category:[element:'cap:category',    langstring:false,    json_element:'category'],
       event:[element:'cap:event',       langstring:true,     json_element:'event'],
      source:[element:'cap:source',      langstring:true,     json_element:'source'],
       scope:[element:'cap:scope',       langstring:true,     json_element:'scope'],
    headline:[element:'cap:headline',    langstring:true,     json_element:'headline'],
 description:[element:'cap:description', langstring:true,     json_element:'description'],
 instruction:[element:'cap:instruction', langstring:true,     json_element:'instruction'],
         web:[element:'cap:web',         langstring:false,    json_element:'web']
];

feeds = [
  'https://alerts.internetalerts.org/subscriptions/public-alerts'
]

// Do it
listen()

// Will never reach this. use ctrl-c to exit
System.exit(0);

def listen() {
  while ( true ) {
    println("Run as groovy -Dgroovy.grape.autoDownload=false  ./ial.groovy\nTo avoid startup lag");

    config = null;

    cfg_file = new File('./ial-config.json');
    if ( cfg_file.exists() ) {
      config = new JsonSlurper().parseText( cfg_file.text )
    }
    else {
      config = [:]
    }

    // doUpdate('https://alerts.internetalerts.org/feed')
    feeds.each { feed ->
      doUpdate(feed);
    }

    // Update config file with latest state information
    cfg_file << toJson(config);

    synchronized(this) {
      println("Sleeping 5 mins");
      Thread.sleep(300000);
    }
  }
}

def doUpdate(baseurl) {

  try {

    def baseurl_config = config[baseurl]
    if ( baseurl_config == null ) {
      baseurl_config = [:]
      baseurl_config.head_timestamp=0;
      baseurl_config.last_checked=0;
      baseurl_config.atom_highest_timestamp=0
      baseurl_config.feed_updated_timestamp=0
      config[baseurl] = baseurl_config;
    }

    // lets use HTTPBuilders neat HeadMethod to see when the file was last touched
    def head = new org.apache.http.client.methods.HttpHead(baseurl);

    def headers = head.getAllHeaders();
    println("Headers : ${headers}");
    // looks like //alerts.internetalerts.org/subscriptions/public-alerts isn't carrying any last modified headers
    // in response to HEAD -- would be great if this could be added -- especially if we're polling at second level resolution

    // We really should use HTTP HEAD here to get the last touched time of the feed - and be super efficient
    def feed = new XmlSlurper()
                     .parse(baseurl)
                     .declareNamespace(atom:'http://www.w3.org/2005/Atom')

    def feed_last_modified = feed.'atom:updated'.text().trim()
    // TimeZone in the feed is not RFC822 - grr - strip out the annoying :
    def l = feed_last_modified.length()
    feed_last_modified = feed_last_modified.substring(0,l-3)+feed_last_modified.substring(l-2,l);

    def sdt = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
    def parsed_feed_last_modified = sdt.parse(feed_last_modified).getTime();

    println("Last modified : ${feed_last_modified}/${parsed_feed_last_modified}");

    def biggest_timestamp = baseurl_config.atom_highest_timestamp;

    if ( parsed_feed_last_modified > baseurl_config.feed_updated_timestamp ) {

      println("Feed updated(${parsed_feed_last_modified}) since last check(${baseurl_config.feed_updated_timestamp}) - process");

      feed.'atom:entry'.each { entry ->

        def entry_updated = entry.'atom:updated'.text().trim();
        entry_updated = entry_updated.substring(0,l-3)+entry_updated.substring(l-2,l);
        def parsed_entry_updated = sdt.parse(entry_updated).getTime();

        if ( parsed_entry_updated > baseurl_config.atom_highest_timestamp ) {

          if (  parsed_entry_updated > biggest_timestamp )
            biggest_timestamp = parsed_entry_updated

          processEntry(entry.'atom:title'.text(),
                       entry.'atom:id'.text(),
                       entry_updated,
                       entry.'atom:link'.@href.text())
        }
        else {
          // println("Already seen: ${entry.'atom:id'.text()}");
        }

      }
      println("Completed processing");

      println("Updating config - parsed_feed_last_modified = ${parsed_feed_last_modified}");

      // update for config file -- Time of last update to feed
      baseurl_config.feed_updated_timestamp=parsed_feed_last_modified
      // Biggest timestamp we have seen in a record so far
      baseurl_config.atom_highest_timestamp=biggest_timestamp
    }
  }
  catch ( Exception e ) {
    println("ERROR....(${baseurl})"+e.message);
    e.printStackTrace();
    throw(e)
  }
}

def processEntry(title, rec_id, timestamp, url) {

  def es_record = [:]
  es_record.areas = []
  def default_langcode = 'en'

  println("processEntry id:${rec_id} url:${url}");

  def entry = new XmlSlurper()
                    .parse(url)
                    .declareNamespace(cap: 'urn:oasis:names:tc:emergency:cap:1.2', test: 'urn:oasis:names:tc:emergency:captest:1.2')

  String local_record_id = entry.'cap:identifier'[0].text()

  // Lets use the feed ID to make sure we don't have namespace clashes
  es_record.id = rec_id

  entry.'cap:info'.each { info ->

    // Looks like each info section is repeated with a language code.. 
    def entry_lang = info.'cap:language'.text()
    def langcode = default_langcode

    if ( entry_lang.trim().length() > 0 ) {
      langcode=entry_lang.substring(0,2);
    }

    // Cycle through the fields we want to extract, and see if each property is present
    infoFields.each { k,v ->
      def value = info."${v.element}"?.text().trim()
      if ( value && ( value.trim().length() > 0 ) ) {
        // println("Element ${v.element} present, lang is ${entry_locale}, value is ${value}");
        addOrAppendElement(es_record, v.json_element, value, langcode, default_langcode, v.langstring)
      }
    }

    info.'cap:parameter'.each { param ->
      // println("${param.'cap:valueName'} = ${param.'cap:value'}")
    }
    info.'cap:area'.each { area_xml ->
      // println("${area.'cap:areaDesc'} -- ${area.'cap:polygon'}");
      def area = extractArea(area_xml)

      // What we *should* do here is to see if we have the area already, but with a different langstring variant,
      // and add a variant label to the area rather than duplicating the whole area. But it's a POC, so lets live with it

      // Do we alreadt have an area with a fingerprint that matches the new area? If so, just add a langstring variant
      def existing_area = es_record.areas.find { it.fingerprint = area.fingerprint }
      if ( existing_area ) {
        addOrAppendElement(existing_area, "label", area.label, langcode, default_langcode, true);
      }
      else {
        es_record.areas.add(area);
        addOrAppendElement(area, "label", area.label, langcode, default_langcode, true);
      }
    }

    println("Add record ${ctr}");

    try {
        def future = esclient.index {
          index "alerts"
          type "alert"
          id rec_id
          source es_record
        }

        future.get()
        println("Done.. ${ctr++}");
    }
    catch ( Exception e ) {
      e.printStackTrace()
    }

  }
}

def addOrAppendElement(basemap, elementname, value, langcode, default_langcode, is_langstring) {
  // println("addOrAppendElement(${basemap},${elementname},${value}");
  if ( is_langstring ) {
    addString(basemap,elementname+'_'+langcode,value);
    if ( langcode == default_langcode ) {
      addString(basemap,elementname,value);
    }
  }
  else {
    addString(basemap,elementname,value);
  }
}

def addString(basemap,elementname,value) {
  if ( basemap[elementname] == null ) {
    // no value currently
    basemap[elementname] = value
  }
  else if ( basemap[elementname] instanceof List ) {
    // property is already a list, add another value
    if ( ! basemap[elementname].contains(value) ) {
      basemap[elementname] = basemap[elementname].add(value)
    }
  }
  else {
    // Convert scalar to list
    if ( basemap[elementname] != value ) {
      basemap[elementname] = [basemap[elementname], value]
    }
  }
}

def extractArea(area_xml) {
  // Take a cap:area - cap:areaDesc label and geometry such as cap:circle and convert
  def result = [:]
  result.label = area_xml.'cap:areaDesc'.text().trim()

  def cap_circle = area_xml.'cap:circle'.text().trim()
  if ( cap_circle.length() > 0 ) {
    def stage1 = cap_circle.split(' ' as String); // Split on space to get radius. ES Circle defaults to meters as a unit. CAP seems to be different.
    def stage2 = stage1[0].split(',' as String); // Split cap records lat,lon. ES expects X,Y so we have to flip
    result.geom=[type:'circle', coordinates:[stage2[1],  stage2[0]], radius:stage1[1]]
    result.fingerPrint = generateMD5_A("circle_"+stage2[1]+"_"+stage2[0]+"_"+stage1[1]);
  }

  def cap_polygon = area_xml.'cap:polygon'.text().trim()
  if ( cap_polygon.length() > 0 ) {
    def stage1 = cap_polygon.split(' ' as String);  // Split components of polygon
    StringWriter sw = new StringWriter()
    stage1.each {
      def stage2 = it.split(',' as String); // Sply lat,lon so we can flip to X,Y
      sw.write(stage2[1]);
      sw.write(',');
      sw.write(stage2[0]);
      sw.write(' ');
    }
    result.geom=[type:'polygon', coordinates:sw.toString()]
    result.fingerPrint = generateMD5_A('polygon'+cap_polygon)
  }

  return result
}

def generateMD5_A(String s){
    MessageDigest.getInstance("MD5").digest(s.bytes).encodeHex().toString()
}

