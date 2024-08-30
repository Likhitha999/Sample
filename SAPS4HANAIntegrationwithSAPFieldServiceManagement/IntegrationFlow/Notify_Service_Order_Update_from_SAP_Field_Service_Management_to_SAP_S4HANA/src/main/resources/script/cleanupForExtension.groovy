import com.sap.gateway.ip.core.customdev.util.Message;
import java.util.HashMap;
import groovy.xml.*

def Message processData(Message message) {
    
    def body = message.getBody(java.lang.String);
    def xmlparser = new XmlSlurper();
     
    //used for Notes Replication
    message.setProperty("mappingResultPayload", body);
    
    //cleanup Payload for Extension Exit
    body = xmlparser.parseText(body); 
    body.A_ServiceOrderType.to_Text.replaceNode{ }
    body = groovy.xml.XmlUtil.serialize( body )
    
    //used for ExtensionExit & Update Header
    message.setBody(body);
    
    return message;
}