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

    // get an exception java class instance
    def ex = map.get('CamelExceptionCaught')
    if (ex != null) {
        // fill only minimal information into email body
        email_body = "MPL ID: " + map.get("SAP_MessageProcessingLogID") + "\n" 
        email_body += "Correlation ID: " + headers.get("SAP_MplCorrelationId") + "\n" 
        email_body += "Timestamp: " + map.get("CamelCreatedTimestamp") + "\n" 

        exmap = ex.getProperties()
        // an http adapter throws an instance of org.apache.camel.component.ahc.AhcOperationFailedException
        if (ex.getClass().getCanonicalName().equals('org.apache.camel.component.ahc.AhcOperationFailedException')) {
            if (add_to_log) {
               // save the http error response as a log attachment
                def messageLog = messageLogFactory.getMessageLog(message)
                messageLog.addAttachmentAsString('ResponseBody', ex.getResponseBody(), 'text/plain')
            }
            message.setProperty('http.StatusCode', ex.getStatusCode())
            message.setProperty('http.StatusText', ex.getStatusText())
            message.setProperty('http.ResponseBody', ex.getResponseBody())
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
            if( map.get("RequestPayload") != null && map.get("RequestPayload") != '' ) {
                def messageLog4 = messageLogFactory.getMessageLog(message)
                messageLog4.addAttachmentAsString('RequestPayload', map.get("RequestPayload"), 'text/plain')
            }
            // save payload as a log attachment
            if (map.get("BatchErrorResponse") != null && map.get("BatchErrorResponse") != '' ) {
                def messageLog5 = messageLogFactory.getMessageLog(message)
                messageLog5.addAttachmentAsString('BatchResponseBody', map.get("BatchErrorResponse"), 'text/plain')
            }
        }
    }

    message.setBody(email_body)
    return message
}