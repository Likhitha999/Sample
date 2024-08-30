import com.sap.gateway.ip.core.customdev.util.Message
import groovy.util.XmlParser;
import groovy.util.XmlSlurper;
import com.sap.it.api.mapping.ValueMappingApi;
import com.sap.it.api.ITApiFactory;

// process message
def Message processData(Message message) {
    def body = message.getBody(String.class);
	def object = new XmlParser().parseText(body);
    def serviceCallUpdatePayload;
    
    def service = ITApiFactory.getApi(ValueMappingApi.class, null);

	String ServiceOrderId   = object.A_ServiceOrderType.ServiceOrder.text();
	String ServiceCallCode  = message.getProperty("FSMServiceCall");
	String NewStatus       = service.getMappedValue("S4HANA", "ServiceOrderStatus", "RELEASEREVOKED", "FSM", "ServiceCallStatusCode") ?: "";
	
    serviceCallUpdatePayload = '''{"externalId":"''' + ServiceOrderId + '''", "code": "''' + ServiceCallCode + '''", "status": "''' + NewStatus + '''"}'''

    message.setBody(serviceCallUpdatePayload);
    
    return message
}