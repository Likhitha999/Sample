/*
 The integration developer needs to create the method processData 
 This method takes Message object of package com.sap.gateway.ip.core.customdev.util 
which includes helper methods useful for the content developer:
The methods available are:
    public java.lang.Object getBody()
	public void setBody(java.lang.Object exchangeBody)
    public java.util.Map<java.lang.String,java.lang.Object> getHeaders()
    public void setHeaders(java.util.Map<java.lang.String,java.lang.Object> exchangeHeaders)
    public void setHeader(java.lang.String name, java.lang.Object value)
    public java.util.Map<java.lang.String,java.lang.Object> getProperties()
    public void setProperties(java.util.Map<java.lang.String,java.lang.Object> exchangeProperties) 
    public void setProperty(java.lang.String name, java.lang.Object value)
    public java.util.List<com.sap.gateway.ip.core.customdev.util.SoapHeader> getSoapHeaders()
    public void setSoapHeaders(java.util.List<com.sap.gateway.ip.core.customdev.util.SoapHeader> soapHeaders) 
       public void clearSoapHeaders()
 */
import com.sap.gateway.ip.core.customdev.util.Message;
import java.util.HashMap;
import java.lang.*;

def Message processData(Message message) 
{
	def body = message.getBody(String.class);
	def messageLog = messageLogFactory.getMessageLog(message);
	//def properties = (Map<String, Object>) message.getProperties();
	def xmlSlurper = new XmlSlurper();
	def root = xmlSlurper.parseText(body);
	
	def phoneNumList = root.BusinessPartnerSUITEReplicateRequestMessage.BusinessPartner.AddressInformation.Address.Telephone.collect{ it -> it.MobilePhoneNumberIndicator.text()  + "|" + it.Number.SubscriberID.text() };
    def bankAccountList = root.BusinessPartnerSUITEReplicateRequestMessage.BusinessPartner.BankDetails.collect{ bt -> bt.BankAccountID.text() };
    def taxNumberList = root.BusinessPartnerSUITEReplicateRequestMessage.BusinessPartner.TaxNumber.collect{ tn -> tn.PartyTaxID.text() + "|" + tn.LONG_PartyTaxID.text()};
    
    def personLastName = root.BusinessPartnerSUITEReplicateRequestMessage.BusinessPartner.Common.Person.Name.FamilyName.text();
    def organizationName = root.BusinessPartnerSUITEReplicateRequestMessage.BusinessPartner.Common.Organisation.Name.FirstLineName.text();
    
    if(!personLastName.isEmpty()){
        message.setProperty("BP_Type", "Person");        
    }else if(!organizationName.isEmpty()){
        message.setProperty("BP_Type", "Organization");
    }else{
        String error = "Invalid Business Partners. Both Person and Organisation node don't have provided Name";
        throw new IllegalStateException(error);
    }
    
    message.setProperty("PhoneNumList", phoneNumList.join(","));
    messageLog.setStringProperty("LogInfo#phoneNum", phoneNumList.join(","))
    message.setProperty("BankAccountList", bankAccountList.join(","));
    message.setProperty("TaxNumberList", taxNumberList.join(","));
    
	
	return message;
}