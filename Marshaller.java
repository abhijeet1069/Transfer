package com.servionsfw.framework.loader.util;

import java.io.File;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.SAXParseException;

import com.servionsfw.framework.loader.common.ApplicationAPI;

public class Marshaller {
	public <Generic>Map<String,Generic> readXML(String strConfigPath,String strFolderLocation,String strXSDLocation,Class<Generic> clazz,JAXBContext context,Unmarshaller um){
		Map <String,Generic> mapGeneric=new CollectionProvider().getGenericMap1();
		setXMLData(strConfigPath, strFolderLocation, strXSDLocation, clazz, mapGeneric,context,um);
		return mapGeneric;
	}
	private <Generic> void setXMLData(String strConfigPath,String strFolderLocation,String strXSDLocation,Class<Generic> clazz,Map <String,Generic> mapGeneric
			,JAXBContext context,Unmarshaller um){
		File audioFolder = new File(strConfigPath+ strFolderLocation);
		if(audioFolder.exists()){
			if(audioFolder.isDirectory()){
				File[] arrAudioFiles = audioFolder.listFiles();
				for (File audioFile : arrAudioFiles) {
					try{
						if(audioFile.isDirectory()){
							setXMLData(strConfigPath, strFolderLocation+"\\"+audioFile.getName(), strXSDLocation, clazz,mapGeneric,context,um);
						}else{
							//ApplicationAPI.setElementName(audioFile.getName());
							mapGeneric.put(audioFile.getName(),(Generic)loadXML(strConfigPath, strXSDLocation, audioFile,clazz,context,um));	
						}
					}catch(Exception e){
						ApplicationAPI.ErrorLog("Exception Occured While loading the file name",audioFolder.getName());			
					}
				}
			}else{
				//ApplicationAPI.setElementName(audioFolder.getName());
				mapGeneric.put(audioFolder.getName(),(Generic)loadXML(strConfigPath,strXSDLocation, audioFolder,clazz,context,um));
			}	
		}else{
//			ApplicationAPI.ErrorLog("FileNotFound in location : "+strConfigPath+strFolderLocation);
		}	
	}
	public <Generic> Generic loadXML(String strConfigPath, String strXSDLocation, File audioFile,Class<Generic> bean,JAXBContext context,Unmarshaller um) {
		Generic generic = null;
		if (audioFile.isFile()) {
			if (xmlValidation(strXSDLocation,audioFile.getAbsolutePath(),audioFile.getName())) {
				generic = xmlParser(bean,audioFile.getAbsolutePath(),context,um);
			} else {
				ApplicationAPI.ErrorLog(" : validation failed, Kindly check the application logs, XSD location : "+strXSDLocation+" , XML Location : "+strConfigPath,audioFile.getName());
			}
		}else {
			ApplicationAPI.ErrorLog("cannot be validated, Found folder in place of xml ",audioFile.getName());
		}
		return generic;
	}

	public boolean xmlValidation(String strXSDPath, String strXMLPath,String strFileName) {
		try {
			SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = factory.newSchema(new File(strXSDPath));
			Validator validator = schema.newValidator();
			validator.validate(new StreamSource(new File(strXMLPath)));
		}catch (SAXParseException e) {
			int intLineNumber = e.getLineNumber();
			int intColumnNumber = e.getColumnNumber();
			String strMessage = "Exception occurs at the Line Number "+ intLineNumber + " and in the column " + intColumnNumber+ " and the Message is " + e.getMessage();
			ApplicationAPI.ErrorLog("================================================================================================================",strXMLPath);
			ApplicationAPI.ErrorLog(strMessage,strXMLPath);
			ApplicationAPI.ErrorLog("================================================================================================================",strXMLPath);
			return false;
		}catch (Exception e) {
			ApplicationAPI.ErrorLog("Exception Occured while validation : ", e,strXMLPath);
			return false;
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	public  <Generic> Generic xmlParser(Class<Generic> clazz, String FileLocaion,JAXBContext context,Unmarshaller um) {
		Generic generic = null;
		try {
			File file = new File(FileLocaion);
			if(context==null){
				context=JAXBContext.newInstance(clazz);
				um=context.createUnmarshaller();
			}
			/** Umarshaller is used to convert xml into java bean object** */
			generic = (Generic) um.unmarshal(file);
		} catch (Exception e) {
			ApplicationAPI.ErrorLog("Exception Occured while marshalling : "+FileLocaion, e,FileLocaion);
		}
		return generic;
	}
}
