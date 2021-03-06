function applyTheme() {
	var theme = localStorage["color"];
	if (theme != null) {
		changeTheme(theme);
		showWelcomeImage(theme);
	} else {
		theme = "theme/photon/css/photon_theme.css";
		changeTheme(theme);
		showWelcomeImage(theme);
	}
}

function changeTheme(localstore) {
	if (localstore == "theme/red_blue/css/red.css") {
		$("link[title='phresco']").attr("href", "theme/red_blue/css/red.css");
        $("link[id='phresco']").attr("href", "theme/red_blue/css/phresco.css");
        $("link[id='media-query']").attr("href", "theme/red_blue/css/media-queries.css");
        $("script[id='windowResizer']").attr("src", "js/windowResizer.js");
        $('#loadingIconDiv').activity({segments: 10, color: '#ff424c', speed: 1.5});
    } else if (localstore == "theme/red_blue/css/blue.css") {
        $("link[title='phresco']").attr("href", localstore);
        $("link[id='phresco']").attr("href", "theme/red_blue/css/phresco.css");
        $("link[id='media-query']").attr("href", "theme/red_blue/css/media-queries.css");
        $("script[id='windowResizer']").attr("src", "js/windowResizer.js");
        $('#loadingIconDiv').activity({segments: 10, color: '#5dd0ff', speed: 1.5});
    } else if (localstore == "theme/photon/css/photon_theme.css") {
        $("link[title='phresco']").attr("href", localstore);
        $("link[id='phresco']").attr("href", "theme/photon/css/phresco_default.css");
        $("link[id='media-query']").attr("href", "theme/photon/css/media-queries.css");
        $("script[id='windowResizer']").attr("src", "js/windowResizer_default.js");
        $('#loadingIconDiv').activity({segments: 10, color: '#C2C2C2', speed: 1.5});
    } 
}

function showWelcomeImage(localstore) {
	if (localstore == "theme/red_blue/css/blue.css") {
		$('.headerlogoimg').attr("src","theme/red_blue/images/phresco_header_blue.png");
		$('.phtaccinno').attr("src","theme/red_blue/images/acc_inov_blue.png");
		$('.welcomeimg').attr("src","theme/red_blue/images/welcome_photon_blue.png");
		$('.logoimage').attr("src","theme/red_blue/images/phresco_header_blue.png");
	} else if (localstore == "theme/red_blue/css/red.css") {
		$('.headerlogoimg').attr("src","theme/red_blue/images/phresco_header_red.png");
		$('.phtaccinno').attr("src","theme/red_blue/images/acc_inov_red.png");
		$('.welcomeimg').attr("src","theme/red_blue/images/welcome_photon_red.png");
		$('.logoimage').attr("src","theme/red_blue/images/phresco_header_red.png");
	} else if (localstore == "theme/photon/css/photon_theme.css") {
		$('.headerlogoimg').attr("src","theme/photon/images/photon_phresco_logo.png");
		/*$('.phtaccinno').attr("src","theme/photon/images/acc_inov_green.png");*/
		$('.welcomeimg').attr("src","theme/photon/images/welcome_photon.png");
	}
}

var jars = [];

function clickMenu(menu, tag, form) {
	menu.click(function() {
		showLoadingIcon();
		inActivateAllMenu(menu);
		activateMenu($(this));
		var selectedMenu = $(this).attr("id");
		var additionalParam = $(this).attr('additionalParam');
		loadContent(selectedMenu, form, tag, additionalParam);
	});
}


function copyToClipboard(data) {
    var params = "copyToClipboard=";
    params = params.concat(data);
    loadContent('copyToClipboard','','', params,'');
}

function clickButton(button, tag) {
	button.click(function() {
		var selectedMenu = $(this).attr("id");
		loadContent(selectedMenu, '', tag);
	});
}

function loadJsonContent(url, jsonParam, containerTag, progressText) {
	if (progressText !== undefined && !isBlank(progressText)) {
		showProgressBar(progressText);
	}
	$.ajax({
		url : url,
		data : jsonParam,
		type : "POST",
		contentType: "application/json; charset=utf-8",
		success : function(data) {
			if (containerTag != undefined) {
				if (progressText !== undefined && !isBlank(progressText)) {
					hideProgressBar();
				}
				loadData(data, containerTag);
			}
		}
	});	
}

function loadContent(pageUrl, form, tag, additionalParams, callSuccessEvent, callbackFunction, progressText) {
	if (progressText !== undefined && !isBlank(progressText)) {
		showProgressBar(progressText);
	}
	var params = "";
	if (form != undefined && form != "" && !isBlank(form.serialize())) {
		params = form.serialize();
		if (!isBlank(additionalParams)) {
			params = params.concat("&");
			params = params.concat(additionalParams);
		} 
	} else if (additionalParams != undefined && additionalParams != "")  {
		params = additionalParams;
	}
	$.ajax({
		url : pageUrl,
		data : params,
		type : "POST",
		success : function(data) {
			if (progressText !== undefined && !isBlank(progressText)) {
				hideProgressBar();
			}
			loadData(data, tag, pageUrl, callSuccessEvent, callbackFunction);
		}
	});
}

function clickSave(pageUrl, params, tag, progressText) {
	if (progressText !== undefined) {
		hideLoadingIcon();
		showProgressBar(progressText);
	}
	$.ajax({
		url : pageUrl,
		data : params,
		type : "POST",
		success : function(data) {
			hideProgressBar();
			loadData(data, tag);
		}
	});
}

function validate(pageUrl, form, tag, progressText, disabledDiv) {
	showLoadingIcon();
	/*if (pageUrl == 'downloadSave') {
		showLoadingIcon();
	}*/
	if (disabledDiv != undefined && disabledDiv != "") {
		enableDivCtrls(disabledDiv);
	}
	var params = "";
	if (form != undefined && !isBlank(form)) {
		params = form.serialize();
	}
	$.ajax({
		url : pageUrl + "Validate",
		data : params,
		type : "POST",
		success : function(data) {
			if (data.errorFound != undefined && data.errorFound) {
				hideLoadingIcon();
				if (data.versioning == "versioning") {
					disableCtrl(disabledDiv);
					$(".upload").attr("disabled", false);
					$("#pluginjarUpload").attr("disabled", false);
				}
				findError(data);
			} else {
				clickSave(pageUrl, params, tag, progressText);
			}
		}
	});
}

function loadData(data, tag, pageUrl, callSuccessEvent, callbackFunction) {
	//To load the login page if the user session is not available
	if (data != undefined && data != "[object Object]" && data != "[object XMLDocument]" 
		&& !isBlank(data) && data.indexOf("Remember me") >= 0) {
		window.location.href = "logout.action";
	} else {
		if (callSuccessEvent != undefined && !isBlank(callSuccessEvent) && callSuccessEvent) {
			if (callbackFunction != undefined && !isBlank(callbackFunction)) {
				window[callbackFunction](data);
			} else {
				successEvent(pageUrl, data);
			}
		} else {
			if (tag !== undefined && !isBlank(tag)) {
				tag.empty();
				tag.html(data);
			}
			accordion();
			setTimeOut();
		}
	}
}

function inActivateAllMenu(allLink) {
	allLink.attr("class", "inactive");
}

function activateMenu(selectedMenu) {
	selectedMenu.attr('class', "active");
}

function checkAllEvent(currentCheckbox, childCheckBox, disable) {
	var checkAll = $(currentCheckbox).prop('checked');
	childCheckBox.prop('checked', checkAll);
	buttonStatus(checkAll);
	if (!checkAll) {
		disable = false;
	}
	toDisableAllCheckbox(currentCheckbox,childCheckBox, disable);
}

function checkboxEvent(parentChkBoxObj, chldChkBoxCls) {
	var chkboxStatus = $('.' + chldChkBoxCls).is(':checked');
	buttonStatus(chkboxStatus);
	if ($('.' + chldChkBoxCls).length == $("." + chldChkBoxCls + ":checked").length) {
		parentChkBoxObj.prop('checked', true);
	} else {
		parentChkBoxObj.prop('checked', false);
	}
}

function buttonStatus(checkAll) {
	$('#del').attr('disabled', !checkAll);
	if (checkAll) {
		$('#del').addClass('btn-primary');
	} else {
		$('#del').removeClass('btn-primary');
	}
}

function toDisableCheckAll(parentChkBoxObj, chldChkBoxCls) {
	if ($('.' + chldChkBoxCls).length > 0 && $('.' + chldChkBoxCls + ':disabled').length == 0) {
		parentChkBoxObj.prop('disabled', false);
	} else {
		parentChkBoxObj.prop('disabled', true);
	}
}

function toDisableAllCheckbox(currentCheckbox,childCheckBox, disable) {
	if($(currentCheckbox).is(':checked')){
		childCheckBox.prop('disabled', disable);
	} else {
		childCheckBox.prop('disabled', disable);
		childCheckBox.prop('checked', false);
	}
}

function showError(tag, span, errmsg) {
	tag.addClass("error");
	span.text(errmsg);
}

function hideError(tag, span) {
	if (!isBlank(tag)) {
		tag.removeClass("error");
		span.empty();
	} 
}

function setTimeOut() {
	setTimeout(function() {
		$('#successmsg').fadeOut("slow", function () {
			$('#successmsg').remove();
		});
	}, 2000);
	
	setTimeout(function() {
		$('#errormsg').fadeOut("slow", function () {
			$('#errormsg').remove();
		});
	}, 2000);
	
	setTimeout(function() {
		$('.errMsg').empty("slow", function () {
			$('.errMsg').remove();
		});
	}, 2000);
}

function accordion() {
	/** Accordian starts **/
	var showContent = 0;	
    $('.siteaccordion').removeClass('openreg').addClass('closereg');
    $('.mfbox').css('display','none');
    
    $('.siteaccordion').bind('click',function(e) {
        var _tempIndex = $('.siteaccordion').index(this);
        $('.siteaccordion').removeClass('openreg').addClass('closereg');
        $('.mfbox').each(function(e) {
            if ($(this).css('display')=='block'){
                $(this).slideUp('300');
            }
        });
        if ($('.mfbox').eq(_tempIndex).css('display')=='none') {
            $(this).removeClass('closereg').addClass('openreg');
            $('.mfbox').eq(_tempIndex).slideDown(300,function() {
                
            });
        }
    });
}

function showLoadingIcon() {
	var src = "theme/photon/images/loading_blue.gif";
	var theme =localStorage["color"];
    if (theme == "theme/red_blue/css/red.css") {
    	src = "theme/red_blue/images/loading_red.gif";
    } else if (theme == "theme/red_blue/css/blue.css") {
    	src = "theme/red_blue/images/loading_blue.gif";
    } else if (theme == undefined || theme == "theme/photon/css/photon_theme.css") {
    	src = "theme/photon/images/loading_green.gif";
    }
    $("#loadingIconDiv").show();
	$("#loadingIconImg").attr("src", src);
    disableScreen();
}

function hideLoadingIcon() {
    $("#loadingIconDiv").hide();
    enableScreen();
}

function hidePopuploadingIcon() {
	$(".popuploadingIcon").hide();
}

function showProgressBar(progressText) {
	$("#progressnum").html(progressText);
	disableScreen();
	$("#progressbar").show();
	setInterval(prog, 100);
}

function hideProgressBar() {
	enableScreen();
	$("#progressbar").hide();
}

// It allows A-Z, a-z, 0-9, - and _ 
function checkForSplChr(inputStr) {
	return inputStr.replace(/[^a-zA-Z 0-9\-\_]+/g, '');
}

//It allows A-Z, a-z,
function allowAlpha(inputStr) {
	return inputStr.replace(/[^a-zA-Z]+/g, '');
}

//It allows A-Z, a-z, 0-9, - , _ and .
function checkForSplChrExceptDot(inputStr) {
	return inputStr.replace(/[^a-zA-Z 0-9\.\-\_]+/g, '');
}

//It allows A-Z, a-z, 0-9
function allowAlphaNum(inputStr) {
	return inputStr.replace(/[^a-zA-Z 0-9]+/g, '');
}

//It allows 0-9,- and +
function allowNumHyphenPlus(numbr) {
	return numbr.replace(/[^0-9\-\+]+/g, '');
}

//It allows _,-
function allowHypenUnderscore(inputStr) {
	return inputStr.replace(/[^a-zA-Z\-\_]+/g, '');
}

function isBlank(str) {
    return (!str || /^\s*$/.test(str));
}

//To disable the screen by showing an overlay
function disableScreen() {
	$(".modal-backdrop").show();
}

//To strip the Space
function stripSpace(inputStr) {
	return inputStr.replace(/\s/g,'');
}

//To enable the screen by hiding an overlay
function enableScreen() {
	$(".modal-backdrop").hide();
}

//To fill the pom details in the textbox if available while uploading the files
function fillTextBoxes(responseJSON, type, fileName) {
	if (type === "pluginJar"){
		$('#jarDetailsDivPopup').show();
	} else if (type === "videoFile") {
		$('#videoDetailsDiv').show();
	} else {
		$('#jarDetailsDiv').show();
	}
	if (responseJSON.mavenJar) {
		disableEnableTextBox(responseJSON.groupId, responseJSON.artifactId, responseJSON.version, true, type, fileName);
	} else {
		disableEnableTextBox('', '', '', false, type, fileName);
	}
}

function checkExistence(fileName){
	for (var i = 0; i < jars.length; i++) {
        if (jars[i] === fileName) {
        	return true;
        }
    }
	return false;
}

function arrayPushPop(fileName, isEnable){
	if(isEnable){
		jars.push(fileName);
	}else{
		 for(var i in jars){
	            if(jars[i]==fileName){
	                jars.splice(i,1);
	                break;
	                }
	        }
	}
}

function disableEnableTextBox(groupId, artifactId, jarVersion, isEnable, type, fileName) {
	if (type === "pluginJar") {
		var groupIdDisable = "disabled";
		if (groupId == undefined || isBlank(groupId)) {
			groupIdDisable = false;
		}
	
		var artifactIdDisable = "disabled";
		if (artifactId == undefined || isBlank(artifactId)) {
			artifactIdDisable = false;
		}
	
		var jarVersionDisable = "disabled";
		if (jarVersion == undefined || isBlank(jarVersion)) {
			jarVersionDisable = false;
		}
		
		var duplicate = checkExistence(fileName);
		arrayPushPop(fileName, true);
		var groupid = "grouId" ;
		var artifId = "artifId" ;
		var versnId = "versnId" ;
		var fileDetParentDiv = $(document.createElement('div')).attr("id", fileName).attr("class","fileClass");
		fileDetParentDiv.html("<div style='float: left; margin: 0px 10px 0px 0px;'><div class='controls' style='margin-left: 0%;'><input style='text-align:center;'id='" +groupid +"' class='pluginGroupId' class='input-xlarge' name='"+fileName+"_groupId" +"' maxlength='40' title='40 Characters only' type='text'  value='" + groupId +"' "+groupIdDisable+" >" +
				           "</div></div></td>"); 
		fileDetParentDiv.append("<div style='float: left; margin: 0px 10px 0px 0px;'><div class='controls' style='margin-left: 0%;'><input style='text-align:center;'id='" +artifId +"'class='pluginArtifactId' class='input-xlarge' name='"+fileName+"_artifactId" +"' maxlength='40' title='40 Characters only' type='text' value='" + artifactId +"' "+artifactIdDisable+">" +
				              "</div></div>");
		fileDetParentDiv.append("<div style='float: left; margin: 0px 10px 0px 0px;'><div class='controls'  style='margin-left: 0%;'><input style='text-align:center;'id='" +versnId +"'class='pluginJarVersion' name='"+fileName+"_version" +"'  maxlength='30' title='30 Characters only' class='input-xlarge' type='text' value='" +jarVersion +"' "+jarVersionDisable+"> " +
		                   "</div></div>");
		fileDetParentDiv.append("</div>");
		fileDetParentDiv.appendTo("#tableAdd");
		if(duplicate){
			arrayPushPop(fileName, false);
			var lis = $('#tableAdd').children(); // get all the children
			var len=lis.length-1;
			$(lis.get(len)).remove();
			$('li').last().remove()
		}
	} else {
		$('.groupId').val(groupId).attr('disabled', isEnable);
		$('.artifactId').val(artifactId).attr('disabled', isEnable);
		$('.jarVersion').val(jarVersion).attr('disabled', isEnable);
		$('input[name=groupId]').val(groupId);
		$('input[name=artifactId]').val(artifactId);
		$('input[name=jarVersion]').val(jarVersion);
	}
}

function isiPad() {
    return (
        (navigator.platform.indexOf("iPhone") != -1) ||
        (navigator.platform.indexOf("iPad") != -1)
    );
}

//This method is calling from triggering list.jsp
function showDeleteConfirmation(confirmMsg) {
	$('#popupPage').addClass('confirm');
	$('#popupTitle').html("Confirmation Dialog"); 
	$('.modal-body').html(confirmMsg);
	$('#clipboard').hide();
	$('.modal-body').css("height","50px");
	$('#popupPage').css({"width":"649px","position":"relative","left":"48%"});
	$('.popupOk').removeAttr("onclick");
	$('.popupOk').attr("value", "Ok");
	$('.popupOk').attr("onclick","continueDeletion()");
	$('#popupClose').hide();
	
	$('#popupPage').modal("show");
}

function hidePopup() {
	$('#popupPage').modal("hide");
}

function copyToClipboard(data) {
    var params = "copyToClipboard=";
    params = params.concat(data);
    $.ajax({
		url : "copyToClipboard",
		data : params,
		type : "POST",
		success : function() {
		}
	});
}

//trim the long content
function textTrim(obj) {
    var val = $(obj).text();
    $(obj).attr("title", $.trim(val));
    var len = val.length;
    if(len > 50) {
        val = val.substr(0, 50) + "...";
        return val;
    }
    return val;
}

function depenTrim(obj) {
    var val = $(obj).text();
    $(obj).attr("title", $.trim(val));
    var len = val.length;
    if(len > 50) {
        val = val.substr(0, 50) + "..." +"]";
        return val;
    }
    return val;
}

$(document).keydown(function(e) {
    // ESCAPE key pressed
	if (e.keyCode == 27) {
	   showParentPage();
    }
});

//Shows the parent page
function showParentPage() {
	$('#popupPage').modal("hide");
}

//To disable the given control
function disableCtrl(control) {
	control.attr("disabled", true);
}

//To enable the given control
function enableCtrl(control) {
	control.removeAttr("disabled");
}

function enableDivCtrls(disabledDiv) {
	disabledDiv.removeAttr("disabled");
}

function enableDisableUploads(type, controlObj){
	if ($('ul[temp='+type+'] > li').length === 1) {
		disableUploadButton(controlObj);
	} else {
		enableUploadButton(controlObj);
	}
}

function validateJson(url, form, containerTag, jsonParam, progressText, disabledDiv) {
	if (disabledDiv != undefined && disabledDiv != "") {
		enableDivCtrls(disabledDiv);
	}
	$.ajax({
		url : url + "Validate",
		data : jsonParam,
		type : "POST",
		contentType: "application/json; charset=utf-8",
		success : function(data) {
			if (data.errorFound != undefined && data.errorFound) {
				findError(data);
			} else {
				loadJsonContent(url, jsonParam, containerTag, progressText);
			}
		}
	});
}

function yesnoPopup(url, title, okUrl, okLabel, form, additionalParams) {
	$('#popupPage').modal('show');//To show the popup
	$('#popupTitle').html(title); // Title for the popup
	$('.popupOk, #popupCancel').show(); // show ok & cancel button
	$(".popupOk").attr('id', okUrl); // popup action mapped to id
	if (okLabel !== undefined && !isBlank(okLabel)) {
		$('#' + okUrl).val(okLabel); // label for the ok button
	}
	
	if (url === "fetchFeaturesForDependency") {
		$('.popuploadingIcon').activity({segments: 10, color: '#3c3c3c', speed: 1.8});
	}
	
	var params = "";
	if (form != undefined && form != "" && !isBlank(form.serialize())) {
		params = form.serialize();
		if (!isBlank(additionalParams)) {
			params = params.concat("&");
			params = params.concat(additionalParams);	
		} 
	} else if (additionalParams != undefined && additionalParams != "")  {
		params = additionalParams;
	}
	$("#errMsg").empty();
	$("#updateMsg").empty();
	$('#successMsg').empty();
	$('#popupPage_modal-body').empty();
	$('#popupPage_modal-body').css("height", "300px");
	
	$.ajax({
		url : url,
		data : params,
		type : "POST",
		success : function(data) {
			loadData(data, $('#popupPage_modal-body'));
		}
	});
}

//To enable the given button object
function enableButton(buttonObj) {
	buttonObj.addClass('btn-primary');
	buttonObj.attr("disabled", false);
}

function disableButton(buttonObj) {
	buttonObj.removeClass('btn-primary');
	buttonObj.attr("disabled", true);
}

function disableUploadButton(controlObj) {
	controlObj.find("input[type='file']").attr('disabled', 'disabled');
	controlObj.find($(".qq-upload-button")).removeClass("btn-primary qq-upload-button").addClass("disabled");
}

function removeSpaces(str) {
	return str.replace(/\s+/g, '');
}

function enableUploadButton(controlObj) {
	controlObj.find("input[type='file']").attr('disabled', false);
	controlObj.find($(".btn")).removeClass("disabled").addClass("btn-primary qq-upload-button");
}

function changeChckBoxValue(obj) {
	if ($(obj).is(':checked')) {
		$(obj).val("true");
	} else {
		$(obj).val("false");
	}
}

function open_win() {
	window.open("http://www.photon.in");
}

document.onkeydown = function(evt) {
    evt = evt || window.event;
    if (evt.keyCode == 27) {
    	$("#popupPage").modal('hide');
    }
};