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
package cern.c2mon.shared.common.datatag.address.impl;

import org.simpleframework.xml.Element;

import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.common.datatag.address.ENSHardwareAddress;

/**
 * Implementation of the ENSHardwareAddress interface.
 *
 * @author Jan Stowisek
 * @version $Revision: 1.4 $ ($Date: 2007/07/04 12:39:13 $ - $State: Exp $)
 * @see cern.c2mon.shared.common.datatag.address.ENSHardwareAddress
 */
 public class ENSHardwareAddressImpl  extends HardwareAddressImpl implements ENSHardwareAddress {

  private static final long serialVersionUID = -2040981497036089084L;
  /**
   * ENS address of the tag.
   */
  @Element
  protected String address = null;

  /**
   * ENS data type of the tag.
   */
  @Element(name = "data-type")
  protected String dataType = null;

  /**
   * Constructor for internal use
   * (for reading the HardwareAddress back from XML)
   */
  protected ENSHardwareAddressImpl() {
    // nothing to do
  }

  /**
   * Constructor for creating a fully initialised ENSHardwareAddress object.
   * @param pAddress
   * @param pDataType
   */
  public ENSHardwareAddressImpl(final String pAddress, final String pDataType) throws ConfigurationException {
    setAddress(pAddress);
    setDataType(pDataType);
  }

  @Override
  public final String getAddress() {
    return this.address;
  }

  @Override
  public final String getDataType() {
    return this.dataType;
  }

  protected final void setAddress(final String pAddress) throws ConfigurationException {
    if (pAddress == null) {
      throw new ConfigurationException(
          ConfigurationException.INVALID_PARAMETER_VALUE,
          "Parameter \"address\" must not be null for ENS HardwareAddress."
      );
    }
    if (pAddress.length() > 16) {
      throw new ConfigurationException(
          ConfigurationException.INVALID_PARAMETER_VALUE,
          "Parameter \"address\" can be max. 16 characters."
      );
    }
    this.address = pAddress;
  }

  protected final void setDataType(final String pDataType) throws ConfigurationException {
    if (pDataType == null) {
      throw new ConfigurationException(
          ConfigurationException.INVALID_PARAMETER_VALUE,
          "Parameter \"dataType\" must not be null for ENS HardwareAddress."
      );
    }
    if (!pDataType.equalsIgnoreCase(ENSHardwareAddress.TYPE_ANALOG) &&
        !pDataType.equalsIgnoreCase(ENSHardwareAddress.TYPE_DIGITAL) &&
        !pDataType.equalsIgnoreCase(ENSHardwareAddress.TYPE_COUNTER) &&
        !pDataType.equalsIgnoreCase(ENSHardwareAddress.TYPE_CTRL_SETPOINT) &&
        !pDataType.equalsIgnoreCase(ENSHardwareAddress.TYPE_CTRL_SIMPLE)
       ) {
      throw new ConfigurationException(
          ConfigurationException.INVALID_PARAMETER_VALUE,
          "Invalid value for parameter \"dataType\" : " + pDataType
      );
    }
    this.dataType = pDataType;
  }

  @Override
  public void validate() throws ConfigurationException {
    if (this.address == null) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"address\" must not be null");
    }

    if (this.dataType == null) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"dataType\" must not be null");
    }

    if (!this.dataType.equalsIgnoreCase(ENSHardwareAddress.TYPE_ANALOG) &&
        !this.dataType.equalsIgnoreCase(ENSHardwareAddress.TYPE_DIGITAL) &&
        !this.dataType.equalsIgnoreCase(ENSHardwareAddress.TYPE_COUNTER) &&
        !this.dataType.equalsIgnoreCase(ENSHardwareAddress.TYPE_CTRL_SETPOINT) &&
        !this.dataType.equalsIgnoreCase(ENSHardwareAddress.TYPE_CTRL_SIMPLE)
       ) {
         throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Invalid value for parameter \"dataType\" : " + this.dataType);
    }

  }

}