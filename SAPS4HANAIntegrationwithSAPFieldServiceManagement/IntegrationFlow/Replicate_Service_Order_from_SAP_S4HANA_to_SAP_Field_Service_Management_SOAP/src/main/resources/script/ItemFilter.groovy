import com.sap.it.api.mapping.*;

def String ItemFilter(String value, String itemCategory, String reservedMaterial, MappingContext context){
    
    def ActivitiesCategories = context.getProperty("ItemCategoriesForFSMActivities")
    def ReservedMaterialsCategories = context.getProperty("ItemCategoriesForFSMReservedMaterials")
    def result = null
    def allowedCategories = ''

    if (reservedMaterial ==~ /(?i)(true|x)/ ) {
        if (ReservedMaterialsCategories != null && ReservedMaterialsCategories != '') {
            allowedCategories = ReservedMaterialsCategories.split("\\|")*.trim()
        }
    } else {
        if (ActivitiesCategories != null && ActivitiesCategories !='') {
            allowedCategories = ActivitiesCategories.split("\\|")*.trim()
        }
    }   
    
    if (allowedCategories.contains(itemCategory)) {
        result = value
    }
	return result 
}