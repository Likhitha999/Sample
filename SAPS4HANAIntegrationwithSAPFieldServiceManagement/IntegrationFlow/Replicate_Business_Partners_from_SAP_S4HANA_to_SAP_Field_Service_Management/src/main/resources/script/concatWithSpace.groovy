import com.sap.it.api.mapping.*;

def String concatWithSpace(String name1,String name2,String name3,String name4){
    String concatenatedString = ''
    if(name1!=''){
        concatenatedString = concatenatedString+name1+' ';
    }
    if(name2!=''){
        concatenatedString = concatenatedString+name2+' ';
    }
    if(name3!=''){
        concatenatedString = concatenatedString+name3+' ';
    }
    if(name4!=''){
        concatenatedString = concatenatedString+name4+' ';
    }
	return concatenatedString.trim()
}