import com.sap.gateway.ip.core.customdev.util.Message
import java.util.HashMap
import groovy.util.XmlParser
import groovy.xml.XmlUtil

// remove empty nodes from the XML message 
def Message processData(Message message) {
    def body = message.getBody(String.class)
    def xmlBody = new XmlParser().parseText(body)

    xmlBody.depthFirst().each {
        // remove empty activities nodes
        if (it.name() == 'activities' && !it.text()) {
            it.replaceNode{}            
        }
        // remove empty reservedMaterials nodes
        if (it.name() == 'reservedMaterials' && !it.text()) {
            it.replaceNode{}            
        }
    }
    message.setBody(XmlUtil.serialize(xmlBody))
    return message
}