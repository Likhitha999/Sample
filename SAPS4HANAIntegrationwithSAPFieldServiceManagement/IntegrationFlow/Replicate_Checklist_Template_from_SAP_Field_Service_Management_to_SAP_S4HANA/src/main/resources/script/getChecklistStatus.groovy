import com.sap.it.api.mapping.*;

def String getChecklistStatus(String eventType,String status,String inactive){
    if(eventType == 'checklisttemplate.activated' && status == 'ACTIVE' && inactive == 'false'){
        return 'Template_Released';
    }else if(eventType == 'checklisttemplate.deactivated' && status == 'INACTIVE' && inactive == 'false'){
        return 'Template_Deactivated';
    }else if(eventType == 'checklisttemplate.deactivated' && status == 'INACTIVE' && inactive == 'true'){
        return 'Template_Deleted'
    }
}