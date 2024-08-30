import com.sap.gateway.ip.core.customdev.util.Message;
import java.util.HashMap;
import groovy.json.JsonSlurper
import groovy.xml.*

def buildBatchRequest(sRequestBody){
    if (sRequestBody == null || sRequestBody == "") {
        return null;
    } else {
        sBatchRequest = "<?xml version='1.0' encoding='UTF-8'?><batchParts><batchChangeSet>" + sRequestBody + "</batchChangeSet></batchParts>";
        return sBatchRequest;
    }
}

def buildItemUpdateString(map){
    def headers = "<method>PATCH</method><uri>A_ServiceOrderItem(ServiceOrder='" + map.get("ServiceOrder") + "',ServiceOrderItem='" + map.get("ServiceOrderItem") + "')</uri>";
    String body = filterXMLTag(map.get("updateItemPayload") as String);
    
    return "<batchChangeSetPart>" + headers + body + "</batchChangeSetPart>";
}

def buildRemoveRemarksString (map) {
    def headers = "<method>DELETE</method><uri>A_ServiceOrderItemText(ServiceOrder='" + map.get("ServiceOrder") + "',ServiceOrderItem='" + map.get("ServiceOrderItem") + "',Language='" +  map.get("TextLangu") + "',LongTextID='" + map.get("ItemLongTextRemarkType") + "')</uri>";
    return "<batchChangeSetPart>" + headers + "</batchChangeSetPart>";
}

def buildAddRemarksString (map) {
    def headers = "<method>PUT</method><uri>A_ServiceOrderItemText(ServiceOrder='" + map.get("ServiceOrder") + "',ServiceOrderItem='" + map.get("ServiceOrderItem") + "',Language='" +  map.get("TextLangu") + "',LongTextID='" + map.get("ItemLongTextRemarkType") + "')</uri>";
    String body = filterXMLTag(map.get("updateItemRemarkPayload")); 
    return "<batchChangeSetPart>" + headers + body + "</batchChangeSetPart>";
}

def buildRemoveRefObjectString (map) {
    def headers = "<method>DELETE</method><uri>A_ServiceOrderItemRefObject(ServiceOrder='" + map.get("ServiceOrder") + "',ServiceOrderItem='" + map.get("ServiceOrderItem") + "',ServiceReferenceEquipment='" +  map.get("CurrentEquipment") + "',ServiceRefFunctionalLocation='" + map.get("CurrentFunctionalLocation") + "')</uri>";
    return "<batchChangeSetPart>" + headers + "</batchChangeSetPart>";
}

def buildAddRefObjectString (map) {
    def headers = "<method>POST</method><uri>A_ServiceOrderItemRefObject</uri>";
    def body = '''
    <A_ServiceOrderItemRefObject>
        <A_ServiceOrderItemRefObjectType>
            <ServiceOrder>''' + map.get("ServiceOrder") + '''</ServiceOrder>
            <ServiceOrderItem>''' + map.get("ServiceOrderItem") + '''</ServiceOrderItem>
            <ServiceReferenceEquipment>''' + map.get("NewEquipment") + '''</ServiceReferenceEquipment>
            <ServiceRefFunctionalLocation>''' + map.get("NewFunctionalLocation") + '''</ServiceRefFunctionalLocation>
            <SrvcRefObjIsMainObject>true</SrvcRefObjIsMainObject>
        </A_ServiceOrderItemRefObjectType>
    </A_ServiceOrderItemRefObject>'''; 
    
    return "<batchChangeSetPart>" + headers + body + "</batchChangeSetPart>";
}

def filterXMLTag (body) {
    String filteredBody = body;
    filteredBody = filteredBody.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "")
    
    return filteredBody;
}

def Message processData(Message message) {
    def body = message.getBody();
    def map = message.getProperties();
    def batchRequestBody = "";
    
    // Build Remark Payload
    if (map.get("isItemRemarkUpdated") == true && map.get("isItemRemarkDeleted") == false) {
        batchRequestBody = batchRequestBody + buildAddRemarksString(map);
    } else if (map.get("isItemRemarkUpdated") == true && map.get("isItemRemarkDeleted") == true) {
        batchRequestBody = batchRequestBody + buildRemoveRemarksString(map);
    }
    
    if (map.get("SendRequest_DeleteReferenceObject") == true) {
        batchRequestBody = batchRequestBody + buildRemoveRefObjectString(map);
    }
    
    if (map.get("SendRequest_AddReferenceObject") == true) {
        batchRequestBody = batchRequestBody + buildAddRefObjectString(map);
    }
    
    if (map.get("isItemUpdated") == true || (map.get("ExtensionEnabled") ==~ /(?i)(true|x)/) == true) {
        batchRequestBody = batchRequestBody + buildItemUpdateString(map);
    }
    
    message.setBody(buildBatchRequest(batchRequestBody));
    message.setProperty("RequestPayload", buildBatchRequest(batchRequestBody));
    
    return message;
}