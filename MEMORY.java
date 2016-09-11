/*
 Description:
The memory subsystem uses mem[] as static because loader can directly access it.
String buffer is used for memory_buffer_register because pass by reference can 
only be done by it in java.It is sent as string and converted to hexadecimal in 
later case.It does the address translation which is converts the given virtual 
address to physical and maps it to respective page in the page table.In case of 
the page fault based on the given conditions a new page is brought and replaced 
appropriately.For tie breaker LRU is used because at an estimate average number of
page faults occur will be lesser when compared to other algorithms.
*/
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Arrays;
import java.math.BigInteger;
class MEMORY {
 static long MEM[] = new long[256];
 //static int page_Table_Index = 0;
 //static int page_Table[] = new int[16];
 //This function compares boolean values and returns an int which is used in replacement algorithm.
 public static int checkBit(boolean a, boolean b) {
  return ((a) ? 1 : 0) + ((b) ? 1 : 0);
 }
 //page replacement is done with the tie breaker as LRU and implemented with appropriate condidions given.
 public static int page_Replacement_Algorithm() {
  int index = -1, sum = 0;
  //getting the context of the object
  ProcessControlBlock object_For_Replacement = (ProcessControlBlock) (MEMORY_MANAGER.sub_Queues.get(SYSTEM.current_Sub_Queue_Using)).getFirst();
  for (int i = 0; i < object_For_Replacement.page_Table.length; i++) {
   if (object_For_Replacement.valid_Bit[i] == true) {
    if (index == -1) {
     index = i;
    } else {
     int table_Sum = checkBit(object_For_Replacement.reference_Bit[i], object_For_Replacement.dirty_Bit[i]);
     //LRU implementation
     if (sum > table_Sum) {
      index = i;

     } else {
      if (sum == table_Sum) {
       if (object_For_Replacement.count[i] < object_For_Replacement.count[index]) {
        index = i;
       }
      }
     }
    }
   }
  }
  return index;
 }
//Conversion of the virutal address to physical address.
 public static int get_Physical_Address(int memory_Address_Register, String signal) {
  ProcessControlBlock object_For_Replacement = (ProcessControlBlock)(MEMORY_MANAGER.sub_Queues.get(SYSTEM.current_Sub_Queue_Using)).getFirst();
  String to_Binary = Integer.toBinaryString(memory_Address_Register);
  while (to_Binary.length() < 8)
   to_Binary = "0" + to_Binary;
  int x = Integer.parseInt(to_Binary.substring(0, 4), 2);
  int y = Integer.parseInt(to_Binary.substring(4), 2);


  if (x > ((ProcessControlBlock) (MEMORY_MANAGER.sub_Queues.get(SYSTEM.current_Sub_Queue_Using)).getFirst()).page_Table.length - 1) {
    
   SYSTEM.final_Exit(1);
        if(ERROR_HANDLER.batch_Finish==true){return 0;}

  }

//setting the given bits accordingly specified in the specifications.
  if (object_For_Replacement.valid_Bit[x] == false) {
   SYSTEM.page_Faults_Handling_Time += 5;
   SYSTEM.clock += 5;
   int i = page_Replacement_Algorithm();
   object_For_Replacement.valid_Bit[i] = false;
   object_For_Replacement.valid_Bit[x] = true;
   object_For_Replacement.reference_Bit[x] = true;
   object_For_Replacement.count[x] = object_For_Replacement.page_Replacement_Count++;
   object_For_Replacement.page_Table[x] = object_For_Replacement.page_Table[i];
   if (object_For_Replacement.dirty_Bit[i] == true) {
    new MEMORY_MANAGER().write_Page_To_Disk(object_For_Replacement.pages_Allocated_For_Program[i], object_For_Replacement.page_Table[i]);
   }
   new MEMORY_MANAGER().copy_Page_From_Disk(object_For_Replacement.pages_Allocated_For_Program[x], object_For_Replacement.page_Table[x]);


  } else {
   if (signal.contains("WRIT")) {
    object_For_Replacement.dirty_Bit[x] = true;
    object_For_Replacement.reference_Bit[x] = true;
   } else {
    object_For_Replacement.reference_Bit[x] = true;
   }
  }
  return (((ProcessControlBlock) (MEMORY_MANAGER.sub_Queues.get(SYSTEM.current_Sub_Queue_Using)).getFirst()).page_Table[x] * 16) + y;
 }

 /*The below procedure is used by the CPU AND INSTRUCTION SET to in order to preserve the hard boundaries by not accessing MEM[] directly.*/
 public static int MEMORY(String control_Signal, int memory_Address_Register, StringBuffer memory_Buffer_Register) throws ERROR_HANDLER {

  if (!control_Signal.contains("DUMP")) {
   //to get the converted physical address
   memory_Address_Register = get_Physical_Address(memory_Address_Register, control_Signal);
   //The below function is declared in ERROR_HANDLER which is used to check if the value is in given range.
   ERROR_HANDLER.check_Range("INVALID MEMORY RANGE", 103, memory_Address_Register, 0xff);
        if(ERROR_HANDLER.batch_Finish==true){return 0;}
  }

  try {
   switch (control_Signal) {
    case "READ": //The below code replaces contents in memory_Buffer_Register and reassigns with new value
     memory_Buffer_Register = memory_Buffer_Register.replace(0, memory_Buffer_Register.length(), "0x" + Long.toHexString(MEM[memory_Address_Register]));

    case "WRIT":
     String x = memory_Buffer_Register.substring(2);
     BigInteger b = new BigInteger(x, 16);
     long input_Value = b.longValue();
     ERROR_HANDLER.check_Range("NUMBER OUT OF RANGE", 104, input_Value, 0xFFFFFFFFL);
          if(ERROR_HANDLER.batch_Finish==true){return 0;}
     //Taking the contents from memory_Buffer_Register and assigning to MEM[]
     MEM[memory_Address_Register] = input_Value; //Long.decode(memory_Buffer_Register.toString());
     break;
    //now dump actually writer the output to ExecutionProfile file which is our console.
    case "DUMP":
     ProcessControlBlock object_For_Dump = (ProcessControlBlock) (MEMORY_MANAGER.sub_Queues.get(SYSTEM.current_Sub_Queue_Using)).getFirst();
     String dump_To_Progress_File = "";
     for (int i = 0, j = 0; i < object_For_Dump.program_Length; i++, j++) {
      String fetched_Variable;
      if ((j % 8) == 0) //This is just to print index value and new line.
      {
       dump_To_Progress_File = dump_To_Progress_File + "\n";
       String s = Integer.toHexString(j);
       while (s.length() < 4) s = "0" + s; //padding bits
       dump_To_Progress_File = dump_To_Progress_File + s + " ";
      }
      fetched_Variable = Long.toHexString(MEM[get_Physical_Address(i, "DUMP")]);
      while (fetched_Variable.length() < 8)
       fetched_Variable = "0" + fetched_Variable;

      dump_To_Progress_File = dump_To_Progress_File + fetched_Variable + " ";
     }
     SYSTEM.progress_File_Writer.write("\nJOB ID: " + Integer.toHexString(object_For_Dump.job_ID));
     SYSTEM.progress_File_Writer.write(dump_To_Progress_File + "\n");
     break;
    default:
     	  new ERROR_HANDLER("WRONG INPUT SIGNAL PASSED", 105);
          if(ERROR_HANDLER.batch_Finish==true){return 0;}

     //Directly a error can be thrown by creating a object to the ERROR_HANDLER.
   }

  } catch (Exception e) {
        new ERROR_HANDLER("MEMORY FORMAT ERROR", 106);
        if(ERROR_HANDLER.batch_Finish==true){return 0;}

  }
 return 0;}
}

/*
 The memory manger class takes the processes from the disk loads it into the
 memory by checking the available space on the memory.Context switching is done
 by it by copying register's,pc  etc values back and forth.Appropriate functions
 for the number of pages in memory and write and read pages etc are writen to 
 serve given conditions.It contains two queue's ready and blocked which actually
 are implemented using the linked list using queue implementation.Object type 
 casting is done to get the pcb's and to assigning values to pcb when context 
 switched.static variable frame checker is used to know free pages in the memory.
*/

class MEMORY_MANAGER {
//four sub queues are created and attached to the list
 static LinkedList < Object > blocked_Queue = new LinkedList < Object > ();
 static LinkedList < Object > sub_Queue1 = new LinkedList < Object > ();
 static LinkedList < Object > sub_Queue2 = new LinkedList < Object > ();
 static LinkedList < Object > sub_Queue3 = new LinkedList < Object > ();
 static LinkedList < Object > sub_Queue4 = new LinkedList < Object > ();
 static ArrayList<LinkedList<Object>> sub_Queues = new  ArrayList<LinkedList<Object>>(4);
  
 
 //Initially everting is false in the framechecker.
 static boolean frame_Checker[] = new boolean[16];
//To know the free pages in the memory.
 public int get_Free_Pages_In_Memory() {

  int j = 0;
  for (int i = 0; i < 16; i++) {
   if (frame_Checker[i] == false) {
    j++;
   }
  }
  return j;
 }
//This function allocate number of pages and returns the indices for future use.
 public int[] allocate_Pages_In_Memory(int pages_Needed_In_Memory) {
  int arr[] = new int[pages_Needed_In_Memory];
  int j = 0;
  for (int i = 0; i < 16; i++) {
   if (frame_Checker[i] == false) {
    frame_Checker[i] = true;
    arr[j] = i;
    j++;
    if (j == pages_Needed_In_Memory) {
     break;
    }
   }

  }
  return arr;
 }
//In case of dirty bit true this function writes back.
 public void write_Page_To_Disk(int page_Number_Of_Disk, int page_Number_Of_Memory) {
  for (int i = 0; i < 16; i++) {
   Disk.DISK[page_Number_Of_Disk * 16 + i] = MEMORY.MEM[page_Number_Of_Memory * 16 + i];
  }

 }
//this function takes the indices of free pages and frees the pages for next process
 public void free_Pages_In_Memory(int arr[]) {
  ProcessControlBlock object_For_Free = (ProcessControlBlock) (MEMORY_MANAGER.sub_Queues.get(SYSTEM.current_Sub_Queue_Using)).getFirst();
  for (int i = 0; i < arr.length; i++) {
   if (object_For_Free.valid_Bit[i] == true) {
    frame_Checker[arr[i]] = false;
   }
  }
  if (object_For_Free.pages_Allocated_For_Program.length == 1) {
   frame_Checker[object_For_Free.extra_Page_Index] = false;
  }

 }
//To retrive page from the disk and copy into the memory.
 public void copy_Page_From_Disk(int page_Number_Of_Disk, int page_Number_Of_Memory) {
  //load pages in memory
  long arr[] = new Disk().get_Page_From_Disk(page_Number_Of_Disk);
  for (int i = 0; i < 16; i++) {
   MEMORY.MEM[page_Number_Of_Memory * 16 + i] = arr[i];
  }
 }
//This is the main function which loads into the memory and 
//initializes the page table and pushes into the ready queue when
//the sufficient space is available.
 public void load_In_Memory() {
 //creating a referece variable for future use.
  ProcessControlBlock jobs_On_Disk = new ProcessControlBlock();
  //loading until it's not empty
  while (!SYSTEM.process_Queue.isEmpty()) {
   //getting  indices of free pages
   int pages_Available_In_Memory = get_Free_Pages_In_Memory();
   //taking from the disk table and loading into memory.
   jobs_On_Disk = (ProcessControlBlock) SYSTEM.process_Queue.getFirst();
   //this is assgin minimum number of pages as specified for a job
   int initial_Pages_To_Allocate_For_Job = jobs_On_Disk.pages_Allocated_For_Program.length;
   initial_Pages_To_Allocate_For_Job /= 3;

   if (initial_Pages_To_Allocate_For_Job < 2) {
    initial_Pages_To_Allocate_For_Job = 2;
   }
   //if pages avalilable in memory then load.
   if (pages_Available_In_Memory > initial_Pages_To_Allocate_For_Job) {
    if (jobs_On_Disk.pages_Allocated_For_Program.length != 1) {
     //page table initialized
     System.arraycopy(allocate_Pages_In_Memory(initial_Pages_To_Allocate_For_Job), 0, jobs_On_Disk.page_Table, 0, initial_Pages_To_Allocate_For_Job);
     for (int i = 0; i < initial_Pages_To_Allocate_For_Job; i++) {
      jobs_On_Disk.valid_Bit[i] = true;
      jobs_On_Disk.count[i] = 0;
      copy_Page_From_Disk(jobs_On_Disk.pages_Allocated_For_Program[i], jobs_On_Disk.page_Table[i]);
     }
    } else {
     int arr[] = allocate_Pages_In_Memory(initial_Pages_To_Allocate_For_Job);
     System.arraycopy(arr, 0, jobs_On_Disk.page_Table, 0, 1);
     jobs_On_Disk.valid_Bit[0] = true;
     copy_Page_From_Disk(jobs_On_Disk.pages_Allocated_For_Program[0], jobs_On_Disk.page_Table[0]);
     jobs_On_Disk.extra_Page_Index = arr[1];
    }
    (MEMORY_MANAGER.sub_Queues.get(0)).addLast(jobs_On_Disk);
    SYSTEM.process_Queue.removeFirst();
   } else {
    //if sufficient pages not available
    break;
   }
  }
 }
 //to load the blocked process.
 public void load_Blocked_Process() {
  load_In_Memory();
 }


}
