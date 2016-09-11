#!/bin/bash
#Print effective username
echo "whoami | name" 
whoami | name       
#Display files
echo "ls -ltr"
ls -ltr
#Compilation of the program's
echo "javac *.java"
javac *.java
echo "ls -ltr"
ls -ltr
#Format and convert text files for printing
echo "pr -n *.java"
pr -n *.java
#Execution of the program's for tb
echo "java SYSTEM /home/opsys/SPR16/tb"
java SYSTEM /home/opsys/SPR16/tb
#Reading of the file
echo "cat -n ExecutionProfile.txt| head -40"
cat -n ExecutionProfile.txt| head -40
echo "ls"
ls
echo "cat -n ExecutionProfile.txt | tail -3000 |head -40"
cat -n ExecutionProfile.txt | tail -3000 |head -40
echo "ls"
ls
echo "cat -n ExecutionProfile.txt| tail -1400 |head -40"
cat -n ExecutionProfile.txt| tail -1400 |head -40
echo "ls"
ls
echo "cat -n ExecutionProfile.txt | tail -600|head -40"
cat -n ExecutionProfile.txt | tail -600|head -40
echo "ls"
ls
echo "cat -n ExecutionProfile.txt| tail -200 |head -40"
cat -n ExecutionProfile.txt| tail -200 |head -40
echo "ls"
ls
echo "cat -n ExecutionProfile.txt | tail -40"
cat -n ExecutionProfile.txt | tail -40
#The below commands are for tracefiles and the file names are started with job id's.
echo "cat -n 4.txt| head -20"
cat -n 4.txt| head -20   
echo "ls"
ls
echo "cat -n 4.txt| tail -20"
cat -n 4.txt| tail -20
echo "cat -n 9.txt| head -20"
cat -n 9.txt| head -20
echo "ls"
ls
echo "cat -n 9.txt| tail -20"
cat -n 9.txt| tail -20
echo "cat -n 76.txt| head -20"
cat -n 76.txt| head -20
echo "ls"
ls
echo "cat -n 76.txt| tail -20"
cat -n 76.txt| tail -20
echo "cat -n 38.txt| head -20"
cat -n 38.txt| head -20
echo "ls"
ls
echo "cat -n 38.txt| tail -20"
cat -n 38.txt| tail -20
echo "cat -n Matrix.txt"
cat -n Matrix.txt
echo "cat -n MLFBQ.txt | head -60"
cat -n MLFBQ.txt | head -60
echo "cat -n MLFBQ.txt|tail -60"
cat -n MLFBQ.txt|tail -60
echo "cat -n MLFBQ.txt|tail -1400|head -60"
cat -n MLFBQ.txt|tail -1400|head -60
#The above same process repeated for tb+err
echo "java SYSTEM /home/opsys/SPR16/tb+err"
java SYSTEM /home/opsys/SPR16/tb+err
echo "cat -n ExecutionProfile.txt| head -40"
cat -n ExecutionProfile.txt| head -40
echo "ls"
ls
echo "cat -n ExecutionProfile.txt | tail -3000 |head -40"
cat -n ExecutionProfile.txt | tail -3000 |head -40
echo "ls"
ls
echo "cat -n ExecutionProfile.txt| tail -1400 |head -40"
cat -n ExecutionProfile.txt| tail -1400 |head -40
echo "ls"
ls
echo "cat -n ExecutionProfile.txt | tail -600|head -40"
cat -n ExecutionProfile.txt | tail -600|head -40
echo "ls"
ls
echo "cat -n ExecutionProfile.txt| tail -200 |head -40"
cat -n ExecutionProfile.txt| tail -200 |head -40
echo "ls"
ls
echo "cat -n ExecutionProfile.txt | tail -40"
cat -n ExecutionProfile.txt | tail -40
echo "cat -n Matrix.txt"
cat -n Matrix.txt
echo "cat -n MLFBQ.txt | head -60"
cat -n MLFBQ.txt | head -60
echo "cat -n MLFBQ.txt|tail -60"
cat -n MLFBQ.txt|tail -60
echo "cat -n MLFBQ.txt|tail -1400|head -60"
cat -n MLFBQ.txt|tail -1400|head -60
#Reads the software engineering report.
echo "cat -n swe_report.txt"
cat -n swe_report.txt
