import com.sap.it.api.mapping.*;
import com.sap.it.api.ITApiFactory;
import com.sap.it.api.ITApi;
import com.sap.it.api.mapping.ValueMappingApi;

def String GetServiceCallType(String input, MappingContext context){
    def serviceOrderType = input;
    def service = ITApiFactory.getApi(ValueMappingApi.class, null);
    String serviceCallType = service.getMappedValue('ServiceOrderS4HANA', 'TransactionType', serviceOrderType , 'ServiceCallFSM', 'Type');
    
    //<blank> is used as a placeholder in the ValueMapping for a null or initial value as such values can't be maintained directly in the Value Mapping
    if (serviceCallType == "<null>") {
        serviceCallType = null;
    }
    
	return serviceCallType; 
}