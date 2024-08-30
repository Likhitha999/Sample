import com.sap.gateway.ip.core.customdev.util.Message
import groovy.util.XmlParser
import com.sap.it.api.ITApiFactory
import com.sap.it.api.ITApi
import com.sap.it.api.mapping.ValueMappingApi

// translate status codes
String getFsmStatusCode(String s4Status){
    def service = ITApiFactory.getApi(ValueMappingApi.class, null)
    def fsmStatusCode = service.getMappedValue('S4HANA', 'ServiceOrderStatus', s4Status.toUpperCase(), 'FSM', 'ServiceCallStatusCode')
    if (fsmStatusCode == null || fsmStatusCode == '') {
        throw new Exception("The integration flow message won’t be processed further. No value mapping for (S4HANA, ServiceOrderStatus='" + s4Status.toUpperCase() + "', FSM, ServiceCallStatusCode)!")
    }
    return fsmStatusCode
}

// Fix date/time formatting
String fixIsoDateTime(String isoDateTime) {
    if ( isoDateTime != null && isoDateTime != '' ) {
        String dateTimeString = isoDateTime.replace('Z','')
        def pattern = ~/(.*?)\.(\d*)/
        def matches = pattern.matcher(dateTimeString).matches()
        if (matches) {
            return dateTimeString + 'Z'
        } else {
            return dateTimeString + '.000Z'
        }
    } else {
        return null
    }
}

def Message processData(Message message) {
    def body = message.getBody(java.lang.String) as String
    def parser = new XmlSlurper()
    def parserResult = parser.parseText(body)
    def service = ITApiFactory.getApi(ValueMappingApi.class, null)
    
    if ( parserResult != null ) {
        // service order status
        if ( parserResult.depthFirst().find{ it.ServiceOrderIsReleased.text() ==~ /(?i)(true|x)/ } ) {
            message.setProperty('ServiceOrderStatus','Released')
        }
        else if ( parserResult.depthFirst().find{ it.ServiceOrderIsCompleted.text() ==~ /(?i)(true|x)/ } || parserResult.depthFirst().find{ it.ServiceOrderIsRejected.text() ==~ /(?i)(true|x)/ } ) {
            message.setProperty('ServiceOrderStatus','Completed')
        } else {
            message.setProperty('ServiceOrderStatus','ReleaseRevoked')
        }
        // set matching FSM service call statuscode
        message.setProperty('ServiceCallStatusCode', getFsmStatusCode(message.getProperty('ServiceOrderStatus')))
        // fetch ServiceDocChangedDateTime
        try {
            def serviceDocChangedDateTime = parserResult.'**'.find { it.name() == 'ServiceDocChangedDateTime' }.text()
            message.setHeader('X-Source-Last-Changed', fixIsoDateTime(serviceDocChangedDateTime))
        } catch(Exception ex0) { }
        
        // check for flag SrvcRefObjIsMainObject
        if (parserResult.'**'.find { it.name() == 'ServiceReferenceObject' }) {
            if (parserResult.'**'.find { it.name() == 'SrvcRefObjIsMainObject' } ) {
                message.setProperty('SrvcRefObjIsMainObjectPresent', 'true')
            } else {
                message.setProperty('SrvcRefObjIsMainObjectPresent', 'false')
            }
        } else {
            //No Reference Object in Payload; no Additional read needed -> set to true
            message.setProperty('SrvcRefObjIsMainObjectPresent', 'true')
        }
        
        // check for flag CustMgmtPartnerIsMainPartner
        if ( parserResult.'**'.find { it.name() == 'CustMgmtPartnerIsMainPartner' } ) {
            message.setProperty('CustMgmtPartnerIsMainPartnerPresent', 'true')
        } else {
            message.setProperty('CustMgmtPartnerIsMainPartnerPresent', 'false')
        }
        
        // check for ContactPerson on item level
        if ( parserResult.ServiceOrder?.Item?.'**'.find { it.name() == 'ContactPerson' } ) {
            message.setProperty('ItemContactPersonPresent', 'true')
        } else {
            message.setProperty('ItemContactPersonPresent', 'false')
        }
        
        // check for ServiceOrderItemIsReleased on item level
        if ( parserResult.ServiceOrder?.Item?.'**'.find { it.name() == 'ServiceOrderItemIsReleased' } ) {
            message.setProperty('ServiceOrderItemIsReleasedPresent', 'true')
        } else {
            message.setProperty('ServiceOrderItemIsReleasedPresent', 'false')
        }
        
        message.setProperty('ServiceOrderContainsCompletedItem', 'false')
        // check for item status, but only if flag Released exists in S/4 release
        if (message.getProperty('ServiceOrderItemIsReleasedPresent') == 'true') {
            parserResult.ServiceOrder?.Item?.'**'.each { item ->
                // if the item is not released, it is supposed as completed
                if ( item.name() == 'ServiceOrderItemIsReleased' && !(item.text() ==~ /(?i)(true|x)/ )) {
                    message.setProperty('ServiceOrderContainsCompletedItem', 'true')
                } 
            }
        } else {
            // backwards compatibility
            message.setProperty('ServiceOrderContainsCompletedItem', 'true')
        }

        //Additional fetch of Service Order via Odata needed?
        def props = message.getProperties();
        if (props.get('ItemContactPersonPresent') == 'true' && 
            props.get('CustMgmtPartnerIsMainPartnerPresent') == 'true' && 
            props.get('SrvcRefObjIsMainObjectPresent') == 'true' && 
            props.get('ServiceOrderItemIsReleasedPresent') == 'true') {
                
            message.setProperty('fetchServiceOrderNeeded', 'false');
        } else {
            
            message.setProperty('fetchServiceOrderNeeded', 'true');
        }
        
        // Exception Handling Test
        if ( parserResult.depthFirst().find{ it.ProvokeException.text() ==~ /(?i)(true|x)/ } ) {
            throw new Exception("The integration flow message won’t be processed further. Provoked Exception for Exception Handling Test!")
        }
    }
    // determine FSM company
    if ( message.getProperty("CompanyName") == '' && message.getProperty("FSMCompanyID") == '' ) {
        def fsm_company = message.getProperty("DRFCompanyName")
        if ( fsm_company == '' ) {
            throw new Exception("The integration flow message won’t be processed further due to a missing configuration entry; provide a FSM Company via DRF configuration or via configuration parameter FSM Company Name!")
        } else {
            message.setProperty("CompanyName", fsm_company)
        }
    }
    // check if transaction type is supported
    message.setProperty('TransactionTypeSupported', 'false')
    def transactionType = message.getProperty('TransactionType')
    if (transactionType != null && transactionType != '') {
        String serviceCallType = service.getMappedValue('ServiceOrderS4HANA', 'TransactionType', transactionType , 'ServiceCallFSM', 'Type')
        if (serviceCallType != null && serviceCallType != '') {
            message.setProperty('TransactionTypeSupported', 'true')
        }
    }

    return message
}