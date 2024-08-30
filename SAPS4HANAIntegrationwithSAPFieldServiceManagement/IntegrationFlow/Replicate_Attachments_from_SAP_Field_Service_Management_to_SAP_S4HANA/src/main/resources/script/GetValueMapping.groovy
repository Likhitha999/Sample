/*

 */
import com.sap.gateway.ip.core.customdev.util.Message;
import com.sap.it.api.ITApiFactory;
import com.sap.it.api.msglog.MessageLog;
import com.sap.it.api.mapping.ValueMappingApi;



def Message processData(Message message) {

    String targetValue = null;
 	//Get Headers 
	def map = message.getHeaders();
	def FSMContentType = map.get("FSMContentType");
 
    final valueMapApi = ITApiFactory.getApi(ValueMappingApi.class, null)
 
    targetValue = valueMapApi.getMappedValue('FSM', 'AttachmentType', FSMContentType, 'S4HANA','MimeCode')
 
    if(targetValue==null || targetValue.isEmpty() ){
 
                throw new IllegalStateException("The integration flow message won’t be processed further, as there is no ValueMapping maintained for Content Type::"+FSMContentType)
    }
 
    message.setHeader("Content-Type", targetValue);
 
    // encode unicode characters in header Slug (filename)
    def slugOrg = message.getProperty("Slug")
    def slugEncoded = URLEncoder.encode(slugOrg.toString(), "UTF-8")
    message.setProperty("SlugOrg", slugOrg)
    message.setProperty("Slug", slugEncoded)
    message.setHeader("Slug", slugEncoded);
    
    return message;
}
