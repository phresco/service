/**
 * Service Web Archive
 *
 * Copyright (C) 1999-2013 Photon Infotech Inc.
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
package com.photon.phresco.service.admin.actions.components;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import com.photon.phresco.commons.model.Element;
import com.photon.phresco.commons.model.PropertyTemplate;
import com.photon.phresco.commons.model.SettingsTemplate;
import com.photon.phresco.commons.model.Technology;
import com.photon.phresco.exception.PhrescoException;
import com.photon.phresco.logger.SplunkLogger;
import com.photon.phresco.service.admin.actions.ServiceBaseAction;
import com.photon.phresco.service.client.api.ServiceManager;

public class ConfigTemplates extends ServiceBaseAction {
	
	private static final long serialVersionUID = 6801037145464060759L;
	
	private static final SplunkLogger S_LOGGER = SplunkLogger.getSplunkLogger(ConfigTemplates.class.getName());
	private static Boolean isDebugEnabled = S_LOGGER.isDebugEnabled();
	
	private String name = "";
	private String dispName = "";
	private String description = "";
	private List<String> appliesTo = null;
	private boolean defaultCustProp;
	private boolean favourite;
	private boolean envSpecific;
	private boolean system;
	private List<PropertyTemplate> propTemps = null;
    private String propTempKey = null;
    private String defaultValue = "";
	
	private String nameError = "";
	private String dispError = "";
	private String applyError = "";
	private boolean errorFound = false;
	
    private String customerId = "";
    
    private String configId = ""; // it will be generated by the mongodb
    private String oldName = "";
    
    private String fromPage = "";
    
    /**
     * To get all config templates form DB
     * @return List of config templates
     * @throws PhrescoException
     */
	public String list() {
		if (isDebugEnabled) {
	    	S_LOGGER.debug("ConfigTemplates.list : Entry");
	    }
		
		try {
			if (isDebugEnabled) {
				if (StringUtils.isEmpty(getCustomerId())) {
					S_LOGGER.warn("ConfigTemplates.list", "status=\"Bad Request\"", "message=\"Customer Id is empty\"");
					return showErrorPopup(new PhrescoException("Customer Id is empty"), getText(EXCEPTION_APPTYPES_UPDATE));
				}
				S_LOGGER.info("ConfigTemplates.list", "customerId=" + "\"" + getCustomerId() + "\"");
			}
			List<SettingsTemplate> configTemplates = getServiceManager().getConfigTemplates(getCustomerId());
			if (CollectionUtils.isNotEmpty(configTemplates)) {
				Collections.sort(configTemplates, sortTemplateInAlphaOrder());
			}
			setReqAttribute(REQ_CONFIG_TEMPLATES, configTemplates);
			setReqAttribute(REQ_CUST_CUSTOMER_ID, getCustomerId());
		} catch (PhrescoException e) {
			if (isDebugEnabled) {
		        S_LOGGER.error("ConfigTemplates.list", "status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
		    }
			return showErrorPopup(e, getText(EXCEPTION_CONFIG_TEMP_LIST));
		}
		if (isDebugEnabled) {
	    	S_LOGGER.debug("ConfigTemplates.list : Exit");
	    }
		
		return COMP_CONFIGTEMPLATE_LIST;
	}
	
	private Comparator sortTemplateInAlphaOrder() {
		return new Comparator() {
		    public int compare(Object firstObject, Object secondObject) {
		    	SettingsTemplate configTemplate1 = (SettingsTemplate) firstObject;
		    	SettingsTemplate configTemplate2 = (SettingsTemplate) secondObject;
		       return configTemplate1.getName().compareToIgnoreCase(configTemplate2.getName());
		    }
		};
	}
	
	/**
	 * To return to the page to add config templates
	 * @return 
	 * @throws PhrescoException
	 */
	public String add() {
		if (isDebugEnabled) {
	    	S_LOGGER.debug("ConfigTemplates.add : Entry");
	    }
		
		try {
			if (isDebugEnabled) {
				if (StringUtils.isEmpty(getCustomerId())) {
					S_LOGGER.warn("ConfigTemplates.add", "status=\"Bad Request\"", "message=\"Customer Id is empty\"");
					return showErrorPopup(new PhrescoException("Customer Id is empty"), getText(EXCEPTION_CONFIG_TEMP_ADD));
				}
				S_LOGGER.info("ConfigTemplates.add", "customerId=" + "\"" + getCustomerId() + "\"");
			}
			List<Technology> technologies = getServiceManager().getArcheTypes(getCustomerId());
			setReqAttribute(REQ_ARCHE_TYPES, technologies);
			setReqAttribute(REQ_FROM_PAGE, ADD);
		} catch (PhrescoException e) {
			if (isDebugEnabled) {
		        S_LOGGER.error("ConfigTemplates.add", "status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
		    }
		    return showErrorPopup(e, getText(EXCEPTION_CONFIG_TEMP_ADD));
		}
		if (isDebugEnabled) {
	    	S_LOGGER.debug("ConfigTemplates.add : Exit");
	    }
		
		return COMP_CONFIGTEMPLATE_ADD;
	}
	
	/**
	 * To return the edit page with the details of the selected config template
	 * @return
	 * @throws PhrescoException
	 */
	public String edit() {
		if (isDebugEnabled) {
	    	S_LOGGER.debug("ConfigTemplates.edit : Entry");
	    }
		
		try {
			if (isDebugEnabled) {
				if (StringUtils.isEmpty(getCustomerId())) {
					S_LOGGER.warn("ConfigTemplates.edit", "status=\"Bad Request\"", "message=\"Customer Id is empty\"");
					return showErrorPopup(new PhrescoException("Customer Id is empty"), getText(EXCEPTION_CONFIG_TEMP_EDIT));
				}
				if (StringUtils.isEmpty(getConfigId())) {
					S_LOGGER.warn("ConfigTemplates.edit", "status=\"Bad Request\"", "message=\"Configuration template Id is empty\"");
					return showErrorPopup(new PhrescoException("Configuration template Id is empty"), getText(EXCEPTION_CONFIG_TEMP_EDIT));
				}
				S_LOGGER.info("ConfigTemplates.edit", "customerId=" + "\"" + getCustomerId() + "\"", "configId=" + "\"" + getConfigId() + "\"");
			}
		    ServiceManager serviceManager = getServiceManager();
			SettingsTemplate configTemp = serviceManager.getConfigTemplate(getConfigId());
			List<Technology> technologies = serviceManager.getArcheTypes(getCustomerId());
			setReqAttribute(REQ_CUST_CUSTOMER_ID, getCustomerId());
		    setReqAttribute(REQ_CONFIG_TEMP, configTemp);
			setReqAttribute(REQ_ARCHE_TYPES, technologies);
			setReqAttribute(REQ_FROM_PAGE, EDIT);
		} catch (PhrescoException e) {
			if (isDebugEnabled) {
		        S_LOGGER.error("ConfigTemplates.edit", "status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
		    }
		    return showErrorPopup(e, getText(EXCEPTION_CONFIG_TEMP_EDIT));
		}
		if (isDebugEnabled) {
	    	S_LOGGER.debug("ConfigTemplates.edit : Exit");
	    }
		
		return COMP_CONFIGTEMPLATE_ADD;
	}
	
	/**
	 * To create a config template with the provided details
	 * @return List of Config templates
	 * @throws PhrescoException
	 */
	public String save() {
		if (isDebugEnabled) {
	    	S_LOGGER.debug("ConfigTemplates.save : Entry");
	    }

		try  {
			if (isDebugEnabled) {
				if (StringUtils.isEmpty(getCustomerId())) {
					S_LOGGER.warn("ConfigTemplates.save", "status=\"Bad Request\"", "message=\"Customer Id is empty\"");
					return showErrorPopup(new PhrescoException("Customer Id is empty"), getText(EXCEPTION_CONFIG_TEMP_SAVE));
				}
				S_LOGGER.info("ConfigTemplates.save", "customerId=" + "\"" + getCustomerId() + "\"");
			}
			List<SettingsTemplate> settingsTemplates = new ArrayList<SettingsTemplate>();
            settingsTemplates.add(createSettingsTemplateInstance());
            getServiceManager().createConfigTemplates(settingsTemplates, getCustomerId());
        	addActionMessage(getText(CONFIGTEMPLATE_ADDED, Collections.singletonList(getName())));
		} catch (PhrescoException e) {
			if (isDebugEnabled) {
		        S_LOGGER.error("ConfigTemplates.save", "status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
		    }
		    return showErrorPopup(e, getText(EXCEPTION_CONFIG_TEMP_SAVE));
		}
		if (isDebugEnabled) {
	    	S_LOGGER.debug("ConfigTemplates.save : Exit");
	    }
		
		return  list();
	}
	
	/**
	 * To update the details of the selected config template
	 * @return
	 * @throws PhrescoException
	 */
	public String update() {
		if (isDebugEnabled) {
	    	S_LOGGER.debug("ConfigTemplates.update : Entry");
	    }
    	
    	try {
    		if (isDebugEnabled) {
				if (StringUtils.isEmpty(getCustomerId())) {
					S_LOGGER.warn("ConfigTemplates.update", "status=\"Bad Request\"", "message=\"Customer Id is empty\"");
					return showErrorPopup(new PhrescoException("Customer Id is empty"), getText(EXCEPTION_CONFIG_TEMP_SAVE));
				}
				if (StringUtils.isEmpty(getCustomerId())) {
					S_LOGGER.warn("ConfigTemplates.update", "status=\"Bad Request\"", "message=\"Config template Id is empty\"");
					return showErrorPopup(new PhrescoException("Config template Id is empty"), getText(EXCEPTION_CONFIG_TEMP_SAVE));
				}
				S_LOGGER.info("ConfigTemplates.update", "customerId=" + "\"" + getCustomerId() + "\"", "configId=" + "\"" + getConfigId() + "\"");
			}
    		getServiceManager().createConfigTemplates(Collections.singletonList(createSettingsTemplateInstance()), getCustomerId());
    		addActionMessage(getText(CONFIGTEMPLATE_UPDATED, Collections.singletonList(getName())));
    	} catch (PhrescoException e) {
    		if (isDebugEnabled) {
		        S_LOGGER.error("ConfigTemplates.update", "status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
		    }
    	    return showErrorPopup(e, getText(EXCEPTION_CONFIG_TEMP_UPDATE));
		}
    	if (isDebugEnabled) {
	    	S_LOGGER.debug("ConfigTemplates.update : Exit");
	    }
    	
    	return list();
    }
	
	/**
	 * To update the details of the selected config template
	 * @return List of config templates
	 * @throws PhrescoException
	 */
	private SettingsTemplate createSettingsTemplateInstance() throws PhrescoException {
		if (isDebugEnabled) {
	    	S_LOGGER.debug("ConfigTemplates.createSettingsTemplateInstance : Entry");
	    }
		
		SettingsTemplate settingTemplate = null;
		try {
			List<String> techIds = getAppliesTo();
			if(isSystem() && !DEFAULT_CUSTOMER_NAME.equalsIgnoreCase(getCustomerId()) && EDIT.equalsIgnoreCase(fromPage)) {
				settingTemplate = getServiceManager().getConfigTemplate(getConfigId());
				settingTemplate.setCustomerIds(Arrays.asList(getCustomerId()));
				settingTemplate.setCustomProp(getDefaultCustProp());
				List<Element> appliesTos = new ArrayList<Element>();
				for (String techId : techIds) {
					String techName = getServiceManager().getTechnology(techId).getName();
					Element element = new Element();
					element.setId(techId);
					element.setName(techName);
					appliesTos.add(element);
				}
				settingTemplate.setAppliesToTechs(appliesTos);
				settingTemplate.setProperties(getPropTemps());
			} else {
				settingTemplate = new SettingsTemplate();
				settingTemplate.setName(getName());
				settingTemplate.setDisplayName(StringUtils.isEmpty(getDispName()) ? getName() : getDispName());
				settingTemplate.setDescription(getDescription());
				settingTemplate.setCustomProp(getDefaultCustProp());
				settingTemplate.setFavourite(isFavourite());
				settingTemplate.setEnvSpecific(isEnvSpecific());
				settingTemplate.setCustomerIds(Arrays.asList(getCustomerId()));
				if (StringUtils.isNotEmpty(getConfigId())) {
					settingTemplate.setId(getConfigId());
				}
				List<Element> appliesTos = new ArrayList<Element>();
				for (String techId : techIds) {
					String techName = getServiceManager().getTechnology(techId).getName();
					Element element = new Element();
					element.setId(techId);
					element.setName(techName);
					appliesTos.add(element);
				}
				
				settingTemplate.setAppliesToTechs(appliesTos);
				settingTemplate.setProperties(getPropTemps());
			}
			
		} catch (Exception e) {
			if (isDebugEnabled) {
		        S_LOGGER.error("ConfigTemplates.createSettingsTemplateInstance", "status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
		    }
			throw new PhrescoException(e);
		}
		if (isDebugEnabled) {
	    	S_LOGGER.debug("ConfigTemplates.createSettingsTemplateInstance : Exit");
	    }
		
		return settingTemplate;
	}
	
	public String showPropTempPopup() {
		if (isDebugEnabled) {
	    	S_LOGGER.debug("ConfigTemplates.showPropTempPopup : Entry");
	    }
		
		setReqAttribute(REQ_CONFIG_KEY, getPropTempKey());
		setReqAttribute(REQ_FROM_PAGE, getFromPage());
		if (isDebugEnabled) {
	    	S_LOGGER.debug("ConfigTemplates.showPropTempPopup : Exit");
	    }
		return SUCCESS;
	}

	/**
	 * To delete selected config templates
	 * @return
	 * @throws PhrescoException
	 */
	public String delete() throws PhrescoException {
		if (isDebugEnabled) {
	    	S_LOGGER.debug("ConfigTemplates.delete : Entry");
	    }

		try {
			if (isDebugEnabled) {
				if (StringUtils.isEmpty(getCustomerId())) {
					S_LOGGER.warn("ConfigTemplates.delete", "status=\"Bad Request\"", "message=\"Customer Id is empty\"");
					return showErrorPopup(new PhrescoException("Customer Id is empty"), getText(EXCEPTION_CONFIG_TEMP_DELETE));
				}
				S_LOGGER.info("ConfigTemplates.delete", "customerId=" + "\"" + getCustomerId() + "\"");
			}
			String[] configIds = getHttpRequest().getParameterValues(REQ_CONFIG_ID);
			if (ArrayUtils.isNotEmpty(configIds)) {
				if (isDebugEnabled) {
					S_LOGGER.info("ConfigTemplates.delete", "configIds=" + "\"" + configIds.toString() + "\"");
				}
				for (String configid : configIds) {
					getServiceManager().deleteConfigTemp(configid, getCustomerId());
				}
				addActionMessage(getText(CONFIGTEMPLATE_DELETED));
			}
		} catch (PhrescoException e) {
		    return showErrorPopup(e, getText(EXCEPTION_CONFIG_TEMP_DELETE));
		}
		if (isDebugEnabled) {
	    	S_LOGGER.debug("ConfigTemplates.delete : Exit");
	    }

		return list();
	}
	
	/**
	 * To validate mandatory fields
	 * @return
	 * @throws PhrescoException
	 */
	public String validateForm() throws PhrescoException {
		if (isDebugEnabled) {
	    	S_LOGGER.debug("ConfigTemplates.validateForm : Entry");
	    }
		
		boolean isError = false;
		if (!DEFAULT_CUSTOMER_NAME.equalsIgnoreCase(customerId) && EDIT.equalsIgnoreCase(fromPage)) {
			//Empty validation for applies to technology
			if (CollectionUtils.isEmpty(getAppliesTo())) {
				setApplyError(getText(KEY_I18N_ERR_APPLIES_EMPTY ));
				isError = true;
			}
			
			return SUCCESS;
		}
		
		if (StringUtils.isEmpty(getName())) {//Empty validation for name
			setNameError(getText(KEY_I18N_ERR_NAME_EMPTY ));
			isError = true;
		} else if (ADD.equals(getFromPage()) || (!getName().equals(getOldName()))) {
			// to check duplication of name
			List<SettingsTemplate> configTemplates = getServiceManager().getConfigTemplates(getCustomerId());
			if (CollectionUtils.isNotEmpty(configTemplates)) { //TODO: this should handled by query
				for (SettingsTemplate configTemplate : configTemplates) {
					if (configTemplate.getName().equalsIgnoreCase(getName())) {
						setNameError(getText(KEY_I18N_ERR_NAME_ALREADY_EXIST));
			    		isError = true;
						break;
					}
				}
			}
		}
		
		if ((StringUtils.isNotEmpty(getDispName())) && (ADD.equals(getFromPage()))) {//Empty validation for dispName
			// to check duplication of dispName
			List<SettingsTemplate> configTemplates = getServiceManager().getConfigTemplates(getCustomerId());
			if (CollectionUtils.isNotEmpty(configTemplates)) { 
				for (SettingsTemplate configTemplate : configTemplates) {
					if(StringUtils.isNotEmpty(configTemplate.getDisplayName())) {
						if (configTemplate.getDisplayName().equalsIgnoreCase(getDispName())) {
							setDispError(getText(KEY_I18N_ERR_DISPLAYNAME_ALREADY_EXIST));
				    		isError = true;
							break;
						}
					}
				}
			}
		}
		
		//Empty validation for applies to technology
		if (CollectionUtils.isEmpty(getAppliesTo())) {
			setApplyError(getText(KEY_I18N_ERR_APPLIES_EMPTY ));
			isError = true;
		}
		
		if (isError) {
            setErrorFound(true);
        }
		
		if (isDebugEnabled) {
	    	S_LOGGER.debug("ConfigTemplates.validateForm : Exit");
	    }

		return SUCCESS;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNameError() {
		return nameError;
	}

	public void setNameError(String nameError) {
		this.nameError = nameError;
	}

	public String getApplyError() {
		return applyError;
	}

	public void setApplyError(String applyError) {
		this.applyError = applyError;
	}

	public boolean isErrorFound() {
		return errorFound;
	}

	public void setErrorFound(boolean errorFound) {
		this.errorFound = errorFound;
	}
	
	public String getCustomerId() {
		return customerId;
	}

	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public List<String> getAppliesTo() {
		return appliesTo;
	}

	public void setAppliesTo(List<String> appliesTo) {
		this.appliesTo = appliesTo;
	}

	public String getConfigId() {
		return configId;
	}

	public void setConfigId(String configId) {
		this.configId = configId;
	}

	public void setFromPage(String fromPage) {
		this.fromPage = fromPage;
	}

	public String getFromPage() {
		return fromPage;
	}

	public void setOldName(String oldName) {
		this.oldName = oldName;
	}

	public String getOldName() {
		return oldName;
	}

	public boolean getDefaultCustProp() {
		return defaultCustProp;
	}

	public void setDefaultCustProp(boolean defaultCustProp) {
		this.defaultCustProp = defaultCustProp;
	}

	public String getDispName() {
		return dispName;
	}

	public void setDispName(String dispName) {
		this.dispName = dispName;
	}

	public String getDispError() {
		return dispError;
	}

	public void setDispError(String dispError) {
		this.dispError = dispError;
	}

	public List<PropertyTemplate> getPropTemps() {
		return propTemps;
	}

	public void setPropTemps(List<PropertyTemplate> propTemps) {
		this.propTemps = propTemps;
	}

	public void setPropTempKey(String propTempKey) {
		this.propTempKey = propTempKey;
	}

	public String getPropTempKey() {
		return propTempKey;
	}

	public void setFavourite(boolean favourite) {
		this.favourite = favourite;
	}

	public boolean isFavourite() {
		return favourite;
	}

	public void setSystem(boolean system) {
		this.system = system;
	}

	public boolean isSystem() {
		return system;
	}

	public void setEnvSpecific(boolean envSpecific) {
		this.envSpecific = envSpecific;
	}

	public boolean isEnvSpecific() {
		return envSpecific;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public String getDefaultValue() {
		return defaultValue;
	}
}