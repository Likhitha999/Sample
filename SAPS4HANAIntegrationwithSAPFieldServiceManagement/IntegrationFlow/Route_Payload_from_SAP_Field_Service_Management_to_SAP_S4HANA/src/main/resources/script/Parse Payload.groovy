import com.sap.gateway.ip.core.customdev.util.Message
import java.util.HashMap
import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import com.sap.it.api.mapping.ValueMappingApi
import com.sap.it.api.ITApiFactory

// check is string is empty
def boolean isEmpty(String id) {
    return (id == null || id == "" || id == "null")
}
// returns a map of external Ids by splitting the provided string extId at the slash (/)
def Map getExternalIds(String extId, String fallbackServiceOrder=null) {
    Map extIds = [:]
    List splitIds = []
    if (extId != null && extId.size() > 0) {
        splitIds = extId.split('/')
        extIds['ServiceOrder'] = splitIds[0]
        if (splitIds.size() > 1) {
            extIds['ServiceOrderItem'] = splitIds[-1]
            if (splitIds.size() == 3) {
                extIds['ServiceOrderBundleItem'] = splitIds[-2]
            }
        }
    }
    if ((extIds['ServiceOrder'] == null || extIds['ServiceOrder'] == '') 
        && (fallbackServiceOrder != null || fallbackServiceOrder != '' ) ) {
        extIds['ServiceOrder'] = fallbackServiceOrder
    }
    return extIds
}
// routes
def clearRoute(message) {
    message.setProperty("checkErrorOccured", "");
    message.setProperty("Irrelevant", "")
    message.setProperty("serviceType", "")
    message.setProperty("CreateServiceItem", "")
    message.setProperty("SourceActivityCompleted", "")
}
def setRouteIrrelevant(Message message) {
    clearRoute(message)
    message.setProperty("Irrelevant", "X")
}
def setRouteUpdateServiceOrder(Message message) {
    clearRoute(message)
}
def setRouteCreateServiceConfirmation(Message message) {
    clearRoute(message)
    message.setProperty("serviceType", "ServiceConfirmation")
}
def setRouteCreateServiceItem(message) {
    clearRoute(message)
    message.setProperty("CreateServiceItem", "X")
}
def setRouteError(Message message, String event, String fields) {
    clearRoute(message)
    message.setProperty("checkErrorOccured", "X");
    message.setProperty("checkErrorMessage", "The integration flow message won’t be processed further. Event " + event + " is missing field(s): " + fields);
}
def setRouteCreateServiceOrder(message) {
    clearRoute(message);
    message.setProperty("CreateServiceOrder", "X")
}
// add-on routes
def addRouteSourceActivityCompleted(message) {
    message.setProperty("SourceActivityCompleted","X")
}
// set globals
def setGlobalServiceOrderIds(Message message, Map externalIds) {
    message.setProperty("ServiceOrder", externalIds.ServiceOrder ?: '')
    message.setProperty("ServiceOrderItem", externalIds.ServiceOrderItem ?: '')
    message.setProperty("ServiceOrderBundleItem", externalIds.ServiceOrderBundleItem ?: '')
    message.setProperty("ParentServiceOrderItem", (externalIds.ServiceOrderBundleItem ?: externalIds.ServiceOrderItem) ?: '')
    message.setHeader("ServiceOrder", externalIds.ServiceOrder)
    message.setHeader("ServiceOrderItem", externalIds.ServiceOrderItem)
    message.setHeader("ServiceOrderBundleItem", externalIds.ServiceOrderBundleItem)
    message.setHeader("ParentServiceOrderItem", (externalIds.ServiceOrderBundleItem ?: externalIds.ServiceOrderItem))
}
def isServiceCallStatusSupported(sStatus, sStatusList) {
    def supportedStatusList = sStatusList.split("\\|")*.trim();
    
    if (sStatus in supportedStatusList) {
        return true;
    } else {
        return false;
    }
}
def isServiceCallTypeSupported(sType) {
    def service = ITApiFactory.getApi(ValueMappingApi.class, null);
    
    if (sType == '' || sType == null) {
        sType = "<null>"
    }
    
    def ServiceOrderType = service.getMappedValue("ServiceCallFSM", "Type", sType, "ServiceOrderS4HANA", "TransactionType") ?: null;
    
    if (ServiceOrderType != null) {
        return true;
    } else {
        return false;
    }
}

// process message
def Message processData(Message message) { 
    def body = message.getBody(String.class)
    def jsonSlurper = new JsonSlurper()
    def jsonResult = jsonSlurper.parseText(body)
    def eventType = jsonResult.eventType?.toLowerCase()
    def externalId
    def externalIds = [:]
    def completeCompatibilityMode = false
    def sourceActivity = jsonResult.data?.serviceCall?.activity?.sourceActivity as String
    def sourceActivityCompleted = jsonResult.data?.serviceCall?.sourceActivityCompleted as String
    def activityStatus = jsonResult.data?.serviceCall?.activity?.status as String
    def statusChangeReason = jsonResult.data?.serviceCall?.activity?.statusChangeReason as String
    def updatedProperties = jsonResult.data?.updatedProperty
    def activityExecutionStage = jsonResult.data?.serviceCall?.activity?.executionStage as String
    def fallbackServiceOrder = jsonResult.data?.serviceCall?.unifiedIdentifier?.externalId as String 
    def serviceCallStatus;
    def bUnassignCase = false;

    message.setHeader("EventType", eventType)

    // decide on iFlow route
    switch (eventType) {
        
        case "servicecall.created":
            serviceCallStatus = jsonResult.data?.status as String ?: ''
            serviceCallType = jsonResult.data?.type as String ?: ''
            serviceCallStatusList = message.getProperty("SupportedServiceCallStatus") as String ?: ''
            if (isServiceCallStatusSupported(serviceCallStatus, serviceCallStatusList) && isServiceCallTypeSupported(serviceCallType)) {
                setRouteCreateServiceOrder(message);
            } else {
                // route: Irrelevant, ignore, no error
                setRouteIrrelevant(message)
            }
            break;

        case "servicecall.updated":
            externalId = jsonResult.data?.serviceCall?.externalId as String
            externalIds = getExternalIds(externalId, fallbackServiceOrder)
            setGlobalServiceOrderIds(message, externalIds)
            if (isEmpty(externalIds.ServiceOrder)  || updatedProperties == null) {
                // route: Irrelevant, ignore, no error
                setRouteIrrelevant(message)
            } else {
                // route: Update Service Order
                //        (iFlow Notify Service Order Update)
                setRouteUpdateServiceOrder(message)
            }
            break

        case "activity.created":
            if (sourceActivity != null) { //Duplication Cases
                externalId = jsonResult.data?.serviceCall?.activity?.sourceActivity?.externalId as String
                externalIds = getExternalIds(externalId, fallbackServiceOrder)
                setGlobalServiceOrderIds(message, externalIds)
                if (isEmpty(externalIds.ServiceOrder)) {
                    // route: Irrelevant, ignore, no error
                    setRouteIrrelevant(message)
                } else if (isEmpty(externalIds.ServiceOrderItem)) {
                    // route: Raise error
                    setRouteError(message, eventType, 'activity.sourceActivity.externalId')
                } else {
                    // route: Create Service Item
                    //        (iFlow Update Service Order for Duplicate Activity)
                    setRouteCreateServiceItem(message)
                }
            } else { //Creation Case
                externalId = jsonResult.data?.serviceCall?.unifiedIdentifier?.externalId as String
                externalIds = getExternalIds(externalId, fallbackServiceOrder)
                setGlobalServiceOrderIds(message, externalIds)
                
                if (isEmpty(externalIds.ServiceOrder)) {
                    // route: Irrelevant, ignore, no error
                    setRouteIrrelevant(message)
                } else {
                    // route: Create Service Item
                    //        (iFlow Update Service Order for Duplicate Activity)
                    setRouteCreateServiceItem(message)
                }
            }
            break

        case "activity.updated":
            externalId = jsonResult.data?.serviceCall?.activity?.externalId as String
            externalIds = getExternalIds(externalId, fallbackServiceOrder)
            setGlobalServiceOrderIds(message, externalIds)
            
            if (updatedProperties && updatedProperties.contains("status") && statusChangeReason == "UNASSIGN") {
                bUnassignCase = true;
            }
            
            if (isEmpty(externalIds.ServiceOrder) || updatedProperties == null) {
                // route: Irrelevant, ignore, no error
                setRouteIrrelevant(message)
            } else if (activityStatus == null || activityExecutionStage == null) {
                // route: Raise error
                setRouteError(message, eventType, 'activity.status, activity.executionStage')
            } else if (activityStatus == "DRAFT" && !bUnassignCase) {
                // route: Irrelevant, ignore, no error
                setRouteIrrelevant(message)
            } else if (isEmpty(externalIds.ServiceOrderItem)) {
                // route: Raise error
                setRouteError(message, eventType, 'activity.externalId')
            } else {
                // route: Update Service Order
                //        (iFlow Notify Service Order Update)
                setRouteUpdateServiceOrder(message)
            }
            break

        case "activity.released":
            externalId = jsonResult.data?.serviceCall?.activity?.externalId as String
            externalIds = getExternalIds(externalId, fallbackServiceOrder)
            setGlobalServiceOrderIds(message, externalIds)
            if (isEmpty(externalIds.ServiceOrder)) {
                // route: Irrelevant, ignore, no error
                setRouteIrrelevant(message)
            } else if (isEmpty(externalIds.ServiceOrderItem)) {
                // route: Raise error
                setRouteError(message, eventType, 'activity.externalId')
            } else {
                // route: Update Service Order
                //        (iFlow Notify Service Order Update)
                setRouteUpdateServiceOrder(message)
            }
            break

        case "activity.completed":
            externalId = jsonResult.data?.serviceCall?.activity?.unifiedIdentifier?.externalId as String
            externalIds = getExternalIds(externalId, fallbackServiceOrder)
            setGlobalServiceOrderIds(message, externalIds)
            if (isEmpty(externalIds.ServiceOrder)) {
                // route: Irrelevant, ignore, no error
                setRouteIrrelevant(message)
            } else if (isEmpty(externalIds.ServiceOrderItem)) {
                // route: Raise error
                setRouteError(message, eventType, 'activity.unifiedIdentifier.externalId')
            } else {
                // route: Update Service Order
                //        (iFlow Notify Service Order Update)
                setRouteUpdateServiceOrder(message)
            }
            break

        case ["activity.replannedreleased", "activity.releasedunassigned"]:
            externalId = jsonResult.data?.serviceCall?.sourceActivityCompleted?.unifiedIdentifier?.externalId as String
            if (isEmpty(externalId)) {
                externalId = jsonResult.data?.serviceCall?.activity?.sourceActivity?.externalId as String
            }
            externalIds = getExternalIds(externalId, fallbackServiceOrder)
            setGlobalServiceOrderIds(message, externalIds)
            if (isEmpty(externalIds.ServiceOrder)) {
                // route: Irrelevant, ignore, no error
                setRouteIrrelevant(message)
            } else if (isEmpty(externalIds.ServiceOrderItem)) {
                // route: Raise error
                setRouteError(message, eventType, 'sourceActivityCompleted.unifiedIdentifier.externalId, activity.sourceActivity.externalId')
            } else {
                // route: Create Service Item
                //        (iFlow Update Service Order for Duplicate Activity)
                setRouteCreateServiceItem(message)
                if (sourceActivityCompleted != null && completeCompatibilityMode) {
                    // route: + Source Activity Completed 
                    //        (iFlow Notify Service Order Update)
                    addRouteSourceActivityCompleted(message)
                }
            }
            break

        case ["activity.confirmed", "activity.confirmedcompleted"]:
            externalId = jsonResult.data?.serviceCall?.activity?.unifiedIdentifier?.externalId as String
            externalIds = getExternalIds(externalId, fallbackServiceOrder)
            setGlobalServiceOrderIds(message, externalIds)
            if (isEmpty(externalIds.ServiceOrder)) {
                // route: Irrelevant, ignore, no error
                setRouteIrrelevant(message)
            } else if (isEmpty(externalIds.ServiceOrderItem)) {
                // route: Raise error
                setRouteError(message, eventType, 'activity.unifiedIdentifier.externalId')
            } else {
                // route: Create Service Confirmantiom 
                //        (iFlow Replicate Confirmed Service)
                setRouteCreateServiceConfirmation(message)
                message.setProperty("originalPayload", body)
            }
            break

        default:
            // route: Irrelevant, ignore, no error
            setRouteIrrelevant(message)
            break
    }

    return message
}