import com.sap.gateway.ip.core.customdev.util.Message;
import groovy.json.JsonSlurper;
import groovy.xml.*

def Message processData(Message message) {

    def bAddReferenceObject = false
    def bDeleteReferenceObject = false
    def currentEquipment = ""
    def currentFunctionalLocation = ""
    def newEquipment = message.getProperty("NewEquipment") ?: ""
    def newFunctionalLocation = message.getProperty("NewFunctionalLocation") ?: ""
    def isItemReferenceObjectUpdated = message.getProperty("isItemReferenceObjectUpdated")
    def setServiceOrderReleased = false;
    def referenceObjects
    def xmlparser = new XmlSlurper();
    
    def body = message.getBody(java.lang.String);
    def parser = new JsonSlurper();
    def readPayload = parser.parseText(body);

    try {
        def test_item = readPayload.A_ServiceOrderItem.A_ServiceOrderItemType         
    } catch (Exception ex) {
        def msg_so = message.getProperty("ServiceOrder") ?: ""
        def msg_pos = message.getProperty("ServiceOrderItem") ?: ""        
        def msg = "The integration flow message won’t be processed further. Service order item not found in S/4HANA: " + msg_so + "/" + msg_pos
        throw new Exception(msg)
    }
    
    //Get Current Main Equipment/Functional Location
    try {
        referenceObjects = readPayload.A_ServiceOrderItem.A_ServiceOrderItemType.to_ReferenceObject.A_ServiceOrderItemRefObjectType
        for (i = 0; i < referenceObjects.size(); i++) {
            if (referenceObjects[i] && referenceObjects[i].SrvcRefObjIsMainObject == "true") {
                if (referenceObjects[i]?.ServiceReferenceEquipment && referenceObjects[i]?.ServiceReferenceEquipment != "") {
                    currentEquipment = referenceObjects[i]?.ServiceReferenceEquipment
                    i = referenceObjects.size() // leave loop
                } else if (referenceObjects[i]?.ServiceRefFunctionalLocation && referenceObjects[i]?.ServiceRefFunctionalLocation != "") {
                    currentFunctionalLocation = referenceObjects[i]?.ServiceRefFunctionalLocation
                    i = referenceObjects.size() // leave loop
                }
            }
        }
    } catch (Exception ex) { 
        currentEquipment = ""
        currentFunctionalLocation = ""
    }

    //Delete and/or add equipment/functional Location
    def newRefObject = newEquipment ?: newFunctionalLocation
    def currentRefObject = currentEquipment ?: currentFunctionalLocation
    if (isItemReferenceObjectUpdated) {
        if (newRefObject) {
            if (newRefObject != currentRefObject) {
                bAddReferenceObject = true
                setServiceOrderReleased = true
                if (currentRefObject) {
                    bDeleteReferenceObject = true
                }
            }
        } else {
            if (currentRefObject) {
                bDeleteReferenceObject = true
                setServiceOrderReleased = true
            }
        }
    }
    
    //Requests like Deleting and then Adding an Equipment can lead to an temporary Error on the Service Order Item
    //Such Errors will set an Service Order Item from Status 'Released' to 'In Process' even if the Error is fxied by the seconed (Add) Request
    //To fix this we send an explicit Request to set the Service Order Item back to 'Released'
    if (setServiceOrderReleased && readPayload?.A_ServiceOrderItem?.A_ServiceOrderItemType?.ServiceOrderItemIsReleased) {
        message.setProperty("isItemUpdated", true);
        def updatePayload = xmlparser.parseText(message.getProperty("updateItemPayload"));
        def newNode = xmlparser.parseText("<ServiceOrderItemIsReleased>X</ServiceOrderItemIsReleased>");
        updatePayload.A_ServiceOrderItemType.appendNode(newNode);
        updatePayload = XmlUtil.serialize(updatePayload);
        message.setProperty("updateItemPayload", updatePayload);
    }

    message.setProperty("CurrentEquipment", currentEquipment)
    message.setProperty("CurrentFunctionalLocation", currentFunctionalLocation)
    message.setProperty("SendRequest_DeleteReferenceObject", bDeleteReferenceObject)
    message.setProperty("SendRequest_AddReferenceObject", bAddReferenceObject)

    return message;
}