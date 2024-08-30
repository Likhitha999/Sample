<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:hci="http://sap.com/it/"
 xmlns:m="http://schemas.microsoft.com/ado/2007/08/dataservices/metadata" xmlns:d="http://schemas.microsoft.com/ado/2007/08/dataservices">  
<xsl:output method="xml" indent="yes" omit-xml-declaration="yes"/> 
<xsl:param name= "LogicalDocument"/>
<xsl:param name= "ArchiveDocumentID"/>


<xsl:template match="node() | @*">
    <xsl:copy>
        <xsl:apply-templates select="node() | @*"/>
    </xsl:copy>
</xsl:template>
<xsl:template match= "m:properties[not(d:LogicalDocument = $LogicalDocument or d:ArchiveDocumentID = $ArchiveDocumentID)]"/>

</xsl:stylesheet>
