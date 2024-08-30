import com.sap.it.api.mapping.*;

def String addHyphen(String value){
    value = value[0..7]+'-'+value[8..11]+'-'+value[12..15]+'-'+value[16..19]+'-'+value[20..31]
	return value 
}