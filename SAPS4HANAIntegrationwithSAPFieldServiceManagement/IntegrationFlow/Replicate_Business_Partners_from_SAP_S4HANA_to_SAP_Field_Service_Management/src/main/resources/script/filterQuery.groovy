import com.sap.gateway.ip.core.customdev.util.Message;

def Message processData(Message message) {
    def Body = message.getBody(java.lang.String) as String;
    Body = Body.trim();
    String ODataQuery = Body.replaceAll("\n", "or ").replaceAll(" ",'%20' );
    message.setProperty("filterQuery", ODataQuery);
    message.setBody('')
    return message;
    
}