/**
 * Copyright (c) 2015-2016 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephane Bouchet (Intel Corporation) - initial API and implementation
 *    Olivier Constant (Thales Global Services) - tight integration
 */
package org.eclipse.emf.diffmerge.connector.core.ext;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.CompareUI;
import org.eclipse.compare.INavigatable;
import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.emf.diffmerge.api.Role;
import org.eclipse.emf.diffmerge.connector.core.EMFDiffMergeCoreConnectorPlugin;
import org.eclipse.emf.diffmerge.connector.core.Messages;
import org.eclipse.emf.diffmerge.ui.EMFDiffMergeUIPlugin;
import org.eclipse.emf.diffmerge.ui.setup.ComparisonSetup;
import org.eclipse.emf.diffmerge.ui.setup.ComparisonSetupManager;
import org.eclipse.emf.diffmerge.ui.setup.EMFDiffMergeEditorInput;
import org.eclipse.emf.diffmerge.ui.viewers.AbstractComparisonViewer;
import org.eclipse.emf.diffmerge.ui.viewers.EMFDiffNode;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;


/**
 * A viewer that can convert ICompareInput to model comparison inputs in Team usage scenarios.
 * It wraps a comparison 
 */
public class TeamComparisonViewer extends Viewer {
  
  /** The non-null control of the viewer */
  protected final Composite _control;
  
  /** The initially null actual comparison viewer */
  protected AbstractComparisonViewer _innerViewer;
  
  /** The potentially null input of this viewer */
  protected Object _input;
  
  
  /**
   * Constructor
   * @param parent_p a non-null composite
   */
  public TeamComparisonViewer(Composite parent_p) {
    _control = createControl(parent_p);
    _innerViewer = null;
    _input = null;
  }
  
  /**
   * Close the active editor without saving
   */
  protected void closeEditor() {
    IWorkbenchWindow activeWorkbenchWindow =
        EMFDiffMergeUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow();
    if (activeWorkbenchWindow != null) {
      IWorkbenchPage page = activeWorkbenchWindow.getActivePage();
      if (page != null) {
        IEditorPart editor = page.getActiveEditor();
        if (editor != null)
          page.closeEditor(editor, false);
      }
    }
  }
  
  /**
   * Create and return the control of this viewer in the context of the given parent composite
   * @param parent_p a non-null composite
   * @return a non-null control
   */
  protected Composite createControl(Composite parent_p) {
    Composite result = new Composite(parent_p, SWT.NONE);
    // For switching pane combo label, see CompareContentViewerSwitchingPane#setText(String)
    result.setData(CompareUI.COMPARE_VIEWER_TITLE,
        EMFDiffMergeCoreConnectorPlugin.getDefault().getViewerLabel());
    GridLayout layout = new GridLayout(1, true);
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    result.setLayout(layout);
    result.addDisposeListener(new DisposeListener() {
      /**
       * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
       */
      public void widgetDisposed(DisposeEvent e_p) {
        handleDispose();
      }
    });
    registerNavigatable(result);
    return result;
  }
  
  /**
   * @see org.eclipse.jface.viewers.Viewer#getControl()
   */
  @Override
  public Control getControl() {
    return _control;
  }
  
  /**
   * @see org.eclipse.jface.viewers.Viewer#getInput()
   */
  @Override
  public Object getInput() {
    return _input;
  }
  
  /**
   * @see org.eclipse.jface.viewers.Viewer#getSelection()
   */
  @Override
  public ISelection getSelection() {
    return (_innerViewer == null)? null: _innerViewer.getSelection();
  }
  
  /**
   * Return the shell of the graphical context
   * @return a non-null shell
   */
  protected Shell getShell() {
    return _control.getShell();
  }
  
  /**
   * Dispose this viewer as a reaction to the disposal of its control
   */
  protected void handleDispose() {
    _innerViewer = null;
    _input = null;
  }
  
  /**
   * @see org.eclipse.jface.viewers.Viewer#refresh()
   */
  @Override
  public void refresh() {
    if (_innerViewer != null)
      _innerViewer.refresh();
  }
  
  /**
   * Register a navigatable for navigation from the workbench toolbar buttons
   * @param control_p the non-null control of this viewer
   */
  protected void registerNavigatable(Composite control_p) {
    INavigatable navigatable = new INavigatable() {
      /**
       * Return the delegate navigatable, if any
       * @return a potentially null object
       */
      protected INavigatable getDelegate() {
        INavigatable navResult = null;
        if (_innerViewer != null)
          navResult = _innerViewer.getNavigatable();
        return navResult;
      }
      /**
       * @see org.eclipse.compare.INavigatable#getInput()
       */
      public Object getInput() {
        return TeamComparisonViewer.this.getInput();
      }
      /**
       * @see org.eclipse.compare.INavigatable#hasChange(int)
       */
      public boolean hasChange(int changeFlag_p) {
        boolean result = false;
        INavigatable delegate = getDelegate();
        if (delegate != null)
          result = delegate.hasChange(changeFlag_p);
        return result;
      }
      /**
       * @see org.eclipse.compare.INavigatable#openSelectedChange()
       */
      public boolean openSelectedChange() {
        boolean result = false;
        INavigatable delegate = getDelegate();
        if (delegate != null)
          result = delegate.openSelectedChange();
        return result;
      }
      /**
       * @see org.eclipse.compare.INavigatable#selectChange(int)
       */
      public boolean selectChange(int changeFlag_p) {
        boolean result = false;
        INavigatable delegate = getDelegate();
        if (delegate != null)
          result = delegate.selectChange(changeFlag_p);
        return result;
      }
    };
    control_p.setData(INavigatable.NAVIGATOR_PROPERTY, navigatable);
  }
  
  /**
   * @see org.eclipse.jface.viewers.Viewer#setInput(java.lang.Object)
   */
  @Override
  public void setInput(Object input_p) {
    if (input_p == _input)
      return;
    _input = input_p;
    if (input_p instanceof EMFDiffNode || input_p == null) {
      // Can be directly handled by inner viewer
      if (_innerViewer != null)
        _innerViewer.setInput(input_p);
    } else if (input_p instanceof ICompareInput) {
      // Requires preliminary work
      Object left = ((ICompareInput) input_p).getLeft();
      Object right = ((ICompareInput) input_p).getRight();
      Object ancestor = ((ICompareInput) input_p).getAncestor();
      // Prompt user for comparison method
      ComparisonSetupManager manager = EMFDiffMergeUIPlugin.getDefault().getSetupManager();
      ComparisonSetup setup = manager.createComparisonSetup(left, right, ancestor);
      if (setup != null) {
        setup.setTwoWayReferenceRole(Role.REFERENCE);
        setup.setCanChangeTwoWayReferenceRole(false);
        setup.setCanSwapScopeDefinitions(false);
      }
      EMFDiffMergeEditorInput editorInput = manager.createEditorInputWithUI(getShell(), setup);
      if (editorInput != null) {
        // Not failed/cancelled
        try {
          // Compute comparison
          ProgressMonitorDialog dialog = new ProgressMonitorDialog(getShell());
          dialog.run(true, true, editorInput);
        } catch (InvocationTargetException e) {
          EMFDiffMergeCoreConnectorPlugin.getDefault().logError(e);
        } catch (InterruptedException e) {
          EMFDiffMergeCoreConnectorPlugin.getDefault().logError(e);
        }
        EMFDiffNode compareResult = editorInput.getCompareResult();
        if (compareResult != null) {
          // Success: create the viewer and set the input
          Control contents = editorInput.createContents(_control);
          GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
          contents.setLayoutData(layoutData);
          _innerViewer = editorInput.getViewer();
          _control.pack();
          _control.getParent().layout();
        } else {
          // Failure (no diff): close the viewer and open a dialog
          _innerViewer = null;
          String message = editorInput.getMessage();
          if (message == null || message.length() == 0) {
            MessageDialog.openInformation(getShell(),
                Messages.TeamComparisonViewer_NoDiff_Title,
                Messages.TeamComparisonViewer_NoDiff_Message);
          } else {
            MessageDialog.openError(
                getShell(), Messages.TeamComparisonViewer_NoDiff_Title, message);
          }
          closeEditor();
        }
      }
    }
  }
  
  /**
   * @see org.eclipse.jface.viewers.Viewer#setSelection(org.eclipse.jface.viewers.ISelection, boolean)
   */
  @Override
  public void setSelection(ISelection selection_p, boolean reveal_p) {
    if (_innerViewer != null)
      _innerViewer.setSelection(selection_p, reveal_p);
  }
  
}