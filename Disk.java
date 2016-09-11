import java.io.*;
import java.util.*;
import java.math.BigInteger;
/* 
In this Disk class paging is implemented and divided into 256 pages.Helper 
function's to write,read,free pages to disk are written to server specific 
purposes.Static variable DISK is the virtual disk used.While exiting a job 
or when the disk is getting free new jobs are loaded into the disk,until the 
loader has no sufficient jobs and to give enough room for new jobs the executed
jobs are taken out and respective pages are freed from the disk.At regular 
intervals statistics of the disk are calculated.At many instances fd variable is
use which means fileDescripter which is initalized in system.Paging is done for the disk
management as it is an widely used approach and it is also easier to swap pages between the
memory and disk while loading or during page faults.
*/
public class Disk extends SYSTEM {
  FileReader fr;
  BufferedReader br;
 //track number of free pages on disk initially false
 static boolean disk_Page_Checker[] = new boolean[256];
 static long DISK[] = new long[4096];//virtual disk
 static int number_Of_Pages_On_Disk = 256;
 //static int i = 0;
 static String warning = "";
 //used to free the pages in disk
 public static void free_Disk_Pages(int arr[]) {

  for (int i = 0; i < arr.length; i++) {
   disk_Page_Checker[arr[i]] = false;
  }
 }
 //returns indices of free pages available
 public static int get_Disk_Page_Index() {
  for (int i = 0; i < 256; i++) {
   if (disk_Page_Checker[i] == false) {
    disk_Page_Checker[i] = true;
    return i;
   }
  }
  return -1;
 }
//This function takes the disk to previous state when there is an error.
//Simply to say it preserves the state of disk at all times.
 public void roll_Back(int number_Of_Pages_Needed, int free_Pages_On_Disk[]) {
  try {
   String current_Line;
   //when rollbacking looping until next job
   while (!(current_Line = br.readLine()).contains("**FIN")) {
    fd++;
   }
   fd++; 
   number_Of_Pages_On_Disk += number_Of_Pages_Needed; //assigning to previous value
   SYSTEM.average_Unused_Pages+=number_Of_Pages_Needed;
   free_Disk_Pages(free_Pages_On_Disk); //free used pages upto now on Disk
  } catch (IOException e) {
  }
 }
 //retrive a page from disk
 public long[] get_Page_From_Disk(int page_Number) {
  long arr[] = new long[16];
  for (int i = 0; i < 16; i++) {
   arr[i] = DISK[page_Number * 16 + i];
  }
  return arr;
 }
//This if the main function which loads in the disk
 public void load_In_Disk() throws ERROR_HANDLER {
  //big integer used because even though long is used to store values underlying implementation takes it as integer.
  BigInteger big;
  //just to track current line
  String current_Line_Read = "";
  try {
   fr = new FileReader(input_File);
   br = new BufferedReader(fr);
   //traverse until the last read job
   int traverse_Until_Last_Read;
   //flag's used here just to track errors and warning's
   int free_Page_Index = -1, start_Address_To_Load, current_Length_Of_Program, length_Of_Program, pc_Value, trace_Bit = 0, invalid_loader_flag = 0;
   int number_Of_Pages_Needed, output_Pages_Needed, data_Pages_Needed, program_Pages_Needed, flag_For_Page_Count = 0, missing_Job_Flag = 0, multiple_Data_Flag = 0;
   float output_Line_Limit;
   //reading upto current state of file
   for (traverse_Until_Last_Read = 1; traverse_Until_Last_Read < fd; traverse_Until_Last_Read++) {
    br.readLine();
   }
   //reads until disk accepts
   while (true) {
    warning = "";
    multiple_Data_Flag = 0;
    if (missing_Job_Flag == 0) {
     current_Line_Read = br.readLine();
     //if end of the line just break
     if (current_Line_Read == null) {
  		  br.close();
   		  fr.close();
      break;
     }
     fd++;
     //if it doesn't contain **JOB
     if (!(current_Line_Read).contains("**JOB")) {
      new ERROR_HANDLER("MISSING **JOB", 70, job_Id);
      while (!(current_Line_Read = br.readLine()).contains("**FIN")) {
       fd++;
      }
      fd++;
      job_Id++;
      //go back to start point
      continue;
     }
    } else {
     missing_Job_Flag = 0;
    }
    //get total output lines
    output_Line_Limit = Integer.decode("0x" + current_Line_Read.substring(9));
    output_Pages_Needed = (int) Math.ceil(output_Line_Limit / 4);
    //Total pages needed
    number_Of_Pages_Needed = Integer.decode("0x" + current_Line_Read.substring(6, 8)) + output_Pages_Needed;
    current_Line_Read = br.readLine();
    fd++;
    if (current_Line_Read.length() > 5) {
     if (current_Line_Read.contains("**JOB")) {
      new ERROR_HANDLER("DOUBLE **JOB FOUND", 71, job_Id);
      job_Id++;
     } else {
      new ERROR_HANDLER("INVALID LOADER FORMAT", 72, job_Id);
     }
     while (!(current_Line_Read = br.readLine()).contains("**FIN")) {
      fd++;
     }
     fd++;
     continue; //if invalid pc_Value or trace bit
    }
    //read the start address to be loaded and length of the program 
    start_Address_To_Load = Integer.decode("0x" + current_Line_Read.substring(0, 2)); //start address
    length_Of_Program = Integer.decode("0x" + current_Line_Read.substring(3));
    program_Pages_Needed = (int) Math.ceil(Math.ceil((float) length_Of_Program / 4) / 4);
    current_Length_Of_Program = Integer.decode("0x" + current_Line_Read.substring(3)); //number of words
    //giving extra space on disk if not sufficient
    if (((program_Pages_Needed * 16) - length_Of_Program) > 0) {
     number_Of_Pages_Needed++;
    }
    //storing indices of free pages on disk
    int free_Pages_On_Disk[] = new int[number_Of_Pages_Needed];
    //decrement  number of pages on disk which are used
    number_Of_Pages_On_Disk -= number_Of_Pages_Needed;
    //If there's no enough pages on disk just exiting
    if (number_Of_Pages_On_Disk < 0) {
     //assigning to previous value
     number_Of_Pages_On_Disk += number_Of_Pages_Needed;
     //Next time don't read read Job Line
     fd -= 2;
     break;
    }
    //find index of free pages
    for (int i = 0; i < number_Of_Pages_Needed; i++) {
     free_Pages_On_Disk[i] = get_Disk_Page_Index();
    }
    int current_Page_Using = -1, current_Word_Using = 0;
    while (current_Length_Of_Program > 0) {
     current_Line_Read = br.readLine();
     fd++;
     int length_Of_Line_Read = current_Line_Read.length(), current_Word_Read = 0;
     invalid_loader_flag = 0;
     //For Each line break into words and keep in disk
     while (length_Of_Line_Read > 0) {
      try {
       if (current_Word_Using % 16 == 0) {
        current_Page_Using++;
        current_Word_Using = 0;
        if (current_Page_Using > program_Pages_Needed - 1) {
         new ERROR_HANDLER("PROGRAM TOO LONG", 73, job_Id);
        }
       }
       big = new BigInteger(current_Line_Read.substring(current_Word_Read, current_Word_Read + 8), 16); //using BigInteger
       //retrive each word and put into disk
       DISK[free_Pages_On_Disk[current_Page_Using] * 16 + current_Word_Using] = big.longValue();
       current_Word_Read += 8;
       current_Word_Using++;

      } catch (StringIndexOutOfBoundsException e) {
       invalid_loader_flag = 1;
       if (current_Line_Read.length() == 4) {
        new ERROR_HANDLER("NULL JOB", 74, job_Id);

       } else {

        new ERROR_HANDLER("INVALID WORD LENGTH", 75, job_Id);
       }
       break;//in error just terminate at all cases
      } catch (NumberFormatException e) {
       invalid_loader_flag = 2;
       new ERROR_HANDLER("BAD CHARACTER ENCOUNTERED BY LOADER", 76, job_Id);
       break;
      } catch (ArrayIndexOutOfBoundsException e) {
       invalid_loader_flag = 3;
       new ERROR_HANDLER("ACCESSING OUT OF RANGE VALUES", 77, job_Id);
       break;
      }
      current_Length_Of_Program--;
      length_Of_Line_Read -= 8;
     }
     if (invalid_loader_flag != 0) break;
    } //end of program
    current_Line_Read = br.readLine();
    fd++;
    //This is mainly to check if we have trace errors or any wrong loader format
    if (current_Line_Read.length() > 4 || current_Line_Read.length() < 4 || invalid_loader_flag != 0) {
     if (invalid_loader_flag == 0) {
      if (current_Line_Read.length() == 32) {
       new ERROR_HANDLER("PROGRAM TOO LONG", 78, job_Id);
      } else {
       new ERROR_HANDLER("MISSING PC AND TRACE BITS", 79, job_Id);
      }
     }
     job_Id++;
     //get back to previous state in case of error
     roll_Back(number_Of_Pages_Needed, free_Pages_On_Disk); 
     continue; //if invalid pc_Value or trace bit 
    }

    pc_Value = Integer.decode("0x" + current_Line_Read.substring(0, 2));
    trace_Bit = Integer.decode("0x" + current_Line_Read.substring(3));
    if (trace_Bit != 0 && trace_Bit != 1) {
     warning = "BAD TRACE BIT (LOAD TIME WARNING.CODE:510)";
     trace_Bit = 0;
    }

    /*TO CHECK IF **DATA IS MISSING*/
    if (!(current_Line_Read = br.readLine()).contains("**DATA")) {
     fd++;
     new ERROR_HANDLER("MISSING **DATA", 80, job_Id);
     roll_Back(number_Of_Pages_Needed, free_Pages_On_Disk); //rollback
     missing_Job_Flag = 0;
     job_Id++;
    } else {
     int excess_Data_Pages_Flag = 0, data_Pages_Error_Flag = 0, current_data_Word_Using = 0, data_Pages_Limit, start_Data_Page = -1, input_Limit = 0;
     data_Pages_Needed = number_Of_Pages_Needed - program_Pages_Needed - output_Pages_Needed;
     data_Pages_Limit = data_Pages_Needed + current_Page_Using;
     int unused_Pages = data_Pages_Limit - data_Pages_Needed + program_Pages_Needed;
     int i = 0;
     fd++;
     while (!(current_Line_Read = br.readLine()).contains("**FIN")) {
      int length_Of_Line_Read = current_Line_Read.length(), current_Word_Read = 0;
      //IN CASE OF multiple_Data_Flag DATA FOUND
      if (current_Line_Read.contains("**DATA")) {

       new ERROR_HANDLER("DOUBLE DATA FOUND", 81, job_Id);
       roll_Back(number_Of_Pages_Needed, free_Pages_On_Disk); //rollback
       multiple_Data_Flag = 1;
       break;
      }
      /*IF **FIN IS MISSING TERMINATE JOB AND SPOOL OUT OF DISK*/
      if (current_Line_Read.contains("**JOB")) {

       number_Of_Pages_On_Disk += number_Of_Pages_Needed; //assigning to previous value
       free_Disk_Pages(free_Pages_On_Disk); //free used pages upto now on Disk
       new ERROR_HANDLER("MISSING **FIN", 82, job_Id);
       missing_Job_Flag = 1;
       break;
      }

      {
       //For Each line break into words and keep in disk
       while (length_Of_Line_Read > 0) {
        try {

         if (current_data_Word_Using % 16 == 0) {
          start_Data_Page++;
          current_data_Word_Using = 0;
          if (start_Data_Page > data_Pages_Needed - 1) {
           new ERROR_HANDLER("INSUFFICIENT INPUT PAGES", 83, job_Id);
           excess_Data_Pages_Flag = 1;
           break;
          }
         }

         big = new BigInteger(current_Line_Read.substring(current_Word_Read, current_Word_Read + 8), 16); //using BigInteger
         //retrive each word and put int disk
         DISK[free_Pages_On_Disk[current_Page_Using + start_Data_Page + 1] * 16 + current_data_Word_Using] = big.longValue();
         current_Word_Read += 8;
         input_Limit++;
         current_data_Word_Using++;
        } catch (StringIndexOutOfBoundsException e) {
         new ERROR_HANDLER("INVALID WORD LENGTH", 84, job_Id);
         data_Pages_Error_Flag = 1;
         break;
        } catch (NumberFormatException e) {
         new ERROR_HANDLER("BAD CHARACTER ENCOUNTERED", 85, job_Id);
         data_Pages_Error_Flag = 1;
         break;
        } catch (ArrayIndexOutOfBoundsException e) {
         new ERROR_HANDLER("ACCESSING OUT OF RANGE VALUES", 86, job_Id);
         data_Pages_Error_Flag = 1;
         break;
        }
        length_Of_Line_Read -= 8;
       }
       if (excess_Data_Pages_Flag != 0) break;
      } //end of program

      missing_Job_Flag = 0;
      fd++;
      if (data_Pages_Error_Flag != 0 || excess_Data_Pages_Flag != 0) {
       break;

      }
     } //In case of excess pages used or any load time error 
     if (data_Pages_Error_Flag != 0 || excess_Data_Pages_Flag != 0) {
      roll_Back(number_Of_Pages_Needed, free_Pages_On_Disk); //rollback
      fd++;
      job_Id++;
      continue;
     }
     //if Loader format is correct then assign to pcb
     if (multiple_Data_Flag == 0 && missing_Job_Flag == 0 && excess_Data_Pages_Flag == 0) {
      String s;
      //pages assigned but unused
      if ((data_Pages_Needed - 2 - start_Data_Page) > 0) {
       warning = "UNUSED PAGES (LOAD TIME WARNING.CODE: 507)";
       SYSTEM.average_Unused_Pages=data_Pages_Needed - 2 - start_Data_Page;
      }
      if (warning.length() == 0) {
       s = String.format("%n%-12s%-3s%-12s%-6s%n", "Job ID(Hex):", Integer.toHexString(job_Id), "Clock(Hex):", Long.toHexString(SYSTEM.clock));
       SYSTEM.progress_File_Writer.write(s);
      } else {
       s = String.format("%n%-12s%-3s%-12s%-6s%-5s%-10s%n", "Job ID(Hex):", Integer.toHexString(job_Id), "Clock(Hex):", Long.toHexString(SYSTEM.clock), "WARNING:", warning);
       SYSTEM.progress_File_Writer.write(s);
      }
       //create a object and add it to the process queue.
      process_Queue.addLast(new ProcessControlBlock(job_Id, pc_Value, trace_Bit, length_Of_Program, input_Limit, output_Line_Limit, program_Pages_Needed, data_Pages_Needed, output_Pages_Needed, free_Pages_On_Disk));
      SYSTEM.number_Of_Holes_In_Disk+=256-number_Of_Pages_On_Disk;
     }
     job_Id++;
     fd++;
    } 
   } 
   br.close();
   fr.close(); //closing both the buffers.
  } catch (FileNotFoundException e) {
  try
  {
   SYSTEM.progress_File_Writer.write("ERROR:INPUT FILE NOT FOUND (LOAD TIME ERROR.CODE:402)");
   SYSTEM.progress_File_Writer.flush();
   SYSTEM.progress_File_Writer.close();
   System.exit(0);
   }catch(Exception file_Error){}
  } catch (Exception e) {
   System.exit(0);
   //throw new ERROR_HANDLER("INVALID LOADER FORMAT", 103);
  }
 }
}