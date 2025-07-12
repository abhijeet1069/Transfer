package com.idfc.single.bc.transfer;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;

import com.idfc.single.bc.pulse.PulseDesk;
import com.idfc.single.util.AppConstants;
import com.idfc.single.util.CommonUtilities;
import com.servionsfw.core.SessionAPI;
import com.servionsfw.framework.creator.decision.DecisionInterface;
import com.servionsfw.framework.creator.voice.VoiceElementsInterface;
import com.servionsfw.framework.loader.common.ElementInfo;


public class SetTransferInfo_BC implements DecisionInterface,AppConstants{

	String strIsHotline=null;
	String activeLob = null;
	Properties skillNameProp = null;
	String skillName = null;
	String strKeyForSkill = null;
	String strLang = null;
	String flowType = null;
	Map<String, Object> transferDecisionMap = null;
	Map<String,String> mapTransferDetails = null;
	CommonUtilities objCommonUtilities = null;
	String strAgentTransKey = EMPTY;

	@SuppressWarnings("unchecked")
	@Override
	public String doExecute(SessionAPI sessionAPI, Map<String, Object> decisionData, Map<String, Object> hostData) {
		String strExitState = NBH;
		String methodName = "SetTransferInfo_BC:: ";
		String strIsPulseFlag=null;
		String strIsCorporateFlag=null;
		String escalationCaller = null;
		String flagNTBdesk = N;
		String staffFlag = null;
		String customerCategoryFlag = null;
		String loanPLAcctProceedFlag = null;
		String vvipFlag = null;

		try{
			sessionAPI.setSessionData(S_SALE_OFFER_TRANSFER_FLAG, N);//Set Default value N for S2S desk flow
			//Set flow type basis active LOB. If LOB session is null/empty, default will be "RL"
			activeLob = (String)sessionAPI.getSessionData(S_ACTIVE_LOB);
			sessionAPI.addToLog(methodName+" :: Active LOB :: " + activeLob, LOG_DEBUG);

			staffFlag = (String)sessionAPI.getSessionData(S_STAFF_FLAG);
			sessionAPI.addToLog(methodName+" :: S_STAFF_FLAG :: " + staffFlag, LOG_DEBUG);

			//If Lob session is null/empty, default will be "Single_TFN_RL"
			if(null == activeLob || "".equals(activeLob)){
				flowType = (String)sessionAPI.getSessionData(S_FLOW_TYPE);
			}else{
				flowType = (String)sessionAPI.getSessionData(S_FLOW_TYPE_+activeLob);
			}
			//If flowType session is null/empty, default will be "Single_TFN"
			flowType = (null == flowType || "".equals(flowType))?"Single_TFN_RL":flowType;
			sessionAPI.setSessionData(S_FLOW_TYPE, flowType);
			sessionAPI.addToLog(methodName+" :: flow Type :: " + flowType+"  activeLob = "+activeLob, LOG_INFO);
			//End set flow type


			//12-12-2022 set authentication validation result in session for ICM variables
			objCommonUtilities = new CommonUtilities(sessionAPI);
			objCommonUtilities.setAuthValidationResult(sessionAPI);

			ElementInfo elementInfo=(ElementInfo) sessionAPI.getApplicationData(TRANSFER);
			strLang=(String)sessionAPI.getSessionData(S_ACTIVE_LANG);

			if(elementInfo!=null){	
				strAgentTransKey = (String)sessionAPI.getSessionData(S_AGENTTRANSER_XML_KEY, AGENT_TRANSFER);
				sessionAPI.addToLog(methodName + "strAgentTransKey :: " + strAgentTransKey, LOG_DEBUG);
				if (AGENT_TRANSFER.equalsIgnoreCase(strAgentTransKey)){
					sessionAPI.addToLog(methodName +"Transfer",LOG_DEBUG);
					sessionAPI.setSessionData(S_TRANSFER_FLAG, Y);
					sessionAPI.setSessionData(IS_AGENT_TRANSFER_FLAG, Y);
					sessionAPI.setSessionData(S_CALLER_INPUT, CALLER_INPUT_AGENT_TRANSFER);

					//Skills setting
					//Added the below for handling hotline transfer.
					skillNameProp = (Properties)sessionAPI.getApplicationData(A_SKILL_CONFIG_FILENAME);
					strIsHotline=(String)sessionAPI.getSessionData(S_IS_HOTLINE);
					sessionAPI.addToLog(methodName +"Hotline : "+strIsHotline, LOG_DEBUG);
					if(Y.equalsIgnoreCase(strIsHotline))
					{
						strKeyForSkill = HOTLINE;
					}
					else
					{
						//Setting the default overflow skill
						setDefaultOverFlowSkill(sessionAPI);

						String flagCBDCcaller = (String)sessionAPI.getSessionData(S_CBDC_CALLER_FLAG);
						vvipFlag = (String)sessionAPI.getSessionData(S_VVIP_FLAG);
						String raCollectionDeskFlag = (String) sessionAPI.getSessionData(S_RA_COLLECTION_DESK_FLAG);

						//Change done for Pulse segmentation
						String isPulseHostInvoked = (String)sessionAPI.getSessionData(S_PULSE_HOST_INVOKED_FLAG);
						String strCallerIdentifiedFlag = (String) sessionAPI.getSessionData(S_CALLER_IDENTIFIED_FLAG);
						strIsPulseFlag=(String)sessionAPI.getSessionData(S_IS_PULSE);
						customerCategoryFlag = (String)sessionAPI.getSessionData(S_CUSTOMER_CATEGORY);

						sessionAPI.addToLog(methodName +"VVIP = "+vvipFlag+"  flagCBDC caller = "+flagCBDCcaller+"  CallerIdentifiedFlag = "+strCallerIdentifiedFlag
								+ " Pulse already Invokedflag = "+isPulseHostInvoked+"  strIsPulseFlag = "+strIsPulseFlag
								+"  customerCategoryFlag = "+customerCategoryFlag,LOG_INFO);

						if(Y.equals(strCallerIdentifiedFlag) && !Y.equals(isPulseHostInvoked) 
								&& !Y.equals(flagCBDCcaller) && !Y.equals(flagNTBdesk) && !Y.equals(strIsPulseFlag)){
							String ucic = getLastSelectedUCIC(sessionAPI);
							PulseDesk.checkPulseEligibility(sessionAPI, ucic);
							strIsPulseFlag=(String)sessionAPI.getSessionData(S_IS_PULSE);
						}

						flagNTBdesk = (String)sessionAPI.getSessionData(S_NTB_DESK_FLAG);
						String forexFlag = (String)sessionAPI.getSessionData(S_FOREX_FLAG);
						String fraudCallerFlag = (String)sessionAPI.getSessionData(S_FRAUD_CALLER);
						strIsCorporateFlag=(String)sessionAPI.getSessionData(S_IS_CORPORATE_CALLER);
						escalationCaller = (String) sessionAPI.getSessionData(S_ESCALATION_SENSITIVE_CALLER);

						sessionAPI.addToLog(methodName +"fraudCaller = "+fraudCallerFlag+"  NTBDesk = " + flagNTBdesk+ "  Forex = "+forexFlag +
								"  escalationCaller = "+escalationCaller +"  Corporate = "+strIsCorporateFlag	+ " Pulse = "+strIsPulseFlag,LOG_INFO);

						String strCallRouterFrom = (String) sessionAPI.getSessionData(S_CALL_ROUTED_FROM);
						String ruralFlag = (String) sessionAPI.getSessionData(S_RURAL_FLAG);
						String flagOLSA = (String) sessionAPI.getSessionData(S_OLSA_FLAG);
						String flagNTB = (String) sessionAPI.getSessionData(S_IS_NTB);
						String payLaterFlag = (String) sessionAPI.getSessionData(S_LOAN_PAY_LATER_FLAG);
						String loanEmiFlag = (String) sessionAPI.getSessionData(S_LOAN_EMI_FLAG);
						String raCollectionFlag = (String) sessionAPI.getSessionData(S_IS_RA_COLLECTION_SKILL_ELIGIBLE);
						loanPLAcctProceedFlag = (String) sessionAPI.getSessionData(S_LOAN_PL_FC_PROCEED_FLAG);
						String settlementSkillFlag = (String) sessionAPI.getSessionData(S_SETTLEMENT_SKILL_FLAG);

						sessionAPI.addToLog(methodName +"strCallRouterFrom = "+strCallRouterFrom + "  Rural FLag = "+ruralFlag+"  OLSA flag = "+flagOLSA+" S_IS_NTB:: "+flagNTB
								+ " S_IS_RA_COLLECTION_SKILL_ELIGIBLE:: "+raCollectionFlag+" loanPLAcctProceedFlag: "+loanPLAcctProceedFlag+" S_SETTLEMENT_SKILL_FLAG:: "+settlementSkillFlag, LOG_INFO);

						if(Y.equals(vvipFlag)){
							strKeyForSkill = VVIP;
						} else if(Y.equals(raCollectionDeskFlag)){
							strKeyForSkill = RA_COLLECTION_DESK;
							sessionAPI.setSessionData(S_OVERFLOW_SKILL, NA);
						}
						else if(Y.equals(flagCBDCcaller)){
							strKeyForSkill = CBDC;
						}
						//Added for staff callers : 06th June 2023
						else if(RL.equals(activeLob) && Y.equalsIgnoreCase(staffFlag)){
							strKeyForSkill = STAFF;
							sessionAPI.setSessionData(S_OVERFLOW_SKILL,NA);
						}
						//Added for RL premium callers
						else if(Y.equalsIgnoreCase(strIsPulseFlag))
						{
							strKeyForSkill = PULSE;
							sessionAPI.addToLog(methodName + "The caller belongs to pulse category and hence no repeat logic is applicable" , LOG_INFO);
							sessionAPI.setSessionData(S_OVERFLOW_SKILL,NA);
						}
						//Added for RA premium callers : 19-Oct-23
						else if(PREMIUM.equalsIgnoreCase(customerCategoryFlag))
						{	
							strKeyForSkill = PREMIUM;
							sessionAPI.addToLog(methodName + "The caller belongs to RA premium category and hence no repeat logic is applicable" , LOG_INFO);
							sessionAPI.setSessionData("S_OVERFLOW_SKILL",NA);
						}
						else if((Y.equalsIgnoreCase(escalationCaller)))
						{
							strKeyForSkill = ESCALLATION;
						}
						else if(Y.equals(flagOLSA) && RL.equals(activeLob)){
							sessionAPI.addToLog(methodName, "OLSA");
							strKeyForSkill = OLSA;
							sessionAPI.setSessionData(S_OVERFLOW_SKILL, NA);
						}
						else if(Y.equalsIgnoreCase(flagNTB)){
							strKeyForSkill = (String) sessionAPI.getSessionData(S_NTB_KEY_SKILL);
							sessionAPI.addToLog(methodName + "S_NTB_KEY_SKILL:: "+strKeyForSkill , LOG_INFO); 
						}

						else{
							if(Y.equalsIgnoreCase(loanPLAcctProceedFlag)){
								strKeyForSkill = LOAN_PL_FC;
								sessionAPI.setSessionData(S_OVERFLOW_SKILL, NA);
								sessionAPI.addToLog(methodName, "LOAN_PL_FC");

							}
							else if(Y.equalsIgnoreCase(raCollectionFlag)) {
								sessionAPI.addToLog(methodName, "RA_COLLECTION_SKILL");
								strKeyForSkill = RA_COLLECTION_SKILL;
								sessionAPI.setSessionData(S_OVERFLOW_SKILL, NA);
							}
							else if(Y.equalsIgnoreCase(settlementSkillFlag)) {
								sessionAPI.addToLog(methodName, "RA_SETTLEMENT_SKILL");
								strKeyForSkill = RA_SETTLEMENT_SKILL;
								sessionAPI.setSessionData(S_OVERFLOW_SKILL, NA);
							}
							else if(Y.equalsIgnoreCase(payLaterFlag)){
								sessionAPI.addToLog(methodName, "RA PAY LATER");
								strKeyForSkill = LOAN_PAY_LATER;
								sessionAPI.setSessionData(S_OVERFLOW_SKILL, NA);
							}
							else if(Y.equalsIgnoreCase(loanEmiFlag)){
								sessionAPI.addToLog(methodName, "LOAN EMI");
								strKeyForSkill = LOAN_EMI;
								sessionAPI.setSessionData(S_OVERFLOW_SKILL, NA);
							}
							else if(BharathBanking.equalsIgnoreCase(strCallRouterFrom) && N.equalsIgnoreCase(strCallerIdentifiedFlag)){
								sessionAPI.addToLog(methodName, "Rural");
								strKeyForSkill = RURAL;
								sessionAPI.setSessionData(S_OVERFLOW_SKILL, NA);

							}else if(Y.equals(ruralFlag)){
								sessionAPI.addToLog(methodName, "Rural");
								strKeyForSkill = RURAL;
								sessionAPI.setSessionData(S_OVERFLOW_SKILL, NA);

							}else if(Y.equals(fraudCallerFlag)){
								strKeyForSkill = FRAUD;
							}
							else if(Y.equalsIgnoreCase(flagNTBdesk)){
								strKeyForSkill=NTB;
							}
							else if(Y.equalsIgnoreCase((String)sessionAPI.getSessionData(S_SENIOR_CITIZEN_FLAG, N))){
								strKeyForSkill=SENIOR_CITIZEN;
							}
							//28-Aug-23 - For forex menu in RL
							else if(Y.equalsIgnoreCase(forexFlag)){
								strKeyForSkill=FOREX;
								sessionAPI.setSessionData(S_OVERFLOW_SKILL, NA);
							}
							else{
								sessionAPI.addToLog(methodName +"Does not fall under any category. Hence default skill !!",LOG_INFO);
								strKeyForSkill=GENERAL;
							}
						}
					}

					//Set SkillName based on field strKeyForSkill from SkillNames.properties
					//strKeyForSkill field is set above based flow gone through in IVR
					skillName = getSkillNameFromProperty(sessionAPI);
					sessionAPI.addToLog(methodName + "skillName As per Flow :: " + skillName, LOG_INFO);

					//Added for CR: S2S desk => 03 MAR 2025
					//Start
					skillName = getSaleOfferSkillName(sessionAPI);
					//end
					
					sessionAPI.addToLog(methodName + "Final skillName :: " + skillName, LOG_INFO);
					sessionAPI.setSessionData(S_SKILL_NAME,skillName);

					if(Y.equals(vvipFlag)){
						strExitState = checkHoliday_businessHours(sessionAPI, VVIP);

					}else{
						
						boolean flagCallerIdentified = isCallerIdentified(sessionAPI);
						boolean flagVRMFlow = false;
						
						if(flagCallerIdentified) {
							flagVRMFlow = getVRMCheckResult(sessionAPI);
							if(flagVRMFlow) {
								strExitState = checkHoliday_businessHours(sessionAPI, VRM);
								strExitState = VRM_ + strExitState;//VRM_BH, VRM_NBH
							}
							else {
								sessionAPI.addToLog(methodName +"VRM check not applicable as not VRM flow" , LOG_INFO);
							}
						}
						else {
							sessionAPI.addToLog(methodName +"VRM check not applicable as not RMN" , LOG_INFO);
						}
						sessionAPI.addToLog(methodName +"flagVRMFlow :: " +flagVRMFlow+" flagCallerIdentified :: " +flagCallerIdentified , LOG_INFO);

						if(!VRM_BH.equals(strExitState)){

							//check LOB wise for business hours/Holiday checking 
							if(RA.equals(activeLob)){
								strExitState = checkHoliday_businessHours(sessionAPI, RA);

							}else if(RL.equals(activeLob)){

								strExitState = BH;

								//Check business hours for Bharat Banking
								List<String> skillList= (List<String>) sessionAPI.getApplicationData(S_RL_BH_SKILLS);
								if(skillList != null && !skillList.isEmpty() && skillList.contains(skillName)){
									//Check for holiday
									sessionAPI.addToLog(methodName + "holiday check to be done as it is in the list of BH skills :: " + skillName, LOG_INFO);
									strExitState = checkHoliday_businessHours(sessionAPI, RL);
								}
								if(NTB_LEAP2UNICORN.equalsIgnoreCase(strKeyForSkill)){
									strExitState = checkBusinessHoursLeap2Unicorn(sessionAPI); //called only for Leap2Unicorn
								}

							}else if(CC.equals(activeLob)){
								sessionAPI.addToLog(methodName +"CreditCard : No Holiday/Business hours check",LOG_INFO);
								strExitState = BH;

							}else{
								sessionAPI.addToLog(methodName +"NonLOB  : Consider Holiday/Businesshours check of RL",LOG_INFO);
								strExitState =  BH;//checkHoliday_businessHours(sessionAPI, RL);
							}

							if(NBH.equalsIgnoreCase(strExitState) || HOLIDAY.equalsIgnoreCase(strExitState) 
									|| PUBLIC_HOLIDAY.equalsIgnoreCase(strExitState) )
							{
								sessionAPI.setSessionData(S_TRANSFER_FLAG, N);
								// Assign the Last Menu Accessed 
								LinkedList<String> listMenuHistory = (LinkedList<String>) sessionAPI.getSessionData(S_LIST_MENU_TRAVERSAL_HISTORY);
								if (null != listMenuHistory && !listMenuHistory.isEmpty()) {
									sessionAPI.setSessionData(VoiceElementsInterface.S_LAST_MENU_ACCESSED, listMenuHistory.getLast());
								}
								sessionAPI.addToLog(methodName + "Non working day today",LOG_INFO); 

							}


							String mobNoWithCountryCode = AppConstants.NA;
							String mobNb = (String) sessionAPI.getSessionData(S_MOBILE_NUMBER);
							if(mobNb != null && !mobNb.equals("") && !mobNb.equals(NA)){
								if(mobNb.length()==10){
									mobNoWithCountryCode = "91"+mobNb;
								}else if(mobNb.length()==11){
									mobNb = mobNb.substring(mobNb.length()-10);
									mobNoWithCountryCode = "91"+mobNb;
								}else{
									mobNoWithCountryCode = mobNb;
								}
								sessionAPI.addToLog(methodName+" Mobile number with country code = "+mobNoWithCountryCode, LOG_INFO);
								sessionAPI.setSessionData(S_MOBILE_NUMBER, mobNoWithCountryCode);
							}

						}else{
							//VRM Businesshours
							strExitState = "VRM_CHECK";
						}
					}
				}else if(APP_TRANSFER.equalsIgnoreCase(strAgentTransKey)){
					sessionAPI.setSessionData(S_TRANSFER_FLAG, Y);
					sessionAPI.setSessionData(IS_AGENT_TRANSFER_FLAG, Y);
					sessionAPI.setSessionData(S_CALLER_INPUT, CALLER_INPUT_APP_TRANSFER);
					strExitState = CALLER_INPUT_APP_TRANSFER;
					sessionAPI.addToLog("Inside APP_TRANSFER "+strExitState);
				}
				else {
					sessionAPI.setSessionData(S_TRANSFER_FLAG, N);
					sessionAPI.addToLog(methodName +"strAgentTransferKey not matched",LOG_INFO);
				}

				if(strExitState.equalsIgnoreCase(BH) || strExitState.equalsIgnoreCase(CALLER_INPUT_APP_TRANSFER)){
					sessionAPI.addToLog(methodName+"Setting ECC Variables ...",LOG_INFO);
					setECCVariables(sessionAPI,elementInfo);
				}		
			}
		}
		catch(Exception e){
			sessionAPI.ErrorLog(e.getMessage(), e);	
			strExitState = ER;
		} 
		finally
		{
			if(Y.equalsIgnoreCase(loanPLAcctProceedFlag) && NBH.equalsIgnoreCase(strExitState)){
				strExitState = NBH_LOAN_PL;
			}
		}
		return strExitState;
	}

	@SuppressWarnings("unchecked")
	private void setECCVariables(SessionAPI sessionAPI, ElementInfo elementInfo) {
		// TODO Auto-generated method stub

		String methodName = "setECCVariables:: ";

		try{
			// Assign the Last Menu Accessed 
			LinkedList<String> listMenuHistory = (LinkedList<String>) sessionAPI.getSessionData(S_LIST_MENU_TRAVERSAL_HISTORY);
			if (null != listMenuHistory && !listMenuHistory.isEmpty()) {
				sessionAPI.setSessionData(VoiceElementsInterface.S_LAST_MENU_ACCESSED, listMenuHistory.getLast());
			}
			transferDecisionMap = elementInfo.getDecisionData();				
			mapTransferDetails = (Map<String,String>)transferDecisionMap.get(strAgentTransKey);
			sessionAPI.addToLog(methodName + "mapTransferDetails :: " + mapTransferDetails, LOG_DEBUG);
			if(mapTransferDetails != null) {
				// Setting ECC values
				for (Iterator<Map.Entry<String,String>> it = mapTransferDetails.entrySet().iterator(); it.hasNext();) {
					Map.Entry<String,String> entry = (Map.Entry<String,String>) it.next();
					String strKey = entry.getKey();
					String arrValue[] = entry.getValue().split(BACK_SLASH_PIPE_SEPARATOR);						
					sessionAPI.addToLog(methodName + strKey + " :: " + Arrays.toString(arrValue),LOG_INFO);
					sessionAPI.setECCVariables(arrValue, strKey);											
				}
				sessionAPI.addToLog(methodName + "ECC Variables are Set Successfully...", LOG_INFO);
			}
		}catch (Exception e) {
			// TODO: handle exception
			sessionAPI.addToLog(methodName + "Unable to set ECC Variables Error", LOG_INFO);
			sessionAPI.ErrorLog(e);
		}
	}

	/**
	 *BOC VRM routing eligibility check
	 * */
	@SuppressWarnings("unchecked")
	private boolean getVRMCheckResult(SessionAPI sessionAPI) {
		// TODO Auto-generated method stub
		boolean flagVRMFlow = false;
		String methodName = "getVRMCheckResult:: ";
		try{
			String vrmAPIFlag = (String)sessionAPI.getSessionData(S_VRM_API_INVOKED_FLAG);
			String vrmRLFlowFlag = (String)sessionAPI.getSessionData(S_VRM_RL_FLOW_FLAG);
			String vrmCheckPriority = (String)sessionAPI.getSessionData(S_VRM_CHECK_PRIORITY);
			String vrmCCFlowFlag = (String)sessionAPI.getSessionData(S_VRM_CC_FLOW_FLAG);
			String vrmCheckPriorityCC = (String)sessionAPI.getSessionData(S_VRM_CHECK_PRIORITY_CC);
			String tfnType = (String)sessionAPI.getSessionData(S_TFN_TYPE);
			List<String> vrmRACheckSkippingList = null;
			Object vrmRACheckSkippingData = sessionAPI.getSessionData(S_BOC_RA_TO_VRM_ROUTING_SKIP_LIST); 
			if (vrmRACheckSkippingData instanceof List) { 
				vrmRACheckSkippingList = (List<String>) vrmRACheckSkippingData; 
			}

			sessionAPI.addToLog(methodName +"TFNType = "+tfnType+" vrmAPIFlag :: " +vrmAPIFlag+ ", vrmRLFlowFlag :: " +vrmRLFlowFlag+ ", vrmCCFlowFlag :: " +vrmCCFlowFlag
					+ ", vrmCheckPriority :: " +vrmCheckPriority +"  ActiveLob = "+activeLob, LOG_INFO);

			if(!Y.equals(vrmAPIFlag)){
				if(Y.equals(vrmRLFlowFlag)){
					if(vrmCheckPriority != null && vrmCheckPriority.contains(strKeyForSkill)){
						//non applicable scenario
						sessionAPI.addToLog(methodName +"VRM Check is skipped for the RL flow :: " + strKeyForSkill, LOG_INFO);
					}
					else{
						flagVRMFlow = true;
						sessionAPI.addToLog(methodName +"RL eligible for VRM Check", LOG_INFO);
					}
				}else if(Y.equals(vrmCCFlowFlag)){
					if(vrmCheckPriorityCC != null && vrmCheckPriorityCC.contains(strKeyForSkill)){
						//non applicable scenario
						sessionAPI.addToLog(methodName +"VRM Check is skipped for the CC flow :: " + strKeyForSkill, LOG_INFO);
					}
					else{
						flagVRMFlow = true;
						sessionAPI.addToLog(methodName +"CC eligible for VRM Check", LOG_INFO);
					}
				}
				else if(RA.equals(activeLob) && !RA_COLLECTION_DESK.equals(tfnType)){
					if(vrmRACheckSkippingList != null && skillName != null && vrmRACheckSkippingList.contains(skillName)){
						//non applicable scenario
						sessionAPI.addToLog(methodName +"VRM Check is skipped for the RA flow :: " + strKeyForSkill +" => "+skillName, LOG_INFO);
					}
					else{
						flagVRMFlow = true;
						sessionAPI.addToLog(methodName +"RA eligible for VRM Check", LOG_INFO);
					}
				}
				else{
					sessionAPI.addToLog(methodName +"VRM Check is skipped due to Non-Applicable(other than RL/CC/RA) flow ", LOG_INFO);
				}
			}
			else{
				sessionAPI.addToLog(methodName +"Already checked VRM businessHrs & VRM validation ", LOG_INFO);
			}

		}catch (Exception e) {
			// TODO: handle exception
			sessionAPI.addToLog(methodName +"VRM check error.  flagVRMFlow = "+flagVRMFlow, LOG_INFO);
			sessionAPI.ErrorLog(e);
		}

		return flagVRMFlow;
	}

	private String getSkillNameFromProperty(SessionAPI sessionAPI) {
		// TODO Auto-generated method stub

		String skillName = null;
		String methodName = "getSkillNameFromProperty:: ";
		try{
			if(skillNameProp != null && (!skillNameProp.isEmpty())) {

				//23-FEB-2022 set skill on language basis

				skillName = skillNameProp.getProperty(strKeyForSkill+"_"+activeLob+"_"+strLang);
				sessionAPI.addToLog(methodName + "Active LOB & language basis skillName [" + strKeyForSkill+"_"+ activeLob + "_" + strLang + "] :: " + skillName, LOG_INFO);
				if(null == skillName || "".equals(skillName)){
					skillName = skillNameProp.getProperty(strKeyForSkill+"_"+activeLob);
					sessionAPI.addToLog(methodName + "Active LOB basis skillName [" + strKeyForSkill+"_"+activeLob + "] :: " + skillName, LOG_INFO);
				}
				if(null == skillName || "".equals(skillName)){
					skillName = skillNameProp.getProperty(strKeyForSkill+"_"+strLang);
					sessionAPI.addToLog(methodName + "Language basis skillName [" + strKeyForSkill+"_"+strLang + "] :: " + skillName, LOG_INFO);
				}
				if(skillName==null || skillName.equals("")){
					skillName = skillNameProp.getProperty(strKeyForSkill);
					sessionAPI.addToLog(methodName + "Key Basis Skill ["+strKeyForSkill+"] :: " + skillName, LOG_INFO);
				}

				if(skillName==null || skillName.equals("")){

					if(Y.equalsIgnoreCase(strIsHotline))
					{
						skillName=(String)sessionAPI.getSessionData(S_HOTLINE_OVERFLOW_SKILL);
						sessionAPI.addToLog(methodName +"Sending default skill of hotline : "+skillName,LOG_INFO);

					}else if(RA.equals(activeLob)){
						skillName = ASSET_NORMAL_SKILL;
						sessionAPI.addToLog(methodName + "Default skillName ["+activeLob+"] :: " + skillName, LOG_INFO);

					}else if(RL.equals(activeLob)){
						skillName = CB_NORMAL_SKILL;
						sessionAPI.addToLog(methodName + "Default skillName ["+activeLob+"] :: " + skillName, LOG_INFO);

					}else if(CC.equals(activeLob)){
						skillName = CC_GENERAL_SKILL;
						sessionAPI.addToLog(methodName + "Default skillName ["+activeLob+"] :: " + skillName, LOG_INFO);

					}else{
						skillName=(String)sessionAPI.getSessionData(S_DEFAULT_SKILL_NAME);
						sessionAPI.addToLog(methodName + "Default skillName [S_DEFAULT_SKILL_NAME] :: " + skillName, LOG_INFO);
					}
				}
			}
			else
			{
				skillName=(String)sessionAPI.getSessionData(S_DEFAULT_SKILL_NAME);
				sessionAPI.addToLog(methodName + "Default skillName [S_DEFAULT_SKILL_NAME] :: " + skillName, LOG_INFO);
			}
		}catch (Exception e) {
			// TODO: handle exception
			if(skillName == null || skillName.isEmpty()){
				skillName = CB_NORMAL_SKILL;
				sessionAPI.addToLog(methodName + "Unable to set skill. hard code skill = "+CB_NORMAL_SKILL, LOG_INFO);
			}else{
				sessionAPI.addToLog(methodName + "Unable to set proper skill. skill = "+skillName, LOG_INFO);
			}
			sessionAPI.ErrorLog(e);
		}

		return skillName;
	}

	//Set Default overflow skill initially based on overflow skill configuration availability basis of Active LOB & Language
	private void setDefaultOverFlowSkill(SessionAPI sessionAPI) {

		String overFlowName = GENERAL;
		String overFlowSkillName = "";
		String methodName = "SetDefaultOverFlowSkill :: ";
		try{
			if(skillNameProp != null && (!skillNameProp.isEmpty())) {

				overFlowSkillName = skillNameProp.getProperty(overFlowName+"_"+activeLob+"_"+strLang);
				sessionAPI.addToLog(methodName + "Active LOB & language basis Overflow skillName [" + overFlowName+"_"+ activeLob + "_" + strLang + "] :: " + overFlowSkillName, LOG_INFO);
				if(null == overFlowSkillName || "".equals(overFlowSkillName)){
					overFlowSkillName = skillNameProp.getProperty(overFlowName+"_"+activeLob);
					sessionAPI.addToLog(methodName + "Active LOB basis Overflow skillName [" + overFlowName+"_"+activeLob + "] :: " + overFlowSkillName, LOG_INFO);
				}
				if(null == overFlowSkillName || "".equals(overFlowSkillName)){
					overFlowSkillName = (String)sessionAPI.getSessionData(S_DEFAULT_SKILL_NAME);
					sessionAPI.addToLog(methodName + "S_DEFAULT_SKILL_NAME :: " + overFlowSkillName, LOG_INFO);
				}
				sessionAPI.setSessionData(S_OVERFLOW_SKILL,overFlowSkillName);
			}else{
				sessionAPI.setSessionData(S_OVERFLOW_SKILL,(String)sessionAPI.getSessionData(S_DEFAULT_SKILL_NAME));
				sessionAPI.addToLog(methodName + "skillNameProp Empty/Null :: S_DEFAULT_SKILL_NAME :: " + (String)sessionAPI.getSessionData(S_DEFAULT_SKILL_NAME), LOG_INFO);
			}
		}catch (Exception e) {
			sessionAPI.setSessionData(S_OVERFLOW_SKILL,(String)sessionAPI.getSessionData(S_DEFAULT_SKILL_NAME));
			sessionAPI.addToLog(methodName + "Exception :: S_DEFAULT_SKILL_NAME :: " + (String)sessionAPI.getSessionData(S_DEFAULT_SKILL_NAME), LOG_INFO);
		}

	}

	//Added for CR: S2S desk => 03 MAR 2025
	/** Checking Sale Offer eligibility from session config: S_SALE_OFFER_ELIGIBLE_FLAG
	 * ignoring/skipping skills flow for setting S2S skill from session config: S_RA_SKILLS_SALES_OFFER_ELIGIBILITY_SKIPPING_LIST
	 * Fetching Skill name using key RA_S2S from skill name property
	 * Setting Overflow skill for S2S skill from session config: S_RA_S2S_DESK_OVERFLOW_SKILL
	 */
	@SuppressWarnings("unchecked")
	private String getSaleOfferSkillName(SessionAPI sessionAPI) {
		// TODO Auto-generated method stub
		String methodName = "S2S Desk :: ";
		String finalSkillName = skillName; //Assigning skill In default as per other flow
		List<String> saleOfferSkippingList = null;
		try{
			String flagS2S = (String) sessionAPI.getSessionData(S_SALE_OFFER_ELIGIBLE_FLAG);
			if(Y.equalsIgnoreCase(flagS2S) && RA.equalsIgnoreCase(activeLob)){

				Object saleOfferSkippingData = sessionAPI.getSessionData(S_RA_SKILLS_SALES_OFFER_ELIGIBILITY_SKIPPING_LIST); 
				if (saleOfferSkippingData instanceof List) { 
					saleOfferSkippingList = (List<String>) saleOfferSkippingData; 
				}
				sessionAPI.addToLog(methodName+"skillName [as per other flow] = "+skillName+"  saleOfferSkippingList = "+saleOfferSkippingList, LOG_INFO);

				boolean s2sRASkillEligibleFlag = true;
				if(null != saleOfferSkippingList && saleOfferSkippingList.size() > 0 && saleOfferSkippingList.contains(skillName)){
					s2sRASkillEligibleFlag = false;
				}

				if(s2sRASkillEligibleFlag){
					String skillNameS2S = skillNameProp.getProperty(RA_S2S);
					String overFlowSkillS2S = (String) sessionAPI.getSessionData(S_RA_S2S_DESK_OVERFLOW_SKILL);
					sessionAPI.addToLog(methodName+"Skill = "+skillNameS2S + " SkillNameKey = " +RA_S2S 
							+ "  OverFlowSkill config = "+overFlowSkillS2S, LOG_INFO);

					if(skillNameS2S != null && !skillNameS2S.isEmpty()){
						finalSkillName = skillNameS2S;
						sessionAPI.setSessionData(S_SALE_OFFER_TRANSFER_FLAG, Y);
						if(overFlowSkillS2S == null || overFlowSkillS2S.isEmpty()){
							sessionAPI.addToLog(methodName+"Set Default RA Normal skill", LOG_INFO);
							overFlowSkillS2S = ASSET_NORMAL_SKILL; // default normal skill for RA LOB
						}
						sessionAPI.setSessionData(S_OVERFLOW_SKILL,overFlowSkillS2S);
					}
				}else{
					sessionAPI.addToLog(methodName+"Skipping to route S2S desk due to Skill = "+skillName, LOG_INFO);
				}
			}else{
				sessionAPI.addToLog(methodName+"Not eligible since S2S Flag = " + flagS2S + "  ActiveLOB = "+activeLob, LOG_INFO);
			}
		}catch (Exception e) {
			sessionAPI.addToLog(methodName+"skill Setting Error =  " + e.getMessage(), LOG_INFO);
		}
		return finalSkillName;
	}

	private String checkHoliday_businessHours(SessionAPI sessionAPI, String lob){
		String strExitState=NBH;
		int currentDayOfWeek = 0;
		String methodName = "checkHoliday_businessHours:: ";
		try{
			Properties bhHolidayProp = (Properties)sessionAPI.getApplicationData(A_BH_HOLIDAY_PROP);
			sessionAPI.addToLog(methodName + "LOB:"+lob+"   Business Hours / Holiday Properties :: " + bhHolidayProp, LOG_INFO);
			if(bhHolidayProp != null && (!bhHolidayProp.isEmpty())) {


				// taking SUNDAY 
				Calendar localCalendar = Calendar.getInstance(TimeZone.getDefault());
				currentDayOfWeek = localCalendar.get(Calendar.DAY_OF_WEEK);
				sessionAPI.addToLog("","Day of Week "+currentDayOfWeek);

				String strPublicHolidayList= bhHolidayProp.getProperty(lob+"_"+PUBLIC_HOLIDAYS);
				if(strPublicHolidayList != null){
					sessionAPI.addToLog("","Public Holidays are :"+strPublicHolidayList);
					// taking today's date to check Public Holiday
					Date datepublicHoliday = new Date();
					SimpleDateFormat sdf = new SimpleDateFormat("ddMM");
					String strPublicholiday=sdf.format(datepublicHoliday);
					sessionAPI.addToLog(methodName+"Current Day: "+strPublicholiday);// Date format eg January 26

					if(!NA.equalsIgnoreCase(strPublicHolidayList) 
							&& strPublicHolidayList.contains(strPublicholiday)){

						sessionAPI.addToLog("  Today is Public holiday :: ");
						strExitState = PUBLIC_HOLIDAY;
					}
				}else{
					sessionAPI.addToLog("Removed Public Holiday check");
				}

				String strWorkingSats = bhHolidayProp.getProperty(lob+"_"+WORKING_SATURDAYS);
				if(strWorkingSats != null){
					sessionAPI.addToLog("","Working Saturdays are :"+strWorkingSats);
					// getting current month 
					int currentMonth = localCalendar.get(Calendar.MONTH) ;
					int month=currentMonth;
					sessionAPI.addToLog("","Current Month: " + month);
					//Take the working sat config from properties and check if not 2nd or 4th saturday				
					int currentWeek = localCalendar.get(Calendar.WEEK_OF_MONTH);
					sessionAPI.addToLog("Current Week ::"+String.valueOf(currentWeek));
					if(!strWorkingSats.contains(String.valueOf(currentWeek)) && currentDayOfWeek == 7 )
					{
						sessionAPI.addToLog("","Second and Fourth Saturday is holiday");
						strExitState = HOLIDAY;
					}
				}else{
					sessionAPI.addToLog("Removed Saturday Holiday check");
				}

				/*else if(currentDayOfWeek == 1)
				{
					sessionAPI.addToLog("Sunday is a holiday");
					strExitState = "HOLIDAY";
				}*/

				sessionAPI.addToLog("Initial ExitState: "+strExitState);

				if(!strExitState.contains(HOLIDAY)){ //check Business hours if its not Holiday
					String strBh = EMPTY;
					String strStartTime=EMPTY;
					String strEndTime=EMPTY;
					String strBHOneday = EMPTY;
					String[] strArrTime = null;

					String[] strBHOneDayArr = null;
					String time = EMPTY;
					String[] strSplitDays = null;
					strBh = bhHolidayProp.getProperty(lob+"_"+BUSINESS_HOURS);
					if(strBh.contains(",")){
						strSplitDays = strBh.split(",");
						strBHOneday= strSplitDays[currentDayOfWeek-1];
					}else{
						sessionAPI.addToLog(methodName+"Business Hours does not contain char - comma");
						strBHOneday = strBh;
					}
					sessionAPI.addToLog(methodName +"strBHOneday ::"+strBHOneday, LOG_INFO);
					if(strBHOneday.contains("~")){
						strBHOneDayArr = strBHOneday.split("\\~");
						sessionAPI.addToLog("strBHOneDayArr Length::"+strBHOneDayArr.length, LOG_DEBUG);
						if(strBHOneDayArr.length<=2){
							sessionAPI.addToLog(methodName +"strBHOneDayArr Length::"+strBHOneDayArr[0], LOG_DEBUG);
							time = strBHOneDayArr[1];
							if(time.contains("|")){
								strArrTime = time.split("\\|");
								sessionAPI.addToLog(methodName +"strArrTime Length::"+strArrTime.length, LOG_DEBUG);
								if(strArrTime.length <= 2){
									strStartTime = strArrTime[0];
									strEndTime = strArrTime[1];
								}else{
									sessionAPI.addToLog(methodName+"cant determine if start or end as it the array more than or less than 2 ");
									strExitState=NBH;
								}
							}else{
								sessionAPI.addToLog(methodName+"Time configured is not specific there is no |  ");
								strExitState=NBH;
							}
						}else{
							sessionAPI.addToLog(methodName+"Cant determine if the time is configured or not");
							strExitState=NBH;
						}
					}else{
						sessionAPI.addToLog(methodName+"does not contain a ~ ");
						strExitState=NBH;
					}
					sessionAPI.addToLog("Start :: End Time "+strStartTime+"::"+strEndTime, LOG_DEBUG);
					if(strStartTime!=null && !strStartTime.isEmpty() && strEndTime!=null && !strEndTime.isEmpty()){
						sessionAPI.addToLog(methodName+"working day "+" BH StartTime: "+strStartTime+" BH EndTime: "+strEndTime+" ", LOG_DEBUG);
						SimpleDateFormat dateFormat = new SimpleDateFormat("HHmm");
						Date date = new Date();
						String strCurrTime =dateFormat.format(date);
						Date currTime = new SimpleDateFormat("HHmm").parse(strCurrTime);
						Date startTime = new SimpleDateFormat("HHmm").parse(strStartTime);
						Date endTime = new SimpleDateFormat("HHmm").parse(strEndTime);

						sessionAPI.addToLog(methodName+"working day "+" CurrentTime: "+currTime+" ", LOG_DEBUG);
						if ((currTime.after(startTime) && currTime.before(endTime)) || currTime.equals(endTime))
						{
							sessionAPI.addToLog(methodName+" Call reached IVR Inbetween Working Hour ");
							strExitState=BH;
						}
						else
						{
							sessionAPI.addToLog(methodName+"Call reached IVR In OOO"+" ");
							strExitState=NBH;
						}
					}else{
						sessionAPI.addToLog(methodName+"Start time or End Time is null"+" ");
						strExitState=NBH;
					}
				}
			}

			else{
				sessionAPI.addToLog(methodName+"Business hours holidays property not loaded properly"+" ");
				strExitState=NBH;
			}

		}catch(Exception e){
			strExitState=NBH;
			sessionAPI.ErrorLog(e);
		}
		finally{
			//Enabling for IDFC testing purpose post production movement
			//Allowing transfer calls to agent based on below Property which contains list of ANIs (calling numbers)
			try{
				if(!BH.equalsIgnoreCase(strExitState)){
					String xferTestAniList = (String)sessionAPI.getSessionData("S_TRANSFER_TESTING_ANI_LIST");
					String cli = (String)sessionAPI.getSessionData(S_CLI);
					sessionAPI.addToLog("ANI = "+cli+"  S_TRANSFER_TESTING_ANI_LIST = "+xferTestAniList, LOG_INFO);
					if(cli != null && cli.length() >= 10 && xferTestAniList != null){
						cli = cli.substring(cli.length()-10, cli.length());
						if(xferTestAniList.contains(cli)){
							strExitState=BH;
							sessionAPI.addToLog("Allowing Transfer for test cli ::"+cli);
						}
					}
				}
			}catch (Exception e) {
			}
		}
		sessionAPI.addToLog(methodName+"Exit ::"+strExitState);
		return strExitState;

	}

	private String checkBusinessHoursLeap2Unicorn(SessionAPI sessionAPI){
		String strExitState = NBH_NTB;
		String methodName = "checkBusinessHoursLeap2Unicorn";
		try{
			Properties bhHolidayProp = (Properties)sessionAPI.getApplicationData(A_BH_HOLIDAY_PROP);
			sessionAPI.addToLog(methodName + "Business Hours / Holiday Properties :: " + bhHolidayProp, LOG_INFO);
			if(bhHolidayProp != null && (!bhHolidayProp.isEmpty())) {
				sessionAPI.addToLog(methodName + "Checking Leap2Unicorn Timing :: ", LOG_INFO);
				String strNTBStart = bhHolidayProp.getProperty("LEAP2UNICORN_"+BUSINESS_HOURS+"_STARTTIME");
				String strNTBEnd = bhHolidayProp.getProperty("LEAP2UNICORN_"+BUSINESS_HOURS+"_ENDTIME");

				sessionAPI.addToLog(methodName+"LEAP2UNICORN_Business_HOURS_STARTTIME : "+strNTBStart+" ", LOG_INFO);
				sessionAPI.addToLog(methodName+"LEAP2UNICORN_Business_HOURS_ENDTIME : "+strNTBEnd+" ", LOG_INFO);

				SimpleDateFormat dateFormat = new SimpleDateFormat("HHmm");
				Date date = new Date();
				String strCurrTime =dateFormat.format(date);
				Date currTime = new SimpleDateFormat("HHmm").parse(strCurrTime);
				Date startTime = new SimpleDateFormat("HHmm").parse(strNTBStart);
				Date endTime = new SimpleDateFormat("HHmm").parse(strNTBEnd);


				sessionAPI.addToLog(methodName+"working day "+" CurrentTime: "+currTime+" ", LOG_DEBUG);

				if (currTime.after(startTime) && currTime.before(endTime))
				{
					sessionAPI.addToLog(methodName+" Call reached IVR Inbetween Working Hour ");
					strExitState=BH;
				}
				else
				{
					sessionAPI.addToLog(methodName+"Call reached IVR In OOO"+" ");
				}


			}
			else{
				sessionAPI.addToLog(methodName+"Business hours holidays property not loaded properly"+" ");
				strExitState=NBH;
			}

		}catch(Exception e){
			strExitState=NBH;
			sessionAPI.ErrorLog(e);
		}
		sessionAPI.addToLog(methodName+" Exit ::"+strExitState);
		return strExitState;

	}

	private String getLastSelectedUCIC(SessionAPI sessionAPI) {
		String methodName = "getLastSelectedUCIC():: ";
		String strUCIC=(String) sessionAPI.getSessionData(S_UCIC);
		String strFirstUCIC=(String) sessionAPI.getSessionData("S_FIRST_UCIC");
		String cdpFirstUCIC = (String) sessionAPI.getSessionData("S_CDP_API_FIRST_UCIC");
		String finalUCIC;
		if(strUCIC != null && !NA.equalsIgnoreCase(strUCIC))
			finalUCIC = strUCIC;
		else if(strFirstUCIC != null && !NA.equalsIgnoreCase(strFirstUCIC))
			finalUCIC = strFirstUCIC;
		else
			finalUCIC = cdpFirstUCIC;
		sessionAPI.addToLog(methodName+"S_UCIC = "+strUCIC+" S_FIRST_UCIC = "+strFirstUCIC+" S_CDP_API_FIRST_UCIC = "+cdpFirstUCIC ,LOG_DEBUG);
		sessionAPI.addToLog(methodName+"Final UCIC = "+finalUCIC,LOG_INFO);
		return finalUCIC;
	}
	
	private boolean isCallerIdentified(SessionAPI sessionAPI) {
		String methodName = "isCallerIdentified():: ";
		boolean value = false;
		String callerIdentifiedFlag = (String) sessionAPI.getSessionData("S_CALLER_IDENTIFIED_FLAG");
		if(Y.equalsIgnoreCase(callerIdentifiedFlag))
			value = true;
		sessionAPI.addToLog(methodName+"callerIdentifiedFlag = "+callerIdentifiedFlag+" value = "+value,LOG_DEBUG);
		return value;
	}
}
