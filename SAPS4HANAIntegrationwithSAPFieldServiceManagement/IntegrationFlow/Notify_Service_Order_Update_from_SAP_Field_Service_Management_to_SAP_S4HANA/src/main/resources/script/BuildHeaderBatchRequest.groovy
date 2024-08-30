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

def buildHeaderUpdateString(map){
    def headers = "<method>PATCH</method><uri>A_ServiceOrder(ServiceOrder='" + map.get("ServiceOrder") + "')</uri>";
    String body = filterXMLTag(map.get("updateHeaderPayload") as String);
    
    return "<batchChangeSetPart>" + headers + body + "</batchChangeSetPart>";
}

def buildRemoveRemarksString (map) {
    def headers = "<method>DELETE</method><uri>A_ServiceOrderText(ServiceOrder='" + map.get("ServiceOrder") + "',Language='" +  map.get("TextLangu") + "',LongTextID='" + map.get("LongTextRemarkType") + "')</uri>";
    return "<batchChangeSetPart>" + headers + "</batchChangeSetPart>";
}

def buildAddRemarksString (map) {
    def headers = "<method>PUT</method><uri>A_ServiceOrderText(ServiceOrder='" + map.get("ServiceOrder") + "',Language='" +  map.get("TextLangu") + "',LongTextID='" + map.get("LongTextRemarkType") + "')</uri>";
    String body = filterXMLTag(map.get("updateRemarkPayload")); 
    return "<batchChangeSetPart>" + headers + body + "</batchChangeSetPart>";
}

def buildRemoveResolutionString (map) {
    def headers = "<method>DELETE</method><uri>A_ServiceOrderText(ServiceOrder='" + map.get("ServiceOrder") + "',Language='" +  map.get("TextLangu") + "',LongTextID='" + map.get("LongTextResolutionType") + "')</uri>";
    return "<batchChangeSetPart>" + headers + "</batchChangeSetPart>";
}

def buildAddResolutionString (map) {
    def headers = "<method>PUT</method><uri>A_ServiceOrderText(ServiceOrder='" + map.get("ServiceOrder") + "',Language='" +  map.get("TextLangu") + "',LongTextID='" + map.get("LongTextResolutionType") + "')</uri>";
    String body = filterXMLTag(map.get("updateResolutionPayload")); 
    return "<batchChangeSetPart>" + headers + body + "</batchChangeSetPart>";
}

def buildRemoveRefObjectString (map) {
    def headers = "<method>DELETE</method><uri>A_ServiceOrderRefObject(ServiceOrder='" + map.get("ServiceOrder") + "',ServiceReferenceEquipment='" +  map.get("CurrentEquipment") + "',ServiceRefFunctionalLocation='" + map.get("CurrentFunctionalLocation") + "')</uri>";
    return "<batchChangeSetPart>" + headers + "</batchChangeSetPart>";
}

def buildAddRefObjectString (map) {
    def headers = "<method>POST</method><uri>A_ServiceOrderRefObject</uri>";
    def body = '''
    <A_ServiceOrderRefObject>
        <A_ServiceOrderRefObjectType>
            <ServiceOrder>''' + map.get("ServiceOrder") + '''</ServiceOrder>
            <ServiceReferenceEquipment>''' + map.get("NewEquipment") + '''</ServiceReferenceEquipment>
            <ServiceRefFunctionalLocation>''' + map.get("NewFunctionalLocation") + '''</ServiceRefFunctionalLocation>
            <SrvcRefObjIsMainObject>true</SrvcRefObjIsMainObject>
        </A_ServiceOrderRefObjectType>
    </A_ServiceOrderRefObject>'''; 
    
    return "<batchChangeSetPart>" + headers + body + "</batchChangeSetPart>";
}

def buildRemoveResponsibleString (map) {
    def headers = "<method>DELETE</method><uri>A_ServiceOrderPersonResp(ServiceOrder='" + map.get("ServiceOrder") + "',PersonResponsible='" + map.get("CurrentResponsible") + "')</uri>";
    return "<batchChangeSetPart>" + headers + "</batchChangeSetPart>";
}

def buildAddResponsibleString (map) {
    def headers = "<method>POST</method><uri>A_ServiceOrderPersonResp</uri>";
    def body = '''
    <A_ServiceOrderPersonResp>
        <A_ServiceOrderPersonRespType>
            <ServiceOrder>''' + map.get("ServiceOrder") + '''</ServiceOrder>
            <PersonResponsible>''' + map.get("NewResponsible") + '''</PersonResponsible>
        </A_ServiceOrderPersonRespType>
    </A_ServiceOrderPersonResp>'''; 
    
    return "<batchChangeSetPart>" + headers + body + "</batchChangeSetPart>";
}

def filterXMLTag (body) {
    String filteredBody = body;
    filteredBody = filteredBody.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "")
    
    return filteredBody;
}

def Message processData(Message message) {
    def map = message.getProperties();
    def batchRequestBody = "";
    
    if (map.get("SendRequest_DeleteResponsible") == true) {
        batchRequestBody = batchRequestBody + buildRemoveResponsibleString(map);
    }
    
    if (map.get("SendRequest_AddResponsible") == true) {
        batchRequestBody = batchRequestBody + buildAddResponsibleString(map);
    }
    
    if (map.get("SendRequest_DeleteReferenceObject") == true) {
        batchRequestBody = batchRequestBody + buildRemoveRefObjectString(map);
    }
    
    if (map.get("SendRequest_AddReferenceObject") == true) {
        batchRequestBody = batchRequestBody + buildAddRefObjectString(map);
    }
    
    if (map.get("isHeaderRemarkUpdated") == true && map.get("isHeaderRemarkDeleted") == false) {
        batchRequestBody = batchRequestBody + buildAddRemarksString(map);
    } else if (map.get("isHeaderRemarkUpdated") == true && map.get("isHeaderRemarkDeleted") == true) {
        batchRequestBody = batchRequestBody + buildRemoveRemarksString(map);
    }
    
    if (map.get("isHeaderResolutionUpdated") == true && map.get("isHeaderResolutionDeleted") == false) {
        batchRequestBody = batchRequestBody + buildAddResolutionString(map);
    } else if (map.get("isHeaderResolutionUpdated") == true && map.get("isHeaderResolutionDeleted") == true) {
        batchRequestBody = batchRequestBody + buildRemoveResolutionString(map);
    }
    
    if (map.get("SendRequest_UpdateHeader") == true || (map.get("HeaderExtensionEnabled") ==~ /(?i)(true|x)/) == true) {
        batchRequestBody = batchRequestBody + buildHeaderUpdateString(map);
    }
    
    message.setBody(buildBatchRequest(batchRequestBody));
    message.setProperty("RequestPayload", buildBatchRequest(batchRequestBody));
    
    return message;
}