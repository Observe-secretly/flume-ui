package com.flume.ui.service;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.flume.ui.resource.util.FlumeResource;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Service
public class FlumeUiService {
    
    private static final String SOURCE_PATH = "com/flume/ui/resource/source";
    
    private static final String CHANNEL_PATH = "com/flume/ui/resource/channel";
    
    private static final String SINK_PATH = "com/flume/ui/resource/sink";
    
    private static final String SOURCEB = "source";
    
    private static final String CHANNELB = "channel";
    
    private static final String SINKB = "sink";
    
    private static final String SAVED = "saved";
    
    private static final String SUFIX_SOURCE = "Source";
    
    private static final String SUFIX_CHANNEL = "Channel";
    
    private static final String SUFIX_SINK = "Sink";
    
    private static final String MANDATORY_FIELD = "_ISMANDATORY";
    
    private static final String DOT = ".";
    
    private static final String EQUALS = " = ";
    
    private static String CR = System.getProperty("line.separator");
    
    private static final JsonParser jsonParser = new JsonParser();
    
    public String getFlumeConfiguration(final String jsonConf) {
	JsonObject jConfObj = (JsonObject) jsonParser.parse(jsonConf);
	String sourceName = null;
	List<String> channelName = new ArrayList<>();
	List<String> sinkName = new ArrayList<>();
	sourceName = fillNames(jConfObj, sourceName, channelName, sinkName);
	
	JsonObject innSource = (JsonObject) jConfObj.get(sourceName);
	
	String flowName = innSource.get("appname").getAsString().trim();
	
	String sourceString = flowName + ".sources";
	String channelString = flowName + ".channels";
	
	StringBuilder sourceBuilder = new StringBuilder(sourceString).append(EQUALS);
	StringBuilder channelBuilder = new StringBuilder(channelString).append(EQUALS);
	StringBuilder channelInformationBuilder = new StringBuilder();
	
	getCollectorConfig(jConfObj, channelName, innSource, flowName, sourceString, channelString, sourceBuilder,
		channelBuilder, channelInformationBuilder);

	
	return sourceBuilder.append(channelBuilder).append(channelInformationBuilder).toString();

    }

    private String fillNames(JsonObject jConfObj, String sourceName, List<String> channelName, List<String> sinkName) {
	for (Map.Entry<String, JsonElement> entry : jConfObj.entrySet()) {
	  if (entry.getKey().toLowerCase().contains(SOURCEB) && 
		  entry.getKey().toLowerCase().contains(SAVED)) {
	      sourceName = entry.getKey();
	  } else if ((entry.getKey().toLowerCase().contains(CHANNELB) && !entry.getKey().toLowerCase().contains(SINKB)) && 
		  entry.getKey().toLowerCase().contains(SAVED)) {
	          channelName.add(entry.getKey());
	  } else if (entry.getKey().toLowerCase().contains(SINKB) && 
		  entry.getKey().toLowerCase().contains(SAVED)) {
	          sinkName.add(entry.getKey());
          }
	}
	return sourceName;
    }

    private void getCollectorConfig(JsonObject jConfObj, List<String> channelName, JsonObject innSource,
	    String flowName, String sourceString, String channelString, StringBuilder sourceBuilder,
	    StringBuilder channelBuilder, StringBuilder channelInformationBuilder) {
	for(String channNam : channelName) {
	    channelBuilder.append(jConfObj.get(channNam).getAsJsonObject().get("channelname").getAsString()).append(" ");
	    extractElements(jConfObj.get(channNam).getAsJsonObject(), jConfObj.get(channNam).getAsJsonObject().get("channelname").getAsString()
		    , channelString, channelInformationBuilder, CHANNELB);
	}
        sourceBuilder.append(flowName).append(SOURCEB).append(" "); 
        
	extractElements(innSource, flowName, sourceString, sourceBuilder, SOURCEB);
    }

    private void extractElements(JsonObject innSource, String flowName, String sourceString,
	    StringBuilder builder, String type) {
	JsonObject sours = (JsonObject) innSource.get("stored");
	 builder.append(CR);
	   for (Map.Entry<String, JsonElement> entry : sours.entrySet()) {
	       if ("serializers".equals(entry.getKey())) {
		   
	       } else if ("converters".equals(entry.getKey())) {
		   
	       } else if ("interceptors".equals(entry.getKey())) {
		   
	       } else if ("selectors".equals(entry.getKey())) {
		   
	       } else {
		   if (!"".equals(entry.getValue().getAsString())) {
		       builder.append(sourceString).append(DOT)
		   .append(flowName);
		    if(SOURCEB.equals(type)) {
		     builder.append(type);
		    }
		    builder.append(DOT).append(entry.getKey())
	           .append(EQUALS).append(entry.getValue().getAsString()).append(CR);
		 } 
	       }
	       
	   }
	   builder.append(CR);
    }
    
    /**
     * Returns a list element with all available <b>Source</b> Flume resources.
     * @return
     * @throws IOException
     */
    public List<String> getSourceNames() throws IOException {
	return getResourcesName(SOURCE_PATH);
    }

    /**
     * Returns a list element with all available <b>Channel</b> Flume resources.
     * @return
     * @throws IOException
     */
    public List<String> getChannelNames() throws IOException {
	return getResourcesName(CHANNEL_PATH);
    }

    /**
     * Returns a list element with all available <b>Sink</b> Flume resources.
     * @return
     * @throws IOException
     */
    public List<String> getSinkNames() throws IOException {
	return getResourcesName(SINK_PATH);
    }

    
    private List<String> getResourcesName(final String packageName) throws IOException {
	final List<String> resourcesNames = new ArrayList<>();
	final Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(packageName);
	while (resources.hasMoreElements()) {
		final URL url = resources.nextElement();
		final String allClassesString = StringUtils.replace(new Scanner((InputStream) url.getContent()).useDelimiter("\\A").next(), ".class", "");
		if (StringUtils.isNotEmpty(allClassesString)) {
			final String[] classesList = allClassesString.split("\n");
			for (String beanClass : classesList) {
				if (beanClass.contains(SUFIX_SOURCE)) {
					resourcesNames.add(StringUtils.replace(beanClass, SUFIX_SOURCE, " " + SUFIX_SOURCE));
				} else if (beanClass.contains(SUFIX_CHANNEL)) {
					resourcesNames.add(StringUtils.replace(beanClass, SUFIX_CHANNEL, " " + SUFIX_CHANNEL));
				} else if (beanClass.contains(SUFIX_SINK)) {
					resourcesNames.add(StringUtils.replace(beanClass, SUFIX_SINK, " " + SUFIX_SINK));
				}
			}
		}
	}
	return resourcesNames;
    }
    
    /**
     * Having the class name and the package location, retrieves all possible attributes.
     * @param className
     * @param fqn
     * @return
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws InstantiationException
     */
    public Map<String, Object> getAttrib(final String className, final String fqn)
		throws ClassNotFoundException, IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {
	Class<?> cls = Class.forName(fqn + className);
	return showField(cls);
    }
    
    private Map<String, Object> showField(Class clazz) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {
	Map<String, Object> attr = new LinkedHashMap<>();
	for (Field field : clazz.getDeclaredFields()) {
		if (field.isAnnotationPresent(FlumeResource.class)) {
			String property;
			if (field.getAnnotation(FlumeResource.class).mandatory()) {
				property = field.getAnnotation(FlumeResource.class).name() + MANDATORY_FIELD;
			} else {
				property = field.getAnnotation(FlumeResource.class).name();
			}
			attr.put(property, field.getAnnotation(FlumeResource.class).value());

		} else {
			field.setAccessible(true);
			String property = field.getName();
			String methodName = "get" + property.substring(0, 1).toUpperCase() + property.substring(1, property.length());
			Method method = clazz.getMethod(methodName, null);
			if (method.invoke(clazz.newInstance(), null) != null) {
				attr.put(field.getName(), method.invoke(clazz.newInstance(), null));
			} else {
				attr.put(field.getName(), "");
			}

		}
	}
	return attr;
    }

}
