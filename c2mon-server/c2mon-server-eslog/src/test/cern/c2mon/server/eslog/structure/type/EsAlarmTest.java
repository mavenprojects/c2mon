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
package cern.c2mon.server.eslog.structure.type;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import cern.c2mon.server.eslog.structure.types.EsAlarm;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

/**
 * @author Alban Marguet
 */
public class EsAlarmTest {
  private Gson gson = new GsonBuilder().create();
  private EsAlarm EsAlarm;
  private String expectedJson;
  private long tagId = 1;
  private long alarmId = 2;

  private String faultFamily = "family";
  private String faultMember = "member";
  private int faultCode = 0;

  private boolean active = true;
  private String activity = "active";
  private double activeNumeric = 1;
  private int priority = 1;
  private String info = "info";

  private long serverTimestamp = 0;
  private Map<String, String> metadata = new HashMap<>();

  @Before
  public void setup() {
    metadata.put("test1", "value1");
    metadata.put("test2", "value2");

    EsAlarm = new EsAlarm();
    EsAlarm.setAlarmId(alarmId);
    EsAlarm.setTagId(tagId);
    EsAlarm.setFaultFamily(faultFamily);
    EsAlarm.setFaultMember(faultMember);
    EsAlarm.setFaultCode(faultCode);
    EsAlarm.setActive(active);
    EsAlarm.setActivity(activity);
    EsAlarm.setActiveNumeric(activeNumeric);
    EsAlarm.setPriority(priority);
    EsAlarm.setInfo(info);
    EsAlarm.setServerTimestamp(serverTimestamp);
    EsAlarm.setMetadata(metadata);
    JsonObject element = gson.toJsonTree(new Object()).getAsJsonObject();
    element.addProperty("tagId", tagId);
    element.addProperty("alarmId", alarmId);
    element.addProperty("faultFamily", faultFamily);
    element.addProperty("faultMember", faultMember);
    element.addProperty("faultCode", faultCode);
    element.addProperty("active", active);
    element.addProperty("activity", activity);
    element.addProperty("activeNumeric", activeNumeric);
    element.addProperty("priority", priority);
    element.addProperty("info", info);
    element.addProperty("serverTimestamp", serverTimestamp);
    for (String key : metadata.keySet()) {
      element.addProperty(key, metadata.get(key));
    }
    expectedJson = gson.toJson(element);
  }

  @Test
  public void testJsonSerialization() {
    String json = EsAlarm.toString();
    try {
      assertEquals(expectedJson, EsAlarm.toString());
      assertEquals(EsAlarm, EsAlarm.getObject(expectedJson));
    }
    catch (Exception e) {
      fail("Should be able to serialize/deserialize JSON");
    }
  }
}