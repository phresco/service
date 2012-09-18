/*
 * ###
 * Phresco Service Implemenation
 * 
 * Copyright (C) 1999 - 2012 Photon Infotech Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ###
 */

package com.photon.phresco.service.rest.api;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.data.document.mongodb.query.Criteria;
import org.springframework.data.document.mongodb.query.Query;
import org.springframework.stereotype.Component;

import com.photon.phresco.exception.PhrescoException;
import com.photon.phresco.exception.PhrescoWebServiceException;
import com.photon.phresco.model.ApplicationType;
import com.photon.phresco.model.ArchetypeInfo;
import com.photon.phresco.model.Database;
import com.photon.phresco.model.DownloadInfo;
import com.photon.phresco.model.Module;
import com.photon.phresco.model.ModuleGroup;
import com.photon.phresco.model.ProjectInfo;
import com.photon.phresco.model.Server;
import com.photon.phresco.model.SettingsTemplate;
import com.photon.phresco.model.Technology;
import com.photon.phresco.model.WebService;
import com.photon.phresco.service.api.Converter;
import com.photon.phresco.service.api.DbService;
import com.photon.phresco.service.api.PhrescoServerFactory;
import com.photon.phresco.service.api.RepositoryManager;
import com.photon.phresco.service.converters.ConvertersFactory;
import com.photon.phresco.service.dao.ApplicationTypeDAO;
import com.photon.phresco.service.model.ArtifactInfo;
import com.photon.phresco.service.util.ServerUtil;
import com.photon.phresco.util.FileUtil;
import com.photon.phresco.util.ServiceConstants;
import com.sun.jersey.multipart.BodyPart;
import com.sun.jersey.multipart.BodyPartEntity;
import com.sun.jersey.multipart.MultiPart;
import com.sun.jersey.multipart.MultiPartMediaTypes;

@Component
@Path(ServiceConstants.REST_API_COMPONENT)
public class ComponentService extends DbService implements ServiceConstants {
	
	private static final Logger S_LOGGER= Logger.getLogger(ComponentService.class);
	private static Boolean isDebugEnabled = S_LOGGER.isDebugEnabled();
	private static RepositoryManager repositoryManager;
	
	public ComponentService() throws PhrescoException {
		super();
		PhrescoServerFactory.initialize();
		repositoryManager = PhrescoServerFactory.getRepositoryManager();
    }
	
	/**
	 * Returns the list of apptypes
	 * @return
	 * @throws PhrescoException 
	 */
	@GET
	@Path (REST_API_APPTYPES)
	@Produces (MediaType.APPLICATION_JSON)
	public Response findAppTypes(@QueryParam(REST_QUERY_CUSTOMERID) String customerId) throws PhrescoException {
	    if (isDebugEnabled) {
	        S_LOGGER.debug("Entered into ComponentService.findAppTypes()");
	    }
		
		try {
			List<ApplicationType> applicationTypes = new ArrayList<ApplicationType>();
			List<ApplicationTypeDAO> appDAOList = new ArrayList<ApplicationTypeDAO>();
			if(!customerId.equals(DEFAULT_CUSTOMER_NAME)) {
				appDAOList = mongoOperation.find(APPTYPESDAO_COLLECTION_NAME, 
							new Query(Criteria.where(REST_QUERY_CUSTOMERID).is(customerId)), ApplicationTypeDAO.class);
			}
			appDAOList.addAll(mongoOperation.find(APPTYPESDAO_COLLECTION_NAME, 
						new Query(Criteria.where(REST_QUERY_CUSTOMERID).is(DEFAULT_CUSTOMER_NAME)), ApplicationTypeDAO.class));
				
	        Converter<ApplicationTypeDAO, ApplicationType> converter = 
	            (Converter<ApplicationTypeDAO, ApplicationType>) ConvertersFactory.getConverter(ApplicationTypeDAO.class);
	        
	        if (CollectionUtils.isNotEmpty(appDAOList)) {
		        for (ApplicationTypeDAO applicationTypeDAO : appDAOList) {
		            applicationTypes.add(converter.convertDAOToObject(applicationTypeDAO, mongoOperation));
		        }
	        }

	        return Response.status(Response.Status.OK).entity(applicationTypes).build();
		}
		catch (Exception e) {
			throw new PhrescoWebServiceException(e, EX_PHEX00005, APPTYPES_COLLECTION_NAME);
		}
	}
	
	/**
	 * Creates the list of apptypes
	 * @param appTypes
	 * @return 
	 * @throws PhrescoException 
	 */
	@POST
	@Consumes (MediaType.APPLICATION_JSON)
	@Path (REST_API_APPTYPES)
	public Response createAppTypes(List<ApplicationType> appTypes) throws PhrescoException {
	    if (isDebugEnabled) {
	        S_LOGGER.debug("Entered into ComponentService.createAppTypes(List<ApplicationType> appTypes)");
        }
		
		try {
			Converter<ApplicationTypeDAO, ApplicationType> converter = 
			    (Converter<ApplicationTypeDAO, ApplicationType>) ConvertersFactory.getConverter(ApplicationTypeDAO.class);			
			List<ApplicationTypeDAO> appTypeDAOs = new ArrayList<ApplicationTypeDAO>();
			for (ApplicationType applicationType : appTypes) {
				appTypeDAOs.add(converter.convertObjectToDAO(applicationType));
			}
			mongoOperation.insertList(APPTYPESDAO_COLLECTION_NAME , appTypeDAOs);
		} catch (Exception e) {
			throw new PhrescoWebServiceException(e, EX_PHEX00006, INSERT);
		}
		
		return Response.status(Response.Status.CREATED).build();
	}
	
	/**
	 * Updates the list of apptypes
	 * @param appTypes
	 * @return
	 */
	@PUT
	@Consumes (MediaType.APPLICATION_JSON)
	@Produces (MediaType.APPLICATION_JSON)
	@Path (REST_API_APPTYPES)
	public Response updateAppTypes(List<ApplicationType> appTypes) {
	    if (isDebugEnabled) {
	        S_LOGGER.debug("Entered into ComponentService.updateAppTypes(List<ApplicationType> appTypes)");
	    }
		
	    try {
            Converter<ApplicationTypeDAO, ApplicationType> converter = 
                (Converter<ApplicationTypeDAO, ApplicationType>) ConvertersFactory.getConverter(ApplicationTypeDAO.class);          
            List<ApplicationTypeDAO> appTypeDAOs = new ArrayList<ApplicationTypeDAO>();
            for (ApplicationType applicationType : appTypes) {
                appTypeDAOs.add(converter.convertObjectToDAO(applicationType));
            }
            mongoOperation.insertList(APPTYPESDAO_COLLECTION_NAME , appTypeDAOs);
        } catch (Exception e) {
            throw new PhrescoWebServiceException(e, EX_PHEX00006, INSERT);
        }
		
		return Response.status(Response.Status.OK).entity(appTypes).build();
	}
	
	/**
	 * Deletes the list of apptypes
	 * @param appTypes
	 * @throws PhrescoException 
	 */
	@DELETE
	@Path (REST_API_APPTYPES)
	@Produces (MediaType.TEXT_PLAIN)
	public void deleteAppTypes(List<ApplicationType> appTypes) throws PhrescoException {
	    if (isDebugEnabled) {
	        S_LOGGER.debug("Entered into ComponentService.deleteAppTypes(List<ApplicationType> appTypes)");
        }
		
		PhrescoException phrescoException = new PhrescoException(EX_PHEX00001);
		S_LOGGER.error("PhrescoException Is" + phrescoException.getErrorMessage());
		throw phrescoException;
	}
	
	/**
	 * Get the apptype by id for the given parameter
	 * @param id
	 * @return
	 */
	@GET
	@Produces (MediaType.APPLICATION_JSON)
	@Path (REST_API_APPTYPES + REST_API_PATH_ID)
	public Response getApptype(@PathParam(REST_API_PATH_PARAM_ID) String id) {
	    if (isDebugEnabled) {
	        S_LOGGER.debug("Entered into ComponentService.getApptype(String id)" + id);
	    }
		
		try {
			ApplicationType appType = mongoOperation.findOne(APPTYPESDAO_COLLECTION_NAME, 
			        new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(id)), ApplicationType.class);
			if(appType != null) {
				return Response.status(Response.Status.OK).entity(appType).build();
			} 
		} catch (Exception e) {
			throw new PhrescoWebServiceException(e, EX_PHEX00005, APPTYPES_COLLECTION_NAME);
		}
		
		return Response.status(Response.Status.NO_CONTENT).build();
	}
	
	/**
	 * Updates the list of apptypes
	 * @param appTypes
	 * @return
	 */
	@PUT
	@Consumes (MediaType.APPLICATION_JSON)
	@Produces (MediaType.APPLICATION_JSON)
	@Path (REST_API_APPTYPES + REST_API_PATH_ID)
	public Response updateAppType(@PathParam(REST_API_PATH_PARAM_ID) String id , ApplicationType appType) throws PhrescoException {
	    if (isDebugEnabled) {
	        S_LOGGER.debug("Entered into ComponentService.updateAppType(String id , ApplicationType appType)" + id);
	    }
		
		try {
		    Converter<ApplicationTypeDAO, ApplicationType> converter = 
	            (Converter<ApplicationTypeDAO, ApplicationType>) ConvertersFactory.getConverter(ApplicationTypeDAO.class);          
	        ApplicationTypeDAO appTypeDAO = converter.convertObjectToDAO(appType);
	        mongoOperation.save(APPTYPESDAO_COLLECTION_NAME, appTypeDAO);
		} catch (Exception e) {
            throw new PhrescoWebServiceException(e, EX_PHEX00005, APPTYPES_COLLECTION_NAME);
        }
		
		return Response.status(Response.Status.OK).entity(appType).build();
	}
	
	/**
	 * Deletes the apptype by id for the given parameter
	 * @param id
	 * @return 
	 */
	@DELETE
	@Path (REST_API_APPTYPES + REST_API_PATH_ID)
	public Response deleteAppType(@PathParam(REST_API_PATH_PARAM_ID) String id) {
	    if (isDebugEnabled) {
	        S_LOGGER.debug("Entered into ComponentService.deleteAppType(String id)" + id);
	    }
		
		try {
			mongoOperation.remove(APPTYPESDAO_COLLECTION_NAME, 
			        new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(id)), ApplicationType.class);
			mongoOperation.remove(ServiceConstants.TECHNOLOGIES_COLLECTION_NAME, 
			        new Query(Criteria.where(REST_API_FIELD_APPID).is(id)), Technology.class);
		} catch (Exception e) {
			throw new PhrescoWebServiceException(e, EX_PHEX00006, DELETE);
		}
		
		return Response.status(Response.Status.OK).build();
	}
	
	/**
	 * Returns the list of settings
	 * @return
	 */
	@GET
	@Path (REST_API_SETTINGS)
	@Produces (MediaType.APPLICATION_JSON)
	public Response findSettings(@QueryParam(REST_QUERY_CUSTOMERID) String customerId) {
	    if (isDebugEnabled) {
	        S_LOGGER.debug("Entered into ComponentService.findSettings()" + customerId);
	    }
		
		try {
			List<SettingsTemplate> settingsList = new ArrayList<SettingsTemplate>();
			settingsList.addAll(mongoOperation.find(SETTINGS_COLLECTION_NAME, 
                    new Query(Criteria.where(REST_QUERY_CUSTOMERID).is(DEFAULT_CUSTOMER_NAME)), SettingsTemplate.class));
			if(!customerId.equals(DEFAULT_CUSTOMER_NAME)) {
			    settingsList.addAll(mongoOperation.find(SETTINGS_COLLECTION_NAME, 
			            new Query(Criteria.where(REST_QUERY_CUSTOMERID).is(customerId)), SettingsTemplate.class));
			}
			return Response.status(Response.Status.NO_CONTENT).entity(settingsList).build();
		} catch (Exception e) {
			throw new PhrescoWebServiceException(e, EX_PHEX00005, SETTINGS_COLLECTION_NAME);
		}
	}
	
	/**
	 * Creates the list of settings
	 * @param settings
	 * @return 
	 */
	@POST
	@Consumes (MediaType.APPLICATION_JSON)
	@Path (REST_API_SETTINGS)
	public Response createSettings(List<SettingsTemplate> settings) {
		if (isDebugEnabled) {
		    S_LOGGER.debug("Entered into ComponentService.createSettings(List<SettingsTemplate> settings)");
		}
		
		try {
			mongoOperation.insertList(SETTINGS_COLLECTION_NAME , settings);
		} catch (Exception e) {
			throw new PhrescoWebServiceException(e, EX_PHEX00006, INSERT);
		}
		
		return Response.status(Response.Status.CREATED).build();
	}
	
	/**
	 * Updates the list of settings
	 * @param settings
	 * @return
	 */
	@PUT
	@Consumes (MediaType.APPLICATION_JSON)
	@Produces (MediaType.APPLICATION_JSON)
	@Path (REST_API_SETTINGS)
	public Response updateSettings(List<SettingsTemplate> settings) {
	    if (isDebugEnabled) {
	        S_LOGGER.debug("Entered into ComponentService.updateSettings(List<SettingsTemplate> settings)");
	    }
		
		try {
			for (SettingsTemplate settingTemplate : settings) {
				SettingsTemplate settingTemplateInfo = mongoOperation.findOne(SETTINGS_COLLECTION_NAME , 
				        new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(settingTemplate.getId())), SettingsTemplate.class);
				if (settingTemplateInfo != null) {
					mongoOperation.save(SETTINGS_COLLECTION_NAME, settingTemplate);
				}
			}
		} catch (Exception e) {
			throw new PhrescoWebServiceException(e, EX_PHEX00006, UPDATE);
		}
		
		return Response.status(Response.Status.OK).entity(settings).build();
	}
	
	/**
	 * Deletes the list of settings
	 * @param settings
	 * @throws PhrescoException 
	 */
	@DELETE
	@Path (REST_API_SETTINGS)
	public void deleteSettings(List<SettingsTemplate> settings) throws PhrescoException {
	    if (isDebugEnabled) {
	        S_LOGGER.debug("Entered into ComponentService.updateSettings(List<SettingsTemplate> settings)");
	    }
		
		PhrescoException phrescoException = new PhrescoException(EX_PHEX00001);
		S_LOGGER.error("PhrescoException Is" + phrescoException.getErrorMessage());
		throw phrescoException;
		
	}
	
	/**
	 * Get the settingTemplate by id 
	 * @param id
	 * @return
	 */
	@GET
	@Produces (MediaType.APPLICATION_JSON)
	@Path (REST_API_SETTINGS + REST_API_PATH_ID)
	public Response getSettingsTemplate(@PathParam(REST_API_PATH_PARAM_ID) String id) {
	    if (isDebugEnabled) {
	        S_LOGGER.debug("Entered into ComponentService.getSettingsTemplate(String id)" + id);
	    }
		
		try {
			SettingsTemplate settingTemplate = mongoOperation.findOne(SETTINGS_COLLECTION_NAME, 
			        new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(id)), SettingsTemplate.class); 
			if (settingTemplate != null) {
				return Response.status(Response.Status.OK).entity(settingTemplate).build();
			}
		} catch (Exception e) {
			throw new PhrescoWebServiceException(e, EX_PHEX00005, SETTINGS_COLLECTION_NAME);
		}
		
		return Response.status(Response.Status.OK).build();
	}
	
	/**
	 * Updates the list of setting
	 * @param settings
	 * @return
	 */
	@PUT
	@Consumes (MediaType.APPLICATION_JSON)
	@Produces (MediaType.APPLICATION_JSON)
	@Path (REST_API_SETTINGS + REST_API_PATH_ID)
	public Response updateSetting(@PathParam(REST_API_PATH_PARAM_ID) String id , SettingsTemplate settingsTemplate) {
	    if (isDebugEnabled) {
	        S_LOGGER.debug("Entered into ComponentService.updateAppType(String id , SettingsTemplate settingsTemplate)" + id);
	    }
		
		try {
			if (id.equals(settingsTemplate.getId())) {
				mongoOperation.save(SETTINGS_COLLECTION_NAME, settingsTemplate);
				return Response.status(Response.Status.OK).entity(settingsTemplate).build();
			}
		} catch (Exception e) {
			throw new PhrescoWebServiceException(e, EX_PHEX00006, UPDATE);
		}
		
		return Response.status(Response.Status.OK).entity(settingsTemplate).build();
	}
	
	/**
	 * Deletes the settingsTemplate by id for the given parameter
	 * @param id
	 * @return 
	 */
	@DELETE
	@Path (REST_API_SETTINGS + REST_API_PATH_ID)
	public Response deleteSettingsTemplate(@PathParam(REST_API_PATH_PARAM_ID) String id) {
	    if (isDebugEnabled) {
	        S_LOGGER.debug("Entered into ComponentService.deleteSettingsTemplate(String id)" + id);
	    }
		
		try {
			mongoOperation.remove(SETTINGS_COLLECTION_NAME, 
			        new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(id)), SettingsTemplate.class);
		} catch (Exception e) {
			throw new PhrescoWebServiceException(e, EX_PHEX00006, DELETE);
		}
		
		return Response.status(Response.Status.OK).build();
	}
	
	/**
	 * Returns the list of modules
	 * @return
	 */
	@GET
	@Path (REST_API_MODULES)
	@Produces (MediaType.APPLICATION_JSON)
	public Response findModules(@QueryParam(REST_QUERY_TYPE) String type, @QueryParam(REST_QUERY_CUSTOMERID) String customerId,
			@QueryParam(REST_QUERY_TECHID) String techId) {
	    if (isDebugEnabled) {
	        S_LOGGER.debug("Entered into ComponentService.findModules()" + type);
	    }
		
		try {
			List<ModuleGroup> foundModules = new ArrayList<ModuleGroup>();
			if(StringUtils.isEmpty(techId)) {
				if(!customerId.equals(DEFAULT_CUSTOMER_NAME)) {
					foundModules = mongoOperation.find(MODULES_COLLECTION_NAME,
								new Query(Criteria.where(REST_QUERY_CUSTOMERID).is(customerId)), ModuleGroup.class);
				}
				foundModules.addAll(mongoOperation.find(MODULES_COLLECTION_NAME,
							new Query(Criteria.where(REST_QUERY_CUSTOMERID).is(DEFAULT_CUSTOMER_NAME)), ModuleGroup.class));
	
				return Response.status(Response.Status.OK).entity(foundModules).build();
			}

			if(StringUtils.isNotEmpty(customerId) && type.equals(REST_QUERY_TYPE_MODULE)) {
				if (!customerId.equals(DEFAULT_CUSTOMER_NAME)) {
					foundModules = mongoOperation.find(MODULES_COLLECTION_NAME, new Query(Criteria.where(REST_QUERY_CUSTOMERID).is(customerId)
								.and(REST_QUERY_TYPE).is(type).and(REST_QUERY_TECHID).is(techId)), ModuleGroup.class);
				}
				foundModules.addAll(mongoOperation.find(MODULES_COLLECTION_NAME, new Query(Criteria.where(REST_QUERY_CUSTOMERID).is(DEFAULT_CUSTOMER_NAME)
							.and(REST_QUERY_TYPE).is(type).and(REST_QUERY_TECHID).is(techId)), ModuleGroup.class));
				
				return Response.status(Response.Status.OK).entity(foundModules).build();
			}

			if(StringUtils.isNotEmpty(customerId) && type.equals(REST_QUERY_TYPE_JS)) {
			    if (!customerId.equals(DEFAULT_CUSTOMER_NAME)) {
					foundModules = mongoOperation.find(MODULES_COLLECTION_NAME, new Query(Criteria.where(REST_QUERY_CUSTOMERID).is(customerId)
								.and(REST_QUERY_TYPE).is(type).and(REST_QUERY_TECHID).is(techId)), ModuleGroup.class);
				}
				foundModules.addAll(mongoOperation.find(MODULES_COLLECTION_NAME, new Query(Criteria.where(REST_QUERY_CUSTOMERID).is(DEFAULT_CUSTOMER_NAME)
							.and(REST_QUERY_TYPE).is(type).and(REST_QUERY_TECHID).is(techId)), ModuleGroup.class));
				
				return Response.status(Response.Status.OK).entity(foundModules).build();
			}

		} catch(Exception e) {
			throw new PhrescoWebServiceException(e, EX_PHEX00005, MODULES_COLLECTION_NAME);
		}
		
		return Response.status(Response.Status.BAD_REQUEST).build();
	}
	
	
	/**
     * Creates the list of modules
     * @param modules
     * @return 
     * @throws PhrescoException 
     */
    @POST
    @Consumes (MultiPartMediaTypes.MULTIPART_MIXED)
    @Path (REST_API_MODULES)
    public Response createModules(MultiPart moduleInfo) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ComponentService.createModules(List<ModuleGroup> modules)");
        }
        
        ModuleGroup moduleGroup = null;
        BodyPartEntity bodyPartEntity = null;
        File moduleFile = null;
        List<BodyPart> bodyParts = moduleInfo.getBodyParts();
       
        if (CollectionUtils.isNotEmpty(bodyParts)) {
            for (BodyPart bodyPart : bodyParts) {
                if (bodyPart.getMediaType().equals(MediaType.APPLICATION_JSON_TYPE)) {
                    moduleGroup = new ModuleGroup();
                    moduleGroup = bodyPart.getEntityAs(ModuleGroup.class);
                } else {
                    bodyPartEntity = (BodyPartEntity) bodyPart.getEntity();
                }
            }
        }
        
        if (bodyPartEntity != null) {
            moduleFile = ServerUtil.writeFileFromStream(bodyPartEntity.getInputStream(), null);
        }
        
        Module module = moduleGroup.getVersions().get(0);
        ArchetypeInfo archetypeInfo = new ArchetypeInfo(module.getGroupId(), 
        		module.getArtifactId(), module.getVersion(), module.getContentType());
        
        boolean uploadBinary = uploadBinary(archetypeInfo, moduleFile, moduleGroup.getCustomerId());
        
        if (uploadBinary) {
        	ModuleGroup foundModuleGroup = mongoOperation.findOne(MODULES_COLLECTION_NAME, 
        			new Query(Criteria.where("name").is(moduleGroup.getName())), ModuleGroup.class);
        	if (foundModuleGroup != null) {
        		List<Module> versions = foundModuleGroup.getVersions();
        		versions.add(module);
        		foundModuleGroup.setVersions(versions);
        		mongoOperation.save(MODULES_COLLECTION_NAME, foundModuleGroup);
        	} else {
        		mongoOperation.save(MODULES_COLLECTION_NAME, moduleGroup);
        	}
        }
        FileUtil.delete(moduleFile);
       
        return Response.status(Response.Status.CREATED).build();
    }
	
	/**
	 * Updates the list of modules
	 * @param modules
	 * @return
	 */
	@PUT
	@Consumes (MediaType.APPLICATION_JSON)
	@Produces (MediaType.APPLICATION_JSON)
	@Path (REST_API_MODULES)
	public Response updateModules(List<ModuleGroup> modules) {
	    if (isDebugEnabled) {
	        S_LOGGER.debug("Entered into ComponentService.updateModules(List<ModuleGroup> modules)");
	    }
		
		try {
			for (ModuleGroup moduleGroup : modules) {
				ModuleGroup module = mongoOperation.findOne(MODULES_COLLECTION_NAME , 
				        new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(moduleGroup.getId())), ModuleGroup.class);
				if (module != null) {
					mongoOperation.save(MODULES_COLLECTION_NAME, moduleGroup);
				}
			}
		} catch (Exception e) {
			throw new PhrescoWebServiceException(e, EX_PHEX00006, UPDATE);
		}
		
		return Response.status(Response.Status.OK).entity(modules).build();
	}
	
	/**
	 * Deletes the list of modules
	 * @param modules
	 * @throws PhrescoException 
	 */
	@DELETE
	@Path (REST_API_MODULES)
	public void deleteModules(List<ModuleGroup> modules) throws PhrescoException {
	    if (isDebugEnabled) {
	        S_LOGGER.debug("Entered into ComponentService.deleteModules(List<ModuleGroup> modules)");
	    }
		
		PhrescoException phrescoException = new PhrescoException(EX_PHEX00001);
		S_LOGGER.error("PhrescoException Is" + phrescoException.getErrorMessage());
		throw phrescoException;
	}
	
	/**
	 * Get the module by id for the given parameter
	 * @param id
	 * @return
	 */
	@GET
	@Produces (MediaType.APPLICATION_JSON)
	@Path (REST_API_MODULES + REST_API_PATH_ID)
	public Response getModule(@PathParam(REST_API_PATH_PARAM_ID) String id) {
	    if (isDebugEnabled) {
	        S_LOGGER.debug("Entered into ComponentService.getModule(String id)" + id);
	    }
		
		try {
			ModuleGroup module = mongoOperation.findOne(MODULES_COLLECTION_NAME, 
			        new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(id)), ModuleGroup.class);
			if (module != null) {
				return  Response.status(Response.Status.OK).entity(module).build();
			} 
		} catch (Exception e) {
			throw new PhrescoWebServiceException(e, EX_PHEX00005, MODULES_COLLECTION_NAME);
		}
		
		return Response.status(Response.Status.NO_CONTENT).build();
	}
	
	/**
	 * Updates the module given by the parameter
	 * @param id
	 * @param module
	 * @return
	 */
	@PUT
	@Consumes (MediaType.APPLICATION_JSON)
	@Produces (MediaType.APPLICATION_JSON)
	@Path (REST_API_MODULES + REST_API_PATH_ID)
	public Response updatemodule(@PathParam(REST_API_PATH_PARAM_ID) String id , ModuleGroup module) {
	    if (isDebugEnabled) {
	        S_LOGGER.debug("Entered into ComponentService.updatemodule(String id , ModuleGroup module)" + id);
	    }
		
		try {
			if (id.equals(module.getId())) {
				mongoOperation.save(MODULES_COLLECTION_NAME, module);
				return  Response.status(Response.Status.OK).entity(module).build();
			}
		} catch (Exception e) {
			throw new PhrescoWebServiceException(e, EX_PHEX00006, UPDATE);
		}
		
		return Response.status(Response.Status.NO_CONTENT).entity(module).build();
	}
	
	/**
	 * Deletes the module by id for the given parameter
	 * @param id
	 * @return 
	 */
	@DELETE
	@Path (REST_API_MODULES + REST_API_PATH_ID)
	public Response deleteModules(@PathParam(REST_API_PATH_PARAM_ID) String id) {
	    if (isDebugEnabled) {
	        S_LOGGER.debug("Entered into ComponentService.deleteModules(String id)" + id);
	    }
		
		try {
			mongoOperation.remove(MODULES_COLLECTION_NAME, 
			        new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(id)), ModuleGroup.class);
		} catch (Exception e) {
			throw new PhrescoWebServiceException(e, EX_PHEX00006, DELETE);
		}
		
		return Response.status(Response.Status.OK).build();
	}
	
	/**
	 * Returns the list of pilots
	 * @return
	 */
	@GET
	@Path (REST_API_PILOTS)
	@Produces (MediaType.APPLICATION_JSON)
	public Response findPilots(@QueryParam(REST_QUERY_CUSTOMERID) String customerId) {
	    if (isDebugEnabled) {
	        S_LOGGER.debug("Entered into ComponentService.findPilots()" + customerId);
	    }
	    List<ProjectInfo> pilotsList = new ArrayList<ProjectInfo>();
		try {
		    if(!customerId.equals(DEFAULT_CUSTOMER_NAME)) {
    			pilotsList = mongoOperation.find(PILOTS_COLLECTION_NAME ,
    			        new Query(Criteria.where(REST_QUERY_CUSTOMERID).is(customerId)), ProjectInfo.class);
		    }
			pilotsList.addAll(mongoOperation.find(PILOTS_COLLECTION_NAME ,
                    new Query(Criteria.where(REST_QUERY_CUSTOMERID).is(DEFAULT_CUSTOMER_NAME)), ProjectInfo.class));
			return Response.status(Response.Status.OK).entity(pilotsList).build();
		} catch (Exception e) {
			throw new PhrescoWebServiceException(e, EX_PHEX00005, PILOTS_COLLECTION_NAME);
		}
	}
	
	/**
     * Creates the list of pilots
     * @param projectInfos
     * @return 
     * @throws PhrescoException 
     */
    @POST
    @Consumes (MultiPartMediaTypes.MULTIPART_MIXED)
    @Path (REST_API_PILOTS)
    public Response createPilots(MultiPart pilotInfo) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ComponentService.createPilots(List<ProjectInfo> projectInfos)");
        }
        
        ProjectInfo projectInfo = null;
        BodyPartEntity bodyPartEntity = null;
        File pilotFile = null;
        
        List<BodyPart> bodyParts = pilotInfo.getBodyParts();
        if(CollectionUtils.isNotEmpty(bodyParts)) {
            for (BodyPart bodyPart : bodyParts) {
                if (bodyPart.getMediaType().equals(MediaType.APPLICATION_JSON_TYPE)) {
                    projectInfo = new ProjectInfo();
                    projectInfo = bodyPart.getEntityAs(ProjectInfo.class);
                } else {
                    bodyPartEntity = (BodyPartEntity) bodyPart.getEntity();
                }
            }
        }
        
        if(bodyPartEntity != null) {
            pilotFile = ServerUtil.writeFileFromStream(bodyPartEntity.getInputStream(), null);
        }
        
        boolean uploadBinary = uploadBinary(projectInfo.getArchetypeInfo(), 
                pilotFile, projectInfo.getCustomerId());
        if(uploadBinary) {
            projectInfo.setProjectURL(createContentURL(projectInfo.getArchetypeInfo()));
            mongoOperation.save(PILOTS_COLLECTION_NAME, projectInfo);
        }
        FileUtil.delete(pilotFile);
        
        return Response.status(Response.Status.CREATED).build();
    }
    
    private String createContentURL(ArchetypeInfo archetypeInfo) {
        String groupId = archetypeInfo.getGroupId().replace(".", "/");
        return groupId + "/" + archetypeInfo.getArtifactId() + "/" + archetypeInfo.getVersion() + "/" +
                archetypeInfo.getArtifactId() + "-" + archetypeInfo.getVersion() + "." + archetypeInfo.getPackaging();
    }
	
	/**
	 * Updates the list of pilots
	 * @param projectInfos
	 * @return
	 */
	@PUT
	@Consumes (MediaType.APPLICATION_JSON)
	@Produces (MediaType.APPLICATION_JSON)
	@Path (REST_API_PILOTS)
	public Response updatePilots(List<ProjectInfo> pilots) {
	    if (isDebugEnabled) {
	        S_LOGGER.debug("Entered into ComponentService.updatePilots(List<ProjectInfo> pilots)");
	    }
		
		try {
			for (ProjectInfo pilot : pilots) {
				ProjectInfo projectInfo = mongoOperation.findOne(PILOTS_COLLECTION_NAME , 
				        new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(pilot.getId())), ProjectInfo.class);
				if (projectInfo != null) {
					mongoOperation.save(PILOTS_COLLECTION_NAME, pilot);
				}
			}
		} catch (Exception e) {
			throw new PhrescoWebServiceException(e, EX_PHEX00006, UPDATE);
		}
		
		return Response.status(Response.Status.OK).entity(pilots).build();
	}
	
	/**
	 * Deletes the list of pilots
	 * @param pilots
	 * @throws PhrescoException 
	 */
	@DELETE
	@Path (REST_API_PILOTS)
	public void deletePilots(List<ProjectInfo> pilots) throws PhrescoException {
	    if (isDebugEnabled) {
	        S_LOGGER.debug("Entered into ComponentService.deletePilots(List<ProjectInfo> pilots)");
	    }
		
		PhrescoException phrescoException = new PhrescoException(EX_PHEX00001);
		S_LOGGER.error("PhrescoException Is" + phrescoException.getErrorMessage());
		throw phrescoException;
	}
	
	/**
	 * Get the pilot by id for the given parameter
	 * @param id
	 * @return
	 */
	@GET
	@Produces (MediaType.APPLICATION_JSON)
	@Path (REST_API_PILOTS + REST_API_PATH_ID)
	public Response getPilot(@PathParam(REST_API_PATH_PARAM_ID) String id) {
	    if (isDebugEnabled) {
	        S_LOGGER.debug("Entered into ComponentService.getPilot(String id)" + id);
	    }
		
		try {
			ProjectInfo projectInfo = mongoOperation.findOne(PILOTS_COLLECTION_NAME, 
			        new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(id)), ProjectInfo.class);
			if (projectInfo != null) {
				return Response.status(Response.Status.OK).entity(projectInfo).build();
			}
		} catch (Exception e) {
			throw new PhrescoWebServiceException(e, EX_PHEX00005, PILOTS_COLLECTION_NAME);
		}
		
		return Response.status(Response.Status.OK).build();
	}
	
	/**
	 * Updates the pilot given by the parameter
	 * @param id
	 * @param pilot
	 * @return
	 */
	@PUT
	@Consumes (MediaType.APPLICATION_JSON)
	@Produces (MediaType.APPLICATION_JSON)
	@Path (REST_API_PILOTS + REST_API_PATH_ID)
	public Response updatePilot(@PathParam(REST_API_PATH_PARAM_ID) String id , ProjectInfo pilot) {
	    if (isDebugEnabled) {
	        S_LOGGER.debug("Entered into ComponentService.updatePilot(String id, ProjectInfo pilot)" + id); 
	    }
		
		try {
			if (id.equals(pilot.getId())) {
				mongoOperation.save(PILOTS_COLLECTION_NAME, pilot);
				return  Response.status(Response.Status.OK).entity(pilot).build();
			}
		} catch (Exception e) {
			throw new PhrescoWebServiceException(e, EX_PHEX00006, UPDATE);
		}
		
		return Response.status(Response.Status.NO_CONTENT).entity(pilot).build();
	}
	
	/**
	 * Deletes the pilot by id for the given parameter
	 * @param id
	 * @return 
	 */
	@DELETE
	@Path (REST_API_PILOTS + REST_API_PATH_ID)
	public Response deletePilot(@PathParam(REST_API_PATH_PARAM_ID) String id) {
	    if (isDebugEnabled) {
	        S_LOGGER.debug("Entered into ComponentService.deletePilot(String id)" + id);
	    }
		
		try {
			mongoOperation.remove(PILOTS_COLLECTION_NAME, 
			        new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(id)), ProjectInfo.class);
		} catch (Exception e) {
			throw new PhrescoWebServiceException(e, EX_PHEX00006, DELETE);
		}
		
		return Response.status(Response.Status.OK).build();
	}
	
	/**
	 * Returns the list of servers
	 * @return
	 */
	@GET
	@Path (REST_API_SERVERS)
	@Produces (MediaType.APPLICATION_JSON)
	public Response findServers(@QueryParam(REST_QUERY_CUSTOMERID) String customerId) {
	    if (isDebugEnabled) {
	        S_LOGGER.debug("Entered into ComponentService.findServers()" + customerId);
	    }
		
		List<Server> serverList = new ArrayList<Server>();
		try {
			if (!customerId.equals(DEFAULT_CUSTOMER_NAME)) {
				serverList= mongoOperation.find(SERVERS_COLLECTION_NAME, 
				        new Query(Criteria.where (REST_QUERY_CUSTOMERID).is(customerId)), Server.class);
			}
			serverList.addAll(mongoOperation.find(SERVERS_COLLECTION_NAME, 
			        new Query(Criteria.where(REST_QUERY_CUSTOMERID)
			        .is(DEFAULT_CUSTOMER_NAME)), Server.class));
			return Response.status(Response.Status.OK).entity(serverList).build();
		} catch (Exception e) {
			throw new PhrescoWebServiceException(e, EX_PHEX00005, SERVERS_COLLECTION_NAME);
		}
	}
	
	/**
	 * Creates the list of servers
	 * @param servers
	 * @return 
	 */
	@POST
	@Consumes (MediaType.APPLICATION_JSON)
	@Path (REST_API_SERVERS)
	public Response createServers(List<Server> servers) {
	    if (isDebugEnabled) {
	        S_LOGGER.debug("Entered into ComponentService.createServers(List<Server> servers)");
	    }
		
		try {
			mongoOperation.insertList(SERVERS_COLLECTION_NAME , servers);
		} catch (Exception e) {
			throw new PhrescoWebServiceException(e, EX_PHEX00005, INSERT);
		}
		
		return Response.status(Response.Status.CREATED).build();
	}
	
	/**
	 * Updates the list of servers
	 * @param servers
	 * @return
	 */
	@PUT
	@Consumes (MediaType.APPLICATION_JSON)
	@Produces (MediaType.APPLICATION_JSON)
	@Path (REST_API_SERVERS)
	public Response updateServers(List<Server> servers) {
	    if (isDebugEnabled) {
	        S_LOGGER.debug("Entered into ComponentService.updateServers(List<Server> servers)");
	    }
		
		try {
			for (Server server : servers) {
				Server serverInfo = mongoOperation.findOne(SERVERS_COLLECTION_NAME , 
				        new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(server.getId())), Server.class);
				if (serverInfo != null) {
					mongoOperation.save(SERVERS_COLLECTION_NAME , server);
				}
			}
		} catch (Exception e) {
			throw new PhrescoWebServiceException(e, EX_PHEX00006, UPDATE);
		}
		
		return Response.status(Response.Status.OK).entity(servers).build();
	}
	
	/**
	 * Deletes the list of servers
	 * @param servers
	 * @throws PhrescoException 
	 */
	@DELETE
	@Path (REST_API_SERVERS)
	public void deleteServers(List<Server> servers) throws PhrescoException {
	    if (isDebugEnabled) {
	        S_LOGGER.debug("Entered into ComponentService.deleteServers(List<Server> servers)");
	    }
		
		PhrescoException phrescoException = new PhrescoException(EX_PHEX00001);
		S_LOGGER.error("PhrescoException Is" + phrescoException.getErrorMessage());
		throw phrescoException;
	}
	
	/**
	 * Get the server by id for the given parameter
	 * @param id
	 * @return
	 */
	@GET
	@Produces (MediaType.APPLICATION_JSON)
	@Path (REST_API_SERVERS + REST_API_PATH_ID)
	public Response getServer(@PathParam(REST_API_PATH_PARAM_ID) String id) {
	    if (isDebugEnabled) {
	        S_LOGGER.debug("Entered into ComponentService.getServer(String id)" + id);
	    }

	    try {
			Server server = mongoOperation.findOne(SERVERS_COLLECTION_NAME, 
			        new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(id)), Server.class);
			if (server != null ) {
				return  Response.status(Response.Status.OK).entity(server).build();
			} 
		} catch (Exception e) {
			throw new PhrescoWebServiceException(e, EX_PHEX00005, SERVERS_COLLECTION_NAME);
		}
		
		return Response.status(Response.Status.OK).entity(ERROR_MSG_NOT_FOUND).build();
	}
	
	/**
	 * Updates the server given by the parameter
	 * @param id
	 * @param server
	 * @return
	 */
	@PUT
	@Consumes (MediaType.APPLICATION_JSON)
	@Produces (MediaType.APPLICATION_JSON)
	@Path (REST_API_SERVERS + REST_API_PATH_ID)
	public Response updateServer(@PathParam(REST_API_PATH_PARAM_ID) String id , Server server) {
	    if (isDebugEnabled) {
	        S_LOGGER.debug("Entered into ComponentService.updateServer(String id, Server server)" + id);
	    }
		
		try {
			if (id.equals(server.getId())) {
				mongoOperation.save(SERVERS_COLLECTION_NAME, server);
				return Response.status(Response.Status.OK).entity(server).build();
			} 
		} catch (Exception e) {
			throw new PhrescoWebServiceException(e, EX_PHEX00006, UPDATE);
		}
		
		return Response.status(Response.Status.OK).entity(ERROR_MSG_ID_NOT_EQUAL).build();
	}
	
	/**
	 * Deletes the server by id for the given parameter
	 * @param id
	 * @return 
	 */
	@DELETE
	@Path (REST_API_SERVERS + REST_API_PATH_ID)
	public Response deleteServer(@PathParam(REST_API_PATH_PARAM_ID) String id) {
	    if (isDebugEnabled) {
	        S_LOGGER.debug("Entered into ComponentService.deleteServer(String id)" + id);
	    }
		
		try {
			mongoOperation.remove(SERVERS_COLLECTION_NAME, 
			        new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(id)), Server.class);
		} catch (Exception e) {
			throw new PhrescoWebServiceException(e, EX_PHEX00006, DELETE);
		}
		
		return Response.status(Response.Status.OK).build();
	}
	
	/**
	 * Returns the list of databases
	 * @return
	 */
	@GET
	@Path (REST_API_DATABASES)
	@Produces (MediaType.APPLICATION_JSON)
	public Response findDatabases(@QueryParam(REST_QUERY_CUSTOMERID) String customerId) {
	    if (isDebugEnabled) {
	        S_LOGGER.debug("Entered into ComponentService.deleteServer(String id)");
	    }
		
		List<Database> databaseList = new ArrayList<Database>();
		try {
			if (!customerId.equals(DEFAULT_CUSTOMER_NAME)) {
				databaseList= mongoOperation.find(DATABASES_COLLECTION_NAME, 
				        new Query(Criteria.where (REST_QUERY_CUSTOMERID).is(customerId)), Database.class);
			}
			databaseList.addAll(mongoOperation.find(DATABASES_COLLECTION_NAME, 
			        new Query(Criteria.where(REST_QUERY_CUSTOMERID)
			        .is(DEFAULT_CUSTOMER_NAME)), Database.class));
			
			return  Response.status(Response.Status.OK).entity(databaseList).build();
		} catch (Exception e) {
			throw new PhrescoWebServiceException(e, EX_PHEX00005, DATABASES_COLLECTION_NAME);
		}
	}
	
	
	/**
	 * Creates the list of databases
	 * @param databases
	 * @return 
	 */
	@POST
	@Consumes (MediaType.APPLICATION_JSON)
	@Path (REST_API_DATABASES)
	public Response createDatabases(List<Database> databases) {
	    if (isDebugEnabled) {
	        S_LOGGER.debug("Entered into ComponentService.createDatabases(List<Database> databases)");
	    }
		
		try {
			mongoOperation.insertList(DATABASES_COLLECTION_NAME , databases);
		} catch (Exception e) {
			throw new PhrescoWebServiceException(e, EX_PHEX00006, INSERT);
		}
		
		return Response.status(Response.Status.OK).build();
	}
	
	/**
	 * Updates the list of databases
	 * @param databases
	 * @return
	 */
	@PUT
	@Consumes (MediaType.APPLICATION_JSON)
	@Produces (MediaType.APPLICATION_JSON)
	@Path (REST_API_DATABASES)
	public Response updateDatabases(List<Database> databases) {
	    if (isDebugEnabled) {
	        S_LOGGER.debug("Entered into ComponentService.updateDatabases(List<Database> databases)");
	    }
		
		try {
			for (Database dataBase : databases) {
				Database dataBaseInfo = mongoOperation.findOne(DATABASES_COLLECTION_NAME , 
				        new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(dataBase.getId())), Database.class);
				if (dataBaseInfo != null) {
					mongoOperation.save(DATABASES_COLLECTION_NAME , dataBase);
				}
			}
		} catch (Exception e) {
			throw new PhrescoWebServiceException(e, EX_PHEX00006, UPDATE);
		}
		
		return Response.status(Response.Status.OK).entity(databases).build();
	}
	
	/**
	 * Deletes the list of databases
	 * @param databases
	 * @throws PhrescoException 
	 */
	@DELETE
	@Path (REST_API_DATABASES)
	public void deleteDatabases(List<Database> databases) throws PhrescoException {
	    if (isDebugEnabled) {
	        S_LOGGER.debug("Entered into ComponentService.deleteDatabases(List<Database> databases)");
	    }
		
		PhrescoException phrescoException = new PhrescoException(EX_PHEX00001);
		S_LOGGER.error("PhrescoException Is" + phrescoException.getErrorMessage());
		throw phrescoException;
	}
	
	/**
	 * Get the database by id for the given parameter
	 * @param id
	 * @return
	 */
	@GET
	@Produces (MediaType.APPLICATION_JSON)
	@Path (REST_API_DATABASES + REST_API_PATH_ID)
	public Response getDatabase(@PathParam(REST_API_PATH_PARAM_ID) String id) {
	    if (isDebugEnabled) {
	        S_LOGGER.debug("Entered into ComponentService.getDatabase(String id)" + id);
	    }
		
		try {
			Database database = mongoOperation.findOne(DATABASES_COLLECTION_NAME, 
			        new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(id)), Database.class);
			if (database != null) {
				return Response.status(Response.Status.OK).entity(database).build();
			} 
		} catch (Exception e) {
			throw new PhrescoWebServiceException(e, EX_PHEX00005, DATABASES_COLLECTION_NAME);
		}
		
		return Response.status(Response.Status.NO_CONTENT).entity(ERROR_MSG_NOT_FOUND).build();
	}
	
	/**
	 * Updates the database given by the parameter
	 * @param id
	 * @param database
	 * @return
	 */
	@PUT
	@Consumes (MediaType.APPLICATION_JSON)
	@Produces (MediaType.APPLICATION_JSON)
	@Path (REST_API_DATABASES + REST_API_PATH_ID)
	public Response updateDatabase(@PathParam(REST_API_PATH_PARAM_ID) String id , Database database) {
	    if (isDebugEnabled) {
	        S_LOGGER.debug("Entered into ComponentService.updateDatabase(String id, Database database)" + id);
	    }
		
		try {
			if (id.equals(database.getId())) {
				mongoOperation.save(DATABASES_COLLECTION_NAME, database);
				return Response.status(Response.Status.OK).entity(database).build(); 
			} 
		} catch (Exception e) {
			throw new PhrescoWebServiceException(e, EX_PHEX00006, UPDATE);
		}
		
		return Response.status(Response.Status.BAD_REQUEST).entity(ERROR_MSG_ID_NOT_EQUAL).build();
	}
	
	/**
	 * Deletes the server by id for the given parameter
	 * @param id
	 * @return 
	 */
	@DELETE
	@Path (REST_API_DATABASES + REST_API_PATH_ID)
	public Response deleteDatabase(@PathParam(REST_API_PATH_PARAM_ID) String id) {
	    if (isDebugEnabled) {
	        S_LOGGER.debug("Entered into ComponentService.deleteDatabase(String id)" + id);
	    }
		
		try {
			mongoOperation.remove(DATABASES_COLLECTION_NAME, new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(id)), Database.class);
		} catch (Exception e) {
			throw new PhrescoWebServiceException(e, EX_PHEX00006, DELETE);
		}
		
		return Response.status(Response.Status.OK).build();
	}
	
	/**
	 * Returns the list of webservices
	 * @return
	 */
	@GET
	@Path (REST_API_WEBSERVICES)
	@Produces (MediaType.APPLICATION_JSON)
	public Response findWebServices(@QueryParam(REST_QUERY_CUSTOMERID) String customerId) {
	    if (isDebugEnabled) {
	        S_LOGGER.debug("Entered into ComponentService.findWebServices()");
	    }
		
		List<WebService> webServiceList = new ArrayList<WebService>();
		try {
			if (!customerId.equals(DEFAULT_CUSTOMER_NAME)) {
				webServiceList= mongoOperation.find(WEBSERVICES_COLLECTION_NAME, 
				        new Query(Criteria.where (REST_QUERY_CUSTOMERID).is(customerId)), WebService.class);
			}
			webServiceList.addAll(mongoOperation.find(WEBSERVICES_COLLECTION_NAME, 
			        new Query(Criteria.where(REST_QUERY_CUSTOMERID)
			        .is(DEFAULT_CUSTOMER_NAME)), WebService.class));
			
			return  Response.status(Response.Status.OK).entity(webServiceList).build();
		} catch (Exception e) {
			throw new PhrescoWebServiceException(e, EX_PHEX00005, WEBSERVICES_COLLECTION_NAME);
		}
	}
	
	/**
	 * Creates the list of webservices
	 * @param webServices
	 * @return 
	 */
	@POST
	@Consumes (MediaType.APPLICATION_JSON)
	@Path (REST_API_WEBSERVICES)
	public Response createWebServices(List<WebService> webServices) {
	    if (isDebugEnabled) {
	        S_LOGGER.debug("Entered into ComponentService.createWebServices(List<WebService> webServices)");
	    }
		
		try {
			mongoOperation.insertList(WEBSERVICES_COLLECTION_NAME , webServices);
		} catch (Exception e) {
			throw new PhrescoWebServiceException(e, EX_PHEX00006, INSERT);
		}
		
		return Response.status(Response.Status.OK).build();
	}
	
	/**
	 * Updates the list of webservices
	 * @param webServices
	 * @return
	 */
	@PUT
	@Consumes (MediaType.APPLICATION_JSON)
	@Produces (MediaType.APPLICATION_JSON)
	@Path (REST_API_WEBSERVICES)
	public Response updateWebServices(List<WebService> webServices) {
	    if (isDebugEnabled) {
	        S_LOGGER.debug("Entered into ComponentService.updateWebServices(List<WebService> webServices)");
	    }
		
		try {
			for (WebService webService : webServices) {
				WebService webServiceInfo = mongoOperation.findOne(WEBSERVICES_COLLECTION_NAME , 
				        new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(webService.getId())), WebService.class);
				if (webServiceInfo != null) {
					mongoOperation.save(WEBSERVICES_COLLECTION_NAME , webService);
				}
			}
		} catch (Exception e) {
			throw new PhrescoWebServiceException(e, EX_PHEX00006, UPDATE);
		}
		
		return Response.status(Response.Status.OK).entity(webServices).build();
	}
	
	/**
	 * Deletes the list of webservices
	 * @param webServices
	 * @throws PhrescoException 
	 */
	@DELETE
	@Path (REST_API_WEBSERVICES)
	public void deleteWebServices(List<WebService> webServices) throws PhrescoException {
	    if (isDebugEnabled) {
	        S_LOGGER.debug("Entered into ComponentService.deleteWebServices(List<WebService> webServices)");
	    }
		
		PhrescoException phrescoException = new PhrescoException(EX_PHEX00001);
		S_LOGGER.error("PhrescoException Is" + phrescoException.getErrorMessage());
		throw phrescoException;
	}
	
	/**
	 * Get the database by id for the given parameter
	 * @param id
	 * @return
	 */
	@GET
	@Produces (MediaType.APPLICATION_JSON)
	@Path (REST_API_WEBSERVICES + REST_API_PATH_ID)
	public Response getWebService(@PathParam(REST_API_PATH_PARAM_ID) String id) {
	    if (isDebugEnabled) {
	        S_LOGGER.debug("Entered into ComponentService.getWebService(String id)" + id);
	    }
		
		try {
			WebService webService = mongoOperation.findOne(WEBSERVICES_COLLECTION_NAME, new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(id)), WebService.class);
			if (webService != null) {
				return Response.status(Response.Status.OK).entity(webService).build();
			} 
		} catch (Exception e) {
			throw new PhrescoWebServiceException(e, EX_PHEX00005, WEBSERVICES_COLLECTION_NAME);
		}
		
		return Response.status(Response.Status.NO_CONTENT).entity(ERROR_MSG_NOT_FOUND).build();
	}
	
	/**
	 * Updates the database given by the parameter
	 * @param id
	 * @param webService
	 * @return
	 */
	@PUT
	@Consumes (MediaType.APPLICATION_JSON)
	@Produces (MediaType.APPLICATION_JSON)
	@Path (REST_API_WEBSERVICES + REST_API_PATH_ID)
	public Response updateWebService(@PathParam(REST_API_PATH_PARAM_ID) String id , WebService webService) {
	    if (isDebugEnabled) {
	        S_LOGGER.debug("Entered into ComponentService.updateWebService(String id, WebService webService)" + id);
	    }
		
		try {
			if (id.equals(webService.getId())) {
				mongoOperation.save(WEBSERVICES_COLLECTION_NAME, webService);
				return Response.status(Response.Status.OK).entity(webService).build();
			} 
		} catch (Exception e) {
			throw new PhrescoWebServiceException(e, EX_PHEX00006, UPDATE);
		}
		
		return Response.status(Response.Status.BAD_REQUEST).entity(ERROR_MSG_ID_NOT_EQUAL).build();
	}
	
	/**
	 * Deletes the server by id for the given parameter
	 * @param id
	 * @return 
	 */
	@DELETE
	@Path (REST_API_WEBSERVICES + REST_API_PATH_ID)
	public Response deleteWebService(@PathParam(REST_API_PATH_PARAM_ID) String id) {
	    if (isDebugEnabled) {
	        S_LOGGER.debug("Entered into ComponentService.deleteWebService(String id)" + id);
	    }
		
		try {
			mongoOperation.remove(WEBSERVICES_COLLECTION_NAME, 
			        new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(id)), WebService.class);
		} catch (Exception e) {
			throw new PhrescoWebServiceException(e, EX_PHEX00006, DELETE);
		}
		
		return Response.status(Response.Status.OK).build();
	}
	
	/**
	 * Returns the list of technologies
	 * @return
	 */
	@GET
	@Path (REST_API_TECHNOLOGIES)
	@Produces (MediaType.APPLICATION_JSON)
	public Response findTechnologies(@QueryParam(REST_QUERY_CUSTOMERID) String customerId) {
	    if (isDebugEnabled) {
	        S_LOGGER.debug("Entered into ComponentService.findTechnologies()");
	    }
		
		try {
			List<Technology> techList = new ArrayList<Technology>();
			if (StringUtils.isEmpty(customerId)) {
				techList = mongoOperation.getCollection(TECHNOLOGIES_COLLECTION_NAME, Technology.class);
			} else {
				if(!customerId.equals(DEFAULT_CUSTOMER_NAME)) {
					techList = mongoOperation.find(TECHNOLOGIES_COLLECTION_NAME, 
								new Query(Criteria.where(REST_QUERY_CUSTOMERID).is(customerId)), Technology.class);
				}
				techList.addAll(mongoOperation.find(TECHNOLOGIES_COLLECTION_NAME, 
							new Query(Criteria.where(REST_QUERY_CUSTOMERID).is(DEFAULT_CUSTOMER_NAME)), Technology.class));
			}
			
			if (CollectionUtils.isNotEmpty(techList)) {
				return Response.status(Response.Status.OK).entity(techList).build();
			} 
		} catch (Exception e) {
			throw new PhrescoWebServiceException(e, EX_PHEX00005, TECHNOLOGIES_COLLECTION_NAME);
		}
		
    	return Response.status(Response.Status.NO_CONTENT).entity(ERROR_MSG_NOT_FOUND).build(); 
	}
	
	/**
	 * Creates the list of technologies
	 * @param technologies
	 * @throws IOException 
	 * @throws PhrescoException 
	 */
	@POST
	@Consumes (MultiPartMediaTypes.MULTIPART_MIXED)
	@Path (REST_API_TECHNOLOGIES)
	public Response createTechnologies(MultiPart multiPart, @QueryParam(REST_QUERY_APPTYPEID) String appTypeId) throws PhrescoException, IOException {
	    if (isDebugEnabled) {
	        S_LOGGER.debug("Entered into ComponentService.createTechnologies(List<Technology> technologies)" + appTypeId);
	    }
	    
	    Map<Technology, List<BodyPart>> map = new HashMap<Technology, List<BodyPart>>(); 
	    List<BodyPart> techs = new ArrayList<BodyPart>();
	    List<BodyPart> entities = new ArrayList<BodyPart>();
	    
	    //To separete the object and binary file
	    List<BodyPart> bodyParts = multiPart.getBodyParts();
	    for (BodyPart bodyPart : bodyParts) {
	        if (bodyPart.getMediaType().equals(MediaType.APPLICATION_JSON_TYPE)) {
	            techs.add(bodyPart);
	        } else {
	            entities.add(bodyPart);
	        }
        }
	    
	    //To map the binary file with corresponding object
	    List<BodyPart> foundBodyPart ;
	     for (BodyPart tech : techs) {
	        foundBodyPart = new ArrayList<BodyPart>();
            for (BodyPart bodyPart : entities) {
                if (tech.getContentDisposition().getFileName().equals(bodyPart.getContentDisposition().getFileName())) {
                    foundBodyPart.add(bodyPart);
                }
            }
            map.put(tech.getEntityAs(Technology.class), foundBodyPart);
        } 
	   
	   //Iterate the content map for upload binaries
       Set<Technology> keySet = map.keySet();
            for (Technology technology : keySet) {
                technology.setId(appTypeId);
                List<BodyPart> list = map.get(technology);
                createBinary (technology, list);
            }
	        
		return Response.status(Response.Status.OK).build();
	}
	
	private void createBinary(Technology technology, List<BodyPart> list) throws PhrescoException {
        List<ArchetypeInfo> infos = new ArrayList<ArchetypeInfo>();
        for (BodyPart bodyPart : list) {
            if (bodyPart.getContentDisposition().getType().equals(APPTYPE)) {
                BodyPartEntity bodyPartEntity = (BodyPartEntity) bodyPart.getEntity();
                File appJarFile = ServerUtil.writeFileFromStream(bodyPartEntity.getInputStream(), null);
                uploadBinary(technology.getArchetypeInfo(), appJarFile, technology.getCustomerId());
                FileUtil.delete(appJarFile);
            } else {
                BodyPartEntity bodyPartEntity = (BodyPartEntity) bodyPart.getEntity();
                ArchetypeInfo artifactinfo = ServerUtil.getArtifactinfo(bodyPartEntity.getInputStream());
                File appJarFile = ServerUtil.writeFileFromStream(bodyPartEntity.getInputStream(), null);
                infos.add(artifactinfo);
                uploadBinary(artifactinfo, appJarFile, technology.getCustomerId());
                FileUtil.delete(appJarFile);
            }
        }
        technology.setPlugins(infos);
        mongoOperation.save(TECHNOLOGIES_COLLECTION_NAME, technology);
    }
	
    /**
     * Upload binaries using the given artifact info
     * @param archetypeInfo
     * @param artifactFile
     * @param customerId
     * @return
     * @throws PhrescoException
     */
    private boolean uploadBinary(ArchetypeInfo archetypeInfo, File artifactFile, String customerId) throws PhrescoException {
        File pomFile = ServerUtil.createPomFile(archetypeInfo);
        
		ArtifactInfo info = new ArtifactInfo(archetypeInfo.getGroupId(), archetypeInfo.getArtifactId(), "", 
                archetypeInfo.getPackaging(), archetypeInfo.getVersion());
        info.setPomFile(pomFile);
        boolean addArtifact = repositoryManager.addArtifact(info, artifactFile, customerId);
        FileUtil.delete(pomFile);
        return addArtifact;
    }

    /**
	 * Updates the list of technologies
	 * @param technologies
	 * @return
	 */
	@PUT
	@Consumes (MediaType.APPLICATION_JSON)
	@Produces (MediaType.APPLICATION_JSON)
	@Path (REST_API_TECHNOLOGIES)
	public Response updateTechnologies(List<Technology> technologies) {
	    if (isDebugEnabled) {
	        S_LOGGER.debug("Entered into ComponentService.updateTechnologies(List<Technology> technologies)");
	    }
		
		try {
			for (Technology tech : technologies) {
				Technology techInfo = mongoOperation.findOne(TECHNOLOGIES_COLLECTION_NAME , 
				        new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(tech.getId())), Technology.class);
				if (techInfo != null) {
					mongoOperation.save(TECHNOLOGIES_COLLECTION_NAME , tech);
				}
			}
		} catch (Exception e) {
			throw new PhrescoWebServiceException(e, EX_PHEX00006, UPDATE);
		}
		
		return Response.status(Response.Status.OK).entity(technologies).build();
	}
	
	/**
	 * Deletes the list of technologies
	 * @param technologies
	 * @throws PhrescoException 
	 */
	@DELETE
	@Path (REST_API_TECHNOLOGIES)
	public void deleteTechnologies(List<WebService> technologies) throws PhrescoException {
	    if (isDebugEnabled) {
	        S_LOGGER.debug("Entered into ComponentService.deleteTechnologies(List<WebService> technologies)");
	    }
		
		PhrescoException phrescoException = new PhrescoException(EX_PHEX00001);
		S_LOGGER.error("PhrescoException Is" + phrescoException.getErrorMessage());
		throw phrescoException;
	}
	
	/**
	 * Get the technology by id for the given parameter
	 * @param id
	 * @return
	 */
	@GET
	@Produces (MediaType.APPLICATION_JSON)
	@Path (REST_API_TECHNOLOGIES + REST_API_PATH_ID)
	public Response getTechnology(@PathParam(REST_API_PATH_PARAM_ID) String id) {
	    if (isDebugEnabled) {
	        S_LOGGER.debug("Entered into ComponentService.getTechnology(String id)" + id);
	    }
		
		try {
			Technology technology = mongoOperation.findOne(TECHNOLOGIES_COLLECTION_NAME, 
			        new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(id)), Technology.class);
			if (technology != null) {
				return Response.status(Response.Status.OK).entity(technology).build();
			} 
		} catch (Exception e) {
			throw new PhrescoWebServiceException(e, EX_PHEX00005, TECHNOLOGIES_COLLECTION_NAME);
		}
		
		return Response.status(Response.Status.NO_CONTENT).entity(ERROR_MSG_NOT_FOUND).build();
	}
	
	/**
	 * Updates the technology given by the parameter
	 * @param id
	 * @param technology
	 * @return
	 */
	@PUT
	@Consumes (MediaType.APPLICATION_JSON)
	@Produces (MediaType.APPLICATION_JSON)
	@Path (REST_API_TECHNOLOGIES + REST_API_PATH_ID)
	public Response updateTechnology(@PathParam(REST_API_PATH_PARAM_ID) String id , Technology technology) {
	    if (isDebugEnabled) {
	        S_LOGGER.debug("Entered into ComponentService.getTechnology(String id, Technology technology)" + id);
	    }
		
		try {
			if (id.equals(technology.getId())) {
				mongoOperation.save(TECHNOLOGIES_COLLECTION_NAME, technology);
				return Response.status(Response.Status.OK).entity(technology).build();
			} 
		} catch (Exception e) {
			throw new PhrescoWebServiceException(e, EX_PHEX00006, UPDATE);
		}
		
		return Response.status(Response.Status.BAD_REQUEST).entity(ERROR_MSG_ID_NOT_EQUAL).build();
	}
	
	/**
	 * Deletes the server by id for the given parameter
	 * @param id
	 * @return 
	 */
	@DELETE
	@Path (REST_API_TECHNOLOGIES + REST_API_PATH_ID)
	public Response deleteTechnology(@PathParam(REST_API_PATH_PARAM_ID) String id) {
	    if (isDebugEnabled) {
	        S_LOGGER.debug("Entered into ComponentService.deleteTechnology(String id)" + id);
	    }
		
		try {
			mongoOperation.remove(TECHNOLOGIES_COLLECTION_NAME, 
			        new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(id)), Technology.class);
		} catch (Exception e) {
			throw new PhrescoWebServiceException(e, EX_PHEX00006, DELETE);
		}
		
		return Response.status(Response.Status.OK).build();
	}
	
	/**
     * Returns the list of downloadInfo
     * @return
     */
    @GET
    @Path (REST_API_DOWNLOADS)
    @Produces (MediaType.APPLICATION_JSON)
    public Response findDownloadInfo() {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into AdminService.findDownloadInfo()");
        }
        
        try {
            List<DownloadInfo> downloadList = mongoOperation.getCollection(DOWNLOAD_COLLECTION_NAME , DownloadInfo.class);
            if (downloadList != null) {
                return Response.status(Response.Status.OK).entity(downloadList).build();
            } 
        } catch (Exception e) {
            throw new PhrescoWebServiceException(e, EX_PHEX00006, DOWNLOAD_COLLECTION_NAME);
        }
        
        return Response.status(Response.Status.NO_CONTENT).entity(ERROR_MSG_NOT_FOUND).build();
    }

    /**
     * Creates the list of downloads
     * @param downloads
     * @return 
     * @throws PhrescoException 
     */
    @POST
    @Consumes (MultiPartMediaTypes.MULTIPART_MIXED)
    @Path (REST_API_DOWNLOADS)
    public Response createDownloads(MultiPart downloadPart) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ComponentService.createModules(List<ModuleGroup> modules)");
        }
        
        DownloadInfo downloadInfo = null;
        BodyPartEntity bodyPartEntity = null;
        File downloadFile = null;
        
        List<BodyPart> bodyParts = downloadPart.getBodyParts();
        if(CollectionUtils.isNotEmpty(bodyParts)) {
            for (BodyPart bodyPart : bodyParts) {
                if (bodyPart.getMediaType().equals(MediaType.APPLICATION_JSON_TYPE)) {
                    downloadInfo = new DownloadInfo();
                    downloadInfo = bodyPart.getEntityAs(DownloadInfo.class);
                } else {
                    bodyPartEntity = (BodyPartEntity) bodyPart.getEntity();
                }
            }
        }
        
        if(bodyPartEntity != null) {
            downloadFile = ServerUtil.writeFileFromStream(bodyPartEntity.getInputStream(), null);
        }
        
        boolean uploadBinary = uploadBinary(downloadInfo.getArchetypeInfo(), 
                downloadFile, downloadInfo.getCustomerId());
        if(uploadBinary) {
            downloadInfo.setDownloadURL(createContentURL(downloadInfo.getArchetypeInfo()));
            mongoOperation.save(DOWNLOAD_COLLECTION_NAME, downloadInfo);
        }
        FileUtil.delete(downloadFile);
        return Response.status(Response.Status.CREATED).build();
    }

    /**
     * Updates the list of downloadInfos
     * @param downloads
     * @return
     */
    @PUT
    @Consumes (MediaType.APPLICATION_JSON)
    @Produces (MediaType.APPLICATION_JSON)
    @Path (REST_API_DOWNLOADS)
    public Response updateDownloadInfo(List<DownloadInfo> downloads) {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into AdminService.updateDownloadInfo(List<DownloadInfo> downloads)");
        }
        
        try {
            for (DownloadInfo download : downloads) {
                DownloadInfo downloadInfo = mongoOperation.findOne(DOWNLOAD_COLLECTION_NAME , 
                        new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(download.getId())), DownloadInfo.class);
                if (downloadInfo  != null) {
                    mongoOperation.save(DOWNLOAD_COLLECTION_NAME, download);
                }
            }
        } catch (Exception e) {
            throw new PhrescoWebServiceException(e, EX_PHEX00006, UPDATE);
        }
        
        return Response.status(Response.Status.OK).entity(downloads).build();
    }

    /**
     * Deletes the list of DownloadInfo
     * @param downloadInfos
     * @throws PhrescoException 
     */
    @DELETE
    @Path (REST_API_DOWNLOADS)
    public void deleteDownloadInfo(List<DownloadInfo> downloadInfos) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into AdminService.deleteDownloadInfo(List<DownloadInfo> downloadInfos)");
        }
        
        PhrescoException phrescoException = new PhrescoException(EX_PHEX00001);
        S_LOGGER.error("PhrescoException Is"  + phrescoException.getErrorMessage());
        throw phrescoException;
    }

    /**
     * Get the downloadInfo by id for the given parameter
     * @param id
     * @return
     */
    @GET
    @Produces (MediaType.APPLICATION_JSON)
    @Path (REST_API_DOWNLOADS + REST_API_PATH_ID)
    public Response getDownloadInfo(@PathParam(REST_API_PATH_PARAM_ID) String id) {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into AdminService.getDownloadInfo(String id)" + id);
        }
        
        try {
            DownloadInfo downloadInfo = mongoOperation.findOne(DOWNLOAD_COLLECTION_NAME, 
                    new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(id)), DownloadInfo.class);
            if (downloadInfo != null) {
                return Response.status(Response.Status.OK).entity(downloadInfo).build();
            } 
        } catch (Exception e) {
            throw new PhrescoWebServiceException(e, EX_PHEX00005, DOWNLOAD_COLLECTION_NAME);
        }
        
        return Response.status(Response.Status.NO_CONTENT).entity(ERROR_MSG_NOT_FOUND).build();
    }
    
    /**
     * Updates the list of downloadInfo by id
     * @param downloadInfos
     * @return
     */
    @PUT
    @Consumes (MediaType.APPLICATION_JSON)
    @Produces (MediaType.APPLICATION_JSON)
    @Path (REST_API_DOWNLOADS + REST_API_PATH_ID)
    public Response updateDownloadInfo(@PathParam(REST_API_PATH_PARAM_ID) String id , DownloadInfo downloadInfo) {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into AdminService.updateDownloadInfo(String id , DownloadInfo downloadInfos)" + id);
        }
        
        try {
            if (id.equals(downloadInfo.getId())) {
                mongoOperation.save(DOWNLOAD_COLLECTION_NAME, downloadInfo);
                return Response.status(Response.Status.OK).entity(downloadInfo).build();
            } 
        } catch (Exception e) {
            throw new PhrescoWebServiceException(e, EX_PHEX00006, UPDATE);
        }
        
        return Response.status(Response.Status.BAD_REQUEST).entity(ERROR_MSG_ID_NOT_EQUAL).build();
    }
    
    /**
     * Deletes the user by id for the given parameter
     * @param id
     * @return 
     */
    @DELETE
    @Path (REST_API_DOWNLOADS + REST_API_PATH_ID)
    public Response deleteDownloadInfo(@PathParam(REST_API_PATH_PARAM_ID) String id) {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into AdminService.deleteDownloadInfo(String id)" + id);
        }
        
        try {
            mongoOperation.remove(DOWNLOAD_COLLECTION_NAME, new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(id)), DownloadInfo.class);
        } catch (Exception e) {
            throw new PhrescoWebServiceException(e, EX_PHEX00006, DELETE);
        }
        
        return Response.status(Response.Status.OK).build();
    }
    

}
