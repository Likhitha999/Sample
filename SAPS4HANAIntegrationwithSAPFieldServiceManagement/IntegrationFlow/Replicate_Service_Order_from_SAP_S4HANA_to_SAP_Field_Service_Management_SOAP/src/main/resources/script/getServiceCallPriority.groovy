import com.sap.it.api.mapping.*;
import com.sap.it.api.ITApiFactory;
import com.sap.it.api.ITApi;
import com.sap.it.api.mapping.ValueMappingApi;

def String GetServiceCallPriority(String input, MappingContext context){
    def serviceOrderPriority = input;
    def String serviceCallPriority;
    def service = ITApiFactory.getApi(ValueMappingApi.class, null);
    
    //If the service order has no priority (e.g. because it is a repair order), its priority has to be set to <null> for the mapping
    if (serviceOrderPriority == null || serviceOrderPriority == "" || serviceOrderPriority == '0') {
        serviceCallPriority = service.getMappedValue("ServiceOrderS4HANA", "ServiceDocumentPriorityS4HANA", "<null>" , "ServiceCallFSM", "PriorityFSM");
    } else {
        serviceCallPriority = service.getMappedValue('ServiceOrderS4HANA', 'ServiceDocumentPriorityS4HANA', serviceOrderPriority , 'ServiceCallFSM', 'PriorityFSM');
    }
	return serviceCallPriority;
}