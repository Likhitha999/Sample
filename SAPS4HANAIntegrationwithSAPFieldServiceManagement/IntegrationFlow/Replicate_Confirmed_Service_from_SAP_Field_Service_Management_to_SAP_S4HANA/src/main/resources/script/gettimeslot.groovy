import com.sap.it.api.mapping.*
import com.sap.it.api.mapping.MappingContext

def String getTimeslot(String startDateTime, String endDateTime) {
    def pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'"
    def start = Date.parse(pattern, startDateTime)
    def end = Date.parse(pattern, endDateTime)
    return Math.abs(end.getTime() - start.getTime()) // milliseconds
}