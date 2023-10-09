package com.logicalis.serviceinsight.util.servicenow;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.UsernamePasswordCredentials;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.ws.transport.http.CommonsHttpMessageSender;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logicalis.serviceinsight.data.SNRecord;

@SuppressWarnings("deprecation")
public class SNObjectAdapter {

    private String baseUrl;
    private String objType;
    private String user;
    private String pass;
    private final Logger log = LoggerFactory.getLogger(SNObjectAdapter.class);

    @Autowired
    SaajSoapMessageFactory soapMessageFactory;

    public SNObjectAdapter(String baseUrl, String user, String pass) {
        this.baseUrl = baseUrl;
        this.user = user;
        this.pass = pass;
    }

    private String buildObjUrl(String objUrl) {
        return "/" + objUrl + ".do?SOAP";
    }

    /**
     * Public utility methods for making the different types of calls
     *
     * @param criteria
     * @return
     */
    public List<String> getKeys(String objUrl, HashMap<String, String> criteria) {
        if (criteria == null) {
            throw new IllegalArgumentException("The criteria hashmap argument is required.");
        }
        List<String> keys = new ArrayList<String>();
        String response;
        StringBuffer sb = new StringBuffer();
        sb.append("<getKeys>");
        Iterator it = criteria.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            sb.append("<");
            sb.append(pairs.getKey());
            sb.append(">");
            sb.append(pairs.getValue());
            sb.append("</");
            sb.append(pairs.getKey());
            sb.append(">");
            it.remove(); // avoids a ConcurrentModificationException
        }
        sb.append("</getKeys>");
        response = makeCall(objUrl, sb.toString());
        keys = parseGetKeys(response);
        return keys;
    }

    private List<String> parseGetKeys(String response) {
        List<String> keys = new ArrayList<String>();
        //System.out.println("GetKeys response: " + response);
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document dom = db.parse(new ByteArrayInputStream(response.getBytes("UTF-8")));
            Element docEle = dom.getDocumentElement();
            NodeList nl = docEle.getElementsByTagName("sys_id");
            String keyCsv = nl.item(0).getTextContent();
            StringTokenizer st = new StringTokenizer(keyCsv, ",");
            while (st.hasMoreTokens()) {
                keys.add(st.nextToken());
            }

        } catch (Exception e) {
            log.error("Error parsing Gets Keys Response: {}", response);
            log.error(e.getMessage());
        }
        return keys;
    }

    public SNRecord get(String objUrl, String sys_id) {

        if (sys_id == null) {
            throw new IllegalArgumentException("The sys_id argument is required.");
        }

        SNRecord r = null;
        String response;
        StringBuffer sb = new StringBuffer();

        try {
            sb.append("<get>");
            sb.append("<sys_id>");
            sb.append(sys_id);
            sb.append("</sys_id>");
            sb.append("</get>");
            response = makeCall(objUrl, sb.toString());
            r = parseGetResponse(response);
        } catch (Exception e) {
            //log.error("Exception in get operation for URL, Object, sys_id: {}{}{}",this.baseurl,this.objUrl,sys_id);
            log.error("Exception message: {}", e.getMessage());
        }
        return r;
    }

    @Deprecated
    public List<SNRecord> getSNRecords(String objUrl, HashMap<String, String> criteria) {
        return getRecords(objUrl, criteria);
    }

    public List<SNRecord> getRecords(String objUrl, HashMap<String, String> criteria) {
        List<SNRecord> SNRecords = new ArrayList<SNRecord>();
        String response;
        StringBuffer sb = new StringBuffer();

        //need to set a limit to ask for more records, as SN caps it at 250 by default
        criteria.put("__limit", "4000");
        
        sb.append("<getRecords>");
        Iterator it = criteria.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            sb.append("<");
            sb.append(pairs.getKey());
            sb.append(">");
            sb.append(pairs.getValue());
            sb.append("</");
            sb.append(pairs.getKey());
            sb.append(">");
            it.remove(); // avoids a ConcurrentModificationException
        }
        sb.append("</getRecords>");
        response = makeCall(objUrl, sb.toString());
        //log.info(response);
        SNRecords = parseGetRecordsResponse(response);

        return SNRecords;
    }

    public String update(String objUrl, HashMap<String, String> attributes) {
        if (!attributes.containsKey("sys_id")) {
            throw new IllegalArgumentException("The HashMap must contain the sys_id attribute");
        } else {
            if (attributes.get("sys_id") == null) {
                throw new IllegalArgumentException("The sys_id value must not be null.");
            }
        }
        String sysId = "";
        String response;
        StringBuffer sb = new StringBuffer();

        sb.append("<update>");
        Iterator it = attributes.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            sb.append("<");
            sb.append(pairs.getKey());
            sb.append(">");
            sb.append(pairs.getValue());
            sb.append("</");
            sb.append(pairs.getKey());
            sb.append(">");
            it.remove(); // avoids a ConcurrentModificationException
        }
        sb.append("</update>");
        //System.out.println("Message: " + sb.toString());
        response = makeCall(objUrl, sb.toString());
        System.out.println(response);
        sysId = parseInsertResponse(response);
        return sysId;
    }

    public String insert(String objUrl, HashMap<String, String> attributes) {//TODO Error handling and logging
        String sysId = "";
        String response;
        StringBuffer sb = new StringBuffer();

        sb.append("<insert>");
        Iterator it = attributes.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            sb.append("<");
            sb.append(pairs.getKey());
            sb.append(">");
            sb.append(pairs.getValue());
            sb.append("</");
            sb.append(pairs.getKey());
            sb.append(">");
            it.remove(); // avoids a ConcurrentModificationException
        }
        sb.append("</insert>");
        //System.out.println("Message: " + sb.toString());
        response = makeCall(objUrl, sb.toString());
        System.out.println(response);
        sysId = parseInsertResponse(response);
        return sysId;
    }

    public SNRecord execute(String objUrl, HashMap<String, String> attributes) {//TODO Error Handling and logging
        String response;
        StringBuffer sb = new StringBuffer();

        sb.append("<execute>");
        Iterator it = attributes.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            sb.append("<");
            sb.append(pairs.getKey());
            sb.append(">");
            sb.append(pairs.getValue());
            sb.append("</");
            sb.append(pairs.getKey());
            sb.append(">");
            it.remove(); // avoids a ConcurrentModificationException
        }
        sb.append("</execute>");
        log.debug("Execute Request: {}", sb.toString());
        response = makeCall(objUrl, sb.toString());
        log.debug("Execute Response: {}", response);
        SNRecord r = parseGetResponse(response);
        return r;
    }

    public void deleteMultiple(HashMap<String, String> attributed) {//TODO Everything delete
        String response = null;
        log.debug("Delete Multiple Response: {}", response);
    }

    /**
     * This private function makes the actual call to Service Now using the
     * message payload constructed by the calling public utility method
     *
     * @param message
     * @return
     */
    private String makeCall(String objUrl, String message) {
        //TODO error handling and logging
        String response = "";
        try {
        	if(objUrl == null) {
        		throw new Exception("SN objUrl cannot be null, as we don't know which SN Table to access.");
        	}
        	
            WebServiceTemplate webServiceTemplate = new WebServiceTemplate();
            String defaultUri = baseUrl + buildObjUrl(objUrl);
            webServiceTemplate.setDefaultUri(defaultUri);
            webServiceTemplate.setMessageFactory((WebServiceMessageFactory) soapMessageFactory);

            // setup a custom sender with basic login credentials
            Credentials creds = new UsernamePasswordCredentials(user, pass);
            CommonsHttpMessageSender sender = new CommonsHttpMessageSender();
            sender.setHttpClient(new org.apache.commons.httpclient.HttpClient());
            sender.getHttpClient().getParams().setAuthenticationPreemptive(true);
            sender.setCredentials(creds);
            sender.afterPropertiesSet();

            webServiceTemplate.setMessageSender(sender);

            // prepare the request and response
            StreamSource source = new StreamSource(new StringReader(message));
            StringWriter sw = new StringWriter();
            StreamResult result = new StreamResult(sw);

            //launch the webservice		
            webServiceTemplate.sendSourceAndReceiveToResult(defaultUri, source, result);
            response = sw.getBuffer().toString();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error making call for message: {}", message);
            log.error(e.getMessage());
        }
        return response;
    }

    private String parseInsertResponse(String response) {
        String sysId = "";

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document dom = db.parse(new ByteArrayInputStream(response.getBytes("UTF-8")));
            Element docEle = dom.getDocumentElement();
            NodeList nl = docEle.getElementsByTagName("sys_id");
            Node node = nl.item(0);
            sysId = node.getTextContent();
        } catch (Exception e) {
            log.error("Error parsing Insert Response: {}", response);
        }

        return sysId;
    }

    private String parseExecuteResponse(String response) {//TODO is this needed? we are parsing using insert response which might not make the most sense
        String sysId = "";

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document dom = db.parse(new ByteArrayInputStream(response.getBytes("UTF-8")));
            Element docEle = dom.getDocumentElement();
            NodeList nl = docEle.getElementsByTagName("sysID");
            Node node = nl.item(0);
            sysId = node.getTextContent();
        } catch (Exception e) {
            log.error("Error parsing Execute Response: {}", response);
        }
        return sysId;
    }

    private List<SNRecord> parseGetRecordsResponse(String response) {
        List<SNRecord> SNRecords = new ArrayList<SNRecord>();
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document dom = db.parse(new ByteArrayInputStream(response.getBytes("UTF-8")));
            Element docEle = dom.getDocumentElement();
            NodeList nl = docEle.getElementsByTagName("getRecordsResult");
            for (int i = 0; i < nl.getLength(); i++) {
                SNRecord r = new SNRecord();
                r.setObjType(objType);
                HashMap<String, String> attribs = buildHashMap(nl.item(i));
                r.setAttributes(attribs);
                SNRecords.add(r);
            }

        } catch (Exception e) {
            log.error("Error parsing Gets Records Response: {}", response);
        }
        return SNRecords;
    }

    /**
     * This method is used by the get request which takes one sys_id as it's
     * parameter
     */
    private SNRecord parseGetResponse(String response) {
        SNRecord r = null;
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document dom = db.parse(new ByteArrayInputStream(response.getBytes("UTF-8")));
            Element docEle = dom.getDocumentElement();
            if (docEle.hasChildNodes()) {
                r = new SNRecord();
                r.setObjType(objType);
                HashMap<String, String> attribs = buildHashMap(docEle);
                r.setAttributes(attribs);
            } else {
                log.info("No child nodes in response: ", response);
            }
        } catch (Exception e) {
            log.error("Exception in parseGetResponse: {}", e.getMessage());
        }
        return r;
    }

    /**
     * Utility Functions for Processing XML Response Data
     *
     * @param n
     * @param tagname
     * @return
     */
    public static String getElement(Node n, String tagname) {
        Element tmpEl = (Element) n;
        NodeList tmpNL = tmpEl.getElementsByTagName(tagname);
        return tmpNL.item(0).getFirstChild().getNodeValue();
    }

    public static String getElement(Element tmpEl, String tagname) {
        NodeList tmpNL = tmpEl.getElementsByTagName(tagname);
        if (tmpNL == null || tmpNL.item(0) == null) {
            return null;
        }
        return tmpNL.item(0).getFirstChild().getNodeValue();
    }

    /**
     * Used in parsing a SNRecord response, this puts info from all child nodes
     * of the node passed in, placing them in a hashmap for inclusion in a
     * SNRecord
     *
     * @param item
     * @return
     */
    private HashMap<String, String> buildHashMap(Node item) {
        HashMap<String, String> attribs = new HashMap<String, String>();
        NodeList nl = item.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            attribs.put(nl.item(i).getNodeName(), nl.item(i).getTextContent());
        }
        return attribs;
    }
}
