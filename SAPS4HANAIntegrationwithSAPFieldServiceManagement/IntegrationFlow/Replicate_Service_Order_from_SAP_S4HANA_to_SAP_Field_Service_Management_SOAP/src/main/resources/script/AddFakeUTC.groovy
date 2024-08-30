import com.sap.it.api.mapping.*

def String AddFakeUTC(String dateTimeString) {
  def result
  if (dateTimeString != "") {
    result = (dateTimeString.endsWith('Z')) ? dateTimeString : dateTimeString + 'Z'
  }
  return result
}