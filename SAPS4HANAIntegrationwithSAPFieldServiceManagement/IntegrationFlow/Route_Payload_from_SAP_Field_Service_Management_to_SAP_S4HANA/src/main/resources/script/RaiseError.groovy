import com.sap.gateway.ip.core.customdev.util.Message
import java.util.HashMap

def Message processData(Message message) {
    def sErrorMessage = message.getProperty("checkErrorMessage");
    
    if (!sErrorMessage) {
        sErrorMessage = 'The integration flow message won’t be processed further due to a incorrect payload.';
    }
    
    throw new Exception(sErrorMessage);
}