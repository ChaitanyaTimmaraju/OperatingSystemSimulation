/*
 Description:
 The error_handler subsystem handles the errors with custom exceptions by throwing
 a string message to the Exception class.It handles both warnings and errors. 
 In case of warinings the program continue's execution and In case of errors it 
 calls the system to handle the termination.The static variable message used to 
 print the Description of error or warining to ExecutionProfile.txt file;
*/

class ERROR_HANDLER extends Exception {
 static int exit_Status;
 static String message = "";
 static boolean batch_Finish;
 public String determine_Error_Or_Warning(int error_Code) {
   String s;
   //The Error's are in range of 0-500 and 500-1000 are warnings.
   if (error_Code < 500) {
    s = "ERROR." + "CODE:" + error_Code;
    exit_Status = 1;
   } else {
    s = "WARNING." + " CODE " + error_Code;
    error_Code = error_Code - 500;
    exit_Status = 0;
   }
   //The below condition is used to determine case specific error messages.
   if ((error_Code >= 0) && (error_Code <= 100)) {
    s = "LOAD TIME " + s;
   } else if ((error_Code > 100) && (error_Code <= 200)) {
    s = "MEMORY REFERENCE TIME " + s;
   } else if ((error_Code > 200) && (error_Code <= 300)) {
    s = "DECODING TIME " + s;
   } else if ((error_Code > 300) && (error_Code <= 400)) {
    s = "EXECUTION TIME " + s;
   } else if ((error_Code > 400) && (error_Code <= 500)) {
    s = "SYSTEM FETCHING TIME " + s;
   }
   return s;
  }
  //The below functions checks if the value is in given range otherwise throws error.
 static int check_Range(String error_Name, int error_Code, long start, long end) {
  if (start >= 0 && start <= end) {
   return 1;
  } else {
   new ERROR_HANDLER(error_Name, error_Code);
   return 0;
  }
 }


 //The below constructor checks if the throwed object is error or warning. If it's an error it returns to SYSTEM.
 public ERROR_HANDLER(String error, int error_Code) {

  message = message + error + " (" + determine_Error_Or_Warning(error_Code) + ")";

  if (exit_Status == 1)
      SYSTEM.final_Exit(1);

 }
 // The below constructer is used to handle load time errors
 public ERROR_HANDLER(String error, int error_Code, int job_Id) {
  error = error + " (" + determine_Error_Or_Warning(error_Code) + ")";
  String s = String.format("%n%-12s%-3s%-12s%-6s%-5s%-10s%n", "Job ID(Hex):", Integer.toHexString(job_Id), "Clock(Hex):", Long.toHexString(SYSTEM.clock), "Error:", error);
  try {
   SYSTEM.progress_File_Writer.write(s);
  } catch (Exception e) {
    
  }
  SYSTEM.unsuccessful_Jobs++;
  SYSTEM.loader_Time_Errors++;
 }

}