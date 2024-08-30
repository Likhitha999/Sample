
import com.sap.gateway.ip.core.customdev.util.Message;
import java.net.URLEncoder;

def Message processData(Message message) {

    //Get properties 
    def map = message.getProperties();
    def ServiceOrderItemUUID = map.get("ServiceOrderKey");

    // Convert GUID
    ServiceOrderItemUUID= ServiceOrderItemUUID.replaceAll('-','');
    ServiceOrderItemUUID= ServiceOrderItemUUID.toUpperCase();
    
    // Set new properties
    message.setProperty("ServiceOrderKey", ServiceOrderItemUUID);
      
        
    return message;
}
