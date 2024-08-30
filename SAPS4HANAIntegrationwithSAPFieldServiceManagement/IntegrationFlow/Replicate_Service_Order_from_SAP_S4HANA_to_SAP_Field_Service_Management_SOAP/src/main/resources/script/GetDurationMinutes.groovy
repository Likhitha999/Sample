def String GetDurationMinutes(String Quantity, String QuantityUnit){
    def result
    if ( (QuantityUnit == "HR" || QuantityUnit == "HUR" || QuantityUnit == "H") && Quantity.isFloat() ) {
        result = (int)(Quantity.toFloat() * 60)
    } else if ( QuantityUnit == "MIN" && Quantity.isFloat() ) {
        result = (int)Quantity.toFloat()
    }
	return result
}