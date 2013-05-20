/**
 * <copyright>
 * 
 * Copyright (c) 2010-2012 Thales Global Services S.A.S.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Thales Global Services S.A.S. - initial API and implementation
 * 
 * </copyright>
 */
package org.eclipse.emf.diffmerge.impl.policies;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.diffmerge.api.IMatchPolicy;
import org.eclipse.emf.diffmerge.api.scopes.IModelScope;
import org.eclipse.emf.diffmerge.util.ModelImplUtil;
import org.eclipse.emf.diffmerge.util.structures.ComparableSequence;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;


/**
 * A default match policy based on unique IDs (intrinsic/extrinsic/URI) of model elements.
 * @author Olivier Constant
 */
public class DefaultMatchPolicy implements IMatchPolicy {
  
  /**
   * Return the intrinsic ID of the given element as defined by its ID attribute, or null if none
   * @see EcoreUtil#getID(EObject)
   * @param element_p a potentially null element
   * @return a potentially null String
   */
  protected String getAttributeId(EObject element_p) {
    return ModelImplUtil.getEcoreId(element_p);
  }
  
  /**
   * Return the URI of the given element as a Comparable (String).
   * @param element_p a non-null element
   * @return a potentially null string
   */
  protected String getComparableUri(EObject element_p) {
    String result = null;
    URI uri = EcoreUtil.getURI(element_p);
    if (uri != null)
      result = uri.toString();
    return result;
  }
  
  /**
   * @see org.eclipse.emf.diffmerge.api.IMatchPolicy#getMatchId(org.eclipse.emf.ecore.EObject, org.eclipse.emf.diffmerge.api.scopes.IModelScope)
   */
  public Comparable<?> getMatchId(EObject element_p, IModelScope scope_p) {
    Comparable<?> result = getAttributeId(element_p);
    if (result == null)
      result = getXmlId(element_p);
    if (result == null)
      result = getComparableUri(element_p);
    return result;
  }
  
  /**
   * Return the name of the given element, if any
   * @param element_p a non-null element
   * @return a potentially null string
   */
  protected String getName(EObject element_p) {
    // Redefine to use qualified names
    return null;
  }
  
  /**
   * Return the qualified name of the given element in the context of the given scope, if any.
   * A qualified name requires all in-scope containers of the element and the element itself
   * to have a name.
   * @param element_p a non-null element
   * @param scope_p a non-null scope to which the element belongs
   * @return a potentially null string representing the qualified name
   */
  protected ComparableSequence<String> getQualifiedName(EObject element_p,
      IModelScope scope_p) {
    ComparableSequence<String> result = new ComparableSequence<String>();
    EObject current = element_p;
    while (current != null) {
      String name = getName(current);
      if (name == null)
        return null;
      result.prepend(name);
      current = scope_p.getContainer(current);
    }
    return result;
  }
  
  /**
   * Return the extrinsic (XML) ID of the given element, or null if none
   * @param element_p a potentially null element
   * @return a potentially null String
   */
  protected String getXmlId(EObject element_p) {
    return ModelImplUtil.getXmlId(element_p);
  }
  
}
