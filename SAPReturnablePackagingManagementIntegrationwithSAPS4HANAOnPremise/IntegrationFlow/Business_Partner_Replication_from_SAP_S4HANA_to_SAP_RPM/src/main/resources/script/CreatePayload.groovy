import com.sap.gateway.ip.core.customdev.util.Message;
import groovy.json.JsonSlurper;
import groovy.json.JsonOutput;
import org.apache.commons.lang3.text.translate.UnicodeUnescaper;

def Message processData(Message message) {
  def body = message.getBody(String.class)
  def jsonSlurper = new JsonSlurper()
  def object = jsonSlurper.parseText(body)

  // Checking if there is only one record
  if (!isCollectionOrArray(object.replicationBusinessPartnerRequest)) {
    // Converting record to an array
    object.replicationBusinessPartnerRequest = [object.replicationBusinessPartnerRequest].toArray()
  }

  // Set DRF bulk message id and sender system id as properties
  message.setProperty('messageID', object.messageID)
  message.setProperty('senderID', object.senderID)

  // Extracting data without 'replicationBusinessPartnerRequest' key
  def request = object.replicationBusinessPartnerRequest.collect {
    item ->
      item.remove('replicationBusinessPartnerRequest')
    return item
  }
  
  // Set the total no. of records to exchange property 'total_records'
  message.setProperty('total_records', request.size())

  //Set address field if addressData is available
  request.each {
    item ->
      if (item.addressData != null) {
        item.address = item.addressData.address
        if (item.addressData.phoneDetails != null) {
          if (!isCollectionOrArray(item.addressData.phoneDetails)) {
            //Converting record to an array
            item.addressData.phoneDetails = [item.addressData.phoneDetails].toArray()
          }

          // Set mobile number based on mobilePhoneIndicator
          def mobileNumber = item.addressData.phoneDetails.find {
            phone ->
              phone.mobilePhoneIndicator == 'true'
          }?.phoneNumber
          item.addressData.mobile = mobileNumber
		  
		  // Remove phoneDetails from payload
          item.addressData.remove('phoneDetails')
        }
      }
  }

  def finalPayload = JsonOutput.toJson(request)
  
  // Unescape unicode characters
  def decodedString = new UnicodeUnescaper().translate(finalPayload)

  // Prepare Message Body
  message.setBody(decodedString)

  // Prepare Message Header
  message.setHeader('Content-Type', 'application/json')

  return message
}

// Returns true if object is an array
boolean isCollectionOrArray(object) {
  [Collection, Object[]].any {
    it.isAssignableFrom(object.getClass())
  }
}