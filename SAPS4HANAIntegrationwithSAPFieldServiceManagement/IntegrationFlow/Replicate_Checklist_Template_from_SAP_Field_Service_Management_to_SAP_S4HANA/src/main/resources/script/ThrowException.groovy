import com.sap.gateway.ip.core.customdev.util.Message;

def Message processData(Message message) {
    throw new Exception("Response from S4 contains error, check ResponseBody in Attachments for error details");
}
