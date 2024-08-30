import com.sap.gateway.ip.core.customdev.util.Message;
import java.util.HashMap;
import org.json.simple.*;
import groovy.json.*;
import javax.xml.bind.DatatypeConverter;
import com.sap.it.api.ITApiFactory;
import com.sap.it.api.securestore.SecureStoreService;
import com.sap.it.api.securestore.UserCredential;
import java.lang.*;

def Message setAccessTokenBody(Message message) {
    
    def map = message.getProperties();
    def CPQCredentials = map.get("CPQCredentials");
    
    def service = ITApiFactory.getApi(SecureStoreService.class, null);
    def credential = service.getUserCredential(CPQCredentials);
    
    if (credential == null){
        String error = "No credential found for alias" + CPQCredentials;
        throw new IllegalStateException(error);
    }
    
    String user = credential.getUsername();
    String cpqDomain = null;
    String cpqUsername = null;
    int index = user.indexOf("#");
    if (index == -1){
        cpqUsername = user;
    } else {
        cpqDomain = user.substring(index + 1);
        cpqUsername = user.substring(0,index);
    }
    String cpqPassword = new String(credential.getPassword());
    
    
    def body = message.getBody();
    body = "grant_type=password";
    body = body + "&username=" + cpqUsername;
    body = body + "&password=" + cpqPassword;
    if(cpqDomain != null){
        body = body + "&domain=" + cpqDomain;
    }
    message.setBody(body);
    
    return message;
}

def Message ExtractIdentifierForSearch(Message message){
	def map = message.getProperties();
    
    def xmlSlurper = new XmlSlurper();
    def body = message.getBody(String.class);
	def root = xmlSlurper.parseText(body);
	
	message.setProperty("BP_ExternalId", "0");
	message.setProperty("BP_PartnerId", "0");
	
	def externalId = root.Customer.InternalID.text();
	def partnerId = root.InternalID.text();	
	
	if(!externalId.isEmpty()){
	    message.setProperty("BP_SearchIdentifier", "externalId");
	    message.setProperty("BP_SearchIdentifier_Value", externalId);
	    message.setProperty("BP_ExternalId", externalId);
	} 
	if(!partnerId.isEmpty()){
	    message.setProperty("BP_SearchIdentifier", "partnerId");
	    message.setProperty("BP_SearchIdentifier_Value", partnerId);
	    message.setProperty("BP_PartnerId", partnerId);
	} 
	if(externalId.isEmpty() && partnerId.isEmpty()){
	    message.setProperty("BP_SearchIdentifier", "partnerId");
	    message.setProperty("BP_SearchIdentifier_Value", "0");
	    message.setProperty("BP_ExternalId", "0");
	    message.setProperty("BP_PartnerId", "0");
	}
	
	return message;
}

def Message AdjustExternalIdValue(Message message){
	def map = message.getProperties();
    def externalId = map.get("BP_ExternalId");
    
	if(externalId == null || externalId == ''){
	    message.setProperty("BP_ExternalId", ' ');
	}
	
	return message;
}

def Message SetBusinessPartnerIdAndSystemIdFromResponse(Message message){
    def body = message.getBody(String.class);
	def messageLog = messageLogFactory.getMessageLog(message);
	
	messageLog.setStringProperty("Logging#body", body);
	
	def root = new XmlSlurper().parseText(body);
	messageLog.setStringProperty("Logging#rootXml", root.toString());
	
	def numberOfRecords = root.totalNumberOfRecords.text().toInteger();
	messageLog.setStringProperty("Logging#numberOfRecords", numberOfRecords.toString());
	
	if(numberOfRecords == 0){
	    message.setProperty("BP_Id", "0");
	    messageLog.setStringProperty("Logging#BP_Id", "No BP found.");
	}else if(numberOfRecords == 1){
	    def bpId = root.pagedRecords.id.text();
	    def bpSystemId = root.pagedRecords.systemId.text();
	    message.setProperty("BP_Id", bpId);
	    message.setProperty("BP_SystemId", bpSystemId);
	}else{
	    String error = "Returned multiple Business Partners for the same External ID or Partner ID. ";
        throw new IllegalStateException(error);
	}
	messageLog.setStringProperty("Logging#method_end", "SetBusinessPartnerIdFromResponse finished successfully.");
	return message;
}









