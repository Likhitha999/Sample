import com.sap.it.api.mapping.*

def String BuildItemExternalId(String itemNo, String itemCategory, String reservedMaterial, MappingContext context) {
    String ServiceOrderID = context.getHeader("ServiceOrderID")
    def result = ServiceOrderID + "/" + itemNo
    return result
}