import com.sap.gateway.ip.core.customdev.util.Message;
import java.util.HashMap;
import org.json.simple.*;
import groovy.json.*;
import javax.xml.bind.DatatypeConverter;
import com.sap.it.api.ITApiFactory;
import com.sap.it.api.securestore.SecureStoreService;
import com.sap.it.api.securestore.UserCredential;
import java.lang.*;

def Message processData(Message message) {
    
    def map = message.getProperties();
    String cpqDomain = map.get("CPQ_Domain");
    def body = message.getBody();
    
    body = "grant_type=client_credentials";
    
    if(cpqDomain != ""){
        body = body + "&domain=" + cpqDomain;
    }
    message.setBody(body);
    
    return message;
}