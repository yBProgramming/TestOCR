package yocr.bcel.com.la.main;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import net.sourceforge.tess4j.*;

@SpringBootApplication
@RestController
public class TestOcrApplication {

	public static void main(String[] args) {
		SpringApplication.run(TestOcrApplication.class, args);
	}
	
	@RequestMapping(value = "/test", method=RequestMethod.GET)
	public String testResponse() {
		return "This is string test";
	}
	
	@RequestMapping(value = "/upload/tesserract", method=RequestMethod.POST, consumes = "multipart/form-data")
	public ResponseEntity<String> uploadImageToServer(@RequestParam("image") MultipartFile file){
		String ext = FilenameUtils.getExtension(file.getOriginalFilename());
		/*if (!"png".equals(ext.toLowerCase()) && !"jpg".equals(ext.toLowerCase())) {
			return ResponseEntity.badRequest().build();
		}*/
		ITesseract instance = new Tesseract();
		String result = "";
        try {
        	BufferedImage img = ImageIO.read(file.getInputStream());
        	//File f = new File("");
        	instance.setDatapath("C:\\Program Files (x86)\\Tesseract-OCR\\tessdata");
            result = instance.doOCR(img);
        } catch (TesseractException | IOException e) {
            System.err.println(e.getMessage());
        }
        result=result.replaceAll("[\r\n]+", " ");
        /*if(result.toUpperCase().indexOf("PHAILIN PHOSIPONG MONK") > 0) {
        	System.out.println("Found needed text at " + result.toUpperCase().indexOf("PHAILIN PHOSIPONG MONK"));
        }*/
        System.out.println(result.toUpperCase().split(" ").length);
        String[] strArray = result.toUpperCase().split(" ");
        if(strArray.length > 0) {
        	System.out.println(Arrays.asList(strArray).contains("LAK"));
            List<Integer> foundIndex = new ArrayList<Integer>();
            for(int i = 0; i < strArray.length; i++) {
            	if(strArray[i].trim().equals("LAK")) {
            		foundIndex.add(i);
            	}
            }
            System.out.println(foundIndex.size());
            if(foundIndex.size() > 0) {
            	String strMoney = strArray[foundIndex.get(foundIndex.size() - 1) - 1];
                double money = checkDoubleMoney(strMoney);
                if( money >= 0) {
                	return new ResponseEntity<String>(
                			String.valueOf(money), 
        					null, 
        					HttpStatus.OK
        				);
                } 
                return new ResponseEntity<String>(
                		result, 
    					null, 
    					HttpStatus.OK
    				);
            } 
            return new ResponseEntity<String>(
            		result, 
					null, 
					HttpStatus.OK
				);
        } 
    	return new ResponseEntity<String>(
				result, 
				null, 
				HttpStatus.OK
			);
	}
	
	private double checkDoubleMoney(String str) {
		try {
			System.out.println(str);
			return Double.parseDouble(str.replaceAll(",", ""));
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}

}

