
import com.sap.gateway.ip.core.customdev.util.Message;


def Message processData(Message message) {

    //Get properties 
    def map = message.getProperties();
    def AttachmentExternalId = map.get("AttachmentExternalId");
    def ActivityExternalId   = map.get("ActivityExternalId");

    // Split Value
    if (AttachmentExternalId) {
        ArchiveDocumentID = AttachmentExternalId.tokenize("/")[0];
        LogicalDocument   = AttachmentExternalId.tokenize("/")[1];
    };
  
    if (ActivityExternalId) {  
        ServiceOrder     = ActivityExternalId.tokenize("/")[0];
        ServiceOrderItem = ActivityExternalId.tokenize("/")[1];
    };
    
    // Set new properties
    if (AttachmentExternalId) {
        message.setProperty("LogicalDocument", LogicalDocument);
        message.setProperty("ArchiveDocumentID", ArchiveDocumentID);
    };
    
    if (ActivityExternalId) {
        message.setProperty("ServiceOrder",     ServiceOrder);
        message.setProperty("ServiceOrderItem", ServiceOrderItem);
    };
  
// To access FSM, company and account are required -- either their name or their ID.
// If the IDs are configured, they are used; otherwise, the names are used.
    def accountCompany = '';
    def accountID = message.getProperty('X-Account-ID');
    def accountName = message.getProperty('X-Account-Name');
    def companyID = message.getProperty('X-Company-ID');
    def companyName = message.getProperty('X-Company-Name');

    if (accountID == null || accountID == '' || accountID == '<FSM_Account_ID>' || companyID == null || companyID == '' || companyID == '<FSM_Company_ID>') {
        accountCompany = '&account='+accountName+'&company='+companyName;
        message.setHeader('X-Account-Name', accountName);
        message.setHeader('X-Company-Name', companyName);
    } else {
        accountCompany = '';
        message.setHeader('X-Account-ID', accountID);
        message.setHeader('X-Company-ID', companyID);
    }
    message.setProperty('AccountCompany', accountCompany);
    
    return message;
}
