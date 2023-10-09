package com.logicalis.serviceinsight.service;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.logicalis.serviceinsight.data.Contract;
import com.logicalis.serviceinsight.data.ContractUpdate;
import com.logicalis.serviceinsight.data.Customer;
import com.logicalis.serviceinsight.web.RestClient;

@org.springframework.stereotype.Service
public class DocManagementServiceImpl extends BaseServiceImpl implements DocManagementService {
	
	@Value("${awsapis.user}")
    String awsclientUser;
    @Value("${awsapis.password}")
    String awsclientPassword;
    @Value("${awsapis.endpoint}")
    String awsclientEndpoint;
    @Value("${aws.s3.sow.bucket}")
    String sowBucket;
    @Value("${aws.s3.pcr.bucket}")
    String pcrBucket;
    
    @Autowired
    ContractDaoService contractDaoService;
    
    @Transactional(readOnly = false, rollbackFor = ServiceException.class)
    public ResponseEntity<?> storeS3Contract(MultipartFile fileKey, Long contractId) throws ServiceException {
    	Contract contract = contractDaoService.contract(contractId);
    	if(contract == null) {
    		throw new ServiceException(messageSource.getMessage("contract_not_found_for_id", new Object[]{contractId}, LocaleContextHolder.getLocale()));
    	}
    	Customer customer = contractDaoService.customer(contract.getCustomerId());
    	if(customer == null) {
    		throw new ServiceException(messageSource.getMessage("customer_not_found_for_id", new Object[]{contract.getCustomerId()}, LocaleContextHolder.getLocale()));
    	}
    	
    	String path = buildContractDocKey(contract, customer);
    	log.info("path: " + path);
    	//add path to contract record
    	contract.setFilePath(path);
    	contractDaoService.updateContract(contract);
    	
    	String fileName = fileKey.getOriginalFilename();
    	String contentType = fileKey.getContentType();
    	byte[] fileContent = null;
    	
    	try {
    		fileContent = fileKey.getBytes();
    	} catch (IOException ex) {
            log.error("Error storing S3 Fil: " + ex);
            return new ResponseEntity<>(String.format("IOException occurred extracting file bytes: [%]", ex.getMessage()), BAD_REQUEST);
        }
    	
    	return storeS3MultipartFile(fileName, contentType, fileContent, sowBucket, path);
    }
    
    @Transactional(readOnly = false, rollbackFor = ServiceException.class)
    public ResponseEntity<?> storeS3ContractUpdate(MultipartFile fileKey, Long contractUpdateId) throws ServiceException {
    	String fileName = fileKey.getOriginalFilename();
    	String contentType = fileKey.getContentType();
    	byte[] fileContent = null;
    	
    	try {
    		fileContent = fileKey.getBytes();
    	} catch (IOException ex) {
            log.error("Error storing S3 Fil: " + ex);
            return new ResponseEntity<>(String.format("IOException occurred extracting file bytes: [%]", ex.getMessage()), BAD_REQUEST);
        }
    	
    	return storeS3ContractUpdate(fileName, contentType, fileContent, contractUpdateId);
    }
    
    @Transactional(readOnly = false, rollbackFor = ServiceException.class)
    public ResponseEntity<?> storeS3ContractUpdate(String fileName, String contentType, byte[] fileContent, Long contractUpdateId) throws ServiceException {
    	ContractUpdate contractUpdate = contractDaoService.contractUpdate(contractUpdateId);
    	if(contractUpdate == null) {
    		throw new ServiceException(messageSource.getMessage("contract_update_not_found_for_id", new Object[]{contractUpdateId}, LocaleContextHolder.getLocale()));
    	}
    	Contract contract = contractDaoService.contract(contractUpdate.getContractId());
    	if(contract == null) {
    		throw new ServiceException(messageSource.getMessage("contract_not_found_for_id", new Object[]{contractUpdate.getContractId()}, LocaleContextHolder.getLocale()));
    	}
    	Customer customer = contractDaoService.customer(contract.getCustomerId());
    	if(customer == null) {
    		throw new ServiceException(messageSource.getMessage("customer_not_found_for_id", new Object[]{contract.getCustomerId()}, LocaleContextHolder.getLocale()));
    	}
    	
    	String path = buildContractUpdateDocKey(contract, customer, contractUpdate);
    	
    	//add path to contract update record
    	contractUpdate.setFilePath(path);
    	contractDaoService.updateContractUpdate(contractUpdate);
    	
    	
    	
    	return storeS3MultipartFile(fileName, contentType, fileContent, pcrBucket, path);
    }
	
    public ResponseEntity<?> storeS3MultipartFile(String fileName, String contentType, byte[] fileContent, String bucket, String path) throws ServiceException {
    	log.info("about to store in S3: " + awsclientEndpoint);
    	if(fileContent == null) {
    		//throw SE
    	}
    	
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, String> headerMap = new LinkedMultiValueMap<>();
        headerMap.add("Content-disposition", "form-data; name=file; filename=" + fileName);
        headerMap.add("Content-type", contentType);
        HttpEntity<byte[]> doc = null;
        //try {
            doc = new HttpEntity<byte[]>(fileContent, headerMap);
        /*} catch (IOException ex) {
            log.error("Error storing S3 Fil: " + ex);
            return new ResponseEntity<>(String.format("IOException occurred extracting file bytes: [%]", ex.getMessage()), BAD_REQUEST);
        }*/
        MultiValueMap<String, Object> multipartReqMap = new LinkedMultiValueMap<>();
        multipartReqMap.add("file", doc);
        multipartReqMap.add("bucket", bucket);
        multipartReqMap.add("path", path);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(multipartReqMap, headers);
        RestTemplate awsclient = RestClient.authenticatingRestTemplate(awsclientUser, awsclientPassword);
        ResponseEntity<?> response = null;
        try {
	        response = awsclient.postForEntity(awsclientEndpoint + "/s3/store", requestEntity, String.class);
	        log.info("Response: " + response);
        } catch (Exception e) {
        	e.printStackTrace();
        	throw new ServiceException(e.getMessage());
        }
        return response;
    }
    
    public void deleteS3File(String bucket, String path) {
        RestTemplate awsclient = RestClient.authenticatingRestTemplate(awsclientUser, awsclientPassword);
        awsclient.delete(awsclientEndpoint + "/s3/remove?bucket="+bucket+"&path="+path);
    }
    
    public ResponseEntity<byte[]> retrieveS3Contract(String path) throws ServiceException {	
    	return retrieveS3File(sowBucket, path);
    }
    
    public ResponseEntity<byte[]> retrieveS3ContractUpdate(String path) throws ServiceException {
    	return retrieveS3File(pcrBucket, path);
    }
    
    public ResponseEntity<byte[]> retrieveS3File(String bucket, String path) {
        RestTemplate awsclient = RestClient.authenticatingRestTemplate(awsclientUser, awsclientPassword);
        try {
        	return awsclient.getForEntity(awsclientEndpoint + "/s3/retrieve?bucket="+bucket+"&path="+path, byte[].class);
        } catch (Exception e) {
        	e.printStackTrace();
        }
        return null;
    }
    
    public ResponseEntity<String[]> listS3Buckets() {
        RestTemplate awsclient = RestClient.authenticatingRestTemplate(awsclientUser, awsclientPassword);
        return awsclient.getForEntity(awsclientEndpoint + "/s3/buckets", String[].class);
    }
    
    public ResponseEntity<String[]> listS3Objects(String bucket) {
        RestTemplate awsclient = RestClient.authenticatingRestTemplate(awsclientUser, awsclientPassword);
        return awsclient.getForEntity(awsclientEndpoint + "/s3/objects?bucket="+bucket, String[].class);
    }
    
    @Transactional(readOnly = false, rollbackFor = ServiceException.class)
    public void deleteS3Contract(Long contractId) throws ServiceException {
    	Contract contract = contractDaoService.contract(contractId);
    	if(contract == null) {
    		throw new ServiceException(messageSource.getMessage("contract_not_found_for_id", new Object[]{contractId}, LocaleContextHolder.getLocale()));
    	}
    	String path = contract.getFilePath();
    	
    	deleteS3File(sowBucket, path);
    	
    	contract.setFilePath(null);
    	contractDaoService.updateContract(contract);
    }
    
	public void deleteS3ContractUpdate(Long contractUpdateId) throws ServiceException {
		ContractUpdate contractUpdate = contractDaoService.contractUpdate(contractUpdateId);
    	if(contractUpdate == null) {
    		throw new ServiceException(messageSource.getMessage("contract_update_not_found_for_id", new Object[]{contractUpdateId}, LocaleContextHolder.getLocale()));
    	}
    	String path = contractUpdate.getFilePath();
    	log.info("Path to delete: " + path);
    	deleteS3File(pcrBucket, path);
    	
    	contractUpdate.setFilePath(null);
    	contractDaoService.updateContractUpdate(contractUpdate);
	}

    /**
     * there are more sophisticated ways to obtain mappings of contentType to file suffix,
     * but it is unclear (to me) if they are free of negatives or worth it.
     * 
     * @param contentType
     * @return 
     */
    public String getFileSuffix(String contentType) {
        String sfx = ".txt";
        if (contentType != null) {
            if (contentType.contains("msword")) {
                sfx = ".doc";
            } else if (contentType.contains("wordprocessingml")) {
                sfx = ".docx";
            } else if (contentType.contains("png")) {
                sfx = ".png";
            } else if (contentType.contains("jpg") || contentType.contains("jpeg")) {
                sfx = ".jpg";
            } else if (contentType.contains("pdf")) {
                sfx = ".pdf";
            } else if (contentType.contains("powerpoint")) {
                sfx = ".ppt";
            } else if (contentType.contains("presentationml")) {
                sfx = ".pptx";
            } else if (contentType.contains("ms-excel")) {
                sfx = ".xls"; // might be a csv file...
            } else if (contentType.contains("spreadsheetml")) {
                sfx = ".xlsx";
            } else if (contentType.contains("text")) {
                sfx = ".txt";
            }
        }
        return sfx;
    }
    
    private String buildContractDocKey(Contract contract, Customer customer) {
        if (contract == null || customer == null) {
            throw new IllegalArgumentException("Customer and Contract must be present");
        }
        
        String contractId = String.valueOf(contract.getId());
        String customerId = String.valueOf(contract.getId());
        String customerName = customer.getName().replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
        String contractName = contract.getAltId().replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMddyyyy_HHmmss");
        String date = simpleDateFormat.format(new Date());
        
        return "sow_cu" + customerId + "_" + customerName +  "_co" + contractId + "_" + contractName + "_" + date;
    }
    
    private String buildContractUpdateDocKey(Contract contract, Customer customer, ContractUpdate contractUpdate) {
        if (contract == null || customer == null) {
            throw new IllegalArgumentException("Customer and Contract must be present");
        }
        
        String contractId = String.valueOf(contract.getId());
        String customerId = String.valueOf(contract.getId());
        String customerName = customer.getName().replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
        String contractName = contract.getAltId().replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
        String contractUpdateName = contractUpdate.getAltId().replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMddyyyy_HHmmss");
        String date = simpleDateFormat.format(new Date());
        
        return "sow_cu" + customerId + "_" + customerName +  "_co" + contractId + "_" + contractName + "_" + contractUpdateName + "_" + date;
    }
    
}
