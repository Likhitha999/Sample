import com.sap.gateway.ip.core.customdev.util.Message;
import groovy.util.XmlParser;
import groovy.json.JsonSlurper;
import groovy.*

def Message processData(Message message) {
    def sourcePayload   = message.getProperty('SourcePayload')
    def rootSource      = new XmlParser().parseText(sourcePayload)
    def itemId = []
    def ActivitiesCategories = message.getProperty('ItemCategoriesForFSMActivities')
    def allowedCategories = ''
    def emptyServiceOrder = 'X'

    if (ActivitiesCategories != null && ActivitiesCategories !='') {
        allowedCategories = ActivitiesCategories.split("\\|")*.trim()
    }

    if (message.getProperty("ServiceOrderContainsCompletedItem") == "true") {
// find all ServiceOrderItemIds in the FSMServiceCallPayload. These are available below "activities" in field "externalId" as the second part after "/" 
        def fsmServiceCall  = message.getBody(String.class)
        def fsm             = new JsonSlurper().parseText(fsmServiceCall)
        fsm?.activities.each{ node ->
            if(node?.externalId != null){
                extId = node?.externalId?.split("/")[-1]
                itemId.add(extId)
            }
        }
    }
    
    if (message.getProperty("ServiceOrderItemIsReleasedPresent") == "true") {
        // If a service order item <> "Released" is not replicated yet to FSM, it shall be ignored, i.e. removed from the payload.
        rootSource.ServiceOrder.Item.each{ sourceItem-> 
            // check if the service order item <> "Released"
            if ( !(sourceItem.ServiceOrderItemIsReleased.text() ==~ /(?i)(true|x)/) ) {
                // Check if the itemId is available in the list extracted from the FSMServiceCallPayload
                if (!(sourceItem.ServiceOrderItem.text() in itemId)){
                    sourceItem.replaceNode {}
                    return false
                }
            }
            
            // Check if the SO contains an item of an allowed category
            if (allowedCategories.contains(sourceItem.ServiceOrderItemCategory.text())) {
                emptyServiceOrder = ''
            }
        }
        
    //Backwards Compatibility
    } else {
        def s4Payload       = message.getProperty('PayloadS4')
        def rootS4          = new XmlParser().parseText(s4Payload)

        rootSource.ServiceOrder.Item.each{ sourceItem->
            
            itemS4 = rootS4.A_ServiceOrderType.to_Item.A_ServiceOrderItemType.'**'.find{
                        sourceItem.ServiceOrderItem.text() == it.ServiceOrderItem.text() }
    
            // If a service item <> "Released" is not replicated yet to FSM, it shall be ignored, i.e. removed from the payload.
            if (itemS4 == null || !(itemS4.ServiceOrderItemIsReleased.text() ==~ /(?i)(true|x)/ )){
                // Check if the itemId is available in the list extracted from the FSMServiceCallPayload
                if (!(sourceItem.ServiceOrderItem.text() in itemId)){
                    sourceItem.replaceNode {}
                    return false
                }
            }
            // Check if the SO contains an item of an allowed category
            if (allowedCategories.contains(sourceItem.ServiceOrderItemCategory.text())) {
                emptyServiceOrder = ''
            }		
        }
    }

    message.setProperty('EmptyServiceOrder', emptyServiceOrder)

    String outxml = groovy.xml.XmlUtil.serialize( rootSource )
    message.setBody(outxml)
    return message
}