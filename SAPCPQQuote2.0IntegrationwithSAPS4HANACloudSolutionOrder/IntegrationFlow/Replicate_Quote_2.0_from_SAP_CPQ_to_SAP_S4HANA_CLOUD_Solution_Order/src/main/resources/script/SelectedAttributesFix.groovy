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
def Message processData(Message message) {

    // Get Message body
    def body = message.getBody(java.lang.String) as String;
    def MessageBody = new XmlParser().parseText(body);   

    //iterate over each item
    MessageBody.row.each{
        //iterate over each selected Attribute
        it.SelectedAttributes.each{
            //check if Values Tag is missing
            if(it.Values.Value.size() == 0) {
                //Add Values Tag
                def Values = new NodeBuilder().Values {
                    Value("")
                    Author("False")
                }
                it.append Values
            }
            //Add Author Tag
            it.Values.each{
                if(it.Author.size() == 0) {
                    def AuthorValue = new NodeBuilder().Author("")
                    it.append AuthorValue
                }
            }
            
        }
    }
    
    message.setBody(groovy.xml.XmlUtil.serialize(MessageBody))
    return message;
}