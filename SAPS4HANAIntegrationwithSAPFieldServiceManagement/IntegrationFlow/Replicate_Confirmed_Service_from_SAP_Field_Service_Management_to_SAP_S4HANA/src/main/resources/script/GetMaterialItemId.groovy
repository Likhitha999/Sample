import com.sap.it.api.mapping.*;

def String GetMaterialItemId(String externalid){
	def materialitemid = externalid.split('/')[1] as String;
	return materialitemid 
}