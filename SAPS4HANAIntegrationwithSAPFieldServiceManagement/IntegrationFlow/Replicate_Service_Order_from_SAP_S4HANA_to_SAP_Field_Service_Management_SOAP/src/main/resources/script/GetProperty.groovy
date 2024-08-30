import com.sap.gateway.ip.core.customdev.util.Message
import java.util.*
import com.sap.it.api.mapping.*
import com.sap.it.api.mapping.MappingContext

// Return value of property prop_name or default_value if property is empty or does not exist
def String getPropertyOrDefault(String prop_name, String default_value, MappingContext context) {
    String value = context.getProperty(prop_name)
    if (value) {
        return value
    } else {
        return default_value
    }
}

// Return value of property prop_name or null if property is empty or does not exist
def String getPropertyOrNull(String prop_name, MappingContext context) {
    String value = context.getProperty(prop_name)
    if (value) {
        return value
    } else {
        return null
    }
}