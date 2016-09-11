/*
 Description:
 The loader susbsystem takes the input file which is retrived from the system 
 class.It calls the disk and memory manager to load the jobs from the file 
 and assign jobid's and other respective operations.
*/
import java.io.*;
import java.util.*;
import java.math.BigInteger;
class LOADER extends SYSTEM {
 public void LOADER() throws ERROR_HANDLER {

  long block_Transfer_Buffer;
  //calls the disk 
  new Disk().load_In_Disk();
  //calls the memory manager to schedule operations accorindingly
  new MEMORY_MANAGER().load_In_Memory();

 }
}

//This class has the current state of each and every process and it is freed when execution finishes
// and then later used by other processes.It contains two constructuors which intializes the values at
//the start of the creation of the pcb.
class ProcessControlBlock {
 int number_Of_Turns_In_Current_Queue=0,current_Sub_Queue=0;
 int job_ID, run_Time = 0, IO_Time = 0,cpu_Shots=0;
 long time_Job_Entered_System = -1;
 int cumulative_Time_Used;
 int program_Counter, time_Stamp = 0, counter = 0;
 int trace_Flag, program_Length, output_Length, input_Length, total_Pages;
 int current_Address_RD_Record = 0, current_Address_WR_Record = 0, time_Completion_Of_Current_IO;
 long registers[] = new long[16];
 int extra_Page_Index;
 int page_Table[], pages_Allocated_For_Output[], pages_Allocated_For_Program[], pages_Allocated_For_Input[], count[];
 boolean valid_Bit[], reference_Bit[], dirty_Bit[];
 int page_Faults_Handling_Time = 0;
 int page_Replacement_Count = 0;
 int file_Open_Flag = 0;
 //This constructor is used to create a reference object of the class
 public ProcessControlBlock() {}
 //This constructor just assign's the values and some values are just initialized and used at later times.
 public ProcessControlBlock(int job_Id, int pc_Value, int trace_Bit, int length_Of_Program, int input_Limit, float output_Line_Limit, int program_Pages_Needed, int input_Pages_Needed, int output_Pages_Needed, int[] free_Pages_On_Disk) {
  job_ID = job_Id;
  program_Counter = pc_Value;
  trace_Flag = trace_Bit;
  program_Length = length_Of_Program;
  output_Length = (int) output_Line_Limit * 4;
  input_Length = input_Limit;
  //To keep track of the data store in the disk at the time of page fault and retriving input and writing output into the disk.
  pages_Allocated_For_Program = Arrays.copyOfRange(free_Pages_On_Disk, 0, program_Pages_Needed);
  pages_Allocated_For_Input = Arrays.copyOfRange(free_Pages_On_Disk, program_Pages_Needed, program_Pages_Needed + input_Pages_Needed);
  pages_Allocated_For_Output = Arrays.copyOfRange(free_Pages_On_Disk, program_Pages_Needed + input_Pages_Needed, free_Pages_On_Disk.length);
  page_Table = new int[pages_Allocated_For_Program.length];//creating page table.
  total_Pages = free_Pages_On_Disk.length;
  valid_Bit = new boolean[pages_Allocated_For_Program.length];
  reference_Bit = new boolean[pages_Allocated_For_Program.length];
  dirty_Bit = new boolean[pages_Allocated_For_Program.length];
  //LRU is implemented and this is used to check the reference string count.
  count = new int[pages_Allocated_For_Program.length];

 }
}