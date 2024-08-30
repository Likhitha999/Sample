import com.sap.it.api.mapping.*
import com.sap.gateway.ip.core.customdev.util.Message
import groovy.util.XmlSlurper

def String customFunc(String Owners) {
   

    // Remove leading zeros
    def modifiedOwners = removeLeadingZeros(Owners)

   

    return modifiedOwners
}

def String removeLeadingZeros(String input) {
    // Remove leading zeros
    return input.replaceAll(/^0+/, '')
}

def Message processData(Message message) {
    def xmlString = message.getBody(java.lang.String) // Assuming the XML content is in the message body
    def xml = new XmlSlurper().parseText(xmlString)

    // Get the value of the "Owners" field from the XML using the custom function
    def modifiedOwners = customFunc(xml.PlntStorLocBySrvcOrg_Type.Owners.text())

    // Update the XML with the modified "Owners" value
    xml.PlntStorLocBySrvcOrg_Type.Owners.text = modifiedOwners


    // Set the modified XML content back to the message body
    message.setBody(xml.toString())

    return message
}
