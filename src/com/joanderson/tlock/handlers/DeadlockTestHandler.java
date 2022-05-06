package com.joanderson.tlock.handlers;

import com.joanderson.tlock.deadlockTest.TestDeadlock;

import com.joanderson.tlock.model.*;

import jdk.jshell.Diag;

import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;

public class DeadlockTestHandler extends AbstractHandler {
	
	private YaoqiangXMLParser parser; 
	private ProgressBarInThread progressBar;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		
		if (!didAnyPreviousCodeGenerationOccourred()) {
			if (!PiAdlGenerationHandler.cancelWasPressedInFileDialog()) {
				MessageDialog.openError(
						window.getShell(),
						"Gere um arquivo pi-ADL primeiro",
					    "Erro: necessário utilizar o menu \"Tlock -> Gerar pi-ADL\" para a geração de um arquivo pi-ADL antes de testar a ocorrência de deadlocks no modelo."
				);	
			}	
			return null;
		}		 
		progressBar = new ProgressBarInThread();

		try {
			parser = YaoqiangXMLParser.getInstance();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			MessageDialog.openError(
					window.getShell(),
					"Um erro ocorreu",
				    "Aconteceu um erro ao instanciar o parser para o Yaoqiang. Tente novamente");
			progressBar.completeProgressBar();
			return null;
		}
		
		String generatedFolderPath = PiAdlGenerationHandler.savingPiADLPath;
		generatedFolderPath = generatedFolderPath.substring(0, generatedFolderPath.lastIndexOf("/"));
		generatedFolderPath = generatedFolderPath + "/src/" + getFileNameFromPath(PiAdlGenerationHandler.savingPiADLPath); 
		
		String result = testDeadlock(generatedFolderPath);
		
		MessageDialog.openInformation(
				window.getShell(),
				"Teste de ocorrência de deadlocks",
				result);
		progressBar.completeProgressBar();
		return null;
	}
	
	private boolean didAnyPreviousCodeGenerationOccourred() {
		return YaoqiangXMLParser.hasInstance();
	}

	private String getFileNameFromPath(String path) {
        String[] s = path.split("/");
        return s[s.length-1].split("[.]")[0];
    }
	
    public String testDeadlock(String generatedFolderPath) {
        
        ArrayList<String> result = TestDeadlock.performDeadlockTest(generatedFolderPath);
        if (result.size() == 1 && result.get(0).equals("-1")) {
        	progressBar.completeProgressBar();
            return "Erro: não foi possível obter retorno de teste de deadlock";
        }
        if (result.get(result.size() - 1).matches("deadlock.*")) {
            //test where deadlock occurred based on received list of Components and Connectors
            if (parser == null) {
            	progressBar.completeProgressBar();
                return result.get(result.size() - 1);
            } else {
                result.remove(result.size() - 1);
                String resultMessage = "";

                Component element = testExecuteResult(result);
                if (element == null) {
                	progressBar.completeProgressBar();
                    return "Nenhum deadlock foi detectado no modelo";
                }
            	progressBar.incrementProgressBar();
                final int TEST_AMOUNT = 15; 
                for (int i = 0; i < TEST_AMOUNT; i++) {
                    //tries again for making sure it's really a deadlock
                	progressBar.incrementProgressBar();
                    result = TestDeadlock.performDeadlockTest(generatedFolderPath);
                    if (result.size() == 1 && result.get(0).equals("-1")) {
                    	progressBar.completeProgressBar();
                        return "Erro: não foi possível obter retorno de teste de deadlock";
                    }
                    //removing not-an-id last element of result
                    result.remove(result.size() - 1);
                    element = testExecuteResult(result);
                    if (element == null) {
                    	progressBar.completeProgressBar();
                        return "Nenhum deadlock foi detectado no modelo";
                    }
                }
                progressBar.completeProgressBar();
                return "Um deadlock foi identificado no seguinte elemento: " + element.getOriginalName();
            }
        } else {
        	progressBar.completeProgressBar();
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
