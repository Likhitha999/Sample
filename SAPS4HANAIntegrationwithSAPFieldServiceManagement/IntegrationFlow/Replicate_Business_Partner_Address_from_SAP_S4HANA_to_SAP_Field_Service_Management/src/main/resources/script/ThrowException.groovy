import com.sap.gateway.ip.core.customdev.util.Message;

def Message processData(Message message) {
    throw new Exception("Response from FSM in Process Direct iFlow contains error, check ResponseBody in Attachments of Replicate Business Partner Address for error details");
}
