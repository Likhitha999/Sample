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
 */
import com.sap.gateway.ip.core.customdev.util.Message;
import java.util.HashMap;
import groovy.json.JsonSlurper;
import groovy.json.JsonOutput;

// recursive method for parsing whole json
def formatDeep(def node){
    node.each{ subnode ->
        
        // case 1: format an array containing an empty string to an empty array
        if (subnode.value == [""]){
            subnode.value = [];
            
        // case 2: format empty string and empty objects to null values
        // FSM interprets null values as a deletion
        } else if ((subnode.value instanceof Map && subnode.value.isEmpty()) || subnode.value == ""){
            
            // S4 service order/item description is optional, FSM subject is mandatory
            // >> empty service order/item description replaced with subject " "
            if (subnode.key == "subject"){
                subnode.value = " ";
            } else {
                subnode.value = null;
            }
            
        // if the current node has subnodes (that means, it is a map or a list)
        //-> go further down in the tree and repeat the logic
        } else if (subnode.value instanceof Map || 
                   subnode.value instanceof List) {
            formatDeep(subnode.value);
        } else if (subnode instanceof Map) {
            formatDeep(subnode);
        }
    }
} 

def Message processData(Message message) { 

    def body = message.getBody(String.class);
    def jsonSlurper = new JsonSlurper();
    def object = jsonSlurper.parseText(body);

    formatDeep(object);
    
    message.setBody(JsonOutput.toJson(object));	
    
    return message;
}