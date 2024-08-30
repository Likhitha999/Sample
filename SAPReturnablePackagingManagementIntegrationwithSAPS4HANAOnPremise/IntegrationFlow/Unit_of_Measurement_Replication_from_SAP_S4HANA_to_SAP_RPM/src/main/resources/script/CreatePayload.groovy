import com.sap.gateway.ip.core.customdev.util.Message;
import groovy.json.JsonSlurper;
import groovy.json.JsonOutput;
import org.apache.commons.lang3.text.translate.UnicodeUnescaper;

def Message processData(Message message) {
  def body = message.getBody(String.class)
  def jsonSlurper = new JsonSlurper()
  def object = jsonSlurper.parseText(body)

  if (!isCollectionOrArray(object.uomReplicationRequestMessage)) {
    //Converting record to an array
    object.uomReplicationRequestMessage = [object.uomReplicationRequestMessage].toArray()
  }

  object.uomReplicationRequestMessage.each {
    uomReplicationMessage ->
      if (!isCollectionOrArray(uomReplicationMessage.uomRecord)) {
        //Converting record to an array
        uomReplicationMessage.uomRecord = [uomReplicationMessage.uomRecord].toArray()
      }
  }

  // Iterate over uomReplicationRequestMessage array and combine all uomRecord
  def combinedUoM = object.uomReplicationRequestMessage.collect {
    record ->
      record.uomRecord
  }.flatten()
  
  combinedUoM.each {
    record ->
    if (record.uomTexts != null && !record.uomTexts.isEmpty()) {
      if (!isCollectionOrArray(record.uomTexts)) {
        // Converting record to an array
        record.uomTexts = [record.uomTexts]
      }
    }
  }

  // Set the total no. of records to exchange property 'total_records'
  message.setProperty('total_records', combinedUoM.size())

  def finalPayload = JsonOutput.toJson(combinedUoM)

  // Unescape unicode characters
  def decodedString = new UnicodeUnescaper().translate(finalPayload)
 
  // Prepare Message Body
  message.setBody(decodedString)

  // Prepare Message Header
  message.setHeader('Content-Type', "application/json")

  return message
}

//Returns true if object is an array
boolean isCollectionOrArray(object) {
  [Collection, Object[]].any {
    it.isAssignableFrom(object.getClass())
  }
}