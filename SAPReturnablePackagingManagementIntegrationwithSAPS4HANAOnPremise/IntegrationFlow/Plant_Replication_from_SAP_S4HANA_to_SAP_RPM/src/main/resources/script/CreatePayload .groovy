import com.sap.gateway.ip.core.customdev.util.Message;
import groovy.json.JsonSlurper;
import groovy.json.JsonOutput;
import org.apache.commons.lang3.text.translate.UnicodeUnescaper;

def Message processData(Message message) {
  def body = message.getBody(String.class)
  def jsonSlurper = new JsonSlurper()
  def object = jsonSlurper.parseText(body)

  // Checking if there is only one record
  if (!isCollectionOrArray(object.plantReplicationRequestMessage.plant)) {
    // Converting record to an array
    object.plantReplicationRequestMessage.plant = [object.plantReplicationRequestMessage.plant].toArray()
  }
  
  // Set the total no. of records to exchange property 'total_records'
  message.setProperty('total_records', object.plantReplicationRequestMessage.plant.size())

  // Set DRF bulk message id and sender system id as properties
  message.setProperty('messageID', object.messageID)
  message.setProperty('senderID', object.senderID)

  def finalPayload = JsonOutput.toJson(object.plantReplicationRequestMessage.plant)
  
  // Unescape unicode characters
  def decodedString = new UnicodeUnescaper().translate(finalPayload)

  // Prepare Message Body
  message.setBody(decodedString)

  // Prepare Message Header
  message.setHeader('Content-Type', "application/json")

  return message
}

// Returns true if object is an array
boolean isCollectionOrArray(object) {
  [Collection, Object[]].any {
    it.isAssignableFrom(object.getClass())
  }
}