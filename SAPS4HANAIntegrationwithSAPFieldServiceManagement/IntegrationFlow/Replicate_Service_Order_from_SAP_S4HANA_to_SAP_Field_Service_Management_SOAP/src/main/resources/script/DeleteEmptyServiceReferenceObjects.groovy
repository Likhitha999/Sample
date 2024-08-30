import com.sap.gateway.ip.core.customdev.util.Message
import groovy.util.XmlParser
import groovy.xml.XmlUtil

def Message processData(Message message) {
    def body = message.getBody(String.class)
    def root = new XmlParser().parseText(body)

    // delete all empty ServiceReferenceObjects
    root.'**'.each{ node ->
        if (node.name() == 'ServiceReferenceObject' && node.value().isEmpty()){
            node.replaceNode{}
        }
    }

    String outxml = groovy.xml.XmlUtil.serialize( root )
    message.setBody(outxml)
    return message
}