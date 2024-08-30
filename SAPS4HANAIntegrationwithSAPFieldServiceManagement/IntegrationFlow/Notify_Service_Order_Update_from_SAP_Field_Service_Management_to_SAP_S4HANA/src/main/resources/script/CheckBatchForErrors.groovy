import com.sap.gateway.ip.core.customdev.util.Message

def parseBody (body, httpStatusCode, statusInfo, defaultBody) {
    def bodyParsed;
    
    try {
        String filteredBody = groovy.xml.XmlUtil.serialize( body );
        filteredBody = filteredBody.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "");
        
        bodyParsed = groovy.xml.XmlUtil.serialize( "<root><httpStatusCode>" + httpStatusCode + "</httpStatusCode><statusInfo>" + statusInfo + "</statusInfo><errorResponseBody>" + filteredBody + "</errorResponseBody></root>" )
    } catch (Exception ex) {
        bodyParsed = defaultBody;
    }
    
    if (!bodyParsed) {
        bodyParsed = defaultBody;
    }
    
    return bodyParsed;
}

def Message processData(Message message) {
    def xmlParser               = new XmlParser();
    String body                 = message.getBody(String.class);
    def batchResponse           = xmlParser.parseText(body);
    def responseParts           = batchResponse.batchChangeSetResponse.batchChangeSetPartResponse;

    String statusCode, errorStatusCode, statusInfo, errorBody;
    def bErrorOccured           = false;


    for (def item in responseParts) {
        statusCode = item.statusCode.text();
        if (statusCode == "409")
            statusCode = "423"
        
        if (statusCode.startsWith("4") == true || statusCode.startsWith("5") == true) {
            if (statusCode == "423") { message.setHeader("CamelHttpResponseCode", 423); }
            bErrorOccured = true;
            
            errorStatusCode = statusCode;
            statusInfo = item.statusInfo.text();
            errorBody = item.body.text();
            
            
        }
    }
    
    if (bErrorOccured) {
        message.setProperty("BatchErrorResponse", parseBody(errorBody, errorStatusCode, statusInfo, body));
    }
    message.setProperty("ErrorInBatchDetected", bErrorOccured);

   return message;
}
