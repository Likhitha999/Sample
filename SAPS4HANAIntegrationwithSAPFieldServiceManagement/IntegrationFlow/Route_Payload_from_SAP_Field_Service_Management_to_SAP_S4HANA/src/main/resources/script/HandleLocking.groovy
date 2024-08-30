import com.sap.gateway.ip.core.customdev.util.Message;
import java.util.HashMap;
def Message processData(Message message) {
    def map = message.getHeaders()
    
    def http_status = map.get("CamelHttpResponseCode");
    def consider409AsLock = (message.getProperty("Consider409Locking") ==~ /(?i)(true|x)/);
    
    if ( (consider409AsLock && http_status == 409) || http_status == 423) {
        message.setHeader("CamelHttpResponseCode", 423)
    } else {
        message.setHeader("CamelHttpResponseCode", 500)
    }
    
    return message
}
