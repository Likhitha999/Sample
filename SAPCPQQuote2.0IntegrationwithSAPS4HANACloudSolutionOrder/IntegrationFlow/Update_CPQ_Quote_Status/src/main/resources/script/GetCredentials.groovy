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
    def CPQCredentials = map.get("CPQCredentials");
    
    String cpqDomainFromProps = map.get("CPQ_Domain");
    
  def service = ITApiFactory.getApi(SecureStoreService.class, null);
  def credential = service.getUserCredential(CPQCredentials);
  
  if (credential == null){
    String error = "No credential found for alias" + CPQCredentials;
    throw new IllegalStateException(error);
  }
  
  String user = credential.getUsername();
    String cpqDomain = null;
    String cpqUsername = null;
    int index = user.indexOf("#");
    if (index == -1){
        cpqUsername = user;
    } else {
        cpqDomain = user.substring(index + 1);
        cpqUsername = user.substring(0,index);
    }
    String cpqPassword = new String(credential.getPassword());
    
    
    def body = message.getBody();
    body = "grant_type=password";
    body = body + "&username=" + cpqUsername;
    body = body + "&password=" + cpqPassword;
    if(cpqDomain != null && cpqDomain != ""){
        body = body + "&domain=" + cpqDomain;
    } else if (cpqDomainFromProps != null && cpqDomainFromProps != "") {
        body = body + "&domain=" + cpqDomainFromProps;
    }
    message.setBody(body);
    
    return message;
}