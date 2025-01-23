// groovy -cp $HOME/.m2/repository/com/h2database/h2/1.3.166/h2-1.3.166.jar jdbdc
// Grab will not load the driver! but at least we can trigger it to download it and then use groovy -cp option to run this script.
//@Grab('com.h2database:h2:1.3.166')

// testing compare groovy
jdbc_url = "jdbc:h2:~/test"
username = "sa"
password = ""
driver = "org.h2.Driver"

Class.forName(driver)
conn = java.sql.DriverManager.getConnection(jdbc_url, username, password)
print conn
conn.close()