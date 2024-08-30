import com.sap.gateway.ip.core.customdev.util.Message;

def Message processData(Message message) {
    throw new Exception("Response from FSM contains error, check ResponseBody in Attachments for error details");
}
