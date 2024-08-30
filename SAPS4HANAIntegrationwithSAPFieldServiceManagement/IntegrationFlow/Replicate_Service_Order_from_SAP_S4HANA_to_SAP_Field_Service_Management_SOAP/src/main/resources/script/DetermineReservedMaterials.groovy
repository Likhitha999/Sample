import com.sap.gateway.ip.core.customdev.util.Message
import java.util.HashMap
import groovy.util.XmlParser
import groovy.*

def Message processData(Message message) {
    def body = message.getBody(String.class)
    def root = new XmlParser().parseText(body)
    def activitiesCategories = message.getProperty("ItemCategoriesForFSMActivities")?.split("\\|")*.trim()

    // check all materials for parent items
    root.reservedMaterials.each{ rm ->
        def parentItem = root.activities.find{ act ->
            rm.temp_ParentItem.text() == act.temp_ItemId.text()
        }
        //check if parent item is a service item
        if (parentItem?.temp_Category?.text() in activitiesCategories){
            //remove temporary node (cannot be done later)
            rm.remove(rm.temp_ParentItem)
            //add the material to the activity
            parentItem.appendNode("reservedMaterials",rm.collect())
            //remove "old" material from Service Call
            rm.parent().remove(rm)
        }
    }

    // delete all temporary fields
    root.depthFirst().each{ it ->
        if (it.name().startsWith("temp_")){
            it.replaceNode{}
        }
    }

    //build output
    String outxml = groovy.xml.XmlUtil.serialize( root )
    message.setBody(outxml)
    return message
}