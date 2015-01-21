/*
 * Sonar Keycloak Plugin
 * Copyright (C) 2012 SonarSource
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.keycloak;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.web.ServletFilter;

/**
 * Validate tokens forwarded by the keycloak  after the request initiated by {@link KeycloakAuthenticationFilter}.
 * If authenfication is successful, then object of type UserDetails is added to request attributes.
 * 
 * @author Mohammad Nadeem
 * 
 */
public final class KeycloakValidationFilter extends ServletFilter {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(KeycloakValidationFilter.class);

	private KeycloakClient keycloakClient;

	public KeycloakValidationFilter(KeycloakClient keycloakClient) {
		this.keycloakClient = keycloakClient;
	}

	@Override
	public UrlPattern doGetPattern() {
		return UrlPattern.create(KeycloakClient.SONAR_VALIDATE_URL);
	}

	public void init(FilterConfig filterConfig) throws ServletException {
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
	    HttpServletResponse httpResponse = (HttpServletResponse) response;

		KeycloakAuthentication authentication = null;
		try {
			authentication = this.keycloakClient.getKeycloakAuthentication(httpRequest);
			LOGGER.info("authentication : " + authentication);
		} catch (Exception e) {
			LOGGER.error("Can't get user ", e);
		}
		if (authentication == null) {
			httpResponse.sendRedirect(KeycloakClient.SONAR_UN_AUTHORIZED_URL);
		} else {
			request.setAttribute(KeycloakClient.KEYCLOAK_AUTHENTICATION_ATTRIBUTE, authentication);
			filterChain.doFilter(request, response);
		}
		httpRequest.getSession().setAttribute(KeycloakClient.KEYCLOAK_AUTHENTICATION_ATTRIBUTE, authentication);
	}

	public void destroy() {
	}
}