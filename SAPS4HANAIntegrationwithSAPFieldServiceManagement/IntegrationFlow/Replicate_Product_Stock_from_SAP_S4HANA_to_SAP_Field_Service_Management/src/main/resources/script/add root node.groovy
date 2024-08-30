import com.sap.gateway.ip.core.customdev.util.Message;
import groovy.json.JsonOutput
import groovy.json.JsonSlurper

def Message processData(Message message) {

def response = new XmlParser().parseText(message.getBody(String))
def newXml = new groovy.xml.StreamingMarkupBuilder().bind {
   root {
     mkp.yield xml
   }
 }
 message.setBody(groovy.xml.XmlUtil.serialize(newXml))

 return message;	
}