/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2011 CERN.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.server.configuration.handler.impl;

import java.util.ArrayList;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import cern.c2mon.server.configuration.ConfigurationLoader;
import cern.c2mon.server.configuration.handler.ControlTagConfigHandler;
import cern.c2mon.server.configuration.handler.EquipmentConfigHandler;
import cern.c2mon.server.configuration.handler.ProcessConfigHandler;
import cern.c2mon.server.configuration.impl.ProcessChange;
import cern.tim.server.cache.ProcessCache;
import cern.tim.server.cache.ProcessFacade;
import cern.tim.server.cache.loading.ProcessDAO;
import cern.tim.server.common.process.Process;
import cern.tim.server.daqcommunication.in.JmsContainerManager;
import cern.tim.shared.client.configuration.ConfigurationElement;
import cern.tim.shared.client.configuration.ConfigurationElementReport;
import cern.tim.shared.client.configuration.ConfigConstants.Action;
import cern.tim.shared.client.configuration.ConfigConstants.Entity;
import cern.tim.shared.common.ConfigurationException;
import cern.tim.shared.daq.config.Change;

/**
 * See interface documentation.
 * 
 * @author Mark Brightwell
 *
 */
public class ProcessConfigHandlerImpl implements ProcessConfigHandler {

  /**
   * Class logger.
   */
  private static final Logger LOGGER = Logger.getLogger(ProcessConfigHandlerImpl.class);
  
  /**
   * Reference to facade.
   */
  private ProcessFacade processFacade;
  
  /**
   * Reference to cache.
   */
  private ProcessCache processCache;
  
  /**
   * Reference to DAO.
   */
  private ProcessDAO processDAO;
  
  /**
   * Reference to Equipment configuration bean.
   */
  @Autowired
  private EquipmentConfigHandler equipmentConfigHandler;
  
  /**
   * Reference to ControlTag configuration bean.
   */
  private ControlTagConfigHandler controlTagConfigHandler;
  
  /**
   * Reference to the bean managing DAQ-in JMS connections.
   */
  private JmsContainerManager jmsContainerManager;
  
  private boolean allowRunningProcessRemoval = false;
    
  /**
   * Autowired constructor.
   * @param processFacade the facade bean
   * @param processCache the cache bean
   * @param processDAO the DAO bean
   * @param equipmentConfigHandler the Equipment configuration bean
   * @param controlTagConfigHandler the ControlTag configuration bean
   * @param jmsContainerManager JmsContainerManager bean
   */
  @Autowired
  public ProcessConfigHandlerImpl(final ProcessFacade processFacade, final ProcessCache processCache, 
                              final ProcessDAO processDAO, final ControlTagConfigHandler controlTagConfigHandler, 
                              final JmsContainerManager jmsContainerManager) {
    super();
    this.processFacade = processFacade;
    this.processCache = processCache;
    this.processDAO = processDAO;    
    this.controlTagConfigHandler = controlTagConfigHandler;
    this.jmsContainerManager = jmsContainerManager;
    
  }

  /**
   * Creates the process and inserts it into the cache and DB (DB first).
   * 
   * <p>Changing a process id is not currently allowed.
   * 
   * @param element the configuration element
   * @throws IllegalAccessException not thrown (inherited from common facade interface) 
   */
  @Transactional("cacheTransactionManager")
  public ProcessChange createProcess(final ConfigurationElement element) throws IllegalAccessException {
    LOGGER.debug("Creating process with id " + element.getEntityId());
    if (processCache.hasKey(element.getEntityId())) {
      throw new ConfigurationException(ConfigurationException.ENTITY_EXISTS, 
          "Attempting to create a process with an already existing id: " + element.getEntityId());
    }
    Process process = (Process) processFacade.createCacheObject(element.getEntityId(), element.getElementProperties());
    try {
      processDAO.insert(process);
      processCache.putQuiet(process);
      //load & status into cache
      processFacade.loadAndStartAliveTag(process.getId());
      jmsContainerManager.subscribe(process);
      return new ProcessChange(process.getId());
    } catch (RuntimeException ex) {
      LOGGER.error("Exception caught while creating a new Process - rolling back DB changes and removing from cache.");
      processCache.remove(process.getId());
      jmsContainerManager.unsubscribe(process);
      throw new UnexpectedRollbackException("Unexpected error while creating a new Process.", ex);
    }
          
  }
  
  /**
   * No changes to the Process configuration are currently passed to the DAQ layer,
   * but the Configuration object is already build into the logic below (always empty
   * and hence ignored in the {@link ConfigurationLoader}).
   * @param id
   * @param properties
   * @return change requiring DAQ reboot, but not to be sent to the DAQ layer (not supported)
   * @throws IllegalAccessException
   */
  @Transactional("cacheTransactionManager")
  public ProcessChange updateProcess(final Long id, final Properties properties) throws IllegalAccessException {    
    if (properties.containsKey("id")) {
      throw new ConfigurationException(ConfigurationException.UNDEFINED, 
          "Attempting to change the process id - this is not currently supported!");
    }
    if (properties.containsKey("name")) {
      throw new ConfigurationException(ConfigurationException.UNDEFINED, 
          "Attempting to change the process name - this is not currently supported!");
    }
    boolean aliveConfigure = false;
    if (properties.containsKey("aliveInterval") || properties.containsKey("aliveTagId")) {
      aliveConfigure = true;
    }
    Change processUpdate;
    Process process = processCache.get(id);    
    try {
      process.getWriteLock().lock();
      if (aliveConfigure){
        processFacade.removeAliveTimer(process.getId());
      }
      processUpdate = processFacade.updateConfig(process, properties); //return always empty
      processDAO.updateConfig(process);
      process.getWriteLock().unlock();
      if (aliveConfigure){
        processFacade.loadAndStartAliveTag(process.getId());
      }
    } catch (RuntimeException e) {
      LOGGER.error("Exception caught while updating a new Process - rolling back DB changes and removing from cache.");
      processCache.remove(id);
      throw new UnexpectedRollbackException("Unexpected exception caught while updating a Process configuration.", e);      
    } finally {
      if (process.getWriteLock().isHeldByCurrentThread()) {
        process.getWriteLock().unlock();     
      }      
    } 
    return new ProcessChange(process.getId());    
  }
  
  /**
   * Tries to remove the process and all its descendents. The process
   * itself is only completely removed if all the equipments, subequipments
   * and associated tags, commands are all removed successfully.
   * 
   * <p>In the case of a failure, the removal is interrupted and the process
   * remains with whatever child objects remain at the point of failure (for tags
   * this is not quite exact: the server will attempt to remove all tags, but will
   * not remove an equipment or any associated subequipments if one tag fails to
   * be removed).
   * @param processId id of process
   * @param processReport the element report for the removal of the process, to which 
   *                          subreports can be attached
   */
  @Override
  public ProcessChange removeProcess(final Long processId, final ConfigurationElementReport processReport) {   
    ProcessChange change = doRemoveProcess(processId, processReport);
    processCache.remove(processId);    
    return change;
  }
      
  @Transactional(value = "cacheTransactionManager", propagation=Propagation.REQUIRES_NEW)
  public ProcessChange doRemoveProcess(final Long processId, final ConfigurationElementReport processReport) {    
    LOGGER.debug("Removing process with id " + processId);
    if (processCache.hasKey(processId)) {
      Process process = processCache.get(processId);
      try {        
        process.getWriteLock().lock();      
        if (processFacade.isRunning(process) && !allowRunningProcessRemoval) {
          String message = "Unable to remove Process " + process.getName() + " as currently running - please stop it first.";
          LOGGER.warn(message); 
          processReport.setFailure(message);
         } else {
          //remove all associated equipment from system   
          for (Long equipmentId : new ArrayList<Long>(process.getEquipmentIds())) {
            ConfigurationElementReport childElementReport = new ConfigurationElementReport(Action.REMOVE, Entity.EQUIPMENT, equipmentId);
            try {        
              processReport.addSubReport(childElementReport);
              equipmentConfigHandler.removeEquipment(equipmentId, childElementReport);
            } catch (RuntimeException ex) {
              LOGGER.error("Exception caught while applying the configuration change (Action, Entity, Entity id) = (" 
                  + Action.REMOVE + "; " + Entity.EQUIPMENT + "; " + equipmentId + ")", ex);
              childElementReport.setFailure("Exception caught while applying the configuration change.", ex);          
              throw new UnexpectedRollbackException("Unexpected exception caught while removing an Equipment.", ex);
            }      
          }          
          //remove process from cache and DB
          processDAO.deleteProcess(processId);             
          removeProcessControlTags(process, processReport);
          process.getWriteLock().unlock();
          //remove alive out of lock (in fact no longer necessary); always after removing control tags, or could be pulled back in from DB to cache
          processFacade.removeAliveTimer(processId);
          jmsContainerManager.unsubscribe(process);            
         }
        return new ProcessChange();
      } catch (RuntimeException ex) {                  
        LOGGER.error("Exception caught when attempting to remove a process - rolling back DB changes.", ex);
        processCache.remove(processId);
        throw new UnexpectedRollbackException("Unexpected exception caught while removing Process.", ex);
      } finally {
        if (process.getWriteLock().isHeldByCurrentThread()) {
          process.getWriteLock().unlock();
        }        
      } 
    } else {
      LOGGER.debug("Process not found in cache - unable to remove it.");
      processReport.setWarning("Process not found in cache so cannot be removed.");
      return new ProcessChange();
    }     
  }

  /**
   * Removes process alive and state tags (from DB and cache).
   * @param process
   * @param processReport
   */
  private void removeProcessControlTags(Process process, ConfigurationElementReport processReport) {
    LOGGER.debug("Removing Process control tags for process " + process.getId());     
    Long aliveTagId = process.getAliveTagId();
    if (aliveTagId != null) {
      ConfigurationElementReport tagReport = new ConfigurationElementReport(Action.REMOVE, Entity.CONTROLTAG, aliveTagId);
      controlTagConfigHandler.removeControlTag(aliveTagId, tagReport);
      processReport.addSubReport(tagReport);
    }          
    Long stateTagId = process.getStateTagId();
    ConfigurationElementReport tagReport = new ConfigurationElementReport(Action.REMOVE, Entity.CONTROLTAG, stateTagId);
    controlTagConfigHandler.removeControlTag(stateTagId, tagReport);
    processReport.addSubReport(tagReport);  
  }

  @Override
  public void removeEquipmentFromProcess(Long equipmentId, Long processId) {
    LOGGER.debug("Removing Process Equipments for process " + processId);
    Process process = processCache.get(processId);
    process.getWriteLock().lock();
    try {
      process.getEquipmentIds().remove(equipmentId);
    } catch (RuntimeException e) {
      throw new UnexpectedRollbackException("Unable to remove equipment reference in process.", e);
    } finally {
      process.getWriteLock().unlock();
    } 
  }

  /**
   * @param allowRunningProcessRemoval the allowRunningProcessRemoval to set
   */
  public void setAllowRunningProcessRemoval(boolean allowRunningProcessRemoval) {
    this.allowRunningProcessRemoval = allowRunningProcessRemoval;
  }
}
