import com.sap.gateway.ip.core.customdev.util.Message

def Message processData(Message message) {
// get service order body and service call body 
    def xmlParser = new XmlParser()
    def Payload                 = message.getProperty("SourcePayload")
    def ActivityExternalId      = message.getProperty("ActivityExternalId")
    def XMLServiceOrder         = xmlParser.parseText(Payload)
    def ServiceOrderItems       = XMLServiceOrder.ServiceOrder.Item as List

// get item number
    if (ActivityExternalId) {
        externalIds = ActivityExternalId.split('/')
        ServiceOrderItem   = (externalIds.size() > 2) ? externalIds[2] : externalIds[1]    
        message.setProperty("ServiceOrderItem",ServiceOrderItem)        
    }

// determine activity code
    for (def item in ServiceOrderItems){

    if (item.ServiceOrderItem.text() == ServiceOrderItem )
        {
            message.setProperty("FSMServiceActivity",item.FSMServiceActivity.text())        
        }
    }  

   return message
}