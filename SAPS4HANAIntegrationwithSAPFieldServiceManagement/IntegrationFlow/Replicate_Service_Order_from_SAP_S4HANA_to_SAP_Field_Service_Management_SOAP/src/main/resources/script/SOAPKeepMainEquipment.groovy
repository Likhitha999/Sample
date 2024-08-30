import com.sap.gateway.ip.core.customdev.util.Message
import groovy.util.XmlParser
import groovy.xml.XmlUtil
import groovy.json.JsonOutput

// determine main equipments from the original payload
// and modify the FSM payload and add the main equipments 
def Message processData(Message message) {
    def original_body = message.getProperty("OriginalPayload")
    def original_root = new XmlParser().parseText(original_body)
    def main_equipments = []
    def equipment

    // find all main equipments/function locations(fl) and add them to list main_equipments
    // the list consists of concatinated strings containing item id and equipment/fl
    // if the item id part is empty the equipment belongs to the service order
    // example: [ ":Z123456", "10:Z00007" ]
    original_root.'**'.each{ node ->
        if (node.name() == 'SrvcRefObjIsMainObject' && node.value()[0] == 'true'){
            def node_parent = node.parent() // is ServiceReferenceObject
            def node_grand_parent = node_parent.parent() // ServiceOrder or Item
            equipment = ''
            try {
                equipment = node_parent.'**'.find { it.name() == 'ServiceReferenceEquipment' }.value()[0]
            } catch(Exception ex0) { /* not found */ }
            if (equipment == null || equipment == '') {
                try {
                    equipment = node_parent.'**'.find { it.name() == 'ServiceRefFunctionalLocation' }.value()[0]
                } catch(Exception ex0) { /* not found */ }
            }
            item_id = ''
            if (node_grand_parent.name() == 'Item') { // at item level
                try {
                    item_id = node_grand_parent.'**'.find { it.name() == 'ServiceOrderItem' }.value()[0]
                } catch(Exception ex1) { /* not found */ }
            }
            if (equipment != null &&  equipment != '') {
                main_equipments.add(item_id + ':' + equipment)
            }
        }
    }
    message.setProperty("MainEquipments", main_equipments.toString())

    // modify mapped FSM payload and add main equipments from main equipment list
    def body = message.getBody(String.class)
    def root = new XmlParser().parseText(body)
    def parent, ref_obj, activity

    main_equipments.each{ object -> 
        parent = object.split(":")[0]
        ref_obj = object.split(":")[1]
        if (!parent) {
            root.appendNode("equipments").appendNode("externalId", ref_obj)
        } else {
            activity = root.activities.find{ act ->
                act.temp_ItemId.text() == parent
            }
            if (activity) {
                activity.appendNode("equipment").appendNode("externalId", ref_obj)
            }
        }
    }

    // return modified payload
    String outxml = groovy.xml.XmlUtil.serialize( root )
    message.setBody(outxml)

    return message
}