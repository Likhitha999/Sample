import com.sap.gateway.ip.core.customdev.util.Message;
import java.util.HashMap;
import groovy.util.XmlParser;

def Message processData(Message message) {

// To access FSM, company and account are required -- either their name or their ID.
// If the IDs are configured, they are used; otherwise, the names are used.
    def accountID = message.getProperty('X-Account-ID');
    def accountName = message.getProperty('X-Account-Name');
    def companyID = message.getProperty('X-Company-ID');
    def companyName = message.getProperty('X-Company-Name');

    if (accountID == null || accountID == '' || accountID == '<FSM_Account_ID>' || companyID == null || companyID == '' || companyID == '<FSM_Company_ID>') {
        if (accountName == null || accountName == '' || accountName == '<FSM Account Name>' || companyName == null || companyName == '' || companyName == '<FSM Company Name>') {
            throw new Exception ("The integration flow message won’t be processed further. Configuration error: configure either FSMM Account ID and FSM Company ID or FSM Account Name and FSM Company Name");
        }
        message.setHeader('X-Account-Name', accountName);
        message.setHeader('X-Company-Name', companyName);
    } else {
        message.setHeader('X-Account-ID', accountID);
        message.setHeader('X-Company-ID', companyID);
    }

    return message;
}