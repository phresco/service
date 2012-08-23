package com.photon.phresco.service;

import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Type;
import java.util.List;

import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.photon.phresco.exception.PhrescoException;
import com.photon.phresco.model.VideoInfo;
import com.photon.phresco.service.api.PhrescoServerFactory;
import com.photon.phresco.service.api.RepositoryManager;
import com.photon.phresco.util.ServiceConstants;

public class HomePageServiceTest {

	@Test
	public void testGetHomePageVideo() throws PhrescoException {
		PhrescoServerFactory.initialize();
		Gson gson = new Gson();
		RepositoryManager repoMgr = PhrescoServerFactory.getRepositoryManager();
		String videoInfoJSON = repoMgr.getArtifactAsString(repoMgr.getHomePageJsonFile(), ServiceConstants.DEFAULT_CUSTOMER_NAME);
		Type type = new TypeToken<List<VideoInfo>>() {
		}.getType();
		List<VideoInfo> videoInfoList = gson.fromJson(videoInfoJSON, type);
		assertNotNull(videoInfoList);
	}

}
