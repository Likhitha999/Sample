import com.sap.gateway.ip.core.customdev.util.Message
import groovy.json.JsonSlurper;

def buildItemUpdateString(item){
    String ServiceOrder = item.ServiceOrder;
    String ServiceOrderItem = item.ServiceOrderItem;
    String FSMCode = item.fsmCode;

    def headers = "<method>PATCH</method><uri>A_ServiceOrderItem(ServiceOrder='${ServiceOrder}',ServiceOrderItem='${ServiceOrderItem}')</uri>";
    def body = "<A_ServiceOrderItem><A_ServiceOrderItemType><FSMServiceActivity>${FSMCode}</FSMServiceActivity><SrvcOrdItemExecutionStatus>02</SrvcOrdItemExecutionStatus></A_ServiceOrderItemType></A_ServiceOrderItem>";
    
    return "<batchChangeSetPart>" + headers + body + "</batchChangeSetPart>";
}

def buildHeaderUpdateString(header){
    String headers = "<method>PATCH</method><uri>A_ServiceOrder(ServiceOrder='${header.ServiceOrder}')</uri>";
    String body = "<A_ServiceOrder><A_ServiceOrderType><FSMServiceCall>${header.fsmCode}</FSMServiceCall></A_ServiceOrderType></A_ServiceOrder>";

    return "<batchChangeSetPart>" + headers + body + "</batchChangeSetPart>";
}

def buildBatchRequest(sRequestBody){
    if (sRequestBody == null || sRequestBody == "") {
        return null;
    } else {
        sBatchRequest = "<?xml version='1.0' encoding='UTF-8'?><batchParts><batchChangeSet>" + sRequestBody + "</batchChangeSet></batchParts>";
        return sBatchRequest;
    }
}

def getFSMActivityCode(activities, ServiceOrder, ServiceOrderItem) {
    def sActivityCode = "", aIdParts;

    for (def currActivity in activities) {
        
        aIdParts = currActivity.externalId.split("/");
        
        if (aIdParts[0] == ServiceOrder && aIdParts[aIdParts.size() - 1] == ServiceOrderItem) {
            sActivityCode = currActivity.code;
            break; 
        }
    }

    return sActivityCode;
}

def Message processData(Message message) {
    def aExternalIds = [], idItem, idHeader = null;
    def bItemUpdateNeeded = false, bHeaderUpdateNeeded = false;
    String outxml = "", batchRequest = "", batchRequestBody = "";
    
    def xmlParser               = new XmlParser();
    def XMLServiceOrder         = xmlParser.parseText(message.getProperty("SourcePayload"));
    def ServiceOrderItems       = XMLServiceOrder.ServiceOrder.Item as List;
    String sServiceOrder        = XMLServiceOrder.ServiceOrder.ServiceOrder.text();
    
    def jsonParser              = new JsonSlurper();
    def FSMResponse             = jsonParser.parseText(message.getProperty("FSMHttpResponseBody"));
    
    // determine Header Ids
    if (XMLServiceOrder.ServiceOrder.FSMServiceCall.text() == "" || XMLServiceOrder.ServiceOrder.FSMServiceCall.text() == null) {
        String sFSMServiceCallCode = FSMResponse.code;
        
        if (sServiceOrder != "" && sServiceOrder != null && sFSMServiceCallCode != "" && sFSMServiceCallCode != null) {
            bHeaderUpdateNeeded = true;
            
            idHeader = [
                ServiceOrder: sServiceOrder,
                fsmCode: sFSMServiceCallCode
            ];
        }
    }
    
    
    // determine Item Ids
    for (def item in ServiceOrderItems){
        if (item.FSMServiceActivity.text() == "" || item.FSMServiceActivity.text() == null) {
    
            idItem = [
                ServiceOrder: sServiceOrder,
                ServiceOrderItem: item.ServiceOrderItem.text(),
                fsmCode: getFSMActivityCode(FSMResponse.activities, sServiceOrder, item.ServiceOrderItem.text())
            ];

            if (idItem.fsmCode != "" && idItem.fsmCode != null && 
                idItem.ServiceOrder != "" && idItem.ServiceOrder != null &&  
                idItem.ServiceOrderItem != "" && idItem.ServiceOrderItem != null ) {
                    
                aExternalIds.push(idItem);
                bItemUpdateNeeded = true;
            }
        }
    } 
    
        
    if (bHeaderUpdateNeeded && idHeader != null) {
        batchRequestBody = batchRequestBody + buildHeaderUpdateString(idHeader);
    }

    // build batch Request
    if (bItemUpdateNeeded && aExternalIds.size() != 0) {
        for (def currId in aExternalIds ) {
            batchRequestBody = batchRequestBody + buildItemUpdateString(currId);
        }
    }
        
    batchRequest = buildBatchRequest(batchRequestBody);
    if (batchRequest) {
        outxml = groovy.xml.XmlUtil.serialize(batchRequest);
        message.setBody(outxml);
    } else {
        message.setProperty("SkipS4Update", "true");
    }
    
   return message;
}