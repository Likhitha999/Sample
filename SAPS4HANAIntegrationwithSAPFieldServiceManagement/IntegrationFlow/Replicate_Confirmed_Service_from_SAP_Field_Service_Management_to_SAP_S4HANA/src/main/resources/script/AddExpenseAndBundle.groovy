import com.sap.gateway.ip.core.customdev.util.Message
import java.util.HashMap
import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import groovy.xml.*
import com.sap.it.api.mapping.*
import com.sap.it.api.ITApiFactory
import com.sap.it.api.ITApi
import com.sap.it.api.mapping.ValueMappingApi

def Message processData(Message message) {
    // Get Service Order Items body 
    def body = message.getBody(java.lang.String) as String
    def xmlSlurper = new XmlSlurper()
    def oServiceOrderBody = xmlSlurper.parseText(body)
    def oServiceOrderItemBody = oServiceOrderBody
      
    // Get Service Call/Activity body
    def oActivityBody = message.getProperty("ActivityPayload")
    def jsonSlurper = new JsonSlurper()
    oActivityBody = jsonSlurper.parseText(oActivityBody)
    def service = ITApiFactory.getApi(ValueMappingApi.class, null)
    String val = ""
    def oServiceOrderType = oServiceOrderBody.A_ServiceOrderType.ServiceOrderType.text()
    def fsmActivityExternalID = oActivityBody.activity.unifiedIdentifier.externalId as String
    def serviceOrderItemId = message.getHeaders().get("ServiceOrderItem") ?: '';
     
    message.setProperty("ServiceOrderType", oServiceOrderType)
    
    // filter out unplanned order items for the check, as they should not be considered as planned items
    def oServiceOrderBodyFiltered = oServiceOrderBody.A_ServiceOrderType.to_Item.A_ServiceOrderItemType.findAll{ it.SrvcOrdItemIsUnplanned != true };
      
    // convert activity external id to Service Order Id
    // if Service Bundle exists: take Service Order Id / Bundle Id
    if (fsmActivityExternalID) {
        externalIds = fsmActivityExternalID.split('/')
        fsmActivityExternalID = (externalIds.size() > 2) ? externalIds[0] + "/" + externalIds[1] : externalIds[0]
        fsmServiceOrderItem   = externalIds[1]
    } 

    // set expense efforts and determine unplanned case
    oActivityBody.activity.expenses.eachWithIndex{ expense, expenseIndex->
        val = service.getMappedValue('Activity', 'Expensetype', expense.type?.code , 'ServiceConfirmationItem', 'Expensetype')
               
        tmpSetBySubItem = false  
        expense.tempItemIsUnplanned = null;
        
        oServiceOrderBodyFiltered.eachWithIndex{ A_ServiceOrderItemType, ServiceOrderItemIndex ->
            oServiceOrder           = A_ServiceOrderItemType.ServiceOrder as String
            oServiceOrderItem       = A_ServiceOrderItemType.ServiceOrderItem as String
            oServiceOrderItemType   = A_ServiceOrderItemType.Product as String
            oParentServiceOrderItem = A_ServiceOrderItemType.ParentServiceOrderItem as String    
            
            // determine reference service order item number item that has same product and is assigned to the same main item or has no main item     
            // items with main item has priority
            if ( val == oServiceOrderItemType && ( fsmServiceOrderItem == oParentServiceOrderItem || oParentServiceOrderItem == '0' )) {
                if ( tmpSetBySubItem == false ) {
                    
                    expense.tempReferenceItemId = oServiceOrderItem
                    expense.tempProductId = null;
                    expense.tempItemIsUnplanned = false;
                    
                    if (!( oParentServiceOrderItem == '0' || oParentServiceOrderItem == 0)) {
                        tmpSetBySubItem = true     
                    }
                }
            } 
        }

        // tempItemIsUnplanned was not set to null -> no planned item was found
        if (expense.tempItemIsUnplanned == null) {
            expense.tempProductId = val;
            expense.tempItemIsUnplanned = true;
            expense.tempReferenceItemId = serviceOrderItemId;
        }
    }
    
    // set mileage efforts and determine unplanned case
    oActivityBody.activity.mileages.eachWithIndex{ mileage, mileageIndex->
        val = service.getMappedValue('Activity', 'Expensetype', 'mileage' , 'ServiceConfirmationItem', 'Expensetype')
               
        tmpSetBySubItem = false        
        mileage.tempItemIsUnplanned = null
        
        oServiceOrderBodyFiltered.eachWithIndex{ A_ServiceOrderItemType, ServiceOrderItemIndex ->
            oServiceOrder           = A_ServiceOrderItemType.ServiceOrder as String
            oServiceOrderItem       = A_ServiceOrderItemType.ServiceOrderItem as String
            oServiceOrderItemType   = A_ServiceOrderItemType.Product as String
            oParentServiceOrderItem = A_ServiceOrderItemType.ParentServiceOrderItem as String    
            
            // determine reference service order item number item that has same product and is assigned to the same main item or has no main item     
            // items with main item has priority
            if ( val == oServiceOrderItemType && ( fsmServiceOrderItem == oParentServiceOrderItem || oParentServiceOrderItem == '0' )) {
                if ( tmpSetBySubItem == false ) {
                    
                    mileage.tempReferenceItemId = oServiceOrderItem
                    mileage.tempProductId = null;
                    mileage.tempItemIsUnplanned = false;
                    
                    if (!( oParentServiceOrderItem == '0' || oParentServiceOrderItem == 0)) {
                        tmpSetBySubItem = true     
                    }
                }
            }
        }

        // tempItemIsUnplanned was not set to null -> no planned item was found
        if (mileage.tempItemIsUnplanned == null) {
            mileage.tempProductId = val;
            mileage.tempItemIsUnplanned = true;
            mileage.tempReferenceItemId = serviceOrderItemId;
        }
    } 
      
    // set material efforts and determine unplanned case
    oActivityBody.activity.materials.eachWithIndex{ material, materialsIndex->
        
        tmpSetBySubItem = false    
        material.tempItemIsUnplanned = null
        
        oServiceOrderBodyFiltered.eachWithIndex{ A_ServiceOrderItemType, ServiceOrderItemIndex ->
            oServiceOrder            = A_ServiceOrderItemType.ServiceOrder as String
            oServiceOrderItem        = A_ServiceOrderItemType.ServiceOrderItem as String
            oServiceOrderItemProduct = A_ServiceOrderItemType.Product as String
            oParentServiceOrderItem  = A_ServiceOrderItemType.ParentServiceOrderItem as String
            
            // determine reference service order item number item that has same product and is assigned to the same main item or has no main item     
            // items with main item has priority
            if( material.item.externalId == oServiceOrderItemProduct && ( fsmServiceOrderItem == oParentServiceOrderItem || oParentServiceOrderItem == '0' )){
                if ( tmpSetBySubItem == false ) {
                    
                    material.tempReferenceItemId = oServiceOrderItem
                    material.tempProductId = null;
                    material.tempItemIsUnplanned = false;
                    
                    if (!( oParentServiceOrderItem == '0' || oParentServiceOrderItem == 0)) {
                        tmpSetBySubItem = true     
                    }
                }
            }       
        }

        // tempItemIsUnplanned was not set to null -> no planned item was found
        if (material.tempItemIsUnplanned == null) {
            material.tempProductId = material.item.externalId;
            material.tempItemIsUnplanned = true;
            material.tempReferenceItemId = serviceOrderItemId;
        }
    }
    
    def result = JsonOutput.toJson(oActivityBody)
    message.setBody(result)
    return message
}