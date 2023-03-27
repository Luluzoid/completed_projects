import java.sql.*; 
import java.math.*;
import java.io.*;
import java.util.*;
import oracle.jdbc.driver.*;
import org.apache.ibatis.jdbc.ScriptRunner;


/*
* Connects to a user's SQL Database and loads in an .sql file with data that stores Movie's ID, Title, Language, Company, Country, Release date, and Length.
* That same database also stores Reviewer's ID and their rating for a certain movie.
* Uses the JDBC API to use SQL queries.
*/

class P2{
   static Connection con;
   static Statement stmt;


   public static void main (String args[]) throws SQLException
   {
      boolean done=false;
      connectToDatabase();
      Scanner sc=new Scanner(System.in);
      int input=0;
      //loading the movies.sql file
      while(!done)
      {
         try{
            String filePath;
            System.out.println("\nEnter a path to your movies.sql file to load it into your SQL database or '-' to skip this step:");
            filePath=sc.nextLine();
            if(!filePath.equals("-"))
            {
               ScriptRunner SR = new ScriptRunner(con);
               Reader reader = new BufferedReader(new FileReader(filePath));
               SR.runScript(reader);
            }
            done=true;
         
         }catch (FileNotFoundException e)
         {
            System.err.println("Invalid file. Try again");
            sc=new Scanner(System.in);
         }
      }
      
      do{
         done=false;
         while(!done)
         {
            try{
               printMenu();
               input = sc.nextInt();
               done=true;
            }
            catch (InputMismatchException e)
            {
               System.err.println("Invalid input. Must be a number between 1 and 4");
               sc=new Scanner(System.in);
            }
         }
         //when user presses 1
         if(input==1)
         {
         
            System.out.println("MOVIES (Yes/No)");
            String displayMovies = sc.next();
            while(true){
               if(displayMovies.equals("No") || displayMovies.equals("Yes"))
                  break;
               else
               {
                  System.out.println("Enter Yes to display, and No to not");
                  displayMovies = sc.next();
               }
            
            }
         
            System.out.println("RATINGS (Yes/No)");
            String displayRatings = sc.next();
            while(true){
               if(displayRatings.equals("No") || displayRatings.equals("Yes"))
                  break;
               else
               {
                  System.out.println("Enter Yes to display, and No to not");
                  displayRatings = sc.next();
               }
            }
         
            if(displayMovies.equals("Yes"))
            {
               String sql = "SELECT * FROM Movies";
               PreparedStatement pstmt=con.prepareStatement(sql);
               
               ResultSet rset=pstmt.executeQuery();
               rset=pstmt.executeQuery();
               
               System.out.format("%-" + 10 + "s  %-45s %-10s %-45s %-45s %-15s %-15s\n", "Movie ID:", "Title:", "Language:", "Production Company:", "Production Country:", "Runtime:", "Release Date:");
            
               while (rset.next())
               {
                  String title=rset.getString(2);
                  String prodComp=rset.getString(4) ;
                  String prodCountry=rset.getString(5);
                  if(title!= null && title.length()>31)
                     title=title.substring(0,29);
                  if(prodComp!= null && prodComp.length()>41)
                     prodComp=prodComp.substring(0,39); 
                  if(prodCountry!= null && prodCountry.length()>41)
                     prodCountry=prodCountry.substring(0,39);
                  System.out.format("%-" + 10 + "s  %-45s %-10s %-45s %-45s %-15d %-15s\n", rset.getString(1), title, rset.getString(3), prodComp, prodCountry, rset.getInt(6), rset.getDate(7)); 
               } 
               rset.close();
            }
            if(displayRatings.equals("Yes"))
            {
               String sql = "SELECT * FROM Ratings";
               PreparedStatement pstmt=con.prepareStatement(sql);
               ResultSet rset=pstmt.executeQuery();
               rset=pstmt.executeQuery();
            
               System.out.format("%-" + 10 + "s  %-10s %-10s\n", "Movie ID:", "Reviewer ID:", "Rating:");
            
               while (rset.next())
               {
                  System.out.format("%-" + 10 + "s  %-10s %-10.2f\n", rset.getString(1), rset.getString(2), rset.getFloat(3));  
               }
               rset.close();
            }
         }
         //when user presses 2
         else if(input==2)
         {
            String movieID;
            System.out.println("Enter a MovieID: ");
            movieID = sc.next();
            String sql = "SELECT movieid, title, language, production_company, production_country, runtime, release_date, AVG(Rating) FROM Ratings R natural join Movies M GROUP BY movieid, title, language, production_company, production_country, runtime, release_date HAVING movieid=?";
            PreparedStatement pstmt2=con.prepareStatement(sql); 
            pstmt2.setString(1, movieID);
         
            ResultSet rset = pstmt2.executeQuery(); 
            
            boolean found=false;
            while (rset.next())
            {
               found=true;
               String title=rset.getString(2);
               String prodComp=rset.getString(4) ;
               String prodCountry=rset.getString(5);
               if(title !=null && title.length()>30)
                  title=title.substring(0,29);
               if(prodComp != null && prodComp.length()>40)
                  prodComp=prodComp.substring(0,39); 
               if(prodCountry != null && prodCountry.length()>40)
                  prodCountry=prodCountry.substring(0,39); 
            
               System.out.format("%-" + 10 + "s   %-35s %-15s %-45s %-45s %-15s %-15s %-10s\n", "Movie ID:", "Title:", "Language:", "Production Company:", "Production Country:", "Runtime:", "Release Date:", "Average Rating:");  
               System.out.format("%-" + 10 + "s   %-35s %-15s %-45s %-45s %-15d %-15s %-10.2f\n", rset.getString(1), title, rset.getString(3), prodComp , prodCountry, rset.getInt(6), rset.getDate(7), rset.getFloat(8));
            }
            if(!found)
               System.out.println("Looks like no results were found...\nTry searching again!");
            rset.close();
         }
         //when user presses 3
         else if(input==3)
         {
         
            String movieID, title, production_company, reviewerid, rating=null, temp;
            float ratingNum=0.0f;
            System.out.print("Enter Movie ID or '-' if none: ");  
            movieID = sc.next();
            System.out.print("Enter Title or '-' if none: ");  
            title = sc.nextLine();
            title = sc.nextLine();
            System.out.print("Enter Production Company or '-' if none: ");  
            production_company = sc.nextLine();
            System.out.print("Enter Reviewer ID or '-' if none: ");  
            reviewerid = sc.next();
            System.out.print("Enter Rating or '-' if none: ");
            done=false;
            while(!done)
            {
               try{
                  rating = sc.next();
                  if(rating.equals("-"))
                     break;
                  ratingNum=Float.parseFloat(rating);
                  done=true;
               }
               catch (NumberFormatException e)
               {
                  System.err.println("Invalid input. try again");
                  sc=new Scanner(System.in);
               }
            }
         
         
            String sql = "SELECT DISTINCT * FROM Movies, Ratings WHERE Movies.movieID=Ratings.movieID AND";
            if(!movieID.equals("-"))
               sql+=" Movies.movieid=?";
            
            if(!title.equals("-")){
               if(!sql.equals("SELECT DISTINCT * FROM Movies, Ratings WHERE Movies.movieID=Ratings.movieID AND"))
                  sql+=" AND";
               sql+=" title LIKE '%"+title+"%'";
            }
            if(!production_company.equals("-")){
               if(!sql.equals("SELECT DISTINCT * FROM Movies, Ratings WHERE Movies.movieID=Ratings.movieID AND"))
                  sql+=" AND";
               sql+=" production_company=?";
            }
            if(!reviewerid.equals("-")){
               if(!sql.equals("SELECT DISTINCT * FROM Movies, Ratings WHERE Movies.movieID=Ratings.movieID AND"))
                  sql+=" AND"; 
               sql+=" userid=?";
            }
            if(!rating.equals("-")){
               if(!sql.equals("SELECT DISTINCT * FROM Movies, Ratings WHERE Movies.movieID=Ratings.movieID AND"))
                  sql+=" AND";
               sql+=" rating=?";
               ratingNum=Float.parseFloat(rating);
            }
         
            PreparedStatement pstmt3=con.prepareStatement(sql);
         
            int count = 1;
            if(!movieID.equals("-")){
               pstmt3.setString(count, movieID);
               count++;
            }
            
            if(!production_company.equals("-")){
               pstmt3.setString(count, production_company);
               count++;
            }
            if(!reviewerid.equals("-")){
               pstmt3.setString(count, reviewerid);
               count++;
            }
            if(!rating.equals("-"))
               pstmt3.setFloat(count, ratingNum);
            
          
            //index for each output value. True if user chose to see it
            boolean[] index = new boolean[9];
            System.out.println("Choose what to display...\nMovie ID (Yes/No): ");  
            temp = sc.next();
            while(true){
               if(temp.equals("Yes"))
               {
                  index[0]=true;
                  break;
               }
               else if(temp.equals("No"))
                  break;
               else
               {
                  System.out.println("Enter Yes to display, and No to not");
                  temp = sc.next();
               }
            }
         
            System.out.println("Title (Yes/No): ");  
            temp = sc.next();
            while(true){
               if(temp.equals("Yes")){
                  index[1]=true;
                  break;
               }
               else if(temp.equals("No"))
                  break;
               else
               {
                  System.out.println("Enter Yes to display, and No to not");
                  temp = sc.next();
               }
            }
         
            System.out.println("Language (Yes/No): ");  
            temp = sc.next();
            while(true){
               if(temp.equals("Yes")){
                  index[2]=true;
                  break;
               }
               else if(temp.equals("No"))
                  break;
               else
               {
                  System.out.println("Enter Yes to display, and No to not");
                  temp = sc.next();
               }
            }
         
            System.out.println("Production Company (Yes/No): ");  
            temp = sc.next();
            while(true){
               if(temp.equals("Yes")){
                  index[3]=true;
                  break;
               }
               else if(temp.equals("No"))
                  break;
               else
               {
                  System.out.println("Enter Yes to display, and No to not");
                  temp = sc.next();
               }
            }
         
            System.out.println("Production Country (Yes/No): ");  
            temp = sc.next();
            while(true){
               if(temp.equals("Yes")){
                  index[4]=true;
                  break;
               }
               else if(temp.equals("No"))
                  break;
               else
               {
                  System.out.println("Enter Yes to display, and No to not");
                  temp = sc.next();
               }
            }
         
            System.out.println("Release Date (Yes/No): ");  
            temp = sc.next();
            while(true){
               if(temp.equals("Yes")){
                  index[5]=true;
                  break;
               }
               else if(temp.equals("No"))
                  break;
               else
               {
                  System.out.println("Enter Yes to display, and No to not");
                  temp = sc.next();
               }
            }
         
            System.out.println("Runtime (Yes/No): ");  
            temp = sc.next();
            while(true){
               if(temp.equals("Yes")){
                  index[6]=true;
                  break;
               }
               else if(temp.equals("No"))
                  break;
               else
               {
                  System.out.println("Enter Yes to display, and No to not");
                  temp = sc.next();
               }
            }
         
            System.out.println("Reviewer ID (Yes/No): ");  
            temp = sc.next();
            while(true){
               if(temp.equals("Yes")){
                  index[7]=true;
                  break;
               }
               else if(temp.equals("No"))
                  break;
               else
               {
                  System.out.println("Enter Yes to display, and No to not");
                  temp = sc.next();
               }
            }
         
            System.out.println("Rating (Yes/No): ");  
            temp = sc.next();
            while(true){
               if(temp.equals("Yes")){
                  index[8]=true;
                  break;
               }
               else if(temp.equals("No"))
                  break;
               else
               {
                  System.out.println("Enter Yes to display, and No to not");
                  temp = sc.next();
               }
            }
         
            ResultSet rset = pstmt3.executeQuery(); 
            
            System.out.println();
            if(index[0])
               System.out.format("%-"+15+"s", "Movie ID:");
            if(index[1])
               System.out.format("%-35s", "Title:");
            if(index[2])
               System.out.format("%-15s", "Language:");
            if(index[3])
               System.out.format("%-45s", "Production Company:");
            if(index[4])
               System.out.format("%-45s", "Production Country:");
            if(index[6])
               System.out.format("%-15s", "Release Date:");
            if(index[5])
               System.out.format("%-15s", "Runtime:");
            if(index[7])
               System.out.format("%-15s", "Reviewer ID:");
            if(index[8])
               System.out.format("%-10s", "Rating:");
            System.out.println("\n");
                              
            boolean found=false;
            while (rset.next())
            {
               found=true;
               
               String movTitle=rset.getString(2);
               String prodComp=rset.getString(4);
               String prodCountry = rset.getString(5);
            
               
               if(title!=null && movTitle.length()>31)
                  movTitle=movTitle.substring(0,29);
               if(rset.getString(4)!=null && prodComp.length()>41)
                  prodComp=prodComp.substring(0,39); 
               if(rset.getString(5)!=null && prodCountry.length()>41)
                  prodCountry=prodCountry.substring(0,39); 
                                 
               if(index[0]){
                  System.out.format("%-"+15+"s", rset.getString(1));
               }
               if(index[1]){
                  System.out.format("%-35s", movTitle);
               }
               if(index[2]){
                  System.out.format("%-15s", rset.getString(3));
               }
               if(index[3]){
                  System.out.format("%-45s", prodComp);
               }
               if(index[4]){
                  System.out.format("%-45s", prodCountry);
               }
               if(index[6]){
                  System.out.format("%-15s",rset.getDate(7));
               }
               if(index[5]){
                  System.out.format("%-15d",rset.getInt(6));
               }
               if(index[7]){
                  System.out.format("%-15s",rset.getString(9));
               }
               if(index[8]){
                  System.out.format("%-10.2f",rset.getFloat(10));
               } 
               System.out.println();
            }
            if(!found)
               System.out.println("Looks like no results were found...\nTry searching again!");
            rset.close();
         
         }
         else
         {
            if(input!=4)
               System.out.println("Enter a valid number: 1-4");
         }
      
      
      
      }while(input!=4);
   
      System.out.println("Bye! Have a good day!");
   }
   //prints menu
   public static void printMenu()
   {
      System.out.println("\n------------------------------------------------------------------------------------------");
      System.out.println("1. View table contents\n2. Search by MOVIEID\n3. Search by one or more attributes\n4. Exit");
      System.out.println("------------------------------------------------------------------------------------------");
   }

   public static void connectToDatabase()
   {
      String driverPrefixURL="jdbc:oracle:thin:@";
      String jdbc_url="artemis.vsnet.gmu.edu:1521/vse18c.vsnet.gmu.edu";
   
     // IMPORTANT: DO NOT PUT YOUR LOGIN INFORMATION HERE. INSTEAD, PROMPT USER FOR HIS/HER LOGIN/PASSWD
      String username;
      String password;
      username = readEntry("Username: "); 
      password = readEntry("Password: ");
   
      try{
      //Register Oracle driver
         DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
      } catch (Exception e) {
         System.out.println("Failed to load JDBC/ODBC driver.");
         return;
      }
   
      try{
         System.out.println(driverPrefixURL+jdbc_url);
         con=DriverManager.getConnection(driverPrefixURL+jdbc_url, username, password);
         DatabaseMetaData dbmd=con.getMetaData();
         stmt=con.createStatement();
      
         System.out.println("Connected.");
      
         if(dbmd==null){
            System.out.println("No database meta data");
         }
         else {
            System.out.println("Database Product Name: "+dbmd.getDatabaseProductName());
            System.out.println("Database Product Version: "+dbmd.getDatabaseProductVersion());
            System.out.println("Database Driver Name: "+dbmd.getDriverName());
            System.out.println("Database Driver Version: "+dbmd.getDriverVersion());
         }
      }catch( Exception e) {e.printStackTrace();}
   
   }
   //reads username and password
   static String readEntry(String prompt)
   {
      try
      {
         StringBuffer buffer = new StringBuffer();
         System.out.print(prompt);
         System.out.flush();
         int c = System.in.read();
         while (c != '\n' && c != -1)
         {
            buffer.append((char)c);
            c = System.in.read();
         }
         return buffer.toString().trim();
      }
      catch(IOException e)
      {
         return "";
      }
   }
}
