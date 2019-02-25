package yocr.bcel.com.la.main;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.tomcat.util.codec.binary.Base64;
import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.opencv_bioinspired.Retina;
import org.bytedeco.javacpp.opencv_face.FaceRecognizer;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.bytedeco.javacpp.opencv_imgcodecs.*;
import org.bytedeco.javacpp.opencv_face.FisherFaceRecognizer;
import org.bytedeco.javacpp.opencv_face.EigenFaceRecognizer;
import org.bytedeco.javacpp.opencv_face.LBPHFaceRecognizer;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Feature.Type;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.protobuf.ByteString;
import com.googlecode.javacpp.IntPointer;

@RestController
public class TestService {
//create marialDB DataSource variable
	private final Path rootLocation = Paths.get("target/classes/static/image");
	private final Path tranLocation = Paths.get("target/classes/static/train");
	
	//Call data from external api using RestTemplate
		/*@SuppressWarnings("unchecked")
		@RequestMapping(value = "/test", method=RequestMethod.GET)
		public ResponseEntity<ResponseClient> test() {
			RestTemplate restTemplate = new RestTemplate();
			//User dt = new User();
			Map<String, Object> dt = restTemplate.getForObject("http://localhost:8090/bcel/api/user//info/1234567890", Map.class);
			System.out.println(dt.get("data"));
			HttpHeaders responseHeaders = new HttpHeaders();		//Create http client header object
			responseHeaders.set("Content-Type", "application/json"); //Set http client type as response json
			return new ResponseEntity<ResponseClient> (new ResponseClient(	//Response data to client if found user and no error
					(boolean) dt.get("success"),
					(String)dt.get("message"),
					dt.get("data")
					),
					responseHeaders, 
					HttpStatus.OK
			);
		}*/
	
	@RequestMapping(value = "/recognic", method=RequestMethod.GET)
	public ResponseEntity<ResponseClient> test() {
		RestTemplate restTemplate = new RestTemplate();
		//User dt = new User();
		Map<String, String> request = new HashMap<String, String>();
		request.put("ctfno", "167");
		request.put("ctfname", "bb");
		request.put("busCode", "checkPerson");
		request.put("verCode", "ver001");
		request.put("orgCode", "0000");
		request.put("ctftype", "0205");
		request.put("channel", "0300");
		request.put("tradingCode", "0601");
		request.put("engineCode", "cyface");
		request.put("dataoneType", "1");
		request.put("netCheckStatus", "0");
		request.put("tradingFlowNO", "35436547");
		request.put("fileDataone", "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");
		/*HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		HttpEntity<String> entity = new HttpEntity<String>(request, headers);	*/
		String dt = restTemplate.postForObject("http://192.168.131.189:8080/ibis/recog/handle", request, String.class);
		System.out.println(dt);
		
		HttpHeaders responseHeaders = new HttpHeaders();		//Create http client header object
		responseHeaders.set("Content-Type", "application/json"); //Set http client type as response json
		return new ResponseEntity<ResponseClient> (new ResponseClient(	//Response data to client if found user and no error
				true,
				"OK recognition success",
				dt
				),
				responseHeaders, 
				HttpStatus.OK
		);
	}
	
	@CrossOrigin("*")
	@RequestMapping(value = "/octet", method=RequestMethod.POST, consumes = "application/json" )
	public ResponseEntity<ResponseClient> octetStream(@RequestBody Map<String, String> data){
		//System.out.println(data.toString());
		byte[] imageByte=Base64.decodeBase64(data.get("fileDataone"));
		System.out.println(rootLocation.toString());
		System.out.println(imageByte.length);
		InputStream myInputStream = new ByteArrayInputStream(imageByte); 
		String imageName = "image"+System.currentTimeMillis()+ ".jpg";
        try {
        	Files.copy(myInputStream, rootLocation.resolve(imageName));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		HttpHeaders responseHeaders = new HttpHeaders();		//Create http client header object
		responseHeaders.set("Content-Type", "application/json"); //Set http client type as response json
		return new ResponseEntity<ResponseClient> (new ResponseClient( //Response error to client while found error
				true,
				imageName,
				data
				),
				responseHeaders, 
				HttpStatus.OK
		);
		
	}
	
	@RequestMapping(value = "/upload", method=RequestMethod.POST, consumes = "multipart/form-data")
	public ResponseEntity<ResponseClient> uploadImageToServer(@RequestParam("image") MultipartFile file){
		String imageName = "";
		String enBase64 = "";
		try {
			System.out.println(file.getSize());
			if(file.getSize() > (100*1024)) {
				BufferedImage image = ImageIO.read(new ByteArrayInputStream(file.getBytes()));
				//BufferedImage resizedImg = resize(image, 300, 300*image.getWidth()/image.getHeight());

				imageName = "compress.jpg";
				File compressedImageFile = new File(rootLocation.resolve(imageName).toString());
			    OutputStream os =new FileOutputStream(compressedImageFile);
			    ImageOutputStream ios = ImageIO.createImageOutputStream(os);
				Iterator<ImageWriter>writers =  ImageIO.getImageWritersByFormatName("jpg");
			    ImageWriter writer = (ImageWriter) writers.next();
			    writer.setOutput(ios);
			    ImageWriteParam param = writer.getDefaultWriteParam();
			    param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
			    param.setCompressionQuality(0.15f);
			    writer.write(null, new IIOImage(image, null, null), param);
			    //System.out.println("Work");
				
			    image = ImageIO.read(rootLocation.resolve(imageName).toFile());
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageIO.write(image, file.getOriginalFilename().split("\\.")[1], baos);
				InputStream myInputStream = new ByteArrayInputStream(baos.toByteArray()); 
				enBase64 = Base64.encodeBase64String(baos.toByteArray());
				System.out.println(baos.size());
				imageName = "image"+System.currentTimeMillis()+ ".jpg";
		        try {
		        	Files.copy(myInputStream, rootLocation.resolve(imageName));
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				/*Iterator<ImageWriter>writers =  ImageIO.getImageWritersByFormatName("jpg");
			    ImageWriter writer = (ImageWriter) writers.next();
			    ImageWriteParam param = writer.getDefaultWriteParam();
			    param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
			    param.setCompressionQuality(0.05f);
			    writer.write(null, new IIOImage(image, null, null), param);
			    writer.dispose();*/
		        os.close();
		        ios.close();
			    writer.dispose();
			}
			
			//BufferedImage image = ImageIO.read(new ByteArrayInputStream(file.getBytes()));
			
			//Save image to folder
			/*imageName = "image"+System.currentTimeMillis()+ "." +file.getOriginalFilename().split("\\.")[1];
			if(!Files.exists(rootLocation)) {
				Files.createDirectory(rootLocation);
			}
			String enBase64 = Base64.encodeBase64String(file.getBytes());
			System.out.println(enBase64);
			Files.copy(file.getInputStream(), this.rootLocation.resolve(imageName));*/
			//Save image to folder
			
		} catch (Exception e) {
			System.out.println(e);
			//e.printStackTrace();
			//throw new RuntimeException("FAIL!");
		}
		HttpHeaders responseHeaders = new HttpHeaders();		//Create http client header object
		responseHeaders.set("Content-Type", "application/json"); //Set http client type as response json
		return new ResponseEntity<ResponseClient> (new ResponseClient( //Response error to client while found error
				false,
				"OK Success",
				enBase64
				),
				responseHeaders, 
				HttpStatus.OK
		);
	}
	
	@RequestMapping(value = "/upload/opencv", method=RequestMethod.POST, consumes = "multipart/form-data")
	public ResponseEntity<ResponseClient> uploadImageToServerAndOpencv(@RequestParam("image") MultipartFile file){
		String imageName = "";
		try {
			//System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
			//System.out.println(file.getSize());
	        String[] nameSplit = file.getOriginalFilename().split("\\.");
			if(file.getSize() > (200*1024)) {
				imageName = "original_img."+nameSplit[nameSplit.length - 1];
				//BufferedImage image = ImageIO.read(new ByteArrayInputStream(file.getBytes()));
				//BufferedImage resizedImg = resize(image, 300, 300*image.getWidth()/image.getHeight());
				if(!Files.exists(rootLocation)) {
					Files.createDirectory(rootLocation);
				}
				BufferedImage image = ImageIO.read(new ByteArrayInputStream(file.getBytes()));
	        	Files.copy(file.getInputStream(), this.rootLocation.resolve(imageName), StandardCopyOption.REPLACE_EXISTING);
				//Mat original = imread(this.rootLocation.resolve(imageName).toString());
				IplImage originalIpl = cvLoadImage(this.rootLocation.resolve(imageName).toString());
				//Mat resizeimage = new Mat();
				//Size sz = new Size(600, 600*image.getHeight()/image.getWidth());
				//resize( original, resizeimage, sz );
				//resize(original, resizeimage, sz);
				 //Mat element = getStructuringElement(original.rows(), new Size(image.getWidth(), image.getHeight()), new Point(image.getWidth(), image.getHeight()) );
				 //morphologyEx(original,resizeimage, MORPH_OPEN, element);
				imageName = "compressed"+System.currentTimeMillis()+ "." +nameSplit[nameSplit.length - 1];
				IplImage iplImg = IplImage.create(300, 300*image.getHeight()/image.getWidth(), originalIpl.depth(), originalIpl.nChannels());
				cvResize(originalIpl, iplImg);
				cvSaveImage(this.rootLocation.resolve(imageName).toString(), iplImg);
				//imwrite(this.rootLocation.resolve(imageName).toString(), resizeimage);
				
				
			}/*else if((file.getSize() < (1024*1024))&&(file.getSize() > (100*1024))) {
				imageName = "original_img."+file.getOriginalFilename().split("\\.")[1];
				//BufferedImage image = ImageIO.read(new ByteArrayInputStream(file.getBytes()));
				//BufferedImage resizedImg = resize(image, 300, 300*image.getWidth()/image.getHeight());
				if(!Files.exists(rootLocation)) {
					Files.createDirectory(rootLocation);
				}
				BufferedImage image = ImageIO.read(new ByteArrayInputStream(file.getBytes()));
	        	Files.copy(file.getInputStream(), this.rootLocation.resolve(imageName), StandardCopyOption.REPLACE_EXISTING);

				Mat original = imread(this.rootLocation.resolve(imageName).toString());
				imageName = "compressed"+System.currentTimeMillis()+ "." +file.getOriginalFilename().split("\\.")[1];
				imwrite(this.rootLocation.resolve(imageName).toString(), original);
			}*/ else {
				if(!Files.exists(rootLocation)) {
					Files.createDirectory(rootLocation);
				}
				imageName = "compressed"+System.currentTimeMillis()+ "." +nameSplit[nameSplit.length - 1];
	        	Files.copy(file.getInputStream(), this.rootLocation.resolve(imageName));
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		HttpHeaders responseHeaders = new HttpHeaders();		//Create http client header object
		responseHeaders.set("Content-Type", "application/json"); //Set http client type as response json
		return new ResponseEntity<ResponseClient> (new ResponseClient( //Response error to client while found error
				true,
				"Response",
				imageName
				),
				responseHeaders, 
				HttpStatus.OK
		);
	}
	
	@RequestMapping(value = "/upload/opencv1", method=RequestMethod.POST, consumes = "multipart/form-data")
	public ResponseEntity<ResponseClient> uploadImageToServerAndOpencv1(@RequestParam("image") MultipartFile file){
		String imageName = "";
		try {
			//System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
			//System.out.println(file.getSize());
	        String[] nameSplit = file.getOriginalFilename().split("\\.");
			if(file.getSize() > (200*1024)) {
				imageName = "original_img."+nameSplit[nameSplit.length - 1];
				//BufferedImage image = ImageIO.read(new ByteArrayInputStream(file.getBytes()));
				//BufferedImage resizedImg = resize(image, 300, 300*image.getWidth()/image.getHeight());
				if(!Files.exists(rootLocation)) {
					Files.createDirectory(rootLocation);
				}
				BufferedImage image = ImageIO.read(new ByteArrayInputStream(file.getBytes()));
				Mat matrix = new OpenCVFrameConverter.ToMat().convert(new Java2DFrameConverter().convert(image));
		        normalize(matrix, matrix, 0, 255, NORM_MINMAX, -1, noArray());
		        matrix.convertTo(matrix, CV_32F);
		        Mat gammaTransformedImage = new Mat(matrix.size(), CV_32F);
		        pow(matrix, 1. / 5, gammaTransformedImage);
		        Retina retina = Retina.create(gammaTransformedImage.size());
		        Mat retinaOutput_parvo = new Mat();
		        Mat retinaOutput_magno = new Mat();
		        retina.clearBuffers();
		        retina.run(gammaTransformedImage);
		        retina.getParvo(retinaOutput_parvo);
		        retina.getMagno(retinaOutput_magno);
				imageName = "compressed"+System.currentTimeMillis()+ "." +nameSplit[nameSplit.length - 1];
				imwrite(this.rootLocation.resolve(imageName).toString(), retinaOutput_magno);
				imwrite(this.rootLocation.resolve(1+imageName).toString(), retinaOutput_parvo);
				
				
			}/*else if((file.getSize() < (1024*1024))&&(file.getSize() > (100*1024))) {
				imageName = "original_img."+file.getOriginalFilename().split("\\.")[1];
				//BufferedImage image = ImageIO.read(new ByteArrayInputStream(file.getBytes()));
				//BufferedImage resizedImg = resize(image, 300, 300*image.getWidth()/image.getHeight());
				if(!Files.exists(rootLocation)) {
					Files.createDirectory(rootLocation);
				}
				BufferedImage image = ImageIO.read(new ByteArrayInputStream(file.getBytes()));
	        	Files.copy(file.getInputStream(), this.rootLocation.resolve(imageName), StandardCopyOption.REPLACE_EXISTING);

				Mat original = imread(this.rootLocation.resolve(imageName).toString());
				imageName = "compressed"+System.currentTimeMillis()+ "." +file.getOriginalFilename().split("\\.")[1];
				imwrite(this.rootLocation.resolve(imageName).toString(), original);
			}*/ else {
				if(!Files.exists(rootLocation)) {
					Files.createDirectory(rootLocation);
				}
				imageName = "compressed"+System.currentTimeMillis()+ "." +nameSplit[nameSplit.length - 1];
	        	Files.copy(file.getInputStream(), this.rootLocation.resolve(imageName));
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		HttpHeaders responseHeaders = new HttpHeaders();		//Create http client header object
		responseHeaders.set("Content-Type", "application/json"); //Set http client type as response json
		return new ResponseEntity<ResponseClient> (new ResponseClient( //Response error to client while found error
				true,
				"Response",
				imageName
				),
				responseHeaders, 
				HttpStatus.OK
		);
	}
	
	@RequestMapping(value = "/upload/ocr", method=RequestMethod.POST, consumes = "multipart/form-data")
	public ResponseEntity<ResponseClient> uploadImageToOCR(@RequestParam("image") MultipartFile file){
		String imageName = "";
		String enBase64 = "";
		Map<String, Object> info = new HashMap<String, Object>();
		try(ImageAnnotatorClient vision = ImageAnnotatorClient.create()) {
			byte[] imageByte = file.getBytes();
			ByteString imgBytes = ByteString.copyFrom(imageByte);
			List<AnnotateImageRequest> requests = new ArrayList<>();
		      com.google.cloud.vision.v1.Image img = com.google.cloud.vision.v1.Image.newBuilder().setContent(imgBytes).build();
		      Feature feat = Feature.newBuilder().setType(Type.LABEL_DETECTION).build();
		      AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
		          .addFeatures(feat)
		          .setImage(img)
		          .build();
		      requests.add(request);
		   // Performs label detection on the image file
		      BatchAnnotateImagesResponse response = vision.batchAnnotateImages(requests);
		      List<AnnotateImageResponse> responses = response.getResponsesList();
		      for (AnnotateImageResponse res : responses) {
		          if (res.hasError()) {
		            System.out.printf("Error: %s\n", res.getError().getMessage());
		            break;
		          }

		          for (EntityAnnotation annotation : res.getLabelAnnotationsList()) {
		            annotation.getAllFields().forEach((k, v) ->
		                System.out.printf("Data %s : %s\n", k, v.toString()));
		          }
		        }
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
		HttpHeaders responseHeaders = new HttpHeaders();		//Create http client header object
		responseHeaders.set("Content-Type", "application/json"); //Set http client type as response json
		return new ResponseEntity<ResponseClient> (new ResponseClient( //Response error to client while found error
				false,
				"OK Success",
				"Response not know error or success"
				),
				responseHeaders, 
				HttpStatus.OK
		);
	}
	
	@RequestMapping(value = "/face/recognition", method=RequestMethod.POST, consumes = "multipart/form-data")
	public ResponseEntity<ResponseClient> faceRecognition(@RequestParam("image") MultipartFile file){
		String imageName = "";
		String ext = FilenameUtils.getExtension(file.getOriginalFilename());
		try {
			
			imageName = "original_img."+file.getOriginalFilename().split("\\.")[1];
			if(!Files.exists(rootLocation)) {
				Files.createDirectory(rootLocation);
			}

			BufferedImage imageResize = ImageIO.read(new ByteArrayInputStream(file.getBytes()));
        	Files.copy(file.getInputStream(), this.rootLocation.resolve(imageName), StandardCopyOption.REPLACE_EXISTING);
        	IplImage originalIpl = cvLoadImage(this.rootLocation.resolve(imageName).toString());
			IplImage iplImg = IplImage.create(100, 100*imageResize.getHeight()/imageResize.getWidth(), originalIpl.depth(), originalIpl.nChannels());
			cvResize(originalIpl, iplImg);
			cvSaveImage(this.rootLocation.resolve(imageName).toString(), iplImg);
			
        	String trainingDir = this.tranLocation.toString();
        	Mat testImage = imread(this.rootLocation.resolve(imageName).toString(), IMREAD_GRAYSCALE);
        	
        	File root = new File(trainingDir);

            FilenameFilter jpgFilter = new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().endsWith(".jpg");
                }
            };
            File[] imageFiles = root.listFiles(jpgFilter);
            MatVector images = new MatVector(imageFiles.length);

            Mat labels = new Mat(imageFiles.length, 1, CV_32SC1);
            IntBuffer labelsBuf = labels.createBuffer();

            int counter = 0;

            for (File image : imageFiles) {
                Mat img = imread(image.getAbsolutePath(), IMREAD_GRAYSCALE);

                int label = Integer.parseInt(image.getName().split("\\-")[0]);

                images.put(counter, img);

                labelsBuf.put(counter, label);

                counter++;
            }

            //FaceRecognizer faceRecognizer = FisherFaceRecognizer.create();
            // FaceRecognizer faceRecognizer = EigenFaceRecognizer.create();
             FaceRecognizer faceRecognizer = LBPHFaceRecognizer.create();
            //System.out.println(faceRecognizer.getDefaultName());
            faceRecognizer.setThreshold(0.1);
            faceRecognizer.train(images, labels);

            org.bytedeco.javacpp.IntPointer label = new org.bytedeco.javacpp.IntPointer(1);
            DoublePointer confidence = new DoublePointer(1);
            faceRecognizer.predict(testImage, label, confidence);
            int predictedLabel = label.get(0);
            

            System.out.println("Predicted label: " + predictedLabel);
            System.out.println("Test new: " + label.get(3));
            
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
		HttpHeaders responseHeaders = new HttpHeaders();		//Create http client header object
		responseHeaders.set("Content-Type", "application/json"); //Set http client type as response json
		return new ResponseEntity<ResponseClient> (new ResponseClient( //Response error to client while found error
				true,
				"OK Success",
				"Response not know error or success"
				),
				responseHeaders, 
				HttpStatus.OK
		);
	}
	
	private BufferedImage resizeImage(BufferedImage img, int height, int width) {
        Image tmp = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resized.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();
        return resized;
    }
	
}
