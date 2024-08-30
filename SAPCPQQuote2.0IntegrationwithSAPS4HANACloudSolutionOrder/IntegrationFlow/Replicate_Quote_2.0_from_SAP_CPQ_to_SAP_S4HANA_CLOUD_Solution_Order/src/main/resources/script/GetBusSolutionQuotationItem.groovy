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
import groovy.xml.XmlUtil;

def Message processData(Message message) {

    
    //Properties 
    
    def messageLog = messageLogFactory.getMessageLog(message);
    def solutionQuote = message.getProperty("S4_SolutionQuoteRepsonse");
    def cpqQuoteItemId = message.getProperty("CPQ_QuoteItemId");
    def xmlSlurper = new XmlSlurper();
    def xmlSolutionQuote = xmlSlurper.parseText(solutionQuote);
    
    def xmlFoundedItem = xmlSolutionQuote.entry.link.inline.feed.entry.find{it.content.properties.BusSolnQtanItemExtReference == cpqQuoteItemId};
    def s4QuotationItemId = xmlFoundedItem?.content.properties.BusSolutionQuotationItem.text();
    
    message.setProperty("S4_BusSolutionQuotationItemIdentifier", s4QuotationItemId);
    messageLog.setStringProperty("Logger#S4_BusSolutionQuotationItemIdentifier", s4QuotationItemId.toString());
    
    return message;
}