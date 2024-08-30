import com.sap.gateway.ip.core.customdev.util.Message
// Version 1.0
// save attachments to log and build error email body
def Message processData(Message message) {
    def map = message.getProperties()
    def headers = message.getHeaders()
    def add_to_log_property = message.getProperty("ErrorLogAttachments") ?: 'false'
    def add_to_log = (add_to_log_property ==~ /(?i)(true|x)/)
    def email_body = ''
    def headers_text = ''
    def response = message.getProperty(message.getProperty("CamelSplitIndex")+"http.ResponseBody")
    
    if (response != null) {
        // fill only minimal information into email body
        email_body = "MPL ID: " + map.get("SAP_MessageProcessingLogID") + "\n" 
        email_body += "Correlation ID: " + headers.get("SAP_MplCorrelationId") + "\n" 
        email_body += "Timestamp: " + map.get("CamelCreatedTimestamp") + "\n" 

        // log the error in FSM response as an attachment
        if (add_to_log) {
               // save the http error response as a log attachment
                def messageLog = messageLogFactory.getMessageLog(message)
                messageLog.addAttachmentAsString('ResponseBody '+message.getProperty("CamelSplitIndex"), response, 'text/plain')
        }
        if (add_to_log) {
            // add headers
            headers.each{ k, v -> 
                headers_text += "${k}: ${v}" + "\n" 
            }
            // save headers as a log attachment
            def messageLog2 = messageLogFactory.getMessageLog(message)
            messageLog2.addAttachmentAsString('Headers', headers_text, 'text/plain')
            // save source payload as a log attachment
            if( map.get("OriginalPayload") != null && map.get("OriginalPayload") != '' ) {
                def messageLog3 = messageLogFactory.getMessageLog(message)
                messageLog3.addAttachmentAsString('OriginalPayload', map.get("OriginalPayload"), 'text/plain')
            }
            // save payload as a log attachment
            if( map.get("RequestPayload"+message.getProperty("CamelSplitIndex")) != null && map.get("RequestPayload"+message.getProperty("CamelSplitIndex")) != '' ) {
                def messageLog4 = messageLogFactory.getMessageLog(message)
                messageLog4.addAttachmentAsString('RequestPayload '+message.getProperty("CamelSplitIndex"), map.get("RequestPayload"+message.getProperty("CamelSplitIndex")), 'text/plain')
            }
        }
    }

    message.setBody(email_body)
    return message
}