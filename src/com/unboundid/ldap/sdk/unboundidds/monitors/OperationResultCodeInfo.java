/*
 * Copyright 2014-2016 UnboundID Corp.
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
package com.unboundid.ldap.sdk.unboundidds.monitors;



import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import com.unboundid.ldap.sdk.Attribute;
import com.unboundid.ldap.sdk.Entry;
import com.unboundid.ldap.sdk.OperationType;
import com.unboundid.util.Debug;
import com.unboundid.util.NotMutable;
import com.unboundid.util.StaticUtils;
import com.unboundid.util.ThreadSafety;
import com.unboundid.util.ThreadSafetyLevel;



/**
 * <BLOCKQUOTE>
 *   <B>NOTE:</B>  This class is part of the Commercial Edition of the UnboundID
 *   LDAP SDK for Java.  It is not available for use in applications that
 *   include only the Standard Edition of the LDAP SDK, and is not supported for
 *   use in conjunction with non-UnboundID products.
 * </BLOCKQUOTE>
 * This class provides a data structure that provides information about the
 * result codes associated with a particular type of operation (or across all
 * types of operations, if the associated operation type is {@code null}).
 */
@NotMutable()
@ThreadSafety(level=ThreadSafetyLevel.COMPLETELY_THREADSAFE)
public final class OperationResultCodeInfo
       implements Serializable
{
  /**
   * The serial version UID for this serializable class.
   */
  private static final long serialVersionUID = 4688688688915878084L;



  // The percentage of operations of the associated type that failed.
  private final Double failedPercent;

  // The total number of operations of the associated type that failed.
  private final Long failedCount;

  // The total number of operations of the associated type.
  private final Long totalCount;

  // Information about each result code returned for the associated operation
  // type, indexed by the result code's integer value.
  private final Map<Integer,ResultCodeInfo> resultCodeInfoMap;

  // The associated operation type.  It may be null if this structure provides
  // information about all operation types.
  private final OperationType operationType;



  /**
   * Creates a new operation result code information object from the provided
   * information.
   *
   * @param  entry             The monitor entry to use to obtain the result
   *                           code information.
   * @param  operationType     The operation type for this object.  It may be
   *                           {@code null} if the information applies to all
   *                           types of operations.
   * @param  opTypeAttrPrefix  The prefix that will be used for information
   *                           about
   */
  OperationResultCodeInfo(final MonitorEntry entry,
                          final OperationType operationType,
                          final String opTypeAttrPrefix)
  {
    this.operationType = operationType;

    totalCount = entry.getLong(opTypeAttrPrefix + "total-count");
    failedCount = entry.getLong(opTypeAttrPrefix + "failed-count");
    failedPercent = entry.getDouble(opTypeAttrPrefix + "failed-percent");

    final String rcPrefix = opTypeAttrPrefix + "result-";
    final TreeMap<Integer,ResultCodeInfo> rcMap =
         new TreeMap<Integer,ResultCodeInfo>();
    final Entry e = entry.getEntry();
    for (final Attribute a : e.getAttributes())
    {
      try
      {
        final String lowerName = StaticUtils.toLowerCase(a.getName());
        if (lowerName.startsWith(rcPrefix) && lowerName.endsWith("-name"))
        {
          final String name = a.getValue();
          final int intValue = Integer.parseInt(lowerName.substring(
               rcPrefix.length(), (lowerName.length() - 5)));
          final long count = entry.getLong(rcPrefix + intValue + "-count");
          final double percent = entry.getDouble(
               rcPrefix + intValue + "-percent");
          final double totalResponseTimeMillis = entry.getDouble(
               rcPrefix + intValue + "-total-response-time-millis");
          final double averageResponseTimeMillis = entry.getDouble(
               rcPrefix + intValue + "-average-response-time-millis");
          rcMap.put(intValue,
               new ResultCodeInfo(intValue, name, operationType, count, percent,
                    totalResponseTimeMillis, averageResponseTimeMillis));
        }
      }
      catch (final Exception ex)
      {
        Debug.debugException(ex);
      }
    }

    resultCodeInfoMap = Collections.unmodifiableMap(rcMap);
  }



  /**
   * Retrieves the type of operation with which this result code information is
   * associated, if appropriate.
   *
   * @return  The type of operation with which this result code information is
   *          associated, or {@code null} if this information applies to all
   *          types of operations.
   */
  public OperationType getOperationType()
  {
    return operationType;
  }



  /**
   * Retrieves the total number of operations of the associated type that have
   * been processed, if available.
   *
   * @return  The total number of operations of the associated type that have
   *          been processed, or {@code null} if this information was not in the
   *          monitor entry.
   */
  public Long getTotalCount()
  {
    return totalCount;
  }



  /**
   * Retrieves the number of operations of the associated type that resulted in
   * failure, if available.
   *
   * @return  The number of operations of the associated type that resulted
   *          in failure, or {@code null} if this information was not in the
   *          monitor entry.
   */
  public Long getFailedCount()
  {
    return failedCount;
  }



  /**
   * Retrieves the percent of operations of the associated type that resulted in
   * failure, if available.
   *
   * @return  The percent of operations of the associated type that resulted
   *          in failure, or {@code null} if this information was not in the
   *          monitor entry.
   */
  public Double getFailedPercent()
  {
    return failedPercent;
  }



  /**
   * Retrieves a map with information about the result codes that have been
   * returned for operations of the associated type, indexed by the result
   * code's integer value.
   *
   * @return  A map with information about the result codes that have been
   *          returned for operations of the associated type.
   */
  public Map<Integer,ResultCodeInfo> getResultCodeInfoMap()
  {
    return resultCodeInfoMap;
  }
}