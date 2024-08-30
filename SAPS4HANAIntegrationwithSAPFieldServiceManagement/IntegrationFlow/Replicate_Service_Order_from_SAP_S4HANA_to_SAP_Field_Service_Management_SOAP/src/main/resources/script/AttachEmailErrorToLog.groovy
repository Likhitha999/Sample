import com.sap.gateway.ip.core.customdev.util.Message;

// save email sending error as attachment
def Message processData(Message message) {
    def body = message.getBody();
    if ( body.contains('ERROR: ErrorSendingEmail') ) {
        def messageLog = messageLogFactory.getMessageLog(message)
        messageLog.addAttachmentAsString('ErrorSendingEmail', body, 'text/plain')    
    }
    return message;
}