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



import com.unboundid.asn1.ASN1Element;
import com.unboundid.asn1.ASN1OctetString;
import com.unboundid.asn1.ASN1Sequence;
import com.unboundid.ldap.sdk.Control;
import com.unboundid.ldap.sdk.ExtendedRequest;
import com.unboundid.ldap.sdk.ExtendedResult;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.ResultCode;
import com.unboundid.util.Debug;
import com.unboundid.util.StaticUtils;
import com.unboundid.util.ThreadSafety;
import com.unboundid.util.ThreadSafetyLevel;
import com.unboundid.util.Validator;

import static com.unboundid.ldap.sdk.unboundidds.extensions.ExtOpMessages.*;



/**
 * <BLOCKQUOTE>
 *   <B>NOTE:</B>  This class is part of the Commercial Edition of the UnboundID
 *   LDAP SDK for Java.  It is not available for use in applications that
 *   include only the Standard Edition of the LDAP SDK, and is not supported for
 *   use in conjunction with non-UnboundID products.
 * </BLOCKQUOTE>
 * This class provides an implementation of an extended request that can be used
 * to identify potential incompatibility problems between two backup
 * compatibility descriptor values.  This can be used to determine whether a
 * backup from one server (or an older version of the same server) could be
 * restored into another server (or a newer version of the same server).  It
 * may also be useful in determining whether replication initialization via
 * binary copy may be performed between two servers.
 * <BR><BR>
 * The OID for this extended request is 1.3.6.1.4.1.30221.2.6.32.  It must have
 * a value with the following encoding:
 * <PRE>
 *   IdentifyBackupCompatibilityProblemsRequest ::= SEQUENCE {
 *        sourceDescriptor     [0] OCTET STRING,
 *        targetDescriptor     [1] OCTET STRING,
 *        ... }
 * </PRE>
 *
 * @see  IdentifyBackupCompatibilityProblemsExtendedResult
 * @see  GetBackupCompatibilityDescriptorExtendedRequest
 */
@ThreadSafety(level=ThreadSafetyLevel.COMPLETELY_THREADSAFE)
public final class IdentifyBackupCompatibilityProblemsExtendedRequest
       extends ExtendedRequest
{
  /**
   * The OID (1.3.6.1.4.1.30221.2.6.32) for the identify backup compatibility
   * problems extended request.
   */
  public static final String
       IDENTIFY_BACKUP_COMPATIBILITY_PROBLEMS_REQUEST_OID =
            "1.3.6.1.4.1.30221.2.6.32";



  /**
   * The BER type for the source descriptor element in the value sequence.
   */
  private static final byte TYPE_SOURCE_DESCRIPTOR = (byte) 0x80;



  /**
   * The BER type for the target descriptor element in the value sequence.
   */
  private static final byte TYPE_TARGET_DESCRIPTOR = (byte) 0x81;



  /**
   * The serial version UID for this serializable class.
   */
  private static final long serialVersionUID = 6723590129573376599L;



  // The backup compatibility descriptor obtained from the source server, or
  // from a backup to be restored.
  private final ASN1OctetString sourceDescriptor;

  // The backup compatibility descriptor obtained from the target server.
  private final ASN1OctetString targetDescriptor;



  /**
   * Creates a new identify backup compatibility problems extended request with
   * the provided information.
   *
   * @param  sourceDescriptor  The backup compatibility descriptor obtained from
   *                           the source server, or from a backup to be
   *                           restored.  It must not be {@code null}.
   * @param  targetDescriptor  The backup compatibility descriptor obtained from
   *                           the target server.  It must not be {@code null}.
   * @param  controls          The set of controls to include in the request.
   *                           It may be {@code null} or empty if no controls
   *                           should be included.
   */
  public IdentifyBackupCompatibilityProblemsExtendedRequest(
       final ASN1OctetString sourceDescriptor,
       final ASN1OctetString targetDescriptor, final Control... controls)
  {
    super(IDENTIFY_BACKUP_COMPATIBILITY_PROBLEMS_REQUEST_OID,
         encodeValue(sourceDescriptor, targetDescriptor), controls);

    this.sourceDescriptor = new ASN1OctetString(TYPE_SOURCE_DESCRIPTOR,
         sourceDescriptor.getValue());
    this.targetDescriptor = new ASN1OctetString(TYPE_TARGET_DESCRIPTOR,
         targetDescriptor.getValue());
  }



  /**
   * Creates a new identify backup compatibility problems extended request from
   * the provided generic extended request.
   *
   * @param  r  The generic extended request to decode as an identify backup
   *            compatibility problems extended request.
   *
   * @throws LDAPException  If the provided request cannot be decoded as an
   *                        identify backup compatibility problems extended
   *                        request.
   */
  public IdentifyBackupCompatibilityProblemsExtendedRequest(
       final ExtendedRequest r)
       throws LDAPException
  {
    super(r);

    final ASN1OctetString value = r.getValue();
    if (value == null)
    {
      throw new LDAPException(ResultCode.DECODING_ERROR,
           ERR_IDENTIFY_BACKUP_COMPAT_PROBLEMS_REQUEST_NO_VALUE.get());
    }

    try
    {
      final ASN1Element[] elements =
           ASN1Sequence.decodeAsSequence(value.getValue()).elements();
      sourceDescriptor =
           new ASN1OctetString(TYPE_SOURCE_DESCRIPTOR, elements[0].getValue());
      targetDescriptor =
           new ASN1OctetString(TYPE_SOURCE_DESCRIPTOR, elements[1].getValue());
    }
    catch (final Exception e)
    {
      Debug.debugException(e);
      throw new LDAPException(ResultCode.DECODING_ERROR,
           ERR_IDENTIFY_BACKUP_COMPAT_PROBLEMS_REQUEST_ERROR_PARSING_VALUE.get(
                StaticUtils.getExceptionMessage(e)),
           e);
    }
  }



  /**
   * Encodes the provided information into a format suitable for use as the
   * value of this extended request.
   *
   * @param  sourceDescriptor  The backup compatibility descriptor obtained from
   *                           the source server, or from a backup to be
   *                           restored.  It must not be {@code null}.
   * @param  targetDescriptor  The backup compatibility descriptor obtained from
   *                           the target server.  It must not be {@code null}.
   *
   * @return  The ASN.1 octet string containing the encoded representation of
   *          the provided information.
   */
  private static ASN1OctetString encodeValue(
                                      final ASN1OctetString sourceDescriptor,
                                      final ASN1OctetString targetDescriptor)
  {
    Validator.ensureNotNull(sourceDescriptor);
    Validator.ensureNotNull(targetDescriptor);

    final ASN1Sequence valueSequence = new ASN1Sequence(
         new ASN1OctetString(TYPE_SOURCE_DESCRIPTOR,
              sourceDescriptor.getValue()),
         new ASN1OctetString(TYPE_TARGET_DESCRIPTOR,
              targetDescriptor.getValue()));

    return new ASN1OctetString(valueSequence.encode());
  }



  /**
   * Retrieves the backup compatibility descriptor obtained from the source
   * server, or from a backup to be restored.
   *
   * @return  The backup compatibility descriptor obtained from the source
   *          server, or from a backup to be restored.
   */
  public ASN1OctetString getSourceDescriptor()
  {
    return sourceDescriptor;
  }



  /**
   * Retrieves the backup compatibility descriptor obtained from the target
   * server.
   *
   * @return  The backup compatibility descriptor obtained from the target
   *          server.
   */
  public ASN1OctetString getTargetDescriptor()
  {
    return targetDescriptor;
  }



  /**
   * {@inheritDoc}
   */
  @Override()
  public IdentifyBackupCompatibilityProblemsExtendedResult process(
              final LDAPConnection connection, final int depth)
         throws LDAPException
  {
    final ExtendedResult extendedResponse = super.process(connection, depth);
    return new IdentifyBackupCompatibilityProblemsExtendedResult(
         extendedResponse);
  }



  /**
   * {@inheritDoc}
   */
  @Override()
  public IdentifyBackupCompatibilityProblemsExtendedRequest duplicate()
  {
    return duplicate(getControls());
  }



  /**
   * {@inheritDoc}
   */
  @Override()
  public IdentifyBackupCompatibilityProblemsExtendedRequest duplicate(
              final Control[] controls)
  {
    final IdentifyBackupCompatibilityProblemsExtendedRequest r =
         new IdentifyBackupCompatibilityProblemsExtendedRequest(
              sourceDescriptor, targetDescriptor, controls);
    r.setResponseTimeoutMillis(getResponseTimeoutMillis(null));
    return r;
  }



  /**
   * {@inheritDoc}
   */
  @Override()
  public String getExtendedRequestName()
  {
    return INFO_EXTENDED_REQUEST_NAME_IDENTIFY_BACKUP_COMPAT_PROBLEMS.get();
  }



  /**
   * {@inheritDoc}
   */
  @Override()
  public void toString(final StringBuilder buffer)
  {
    buffer.append("IdentifyBackupCompatibilityProblemsExtendedRequest(" +
         "sourceDescriptorLength=");
    buffer.append(sourceDescriptor.getValueLength());
    buffer.append(", targetDescriptorLength=");
    buffer.append(targetDescriptor.getValueLength());

    final Control[] controls = getControls();
    if (controls.length > 0)
    {
      buffer.append(", controls={");
      for (int i=0; i < controls.length; i++)
      {
        if (i > 0)
        {
          buffer.append(", ");
        }

        buffer.append(controls[i]);
      }
      buffer.append('}');
    }

    buffer.append(')');
  }
}