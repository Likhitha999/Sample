import com.sap.gateway.ip.core.customdev.util.Message

def Message processData(Message message) {
    def serviceProductItemCategory,serviceProductItemCategoryQueryStatement
    def itemCategoryTemp, itemCategoryList

    serviceProductItemCategory = message.getProperty("ServiceProductItemCategory") as String

    if (serviceProductItemCategory == null || serviceProductItemCategory.trim().size() == 0) serviceProductItemCategoryQueryStatement = "and (ServiceOrderItemCategory eq 'SVP1' or ServiceOrderItemCategory eq 'SRVP')"
    else{
        itemCategoryList = serviceProductItemCategory.split('\\|')
        serviceProductItemCategoryQueryStatement = "and ("
        itemCategoryList.each { item ->
            itemCategoryTemp = item.trim()
            if (itemCategoryTemp.size() == 0) return
            serviceProductItemCategoryQueryStatement += "ServiceOrderItemCategory eq '$itemCategoryTemp' or "
        }
        serviceProductItemCategoryQueryStatement = serviceProductItemCategoryQueryStatement[0..-5] + ')'
    }

    message.setProperty("ServiceProductItemCategoryQueryStatement",serviceProductItemCategoryQueryStatement)
    return message
}

