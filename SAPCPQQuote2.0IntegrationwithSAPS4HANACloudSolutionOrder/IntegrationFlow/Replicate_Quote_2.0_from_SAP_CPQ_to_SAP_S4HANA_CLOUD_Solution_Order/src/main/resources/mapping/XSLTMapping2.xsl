<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
exclude-result-prefixes="xs"
version="2.0">
<xsl:output method="xml" omit-xml-declaration="yes" version="1.0" encoding="UTF-8" indent="yes"/>
<xsl:template match="@*|node()">
<xsl:apply-templates select="@*|node()"/>
</xsl:template>
<xsl:template match="root/message">
Header Message:
<xsl:value-of select="text()"/>
</xsl:template>
<xsl:template match="details/message">
Item Message:
<xsl:value-of select="text()"/>
</xsl:template>
</xsl:stylesheet>