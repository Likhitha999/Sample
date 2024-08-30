import com.sap.gateway.ip.core.customdev.util.Message;
import groovy.json.JsonSlurper
import groovy.xml.*

def Message processData(Message message) {
 
    def bUpdateHeader          = false
    def bAddResponsible        = false
    def bDeleteResponsible     = false
    def bAddReferenceObject    = false
    def bDeleteReferenceObject = false

    def currentEquipment = ""
    def currentFunctionalLocation = ""
    def newEquipment = message.getProperty("NewEquipment") ?: ""
    def newFunctionalLocation = message.getProperty("NewFunctionalLocation") ?: ""
    def isReferenceObjectUpdated = message.getProperty("isReferenceObjectUpdated")
    
    def CurrentResponsible = "", referenceObjects, personResponsibles, i;
    def NewResponsible = message.getProperty("NewResponsible");
    def serviceOrderNeedsUpdate = message.getProperty("serviceOrderNeedsUpdate");
    def setServiceOrderReleased = false;
    def xmlparser = new XmlSlurper();
    
    def body = message.getBody(java.lang.String);
    def parser = new JsonSlurper();
    def readPayload = parser.parseText(body);
    
    try {
        def test_so = readPayload.A_ServiceOrder.A_ServiceOrderType         
    } catch (Exception ex) {
        def msg_so = message.getProperty("ServiceOrder") ?: ""
        def msg = "The integration flow message won’t be processed further. Service order not found in S/4HANA: " + msg_so 
        throw new Exception(msg)
    }
    
    //Get Current Main Equipment/Functional Location
    try {
        referenceObjects = readPayload.A_ServiceOrder.A_ServiceOrderType.to_ReferenceObject.A_ServiceOrderRefObjectType
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

    //Delete and/or add equipment/functional location
    def newRefObject = newEquipment ?: newFunctionalLocation
    def currentRefObject = currentEquipment ?: currentFunctionalLocation
    if (isReferenceObjectUpdated) {
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
    
    // Get Current Main Responsible
    try {
        personResponsibles = readPayload.A_ServiceOrder.A_ServiceOrderType.to_PersonResponsible.A_ServiceOrderPersonRespType;
        for (i = 0; i < personResponsibles.size(); i++) {
            if (personResponsibles[i] && personResponsibles[i].CustMgmtPartnerIsMainPartner == "true") {
                CurrentResponsible = personResponsibles[i].PersonResponsible;
            }
        }
        //If no PersonResponsible with main flag is found, take the first one in the list
        if (CurrentResponsible == ""){
            CurrentResponsible = readPayload.A_ServiceOrder.A_ServiceOrderType.to_PersonResponsible.A_ServiceOrderPersonRespType[0].PersonResponsible;
        }
    } catch (Exception ex) {
        CurrentResponsible = "";
    }
    
    //Delete and/or Add Responsible?
    if (NewResponsible && NewResponsible != CurrentResponsible) {
        bAddResponsible = true;
        if (CurrentResponsible) {
            bDeleteResponsible = true;
            setServiceOrderReleased = true;
        }
    }

    if (serviceOrderNeedsUpdate) {
        bUpdateHeader = true;
    }
    
    //Requests like Deleting and then Adding an Employee Responsible can lead to an temporary Error on the Service Order
    //Such Errors will set an Service Order from Status 'Released' to 'In Process' even if the Error is fxied by the seconed (Add) Request
    //To fix this we send an explicit Request to set the Service Order back to 'Released'
    if (setServiceOrderReleased && readPayload?.A_ServiceOrder?.A_ServiceOrderType?.ServiceOrderIsReleased) {
        bUpdateHeader = true;
        def updatePayload = xmlparser.parseText(message.getProperty("updateHeaderPayload"));
        def newNode = xmlparser.parseText("<ServiceOrderIsReleased>X</ServiceOrderIsReleased>");
        updatePayload.A_ServiceOrderType.appendNode(newNode);
        updatePayload = XmlUtil.serialize(updatePayload);
        message.setProperty("updateHeaderPayload", updatePayload);
    }
    
    message.setProperty("CurrentResponsible", CurrentResponsible)
    message.setProperty("CurrentEquipment", currentEquipment)
    message.setProperty("CurrentFunctionalLocation", currentFunctionalLocation)
    message.setProperty("SendRequest_DeleteReferenceObject", bDeleteReferenceObject)
    message.setProperty("SendRequest_AddReferenceObject", bAddReferenceObject)
    message.setProperty("SendRequest_UpdateHeader", bUpdateHeader);
    message.setProperty("SendRequest_AddResponsible", bAddResponsible);
    message.setProperty("SendRequest_DeleteResponsible", bDeleteResponsible);
    
    //* Handle Text Elements
    def UpdateHeaderPayloadRaw = message.getProperty("mappingResultPayload");
    def UpdateHeaderPayload = " " ;
    def UpdateRemarkPayload = " " ;
    
    //* Text Element Transfer
    def isHeaderUpdated           = message.getProperty("isHeaderUpdated");
    def isHeaderRemarkUpdated     = message.getProperty("isHeaderRemarkUpdated");
    def isHeaderResolutionUpdated = message.getProperty("isHeaderResolutionUpdated");
    def count = "0"
    def ServiceOrder = " ";
    def Language;
    
    def LongTextRemark, LongTextRemarkType, LongTextResolutionType, LongTextResolution;
    
    //* Fill Remark 
    if (isHeaderRemarkUpdated == true) {
        
        UpdateRemarkPayload = xmlparser.parseText(UpdateHeaderPayloadRaw);
        ServiceOrder        = UpdateRemarkPayload.A_ServiceOrderType.to_Text.A_ServiceOrderTextType[0].ServiceOrder
        Language            = UpdateRemarkPayload.A_ServiceOrderType.to_Text.A_ServiceOrderTextType[0].Language
        Language            = Language.toString()
        LongTextRemarkType  = UpdateRemarkPayload.A_ServiceOrderType.to_Text.A_ServiceOrderTextType[0].LongTextID
    
        LongTextRemark = UpdateRemarkPayload?.A_ServiceOrderType?.to_Text?.A_ServiceOrderTextType[0]?.LongText?: ""
        if (LongTextRemark == "") {
            isHeaderRemarkDeleted = true
        } else { 
            isHeaderRemarkDeleted = false
        }
            
        UpdateRemarkPayload = UpdateRemarkPayload.A_ServiceOrderType.to_Text
        UpdateRemarkPayload.A_ServiceOrderTextType.findAll { it.LongTextID != LongTextRemarkType }.replaceNode {}
        UpdateRemarkPayload.replaceNode { 'A_ServiceOrderText'(it.children()) }
    
        UpdateRemarkPayload = groovy.xml.XmlUtil.serialize( UpdateRemarkPayload )
        LongTextRemarkType = LongTextRemarkType.toString()
        message.setProperty("serviceOrder", ServiceOrder)
        message.setProperty("TextLangu", Language)
        message.setProperty("isHeaderRemarkUpdated", isHeaderRemarkUpdated)
        message.setProperty("LongTextRemarkType", LongTextRemarkType)
        message.setProperty("updateRemarkPayload", UpdateRemarkPayload)
        message.setProperty("isHeaderRemarkDeleted", isHeaderRemarkDeleted)
    }
    
    //* Fill isHeaderResolution
    if (isHeaderResolutionUpdated == true) {
        UpdateResolutionPayload = xmlparser.parseText(UpdateHeaderPayloadRaw);
        ServiceOrder        = UpdateResolutionPayload.A_ServiceOrderType.to_Text.A_ServiceOrderTextType[1].ServiceOrder
        Language            = UpdateResolutionPayload.A_ServiceOrderType.to_Text.A_ServiceOrderTextType[1].Language
        Language            = Language.toString()
        LongTextResolutionType  = UpdateResolutionPayload.A_ServiceOrderType.to_Text.A_ServiceOrderTextType[1].LongTextID
      
        LongTextResolution = UpdateResolutionPayload?.A_ServiceOrderType?.to_Text?.A_ServiceOrderTextType[1]?.LongText?: ""
        if (LongTextResolution == "") {
            isHeaderResolutionDeleted = true
        } else {
            isHeaderResolutionDeleted = false
        }
            
        UpdateResolutionPayload = UpdateResolutionPayload.A_ServiceOrderType.to_Text
        UpdateResolutionPayload.A_ServiceOrderTextType.findAll { it.LongTextID != LongTextResolutionType }.replaceNode {}
        UpdateResolutionPayload.replaceNode { 'A_ServiceOrderText'(it.children()) }

        UpdateResolutionPayload = groovy.xml.XmlUtil.serialize( UpdateResolutionPayload )
        LongTextResolutionType = LongTextResolutionType.toString()
        message.setProperty("serviceOrder", ServiceOrder)
        message.setProperty("TextLangu", Language)
        message.setProperty("isHeaderResolutionUpdated", isHeaderResolutionUpdated)
        message.setProperty("LongTextResolutionType",  LongTextResolutionType)
        message.setProperty("updateResolutionPayload", UpdateResolutionPayload)
        message.setProperty("isHeaderResolutionDeleted", isHeaderResolutionDeleted)
    }
    
    return message;
}