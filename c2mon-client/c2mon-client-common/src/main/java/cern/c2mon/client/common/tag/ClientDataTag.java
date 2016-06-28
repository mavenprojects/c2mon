/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 * 
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * 
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.client.common.tag;

import cern.c2mon.client.common.listener.TagUpdateListener;
import cern.c2mon.shared.client.tag.TagUpdate;
import cern.c2mon.shared.client.tag.TagValueUpdate;
import cern.c2mon.shared.rule.RuleFormatException;

/**
 * This interface extends the <code>ClientDataTagValue</code> interface 
 * and provides all methods which are necessary to update a
 * <code>ClientDataTag</code> object. This interface  shall only
 * be used within the C2MON client API. In other words no classes outside
 * of the C2MON client API should make directly changes on a
 * <code>ClientDataTag</code> object.
 *
 * @deprecated Got replaced by {@link Tag}
 * @author Matthias Braeger
 */
@Deprecated
public interface ClientDataTag extends ClientDataTagValue, TagUpdateListener, Cloneable {
  
  /**
   * This thread safe method updates the given <code>ClientDataTag</code> object.
   * It copies every single field of the <code>TransferTag</code> object and notifies
   * then the registered listener about the update by providing a copy of the
   * <code>ClientDataTag</code> object.
   * <p>
   * Please note that the <code>ClientDataTag</code> gets only updated, if the tag id's
   * matches and if the server time stamp of the update is older thatn the current time
   * stamp set.
   * 
   * @param tagUpdate The object that contains the updates.
   * @return <code>true</code>, if the update was successful, otherwise
   *         <code>false</code>
   * @throws RuleFormatException In case that the <code>TransferTag</code>
   *         parameter contains a invalid rule expression.
   */
  boolean update(final TagUpdate tagUpdate) throws RuleFormatException;
  
  
  /**
   * This thread safe method updates the given <code>ClientDataTag</code> object.
   * It copies every single field of the <code>TransferTagValue</code> object and notifies
   * then the registered listener about the update by providing a copy of the
   * <code>ClientDataTag</code> object.
   * <p>
   * Please note that the <code>ClientDataTag</code> gets only updated, if the tag id's
   * matches and if the server time stamp of the update is older than the current time
   * stamp set.
   * 
   * @param transferTag The object that contains the updates.
   * @return <code>true</code>, if the update was successful, otherwise
   *         <code>false</code>
   */
  boolean update(final TagValueUpdate tagValueUpdate);

  
//  /**
//   * This thread safe method updates the accessible state of the given
//   * <code>ClientDataTag</code> object. Once the accessibility has been updated
//   * it notifies the registered listener about the update by providing a copy of
//   * the <code>ClientDataTag</code> object.
//   * 
//   * @param supervisionEvent The supervision event which contains the current
//   *                         status of the process or the equipment.
//   * @return <code>true</code>, if the update was successful. The returning value
//   *         is <code>false</code>, if the supervision event is <code>null</code>
//   *         or the tag is not linked to the given equipment or process.
//   */
//  boolean update(SupervisionEvent supervisionEvent);
  
  
  /**
   * Creates a clone of the this object. The only difference is that
   * it does not copy the registered listeners. If you are only interested
   * in the static information of the object you should call after cloning
   * the {@link #clean()} method.<br>
   * Please note that this method won't notify the registered listeners!
   * @return The clone of this object
   * @throws CloneNotSupportedException Thrown, if one of the field does not support cloning.
   * @see #clean()
   */
  ClientDataTag clone() throws CloneNotSupportedException;


  /**
   * Removes all <code>ClientDataTagValue</code> information from the object.
   * This is in particular interesting for the history mode which sometimes just
   * uses the static information from the live tag object. 
   */
  void clean();
}