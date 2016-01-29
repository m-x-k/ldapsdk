/*
 * Copyright 2012-2016 UnboundID Corp.
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



import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.unboundid.asn1.ASN1Element;
import com.unboundid.asn1.ASN1OctetString;
import com.unboundid.asn1.ASN1Sequence;
import com.unboundid.ldap.sdk.Control;
import com.unboundid.ldap.sdk.ExtendedResult;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.ResultCode;
import com.unboundid.util.Debug;
import com.unboundid.util.NotMutable;
import com.unboundid.util.StaticUtils;
import com.unboundid.util.ThreadSafety;
import com.unboundid.util.ThreadSafetyLevel;

import static com.unboundid.ldap.sdk.unboundidds.extensions.ExtOpMessages.*;



/**
 * <BLOCKQUOTE>
 *   <B>NOTE:</B>  This class is part of the Commercial Edition of the UnboundID
 *   LDAP SDK for Java.  It is not available for use in applications that
 *   include only the Standard Edition of the LDAP SDK, and is not supported for
 *   use in conjunction with non-UnboundID products.
 * </BLOCKQUOTE>
 * This class provides an implementation of an extended result that can be used
 * to provide information about the notification subscriptions defined in the
 * target server.  The OID for this result is 1.3.6.1.4.1.30221.2.6.41, and the
 * value (if present) should have the following encoding:
 * <BR><BR>
 * <PRE>
 *   ListNotificationSubscriptionsResponse ::= SEQUENCE OF SEQUENCE {
 *        notificationDestinationID     OCTET STRING,
 *        destinationDetails            SEQUENCE OF OCTET STRING,
 *        subscriptions                 SEQUENCE OF SEQUENCE {
 *             subscriptionID          OCTET STRING,
 *             subscriptionDetails     SEQUENCE OF OCTET STRING } }
 * </PRE>
 */
@NotMutable()
@ThreadSafety(level=ThreadSafetyLevel.COMPLETELY_THREADSAFE)
public final class ListNotificationSubscriptionsExtendedResult
       extends ExtendedResult
{
  /**
   * The OID (1.3.6.1.4.1.30221.2.6.41) for the list notification subscriptions
   * extended result.
   */
  public static final String LIST_NOTIFICATION_SUBSCRIPTIONS_RESULT_OID =
       "1.3.6.1.4.1.30221.2.6.41";



  /**
   * The serial version UID for this serializable class.
   */
  private static final long serialVersionUID = 8876370324325619149L;



  // The notification destination details for this result.
  private final List<NotificationDestinationDetails> destinations;



  /**
   * Creates a new list notification subscriptions extended result from the
   * provided extended result.
   *
   * @param  extendedResult  The extended result to be decoded as a list
   *                         notification subscriptions extended result.
   *
   * @throws LDAPException  If a problem is encountered while attempting to
   *                         decode the provided extended result as a
   *                         multi-update result.
   */
  public ListNotificationSubscriptionsExtendedResult(
              final ExtendedResult extendedResult)
         throws LDAPException
  {
    super(extendedResult);

    final ASN1OctetString value = extendedResult.getValue();
    if (value == null)
    {
      destinations = Collections.emptyList();
      return;
    }

    try
    {
      final ASN1Element[] destsElements =
           ASN1Sequence.decodeAsSequence(value.getValue()).elements();
      final ArrayList<NotificationDestinationDetails> destList =
           new ArrayList<NotificationDestinationDetails>(destsElements.length);
      for (final ASN1Element destElement : destsElements)
      {
        final ASN1Element[] destElements =
             ASN1Sequence.decodeAsSequence(destElement).elements();
        final String destID =
             ASN1OctetString.decodeAsOctetString(destElements[0]).stringValue();

        final ASN1Element[] destDetailsElements =
             ASN1Sequence.decodeAsSequence(destElements[1]).elements();
        final ArrayList<ASN1OctetString> destDetailsList =
             new ArrayList<ASN1OctetString>(destDetailsElements.length);
        for (final ASN1Element e : destDetailsElements)
        {
          destDetailsList.add(ASN1OctetString.decodeAsOctetString(e));
        }

        final ASN1Element[] subElements =
             ASN1Sequence.decodeAsSequence(destElements[2]).elements();
        final ArrayList<NotificationSubscriptionDetails> subscriptions =
             new ArrayList<NotificationSubscriptionDetails>(subElements.length);
        for (final ASN1Element e : subElements)
        {
          final ASN1Element[] sElements =
               ASN1Sequence.decodeAsSequence(e).elements();
          final String subID =
               ASN1OctetString.decodeAsOctetString(sElements[0]).stringValue();

          final ASN1Element[] subDetailsElements =
               ASN1Sequence.decodeAsSequence(sElements[1]).elements();
          final ArrayList<ASN1OctetString> subDetails =
               new ArrayList<ASN1OctetString>(subDetailsElements.length);
          for (final ASN1Element sde : subDetailsElements)
          {
            subDetails.add(ASN1OctetString.decodeAsOctetString(sde));
          }
          subscriptions.add(
               new NotificationSubscriptionDetails(subID, subDetails));
        }

        destList.add(new NotificationDestinationDetails(destID, destDetailsList,
             subscriptions));
      }

      destinations = Collections.unmodifiableList(destList);
    }
    catch (final Exception e)
    {
      Debug.debugException(e);
      throw new LDAPException(ResultCode.DECODING_ERROR,
           ERR_LIST_NOTIFICATION_SUBS_RESULT_CANNOT_DECODE_VALUE.get(
                StaticUtils.getExceptionMessage(e)),
           e);
    }
  }



  /**
   * Creates a new list notification subscriptions extended request with the
   * provided information.
   *
   * @param  messageID          The message ID for this extended result.
   * @param  resultCode         The result code for this result.  It must not be
   *                            {@code null}.
   * @param  diagnosticMessage  The diagnostic message to include in the result.
   *                            It may be {@code null} if no diagnostic message
   *                            should be included.
   * @param  matchedDN          The matched DN to include in the result.  It may
   *                            be {@code null} if no matched DN should be
   *                            included.
   * @param  referralURLs       The set of referral URLs to include in the
   *                            result.  It may be {@code null} or empty if no
   *                            referral URLs should be included.
   * @param  destinations       The notification destination details for this
   *                            result.  It may be {@code null} or empty for a
   *                            non-success result.
   * @param  controls           The set of controls to include in the
   *                            multi-update result.  It may be {@code null} or
   *                            empty if no controls should be included.
   *
   * @throws  LDAPException  If any of the results are for an inappropriate
   *                         operation type.
   */
  public ListNotificationSubscriptionsExtendedResult(final int messageID,
              final ResultCode resultCode, final String diagnosticMessage,
              final String matchedDN, final String[] referralURLs,
              final Collection<NotificationDestinationDetails> destinations,
              final Control... controls)
         throws LDAPException
  {
    super(messageID, resultCode, diagnosticMessage, matchedDN, referralURLs,
         LIST_NOTIFICATION_SUBSCRIPTIONS_RESULT_OID, encodeValue(destinations),
         controls);

    if (destinations == null)
    {
      this.destinations = Collections.emptyList();
    }
    else
    {
      this.destinations = Collections.unmodifiableList(
           new ArrayList<NotificationDestinationDetails>(destinations));
    }
  }



  /**
   * Encodes the information from the provided set of results into a form
   * suitable for use as the value of the extended result.
   *
   * @param  destinations  The notification destination details for the result.
   *                       It may be {@code null} or empty for a non-success
   *                       result.
   *
   * @return  An ASN.1 element suitable for use as the value of the extended
   *          result.
   */
  private static ASN1OctetString encodeValue(
               final Collection<NotificationDestinationDetails> destinations)
  {
    if ((destinations == null) || destinations.isEmpty())
    {
      return null;
    }

    final ArrayList<ASN1Element> elements =
         new ArrayList<ASN1Element>(destinations.size());
    for (final NotificationDestinationDetails destDetails : destinations)
    {
      final ArrayList<ASN1Element> destElements = new ArrayList<ASN1Element>(3);
      destElements.add(new ASN1OctetString(destDetails.getID()));
      destElements.add(new ASN1Sequence(destDetails.getDetails()));

      final ArrayList<ASN1Element> subElements =
           new ArrayList<ASN1Element>(destDetails.getSubscriptions().size());
      for (final NotificationSubscriptionDetails subDetails :
           destDetails.getSubscriptions())
      {
        subElements.add(new ASN1Sequence(
             new ASN1OctetString(subDetails.getID()),
             new ASN1Sequence(subDetails.getDetails())));
      }
      destElements.add(new ASN1Sequence(subElements));
      elements.add(new ASN1Sequence(destElements));
    }

    return new ASN1OctetString(new ASN1Sequence(elements).encode());
  }



  /**
   * Retrieves a list of the defined notification destinations and their
   * associated subscriptions.
   *
   * @return  A list of the defined notification destinations and their
   *          associated subscriptions.
   */
  public List<NotificationDestinationDetails> getDestinations()
  {
    return destinations;
  }



  /**
   * {@inheritDoc}
   */
  @Override()
  public String getExtendedResultName()
  {
    return INFO_EXTENDED_RESULT_NAME_LIST_NOTIFICATION_SUBS.get();
  }



  /**
   * Appends a string representation of this extended result to the provided
   * buffer.
   *
   * @param  buffer  The buffer to which a string representation of this
   *                 extended result will be appended.
   */
  @Override()
  public void toString(final StringBuilder buffer)
  {
    buffer.append("ListNotificationSubscriptionsExtendedResult(resultCode=");
    buffer.append(getResultCode());

    final int messageID = getMessageID();
    if (messageID >= 0)
    {
      buffer.append(", messageID=");
      buffer.append(messageID);
    }

    buffer.append(", notificationDestinations={");
    final Iterator<NotificationDestinationDetails> destIterator =
         destinations.iterator();
    while (destIterator.hasNext())
    {
      destIterator.next().toString(buffer);
      if (destIterator.hasNext())
      {
        buffer.append(", ");
      }
    }
    buffer.append('}');

    final String diagnosticMessage = getDiagnosticMessage();
    if (diagnosticMessage != null)
    {
      buffer.append(", diagnosticMessage='");
      buffer.append(diagnosticMessage);
      buffer.append('\'');
    }

    final String matchedDN = getMatchedDN();
    if (matchedDN != null)
    {
      buffer.append(", matchedDN='");
      buffer.append(matchedDN);
      buffer.append('\'');
    }

    final String[] referralURLs = getReferralURLs();
    if (referralURLs.length > 0)
    {
      buffer.append(", referralURLs={");
      for (int i=0; i < referralURLs.length; i++)
      {
        if (i > 0)
        {
          buffer.append(", ");
        }

        buffer.append('\'');
        buffer.append(referralURLs[i]);
        buffer.append('\'');
      }
      buffer.append('}');
    }

    final Control[] responseControls = getResponseControls();
    if (responseControls.length > 0)
    {
      buffer.append(", responseControls={");
      for (int i=0; i < responseControls.length; i++)
      {
        if (i > 0)
        {
          buffer.append(", ");
        }

        buffer.append(responseControls[i]);
      }
      buffer.append('}');
    }

    buffer.append(')');
  }
}