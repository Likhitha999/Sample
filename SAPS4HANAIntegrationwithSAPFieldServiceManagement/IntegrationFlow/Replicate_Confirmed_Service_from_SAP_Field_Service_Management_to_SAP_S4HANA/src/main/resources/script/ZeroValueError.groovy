import com.sap.gateway.ip.core.customdev.util.Message;
import java.util.*;
import com.sap.it.api.mapping.*;
import com.sap.it.api.mapping.MappingContext;

//This method throws an exception in case the input value is zero/blank
def String ZeroValueError(String value,MappingContext context) {

    if(!value || value == "0" || value == "0.0" ) {
        throw new Exception("The integration flow message won’t be processed further. Distance mandatory, mileage approved without distance. Service Confirmation not sent to target system");
    } else {
        return value;
    }

}