import com.sap.gateway.ip.core.customdev.util.Message;
import java.util.HashMap;
import groovy.json.JsonSlurper;
import groovy.json.JsonOutput;
import groovy.xml.XmlUtil

def Message processData(Message message) { 
    def body = message.getBody(String.class)
    def jsonSlurper = new JsonSlurper()
    def object = jsonSlurper.parseText(body)
    float duration, plannedDuration;
    String startDate, endDate;
   
    String serviceOrderId       = message.getHeaders().get("ServiceOrder") ?: ''
    String serviceOrderItem     = message.getHeaders().get("ServiceOrderItem") ?: ''
    String eventType            = message.getHeaders().get("EventType") ?: ''
    String itemDescription      = object.data?.serviceCall?.activity?.subject?.take(40) ?: ''
    String activityId           = object.data?.serviceCall?.activity?.id
    String longText             = object.data?.serviceCall?.activity?.remarks?: ""
    String fSMServiceCall       = object.data.serviceCall.unifiedIdentifier.code
    String fSMServiceActivity   = object.data.serviceCall.activity.code
    String itemExecutionStatus  = "02"; //Status: In Planning
    
    //Creation case: activity.created without sourceActivity
    if (object.eventType == "activity.created" && object.data.serviceCall.activity.sourceActivity == null) {
        message.setProperty("CreateRoute", "true");
    } else {
        message.setProperty("DuplicateRoute", "false");
    }
    
    if (object.data.serviceCall.activity.contact != null && object.data.serviceCall.activity.contact.externalId != null){
        message.setProperty("ContactPerson", object.data.serviceCall.activity.contact.externalId);
    }


    if (object.data.serviceCall.activity.plannedStartDate != null){
        startDate = object.data.serviceCall.activity.plannedStartDate;
        message.setProperty("PlannedStartDate", startDate);
    }
    

    if (object.data.serviceCall.activity.plannedEndDate != null){
        endDate = object.data.serviceCall.activity.plannedEndDate  
        message.setProperty("PlannedEndDate", endDate);
    }
    
    if (object.data.serviceCall.activity.durationInMinutes != null) {
        duration = Math.round(object.data.serviceCall.activity.durationInMinutes / 60 * 100) / 100;
    }
        
    if (object.data.serviceCall.activity.plannedDurationInMinutes != null) {
        plannedDuration = Math.round(object.data.serviceCall.activity.plannedDurationInMinutes / 60 * 100) / 100;
    }

    //fallback in case no duration is given
    if (duration == 0.0 && plannedDuration == 0.0) {

        if (startDate && endDate) {
            def startDateParsed = Date.parse("yyyy-MM-dd'T'HH:mm:ss", startDate)
            def endDateParsed = Date.parse("yyyy-MM-dd'T'HH:mm:ss", endDate)
            //get diff in milliseconds convert to hours and round
            duration = Math.round(((endDateParsed.getTime() - startDateParsed.getTime()) / 1000 / 60 / 60) * 100) / 100;
        }
        
    }
        
    message.setProperty("Quantity", duration ?: plannedDuration)
    message.setProperty("PlannedDuration", plannedDuration ?: duration)
    
    if  (object.data.serviceCall.activity.earliestStartDateTime != null){
        message.setProperty("RequestedStartDate", object.data.serviceCall.activity.earliestStartDateTime)
    }
        
    if  (object.data.serviceCall.activity.dueDateTime != null){
        message.setProperty("DueDate", object.data.serviceCall.activity.dueDateTime)
        message.setProperty("RequestedEndDate", object.data.serviceCall.activity.dueDateTime)
    }
    
    if (object.data?.serviceCall?.activity?.serviceProduct?.externalId != null){
        message.setProperty("Product",object.data?.serviceCall?.activity?.serviceProduct?.externalId)
    }


    // reference object: equipment or functional location
    if (object.data?.serviceCall?.activity?.equipment != null){
        def ref_object = object.data?.serviceCall?.activity?.equipment?.externalId
        def ref_object_category = object.data?.serviceCall?.activity?.equipment?.objectCategory
        if (ref_object) {
            if (ref_object_category == null || ref_object_category == 'EQ') {
                message.setProperty("Equipment", ref_object)
                message.setProperty("FunctionalLocation", "")
            } else if (ref_object_category == 'FLOC') {
                message.setProperty("Equipment", "")
                message.setProperty("FunctionalLocation", ref_object)
            }
        } else {
            message.setProperty("Equipment", "")
            message.setProperty("FunctionalLocation", "")
        }
    }
  
    if (eventType == "activity.replannedreleased" && object.data.serviceCall.activity.responsibles[0].externalId != null){
        String executingServiceEmployee = object.data.serviceCall.activity.responsibles[0].externalId
        message.setProperty("ExecutingServiceEmployee", executingServiceEmployee)
        itemExecutionStatus = "04"; //Status: In Execution
    }

    // Text Handling
    def textType = message.getProperty('ServiceOrderINoteType') ?: '0002'       
    def language = message.getProperty('ServiceOrderNoteLang') ?: 'EN'

    //* Build Payload for Text Node 
    String node = ""
    if (textType != null) {
        node = node + '''<A_ServiceOrderItemTextType>'''
        node = node + '''<ServiceOrder>''' + serviceOrderId + '''</ServiceOrder>'''
        node = node + '''<ServiceOrderItem>000000</ServiceOrderItem>'''
        node = node + '''<Language>'''     + language  + '''</Language>'''
        node = node + '''<LongTextID>'''   + textType + '''</LongTextID>'''
        node = node + '''<LongText>'''     + groovy.xml.XmlUtil.escapeXml(longText) + '''</LongText>'''
        node = node + '''</A_ServiceOrderItemTextType>'''        
    }

    message.setProperty("node",               node)
    message.setProperty("ServiceOrderId",     serviceOrderId)
    message.setProperty("ServiceOrderItem",   serviceOrderItem)
    message.setProperty("ItemDescription",    groovy.xml.XmlUtil.escapeXml(itemDescription))
    message.setProperty("ActivityId",         activityId)
    message.setProperty("AppError",           'A')
    message.setProperty("FSMServiceCall",     fSMServiceCall)
    message.setProperty("FSMServiceActivity", fSMServiceActivity)
    message.setProperty("ItemExecutionStatus", itemExecutionStatus)
    return message;
}