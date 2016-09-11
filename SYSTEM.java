/*
 NAME : S V CHAITANYA TIMMARAJU 
 COURSE NUMBER: CS 5323 
 ASSIGNMENT TITLE : OS2 PROJECT-III (OPERATING SYSTEM SIMULATION) 
 DATE : 27 APRIL 2016 
*/
/*
 Description:
 The system module will take the command line arguments for input file and it calls the 
 appropriate subsystems.It also handles the  I /o operation and termination of the 
 program.The below variables are declared to be global.Because they are used by other 
 subsystems such as execution_time needed for cpu to increment the value, Start_address 
 and trace_flag are to be set by the loader subsystem.The major static variables are used 
 to calculate system average performances and for holding times of system for later use.
*/
import java.util.*;
import java.lang.*;
import java.io.*;
public class SYSTEM {
 static int current_Sub_Queue_Using=0;
 static int total_Number_Of_Suspected_Infinite = 0, mean_Jobs_On_Disk = 0,average_Unused_Pages=0;
 static int loader_Time_Errors = 0;
 static String infinite_Jobs_ID = "";
 static int IO_time = 0, execution_Time = 0,idle=0, file_Index = 0, disk_Page_Index = 0;
 static int length_Of_Loaded_Memory, location_Of_First_Address_Loaded, start_Address, trace_Flag, cumulative_Job_Id = 1;
 static String input_File, final_Output; //input file is taken from command line arguments and final_Output is used to print OUTPUT.txt
 static LinkedList < Object > process_Queue = new LinkedList < Object > ();
 static int fd = 1,avg_Pages_On_Disk_Freed=0;//this(fd) is used in many instances and called filedescripter.
 static long clock = 0,mlfbq_Interval=1;//system clock
 static int job_Id = 0;//to track jobid's
 static int successful_Jobs = 0, unsuccessful_Jobs = 0;
 static float average_Holes;//to track mean holes on disk
 static int page_Faults_Handling_Time = 0;
 static FileWriter progress_File,mlfbq_File,matrix_File;//global variable to write to progress file.
 static boolean release =false;//to print status of system at intervals, acting as a lock.
 static BufferedWriter progress_File_Writer,mlfbq_File_Writer,matrix_File_Writer;
 static double average_percentage_Of_Disk_In_Use=0.0,number_Of_Holes_In_Disk;
 static int mean_Job_Run_Time = 0, mean_Job_IO_Time = 0, mean_Job_Execution_Time = 0, mean_Job_Turn_Around_Time = 0, mean_Page_Fault_Time = 0, total_CPU_Idle_Time = 0;
 static int time_Lost_For_Abnormal_Jobs = 0, time_Lost_Suspected_Infinite_Job = 0, total_Number_Of_Page_Faults = 0;
 static int turns[]=new int[3];
 static int time_Quantum[]=new int[4];
 static int average_Jobs_In_SubQueue[]=new int[4];
 static int max_Jobs_In_SubQueue[]=new int[4];
 static int queue_Change_Count=0,average_Sub_Queue_Size=0;
 static int current_Turn=0,current_Time_Quantum=0;
 static int count_MLFB_Queue=0,running_Job;
 static int matrix_Traffic[]=new int[12];
 //This is to reinitialize all the variables in the program to freshly start the
 //system again.
 public static void reinitialize()
 {
    ERROR_HANDLER.message = "";
   mlfbq_Interval=1;
   average_Jobs_In_SubQueue[0]=0;
   average_Jobs_In_SubQueue[1]=0;
   average_Jobs_In_SubQueue[2]=0;
   average_Jobs_In_SubQueue[3]=0;
   max_Jobs_In_SubQueue[0]=0;
   max_Jobs_In_SubQueue[1]=0;
   max_Jobs_In_SubQueue[2]=0;
   max_Jobs_In_SubQueue[3]=0; 
  average_Sub_Queue_Size=0;
  count_MLFB_Queue=0;
  queue_Change_Count=0;
  current_Sub_Queue_Using=0;
  total_Number_Of_Suspected_Infinite = 0;
  mean_Jobs_On_Disk = 0;
  average_Unused_Pages=0;
  loader_Time_Errors = 0;
  page_Faults_Handling_Time = 0;
  CPU.idle_Clock=500l;
  infinite_Jobs_ID = "";
  IO_time = 0;
  execution_Time = 0;
  idle=0;
  file_Index = 0;
  disk_Page_Index = 0;
  fd = 1;
  avg_Pages_On_Disk_Freed=0;
  clock = 0;
  successful_Jobs = 0;
  unsuccessful_Jobs = 0;
  job_Id = 0;
  average_Holes=0;
  average_percentage_Of_Disk_In_Use=0.0;
  number_Of_Holes_In_Disk=0.0;
  mean_Job_Run_Time = 0;
  mean_Job_IO_Time = 0;
  mean_Job_Execution_Time = 0;
  mean_Job_Turn_Around_Time = 0;
  mean_Page_Fault_Time = 0;
  total_CPU_Idle_Time = 0;
  time_Lost_For_Abnormal_Jobs = 0;
  time_Lost_Suspected_Infinite_Job = 0;
  total_Number_Of_Page_Faults = 0;
  queue_Change_Count=0;
  count_MLFB_Queue=0;
  Disk.number_Of_Pages_On_Disk = 256;
  CPU.effective_Address=0;
  CPU.register_Address = 0x00;
  CPU.program_Counter = 0x00;
  CPU.time_Stamp = 0;
  CPU.idle_Clock=500l;
  CPU.counter = 0;
 }
 //This program actually restarts the system again and again.
public static void restart()throws ERROR_HANDLER 
{
    try{
	reinitialize();
   new LOADER().LOADER();
   //get from the ready queue and excute it
   ProcessControlBlock cpu = (ProcessControlBlock) (MEMORY_MANAGER.sub_Queues.get(current_Sub_Queue_Using)).getFirst();
   new CPU().CPU(cpu.program_Counter, 0);
  } catch (ArrayIndexOutOfBoundsException e) {
    new ERROR_HANDLER("NO INPUT FILE PASSED", 401); //In any case if a error or warning occur it throws a object to ERROR_HANDLER.java 
  } 
}
public static void final_Metrics()
{
try
    { 
      //this is to write to progress file
    progress_File_Writer.write(String.format("%n%-50s%-15s%n", "Current CLOCK value(hex):", Long.toHexString(SYSTEM.clock)));
    progress_File_Writer.write(String.format("%-50s%-15s%n", "Mean user job run time(decimal):", round_Off((double) mean_Job_Run_Time / (successful_Jobs + unsuccessful_Jobs-loader_Time_Errors))));
    progress_File_Writer.write(String.format("%-50s%-15s%n", "Mean user job IO time(decimal):", round_Off((double) mean_Job_IO_Time / (successful_Jobs + unsuccessful_Jobs-loader_Time_Errors))));
    progress_File_Writer.write(String.format("%-50s%-15s%n", "Mean user job Execution time(decimal):", round_Off((double) mean_Job_Execution_Time / (successful_Jobs + unsuccessful_Jobs-loader_Time_Errors))));
    progress_File_Writer.write(String.format("%-50s%-15s%n", "Mean user job turnaround time(decimal):", round_Off((double) mean_Job_Turn_Around_Time / (successful_Jobs + unsuccessful_Jobs-loader_Time_Errors))));
    progress_File_Writer.write(String.format("%-50s%-15s%n", "Mean user job page fault handling time(decimal):", round_Off((double) mean_Page_Fault_Time / (successful_Jobs + unsuccessful_Jobs-loader_Time_Errors))));
    progress_File_Writer.write(String.format("%-50s%-15s%n", "Total cpu idle time(hex):", Integer.toHexString(idle)));
    progress_File_Writer.write(String.format("%-50s%-15s%n", "Time lost due to abnormally terminated jobs(hex):", Long.toHexString(time_Lost_For_Abnormal_Jobs-loader_Time_Errors)));
    progress_File_Writer.write(String.format("%-50s%-15s%n", "Normally terminated jobs(decimal):", successful_Jobs));
    progress_File_Writer.write(String.format("%-50s%-15s%n", "Abormally terminated jobs(decimal):", unsuccessful_Jobs));
    progress_File_Writer.write(String.format("%-50s%-15s%n", "Time lost due to suspected infinite jobs(hex):", Integer.toHexString(time_Lost_Suspected_Infinite_Job)));
    if(infinite_Jobs_ID.length()!=0)
    progress_File_Writer.write(String.format("%-50s%-15s%n", "ID's of suspected infinite jobs(decimal):", infinite_Jobs_ID));
    else
    progress_File_Writer.write(String.format("%-50s%-15s%n", "ID's of suspected infinite jobs(decimal):", 0));    
    progress_File_Writer.write(String.format("%-50s%-15s%n", "Total number of page faults encountered(hex):", Long.toHexString((mean_Page_Fault_Time) / 5)));
    progress_File_Writer.write(String.format("%-50s%-15s%n", "Average number of jobs on disk(decimal):", round_Off(((double) mean_Jobs_On_Disk) / (successful_Jobs + unsuccessful_Jobs - loader_Time_Errors))));
    double percentage_Of_Disk_In_Use1 = (1.0 - ((Disk.number_Of_Pages_On_Disk / 256.0))) * 100;
    progress_File_Writer.write(String.format("%-50s%-15s%n", "Percentage of disk occupied(decimal):", round_Off(percentage_Of_Disk_In_Use1)));
    progress_File_Writer.write(String.format("%-50s%-15s%n", "Average percentage of disk occupied(decimal):", round_Off(( average_percentage_Of_Disk_In_Use) / (successful_Jobs + unsuccessful_Jobs - loader_Time_Errors))));
    progress_File_Writer.write(String.format("%-50s%-15s%n", "Number of holes on disk(decimal):",(long)number_Of_Holes_In_Disk ));
    progress_File_Writer.write(String.format("%-50s%-15s%n", "Average number of holes on disk(decimal):", round_Off(( number_Of_Holes_In_Disk) / (successful_Jobs + unsuccessful_Jobs - loader_Time_Errors))));
    progress_File_Writer.write(String.format("%-50s%-15s%n", "Average number of pages freed on disk(decimal):", round_Off(((double) avg_Pages_On_Disk_Freed) / (successful_Jobs + unsuccessful_Jobs - loader_Time_Errors))));
    progress_File_Writer.write(String.format("%-50s%-15s%n", "Average number of unused pages on disk(decimal):", round_Off(((double) average_Unused_Pages) / (successful_Jobs + unsuccessful_Jobs))));
     for(int i=0;i<4;i++)
     	progress_File_Writer.write(String.format("%-25s%-25s%-15s%n", "Average Size of Sub Queue",(i+1)+"(decimal):" ,round_Off(((double)average_Jobs_In_SubQueue[i])/mlfbq_Interval)));

    for(int i=0;i<4;i++)
    {
     progress_File_Writer.write(String.format("%-25s%-25s%-15s%n", "Maximum Jobs in SubQueue",(i+1)+"(decimal):" ,max_Jobs_In_SubQueue[i]));
    }
    progress_File_Writer.flush();
   }catch(Exception e){
   }


}
 public static void main(String args[]) throws ERROR_HANDLER {

  
   //initializing time quantam and number of turns
   turns[0]=3;
   turns[1]=4;
   turns[2]=5;
   time_Quantum[0]=35;
   time_Quantum[1]=40;
   time_Quantum[2]=45;
   time_Quantum[3]=50;
   input_File = args[0]; //It takes the command line arguments in file name
   //adding all queue's to a array only for initialization
   MEMORY_MANAGER.sub_Queues.add(MEMORY_MANAGER.sub_Queue1);
   MEMORY_MANAGER.sub_Queues.add(MEMORY_MANAGER.sub_Queue2);
   MEMORY_MANAGER.sub_Queues.add(MEMORY_MANAGER.sub_Queue3);
   MEMORY_MANAGER.sub_Queues.add(MEMORY_MANAGER.sub_Queue4);   
   for(int batch=0;batch<12;batch++)
   {   ERROR_HANDLER.message = "";

     try{
         progress_File = new FileWriter("ExecutionProfile.txt",false);
         progress_File_Writer = new BufferedWriter(progress_File);
         mlfbq_File=new FileWriter("MLFBQ.txt",false);
         mlfbq_File_Writer = new BufferedWriter(mlfbq_File);
       }catch(Exception e){}
       restart();
       ProcessControlBlock cpu = (ProcessControlBlock) (MEMORY_MANAGER.sub_Queues.get(current_Sub_Queue_Using)).getFirst();
      	boolean check_Point_For_Exit=false;
    	  while(true)
        {
         	 for(int i=0;i<4;i++)
         	{
         
   		      if (!(MEMORY_MANAGER.sub_Queues.get(i)).isEmpty())
              {
		          current_Sub_Queue_Using=i;
       		      check_Point_For_Exit=true;
   			      break;
    		      }
        	}
       	  if (check_Point_For_Exit) 
         {
  	      average_Holes += (float) new MEMORY_MANAGER().get_Free_Pages_In_Memory();
  	      ERROR_HANDLER.batch_Finish=false;
 	        if(new CPU().CPU(cpu.program_Counter, cpu.trace_Flag)==15)
           {
               final_Metrics();  
               matrix_Traffic[batch]=queue_Change_Count;
               try{
                progress_File_Writer.flush();
                progress_File_Writer.close();
                progress_File.close();
                mlfbq_File_Writer.flush();
                mlfbq_File_Writer.close();
                mlfbq_File.close();
                }
                catch(Exception e)
                {}
                 break;
             }
  	     }
      }
    
    current_Time_Quantum++;
    if(current_Time_Quantum==4)
    { 
    current_Time_Quantum=0; current_Turn++;
    
    }
  }
  
  //This part is to write traffic to matrix file.
  try
      {
      matrix_File = new FileWriter("Matrix.txt",false);
      matrix_File_Writer = new BufferedWriter(matrix_File);
      matrix_File_Writer.write(String.format("%-7s%-3s%-7s%-7s%-7s%-7s%n","","q:",time_Quantum[0],time_Quantum[1],time_Quantum[2],time_Quantum[3]));
      for(int i=0;i<12;i++)
      {
        if(i==0)
        {
         
         matrix_File_Writer.write(String.format("%-10s%-7s%-7s%-7s%-7s%n","","(dec)","(dec)","(dec)","(dec)"));
         matrix_File_Writer.write(String.format("%-10s","n:"));
         matrix_File_Writer.write(String.format("%n%-1s%-9s",turns[i/4],"(dec):"));
        }
        else
 		{
        if(i%4==0)   matrix_File_Writer.write(String.format("%n%-1s%-9s",turns[i/4],"(dec):"));
        }
        matrix_File_Writer.write(String.format("%-7s",matrix_Traffic[i])); 

      }
      matrix_File_Writer.flush();
      matrix_File_Writer.close();
      matrix_File.close();
   }
   catch(Exception e){}
 }
//This function writers the given array or page to output disk.
 public static void write_Output_To_Disk(long arr[]) {
  ProcessControlBlock switch_Jobs = new ProcessControlBlock();
  switch_Jobs = (ProcessControlBlock) (MEMORY_MANAGER.sub_Queues.get(current_Sub_Queue_Using)).getFirst();

  for (int i = 0; i < 4; i++) {
   if (switch_Jobs.pages_Allocated_For_Output.length != 0 && switch_Jobs.current_Address_WR_Record < switch_Jobs.output_Length) {
    String to_Binary = Integer.toBinaryString(switch_Jobs.current_Address_WR_Record);
    while (to_Binary.length() < 8)
     to_Binary = "0" + to_Binary;
    int x = Integer.parseInt(to_Binary.substring(0, 4), 2);
    int y = Integer.parseInt(to_Binary.substring(4), 2);

    Disk.DISK[(switch_Jobs.pages_Allocated_For_Output[x] * 16) + y] = arr[i];
    switch_Jobs.current_Address_WR_Record++;
   } else {
   	//if there are more pages required than specified
    ERROR_HANDLER.message = "INSUFFICIENT OUTPUT SPACE (RUN TIME ERROR.CODE:440)";
    SYSTEM.final_Exit(1);
    break;
   }
  }

 }
 //writes to mlfbq file
 public static void write_To_MLFBQ()
 {
   int running=((ProcessControlBlock)(MEMORY_MANAGER.sub_Queues.get(SYSTEM.current_Sub_Queue_Using)).getFirst()).job_ID;
      try{
      mlfbq_File_Writer.write(String.format("%-35s%-15s%n", "Current Clock value(decimal):", SYSTEM.clock));
      for(int i=0;i<4;i++)
      {  String ready_Queue_IDs[];
         Iterator it=MEMORY_MANAGER.sub_Queues.get(i).iterator();
         if(i==SYSTEM.current_Sub_Queue_Using)
           ready_Queue_IDs=new String[MEMORY_MANAGER.sub_Queues.get(i).size()-1];
       	 else
           ready_Queue_IDs=new String[MEMORY_MANAGER.sub_Queues.get(i).size()];     
          int j=0;
       	 while (it.hasNext()) 
       	 	 {
 		     ProcessControlBlock iterated_Job = (ProcessControlBlock) it.next();
       	 	 if(running==iterated_Job.job_ID)
  				   continue;
    		 ready_Queue_IDs[j] = Integer.toHexString(iterated_Job.job_ID);
			     j++;
 			     }
               
         mlfbq_File_Writer.write(String.format("%-8s%-1s%-5s%-15s%n","SubQueue",(i+1),"(hex):",Arrays.toString(ready_Queue_IDs)));        
         
      }
      mlfbq_File_Writer.write("\n");
      mlfbq_File_Writer.flush();}catch(IOException e){}
 }
 
//This function brings input from disk
 public static long get_Input_From_Disk() {
  ProcessControlBlock input_For_Job = (ProcessControlBlock) (MEMORY_MANAGER.sub_Queues.get(current_Sub_Queue_Using)).getFirst();
  String to_Binary = Integer.toBinaryString(input_For_Job.current_Address_RD_Record);
  if (input_For_Job.pages_Allocated_For_Input.length != 0 && input_For_Job.current_Address_RD_Record <= input_For_Job.input_Length) {
   while (to_Binary.length() < 8)
    to_Binary = "0" + to_Binary;
   int x = Integer.parseInt(to_Binary.substring(0, 4), 2);
   int y = Integer.parseInt(to_Binary.substring(4), 2);
   input_For_Job.current_Address_RD_Record++;
   //paging is done here
   return Disk.DISK[((input_For_Job.pages_Allocated_For_Input[x] * 16) + y)];
  } else {
  	//if it is accessing input out of range
   ERROR_HANDLER.message = "ACCESSING INPUT OUT OF DISK (RUN TIME ERROR.CODE:440)";
   //exit in such case
   SYSTEM.final_Exit(1);
   return -1;
  }

 }
 /*
  The below "channel" function is used to take input and display output and 
  declared static because it is needed for CPU subsystem.
 */
 public static String channel(String s, long arr[]) {
   // if(MEMORY_MANAGER.ready_Queue.size()==1)SYSTEM.idle+=8;

  if (s.equals("INPUT")) {
   SYSTEM.IO_time += 8;
   SYSTEM.clock += 8;
   //getting input from disk
   String input = new String();
   for (int i = 0; i < 4; i++) {
     if(ERROR_HANDLER.batch_Finish==true){return null;}
    String word_Read = Long.toHexString(get_Input_From_Disk());
    for (; word_Read.length() < 8;)
     word_Read = "0" + word_Read; //padding extra bits
    input = input + word_Read;
   }
   return input;
  } else {
   SYSTEM.IO_time += 8;
   SYSTEM.clock += 8;
   final_Output = Arrays.toString(arr);
   write_Output_To_Disk(arr);
   return null;
  }
 }
//This function used to print output to progress file from disk
 public static String get_Output_From_Disk(ProcessControlBlock b) {
  String formatted_Output = "";
  //System.out.println("\n\t\tJOB ID"+b.job_ID);
  for (int i = 0, j = -1, k = 0; i < b.current_Address_WR_Record; i++) {
   if (i % 16 == 0) {
    j++;
    k = 0;
   }
   if (i % 4 == 0) {
    formatted_Output = formatted_Output + "\n\t";
   }
   String output_Word = Long.toHexString(Disk.DISK[b.pages_Allocated_For_Output[j] * 16 + k]);
   while (output_Word.length() < 8)
    output_Word = "0" + output_Word;
   formatted_Output = formatted_Output + " " + output_Word;
   k++;
  }
  return formatted_Output;
 }

 static void write_To_Progress_File(ProcessControlBlock b, int flag) {
  try {
  	//string formatting is used here
   progress_File_Writer.write(String.format("%n%-35s%-15s%n", "Job ID(hex):", Integer.toHexString(b.job_ID)));
   progress_File_Writer.write(String.format("%-35s%-15s%n", "Current Clock value(decimal):", SYSTEM.clock));
   progress_File_Writer.write(String.format("%-35s%-15s%n", "Clock at load time(hex):", Long.toHexString(b.time_Job_Entered_System)));
   progress_File_Writer.write(String.format("%-35s%-15s%n", "Clock at termination time(hex):", Long.toHexString(SYSTEM.clock)));
   if (flag == 0) {
    progress_File_Writer.write(String.format("%-35s%-15s%n", "Output(hex):", get_Output_From_Disk(b)));
    if (ERROR_HANDLER.message.length() != 0)
     progress_File_Writer.write(String.format("%-35s%-15s%n", "Warning:", ERROR_HANDLER.message));
    progress_File_Writer.write(String.format("%-35s%-15s%n", "Nature of termination:", "Normal"));
   } else {
    progress_File_Writer.write(String.format("%-35s%-15s%n", "Error:", ERROR_HANDLER.message));
    progress_File_Writer.write(String.format("%-35s%-15s%n", "Nature of termination:", "Abnormal"));
   }
   progress_File_Writer.write(String.format("%-35s%-15s%n", "Turnaround time(hex):", Long.toHexString(SYSTEM.clock - b.time_Job_Entered_System)));
   progress_File_Writer.write(String.format("%-35s%-15s%n", "Run time(hex):", Integer.toHexString(b.page_Faults_Handling_Time + b.IO_Time + b.run_Time)));
   if (flag == 0) {
    progress_File_Writer.write(String.format("%-35s%-15s%n", "Execution time(hex):", Integer.toHexString(b.run_Time)));

   } else {
    progress_File_Writer.write(String.format("%-35s%-15s%n", "Execution time(partial execution)(hex):", Integer.toHexString(b.run_Time)));
   }
   progress_File_Writer.write(String.format("%-35s%-15s%n", "Page fault handling time(hex):", Integer.toHexString(b.page_Faults_Handling_Time)));
   progress_File_Writer.write(String.format("%-35s%-15s%n", "Number of CPU shots(hex):", Integer.toHexString(b.cpu_Shots)));
   progress_File_Writer.flush();

   if (ERROR_HANDLER.message.contains("INFINITE")) {
    total_Number_Of_Suspected_Infinite++;
    infinite_Jobs_ID = infinite_Jobs_ID + " " + b.job_ID;
    time_Lost_Suspected_Infinite_Job = b.run_Time + b.IO_Time + b.page_Faults_Handling_Time;
   }

  } catch (IOException e) {
  }
 }
 //This function is used to round the given number to 2 decimals.
 public static double round_Off(double number) {
  return Math.round(number * 100.0) / 100.0;
 }
 public static String[] contents_In_Queues()
 {
  String id_Of_Job[]=new String[MEMORY_MANAGER.sub_Queues.get(0).size()
  +MEMORY_MANAGER.sub_Queues.get(1).size()
  +MEMORY_MANAGER.sub_Queues.get(2).size()
  +MEMORY_MANAGER.sub_Queues.get(3).size()-1];
  boolean check=true;
  ProcessControlBlock object_For_Queue_Id;
      for(int i=0,j=0;i<4;i++)
         	{   
            Iterator it=MEMORY_MANAGER.sub_Queues.get(i).iterator();
  		      while(it.hasNext())
            {
              object_For_Queue_Id=(ProcessControlBlock) it.next();
               if (i==current_Sub_Queue_Using&&check)
                {
		              running_Job=object_For_Queue_Id.job_ID;
       		        check=false;
   			          continue;
    		        }else
                 {
                    id_Of_Job[j]=Integer.toHexString(object_For_Queue_Id.job_ID);     
                    j++;
                 }
            }
        	}
  return id_Of_Job;
 }
 //This function is used to print status at regular intervals.
 public static void status_Of_Operating_System() {
  double percentage_Of_Disk_In_Use = (1.0 - ((Disk.number_Of_Pages_On_Disk / 256.0))) * 100;
  average_percentage_Of_Disk_In_Use+=percentage_Of_Disk_In_Use;
 	//list class iterator is used to traverse the linked list
    String blocked_Queue_IDs[] = new String[MEMORY_MANAGER.blocked_Queue.size()]; 
    String ready_Queue_IDs[] = contents_In_Queues();
    String running_Job_ID =Integer.toHexString(running_Job);
    int j = 0;
  Iterator it = MEMORY_MANAGER.blocked_Queue.iterator();
  j = 0;
  while (it.hasNext()) {
   ProcessControlBlock iterated_Job = (ProcessControlBlock) it.next();
   blocked_Queue_IDs[j] = Integer.toHexString(iterated_Job.job_ID);
   j++;
  }
  String[] current_Memory_Configuration=new String[ready_Queue_IDs.length+MEMORY_MANAGER.blocked_Queue.size()+1];
  System.arraycopy(blocked_Queue_IDs,0,current_Memory_Configuration,0,blocked_Queue_IDs.length);
  System.arraycopy(ready_Queue_IDs,0,current_Memory_Configuration,blocked_Queue_IDs.length,ready_Queue_IDs.length);
  current_Memory_Configuration[current_Memory_Configuration.length-1]=running_Job_ID;

  try {
   progress_File_Writer.write("\nStatus of operating system");
   String s = String.format("%n%-40s%-15s%n", "Contents Of ready list(hex):", Arrays.toString(ready_Queue_IDs));
   progress_File_Writer.write(s);
   s = String.format("%-40s%-15s%n", "Currently executing job(hex):", "[" + running_Job_ID + "]");
   progress_File_Writer.write(s);
   s = String.format("%-40s%-15s%n", "Contents Of blocked queue(hex):", Arrays.toString(blocked_Queue_IDs));
   progress_File_Writer.write(s);
   s = String.format("%-40s%-15s%n", "Degree of multiprogramming(decimal):", ready_Queue_IDs.length + blocked_Queue_IDs.length + 1);
   progress_File_Writer.write(s);
   s = String.format("%-40s%-15s%n", "Percentage of disk in use(decimal):", round_Off(percentage_Of_Disk_In_Use));
   progress_File_Writer.write(s);
   s = String.format("%-40s%-15s%n", "Number of jobs on disk(hex):",Integer.toHexString( Disk.process_Queue.size()+current_Memory_Configuration.length));
   progress_File_Writer.write(s);
   progress_File_Writer.write("Current Memory Configuration\n");
   s = String.format("%-40s%-15s%n", "Number of jobs in memory(hex):",Arrays.toString(current_Memory_Configuration));
   progress_File_Writer.write(s);
   s = String.format("%-40s%-15s%n", "Number of holes in memory(hex):",Integer.toHexString(new MEMORY_MANAGER().get_Free_Pages_In_Memory()));
   progress_File_Writer.write(s);
   
   progress_File_Writer.flush();
  } catch (Exception e) {
  }
 }
/*
  The below "final_Exit" function is used for managing status of exit and it writes the specified contents to the virtual output "EXECUTION_PROFILE.txt".
  The ERROR_HANDLER and CPU both uses this function.After an error or job finishes it return's here and specified progress is written to
  above mentioned file.
 */
 public static int final_Exit(int status) {
  mean_Jobs_On_Disk += Disk.process_Queue.size();
  
  //taking ready queue linked list object and type casting it.
  ProcessControlBlock executed_Job = (ProcessControlBlock) (MEMORY_MANAGER.sub_Queues.get(current_Sub_Queue_Using)).getFirst();
  Disk.free_Disk_Pages(executed_Job.pages_Allocated_For_Program);
  Disk.free_Disk_Pages(executed_Job.pages_Allocated_For_Input);
  Disk.free_Disk_Pages(executed_Job.pages_Allocated_For_Output);
  Disk.number_Of_Pages_On_Disk += executed_Job.total_Pages;
  new MEMORY_MANAGER().free_Pages_In_Memory(executed_Job.page_Table);
  (MEMORY_MANAGER.sub_Queues.get(current_Sub_Queue_Using)).removeFirst();
  //calculating total progress of batch
  mean_Job_Run_Time += executed_Job.run_Time + executed_Job.IO_Time + executed_Job.page_Faults_Handling_Time;
  mean_Job_IO_Time += executed_Job.IO_Time;
  mean_Job_Execution_Time += executed_Job.run_Time;
  mean_Job_Turn_Around_Time += SYSTEM.clock - executed_Job.time_Job_Entered_System;
  mean_Page_Fault_Time += executed_Job.page_Faults_Handling_Time;
  avg_Pages_On_Disk_Freed+=executed_Job.total_Pages;
  try {
 
   try {
    CPU.trace_Buffered_Writer.close();
    CPU.trace_File_Writer.close();
   } catch (Exception e) {}
   int normal_Abnormal_Flag = 0;
   if (status == 0) /*NORMAL EXECUTION*/ {
    successful_Jobs++;
    normal_Abnormal_Flag = 0;
   } else /*ABNORMAL EXECUTION*/ {
    time_Lost_For_Abnormal_Jobs += executed_Job.run_Time + executed_Job.IO_Time + executed_Job.page_Faults_Handling_Time;
    unsuccessful_Jobs++;
    normal_Abnormal_Flag = 1;
   }
   
   write_To_Progress_File(executed_Job, normal_Abnormal_Flag);
   ERROR_HANDLER.message = "";
   //The below both the functions loads in memory and disk.
   new MEMORY_MANAGER().load_In_Memory();
   new Disk().load_In_Disk();
   ERROR_HANDLER.batch_Finish=true;
  } catch (Exception e) {
   e.printStackTrace();
  }
   return 1;
 }
}