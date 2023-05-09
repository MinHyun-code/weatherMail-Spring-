package sample.prac.common.scheduler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

import org.json.simple.JSONObject;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import sample.prac.common.mail.MailSend;

@Service
public class SchedulerService {
    
    @Scheduled(cron="0 0 7 * * ?") 
    public void scheduleRun() throws Exception{
        // ���� ��¥ ���ϱ�
        LocalDate now = LocalDate.now();
 
        // ���� ����
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
 
        // ���� ����
        String formatedNow = now.format(formatter);
		
        /* 
            @ API LIST ~
            
            getUltraSrtNcst �ʴܱ��Ȳ��ȸ 
            getUltraSrtFcst �ʴܱ⿹����ȸ 
            getVilageFcst ���׿�����ȸ 
            getFcstVersion ����������ȸ
        */
        String url = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst"
            + "?serviceKey=o1XWa/wNX6sTQmXcKT2xFycDsqUsQxo3cqPS3bypU/xZtKNP80DPIc5Or2fqd0MDPmpUygvb3lxlIrVJfAQ6xQ=="
            + ""
            + "&dataType=JSON"            // JSON, XML
            + "&numOfRows=810"            // ������ ROWS
//            + "&pageNo=1"                 // ������ ��ȣ
            + "&base_date=" + formatedNow // ��ǥ����
            + "&base_time=0500"           // ��ǥ�ð�
            + "&nx=61"                    // �������� X ��ǥ
            + "&ny=125";                  // �������� Y ��ǥ
        
        HashMap<String, Object> resultMap = getDataFromJson(url, "UTF-8", "get", "");
        
        System.out.println(url);
        
        System.out.println("# RESULT : " + resultMap);

        JSONObject jsonObj = new JSONObject();
        
        jsonObj.put("result", resultMap);
        
        MailSend mailSend = new MailSend();
        
        mailSend.MailSend(jsonObj);
    }
    
    public HashMap<String, Object> getDataFromJson(String url, String encoding, String type, String jsonStr) throws Exception {
        boolean isPost = false;

        if ("post".equals(type)) {
            isPost = true;
        } else {
            url = "".equals(jsonStr) ? url : url + "?request=" + jsonStr;
        }

        return getStringFromURL(url, encoding, isPost, jsonStr, "application/json");
    }
    
    public HashMap<String, Object> getStringFromURL(String url, String encoding, boolean isPost, String parameter, String contentType) throws Exception {
        URL apiURL = new URL(url);

        HttpURLConnection conn = null;
        BufferedReader br = null;
        BufferedWriter bw = null;

        HashMap<String, Object> resultMap = new HashMap<String, Object>();

        try {
            conn = (HttpURLConnection) apiURL.openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setDoOutput(true);

            if (isPost) {
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", contentType);
                conn.setRequestProperty("Accept", "*/*");
            } else {
                conn.setRequestMethod("GET");
            }

            conn.connect();

            if (isPost) {
                bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream(), "UTF-8"));
                bw.write(parameter);
                bw.flush();
                bw = null;
            }

            br = new BufferedReader(new InputStreamReader(conn.getInputStream(), encoding));

            String line = null;

            StringBuffer result = new StringBuffer();

            while ((line=br.readLine()) != null) result.append(line);

            ObjectMapper mapper = new ObjectMapper();

            resultMap = mapper.readValue(result.toString(), HashMap.class);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(url + " interface failed" + e.toString());
        } finally {
            if (conn != null) conn.disconnect();
            if (br != null) br.close();
            if (bw != null) bw.close();
        }

        return resultMap;
    }
    
}
