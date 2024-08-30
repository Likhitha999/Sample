import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import com.sap.gateway.ip.core.customdev.util.Message

// routes
def clearRoute(message) {
    message.setProperty("doUpdateHeader", "")
    message.setProperty("doCompleteItem", "")
    message.setProperty("doUpdateItem", "")
}
def setRouteIrrelevant(Message message) {
    clearRoute(message)
}
def setRouteUpdateHeader(message) {
    clearRoute(message)
    message.setProperty("doUpdateHeader", "X")
}
def setRouteCompleteItem(message) {
    clearRoute(message)
    message.setProperty("doCompleteItem", "X")
}
def setRouteUpdateItem(message) {
    clearRoute(message)
    message.setProperty("doUpdateItem", "X")
}

def boolean setReferenceObject(message, equipmentNode) {
    def ref_object = equipmentNode?.externalId
    def ref_object_category = equipmentNode?.objectCategory
    def isUpdated = false
    if (equipmentNode) {
        if (ref_object) {
            if (ref_object_category == null || ref_object_category == 'EQ') {
                message.setProperty("NewEquipment", ref_object)
                message.setProperty("NewFunctionalLocation", "")
                isUpdated = true
            } else if (ref_object_category == 'FLOC') {
                message.setProperty("NewEquipment", "")
                message.setProperty("NewFunctionalLocation", ref_object)
                isUpdated = true
            }
        } else { // empty node -> delete
            message.setProperty("NewEquipment", "")
            message.setProperty("NewFunctionalLocation", "")
            isUpdated = true
        }
    } else { // null node -> delete
        message.setProperty("NewEquipment", "")
        message.setProperty("NewFunctionalLocation", "")
        isUpdated = true
    }
    return isUpdated
}

def setResultJsonAndBody(message, resultJson) {
    message.setBody(resultJson.toString())
    message.setProperty("resultJson",resultJson.toString())
}

def Message processData(Message message) {
    def body = message.getBody(java.lang.String) as String
    def sourcePayload = body
    def parser = new JsonSlurper()
    def parserResult = parser.parseText(body)
    def resultMap = [:]
    def responsibles, executingServiceEmployee;

    def isHeaderUpdated              = false
    def isItemUpdated                = false
    def isItemReferenceObjectUpdated = false
    def isReferenceObjectUpdated     = false
    def isResponsibleUpdated         = false
    def serviceOrderNeedsUpdate      = false

    // Text Handling for header and Item
    def isHeaderRemarkUpdated      = false
    def isHeaderResolutionUpdated  = false
    def isHeaderTextUpdated        = false
    def isItemRemarkUpdated        = false

    def ServiceOrderItemEntity = [[:]]
    def ServiceOrderItemTexts = [[:]]
    def ServiceOrderItemTextEntity = [[:]] 

    // get external ids from message header
    def serviceOrder = message.getHeaders().get("ServiceOrder") ?: ''
    def serviceOrderItem = message.getHeaders().get("ServiceOrderItem") ?: ''
    def serviceOrderBundleItem = message.getHeaders().get("ServiceOrderBundleItem") ?: ''
    def serviceOrderParentItem = message.getHeaders().get("ParentServiceOrderItem") ?: ''
    def eventType = message.getHeaders().get("EventType") ?: ''

    message.setProperty("eventType", eventType)
    message.setProperty("ServiceOrder", serviceOrder)
    message.setProperty("ServiceOrderItem", serviceOrderItem)

    def isHeaderExtensionEnabled = (message.getProperty("HeaderExtensionEnabled") ==~ /(?i)(true|x)/);
    def isExtensionEnabled = (message.getProperty("ExtensionEnabled") ==~ /(?i)(true|x)/);

    switch (eventType) {
        case "servicecall.updated":
            def updatedProperty

            updatedProperty = parserResult.data.updatedProperty
            resultMap."ServiceOrder" = serviceOrder;
            resultMap."FSMServiceCall" = parserResult.data.serviceCall.code;

            if (updatedProperty.contains("subject") && parserResult.data.serviceCall.subject != null) {
                resultMap."ServiceOrderDescription" = parserResult.data.serviceCall.subject.take(40)
                serviceOrderNeedsUpdate = true
            }
            if (updatedProperty.contains("priority") && parserResult.data.serviceCall.priority != null) {
                resultMap."ServiceDocumentPriority" = parserResult.data.serviceCall.priority
                serviceOrderNeedsUpdate = true
            }
            if (updatedProperty.contains("contact") && parserResult.data.serviceCall.contact != null && parserResult.data.serviceCall.contact.externalId != null) {
                resultMap."ContactPerson" = parserResult.data.serviceCall.contact.externalId
                serviceOrderNeedsUpdate = true
            } else if (updatedProperty.contains("contact") && parserResult.data.serviceCall.contact == null) {
                //send empty contact to delete contact in S4
                resultMap."ContactPerson" = ""
                serviceOrderNeedsUpdate = true
            }
            if (updatedProperty.contains("responsibles") && parserResult.data.serviceCall.responsibles[0] && parserResult.data.serviceCall.responsibles[0].externalId) {
                message.setProperty("NewResponsible", parserResult.data.serviceCall.responsibles[0].externalId)  
                isResponsibleUpdated = true
            }
            if (updatedProperty.contains("equipments")) {
                isReferenceObjectUpdated = setReferenceObject(message, parserResult.data?.serviceCall?.equipments[0])
            }
            if (updatedProperty.contains("earliestStartDateTime") && parserResult.data.serviceCall.earliestStartDateTime != null) {
                resultMap."RequestedServiceStartDateTime" = parserResult.data.serviceCall.earliestStartDateTime
                serviceOrderNeedsUpdate = true
            }
            if (updatedProperty.contains("dueDateTime") && parserResult.data.serviceCall.dueDateTime != null) {
                resultMap."RequestedServiceEndDateTime" = parserResult.data.serviceCall.dueDateTime
                serviceOrderNeedsUpdate = true
            }
            //* Text handling
            if (updatedProperty.contains("remarks")) {
                resultMap."Remarks" = parserResult.data.serviceCall.remarks
                isHeaderRemarkUpdated = true
                isHeaderTextUpdated = true
            }
            if (updatedProperty.contains("resolution")) {
                resultMap."Resolution" = parserResult.data.serviceCall.resolution
                isHeaderResolutionUpdated = true
                isHeaderTextUpdated = true
            }

            resultMap = ["root":resultMap]
            setResultJsonAndBody(message, JsonOutput.toJson(resultMap))
            message.setProperty("isHeaderUpdated", isHeaderUpdated)
            message.setProperty("isReferenceObjectUpdated", isReferenceObjectUpdated)
            message.setProperty("serviceOrderNeedsUpdate", serviceOrderNeedsUpdate) // for choreography later
            message.setProperty("isHeaderRemarkUpdated", isHeaderRemarkUpdated) 
            message.setProperty("isHeaderResolutionUpdated", isHeaderResolutionUpdated)
            message.setProperty("isHeaderTextUpdated", isHeaderTextUpdated)

            if (isHeaderUpdated || isHeaderTextUpdated || isHeaderExtensionEnabled || isResponsibleUpdated || isReferenceObjectUpdated || serviceOrderNeedsUpdate) {
                setRouteUpdateHeader(message)
            } else {
                setRouteIrrelevant(message)
            }
            break

        case "activity.updated":
            def updatedProperty, plannedServiceStartDateTime, plannedServiceEndDateTime, executionStatus = "";
            float plannedDuration

            updatedProperty = parserResult.data.updatedProperty
            plannedServiceStartDateTime = parserResult.data.serviceCall.activity.plannedStartDate
            plannedServiceEndDateTime = parserResult.data.serviceCall.activity.plannedEndDate

            resultMap."ServiceOrder" = serviceOrder
            if (serviceOrderItem != null) {
                resultMap["ServiceOrderItems"] = ["ServiceOrderItemEntity":[[:]]]
                resultMap["ServiceOrderItems"]["ServiceOrderItemEntity"][0]."ServiceOrderItem" = serviceOrderItem
                resultMap["ServiceOrderItems"]["ServiceOrderItemEntity"][0]."FSMServiceActivity" = parserResult.data.serviceCall.activity.code;
            }
            if (updatedProperty.contains("plannedStartDate") && plannedServiceStartDateTime != null) {
                resultMap["ServiceOrderItems"]["ServiceOrderItemEntity"][0]."PlannedServiceStartDateTime" = plannedServiceStartDateTime
                isItemUpdated = true
            }
            if (updatedProperty.contains("plannedEndDate") && plannedServiceEndDateTime != null) {
                resultMap["ServiceOrderItems"]["ServiceOrderItemEntity"][0]."PlannedServiceEndDateTime" = plannedServiceEndDateTime
                isItemUpdated = true
            }
            if (updatedProperty.contains("plannedDurationInMinutes") && parserResult.data?.serviceCall?.activity?.plannedDurationInMinutes != null) {
                plannedDuration = Math.round(parserResult.data?.serviceCall?.activity?.plannedDurationInMinutes / 60 * 100) / 100
                resultMap["ServiceOrderItems"]["ServiceOrderItemEntity"][0]."ActualServiceDuration" = plannedDuration
                isItemUpdated = true
            }
            if (updatedProperty.contains("subject") && parserResult.data.serviceCall.activity.subject != null) {
                resultMap["ServiceOrderItems"]["ServiceOrderItemEntity"][0]."ServiceOrderItemDescription" = parserResult.data.serviceCall.activity.subject.take(40)
                isItemUpdated = true
            }
            if (updatedProperty.contains("contact") && parserResult.data.serviceCall?.activity?.contact?.externalId != null) {
                resultMap["ServiceOrderItems"]["ServiceOrderItemEntity"][0]."ContactPersonBusinessPartnerId" = parserResult.data.serviceCall.activity.contact.externalId
                isItemUpdated = true
            } else if (updatedProperty.contains("contact") && parserResult.data.serviceCall?.activity?.contact?.externalId == null){
                //send empty contact to delete contact in S4
                resultMap["ServiceOrderItems"]["ServiceOrderItemEntity"][0]."ContactPersonBusinessPartnerId" = ""
                isItemUpdated = true
            }
            if (updatedProperty.contains("equipment")) {
                isItemReferenceObjectUpdated = setReferenceObject(message, parserResult.data?.serviceCall?.activity?.equipment)
            }
            
            if (updatedProperty.contains("responsibles")) {
                responsibles = parserResult.data?.serviceCall?.activity?.responsibles
                if (responsibles && responsibles.size() > 0) {
                    executingServiceEmployee = responsibles[0]?.externalId
                } else {
                    executingServiceEmployee = null;
                }
                resultMap["ServiceOrderItems"]["ServiceOrderItemEntity"][0]."ExecutingServiceEmployee" = executingServiceEmployee;
                isItemUpdated = true
            }
            
            //update Execution Status?
            if (parserResult.data.serviceCall.activity?.statusChangeReason == "UNASSIGN") {
                executionStatus = "02";
                isItemUpdated = true;
            } else if (updatedProperty.contains("status") && parserResult.data.serviceCall.activity.status != null) {
                if (parserResult.data.serviceCall.activity.status == "CLOSED" && parserResult.data.serviceCall.activity.executionStage == "CANCELLED") {
                    executionStatus = "07";
                    isItemUpdated = true;
                } else if (parserResult.data.serviceCall.activity.status == "CLOSED" && parserResult.data.serviceCall.activity.executionStage == "CLOSED") {
                    executionStatus = "06";
                    isItemUpdated = true;
                }
            }
            
            //* Transfer text elements
            if (updatedProperty.contains("remarks")) {
                ServiceOrderItemTextEntity[0].LongText                    = parserResult.data.serviceCall.activity.remarks
                ServiceOrderItemTextEntity[0].LongTextType                = "Remarks"
                isItemRemarkUpdated = true
            }
            if (isItemRemarkUpdated == true) {
                ServiceOrderItemTexts[0].ServiceOrderItemTextEntity       = ServiceOrderItemTextEntity
                ServiceOrderItemEntity[0].ServiceOrderItemTexts           = ServiceOrderItemTexts
                resultMap["ServiceOrderItems"]["ServiceOrderItemEntity"][0]."ServiceOrderItemTexts" = ServiceOrderItemTexts
            }

            resultMap = ["root":resultMap]
            setResultJsonAndBody(message, JsonOutput.toJson(resultMap))
            message.setProperty("updateItemExecutionStatus", executionStatus)
            message.setProperty("isItemUpdated", isItemUpdated)
            message.setProperty("isItemReferenceObjectUpdated", isItemReferenceObjectUpdated)
            message.setProperty("isItemRemarkUpdated", isItemRemarkUpdated)

            if (isItemUpdated || isItemRemarkUpdated || isItemReferenceObjectUpdated || isExtensionEnabled) {
                setRouteUpdateItem(message)
            } else {
                setRouteIrrelevant(message)
            }
            break

        case "activity.released":
            def plannedServiceStartDateTime, plannedServiceEndDateTime, plannedDurationInMinutes
            float plannedDuration

            responsibles = parserResult.data?.serviceCall?.activity?.responsibles
            if (responsibles && responsibles.size() > 0) {
                executingServiceEmployee = responsibles[0]?.externalId
            }
            plannedServiceStartDateTime = parserResult.data?.serviceCall?.activity?.plannedStartDate
            plannedServiceEndDateTime = parserResult.data?.serviceCall?.activity?.plannedEndDate
            plannedDurationInMinutes = parserResult.data?.serviceCall?.activity?.plannedDurationInMinutes

            resultMap."ServiceOrder" = serviceOrder
            if (serviceOrderItem != null) {
                resultMap["ServiceOrderItems"] = ["ServiceOrderItemEntity":[[:]]]
                resultMap["ServiceOrderItems"]["ServiceOrderItemEntity"][0]."ServiceOrderItem" = serviceOrderItem
                resultMap["ServiceOrderItems"]["ServiceOrderItemEntity"][0]."FSMServiceActivity" = parserResult.data.serviceCall.activity.code;
            }
            if (executingServiceEmployee != null) {
                resultMap["ServiceOrderItems"]["ServiceOrderItemEntity"][0]."ExecutingServiceEmployee" = executingServiceEmployee
                isItemUpdated = true
            }
            if (plannedServiceStartDateTime != null) {
                resultMap["ServiceOrderItems"]["ServiceOrderItemEntity"][0]."PlannedServiceStartDateTime" = plannedServiceStartDateTime
                isItemUpdated = true
            }
            if (plannedServiceEndDateTime != null) {
                resultMap["ServiceOrderItems"]["ServiceOrderItemEntity"][0]."PlannedServiceEndDateTime" = plannedServiceEndDateTime
                isItemUpdated = true
            }
            if (plannedDurationInMinutes != null) {
                plannedDuration = Math.round(plannedDurationInMinutes / 60 * 100) / 100
                resultMap["ServiceOrderItems"]["ServiceOrderItemEntity"][0]."ActualServiceDuration" = plannedDuration
                isItemUpdated = true
            }
            if (parserResult.data.serviceCall.activity.subject != null) {
                resultMap["ServiceOrderItems"]["ServiceOrderItemEntity"][0]."ServiceOrderItemDescription" = parserResult.data.serviceCall.activity.subject.take(40)
                isItemUpdated = true
            }
            if (parserResult.data.serviceCall?.activity?.contact?.externalId != null) {
                resultMap["ServiceOrderItems"]["ServiceOrderItemEntity"][0]."ContactPersonBusinessPartnerId" = parserResult.data.serviceCall.activity.contact.externalId
                isItemUpdated = true
            }
            if (parserResult.data?.serviceCall?.activity?.equipment) {
                isItemReferenceObjectUpdated = setReferenceObject(message, parserResult.data?.serviceCall?.activity?.equipment)
            } else {
                isItemReferenceObjectUpdated = true // empty equipment could be a deletion
            }
            //* Transfer text elements
            if (parserResult.data.serviceCall.activity.remarks != null) {
                ServiceOrderItemTextEntity[0].LongText                    = parserResult.data.serviceCall.activity.remarks
                ServiceOrderItemTextEntity[0].LongTextType                = "Remarks"
                isItemRemarkUpdated = true
            }
            if (isItemRemarkUpdated == true) {
                ServiceOrderItemTexts[0].ServiceOrderItemTextEntity       = ServiceOrderItemTextEntity
                ServiceOrderItemEntity[0].ServiceOrderItemTexts           = ServiceOrderItemTexts
                resultMap["ServiceOrderItems"]["ServiceOrderItemEntity"][0]."ServiceOrderItemTexts" = ServiceOrderItemTexts
            }
            
            //released event shall set Execution Status "In Execution"
            message.setProperty("updateItemExecutionStatus", "04");

            resultMap = ["root":resultMap]
            setResultJsonAndBody(message, JsonOutput.toJson(resultMap))
            message.setProperty("isItemUpdated", isItemUpdated)
            message.setProperty("isItemReferenceObjectUpdated", isItemReferenceObjectUpdated)
            message.setProperty("isItemRemarkUpdated", isItemRemarkUpdated)

            if (isItemUpdated || isItemRemarkUpdated || isItemReferenceObjectUpdated || isExtensionEnabled) {
                setRouteUpdateItem(message)
            } else {
                setRouteIrrelevant(message)
            }
            break

        case "activity.releasedunassigned":
        case "activity.replannedreleased":
            def serviceOrderItemIsCompleted

            resultMap."ServiceOrder" = serviceOrder
            if (serviceOrderItem != null) {
                resultMap["ServiceOrderItems"] = ["ServiceOrderItemEntity":[[:]]]
                resultMap["ServiceOrderItems"]["ServiceOrderItemEntity"][0]."ServiceOrderOutter" = serviceOrder
                resultMap["ServiceOrderItems"]["ServiceOrderItemEntity"][0]."ServiceOrderItem" = serviceOrderItem
            }
            serviceOrderItemIsCompleted = parserResult.data?.serviceCall?.sourceActivityCompleted?.activityData?.status
            if (serviceOrderItemIsCompleted != null && serviceOrderItemIsCompleted.toLowerCase() == "closed") {
                serviceOrderItemIsCompleted = "T"
                resultMap["ServiceOrderItems"]["ServiceOrderItemEntity"][0]."ServiceOrderItemIsCompleted" = serviceOrderItemIsCompleted
                isItemUpdated = true
            }

            resultMap = ["root":resultMap]
            setResultJsonAndBody(message, JsonOutput.toJson(resultMap))
            message.setProperty("isItemUpdated", isItemUpdated)

            if (isItemUpdated) {
                setRouteCompleteItem(message)
            } else {
                setRouteIrrelevant(message)
            }
            break

        case "activity.completed":
            def activityExecutionStage = parserResult.data?.serviceCall?.activity?.activityData?.executionStage;
            
            if (activityExecutionStage == 'CANCELLED') {
                message.setProperty("ServiceOrderItemIsCanceled", "X");
            }
            
            setRouteCompleteItem(message)
            break

        default:
            // route: Irrelevant, ignore, no error
            setRouteIrrelevant(message)
            break
    }

    message.setProperty("SourcePayload", sourcePayload)
    return message
}