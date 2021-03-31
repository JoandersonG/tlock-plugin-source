package com.joanderson.tlock.deadlockTest;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TestDeadlock {

public static ArrayList<String> performDeadlockTest(String workingDirectory) {
    ArrayList<String> result = new ArrayList<>();
//        //  Copy files into scheduler directory
        try {
            copyFilesToSchedulerDirectory(
                    workingDirectory,
                    SchedulerCodeManager.getSchedulerCodeWithNoPrint(),
                    PlasmaInterfaceCodeManager.getPlasmaInterfaceCodeWithNoPrint()
            );
        } catch(IOException e) {
            //TODO: warn user of error
            result.add("-1");
            return result;
        }
        try {
            // Change permissions of compile program
            Process exec = Runtime.getRuntime().exec("chmod +x "+ workingDirectory + "/compile");
            exec.waitFor();

            //  Execute compile program
            ProcessBuilder builder = new ProcessBuilder("/bin/bash");
            Process p = null;
            try {
                p = builder.start();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            assert p != null;
            BufferedWriter pIn = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
            pIn.write("cd " + workingDirectory);
            pIn.newLine();
            pIn.flush();
            pIn.write("./compile");
            pIn.newLine();
            pIn.flush();
            try {
                pIn.write("exit");
                pIn.newLine();
                pIn.flush();
            }
            catch (IOException e) {
                System.out.println(e);
            }
            p.waitFor();

            //    Execute model internally looking for deadlock message
            String programName = getProgramName(workingDirectory);
            result.addAll(execProgramAsChildProcess(workingDirectory + "/./" + programName));

            //keep only component and connectors ids:
            result.replaceAll( (res) -> res = res.replaceFirst("0 rdv _", "").split("[.]")[0]);
            //remove duplicates
            ArrayList<String> aux = new ArrayList<>();
            for (String res : result) {
                if (! aux.contains(res)) {
                    aux.add(res);
                }
            }
            result = aux;

            System.out.println("Result:\n " + result);
            //    Copy regular scheduler files into scheduler directory
            copyFilesToSchedulerDirectory(
                    workingDirectory,
                    SchedulerCodeManager.getSchedulerCode(),
                    PlasmaInterfaceCodeManager.getPlasmaInterfaceCode()
            );
            //    Re-execute compile program
            execProgramAsChildProcess(workingDirectory + "/./" + "compile");
            return result;
        } catch (IOException | InterruptedException exception) {
            exception.printStackTrace();
            //TODO: warn user of error
            result.add("-1");
            return result;
        }
    }

    private static String getProgramName(String dirPath) {
        File dir = new File(dirPath);
        if (dir.listFiles() == null){
            return null;
        }
        for (File file : dir.listFiles()) {
            if (file.getName().matches(".*[.]go")) {
                return file.getName().split("[.]go")[0];
            }
        }
        return null;
    }

    private static ArrayList<String> execProgramAsChildProcess(String newProgram) throws IOException, InterruptedException {
        ArrayList<String> result = new ArrayList<>();
        Process exec = Runtime.getRuntime().exec(new String[] { newProgram, "" });
        exec.waitFor();
        InputStreamReader isr = new InputStreamReader(exec.getInputStream());
        BufferedReader reader = new BufferedReader(isr);
        System.out.println(exec.exitValue());
//        result.addAll(getComponentIdsFromExecResult(exec.exitValue()));
        String line = null;
        StringBuilder sb = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            if (line.matches("0 rdv .*")) {
                result.add(line);
            }
            if (line.matches("d.*") || line.matches("n.*")) {//TODO: deixar isso aqui?
                System.out.println(line);
                sb.append(line);
                result.add(line);
            }
        }
//        if (sb.toString().equals("")) {
//            return "Não foi detectado nenhum deadlock no modelo";
//        }
        isr.close();
        reader.close();
        return result;
//        return sb.toString();
    }

    private static void copyFilesToSchedulerDirectory(String workingDirectory, String schedulerCode, String plasmaInterfaceCode) throws IOException {

        //String workingDirectory = System.getProperty("user.dir");
        System.out.println("Current directory: " + workingDirectory);
        //get parent directory
        int index = workingDirectory.lastIndexOf("/");
        String parentPath = workingDirectory.substring(0, index);
        System.out.println("Parent directory: " + parentPath);
        String schedulerDirPath = parentPath + "/scheduler";
        System.out.println("Scheduler directory: " + schedulerDirPath);
        //save scheduler path
        String schedulerFilePath = schedulerDirPath + "/scheduler.go";
        System.out.println("Scheduler file path: " + schedulerFilePath);
        //save plasmaInterface path
        String plasmaInterfPath = schedulerDirPath + "/plasmaInterface.go";
        System.out.println("PlasmaInterface file path: " + plasmaInterfPath);
        // copy file scheduler.go
        FileWriter fileWriter = new FileWriter(schedulerFilePath);
        PrintWriter gravarArq = new PrintWriter(fileWriter);
        gravarArq.println(schedulerCode);
        fileWriter.close();
        //copy file plasmaInterface.go
        fileWriter = new FileWriter(plasmaInterfPath);
        gravarArq = new PrintWriter(fileWriter);
        gravarArq.println(plasmaInterfaceCode);
        fileWriter.close();
    }


}
