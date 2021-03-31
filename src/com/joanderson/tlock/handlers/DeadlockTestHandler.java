package com.joanderson.tlock.handlers;

import com.joanderson.tlock.deadlockTest.TestDeadlock;

import com.joanderson.tlock.model.*;

import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.jface.dialogs.MessageDialog;

public class DeadlockTestHandler extends AbstractHandler {
	
	private YaoqiangXMLParser parser; 

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		
		try {
			parser = YaoqiangXMLParser.getInstance();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			MessageDialog.openError(
					window.getShell(),
					"Um erro ocorreu",
				    "Aconteceu um erro ao instanciar o parser para o Yaoqiang. Tente novamente");
			return null;
		}
		
		String generatedFolderPath = SampleHandler.savingPiADLPath;
		generatedFolderPath = generatedFolderPath.substring(0, generatedFolderPath.lastIndexOf("/"));
		generatedFolderPath = generatedFolderPath + "/src/" + getFileNameFromPath(SampleHandler.savingPiADLPath); 
		
		String result = testDeadlock(generatedFolderPath);
		
		MessageDialog.openInformation(
				window.getShell(),
				"Teste de ocorrência de deadlocks",
				result);

		return null;
	}
	
	private String getFileNameFromPath(String path) {
        String[] s = path.split("/");
        return s[s.length-1].split("[.]")[0];
    }
	
    public String testDeadlock(String generatedFolderPath) {
        
        //String workingDirectory = tfDeadlockPath.getText().substring(0, tfDeadlockPath.getText().lastIndexOf("/"));
    	
        ArrayList<String> result = TestDeadlock.performDeadlockTest(generatedFolderPath);
        if (result.size() == 1 && result.get(0).equals("-1")) {
            return "Erro: não foi possível obter retorno de teste de deadlock";
        }
        if (result.get(result.size() - 1).matches("deadlock.*")) {
            //test where deadlock occurred based on received list of Components and Connectors
            if (parser == null) {
                return result.get(result.size() - 1);
            } else {
                result.remove(result.size() - 1);
                String resultMessage = "";

                Component element = testExecuteResult(result);
                if (element == null) {
                    return "Nenhum deadlock foi detectado no modelo";
                }
                for (int i = 0; i < 15; i++) {
                    //tries again for making sure it's really a deadlock
                    result = TestDeadlock.performDeadlockTest(generatedFolderPath);
                    if (result.size() == 1 && result.get(0).equals("-1")) {
                        return "Erro: não foi possível obter retorno de teste de deadlock";
                    }
                    //removing not-an-id last element of result
                    result.remove(result.size() - 1);
                    element = testExecuteResult(result);
                    if (element == null) {
                        return "Nenhum deadlock foi detectado no modelo";
                    }
                }
                return "Um deadlock foi identificado no seguinte elemento: " + element.getOriginalName();
            }
        } else {
            //no deadlock occurred, just say that to user
            return "Nenhum deadlock foi detectado no modelo";
        }
    }

    private Component testExecuteResult(ArrayList<String> result) {
        Component element = parser.getMoreAdvancedElementByListOfId(result);
        if (element instanceof EndEvent) {
            return null;
        }
        return element;
    }

	
}
