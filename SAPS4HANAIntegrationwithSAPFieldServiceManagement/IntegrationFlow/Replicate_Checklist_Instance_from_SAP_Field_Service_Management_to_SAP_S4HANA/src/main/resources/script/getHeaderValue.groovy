import com.sap.it.api.mapping.*;


def String getHeader(String headerName ,MappingContext context) {
         String value1 = context.getHeader(headerName);
         return value1;
}

