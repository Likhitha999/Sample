import com.sap.gateway.ip.core.customdev.util.Message
import java.util.HashMap;
import groovy.util.XmlParser;

def parseBody (body, httpStatusCode, statusInfo, defaultBody) {
    def bodyParsed;
    
    try {
        String filteredBody = groovy.xml.XmlUtil.serialize( body );
        filteredBody = filteredBody.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "");
        
        bodyParsed = groovy.xml.XmlUtil.serialize( "<root><httpStatusCode>" + httpStatusCode + "</httpStatusCode><statusInfo>" + statusInfo + "</statusInfo><errorResponseBody>" + filteredBody + "</errorResponseBody></root>" )
    } catch (Exception ex) {
        bodyParsed = defaultBody;
    }
    
    if (!bodyParsed) {
        bodyParsed = defaultBody;
    }
    
    return bodyParsed;
}

def Message processData(Message message) {
    def xmlParser               = new XmlParser();
    String body                 = message.getBody(String.class);
    def batchResponse           = xmlParser.parseText(body);
    def responseParts           = batchResponse.batchChangeSetResponse.batchChangeSetPartResponse;
    def activityExternalId      = null;
    def bErrorOccured           = false;
    def dErrorOccured           = false;
    
    String statusCode, errorStatusCode, statusInfo, errorBody;
    
    for (def item in responseParts) {
        statusCode = item.statusCode.text();
        ServiceOrderId = item.body.ServiceOrder.text();
        ServiceOrderItem = item.body.ServiceOrderItem.text();
        
        if (statusCode.startsWith("4") == true || statusCode.startsWith("5") == true) {
            errorStatusCode = item.statusCode.text();
            statusInfo = item.statusInfo.text();
            errorBody = item.body.text();
            if (errorBody){
                try {
                ErrorMsgCode = xmlParser.parseText(errorBody).innererror.errordetails;
                for (def eitem in ErrorMsgCode.errordetail) {
                        if (eitem.code.text() == "CRMS4_SERV_UI/132") {
                            dErrorOccured = true;
                            ServiceOrderItem = (eitem.message.text() =~ /[1-9][0-9]*/).findAll()[0];
                        }
                        if (eitem.code.text() == "CRMS4_SERV_UI/131") {
                            ServiceOrderId = (eitem.message.text() =~ /[1-9][0-9]*/).findAll()[0];
                        }
                    }
                } catch (Exception ex) {};
            }
            if (dErrorOccured == false){
                if (statusCode == "423") { message.setHeader("CamelHttpResponseCode", 423); }
                bErrorOccured = true;
            }
        }
            
        if (ServiceOrderId != "" && ServiceOrderId != null && ServiceOrderItem != "" && ServiceOrderItem != null) {
            activityExternalId = ServiceOrderId + '/' + ServiceOrderItem;
        }
    }
   
    if (bErrorOccured == true) {
        message.setProperty("BatchErrorResponse", parseBody(errorBody, errorStatusCode, statusInfo, body));
    }
    
    if (activityExternalId) {
        message.setProperty("ActivityExternalId", activityExternalId);
    } else {
        bErrorOccured = true;
        message.setProperty("ErrorMessage", "The integration flow message won’t be processed further. Activity externalId could not be determined.");
    }
    
    message.setProperty("ErrorInBatchDetected", bErrorOccured);
    
// To access FSM, company and account are required -- either their name or their ID.
// If the IDs are configured, they are used; otherwise, the names are used.
    def accountCompanyUser = '';
    def accountID = message.getProperty('X-Account-ID');
    def accountName = message.getProperty('X-Account-Name');
    def companyID = message.getProperty('X-Company-ID');
    def companyName = message.getProperty('X-Company-Name');
    def userName = message.getProperty('UserName');

    if (accountID == null || accountID == '' || accountID == '<FSM_Account_ID>' || companyID == null || companyID == '' || companyID == '<FSM_Company_ID>') {
        accountCompanyUser = '&account='+accountName+'&company='+companyName+'&user='+userName;
        message.setHeader('X-Account-Name', accountName);
        message.setHeader('X-Company-Name', companyName);
    } else {
        accountCompanyUser = '';
        message.setHeader('X-Account-ID', accountID);
        message.setHeader('X-Company-ID', companyID);
    }
    message.setProperty('AccountCompanyUser', accountCompanyUser);
    
    return message;
}
