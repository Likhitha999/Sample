import com.sap.gateway.ip.core.customdev.util.Message
import java.util.regex.*
import groovy.xml.XmlUtil

packageName = "Integration of Service in SAP S/4HANA with SAP Field Service Management"
sendEmailIflowId = "Send_Error_Email_for_Integration_SAP_S4HANA_with_SAP_Field_Service_Management"
sendEmailIflowName = "Send Error Email for Integration SAP S4HANA with SAP Field Service Management"
iflowWithEmail = []
isEmailNotifEnabled = false
indent = "\t"

Boolean is_empty(String value, String default_value) {
  if (value.length() == 0 || value == default_value || value == groovy.xml.XmlUtil.escapeXml(default_value)) {
    return true
  } else {
    return false
  }
}

Boolean is_true(String value) {
  return (value ==~ /(?i)(true|x)/)
}

Boolean is_false(String value) {
  return (value ==~ /(?i)(false| )/)
}

String type_check(String value, String type, Integer size, Integer exact){
  String msg = ''
  def email_parts = []
  def email_error = false
  def dom_parts = []
  def i = 0
  def last = 0  
  switch(type) {
    case 'string':
      if (exact > -1 && value.length() != exact) {
        msg = 'The value "' + value + '" of the parameter "__PARAMETER__" must have ' + exact + ' characters. '
      } else if (size > -1 && value.length() > size) {
        msg = 'The value "' + value + '" of the parameter "__PARAMETER__" exceeds ' + size + ' characters. '
      }
      break
    case 'boolean':
      if (!(is_true(value) || is_false(value))) {
        msg = 'The value "' + value + '" of the parameter "__PARAMETER__" must be Boolean. '
      }
      break
    case 'number':
      if (exact > -1 && value.length() != exact) {
        msg = 'The value "' + value + '" of the parameter "__PARAMETER__" must have ' + exact + ' characters. '
      } else if (size > -1 && value.length() > size) {
        msg = 'The value "' + value + '" of the parameter "__PARAMETER__" exceeds ' + size + ' characters. '
      }
      if (!(value.isNumber())) {
        msg += 'The value "' + value + '" of the parameter "__PARAMETER__" must be a number. '
      }
      break
    case 'url':
      if (!(value ==~ /^(https|http):\/\/[-a-zA-Z0-9+&@#%=~_|!,.;]*:?([0-9]*)\/+([-a-zA-Z0-9+&@#%=~_\/|\$?\{\}\.]*)$/)) {
        msg = 'The value "' + value + '" of the parameter "__PARAMETER__" must be a URL. '
      }
      break
    case 'hostname':
      if (!(value ==~ /^(https|http):\/\/[-a-zA-Z0-9+&@#%=~_|!,.;]*:?([0-9]*)\/?$/)) {
        msg = 'The value "' + value + '" of the parameter "__PARAMETER__" must be a hostname. '
      }
      break
    case 'emailhostname':
      if (!(value ==~ /^[-a-zA-Z0-9+&@#%?=~_|!,.;]*:?([0-9]*)$/)) {
        msg = 'The value "' + value + '" of the parameter "__PARAMETER__" must be an email hostname. '        
      }
      break
    case 'email':
      // to avoid ReDos issues of an evil regex split in parts and check individually 
      email_parts = value.split('@')
      if (email_parts.size() == 2) {
        if (!(email_parts[0] ==~ /[_A-Za-z0-9-]+[_A-Za-z0-9-.+]*/)) {
          email_error = true
        } else {
          dom_parts = email_parts[1].split('\\.')
          last = dom_parts.size() - 1
          while (!email_error && i <= last) {
            if (i < last) {
              email_error = !(dom_parts[i] ==~ /[A-Za-z0-9-]+/)
            } else {
              email_error = !(dom_parts[i] ==~ /[A-Za-z]{2,}/)
            }
            i = i + 1
          }
        }
      } else {
        email_error = true
      }
      if (email_error) {
        msg = 'The value "' + value + '" of the parameter "__PARAMETER__" must be an email address. '
      }
      break
    }
    return msg
}

String get_description(Map parameter_checks, String parameter) {
    Map param = parameter_checks.get(parameter) ?: [:]
    if (param == [:]) {
        return ''
    } else {
        return (param.description ?: parameter)
    }
}

String get_default(Map parameter_checks, String parameter) {
    Map param = parameter_checks.get(parameter) ?: [:]
    if (param == [:]) {
        return ''
    } else {
        return (param.default ?: '')
    }
}

String execute_checks(String iflow_id, Map parameter_checks, Map parameter_values) {
    String final_result = ''
    // collect groups
    Map groups_map = [:]
    parameter_checks.each{ parameter -> 
        String param = parameter.key
        Map param_properties = parameter.value ?: [:] 
        String group = ''
        List param_list = []
        if (param_properties.containsKey('group')) {
            group = param_properties.group
            if (groups_map.containsKey(group)) {
                param_list = groups_map.get(group)
                param_list.add(param)
                groups_map.put(group, param_list)
            } else {
                param_list.add(param)
                groups_map.put(group, param_list)
            }
        }
    }
    // check loop
    parameter_checks.each{ parameter -> 
        String param = parameter.key
        Map param_properties = parameter.value ?: [:]
        String type = param_properties.type ?: 'string'
        String description = param_properties.description ?: param
        String separator = param_properties.separator ?: ''
        Integer size = param_properties.size ?: -1
        Integer exact = param_properties.exact ?: -1
        Boolean mandatory = param_properties.mandatory ?: false
        String alternative = param_properties.alternative ?: ''
        String alternative_value = parameter_values[alternative] ?: ''
        String group = param_properties.group ?: ''
        String default_value = param_properties.default ?: ''
        String alternative_default_value = ''
        String alternative_description = ''
        List values = param_properties.values ?: []
        List mandatoryif = param_properties.mandatoryif ?: []
        String raw_value = parameter_values[param] ?: ''
        List valuelist = []
        List param_list = []
        String group_par_val = ''
        String group_par_description = ''
        String result = ''
        String type_result = ''
        String check_result = ''

        if (alternative != '') {
            // either this parameter or the alternative is mandatory
            Map alt_param = parameter_checks.get(alternative) ?: [:]
            if (alt_param == [:]) {
                result += 'Wrong check definition for iflow ' + iflow_id + ' and parameter "__PARAMETER__": alternative parameter "' + alternative + '" not found. '
            } else {
                alternative_default_value = alt_param.default ?: ''
                alternate_description = alt_param.description ?: alternative
                if (is_empty(raw_value, default_value) && is_empty(alternative_value, alternative_default_value)) {
                    result += 'You must enter a value either for the parameter "__PARAMETER__" or for the alternative parameter "' + alternate_description + '". '
                } else if (!is_empty(raw_value, default_value) && !is_empty(alternative_value, alternative_default_value)) {
                    result += 'You entered values for both the parameter "__PARAMETER__" and the parameter "' + alternate_description + '". Enter only one of the two values. '
                }
            }
        }
        if (mandatoryif.size() > 0) {
          String dep_parameter = mandatoryif[0]
          if (parameter_values.containsKey(dep_parameter)) {
            String dep_val = parameter_values[dep_parameter] ?: ''
            Map alt_param = parameter_checks.get(dep_parameter) ?: [:]
            String dep_parameter_description = alt_param.description ?: dep_parameter
            if (mandatoryif.size() == 1) { // no values given, check parameter not empty
              Map dep_param = parameter_checks.get(dep_parameter) ?: [:]
              if (dep_param == [:]) {
                result += 'Wrong check definition for iflow ' + iflow_id + ' and parameter "__PARAMETER__": dependant parameter "' + dep_parameter_description + '" not found. '
              } else {
                  String dep_default_value = dep_param.default ?: ''
                  if (is_empty(raw_value, default_value) && !is_empty(dep_val, dep_default_value)) {
                    result += 'You must specify the parameter "__PARAMETER__" if the parameter "' + dep_parameter_description + '" has been set. '
                  }
              }
            } else {
              mandatoryif.eachWithIndex{ val, idx ->
                if (idx > 0) {
                  if (is_true(val)) {
                    if (is_true(dep_val) && is_empty(raw_value, default_value) ) {
                        result += 'You must specify the parameter "__PARAMETER__" if the value of the parameter "' + dep_parameter_description + '" is "' + val + '". '
                    }
                  } else if (is_false(val)) {
                    if (is_false(dep_val) && is_empty(raw_value, default_value) ) {
                        result += 'You must specify the parameter "__PARAMETER__" if the value of the parameter "' + dep_parameter_description + '" is "' + val + '". '
                    }
                  } else if (dep_val == val && is_empty(raw_value, default_value) ) {
                        result += 'You must specify the parameter "__PARAMETER__" if the value of the parameter "' + dep_parameter_description + '" is "' + val + '". '
                  }
                }
              }
            }
          } else {
            result += 'Wrong check definition for iflow ' + iflow_id + ' and parameter "__PARAMETER__": the dependent parameter "'+ dep_parameter + '" is missing. '
          }
        }
        if (mandatory == true) {
            if (is_empty(raw_value, default_value)) {
                result += 'You must specify the parameter "__PARAMETER__". '
            }
        }
        if (group != '') {
            param_list = groups_map.get(group)
            Integer group_par_val_len = 0
            String group_par_val_default = ''
            String group_par_str = ''
            Integer i = 0
            String sep = '"'
            param_list.each{ group_par ->
                if (param != group_par) {
                    group_par_val = parameter_values[group_par] ?: ''
                    Map group_parameter = parameter_checks.get(group_par)
                    group_par_val_default = group_parameter.default ?: ''
                    group_par_description = group_parameter.description ?: group_par
                    if (!is_empty(group_par_val, group_par_val_default)) {
                      group_par_val_len += group_par_val.length()
                    }
                    sep = (i > 0) ? ' or "' : '"'
                    group_par_str += sep + group_par_description + '"'
                    i++
                }
            }
            if (group_par_val_len > 0 && is_empty(raw_value, default_value)) {
                result += 'You must specify the parameter "__PARAMETER__" if the parameter ' + group_par_str + ' has been set. '
            }
        }
        if (separator != '' && raw_value.size() > 0) {
            String ind_type_result = ''
            valuelist = raw_value.split(separator)*.trim()
            if (raw_value.size() > 0 && valuelist.size() == 0) {
              type_result = 'The value "' + raw_value + '" of the parameter "__PARAMETER__" is not allowed. '
            } else {
              for (value in valuelist) {
                  ind_type_result = ''
                  if (values.size() > 0) {
                      if (!(values.contains(value))) {
                          ind_type_result = 'The value "' + value + '" of the parameter "__PARAMETER__" is not allowed. Possible values are: ' + values + '. '
                      }
                  } else if (value.size() > 0) {
                      ind_type_result = type_check(value, type, size, exact)
                  } else if (value.size() == 0) {
                      ind_type_result = 'The value "" of the parameter "__PARAMETER__" is not allowed. '
                  }
                  type_result += ind_type_result
              }
            }
        } else if (!is_empty(raw_value, default_value)) {
            if (values.size() > 0) {
                if (!(values.contains(raw_value))) {
                    type_result = 'The value "' + raw_value + '" of the parameter "__PARAMETER__" is not allowed. Possible values are: ' + values + '. '
                }
            } else {
                type_result = type_check(raw_value, type, size, exact)
            }
        }
        if (result != '' || type_result != '') {
            check_result = result + type_result
            final_result += indent + '[ERROR] ' + check_result.replace("__PARAMETER__", description) + '\n'
        }
    }
    return final_result
}

String timeout_check(Map parameter_checks, Map parameter_values, String parameter) {
    String timeout = parameter_values[parameter] ?: ''
    if (!(timeout.isNumber())) {
        return indent + '[ERROR] The value "' + timeout + '" of the parameter "' + get_description(parameter_checks, parameter) + '" must be a number. \n'
    }
    try {
        Integer int_time_out = timeout as Integer
        if (int_time_out == 0) {
            return indent + '[WARNING] The value "' + timeout + '" of the parameter "' + get_description(parameter_checks, parameter) + '" specifies that the client will wait indefinitely.\n'+
                   indent + '          This can lead to excessive execution times of the integration flow. We recommend using the default value of 60000 ms.\n' 
        } else if (int_time_out > 60000) {
            return indent + '[WARNING] The value "' + timeout + '" of the parameter "' + get_description(parameter_checks, parameter) + '" is greater than 60000 ms.\n'+
                   indent + '          This can lead to excessive execution times of the integration flow. We recommend using the default value of 60000 ms.\n'
        } else if (int_time_out < 60000) {
            return indent + '[WARNING] The value "' + timeout + '" of the parameter "' + get_description(parameter_checks, parameter) + '" is smaller than 60000 ms.\n'+
                   indent + '          This can lead to instable execution of the integration flow. We recommend using the default value of 60000 ms.\n'
        } else {
            return ''
        }
    } catch(Exception ex) {
        return ''
    }
}

String timeout_check_minutes(Map parameter_checks, Map parameter_values, String parameter) {
    String timeout = parameter_values[parameter] ?: ''
    if (!(timeout.isNumber())) {
        return indent + '[ERROR] The value "' + timeout + '" of the parameter "' + get_description(parameter_checks, parameter) + '" must be a number. \n'
    }
    try {
        Integer int_time_out = timeout as Integer
        if (int_time_out > 1) {
            return indent + '[WARNING] The value "' + timeout + '" of the parameter "' + get_description(parameter_checks, parameter) + '" is greater than 1 min.\n'+
                   indent + '          This can lead to excessive execution times of the integration flow. We recommend using the default value of 1 min.\n'
        } else if (int_time_out < 1) {
            return indent + '[WARNING] The value "' + timeout + '" of the parameter "' + get_description(parameter_checks, parameter) + '" is smaller than 1 min.\n'+
                   indent + '          This can lead to instable execution of the integration flow. We recommend using the default value of 1 min.\n'
        } else {
            return ''
        }
    } catch(Exception ex) {
        return ''
    }
}

String extension_warning(Map parameter_checks, Map parameter_values, String parameter, String address_parameter) {
    String enabled = parameter_values[parameter] ?: ''
    String address = parameter_values[address_parameter] ?: ''
    if (enabled ==~ /(?i)(true|x)/ ) {
        String default_value = get_default(parameter_checks, address_parameter)
        if (is_empty(address, default_value)) {
            return indent + '[WARNING] You\'ve enabled the parameter "' + get_description(parameter_checks, address_parameter) + '". Ensure that a corresponding customer integration flow exists and is deployed. \n'
        } else {
            return indent + '[WARNING] You\'ve enabled the parameter "' + get_description(parameter_checks, address_parameter) + '". Ensure that customer integration flow with the address "' + parameter_values[address_parameter] + '" exists and is deployed. \n'
        }
    } else {
        return ''
    }
}

String role_warning(Map parameter_checks, Map parameter_values, String parameter) {
    def userRole = parameter_values[parameter]
    if (userRole != 'ESBMessaging.send') {
        return indent + '[WARNING] Parameter "' + get_description(parameter_checks, parameter) + '": Make sure that the role "' + userRole + '" has the required authorizations. \n'
    } else {
        return ''
    }
}

String user_warning(Map parameter_checks, Map parameter_values, String parameter, String account_name_parameter, String company_name_parameter){
    def user = parameter_values[parameter]
    if (user.length() == 0){
        return indent + '[ERROR] You must specify the parameter "' + get_description(parameter_checks, parameter) + '" if the parameters "' + get_description(parameter_checks, account_name_parameter) + '" and "' + get_description(parameter_checks, company_name_parameter) + '" have been set. \n'
    } else {
        return ''
    }
}

String execute_special_checks(String iflow_id, Map parameter_checks, Map parameter_values) {
    def result = ''
    String[] idParts = iflow_id.split('\\.');

    switch(idParts[0]) {
        case 'Replicate_Attachments_from_SAP_Field_Service_Management_to_SAP_S4HANA':
        //tab "Sender"
            result += role_warning(parameter_checks, parameter_values, "User Role")
        //tab "More"
            if (parameter_values["Error Email Notification"] ==~ /(?i)(true|x)/ ) {
                isEmailNotifEnabled = true
                iflowWithEmail.add(iflow_id)
            }
            break
        case 'Notify_Service_Order_Update_from_SAP_Field_Service_Management_to_SAP_S4HANA':
        //tab "Receiver"
            result += timeout_check_minutes(parameter_checks, parameter_values, "Timeout")
        //tab "More"
            if (parameter_values["Error Email Notification"] ==~ /(?i)(true|x)/ ) {
                isEmailNotifEnabled = true
                iflowWithEmail.add(iflow_id)   
            }
            result += extension_warning(parameter_checks, parameter_values, "extensionEnable", "extensionAddress")
            result += extension_warning(parameter_checks, parameter_values, "Header Extension Enabled", "Header Extension Address")
            break
        case 'Replicate_Confirmed_Service_from_SAP_Field_Service_Management_to_SAP_S4HANA':
        //tab "More"
            if (parameter_values["Error Email Notification"] ==~ /(?i)(true|x)/ ) {
                isEmailNotifEnabled = true
                iflowWithEmail.add(iflow_id)   
            }
            result += extension_warning(parameter_checks, parameter_values, "Post Exit Enable", "Address")
            String fsm_host = parameter_values["FSM Host"] ?: ''
            String fsm_host_default = get_default(parameter_checks, "FSM Host")
            if (is_empty(fsm_host, fsm_host_default)) {
                result += indent + '[INFO] You have to maintain the parameters "' + get_description(parameter_checks, "FSM Host") + '", "' +  get_description(parameter_checks, "FSM Credentials") + '", \n ' + 
                          indent + '       "' + get_description(parameter_checks, "FSM Account ID") + '" and "' + get_description(parameter_checks, "FSM Company ID") + '". \n' +
                          indent + '       ' + 'This is required if you want to enable the status change for service items when corresponding activities are closed \n' +
                          indent + '       ' + 'and all related time efforts, expenses, materials, and mileage are approved and transferred to SAP S/4HANA. \n'
            } else {
                result += timeout_check(parameter_checks, parameter_values, "FSM Timeout")
            }
            break
        case 'Replicate_Service_Call_from_SAP_Field_Service_Management_to_SAP_S4HANA':
        //tab "More"
            if (parameter_values["Error Email Notification"] ==~ /(?i)(true|x)/ ) {
                isEmailNotifEnabled = true
                iflowWithEmail.add(iflow_id)
            }
            result += extension_warning(parameter_checks, parameter_values, "Post Mapping Extension Enabled", "Post Mapping Extension Address")
            if (!is_empty(parameter_values["FSM Account Name"], get_default(parameter_checks, "FSM Account Name")) && !is_empty(parameter_values["FSM Company Name"], get_default(parameter_checks, "FSM Company Name"))) {
                result += user_warning(parameter_checks, parameter_values, "FSM User", "FSM Account Name", "FSM Company Name")
            }
            break
        case 'Replicate_Service_Contract_from_SAP_S4HANA_to_SAP_Field_Service_Management':
        //tab "Sender"
            result += role_warning(parameter_checks, parameter_values, "Authorization_User_Role")
        //tab "More"
            if (parameter_values["Error Email Notification"] ==~ /(?i)(true|x)/ ) {
                isEmailNotifEnabled = true
                iflowWithEmail.add(iflow_id)   
            }
            result += extension_warning(parameter_checks, parameter_values, "Post Mapping Extension Enabled", "Post Mapping Extension Address")
            break
        case 'Replicate_Service_Order_from_SAP_S4HANA_to_SAP_Field_Service_Management_SOAP':
        //tab "Sender"
            result += role_warning(parameter_checks, parameter_values, "User Role")
        //tab "Receiver"
            result += timeout_check(parameter_checks, parameter_values, "FSM HTTP Timeout")
        //tab "More"
            if (parameter_values["Error Email Notification"] ==~ /(?i)(true|x)/ ) {
                isEmailNotifEnabled = true
                iflowWithEmail.add(iflow_id)   
            }
            result += extension_warning(parameter_checks, parameter_values, "Post Extension Enabled", "Post Extension Address")
            if (!is_empty(parameter_values["FSM Account Name"], get_default(parameter_checks, "FSM Account Name")) && !is_empty(parameter_values["FSM Company Name"], get_default(parameter_checks, "FSM Company Name"))) {
                result += user_warning(parameter_checks, parameter_values, "FSM User", "FSM Account Name", "FSM Company Name")
            }
            break
        case 'Route_Payload_from_SAP_Field_Service_Management_to_SAP_S4HANA':
        //tab "Sender"
            result += role_warning(parameter_checks, parameter_values, "User Role")
        //tab "More"
            if (parameter_values["Error Email Notification"] ==~ /(?i)(true|x)/ ) {
                isEmailNotifEnabled = true
                iflowWithEmail.add(iflow_id)   
            }
            break
        case 'Update_Service_Order_for_Duplicate_Activity_from_SAP_Field_Service_Management_to_SAP_S4HANA':
        //tab "More"
            if (parameter_values["Error Email Notification"] ==~ /(?i)(true|x)/ ) {
                isEmailNotifEnabled = true
                iflowWithEmail.add(iflow_id)   
            }
            result += extension_warning(parameter_checks, parameter_values, "Post Mapping Extension Enabled", "Post Mapping Extension Address")
            result += extension_warning(parameter_checks, parameter_values, "Creation Post Mapping Extension Enabled", "Creation Post Mapping Extension Address")
            if (!is_empty(parameter_values["account"], get_default(parameter_checks, "account")) && !is_empty(parameter_values["company"], get_default(parameter_checks, "company"))) {
                result += user_warning(parameter_checks, parameter_values, "user", "account", "company")
            }
            break
    }    
    return result
}

String build_iflow_with_email_list(List iflow_id_list, Map package_parameter_values) {
    String result = ''
    String iflow_name = ''
    iflow_id_list.each{
        iflow_name = package_parameter_values[it]['__NAME__']
        result += '\t\t\t' + iflow_name + '\n'
    }
    return result
}

String execute_package_checks(Map package_parameter_checks, Map package_parameter_values, String package_id) {
    String iflow_result = ''
    String finalResult = ''
    String result = 'This report checks the configuration of the integration flows in the package "' + packageName + '" \nwith the package ID "' + package_id + '".\n' +
        "These checks are executed at design time in SAP Cloud Integration.\n"+
        "If you detect any errors, navigate to the corresponding configuration parameter of the affected integration flow, correct it, and save it.\n"+
        "After you’ve corrected all errors, deploy the integration flows.\n\n"
    String email_result = ''
    isEmailNotifEnabled = false
    def iflow_id_list = []    
    package_parameter_checks.each{ pc -> 
        String iflow_id_check = pc.key // base iflow id defined in the check 
        Map parameter_checks = pc.value
        // loop through all iflows matching base iflow id from checks
        iflow_id_list = package_parameter_values.keySet().findAll{ it.startsWith(iflow_id_check) }
        iflow_id_list.each{ iflow_id -> 
            Map parameter_values = package_parameter_values[iflow_id] ?: [:]
            if (parameter_values.size() > 0) {
                result += '\n' + package_parameter_values[iflow_id]['__NAME__'] + '\n\n'
                iflow_result = execute_checks(iflow_id, parameter_checks, parameter_values)
                iflow_result += execute_special_checks(iflow_id, parameter_checks, parameter_values)
                if (iflow_result != '') {
                    if(iflow_id_check == sendEmailIflowId) {
                        email_result = iflow_result
                        iflow_result = "__EMAIL_RESULT__"
                    }
                    result += iflow_result
              } else {
                result += indent + '[INFO] OK\n'
              }
            }
        }
    }
    if (isEmailNotifEnabled == false) {
        // no INFO for iFlow "Send Error Email ..."
        finalResult = result.replaceFirst(/__EMAIL_RESULT__/, indent + '[INFO] OK\n')
    } else {
        String email_info = email_result + indent + "[INFO] Integration flows with activated email notifications are:\n\n__IFLOWS_WITH_EMAIL__\n" +
                            indent + "       If you don't want to send error email notifications, deactivate the parameter \"More - Error Email Notification\" in all integration flows.\n"
                            indent + "       As a result, the integration flow \"" + sendEmailIflowName + "\" won't be checked anymore.\n"
        result = result.replaceFirst(/__EMAIL_RESULT__/, email_info)    
        String iflows_with_email = build_iflow_with_email_list(iflowWithEmail, package_parameter_values)
        finalResult = result.replace(/__IFLOWS_WITH_EMAIL__/, iflows_with_email)
    }
    return finalResult
}

def Message processData(Message message) {

    Map package_parameter_checks = message.getProperty('PackageParameterChecks')
    Map package_parameter_values = message.getProperty('packageConfig')
    String package_id = message.getProperty('PackageID')
    
    def report = execute_package_checks(package_parameter_checks.sort(), package_parameter_values, package_id)

    if (report != '') {
        // save the result of the configuration check as a log attachment
        def messageLog = messageLogFactory.getMessageLog(message)
        messageLog.addAttachmentAsString('ConfigurationCheckResult', report, 'text/plain')
    }

    return message
}