package com.joanderson.tlock.handlers;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;

public class ProgressBarInThread {

	private static Shell        shell;
	private static ProgressBar  progressBar;
	
	public ProgressBarInThread()
	{
	    Display display = Display.getDefault();
	    shell = new Shell(SWT.SHELL_TRIM);
	    shell.setText("Tlock");
	    shell.setLayout(new GridLayout(1, false));
	
	    setUpStatusBar();
	
	    runProgressBar();
	
	    shell.pack();
	    shell.setSize(500, 80);
	    
	    Rectangle screenSize = display.getPrimaryMonitor().getBounds();
	    shell.setLocation((screenSize.width - shell.getBounds().width) / 2, (screenSize.height - shell.getBounds().height) / 2);
	    
	    shell.open();
	
	}
	
	public void completeProgressBar() {
		progressBar.setSelection(progressBar.getMaximum());
	}
	
	public void incrementProgressBar() {
		incrementProgressBar(1);
	}
	
	public void incrementProgressBar(int value) {
		if (value < 0 || progressBar.isDisposed()) return;
		
		progressBar.setSelection(progressBar.getSelection() + value);
	}
	
	private void finishProgressBar() {
		progressBar.dispose();
		shell.close();
	}
	
	private void runProgressBar() {
	    new Thread(new Runnable() {
	        @Override
	        public void run() {
	            while (!progressBar.isDisposed()) {
	                Display.getDefault().asyncExec(new Runnable() {
	                    @Override
	                    public void run() {
	                        if (progressBar.getSelection() >= progressBar.getMaximum()) {
	                        	//time to finish thread and execution
	                        	finishProgressBar();
	                        }
	                    }
	                });	
	                try {
	                    Thread.sleep(1000);
	                }
	                catch (InterruptedException e) {
	                    e.printStackTrace();
	                }
	            }
	        }	        
	    }).start();
	}
	
	private static void setUpStatusBar()
	{
	    Composite statusBar = new Composite(shell, SWT.BORDER);
	    statusBar.setLayout(new GridLayout(2, false));
	    statusBar.setLayoutData(new GridData(SWT.FILL, SWT.END, true, false));
	
	    progressBar = new ProgressBar(statusBar, SWT.SMOOTH);
	    progressBar.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false));
	    progressBar.setMaximum(16);
	
	    Label status = new Label(statusBar, SWT.NONE);
	    status.setText("Testando ocorrência de deadlocks...");
	    status.setLayoutData(new GridData(SWT.END, SWT.CENTER, true, false));
	}
}