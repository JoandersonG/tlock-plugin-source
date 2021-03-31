package com.joanderson.tlock.handlers;

import com.joanderson.tlock.model.*;

import java.awt.Component;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileSystems;
import java.nio.file.Paths;

import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.xtext.ui.XtextProjectHelper;


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

import javax.swing.JFileChooser;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class PiAdlGenerationHandler extends AbstractHandler {
	
	public static String savingPiADLPath;
	private YaoqiangXMLParser parser;
    //private YaoqiangXMLParser parser;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		
//		MessageDialog.openInformation(
//				window.getShell(),
//				"Let's convert BPMN to Pi-ADL",
//				"Hello, Eclipse whole (entire) world");

		String bpmnFilePath = openFile(window.getWorkbench().getDisplay());
		
		String contentForPiAdlFile = getPiADLFromBPMN(bpmnFilePath,getFileNameFromPath(bpmnFilePath));
		
		String projectName = getFileNameFromPath(bpmnFilePath);
		IProject currentProject = createAndOpenNewProject(projectName);
		savingPiADLPath = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString() + "/" +  projectName  + "/" + projectName + ".piadl";
		
		createPiADLFile(savingPiADLPath, contentForPiAdlFile);
		
		// refreshing project files
		try {
			currentProject.refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		
		openEditor(window.getActivePage(), getIFileFromPath(savingPiADLPath));
		System.out.println("Current directory: " + ResourcesPlugin.getWorkspace().getRoot().getLocation().toString());

		return null;
	}
	
	private String getFileNameFromPath(String path) {
        String[] s = path.split("/");
        return s[s.length-1].split("[.]")[0];
    }
	
	private void refreshProjectFiles(IProject project) {
		try {
			project.refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
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
