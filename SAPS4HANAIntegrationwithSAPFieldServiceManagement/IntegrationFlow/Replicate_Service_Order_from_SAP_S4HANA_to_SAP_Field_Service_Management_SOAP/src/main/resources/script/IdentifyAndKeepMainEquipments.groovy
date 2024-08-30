import com.sap.gateway.ip.core.customdev.util.Message
import groovy.util.XmlParser
import groovy.xml.XmlUtil
import groovy.json.JsonOutput

// determine main equipments from the oData query to S4 
// and modify the FSM payload and add the main equipments 
def Message processData(Message message) {
    //the retrieval of the S/4 payload had to be executed in a previous step, so its result is available in property 'PayloadS4'
    //def query_body = message.getBody(String.class)
    def query_body = message.getProperty('PayloadS4')
    def query_root = new XmlParser().parseText(query_body)
    def main_equipments = []
    def equipment 

    // find all main equipments/function locations(fl) and add them to list main_equipments
    // the list consists of concatinated strings containing item id and equipment/fl
    // if the item id part is empty the equipment/fl belongs to the service order
    // example: [ ":Z123456", "10:Z00007" ]
    query_root.'**'.each{ node ->
        if (node.name() == 'SrvcRefObjIsMainObject' && node.value()[0] == 'true'){
            node_parent = node.parent()
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
            try {
                item_id = node_parent.'**'.find { it.name() == 'ServiceOrderItem' }.value()[0]
            } catch(Exception ex1) { /* not found */ }
            if (equipment != null && equipment != '') {
                main_equipments.add(item_id + ':' + equipment)
            }
        }
    }
    message.setProperty("MainEquipments", main_equipments.toString())

    // modify mapped FSM payload and add main equipments from main equipment list
    def body = message.getProperty('PayloadFSM')
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