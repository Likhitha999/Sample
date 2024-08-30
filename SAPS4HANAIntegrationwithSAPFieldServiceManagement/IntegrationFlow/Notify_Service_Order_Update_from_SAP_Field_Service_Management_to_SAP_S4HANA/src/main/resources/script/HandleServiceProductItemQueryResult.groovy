import com.sap.gateway.ip.core.customdev.util.Message

def Message processData(Message message) {
    def body = message.getBody(java.lang.String) as String
    def parser = new XmlParser()
    def parserResult = parser.parseText(body)

    def serviceOrderNeedComplete, serviceProductMap = [:]
    def currentServiceOrderItem
    def resultJson, regPattern

    currentServiceOrderItem = message.getProperty("ServiceOrderItem") as String

    parserResult.A_ServiceOrderItemType.each{
        item ->
            if (item.ServiceOrderItem.text() == currentServiceOrderItem) return
            serviceProductMap[item.ServiceOrderItem.text()] = item.ServiceOrderItemIsCompleted.text()
    }
    serviceProductMap.remove(currentServiceOrderItem)
    serviceOrderNeedComplete = serviceProductMap.every { key, value -> value == "X" }

    if (serviceOrderNeedComplete){
        resultJson = message.getProperty("resultJson") as String
        regPattern = /"ServiceOrderItems"/
        def replacePart = '''"ServiceOrderIsCompleted":"T",
     "ServiceOrderItems"'''
        resultJson = resultJson.replaceFirst(regPattern,replacePart)
        message.setProperty("resultJson",resultJson)
    }
    message.setProperty("serviceOrderNeedComplete",serviceOrderNeedComplete)
    return message
}
