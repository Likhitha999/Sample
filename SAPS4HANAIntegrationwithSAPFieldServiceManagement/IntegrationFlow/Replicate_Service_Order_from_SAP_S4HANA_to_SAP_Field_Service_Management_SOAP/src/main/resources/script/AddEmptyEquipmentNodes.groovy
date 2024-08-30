import com.sap.gateway.ip.core.customdev.util.Message
import groovy.util.XmlParser
import groovy.xml.XmlUtil

// add missing empty equipment nodes
def Message processData(Message message) {
    def body = message.getBody(String.class)
    def parser = new XmlParser()
    def root = parser.parseText(body)
    def header_equipments = false
    def item_equipment = false

    // header
    root.'**'.each{ node ->
        if (node.name() == 'equipments'){
            header_equipments = true
        }
    }
    if (!header_equipments) {
        root.append(parser.parseText('<equipments/>'))
    }
    // items
    root.activities.each{ activity ->
        if (activity) {
            item_equipment = false
            activity.'**'.each{ node ->
                if (node.name() == 'equipment'){
                    item_equipment = true
                }
            }
            if (!item_equipment) {
                activity.append(parser.parseText('<equipment/>'))                    
            }
        }
    }
    // return modified payload
    String outxml = groovy.xml.XmlUtil.serialize( root )
    message.setBody(outxml)
    return message
}