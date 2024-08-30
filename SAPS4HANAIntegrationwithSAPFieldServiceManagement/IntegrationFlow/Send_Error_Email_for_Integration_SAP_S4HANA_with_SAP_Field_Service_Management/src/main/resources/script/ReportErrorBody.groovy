import com.sap.gateway.ip.core.customdev.util.Message

// return error in sending email in the body to the caller  
def Message processData(Message message) {
    def map = message.getProperties()
    def error_body = ''

    // get an exception java class instance
    def ex = map.get('CamelExceptionCaught')
    if (ex != null) {
        error_body = 'ERROR: ErrorSendingEmail'  + "\n" 
        error_body += 'Error in Send Email Notifiaction Subprocess'  + "\n" 
        exmap = ex.getProperties()
        error_body += "Exception Class: " + exmap.get("class") + "\n" 
        error_body += "Message: " + exmap.get("message") + "\n"
    }

    message.setBody(error_body)
    return message
}