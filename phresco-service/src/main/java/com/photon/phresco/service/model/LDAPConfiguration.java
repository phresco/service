/**
 * Phresco Service
 *
 * Copyright (C) 1999-2014 Photon Infotech Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.photon.phresco.service.model;

import java.util.Properties;

import com.photon.phresco.exception.PhrescoException;
import com.photon.phresco.service.util.ServerConstants;

/**
 * LDAP configuration
 */
public class LDAPConfiguration implements ServerConstants {
	
	/**
	 * 
	 */
	private String ldapContextFactory;
	
	/**
	 * 
	 */
	private String ldapUrl;
	
	/**
	 * 
	 */
	private String ldapBaseDn;
	
	/**
	 * 
	 */
	private String ldapLoginAttribute;
	
	/**
	 * 
	 */
	private String displayNameAttribute;
	
	/**
	 * 
	 */
	private String mailIdAttribute;
	
	/**
	 * 
	 */
	private String phrescoEnabledAttribute;
	
	/**
	 * 
	 */
	private String customerNameAttribute;

	/**
	 * @param serverProps
	 * @throws PhrescoException
	 */
	public LDAPConfiguration(Properties serverProps) throws PhrescoException {
		if (serverProps == null) {
			throw new PhrescoException("LDAP Properties cannot be null");
		}
		
		Properties ldapProps = serverProps;
		ldapContextFactory = (String) ldapProps.get(LDAP_CONTEXT_FACTORY);
		ldapUrl = (String) ldapProps.get(LDAP_URL);
		ldapBaseDn = (String) ldapProps.get(LDAP_BASEDN);
		ldapLoginAttribute = (String) ldapProps.get(LDAP_LOGIN_ATTRIBUTE);
		displayNameAttribute = (String) ldapProps.get(LDAP_DISPLAY_NAME_ATTRIBUTE);
		mailIdAttribute = (String) ldapProps.get(LDAP_MAIL_ATTRIBUTE);
		phrescoEnabledAttribute=(String) ldapProps.get(LDAP_PHRESCO_ENABLED);
		customerNameAttribute=(String) ldapProps.get(LDAP_CUSTOMER_NAME);

	}

	/**
	 * @return the ldapContextFactory
	 */
	public String getLdapContextFactory() {
		return ldapContextFactory;
	}

	/**
	 * @return the ldapUrl
	 */
	public String getLdapUrl() {
		return ldapUrl;
	}

	/**
	 * @return the ldapBaseDn
	 */
	public String getLdapBaseDn() {
		return ldapBaseDn;
	}

	/**
	 * @return the ldapLoginAttribute
	 */
	public String getLdapLoginAttribute() {
		return ldapLoginAttribute;
	}

	/**
	 * @return the displayName
	 */
	public String getDisplayNameAttribute() {
		return displayNameAttribute;
	}

	/**
	 * @return the mail
	 */
	public String getMailIdAttribute() {
		return mailIdAttribute;
	}
	
	/**
	 * 
	 * @return the customerName
	 */
	public String getCustomerNameAttribute(){
		return customerNameAttribute;
	}

	/**
	 * 
	 * @return the phrescoEnabled true or false
	 */
	public String getPhrescoEnabledAttribute(){
		return phrescoEnabledAttribute;
	}
}