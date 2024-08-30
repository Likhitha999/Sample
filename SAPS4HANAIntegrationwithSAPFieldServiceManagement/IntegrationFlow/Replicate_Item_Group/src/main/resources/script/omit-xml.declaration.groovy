import com.sap.gateway.ip.core.customdev.util.Message;
import com.sap.it.api.ITApiFactory;
import com.sap.it.api.securestore.SecureStoreService;
import com.sap.it.api.securestore.UserCredential;
import javax.xml.bind.DatatypeConverter;

 

def Message processData(Message message) {

  String payload = message.getBody(java.lang.String)
  
  if(payload.contains("?xml")){
    int i = payload.indexOf('>');
    payload = payload.substring(i+1);
    message.setBody(payload);
  }
  
  return message;
}