import com.sap.it.api.mapping.*;
import java.util.*;
import java.text.*;
import java.math.*;

def String parseTimeInDecimal(String input, String EffortsInMinutes) {
    
    Double output = Double.parseDouble(input)
    DecimalFormat df = new DecimalFormat("0.00")
    
    boolean isEffortsInMinutes = Boolean.parseBoolean(EffortsInMinutes)
    
   if (isEffortsInMinutes) {
       
        def minutes = output * 60
         df.setRoundingMode(RoundingMode.HALF_UP)
        return df.format(minutes)
        
        
    } else {
        df.setRoundingMode(RoundingMode.DOWN)
        return df.format(output)
    }
}