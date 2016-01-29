/*
 * Copyright 2013-2016 UnboundID Corp.
 * All Rights Reserved.
 */
/*
 * Copyright (C) 2015-2016 UnboundID Corp.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License (GPLv2 only)
 * or the terms of the GNU Lesser General Public License (LGPLv2.1 only)
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>.
 */
package com.unboundid.ldap.sdk.unboundidds.extensions;



import com.unboundid.util.ThreadSafety;
import com.unboundid.util.ThreadSafetyLevel;



/**
 * <BLOCKQUOTE>
 *   <B>NOTE:</B>  This class is part of the Commercial Edition of the UnboundID
 *   LDAP SDK for Java.  It is not available for use in applications that
 *   include only the Standard Edition of the LDAP SDK, and is not supported for
 *   use in conjunction with non-UnboundID products.
 * </BLOCKQUOTE>
 * This enum defines the types of configurations that may be obtained using the
 * get configuration extended operation.
 */
@ThreadSafety(level=ThreadSafetyLevel.COMPLETELY_THREADSAFE)
public enum GetConfigurationType
{
  /**
   * The type used to specify the current active configuration.
   */
  ACTIVE(GetConfigurationType.ACTIVE_BER_TYPE, 0),



  /**
   * The type used to specify the baseline configuration for the current server
   * version.
   */
  BASELINE(GetConfigurationType.BASELINE_BER_TYPE, 1),



  /**
   * The type used to specify an archived configuration that was previously
   * in effect.
   */
  ARCHIVED(GetConfigurationType.ARCHIVED_BER_TYPE, 2);



  /**
   * The BER type used to designate the active type.
   */
  static final byte ACTIVE_BER_TYPE = (byte) 0x80;



  /**
   * The BER type used to designate the baseline type.
   */
  static final byte BASELINE_BER_TYPE = (byte) 0x81;



  /**
   * The BER type used to designate the archived type.
   */
  static final byte ARCHIVED_BER_TYPE = (byte) 0x82;



  // The BER type that should be used when this configuration type needs to be
  // encoded in a get configuration request.
  private final byte berType;

  // The integer value that should be used when this configuration type needs to
  // be encoded as an enumerated element in a get configuration result.
  private final int intValue;



  /**
   * Creates a new get configuration type value with the specified information.
   *
   * @param  berType   The BER type that should be used when this configuration
   *                   type needs to be encoded in a get configuration request.
   * @param  intValue  The integer value that should be used when this
   *                   configuration type needs to be encoded as an enumerated
   *                   element in a get configuration result.
   */
  private GetConfigurationType(final byte berType, final int intValue)
  {
    this.berType  = berType;
    this.intValue = intValue;
  }



  /**
   * Retrieves the BER type that should be used when this configuration type
   * needs to be encoded in a get configuration request.
   *
   * @return  The BER type that should be used when this configuration type
   *          needs to be encoded in a get configuration request.
   */
  public byte getBERType()
  {
    return berType;
  }



  /**
   * Retrieves the integer value that should be used when this configuration
   * type needs to be encoded as an enumerated element in a get configuration
   * result.
   *
   * @return  The integer value that should be used when this configuration
   *          type needs to be encoded as an enumerated element in a get
   *          configuration result.
   */
  public int getIntValue()
  {
    return intValue;
  }



  /**
   * Retrieves the get configuration type value that has the specified BER type.
   *
   * @param  berType  The BER type for the get configuration type value to
   *                  retrieve.
   *
   * @return  The get configuration type value for the specified BER type, or
   *          {@code null} if there is no enum value with the specified BER
   *          type.
   */
  public static GetConfigurationType forBERType(final byte berType)
  {
    for (final GetConfigurationType t : values())
    {
      if (t.berType == berType)
      {
        return t;
      }
    }

    return null;
  }



  /**
   * Retrieves the get configuration type value that has the specified integer
   * value.
   *
   * @param  intValue  The integer value for the get configuration type value
   *                   to retrieve.
   *
   * @return  The get configuration type value for the specified integer value,
   *          or {@code null} if there is no enum value with the specified
   *          integer value.
   */
  public static GetConfigurationType forIntValue(final int intValue)
  {
    for (final GetConfigurationType t : values())
    {
      if (t.intValue == intValue)
      {
        return t;
      }
    }

    return null;
  }
}