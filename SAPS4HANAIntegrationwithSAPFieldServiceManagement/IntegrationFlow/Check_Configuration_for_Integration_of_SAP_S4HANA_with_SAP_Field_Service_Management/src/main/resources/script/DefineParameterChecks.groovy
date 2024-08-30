import com.sap.gateway.ip.core.customdev.util.Message;
import java.util.HashMap;
def Message processData(Message message) {

Map package_parameter_checks = [
  'Notify_Service_Order_Update_from_SAP_Field_Service_Management_to_SAP_S4HANA': [
    'Header Extension Enabled': [
      'description': 'More - Header Extension Enabled',
      'type': 'boolean',
      'mandatory': true
    ],
    'Service Order Note Language': [
      'description': 'More - Service Order Note Language',
      'size' : 2,
      'mandatory': true
    ],
    'Client Id': [
      'description': 'Receiver: Service_Order_Item_Update - Client Id',
      'mandatory': true
    ],
    'Address': [
      'description': 'Receiver: Service_Order_Item_Update - Address',
      'type': 'url',
      'mandatory': true,
      'default' : 'http://<s4host>:<s4port>/sap/opu/odata/sap/API_SERVICE_ORDER_SRV'
    ],
    'Timeout': [
      'description': 'Receiver: Service_Order_Item_Update - Timeout (in min)',
      'type': 'number',
      'mandatory': true
    ],
    'CredentialName': [
      'description': 'Receiver: Service_Order_Item_Update - Credential Name',
      'mandatoryif': [ 'Authentication', 'Basic' ],
      'default' : '<SAP S/4HANA Credential Name>'
    ],
    'LocationID': [
      'description': 'Receiver: Service_Order_Item_Update - Location ID',
      'mandatoryif': [ 'ProxyType', 'On-Premise']
    ],
    'Service Order Header Note Type': [
      'description': 'More - Service Order Header Note Type',
      'mandatory': true,
      'size' : 4
    ],
    'ProcessDirectAddress': [
      'description': 'Sender: SAP_FSM - Address',
      'mandatory': true
    ],
    'Service Order Header Problem Description Type': [
      'description': 'More - Service Order Header Problem Description Type',
      'mandatory': true,
      'size' : 4
    ],
    'Header Extension Address': [
      'description': 'More - Header Extension Address',
      'mandatoryif': [ 'Header Extension Enabled', 'true']
    ],
    'extensionAddress': [
      'description': 'More - extensionAddress',
      'mandatoryif': [ 'extensionEnable', 'true']
    ],
    'Private Key Alias': [
      'description': 'Receiver: Service_Order_Item_Update - Private Key Alias',
      'mandatoryif': [ 'Authentication', 'Client Certificate']
    ],
    'Authentication': [
        'description': 'Receiver: Service_Order_Item_Update - Authentication'
    ],
    'Error Log Attachments': [
      'description': 'More - Error Log Attachments',
      'type': 'boolean',
      'mandatory': true
    ],
    'Error Email Notification': [
      'description': 'More - Error Email Notification',
      'type': 'boolean',
      'mandatory': true
    ],
    'extensionEnable': [
      'description': 'More - extensionEnable',
      'type': 'boolean',
      'mandatory': true
    ],
    'User Status for Completed Activity': [
      'description': 'More - User Status for Completed Activity',
      'size': 5
    ],
    'ProxyType': [
        'description': 'Receiver: Service_Order_Item_Update - Proxy Type'
    ],
    'Service Order Item Note Type': [
      'description': 'More - Service Order Item Note Type',
      'mandatory': true,
      'size' : 4
    ],
    'Rejection Reason Code': [
      'description': 'More - Rejection Reason Code',
      'size': 2
    ],
    'Header Batch Extension Enabled': [
      'description': 'More - Header Batch Extension Enabled',
      'type': 'boolean',
      'mandatory': true
    ],
    'Item Batch Extension Enabled': [
      'description': 'More - Item Batch Extension Enabled',
      'type': 'boolean',
      'mandatory': true
    ],
    'Header Batch Extension Address': [
      'description': 'More - Header Batch Extension Address',
      'mandatoryif': [ 'Header Batch Extension Enabled', 'true']
    ],
    'Item Batch Extension Address': [
      'description': 'More - Item Batch Extension Address',
      'mandatoryif': [ 'Item Batch Extension Enabled', 'true']
    ],
  ],
  'Replicate_Attachments_from_SAP_Field_Service_Management_to_SAP_S4HANA': [
    'Client Id': [
      'description': 'Receiver: S4Hana - Client Id',
      'mandatory': true
    ],
    'S4HANA Proxy Type': [
        'description': 'Receiver: S4Hana - Proxy Type',
    ],
    'FSM Host': [
      'description': 'Receiver: FSM_Attachment - FSM Host',
      'type': 'hostname',
      'mandatory': true,
      'default' : 'https://<cluster.coresuite.com>'
    ],
    'S4HANA Host': [
      'description': 'Receiver: S4Hana - S4HANA Host',
      'type': 'hostname',
      'mandatory': true,
      'default' : 'http://<S4Host>:<S4Port>'
    ],
    'User Role': [
      'description': 'Sender: FSM - User Role',
      'mandatory': true
    ],
    'S4HANA Credentials': [
      'description': 'Receiver: S4Hana - Credential Name',
      'mandatoryif': ['S4HANA Authentication', 'Basic'],
      'default' : '<SAP S/4HANA Credential Name>'
    ],
    'FSM Company ID': [
      'description': 'More - FSM Company ID',
      'group': 'ID',
      'type': 'number',
      'alternative' : 'FSM Company Name',
      'default': '<FSM_Company_ID>'
    ],
    'FSM Account Name': [
      'description': 'More - FSM Account Name',
      'group': 'Name',
      'alternative' : 'FSM Account ID',
      'default': '<FSM Account Name>'
    ],
    'Endpoint': [
      'description': 'Sender: FSM - Address',
      'mandatory': true,
      'description' : 'Address'
    ],
    'Private Key Alias': [
      'description': 'Receiver: S4Hana - Private Key Alias',
      'mandatoryif': [ 'S4HANA Authentication', 'Client Certificate']
    ],
    'FSM Company Name': [
      'description': 'More - FSM Company Name',
      'group': 'Name',
      'alternative' : 'FSM Company ID',
      'default': '<FSM Company Name>'
    ],
    'Error Email Notification': [
      'description': 'More - Error Email Notification',
      'type': 'boolean',
      'mandatory': true
    ],
    'S4HANA Authentication': [
      'description': 'Receiver: S4Hana - Authentication',
    ],
    'Error Log Attachments': [
      'description': 'More - Error Log Attachments',
      'type': 'boolean',
      'mandatory': true
    ],
    'S4HANA Location ID': [
      'description': 'Receiver: S4Hana - Location ID',
      'mandatoryif': [ 'S4HANA Proxy Type', 'On-Premise']
    ],
    'FSM Account ID': [
      'description': 'More - FSM Account ID',
      'group': 'ID',
      'type': 'number',      
      'alternative' : 'FSM Account Name',
      'default': '<FSM_Account_ID>'
    ],
    'FSM Credentials': [
      'description': 'Receiver: FSM_Attachment - Credential Name',
      'mandatory': true,
      'default' : '<FSM Credentials>'
    ]
  ],
  'Replicate_Confirmed_Service_from_SAP_Field_Service_Management_to_SAP_S4HANA': [
    'Client Id': [
      'description': 'Receiver: ReadServiceOrder - Client Id',
      'mandatory': true
    ],
    'Address': [
      'description' : 'Receiver: Customer_Iflow - Address',
      'mandatoryif': [ 'Post Exit Enable', 'true'],
      'default' : '<customer_iflow_address>'
    ],
    'Address_For_ServiceOrder': [
      'description': 'Receiver: ReadServiceOrder - Address',
      'type': 'url',
      'mandatory': true,
      'default' : 'http://<s4host>:<s4port>/sap/opu/odata/sap/API_SERVICE_ORDER_SRV'
    ],
    'Service Confirmation Note Language': [
      'description': 'More - Service Confirmation Note Language',
      'mandatory': true,
      'size' : 2
    ],
    'Location_ID': [
      'description': 'Receiver: ReadServiceOrder - Location ID',
      'mandatoryif': [ 'Proxy_Type_For_Visiting_S4', 'On-Premise']
    ],
    'Service Confirmation Note Type': [
      'description': 'More - Service Confirmation Note Type',
      'mandatory': true,
      'size' : 4
    ],
    'ProcessDirectAddress': [
      'description': 'Sender - Address',
      'mandatory': true
    ],
    'Private Key Alias': [
      'description': 'Receiver: ReadServiceOrder - Private Key Alias',
      'mandatoryif': [ 'Authentication', 'Client Certificate']
    ],
    'Authentication': [
      'description': 'Receiver: ReadServiceOrder - Authentication',    
    ],
    'Address_For_ServiceConfirmation': [
      'description': 'Receiver: Create_Service_Confirmation - Address',
      'type': 'url',
      'mandatory': true,
      'default' : 'http://<s4host>:<s4port>/sap/opu/odata/sap/API_SERVICE_CONFIRMATION_SRV'
    ],
    'Error Email Notification': [
      'description': 'More - Error Email Notification',
      'type': 'boolean',
      'mandatory': true
    ],
    'Error Log Attachments': [
      'description': 'More - Error Log Attachments',
      'type': 'boolean',
      'mandatory': true
    ],
    'Proxy_Type_For_Visiting_S4': [
      'description': 'Receiver: ReadServiceOrder - Proxy Type'
    ],
    'Credential_For_ServiceConfirmation': [
      'description': 'Receiver: Create_Service_Confirmation - Credential Name',
      'mandatoryif': [ 'Authentication', 'Basic' ],
      'default' : '<SAP S/4HANA Credential Name>'
    ],
    'Credential_For_ServiceOrder': [
      'description': 'Receiver: ReadServiceOrder - Credential Name',
      'mandatory': [ 'Authentication', 'Basic' ],
      'default' : '<SAP S/4HANA Credential Name>'
    ],
    'Item Category for Mileage': [
      'description': 'More - Item Category for Mileage',
      'size': 4,
      'mandatory': true
    ],
    'Post Exit Enable': [ 
      'description': 'More - Post Exit Enable',
      'type': 'boolean',
      'mandatory': true
    ],
    'FSM Host': [
      'description': 'Receiver: FSM_Confirm_Time_and_Material - FSM Host',
      'type': 'hostname',
      'default': 'https://customer-cluster.coresuite.com'
    ],
    'FSM Credentials': [
      'description': 'Receiver: FSM_Confirm_Time_and_Material - Credential Name',
      'default': '<FSM Credentials>',
      'mandatoryif': ['FSM Host']
    ],
    'FSM Timeout': [
      'description': 'Receiver: FSM_Confirm_Time_and_Material - Timeout (in ms)',
      'type': 'number',
      'mandatoryif': ['FSM Host']
    ],
    'FSM Account ID': [
      'description': 'More - FSM Account ID',
      'type': 'number',
      'default': '<FSM_Account_ID>',
      'mandatoryif': ['FSM Host']
    ],
    'FSM Company ID': [
      'description': 'More - FSM Company ID',
      'type': 'number',
      'default': '<FSM_Company_ID>',
      'mandatoryif': ['FSM Host']
    ]    
  ],
  'Replicate_Service_Call_from_SAP_Field_Service_Management_to_SAP_S4HANA': [
    'Service Order Note Language': [
      'description': 'More - Service Order Note Language',
      'mandatory': true,
      'size' : 2
    ],
    'address': [
      'description': 'Sender - Address',
      'mandatory': true
    ],
    'Client Id': [
      'description': 'Receiver: S4HANA_CreateServiceOrder - Client Id',
      'mandatory': true
    ],
    'Credential Name': [
      'description': 'Receiver: S4HANA_CreateServiceOrder - Credential Name',
      'mandatoryif': [ 'Authentication', 'Basic' ],
      'default' : '<SAP S/4HANA Credential Name>'
    ],
    'S4HANA Host': [
      'description': 'Receiver: S4HANA_CreateServiceOrder - S4HANA Host',
      'type': 'hostname',
      'mandatory': true,
      'default' : 'http://<S4Host>:<S4Port>'
    ],
    'FSM Host': [
      'description': 'Receiver: SAP_FSM_Update - FSM Host',
      'type': 'hostname',
      'mandatory': true,
      'default' : 'https://<cluster.coresuite.com>'
    ],
    'Service Order Header Note Type': [
      'description': 'More - Service Order Header Note Type',
      'mandatory': true,
      'size' : 4
    ],
    'Proxy Type': [
      'description': 'Receiver: S4HANA_CreateServiceOrder - Proxy Type',
    ],
    'FSM User': [
      'description': 'More - FSM User',
      'default' : '<FSM User>'
    ],
    'FSM HTTP Timeout': [
      'type': 'number',
      'mandatory': true
    ],
    'FSM Company ID': [
      'description': 'More - FSM Company ID',
      'group': 'ID',
      'type': 'number',
      'alternative' : 'FSM Company Name',
      'default': '<FSM_Company_ID>'
    ],
    'FSM Account Name': [
      'description': 'More - FSM Account Name',
      'group': 'Name',
      'alternative' : 'FSM Account ID',
      'default': '<FSM Account Name>'
    ],
    'Location ID': [
      'description': 'Receiver: S4HANA_CreateServiceOrder - Location ID',
      'mandatoryif': [ 'Proxy Type', 'On-Premise']
    ],
    'Service Order Header Problem Description Type': [
      'description': 'More - Service Order Header Problem Description Type',
      'mandatory': true,
      'size' : 4
    ],
    'FSM Company Name': [
      'description': 'More - FSM Company Name',
      'group': 'Name',
      'alternative' : 'FSM Company ID',
      'default': '<FSM Company Name>'
    ],
    'Private Key Alias': [
      'description': 'Receiver: S4HANA_CreateServiceOrder - Private Key Alias',
      'mandatoryif' : [ 'Authentication', 'Client Certificate']
    ],
    'Authentication': [
      'description': 'Receiver: S4HANA_CreateServiceOrder - Authentication',    
    ],
    'Error Log Attachments': [
      'description': 'More - Error Log Attachments',
      'type': 'boolean',
      'mandatory': true
    ],
    'Error Email Notification': [
      'description': 'More - Error Email Notification',
      'type': 'boolean',
      'mandatory': true
    ],
    'FSM Account ID': [
      'description': 'More - FSM Account ID',
      'group': 'ID',
      'type': 'number',
      'alternative' : 'FSM Account Name',
      'default': '<FSM_Account_ID>'
    ],
    'Post Mapping Extension Address': [
      'description': 'Receiver: Customer_Post_Mapping_Extension - Address',
      'mandatoryif': [ 'Post Mapping Extension Enabled', 'true']
    ],
    'FSM Credentials': [
      'description': 'Receiver: SAP_FSM_Update - Credential Name',
      'mandatory': true,
      'default' : '<FSM Credentials>'
    ],
    'Post Mapping Extension Enabled': [
      'description': 'More - Post Mapping Extension Enabled',
      'type': 'boolean',
      'mandatory': true
    ]
  ],
  'Replicate_Service_Order_from_SAP_S4HANA_to_SAP_Field_Service_Management_SOAP': [
    'Post Extension Address': [
      'description': 'Receiver: Customer_Post_Mapping_Extension_iFlow - Address',
      'mandatoryif': [ 'Post Extension Enabled', 'true'],
      'default' : '/'
    ],
    'Service Order Note Language': [
      'description': 'More - Service Order Note Language',
      'mandatory': true,
      'size' : 2
    ],
    'S4HANA Proxy Type': [
        'description': 'Receiver: S4HANA_Update - Proxy Type',
    ],
    'FSM Host': [
      'description': 'Receiver: FSM_Create_ServiceCall_with_Activities - FSM Host',
      'type': 'hostname',
      'mandatory': true,
      'default' : 'https://<cluster.coresuite.com>'
    ],
    'Service Order Header Note Type': [
      'description': 'More - Service Order Header Note Type',
      'mandatory': true,
      'size' : 4
    ],
    'User Role': [
      'description': 'Sender - User Role',
      'mandatory': true
    ],
    'SubjectDN': [
      'description': 'Sender: S4SOAP - Subject DN',
      'mandatoryif': [ 'Authorization', 'Client Certificate'],
      'default' : '<Subject_DN>'
    ],
    'FSM HTTP Timeout': [
      'description': 'Receiver: FSM_Create_ServiceCall_with_Activities - Timeout (in ms)',
      'type': 'number',
      'mandatory': true
    ],
    'S4HANA Credentials': [
      'description': 'Receiver: S4HANA_Update - Credential Name',
      'mandatoryif': [ 'S4HANA Authentication', 'Basic' ],
      'default' : '<SAP S/4HANA Credential Name>'
    ],
    'Authorization': [
        'description': 'Sender: S4SOAP - Authentication',    
    ],
    'FSM Company ID': [
      'description': 'More - FSM Company ID',
      'group': 'ID',
      'type': 'number',
      'alternative' : 'FSM Company Name',
      'default': '<FSM_Company_ID>'
    ],
    'Service Order Header Problem Description Type': [
      'description': 'More - Service Order Header Problem Description Type',
      'mandatory': true,
      'size' : 4
    ],
    'Endpoint': [
      'description' : 'Sender: S4SOAP - Address',
      'mandatory': true
    ],
    'FSM Company Name': [
      'description': 'More - FSM Company Name',
      'group': 'Name',
      'alternative' : 'FSM Company ID',
      'default': '<FSM Company Name>'
    ],
    'FSM Account ID': [
      'description': 'More - FSM Account ID',
      'group': 'ID',
      'type': 'number',
      'alternative' : 'FSM Account Name',
      'default': '<FSM_Account_ID>'
    ],
    'Item Categories For FSM Activities': [
      'description': 'More - Item Categories For FSM Activities',
      'separator': '\\|',
      'size': 4,
      'mandatory': true
    ],
    'Service Order Item Note Type': [
      'description': 'More - Service Order Header Item Note Type',
      'mandatory': true,
      'size' : 4
    ],
    'Client Id': [
      'description': 'Receiver: S4HANA_Update - Client Id',
      'mandatory': true
    ],
    'S4HANA Host': [
      'description': 'Receiver: S4HANA_Update - S4HANA Host',
      'default' : 'http://<S4Host>:<S4Port>',
      'type': 'hostname',
      'mandatory': true
    ],
    'Item Categories For FSM Reserved Materials': [
      'description': 'More - Item Categories For FSM Reserved Materials',
      'separator': '\\|',
      'size': 4,
      'mandatory': true
    ],
    'Post Extension Enabled': [
      'description': 'More - Post Extension Enabled',
      'type': 'boolean',
      'mandatory': true
    ],
    'FSM User': [
      'description': 'More - FSM User',
      'default': '<FSM User>'
    ],
    'FSM autoCreateActivity': [
      'description': 'More - FSM autoCreateActivity',
      'type': 'boolean',
      'mandatory': true
    ],
    'FSM Account Name': [
      'description': 'More - FSM Account Name',
      'group': 'Name',
      'alternative' : 'FSM Account ID',
      'default': '<FSM Account Name>'
    ],
    'Issuer_DN': [
      'description': 'Sender: S4SOAP - Issuer DN',
      'mandatoryif': [ 'Authorization', 'Client Certificate'],
      'default' : '<Issuer_DN>'
    ],
    'Private Key Alias': [
      'description': 'Receiver: S4HANA_Update - Private Key Alias',
      'mandatoryif': [ 'S4HANA Authentication', 'Client Certificate']
    ],
    'Error Log Attachments': [
      'description': 'More - Error Log Attachments',
      'type': 'boolean',
      'mandatory': true
    ],
    'S4HANA Authentication': [
      'description': 'Receiver: S4HANA_Update - Authentication',
    ],
    'Error Email Notification': [
      'description': 'More - Error Email Notification',
      'type': 'boolean',
      'mandatory': true
    ],
    'S4HANA Location ID': [
      'description': 'Receiver: S4HANA_Update - Location ID',
      'mandatoryif': [ 'S4HANA Proxy Type', 'Internet']
    ],
    'FSM Credentials': [
      'description': 'Receiver: FSM_Create_ServiceCall_with_Activities - Credential Name',
      'mandatory': true,
      'default' : '<FSM Credentials>'
    ]
  ],
  'Route_Payload_from_SAP_Field_Service_Management_to_SAP_S4HANA': [
    'ProcessDirectSODuplicateAct': [
      'description': 'Receiver: Update_Service_Order_Create_Item - Address',
      'mandatory': true
    ],
    'Address Create Service Order': [
      'description': 'Receiver: Create_Service_Order - Address',
      'mandatory': true
    ],
    'Consider HTTP 409 as Locking Issue': [
      'description': 'More - Consider HTTP 409 as Locking Issue',
      'type': 'boolean',
      'mandatory': true
    ],
    'Address': [
      'description': 'Sender: FSM - Address',
      'mandatory': true
    ],
    'Service Call Status Used in Replication': [
      'description': 'More - Service Call Status Used in Replication',
      'separator': '\\|',
      'mandatory': true
    ],
    'Error Email Notification': [
      'description': 'More - Error Email Notification',
      'type': 'boolean',
      'mandatory': true
    ],
    'Error Log Attachments': [
      'description': 'More - Error Log Attachments',
      'type': 'boolean',
      'mandatory': true
    ],
    'ProcessDirectServiceOrder': [
      'description': 'Receiver: Update_Service_Order_For_Complete_Service_Item - Address',
      'mandatory': true
    ],
    'User Role': [
      'description': 'Sender: FSM - User Role',
      'mandatory': true
    ],
    'ProcessDirectServiceConfirmation': [
      'description': 'Receiver: Create_Service_Confirmation - Address',
      'mandatory': true
    ]
  ],
  'Send_Error_Email_for_Integration_SAP_S4HANA_with_SAP_Field_Service_Management': [
    'Email Server Address': [
      'description': 'Receiver: Email_Receiver - Address',
      'mandatory': true,
      'type': 'emailhostname',      
      'default' : '<SMTP_host:port>'
    ],
    'Secret Key Length': [
      'description': 'Receiver: Email_Receiver - Secret Key Length',
      'mandatoryif': [ 'Email Content Encryption Algorithm', 'AEY/CBC/PKCS5Padding']
    ],
    'Email Authentication Method': [
      'description': 'Receiver: Email_Receiver - Authentication',
    ],
    'Email Content Encryption Algorithm': [
      'description': 'Receiver: Email_Receiver - Content Encryption Algorithm',
      'mandatoryif': [ 'Email Signature and Encryption Type', 'S/MIME Encryption', 'S/MIME Signature and Encryption']
    ],
    'Email Credentials for OAuth2': [
      'description': 'Receiver: Email_Receiver - Credential Name',
      'mandatoryif' : [ 'Email Authentication Method', 'OAuth2 Authorization Code']
    ],
    'Email Signature Private Key Alias': [
      'description': 'Receiver: Email_Receiver - Private Key Alias',
      'default' : '<Email Signature Private Key Alias>',
      'mandatoryif': [ 'Email Signature and Encryption Type', 'S/MIME Signature', 'S/MIME Signature and Encryption']
    ],
    'Email Subject': [
      'description': 'Receiver: Email_Receiver - Subject',
      'default' : '<Email Subject>',
      'mandatory': true
    ],
    'Email Sender': [
      'description': 'Receiver: Email_Receiver - From',
      'mandatory': true,
      'default' : '<email_from@email.com>',
      'type': 'email'
    ],
    'Email Protection': [
      'description': 'Receiver: Email_Receiver - Protection',
    ],
    'Email Credentials for User Password': [
      'description': 'Receiver: Email_Receiver - Credential Name',
      'mandatoryif': [ 'Email Authentication Method', 'Encrypted User/Password', 'Plain User/Password'],
      'default' : '<Email Credentials>'
    ],
    'Email Receiver': [
      'description': 'Receiver: Email_Receiver - To',
      'mandatory': true,
      'default' : '<email_to@email.com>',
      'type': 'email'
    ],
    'Email Signature and Encryption Type': [
      'description': 'Receiver: Email_Receiver - Signature and Encryption Type',
    ],
    'Email Connection Location ID': [
      'description': 'Receiver: Email_Receiver - Location ID',
      'mandatoryif': [ 'Email Proxy Type', 'On-Premise']
    ],
    'Email Receiver Bcc': [
      'description': 'Receiver: Email_Receiver - Bcc'
    ],
    'Email Timeout': [
      'description': 'Receiver: Email_Receiver - Timeout (in ms)',
      'type': 'number',
      'mandatory': true
    ],
    'Email Receiver Public Key': [
      'description': 'Receiver: Email_Receiver - Public Key',
      'default' : '<Email Receiver Public Key>',
      'mandatoryif': [ 'Email Signature and Encryption Type', 'S/MIME Encryption', 'S/MIME Signature and Encryption']
    ],
    'Email Send Clear Text Signed Message': [
      'description': 'Receiver: Email_Receiver - Send Clear Text Signed Message'
    ],
    'Email Proxy Type': [
      'description': 'Receiver: Email_Receiver - Proxy Type'
    ],
    'Email Receiver Cc': [
      'description': 'Receiver: Email_Receiver - Cc'
    ]
  ],
  'Update_Service_Order_for_Duplicate_Activity_from_SAP_Field_Service_Management_to_SAP_S4HANA': [
    'Service Order Note Language': [
      'description': 'More - Service Order Note Language',
      'mandatory': true,
      'size' : 2
    ],
    'LocationID': [
      'description': 'Receiver: S4HANA_Create - Location ID',
      'mandatoryif': [ 'ProxyType', 'On-Premise']
    ],
    'Creation Post Mapping Extension Enabled': [
      'description': 'More - Creation Post Mapping Extension Enabled',
      'type': 'boolean',
      'mandatory': true
    ],
    'Product Id for Service Item': [
      'description': 'More - Product Id for Service Item',
      'mandatory': true
    ],
    'AuthenticationMethod': [
      'description': 'Receiver: S4HANA_Create - Authentication',
    ],
    'Creation Post Mapping Address': [
      'description': 'Receiver: Creation_Post_Mapping_Extension_iFlow - Address',
      'mandatoryif': [ 'Creation Post Mapping Extension Enabled', 'true']
    ],
    'FSM Company ID': [
      'description': 'More - FSM Company ID',
      'group': 'ID',
      'type': 'number',      
      'alternative' : 'company',
      'default': '<FSM_Company_ID>'
    ],
    'ProcessDirectAddress': [
      'description': 'Sender: FSM - Address',
      'mandatory': true
    ],
    'company': [
      'description': 'More - company',
      'group': 'Name',
      'alternative' : 'FSM Company ID',
      'default': '<FSM_Company>'
    ],
    'FSM Account ID': [
      'description': 'More - FSM Account ID',
      'group': 'ID',
      'type': 'number',      
      'alternative' : 'account',
      'default': '<FSM_Account_ID>'
    ],
    'Service Order Item Note Type': [
      'description': 'More - Service Order Item Note Type',
      'mandatory': true,
      'size' : 4
    ],
    'Client Id': [
      'description': 'Receiver: S4HANA_Create - Client Id',
      'mandatory': true
    ],
    'CredentialName': [
      'description': 'Receiver: S4HANA_Create - Credential Name',
      'mandatoryif': [ 'AuthenticationMethod', 'Basic' ],
      'default' : '<SAP S/4HANA Credential Name>'
    ],
    'Credential Name': [
      'description' : 'FSM Credentials',
      'mandatory': true,
      'default' : '<FSM Credential Name>'
    ],
    'ODataServiceAddress': [
      'description': 'Receiver: S4HANA_Create - Address',
      'type': 'url',
      'mandatory': true,
      'default' : 'http://<s4host>:<s4port>/sap/opu/odata/sap/API_SERVICE_ORDER_SRV'
    ],
    'Private Key Alias': [
      'description': 'Receiver: S4HANA_Create - Private Key Alias',
      'mandatoryif': [ 'AuthenticationMethod', 'Client Certificate']
    ],
    'Error Log Attachments': [
      'description': 'More - Error Log Atachments',
      'type': 'boolean',
      'mandatory': true
    ],
    'Error Email Notification': [
      'description': 'More - Error Email Notification',
      'type': 'boolean',
      'mandatory': true
    ],
    'Post Mapping Extension Address': [
      'description': 'Receiver: Duplication_Post_Mapping_Extension_iFlow - Address',
      'mandatoryif': [ 'Post Mapping Extension Enabled', 'true']
    ],
    'ProxyType': [
        'description': 'Receiver: S4HANA_Create - Proxy Type'
    ],
    'FSMAddress': [
      'description': 'Receiver: SAP_FSM_Update - Address',
      'type': 'url',
      'mandatory': true,
      'default' : 'https://<cluster>.coresuite.com/api/fsm-connector/v1/data/Activity/${property.ActivityId}'
    ],
    'Post Mapping Extension Enabled': [
      'description': 'More - Post Mapping Extension Enabled',
      'type': 'boolean',
      'mandatory': true
    ],
    'user': [
      'description': 'More - user',
      'default' : '<FSM_User>'
    ],
    'account': [
      'description': 'More - account',
      'group': 'Name',
      'alternative' : 'FSM Account ID',
      'default': '<FSM_Account>'
    ]
  ]
]

/*
Map package_parameter_checks = [ 
  'IntegrationFlowID': [		// integration flow id 
    'Parameter Name': [ 		// externalized parameter name  
        'description': 'UI name',  	// parameter name on the configuration UI if different 
                                    // from Parameter Name  
        'type': 'string',           // supported types are string, number, boolean, url,  
                                    // hostname (with protocol and port), email,  
                                    // emailhostname (with protocol and port) 
        'size': 12,              	// max. size of string (optional) 
        'exact': 12,                	// exact size of string (optional) 
        'values': ['S001', 'S002'], 	// only these values allowed 
        'separator': '\\|',         	// list of separate values, each value is type/size 					
                                        // checked individually 
        'default': '<SMTP Server>', 	// default value to be interpreted as empty 
        'mandatory': true,          	// single mandatory parameter (optional) 
        'mandatoryif': ['another parameter' ,'v1','v2'], // is mandatory if another						
                                        // parameter is set or (optional) has values v1 or v2 
        'alternative': 'parameter', 	// not mandatory if alternative parameter set instead 
        'group': 'group name'       	// mandatory if another parameter of this group is set 
    ], 
    // … repeat for all parameters of IntegrationFlowID 
  ], 
  // … repeat for all integration flows 
] 
*/

    message.setProperty('PackageParameterChecks', package_parameter_checks)
    return message
}