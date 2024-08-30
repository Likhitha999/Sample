import com.sap.gateway.ip.core.customdev.util.Message

def Message processData(Message message) {

    if (message.getProperty("ErrorInBatchDetected")) {
        throw new Exception("The integration flow message won’t be processed further due to an error returned by the SAP S/4HANA OData API. For error details, see the batch response body in the error log attachments.");
    }

   return message;
}
