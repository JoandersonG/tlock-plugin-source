package com.joanderson.tlock.handlers;

import com.joanderson.tlock.deadlockTest.TestDeadlock;
import com.joanderson.tlock.model.*;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class GenerateAndTestHandler extends AbstractHandler {
	
	private YaoqiangXMLParser parser;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		
		new PiAdlGenerationHandler().execute(event);
		//TODO: executar em outra thread e esperar resultado, certo?
		new DeadlockTestHandler().execute(event);
		
//		
//		
//		//pi-ADL generation
//		
//		String bpmnFilePath = openFile(window.getWorkbench().getDisplay());
//		
//		String contentForPiAdlFile = getPiADLFromBPMN(bpmnFilePath,getFileNameFromPath(bpmnFilePath));
//		
//		String projectName = getFileNameFromPath(bpmnFilePath);
//		IProject currentProject = createAndOpenNewProject(projectName);
//		PiAdlGenerationHandler.savingPiADLPath = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString() + "/" +  projectName  + "/" + projectName + ".piadl";
//		
//		createPiADLFile(PiAdlGenerationHandler.savingPiADLPath, contentForPiAdlFile);
//		
//		// refreshing project files
//		try {
//			currentProject.refreshLocal(IResource.DEPTH_INFINITE, null);
//		} catch (CoreException e) {
//			e.printStackTrace();
//		}
//		
//		openEditor(window.getActivePage(), getIFileFromPath(PiAdlGenerationHandler.savingPiADLPath));
//		System.out.println("Current directory: " + ResourcesPlugin.getWorkspace().getRoot().getLocation().toString());
//		
//		
//		
//		//Deadlock testing
//		try {
//			parser = YaoqiangXMLParser.getInstance();
//		} catch (ParserConfigurationException e) {
//			e.printStackTrace();
//			MessageDialog.openError(
//					window.getShell(),
//					"Um erro ocorreu",
//				    "Aconteceu um erro ao instanciar o parser para o Yaoqiang. Tente novamente");
//			return null;
//		}
//		
//		String generatedFolderPath = PiAdlGenerationHandler.savingPiADLPath;
//		generatedFolderPath = generatedFolderPath.substring(0, generatedFolderPath.lastIndexOf("/"));
//		generatedFolderPath = generatedFolderPath + "/src/" + getFileNameFromPath(PiAdlGenerationHandler.savingPiADLPath); 
//		
//		String result = testDeadlock(generatedFolderPath);
//		
//		MessageDialog.openInformation(
//				window.getShell(),
//				"Let's test deadlocks!",
//				result);
//		
//		
//		

		return null;
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
                
                Component element = testExecuteResult(result);
                if (element == null) {
                    return "Nenhum deadlock ocorreu, podes crer mesmo";
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
                        return "Nenhum deadlock ocorreu, podes crer mesmo!";
                    }
                }
                return "Um deadlock ocorreu, podes crer, e aconteceu em " + element.getOriginalName();
            }
        } else {
            //no deadlock occurred, just say that to user
            return "Nenhum deadlock ocorreu, podes crer";
        }
    }

    private Component testExecuteResult(ArrayList<String> result) {
        Component element = parser.getMoreAdvancedElementByListOfId(result);
        if (element instanceof EndEvent) {
            return null;
        }
        return element;
    }

		
	private String getFileNameFromPath(String path) {
        String[] s = path.split("/");
        return s[s.length-1].split("[.]")[0];
    }
	
	private IProject createAndOpenNewProject(String projectName) {
		try {
			IProgressMonitor progressMonitor = new NullProgressMonitor();
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IProject project = root.getProject(projectName);
			//find out if project already exists
			if (project.exists()) {
				project.open(progressMonitor);
			} else {
				project.create(progressMonitor);
				project.open(progressMonitor);
			}
			return project;
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private IFile getIFileFromPath(String filePath) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();    
		IPath location = Path.fromOSString(filePath); 
		IFile ifile = workspace.getRoot().getFileForLocation(location);
		return ifile;
	}
	
	private String getPiADLFromBPMN(String filePath, String fileName) {
        try {
            parser = YaoqiangXMLParser.getNewInstance();
            parser.parseBPMN(filePath);
            return parser.generatePiADL(fileName);
        } catch (Exception e) {
            return "Não foi possível gerar pi-ADL: não foi possível encontrar arquivo .bpmn informado";
        }

    }

	private void createPiADLFile(String path, String content) {
		try {//TODO: testar se não for um arquivo válido
            FileWriter arq = new FileWriter(path);
            PrintWriter gravarArq = new PrintWriter(arq);
            gravarArq.println(content);
            arq.close();
            System.out.println("Arquivo criado com sucesso");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Arquivo .bpmn não encontrado");
        }
	}
			
	private void openEditor(IWorkbenchPage page, IFile file) {
		try {
			IEditorDescriptor desc = PlatformUI.getWorkbench().
			        getEditorRegistry().getDefaultEditor(file.getName());
			page.openEditor(new FileEditorInput(file), desc.getId());
			
		} catch (Exception e) {
		
			e.printStackTrace();
		}
	}
	
	private String openFile(Display d) {
		Shell s = new Shell(d);
		FileDialog fd = new FileDialog(s, SWT.OPEN);
        fd.setText("Selecione o arquivo BPMN que deseja converter em pi-ADL");
        fd.setFilterPath(System.getProperty("user.home"));
        String[] filterExt = { "*.bpmn"};
        fd.setFilterExtensions(filterExt);
        return fd.open();
	}

}
