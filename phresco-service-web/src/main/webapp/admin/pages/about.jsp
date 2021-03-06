<%--

    Service Web Archive

    Copyright (C) 1999-2014 Photon Infotech Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

            http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

--%>
<%@ taglib uri="/struts-tags" prefix="s"%>

<div id="versionInfo">
	 <div class="abt_logo">
		<img src="images/about_logo.png" alt="logo" class="abt_logo_img">
	</div>	

	<div class="abt_content">
		Phresco is a platform for creating next generation web, mobile, and multi channel presences leveraging your existing investments combined with accepted industry best practices.		
	</div>
</div>

<script type="text/javascript">
	$(document).ready(function() {
		$('#updateMsg').html('&copy; 2013.Photon Infotech Pvt Ltd');
		$("#popupCancel").hide();
		$(".popupOk").hide();
		$("#clipboard").hide();
		$('#popupClose').show();
		showVersionInfo();
	});
	
	function showVersionInfo() {
		loadContent("versionInfo", '', '', '', true, false,'');
	}
	
	function successEvent(pageUrl, data) {
		if (pageUrl == "versionInfo") {
			$('#CurrentVersion').html(data.currentVersion);
  		 	$("#popupTitle").html("Phresco Service " + data.currentVersion);
		}
	}
	
</script>