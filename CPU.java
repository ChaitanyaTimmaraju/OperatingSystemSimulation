/*
 Description:
 The cpu subsystem is called by the system.The cpu is divided into two modules
 cpu and instruction set in order to maintain modularity.The below code simply 
 assigns the values to appropriate registers and checks for the values out of 
 range,and writes to trace file.The Operations are performed in the subclass.
 It loops indefinitely until a error occours.The below variables are used as 
 static because they are accessed by the instruction set class which is below.
 By using it also it completely adheres restrictions held by conventional architecture.
 Context switching is done after every 40 clock cycles and after IO operation.
*/
import java.io.*;
import java.util.*;
import java.math.BigInteger;
import java.util.Scanner;
class CPU extends SYSTEM {
 static int file_Trace_Bit = 0;
 static long CPU_REGISTERS[] = new long[16];
 static int effective_Address, register_Address = 0x00;
 static int program_Counter = 0x00;
 static StringBuffer memory_Buffer_Register = new StringBuffer(""); //This is used to transfer data between memory and cpu.
 static int OP_CODE;
 static long time_Stamp = 0,idle_Clock=500l;
 static int counter = 0;
 static FileWriter trace_File_Writer = null;
 static BufferedWriter trace_Buffered_Writer = null;
 ProcessControlBlock switch_Jobs = new ProcessControlBlock();
 static String x;


// to get the previous process context
 public void retrive_Process_Context() {

for(int i=0;i<4;i++)
 {
  if(!(MEMORY_MANAGER.sub_Queues.get(i)).isEmpty())
   	 {
		 SYSTEM.current_Sub_Queue_Using=i;
	     break;
  	  }
 }

  if (!(MEMORY_MANAGER.sub_Queues.get(SYSTEM.current_Sub_Queue_Using)).isEmpty()) {
   switch_Jobs = (ProcessControlBlock) (MEMORY_MANAGER.sub_Queues.get(SYSTEM.current_Sub_Queue_Using)).getFirst();
   for (int i = 0; i < 16; i++) {
    CPU_REGISTERS[i] = switch_Jobs.registers[i];
   }
   time_Stamp = SYSTEM.clock;switch_Jobs.cpu_Shots++;
   program_Counter = switch_Jobs.program_Counter;
   counter = switch_Jobs.counter;
   file_Trace_Bit = switch_Jobs.trace_Flag;
   SYSTEM.IO_time = switch_Jobs.IO_Time;
   SYSTEM.execution_Time = switch_Jobs.run_Time;
   counter = SYSTEM.IO_time + SYSTEM.execution_Time + SYSTEM.page_Faults_Handling_Time;
   SYSTEM.page_Faults_Handling_Time = switch_Jobs.page_Faults_Handling_Time;
   try {

    if (file_Trace_Bit == 1) { //If trace bit is on write to TRACE.txt file
     if (switch_Jobs.file_Open_Flag == 0) {
      switch_Jobs = (ProcessControlBlock) (MEMORY_MANAGER.sub_Queues.get(SYSTEM.current_Sub_Queue_Using)).getFirst();
      trace_File_Writer = new FileWriter(switch_Jobs.job_ID + ".txt",false);
      trace_Buffered_Writer = new BufferedWriter(trace_File_Writer);
      //The below code is used to formatting the string while writing to trace file.
      trace_Buffered_Writer.write(String.format("%-12s%-14s%-12s%-12s%-12s%-12s%n", "PROGRAM", "INSTRUCTION", "C(A)BEFORE", "C(A)AFTER", "C(EA)BEFORE", "C(EA)AFTER"));
      trace_Buffered_Writer.write(String.format("%-12s%-14s%-12s%-12s%-12s%-12s%n", "COUNTER", "(HEX)", "EXECUTION", "EXECUTION", "EXECUTION", "EXECUTION"));
      trace_Buffered_Writer.write(String.format("%-12s%-14s%-12s%-12s%-12s%-12s%n", "(HEX)", "", "(HEX)", "(HEX)", "(HEX)", "(HEX)"));
      switch_Jobs.file_Open_Flag = 1;
     } else {
      trace_File_Writer = new FileWriter(switch_Jobs.job_ID + ".txt", true);
      trace_Buffered_Writer = new BufferedWriter(trace_File_Writer);
     }
    }
   } catch (IOException e) {}
   if (switch_Jobs.time_Job_Entered_System == -1) {
    switch_Jobs.time_Job_Entered_System = SYSTEM.clock;
   }
  } else {
     boolean caught=false;
   	for(int i=0;i<4;i++)
   	{
   	 if(!(MEMORY_MANAGER.sub_Queues.get(i)).isEmpty())
   	 {
   	      caught=true;
		 SYSTEM.current_Sub_Queue_Using=i;
		 retrive_Process_Context();
	  	break;
  	  }
 	}
   if(caught==false)
   {
    
 	if(add_Blocked_Processes())
  		  retrive_Process_Context();
   }
  }

 }
 //To add blocked process to queue.
 public boolean add_Blocked_Processes() {
   
  if (!MEMORY_MANAGER.blocked_Queue.isEmpty()) {
   switch_Jobs = (ProcessControlBlock) MEMORY_MANAGER.blocked_Queue.getFirst();
   MEMORY_MANAGER.blocked_Queue.removeFirst();
   switch_Jobs.number_Of_Turns_In_Current_Queue=0;
   if(SYSTEM.current_Sub_Queue_Using==3)
   {
      SYSTEM.queue_Change_Count++;
      (MEMORY_MANAGER.sub_Queues.get(0)).addLast(switch_Jobs);
   }else
   {
   (MEMORY_MANAGER.sub_Queues.get(switch_Jobs.current_Sub_Queue)).addLast(switch_Jobs);
    }
    return true;
  }
  
  return false;
 }

public int get_Which_Queue_To_Add_Process(int turns)
{
 int max_Time_Quantum_In_Queue=(SYSTEM.turns[SYSTEM.current_Turn]+(SYSTEM.current_Sub_Queue_Using*2));
 if(current_Sub_Queue_Using==3)
 {
   if(turns*max_Time_Quantum_In_Queue*SYSTEM.time_Quantum[SYSTEM.current_Time_Quantum]>=(9*SYSTEM.turns[SYSTEM.current_Turn]*SYSTEM.time_Quantum[SYSTEM.current_Time_Quantum]))
   {
         SYSTEM.queue_Change_Count++;
    return 0;
   }else
   {
    return current_Sub_Queue_Using;
   }
   
 }
 if(turns>=max_Time_Quantum_In_Queue)
 {
    SYSTEM.queue_Change_Count++;
    return current_Sub_Queue_Using+1;
 }else
 {
   return current_Sub_Queue_Using;
 }
   
}

//It context switches and takes a job from the queue and put it into last and 
//takes the job at first in the linked list.
 public void context_Switch(int flag) {
  try {
    switch_Jobs = (ProcessControlBlock)(MEMORY_MANAGER.sub_Queues.get(SYSTEM.current_Sub_Queue_Using)).getFirst();
    int index_Of_Queue_To_Add=get_Which_Queue_To_Add_Process(switch_Jobs.number_Of_Turns_In_Current_Queue);
    
    for (int i = 0; i < 16; i++) {
     switch_Jobs.registers[i] = CPU_REGISTERS[i];
    }
    if (flag == 0) {
     switch_Jobs.number_Of_Turns_In_Current_Queue++;
     switch_Jobs.page_Faults_Handling_Time = SYSTEM.page_Faults_Handling_Time;
     switch_Jobs.IO_Time = SYSTEM.IO_time;
     switch_Jobs.run_Time = SYSTEM.execution_Time;
     switch_Jobs.program_Counter = program_Counter;
     (MEMORY_MANAGER.sub_Queues.get(SYSTEM.current_Sub_Queue_Using)).removeFirst();
     if(index_Of_Queue_To_Add!=current_Sub_Queue_Using)
     {
      switch_Jobs.number_Of_Turns_In_Current_Queue=0;
      (MEMORY_MANAGER.sub_Queues.get(index_Of_Queue_To_Add)).addLast(switch_Jobs);
     }else
     {
      (MEMORY_MANAGER.sub_Queues.get(index_Of_Queue_To_Add)).addLast(switch_Jobs);      
     }
     
     if (file_Trace_Bit == 1) {
      file_Trace_Bit = 0;
      CPU.trace_Buffered_Writer.flush();
      CPU.trace_Buffered_Writer.close();
      CPU.trace_File_Writer.close();
     }
     retrive_Process_Context();
    } else {
     switch_Jobs.page_Faults_Handling_Time = SYSTEM.page_Faults_Handling_Time;
     switch_Jobs.IO_Time = SYSTEM.IO_time;
     switch_Jobs.run_Time = SYSTEM.execution_Time;
     switch_Jobs.program_Counter = program_Counter;
     switch_Jobs.program_Counter++;
     switch_Jobs.current_Sub_Queue=current_Sub_Queue_Using;
     (MEMORY_MANAGER.sub_Queues.get(SYSTEM.current_Sub_Queue_Using)).removeFirst();
     MEMORY_MANAGER.blocked_Queue.addLast(switch_Jobs);
     if (file_Trace_Bit == 1) {
      file_Trace_Bit = 0;
      //just to flush everyting in the buffer
      CPU.trace_Buffered_Writer.flush();
      CPU.trace_Buffered_Writer.close();
      CPU.trace_File_Writer.close();
      
     }
    }

   
  } catch (IOException e) {
  }
 }


 public int CPU(int PC, int trace_Flag) throws ERROR_HANDLER {

  retrive_Process_Context();
  
  try {


   while (true) //executes infinitely
   {
    
    ERROR_HANDLER.check_Range("SUSPECTED INFINITE JOB", 302, (SYSTEM.clock - time_Stamp) + counter, 1000000l);
         if(ERROR_HANDLER.batch_Finish==true){return 0;}
    ERROR_HANDLER.check_Range("PROGRAM COUNTER VALUE OUT OF RANGE", 302, location_Of_First_Address_Loaded, 255);
    MEMORY.MEMORY("READ", program_Counter, memory_Buffer_Register);
        if(ERROR_HANDLER.batch_Finish==true){return 0;}
    String instruction_For_Trace=memory_Buffer_Register.toString();
    //converting the memory_Buffer_Register to binary format for checking the appropriate addressing modes and fetching OP_CODE and register address values.
    String binary_String_For_Comparison = Long.toBinaryString(Long.decode(memory_Buffer_Register.toString()));
    x = Long.toHexString(Long.decode(memory_Buffer_Register.toString()));
    for (; binary_String_For_Comparison.length() < 32;) //padding bits
     binary_String_For_Comparison = "0" + binary_String_For_Comparison;

    effective_Address = Integer.parseInt(binary_String_For_Comparison.substring(16), 2);
    ERROR_HANDLER.check_Range("EFFECTIVE ADDRESS VALUE OUT OF RANGE", 303, register_Address, 255);
     if(ERROR_HANDLER.batch_Finish==true){return 0;}

     //To handle index and indirect addressing
    if (binary_String_For_Comparison.charAt(0) == '1' && !(binary_String_For_Comparison.substring(12, 16).equals("0000"))) {

     MEMORY.MEMORY("READ", effective_Address, memory_Buffer_Register);
          if(ERROR_HANDLER.batch_Finish==true){return 0;}
     long l = Long.decode(memory_Buffer_Register.toString());
     effective_Address = (int) l;
     int indexAddr = Integer.parseInt(binary_String_For_Comparison.substring(12, 16), 2);
     effective_Address = (int) CPU_REGISTERS[indexAddr] + effective_Address;

    } else {
     if (binary_String_For_Comparison.charAt(0) == '0' && !(binary_String_For_Comparison.substring(12, 16).equals("0000"))) { //To check if it's not indirect and it's index addressing.

      MEMORY.MEMORY("READ", effective_Address, memory_Buffer_Register);
           if(ERROR_HANDLER.batch_Finish==true){return 0;}
      int indexAddr = Integer.parseInt(binary_String_For_Comparison.substring(12, 16), 2);
      effective_Address = (int) CPU_REGISTERS[indexAddr] + effective_Address;
     } else {
      if (binary_String_For_Comparison.charAt(0) == '1') {
       //To check if its indirect addressing.
       MEMORY.MEMORY("READ", effective_Address, memory_Buffer_Register);
            if(ERROR_HANDLER.batch_Finish==true){return 0;}
       long l = Long.decode(memory_Buffer_Register.toString()); //converting into long and type casting it to integer.
       effective_Address = (int) l;
      }
     }
    }

    register_Address = Integer.parseInt(binary_String_For_Comparison.substring(8, 12), 2); //Assigning address for CPU_REGISTERS.
    ERROR_HANDLER.check_Range("REGISTER ADDRESS VALUE OUT OF RANGE", 303, register_Address, 15);
         if(ERROR_HANDLER.batch_Finish==true){return 0;}
    OP_CODE = Integer.parseInt(binary_String_For_Comparison.substring(1, 8), 2); //Retriving OPCODE.
    ERROR_HANDLER.check_Range("INVALID OP CODE", 304, OP_CODE, 17);
     if(ERROR_HANDLER.batch_Finish==true){return 0;}
   //to write trace.
    if (file_Trace_Bit == 1) {

     String s = Long.toHexString(CPU_REGISTERS[register_Address]);
     MEMORY.MEMORY("READ", effective_Address, memory_Buffer_Register);
          if(ERROR_HANDLER.batch_Finish==true){return 0;}
     String s2 = new String(memory_Buffer_Register.replace(0, 2, ""));
     new InstructionSet().instructionList(OP_CODE);
     if(ERROR_HANDLER.batch_Finish==true){return 0;}
     MEMORY.MEMORY("READ", effective_Address, memory_Buffer_Register);
     if(ERROR_HANDLER.batch_Finish==true){return 0;}
     memory_Buffer_Register.replace(0, 2, "");
     int x = program_Counter;
     if (x < 0) x++;
     s = String.format("%-12s%-14s%-12s%-12s%-12s%-12s%n", Integer.toHexString(x), instruction_For_Trace.substring(2), s, Long.toHexString(CPU_REGISTERS[register_Address]), s2, memory_Buffer_Register);
     //format the string and writes to TRACE.txt file
     trace_Buffered_Writer.write(s);
     trace_Buffered_Writer.flush();
    } else //if trace flag!=0 just execute
    {
     new InstructionSet().instructionList(OP_CODE); //calling the InstructionSet class with OP_CODE to perform operations.
         if(ERROR_HANDLER.batch_Finish==true){return 0;}
    }
    program_Counter++;

if(SYSTEM.clock>SYSTEM.count_MLFB_Queue*1200)
{
  SYSTEM.write_To_MLFBQ();	
 	SYSTEM.count_MLFB_Queue++;
}
    //TO calculate sample sizes
if(SYSTEM.mlfbq_Interval*500>SYSTEM.clock)
{

	SYSTEM.average_Jobs_In_SubQueue[0]+=MEMORY_MANAGER.sub_Queues.get(0).size();
	SYSTEM.average_Jobs_In_SubQueue[1]+=MEMORY_MANAGER.sub_Queues.get(1).size();
	SYSTEM.average_Jobs_In_SubQueue[2]+=MEMORY_MANAGER.sub_Queues.get(2).size();
	SYSTEM.average_Jobs_In_SubQueue[3]+=MEMORY_MANAGER.sub_Queues.get(3).size();
    
    for(int i=0;i<4;i++)
     {	
       if(max_Jobs_In_SubQueue[i]<=MEMORY_MANAGER.sub_Queues.get(i).size())
       {
          max_Jobs_In_SubQueue[i]=MEMORY_MANAGER.sub_Queues.get(i).size();
       }
     } 
  
     SYSTEM.mlfbq_Interval++;
}

    if(idle_Clock/SYSTEM.clock==0)
    {
     idle_Clock+=10000;
     SYSTEM.release = true;
    }
    //this is to print in regular intervals state of system
    if (!MEMORY_MANAGER.blocked_Queue.isEmpty() && SYSTEM.release == true) {
     SYSTEM.status_Of_Operating_System();
     SYSTEM.release = false;
    }
    //The add blocked processes
    if (SYSTEM.clock - time_Stamp >= 8) {
     add_Blocked_Processes();
    }
    //context switch
    if (SYSTEM.clock - time_Stamp >= SYSTEM.time_Quantum[SYSTEM.current_Time_Quantum]*(SYSTEM.turns[SYSTEM.current_Turn]+(SYSTEM.current_Sub_Queue_Using*2))) {
     context_Switch(0);
    }
   }

  } catch (NumberFormatException e) {
    new ERROR_HANDLER("INVALID NUMBER FORMAT", 304);
       if(ERROR_HANDLER.batch_Finish==true){return 0;}
  } catch (FileNotFoundException e) {
  new ERROR_HANDLER("CANNOT OPEN TRACE FILE", 403);
       if(ERROR_HANDLER.batch_Finish==true){return 0;}
  } 
  catch(NoSuchElementException e)
  {   
   return 15;
  }
  catch (Exception e) {
        e.printStackTrace();
        new ERROR_HANDLER("CPU BOUND EXCEPTION", 305);
        if(ERROR_HANDLER.batch_Finish==true){return 0;}
  }
  return 1;
 }

}

/*
 Description:
 This subsystem is extended class of cpu subsystem.It just performs the appropriate operation and terminate.
*/
class InstructionSet extends CPU {
 //This function takes string and returns long
 long convert_To_Long(StringBuffer mbr) {
  String x = mbr.substring(2);
  BigInteger b = new BigInteger(x, 16);
  long input_Value = b.longValue();
  return input_Value;
 }
//To check the range of the number
 void check_Number(long number) {
  if ((number < -0x7fffffff || number > 0x7fffffff)) {
   new ERROR_HANDLER("CONTENT OF REGISTER OUT OF RANGE", 306);
  }
 }
 //This function takes the number and does two's complement of that.
 public long get_Original_Number(long x) {
  String number = new String(Long.toBinaryString(x));
  while (number.length() < 32)
   number = "0" + number;
  StringBuilder converted_Number = new StringBuilder(number);
  if (converted_Number.charAt(0) == '1') {
   converted_Number.setCharAt(0, '0');
   check_Number(-Long.parseLong(converted_Number.toString(), 2));
   return -Long.parseLong(converted_Number.toString(), 2);
  } else {
   return x;
  }
 }

//This function after the operation preserves the original system format of the number.
 public long get_System_Number(long x) {

  if (x < 0) {
   x = -x;
   String number = new String(Long.toBinaryString(x));
   while (number.length() < 32)
    number = "0" + number;
   StringBuilder converted_Number = new StringBuilder(number);
   converted_Number.setCharAt(0, '1');
   return Long.parseLong(converted_Number.toString(), 2);
  } else {
   return x;
  }
 }

 public int instructionList(int OP_CODE) throws ERROR_HANDLER {
  try {
   switch (OP_CODE) {
    case 0x00:
     SYSTEM.execution_Time++;
     SYSTEM.clock++;
     SYSTEM.final_Exit(0); //SUCCESSFUL TERMINATION
     break;

    case 0x01: //The below code reads the value puts in memory_Buffer_Register and then converts the value from hex String to long and assign
     //to CPU_REGISTERS. 
     MEMORY.MEMORY("READ", effective_Address, memory_Buffer_Register);
     CPU_REGISTERS[register_Address] = convert_To_Long(memory_Buffer_Register); //Long.decode(memory_Buffer_Register.toString());
     SYSTEM.clock++;
     SYSTEM.execution_Time++;

     break;

    case 0x02:
     memory_Buffer_Register.replace(0, memory_Buffer_Register.length(), "0x" + Long.toHexString(CPU_REGISTERS[register_Address]));
     MEMORY.MEMORY("WRIT", effective_Address, memory_Buffer_Register);
     SYSTEM.clock++;
     SYSTEM.execution_Time++;

     break;

    case 0x03:
     MEMORY.MEMORY("READ", effective_Address, memory_Buffer_Register);
     if(ERROR_HANDLER.batch_Finish==true){return 0;}
     check_Number(get_Original_Number(CPU_REGISTERS[register_Address]) + get_Original_Number( convert_To_Long(memory_Buffer_Register)));
     if(ERROR_HANDLER.batch_Finish==true){return 0;}
     CPU_REGISTERS[register_Address] = get_System_Number(get_Original_Number(CPU_REGISTERS[register_Address]) + get_Original_Number(convert_To_Long(memory_Buffer_Register)));
     SYSTEM.clock++;
     SYSTEM.execution_Time++;

     break;

    case 0x04:
     MEMORY.MEMORY("READ", effective_Address, memory_Buffer_Register);
     if(ERROR_HANDLER.batch_Finish==true){return 0;}
     check_Number(get_Original_Number(CPU_REGISTERS[register_Address]) - get_Original_Number(Long.decode(memory_Buffer_Register.toString())));
       if(ERROR_HANDLER.batch_Finish==true){return 0;}
     CPU_REGISTERS[register_Address] = get_System_Number(get_Original_Number(CPU_REGISTERS[register_Address]) - get_Original_Number(convert_To_Long(memory_Buffer_Register)));
     SYSTEM.clock++;
     SYSTEM.execution_Time++;

     break;

    case 0x05:
     MEMORY.MEMORY("READ", effective_Address, memory_Buffer_Register);
          if(ERROR_HANDLER.batch_Finish==true){return 0;}
     check_Number(get_Original_Number(CPU_REGISTERS[register_Address]) * get_Original_Number(Long.decode(memory_Buffer_Register.toString())));
     if(ERROR_HANDLER.batch_Finish==true){return 0;}
     CPU_REGISTERS[register_Address] = get_System_Number(get_Original_Number(CPU_REGISTERS[register_Address]) * get_Original_Number(convert_To_Long(memory_Buffer_Register)));
     SYSTEM.clock += 2;
     SYSTEM.execution_Time += 2;
     break;

    case 0x06:
     MEMORY.MEMORY("READ", effective_Address, memory_Buffer_Register);
          if(ERROR_HANDLER.batch_Finish==true){return 0;}
     if (Long.decode(memory_Buffer_Register.toString()) == 0) new ERROR_HANDLER("DIVIDE BY ZERO", 201);
          if(ERROR_HANDLER.batch_Finish==true){return 0;}
     check_Number(get_Original_Number(CPU_REGISTERS[register_Address]) / get_Original_Number(Long.decode(memory_Buffer_Register.toString())));
        if(ERROR_HANDLER.batch_Finish==true){return 0;}
     CPU_REGISTERS[register_Address] = get_System_Number(get_Original_Number(CPU_REGISTERS[register_Address]) / get_Original_Number(convert_To_Long(memory_Buffer_Register)));
          if(ERROR_HANDLER.batch_Finish==true){return 0;}
     SYSTEM.clock += 2;
     SYSTEM.execution_Time += 2;
     break;

    case 0x07:
     MEMORY.MEMORY("READ", effective_Address, memory_Buffer_Register);
          if(ERROR_HANDLER.batch_Finish==true){return 0;}
     CPU_REGISTERS[register_Address] = CPU_REGISTERS[register_Address] << Long.decode(memory_Buffer_Register.toString());
     //for no wrap around
     CPU_REGISTERS[register_Address] %= 0x100000000l;
     SYSTEM.clock++;
     SYSTEM.execution_Time++;

     break;

    case 0x08:
     MEMORY.MEMORY("READ", effective_Address, memory_Buffer_Register);
          if(ERROR_HANDLER.batch_Finish==true){return 0;}
     CPU_REGISTERS[register_Address] = CPU_REGISTERS[register_Address] >> Long.decode(memory_Buffer_Register.toString());
     CPU_REGISTERS[register_Address] %= 0x100000000l;
     //  ERROR_HANDLER.check_Range("CONTENT OF REGISTER OUT OF RANGE",310,Long.decode(memory_Buffer_Register.toString()),0xffffffffL);
     SYSTEM.clock++;
     SYSTEM.execution_Time++;

     break;

    case 0x09:
     check_Number(get_Original_Number(CPU_REGISTERS[register_Address]));
          if(ERROR_HANDLER.batch_Finish==true){return 0;}
     if (get_Original_Number(CPU_REGISTERS[register_Address]) < 0) {
      program_Counter = effective_Address;
      program_Counter--;
      //here and in the below cases it decrements program_Counter because after taking effective address, in the CPU it increments again. 
     }
     SYSTEM.clock++;
     SYSTEM.execution_Time++;

     break;

    case 0x0A:
     check_Number(get_Original_Number(CPU_REGISTERS[register_Address]));
          if(ERROR_HANDLER.batch_Finish==true){return 0;}
     if (get_Original_Number(CPU_REGISTERS[register_Address]) > 0) {
          if(ERROR_HANDLER.batch_Finish==true){return 0;}
      program_Counter = effective_Address;
      program_Counter--;
     }
     SYSTEM.clock++;
     SYSTEM.execution_Time++;

     break;

    case 0x0B:
     if (get_Original_Number(CPU_REGISTERS[register_Address]) == 0) {
          if(ERROR_HANDLER.batch_Finish==true){return 0;}
      program_Counter = effective_Address;
      program_Counter--;
     }
     SYSTEM.clock++;
     SYSTEM.execution_Time++;

     break;

    case 0x0C:
     MEMORY.MEMORY("READ", program_Counter, memory_Buffer_Register);
          if(ERROR_HANDLER.batch_Finish==true){return 0;}
     CPU_REGISTERS[register_Address] = Long.decode(memory_Buffer_Register.toString());
     program_Counter = effective_Address;
     program_Counter--;
     SYSTEM.clock += 2;
     SYSTEM.execution_Time += 2;
     break;

    case 0x0D:
     MEMORY.MEMORY("READ", effective_Address, memory_Buffer_Register);
          if(ERROR_HANDLER.batch_Finish==true){return 0;}
     CPU_REGISTERS[register_Address] = CPU_REGISTERS[register_Address] & Long.decode(memory_Buffer_Register.toString());
     SYSTEM.clock++;
     SYSTEM.execution_Time++;

     break;

    case 0x0E:
     MEMORY.MEMORY("READ", effective_Address, memory_Buffer_Register);
          if(ERROR_HANDLER.batch_Finish==true){return 0;}
     CPU_REGISTERS[register_Address] = CPU_REGISTERS[register_Address] | Long.decode(memory_Buffer_Register.toString());
     SYSTEM.clock++;
     SYSTEM.execution_Time++;
     break;

    case 0x0F:
     int x = effective_Address; //just to preserve the state of effective address
     //Control is sent to SYSTEM and input of 4 words are received at once from buffer.
     String input_String = SYSTEM.channel("INPUT", null);
          if(ERROR_HANDLER.batch_Finish==true){return 0;}
     for (int i = 0, index = 0; i < 4; i++, index += 8) {
      String buffer = "0x" + input_String.substring(index, index + 8);
      memory_Buffer_Register.replace(0, memory_Buffer_Register.length(), buffer);
      new MEMORY().MEMORY("WRIT", x, memory_Buffer_Register);
           if(ERROR_HANDLER.batch_Finish==true){return 0;}
   	  if(ERROR_HANDLER.batch_Finish==true){return 0;}
      x++;
     }
     SYSTEM.clock += 2;
     SYSTEM.execution_Time += 2; 
     //in I/O case instruction is incremented twice
      new CPU().context_Switch(1);
      new CPU().CPU(0, 0);
     break;

    case 0x10:
     int y = effective_Address;
     String output_String = "";
     long output_Arr[] = new long[4];
     for (int i = 0; i < 4; i++) {

      MEMORY.MEMORY("READ", y, memory_Buffer_Register);
     if(ERROR_HANDLER.batch_Finish==true){return 0;}
      output_Arr[i] = Long.decode(memory_Buffer_Register.toString());
      y++;
     }
     SYSTEM.channel(output_String, output_Arr);
     if(ERROR_HANDLER.batch_Finish==true){return 0;}
     SYSTEM.clock += 2;
     SYSTEM.execution_Time += 2; //in I/O case instruction is incremented twice
      new CPU().context_Switch(2); 
      new CPU().CPU(0, 0);
     break;

    case 0x11:
     MEMORY.MEMORY("DUMP", 0, null);
          if(ERROR_HANDLER.batch_Finish==true){return 0;}
     SYSTEM.clock++;

     SYSTEM.execution_Time++;
     break;

   }
  } catch (NumberFormatException e) {
   new ERROR_HANDLER("INVALID INPUT", 312); 
        if(ERROR_HANDLER.batch_Finish==true){return 0;}
    //this throws error if there is invalid input
  }
return 1;
 }

}