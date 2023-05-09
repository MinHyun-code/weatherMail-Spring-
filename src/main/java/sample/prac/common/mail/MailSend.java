package sample.prac.common.mail;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.springframework.beans.factory.annotation.Value;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MailSend {

    public void MailSend(JSONObject jsonObj) {

        Properties prop = System.getProperties();
        prop.put("mail.smtp.starttls.enable", "true");
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.port", "587");

        Authenticator auth = new MailAuth();

        Session session = Session.getDefaultInstance(prop, auth);

        MimeMessage msg = new MimeMessage(session);
        
        // 현재 날짜 구하기
        LocalDate now = LocalDate.now();
        // 포맷 정의
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        // 포맷 적용
        String formatedNow = now.format(formatter);

        try {
        	String mailText = mailText(jsonObj);
            msg.setSentDate(new Date());
            msg.setFrom(new InternetAddress("mhan@gmail.com", "날씨요정"));
            InternetAddress to = new InternetAddress("mhan@bsgglobal.com");
            msg.setRecipient(Message.RecipientType.TO, to);            
//            msg.setRecipients(Message.RecipientType.CC, "hwjo@bsgglobal.com"); 
            msg.setSubject(formatedNow + " 날씨", "UTF-8");       
            msg.setContent(mailText, "text/html; charset=utf-8");
            Transport.send(msg);
            
        } catch(AddressException ae) {            
            System.out.println("AddressException : " + ae.getMessage());           
        } catch(MessagingException me) {            
            System.out.println("MessagingException : " + me.getMessage());
        } catch(UnsupportedEncodingException e) {
            System.out.println("UnsupportedEncodingException : " + e.getMessage());
        }
    }
    
    public String mailText(JSONObject jsonObj) {

    	Map<String, Object> map = null;
		String text = "";
		String header = "";
		String body = "";
		
		String col1 = "<tr style=\"border: 1px solid #9e9e9e82;\"> <td style=\"border: 1px solid #9e9e9e82;\">시간(H)</td>";
		String col2 = "<tr style=\"border: 1px solid #9e9e9e82;\"> <td style=\"border: 1px solid #9e9e9e82;\">기온(℃)</td>";
		String col3 = "<tr style=\"border: 1px solid #9e9e9e82;\"> <td style=\"border: 1px solid #9e9e9e82;\">풍향(deg)</td>";
		String col4 = "<tr style=\"border: 1px solid #9e9e9e82;\"> <td style=\"border: 1px solid #9e9e9e82;\">풍속(m/s)</td>";
		String col5 = "<tr style=\"border: 1px solid #9e9e9e82;\"> <td style=\"border: 1px solid #9e9e9e82;\">하늘상태</td>";
		String col6 = "<tr style=\"border: 1px solid #9e9e9e82;\"> <td style=\"border: 1px solid #9e9e9e82;\">강수확률(%)</td>";
		String col7 = "<tr style=\"border: 1px solid #9e9e9e82;\"> <td style=\"border: 1px solid #9e9e9e82;\">강수량(mm)</td>";
		String col8 = "<tr style=\"border: 1px solid #9e9e9e82;\"> <td style=\"border: 1px solid #9e9e9e82;\">습도(%)</td>";
		
    	try {
			// JSONObject to Map
    		map = new ObjectMapper().readValue(jsonObj.toString(), Map.class);
    		List<Map<String, Object>> data =  (List<Map<String, Object>>) ((HashMap<String, Object>)((HashMap<String, Object>)((HashMap<String, Object>)((HashMap<String, Object>) map.get("result")).get("response")).get("body")).get("items")).get("item");
    	    
    		JSONArray jsonArray = new JSONArray();

    		// List<Map> to JSONArray
    		for( Map<String,Object> map2 : data){

    		    JSONObject json2 = new JSONObject();

    		    for( Map.Entry<String,Object> entry : map2.entrySet() ){
    		        String key = entry.getKey();
    		        Object value = entry.getValue();
    		        json2.put(key, value);	
    		    }
		        jsonArray.add(  json2 );
    		}
    		

    		System.out.println(jsonArray);
    		
            // 현재 날짜 구하기
            LocalDate now = LocalDate.now();
            // 포맷 정의
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            // 포맷 적용
            String formatedNow = now.format(formatter);
    		
    		for(int i=0; i<jsonArray.size(); i++) {
        		JSONObject index = (JSONObject) jsonArray.get(i);
        		
        		String category = (String) index.get("category");
        		String fcstDate = (String) index.get("fcstDate");
        		String fcstTime = ((String) index.get("fcstTime")).substring(0,2);
        		String fcstValue = (String) index.get("fcstValue");
        		
        		// 오늘 일자만 조회
        		if(Integer.parseInt(formatedNow) >= Integer.parseInt(fcstDate)) {
            		
        			// 1시간 기온
        			if(category.equals("TMP")) {
                		col1 += "<td style=\"border: 1px solid #9e9e9e82;\">" + fcstTime + "<td/>";	
                		col2 += "<td style=\"border: 1px solid #9e9e9e82;\">" + fcstValue + "<td/>";	
        			} 
        			// 풍향
        			else if(category.equals("VEC")) {
                		col3 += "<td style=\"border: 1px solid #9e9e9e82;\">" + fcstValue + "<td/>";
        			}
        			// 풍속
        			else if(category.equals("WSD")) {
        				col4 += "<td style=\"border: 1px solid #9e9e9e82;\">" + fcstValue + "<td/>";
        			}
        			// 하늘상태
        			else if(category.equals("SKY")) {
        				if(fcstValue.equals("1")) {
            				col5 += "<td style=\"border: 1px solid #9e9e9e82;\">맑음<td/>";
        				} else if(fcstValue.equals("3")) {
            				col5 += "<td style=\"border: 1px solid #9e9e9e82;\">구름많음<td/>";
        				} else {
            				col5 += "<td style=\"border: 1px solid #9e9e9e82;\">흐림<td/>";
        				}
        			}
        			// 강수확률
        			else if(category.equals("POP")) {
        				col6 += "<td style=\"border: 1px solid #9e9e9e82;\">" + fcstValue + "<td/>";
        			}
        			// 1시간 강수량
        			else if(category.equals("PCP")) {
        				if(fcstValue.equals("강수없음")) {
            				col7 += "<td style=\"border: 1px solid #9e9e9e82;\">0<td/>";
        				} else {
        				col7 += "<td style=\"border: 1px solid #9e9e9e82;\">" + fcstValue + "<td/>";
        				}
        			}
        			// 습도
        			else if(category.equals("REH")) {
        				col8 += "<td style=\"border: 1px solid #9e9e9e82;\">" + fcstValue + "<td/>";
        			}
        		}

    		}
			
    		
    		header += "<meta name=\"generator\" content=\"Namo WebEditor(Trial)\">\n";
    		header += "<h2 style=\"text-align: center;\">" + formatedNow + " 날씨</h2>";
    		
    		body = "<table style=\"border-collapse: collapse; margin: auto; text-align: center; height: 300px; width: 1500px;\">" + col1 + "<tr/>" + col2 + "<tr/>" + col3 + "<tr/>" + col4 + "<tr/>" + col5 + "<tr/>" + col6 + "<tr/>" + col7 + "<tr/>" + col8 + "<tr/>" + "<table/>"; 
    		
    		
    		text = header + body;
    		
    		// 날짜, 시간
    		// 1시간 기온, 풍향, 풍속, 하늘상태, 강수확률, 1시간 강수, 습도
    		
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return text;
    }
}