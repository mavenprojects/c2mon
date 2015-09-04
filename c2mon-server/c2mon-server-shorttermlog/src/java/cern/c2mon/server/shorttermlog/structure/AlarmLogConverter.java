/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 *
 * Copyright (C) 2008  CERN
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/

package cern.c2mon.server.shorttermlog.structure;

import cern.c2mon.server.common.alarm.Alarm;

/**
 * This class is in charge of all objects transformations that may involved the
 * DataTagShortTermLog class It is aware of the DataShortTermLog java bean
 * structure and knows how its information has to be transfered into/from other
 * objects
 * 
 * @author Felix Ehm
 * 
 */
public final class AlarmLogConverter implements LoggerConverter<Alarm> {
    
    @Override
    public Loggable convertToLogged(Alarm alarm) {
      AlarmLog alarmLog = new AlarmLog();
      
      alarmLog.setTagId(alarm.getTagId());
      alarmLog.setAlarmId(alarm.getId());
      
      alarmLog.setActive(alarm.isActive());
      
      alarmLog.setFaultFamily(alarm.getFaultFamily());
      alarmLog.setFaultMember(alarm.getFaultMember());
      alarmLog.setFaultCode(alarm.getFaultCode());
      
      alarmLog.setServerTimestamp(alarm.getTimestamp());
      
      alarmLog.setInfo(alarm.getInfo());
      return alarmLog;
      
    }

}