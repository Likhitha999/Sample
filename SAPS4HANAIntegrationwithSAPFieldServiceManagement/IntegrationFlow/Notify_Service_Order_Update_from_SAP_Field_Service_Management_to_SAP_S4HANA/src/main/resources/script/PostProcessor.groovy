import com.sap.gateway.ip.core.customdev.util.Message

def Message processData(Message message) {
    def headers = message.getHeaders()
    def eventType = message.getProperty("eventType")
    if (eventType != null) eventType = eventType.toLowerCase()

    def sapMessage, code, body = "", isItemUpdated, isHeaderUpdated

    switch(eventType) {
        case "servicecall.updated":
            isHeaderUpdated = message.getProperty("isHeaderUpdated") || message.getProperty("isHeaderTextUpdated") || message.getProperty("isReferenceObjectUpdated")
            code = isHeaderUpdated ? 200: 202

            sapMessage = headers.get("sap-message")
            if (sapMessage) { body = sapMessage }

            headers.put("CamelHttpResponseCode", code)
            message.setBody(body)
            break

        case "activity.updated":
        case "activity.released":
        case "activity.completed":
            isItemUpdated = message.getProperty("isItemUpdated") || message.getProperty("isItemReferenceObjectUpdated") || message.getProperty("isItemRemarkUpdated")
            code = isItemUpdated ? 200: 202

            sapMessage = headers.get("sap-message")
            if (sapMessage) { body = sapMessage }

            headers.put("CamelHttpResponseCode", code)
            message.setBody(body)
            break

        default:
            headers.put("CamelHttpResponseCode", 202)
            message.setBody("")
            break
    }
    return message
}