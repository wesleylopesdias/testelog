package com.logicalis.serviceinsight.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface DocManagementService extends BaseService {

	public ResponseEntity<?> storeS3Contract(MultipartFile fileKey, Long contractId) throws ServiceException;
	public ResponseEntity<?> storeS3ContractUpdate(MultipartFile fileKey, Long contractUpdateId) throws ServiceException;
	public ResponseEntity<?> storeS3ContractUpdate(String fileName, String contentType, byte[] fileContent, Long contractUpdateId) throws ServiceException;
	public ResponseEntity<?> storeS3MultipartFile(String fileName, String contentType, byte[] fileContent, String bucket, String path) throws ServiceException;
	public void deleteS3File(String bucket, String path);
	public void deleteS3Contract(Long contractId) throws ServiceException;
	public void deleteS3ContractUpdate(Long contractUpdateId) throws ServiceException;
	public ResponseEntity<byte[]> retrieveS3File(String bucket, String path);
	public ResponseEntity<String[]> listS3Buckets();
	public ResponseEntity<String[]> listS3Objects(String bucket);
	public String getFileSuffix(String contentType);
	
	public ResponseEntity<byte[]> retrieveS3Contract(String path) throws ServiceException;
	public ResponseEntity<byte[]> retrieveS3ContractUpdate(String path) throws ServiceException;
	
}
